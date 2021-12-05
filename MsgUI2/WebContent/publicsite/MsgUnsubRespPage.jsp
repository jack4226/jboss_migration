<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="jpa.msgui.publicsite.messages" var="bndl"/>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link href="./styles.css" rel="stylesheet" type="text/css">
<title><fmt:message key="unsubscribeFromMailingLists" bundle="${bndl}"/></title>
</head>
<noscript><p><b>Javascript must be enabled to use this form.</b></p></noscript>
<body>
<div align="center">

<table width="100%" class="headerMenuContent" style="background: white;" border="0" cellspacing="1" cellpadding="1">
	<tr>
		<td colspan="2">
		<table class="gettingStartedHeader">
			<tr>
				<td><span class="gettingStartedTitle">
				<fmt:message key="unsubscribeConfirmTitle" bundle="${bndl}"/>
				</span></td>
			</tr>
		</table>
		</td>
	</tr>

<%@ include file="./loadSbsrDaos.jsp" %>

<%@page import="jpa.model.EmailAddress"%>
<%@page import="jpa.model.Subscription"%>
<%@page import="jpa.model.UnsubComment"%>
<%@page import="jpa.model.BroadcastMessage"%>
<%@page import="jpa.model.BroadcastTracking"%>
<%@page import="jpa.service.common.UnsubCommentService"%>
<%!
UnsubCommentService unsubCommentsDao = null;
UnsubCommentService getUnsubCommentService(ServletContext ctx) {
	if (unsubCommentsDao == null) {
		unsubCommentsDao = SpringUtil.getWebAppContext(ctx).getBean(UnsubCommentService.class);
	}
	return unsubCommentsDao;
}
%>
<%
	Logger logger = Logger.getLogger("jpa.msgui.publicsite.jsp");
	ServletContext ctx = application;
	
	String sbsrId = request.getParameter("sbsrid"); // subscriber email address id
	String msgId = request.getParameter("msgid"); // broadcast message id
	String listId = request.getParameter("listid");
	String comments = request.getParameter("comments");
	String submit = request.getParameter("submit");
	EmailAddress addrVo = null;
	MailingList listVo = null;
	int unsubscribed = 0;
	String unsubListName = listId;
	try {
		addrVo = getEmailAddressService(ctx).getByRowId(Integer.parseInt(sbsrId));
		listVo = getMailingListService(ctx).getByListId(listId);
		if (submit != null && submit.length() > 0 && addrVo != null && listVo != null) {
	Subscription sub = getSubscriptionService(ctx).unsubscribe(addrVo.getAddress(), listId);
	if (sub != null) {
		MailingList vo = getMailingListService(ctx).getByListId(listId);
		if (vo == null) {
			logger.error("MsgUnsubRespPage.jsp - Failed to find mailing list by Id: " + listId);
		}
		else {
			unsubListName += " - " + vo.getDisplayName();
		}
	}
	pageContext.setAttribute("unsubListName", unsubListName);
	// add user comments
	if (unsubscribed > 0 && comments != null && comments.trim().length() > 0) {
		try {
			UnsubComment commVo = new UnsubComment();
			commVo.setEmailAddress(addrVo);
			commVo.setMailingList(listVo);
			commVo.setComments(comments.trim());
			getUnsubCommentService(ctx).insert(commVo);
			logger.info("MsgUnsubRespPage.jsp - unsubcription commonts added: " + 1);
		}
	 	catch (Exception e) {
	 		logger.error("MsgUnsubRespPage.jsp - add comments: " + e.toString());
	 	}
	}
		}
	}
	catch (Exception e) {
		logger.error("MsgUnsubRespPage.jsp", e);
	}
	
	BroadcastTracking countVo = null;

	java.util.Optional<BroadcastMessage> bmsg = getBroadcastMessageService(ctx).getByRowId(Integer.parseInt(msgId));
	if (bmsg.isEmpty()) {
		logger.error("MsgUnsubPage.jsp - Failed to find broadcast message by Id: " + msgId);
	}
	else {
		bmsg.get().setUnsubscribeCount(bmsg.get().getUnsubscribeCount()+1);
		getBroadcastMessageService(ctx).update(bmsg.get());

		countVo = getBroadcastTrackingService(ctx).getByPrimaryKey(addrVo.getRowId(), bmsg.get().getRowId());
		if (countVo == null) {
	 		logger.error("MsgUnsubPage.jsp - Failed to find broadcast tracking by emailAddrRowId/broadcastMsgRowId: " + sbsrId + "/" + msgId);
	 	}
	}

	if (unsubscribed > 0 && addrVo != null && listVo != null) {
		Map<String, String> listMap = new HashMap<String, String>();
		listMap.put("_UnsubscribedMailingLists", unsubListName);
		//listMap.put("SubscriberAddressId", addrVo.getEmailAddrId());
		getMailingListBo(ctx).send(addrVo.getAddress(), listMap, "UnsubscriptionLetter");
%>
 	<tr>
	 	<td align="center" colspan="2">
		 	<table width="90%" border="0">
			<tr>
				<td colspan="2">
					<img src="./images/space.gif" height="10" style="border: 0px">
				</td>
			</tr>
			<tr>
				<td colspan="2">
					<b><fmt:message key="unsubscribeConfirmLabel" bundle="${bndl}"/></b>
				</td>
			</tr>
			<tr>
				<td colspan="2">
					<img src="./images/space.gif" height="10" style="border: 0px">
				</td>
			</tr>
			</table>
	 	</td>
 	</tr>
	<tr>
		<td style="width: 80%;" colspan="2" align="center">
		<table style="width: 80%; background: #E5F3FF;" class="jsfDataTable" border="1" cellspacing="0" cellpadding="8">
			<tr>
				<th style="width: 100%;">List Name un-subscribed</th>
			</tr>
			<tr>
				<td style="width: 100%;"><%= unsubListName %></td>
			</tr>
		</table>
		</td>
	</tr>
	<tr>
		<td style="width: 80%;" colspan="2" align="center">
		<table style="width: 80%;" border="0" cellspacing="2" cellpadding="2">
			<tr>
				<td>
					<span style="font-weight: bold; font-size: 1.0em;">&nbsp;
					<fmt:message key="subscriberEmailAddress" bundle="${bndl}"/></span>&nbsp;
					<b><%= addrVo == null ? "" : addrVo.getAddress() %></b>
				</td>
			</tr>
		</table>
		</td>
	</tr>
	<tr>
		<td colspan="2" align="center">
		<table style="width: 90%;" border="0" cellspacing="2" cellpadding="2">
			<tr>
				<td><img src="./images/space.gif" height="10" style="border: 0px"></td>
			</tr>
			<tr>
				<td style="footNote">&nbsp;<br/>
				If you did this by mistake and want to edit your user profile or re-subscribe, 
				<a href="<%= renderURLVariable(ctx, "UserProfileURL", sbsrId) %>">click here.</a>
				</td>
			</tr>
			<tr>
				<td><img src="./images/space.gif" height="10" style="border: 0px"></td>
			</tr>
		</table>
		</td>
	</tr>
<%	} else { %>
	<tr>
		<td colspan="2" align="center">
		<table style="width: 90%;" border="0" cellspacing="2" cellpadding="2">
			<tr>
				<td><img src="./images/space.gif" height="10" style="border: 0px"></td>
			</tr>
			<tr>
				<td class="errorMessage" style="font-size: 1.2em;">&nbsp;<br/>
				<% if (addrVo != null && listVo != null) { %>
				You have already un-subscribed from the list.
				<% } else { %>
				The subscriber code or HTTP request is invalid.
				<% } %>
				</td>
			</tr>
			<tr>
				<td><img src="./images/space.gif" height="10" style="border: 0px"></td>
			</tr>
		</table>
		</td>
	</tr>
<%	} %>
	<tr>
		<td>
		<table width="100%" class="commandBar">
			<tr>
				<td style="width: 10%;">&nbsp;</td>
				<td style="width: 90%;" align="left">
					<input type="button" value="Return" onclick="javascript:history.go(-2);">
				</td>
			</tr>
		</table>
		</td>
	</tr>
</table>
</div>
</body>
</html>
