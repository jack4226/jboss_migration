<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>

<%@ include file="./loadSbsrDaos.jsp" %>

<%@page import="jpa.model.EmailAddress"%>
<%@page import="jpa.model.BroadcastMessage"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>

<%
	Logger logger = LogManager.getLogger("jpa.msgui.publicsite.jsp");
	ServletContext ctx = application;
	String sbsrId = request.getParameter("sbsrid");
	String listId = request.getParameter("listid");
	int rowsUpdated = 0;
	if (StringUtils.isNotBlank(sbsrId) && StringUtils.isNotBlank(listId)) {
		// update subscriber click count
		EmailAddress addrVo = getEmailAddressService(ctx).getByRowId(Integer.parseInt(sbsrId));
		if (addrVo == null) {
			logger.error("msgopen.jsp - Failed to find email address by id: " + sbsrId);
		}
		else {
			rowsUpdated += getSubscriptionService(ctx).updateOpenCount(addrVo.getRowId(), listId);
		}
	}
	else {
		logger.info("msgopen.jsp - sbsrid or listid is not valued.");
	}

	String msgId = request.getParameter("msgid");
	if (StringUtils.isNotBlank(msgId)) {
		// update newsletter click count
		try {
			java.util.Optional<BroadcastMessage> bm = getBroadcastMessageService(ctx).getByRowId(Integer.parseInt(msgId));
			if (bm.isEmpty()) {
				logger.error("msgunsub.jsp - Failed to find broadcase message by id: " + msgId);
			}
			else {
				rowsUpdated += getBroadcastMessageService(ctx).updateOpenCount(bm.get().getRowId());
			}
		}
		catch (NumberFormatException e) {
			logger.error("NumberFormatException caught: " + e.getMessage());
		}
	}
	else {
		logger.info("msgopen.jsp - msgid is not valued.");
	}
	
	logger.info("msgopen.jsp - rows updated: " + rowsUpdated);

	boolean isInfoEnabled = logger.isInfoEnabled();
	if (isInfoEnabled) {
		logger.info("ServletContext RealPath: " + application.getRealPath("./"));
		logger.info("Request Servlet path: " + request.getServletPath());
		logger.info("Request Context path: " + request.getContextPath());
	}
	
	out.println("<H2>Records Updated: " + rowsUpdated + "</H2>");
	response.setStatus(200);
	response.setContentType("text/html");
	response.flushBuffer();
	%>

<%-- Now serve the space.gif file
<%@ include file="./serveImage.jsp" %>
 --%>