package jpa.msgui.servlet;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import jpa.model.msg.MessageAttachment;
import jpa.msgui.util.SpringUtil;
import jpa.service.msgdata.MessageAttachmentService;

/**
 * The File Servlet that serves files from database.
 */
@WebServlet(name="File Servlet", urlPatterns="/file/*", loadOnStartup=9)
public class FileServlet extends HttpServlet {
	private static final long serialVersionUID = -8129545604805974235L;
	static final Logger logger = Logger.getLogger(FileServlet.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	//private MessageInboxService messageDao = null;
	private MessageAttachmentService attachmentsDao = null;
	private String fileNotFoundPage = "/FileNotFoundError.xhtml";
	
	@Override
	public void init() throws ServletException {
		ServletContext ctx = getServletContext();
		logger.info("init() - ServerInfo: " + ctx.getServerInfo() + ", Context Path: "
				+ ctx.getContextPath());
//		messageDao = SpringUtil.getWebAppContext(ctx).getBean(MessageInboxService.class);
		attachmentsDao = SpringUtil.getWebAppContext(ctx).getBean(MessageAttachmentService.class);
	}
	
	@Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

        // Get ID from request
        String id = request.getParameter("id");
        String depth = request.getParameter("depth");
        String seq = request.getParameter("seq");
        logger.info("File Id/Depth/Seq: " + id + "/" + depth + "/" + seq);
        // Check if ID is supplied from the request
        if (id == null || depth == null || seq == null) {
        	logger.error("Missing file ID and/or Depth and/or Seq from request.");
            response.sendRedirect(fileNotFoundPage);
            return;
        }

        // Lookup MessageAttachment by id/depth/seq in database.
        int attchRowId = 0;
        //int attchmntDepth = 0;
        //int attchmntSeq = 0;
        try {
        	attchRowId = Integer.parseInt(id);
        	//attchmntDepth = Integer.parseInt(depth);
        	//attchmntSeq = Integer.parseInt(seq);
        }
        catch (NumberFormatException e) {
        	logger.error("Failed to convert file ID or Depth or Seq to numeric values.");
            response.sendRedirect(fileNotFoundPage);
            return;
        }
        //MessageInbox msgInbox = messageDao.getByRowId(attchRowId);

    	//MessageAttachmentPK pk = new MessageAttachmentPK();
    	//pk.setMessageInbox(msgInbox);
    	//pk.setAttachmentDepth(attchmntDepth);
    	//pk.setAttachmentSequence(attchmntSeq);
		Optional<MessageAttachment> fileData = attachmentsDao.getByRowId(attchRowId); //getByPrimaryKey(pk);
        // Check if file is actually retrieved from database.
        if (fileData.isEmpty()) {
            logger.error("Failed to retrieve file from database.");
            response.sendRedirect(fileNotFoundPage);
            return;
        }
        if (fileData.get().getAttachmentValue() == null) {
        	logger.warn("Empty attachment, key = " + id +"/" + depth + "/" + seq);
        	return;
        }
        BufferedOutputStream output = null;
        try {
            // Get file content
            ByteArrayInputStream input = new ByteArrayInputStream(fileData.get().getAttachmentValue());
            int contentLength = input.available();
            // initialize servlet response.
            response.reset();
            response.setContentLength(contentLength);
            response.setContentType(fileData.get().getAttachmentType());
            response.setHeader("Content-disposition",
                "attachment; filename=\"" + fileData.get().getAttachmentName() + "\"");
            output = new BufferedOutputStream(response.getOutputStream());
            // Write file contents to response
            while (contentLength-- > 0) {
                output.write(input.read());
            }
            output.flush();
        } 
        catch (IOException e) {
        	logger.error("IOException caught", e);
        	throw e;
        }
        finally {
            // make sure to close stream
            if (output != null) {
                try {
                    output.close();
                }
                catch (IOException e) {
                	// This is a serious error
                	logger.error("IOException caught during output.close()", e);
                    throw e;
                }
            }
        }
    }
}