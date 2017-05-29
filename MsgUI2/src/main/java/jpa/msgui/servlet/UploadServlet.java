package jpa.msgui.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jpa.model.SessionUpload;
import jpa.model.SessionUploadPK;
import jpa.model.Subscription;
import jpa.model.UserData;
import jpa.msgui.bean.FileUploadForm;
import jpa.msgui.bean.MailingListComposeBean;
import jpa.msgui.bean.MessageInboxBean;
import jpa.msgui.filter.MultipartFilter;
import jpa.msgui.filter.SessionTimeoutFilter;
import jpa.msgui.util.FacesUtil;
import jpa.msgui.util.SpringUtil;
import jpa.service.common.SessionUploadService;
import jpa.service.common.SubscriptionService;
import jpa.util.EmailAddrUtil;
import jpa.util.HtmlUtil;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

@WebServlet(name="Upload Servlet", urlPatterns="/upload/uploadServlet", loadOnStartup=10)
public class UploadServlet extends HttpServlet {
	private static final long serialVersionUID = -4905340132022275056L;
	static final Logger logger = Logger.getLogger(UploadServlet.class);
	
	private SessionUploadService sessionUploadDao = null;
	private SubscriptionService subscriptionDao = null;
	
	@Override
	public void init() throws ServletException {
		ServletContext ctx = getServletContext();
		logger.info("init() - ServerInfo: " + ctx.getServerInfo() + ", Context Name: "
				+ ctx.getServletContextName());
		sessionUploadDao = SpringUtil.getWebAppContext(ctx).getBean(SessionUploadService.class);
		subscriptionDao = SpringUtil.getWebAppContext(ctx).getBean(SubscriptionService.class);
		// initialize unread counts
//		MessageInboxService msgInboxDao = SpringUtil.getWebAppContext(ctx).getBean(MessageInboxService.class);
//		int initInboxCount = msgInboxDao.resetInboxUnreadCount(); // TODO
//		int initSentCount = msgInboxDao.resetSentUnreadCount(); // TODO
//		logger.info("init() - InboxUnreadCount = " + initInboxCount + ", SentUnreadCount = "
//				+ initSentCount);
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String fromPage = request.getParameter("frompage");
		
		// Do nothing, just show the form
		forward(request, response, fromPage);
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String action = request.getParameter("submit");
		String fromPage = request.getParameter("frompage");
		if ("Cancel".equals(action) || "Done".equals(action)) {
			// submitted from "Cancel" or "Done" button, return to calling page.
			redirect(request, response, action, fromPage);
			return;
		}
		// Prepare upload bean
		FileUploadForm uploadForm = new FileUploadForm();
		
		if ("Import From List".equals(action)) {
			importEmailsFromList(request, uploadForm);
		}
		else { // upload file(s)
			// calculate total file size
			long size = getTotalFileSize(request, uploadForm);
			if (MultipartFilter.getTotalFileSize() > 0 && size > MultipartFilter.getTotalFileSize()) {
				uploadForm.setError("size", "Total file size exceeded maximum amount of "
						+ MultipartFilter.getTotalFileSize() + " bytes.");
			}
			// Process request
			for (int i = 1; i <= 10; i++) {
				size += process(request, uploadForm, i);
			}
		}
		// Store bean in the request
		request.setAttribute("uploadForm", uploadForm);
		// Post back
		if (uploadForm.hasErrors() || uploadForm.hasMessages()) {
			// redisplay the same page
			forward(request, response, fromPage);
		}
		else { // redirect to previous page (msgInboxSend.jsp or ...)
			redirect(request, response, action, fromPage);
		}
	}
	
	private void redirect(HttpServletRequest request, HttpServletResponse response, String action,
			String fromPage) throws IOException {
		String targetPage = "/msgInboxList.xhtml";
		if ("msgreply".equals(fromPage)) {
			targetPage = "/msgInboxSend.xhtml";
		}
		else if ("mailinglist".equals(fromPage)) {
			targetPage = "/mailingListCompose.xhtml";
		}
		else if ("uploademails".equals(fromPage)) {
			targetPage = "/main.xhtml";
		}
		
		if ("uploademails".equals(fromPage) || "Cancel".equals(action)) {
			redirectOnly(request, response, targetPage);
		}
		else {
			redirectWithUpload(request, response, targetPage);
		}
	}

	private void redirectOnly(HttpServletRequest request, HttpServletResponse response,
			String targetPage) throws IOException {
		String url = request.getContextPath() + targetPage;
		response.sendRedirect(response.encodeRedirectURL(url));
	}
	
	private void redirectWithUpload(HttpServletRequest request, HttpServletResponse response,
			String targetPage) throws IOException {
		String fromPage = request.getParameter("frompage");
		FacesContext facesContext = FacesUtil.getFacesContext(request, response);
		if ("mailinglist".equals(fromPage)) { // from mailing list compose
			// 1) retrieve MailingListComposeBean instance from faces context
			MailingListComposeBean bean = (MailingListComposeBean) facesContext.getELContext()
					.getELResolver().getValue(facesContext.getELContext(), null, "mailingListCompose");
			// 2) populate "uploads" list that contains uploaded files
			bean.retrieveUploadFiles();
		}
		else { // from message reply
			// 1) retrieve MsgInboxBean instance from faces context
			MessageInboxBean bean = (MessageInboxBean) facesContext.getELContext().getELResolver()
					.getValue(facesContext.getELContext(), null, "messageInbox");
			// 2) populate "uploads" list that contains uploaded files
			bean.retrieveUploadFiles();
		}
		response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + targetPage));
	}
	
	private long getTotalFileSize(HttpServletRequest request, FileUploadForm uploadForm) {
		long size = 0;
		for (int i = 0; i <= 10; i++) {
			Object fileObject = request.getAttribute("file" + i);
			if (fileObject != null && fileObject instanceof FileItem) {
				FileItem item = (FileItem) fileObject;
				size += item.getSize();
				uploadForm.setPathes("file" + i, item.getName());
			}
		}
		return size;
	}
	
	private long process(HttpServletRequest request, FileUploadForm uploadForm, int fileSeq) {
	    // Validate file
	    Object fileObject = request.getAttribute("file" + fileSeq);
	    long fileSize = 0;
	    if (fileObject == null) {
	    	if (fileSeq == 1) {
		        // No file uploaded
		        uploadForm.setError("file" + fileSeq, "Please select a file to upload.");
	    	}
	    } else if (fileObject instanceof FileUploadException) {
	        // File upload failed
	        FileUploadException fileUploadException = (FileUploadException) fileObject;
	        uploadForm.setError("file" + fileSeq, fileUploadException.getMessage());
	    }
	    // If there are no errors, proceed with writing file
	    if (!uploadForm.hasErrors() && fileObject != null) {
	        FileItem fileItem = (FileItem) fileObject;
	        fileSize = fileItem.getSize();
	        try {
	        	if ("uploademails".equals(request.getParameter("frompage"))) {
	        		// upload to mailing list subscription table
	        		uploadEmailsToList(request, uploadForm, fileSeq, fileItem);
	        	}
	        	else {
	        		uploadToSessionTable(request, uploadForm, fileSeq, fileItem);
	        	}
	        } catch (Exception e) {
	            // Can be thrown from uniqueFile() and FileItem#write()
	            uploadForm.setError("file" + fileSeq, e.getMessage());
	            logger.error("Exception caught", e);
	        }
	    }
	    return fileSize;
	}
	
	private void uploadToSessionTable(HttpServletRequest request, FileUploadForm uploadForm,
			int fileSeq, FileItem fileItem) throws IOException {
        String fileName = FilenameUtils.getName(fileItem.getName());
        String contentType = fileItem.getContentType();
    	String sessionId = request.getRequestedSessionId();
    	SessionUpload sessVo = new SessionUpload();
    	SessionUploadPK pk = new SessionUploadPK();
    	sessVo.setSessionUploadPK(pk);
    	sessVo.getSessionUploadPK().setSessionSequence(fileSeq); // ignored by insertLast() method
    	sessVo.getSessionUploadPK().setSessionId(sessionId);
    	HttpSession session = request.getSession();
        UserData userVo = (UserData) session.getAttribute(SessionTimeoutFilter.USER_DATA_ID);
        if (userVo != null) {
        	sessVo.setUserData(userVo);
        }
        else {
        	logger.warn("process() - UserData not found in httpSession!");
        }
    	sessVo.setFileName(fileName);
    	sessVo.setContentType(contentType);
    	InputStream is = fileItem.getInputStream();
    	sessVo.setSessionValue(IOUtils.toByteArray(is));
        // Write uploaded file to database
    	sessionUploadDao.insertLast(sessVo);
    	logger.info("process() - rows inserted: " + 1);
        uploadForm.setMessage(fileName, "File succesfully uploaded.");		
	}
	
	private void uploadEmailsToList(HttpServletRequest request, FileUploadForm uploadForm,
			int fileSeq, FileItem fileItem) throws IOException {
		String listId = request.getParameter("listid");
		if (StringUtils.isBlank(listId)) {
			logger.error("uploadEmailsToList() - listid parameter was not valued.");
			uploadForm.setError("Contact Support!", "'listid' request parameter was not valued");
			return;
		}
		InputStream is = fileItem.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		int rowsAdded = 0;
		int rowsInvalid = 0;
		String addr = null;
		while ((addr = br.readLine()) != null) {
			if (EmailAddrUtil.isRemoteEmailAddress(addr)) {
				subscriptionDao.subscribe(addr, listId);
				rowsAdded++;
			}
			else {
				rowsInvalid ++;
				if (HtmlUtil.isHTML(addr)) {
					addr = "Address contains HTML tags, suppressed.";
				}
				uploadForm.setError("Invalid Address " + rowsInvalid, addr);
			}
		}
		uploadForm.setMessage("Number of email addresses added to list", rowsAdded + "");
		logger.info("uploadEmailsToList() - number of addrs added to " + listId + ": " + rowsAdded);
		if (rowsInvalid > 0) {
			//uploadForm.setError("Number of invalid addresses", rowsInvalid + "");
			logger.warn("uploadEmailsToList() - number of invalid addrs: " + rowsInvalid);
		}
	}
	
	private void importEmailsFromList(HttpServletRequest request, FileUploadForm uploadForm)
			throws IOException {
		String listId = request.getParameter("listid");
		if (StringUtils.isBlank(listId)) {
			logger.error("uploadEmailsToList() - listid parameter was not valued.");
			uploadForm.setError("Contact Support!", "'listid' request parameter was not valued");
			return;
		}
		String fromListId = request.getParameter("fromlistid");
		if (StringUtils.isBlank(fromListId)) {
			logger.error("uploadEmailsToList() - fromlistid parameter was not valued.");
			uploadForm.setError("Contact Support!", "'fromlistid' request parameter was not valued");
			return;
		}
		if (listId.equals(fromListId)) {
			logger.error("uploadEmailsToList() - fromlistid and listid are same.");
			uploadForm.setError("Import from list", "should be different from \"Import to\" list.");
			return;
		}
		int rowsSubed = 0;
		List<Subscription> fromList = subscriptionDao.getByListId(fromListId);
		for (int i = 0; i < fromList.size(); i++) {
			Subscription subed = fromList.get(i);
			subscriptionDao.subscribe(subed.getEmailAddress().getAddress(), listId);
			rowsSubed++;
		}
		uploadForm.setMessage("Number of email addresses imported", "" + rowsSubed);
		logger.info("importEmailsFromList() - number of addresses imported from " + fromListId
				+ " to " + listId + ": " + rowsSubed);
	}
	
	private void forward(HttpServletRequest request, HttpServletResponse response, String fromPage)
			throws ServletException, IOException {
		if ("uploademails".equals(fromPage)) {
			request.getRequestDispatcher("emailAddrAttachFile.jsp").forward(request, response);
		}
		else {
			request.getRequestDispatcher("msgInboxAttachFiles.jsp").forward(request, response);
		}
	}
}
