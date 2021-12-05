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

<%@ include file="./loadSbsrDaos.jsp" %>

<%@page import="jpa.message.util.MsgIdCipher"%>
<%@page import="jpa.model.EmailAddress"%>
<%@page import="jpa.service.common.UnsubCommentService"%>
<%@page import="jpa.model.UnsubComment"%>
<%@page import="jpa.model.Subscription"%>
<%!
UnsubCommentService unsubCommentsDao = null;
UnsubCommentService getUnsubCommentsDao(ServletContext ctx) {
	if (unsubCommentsDao == null) {
		unsubCommentsDao = SpringUtil.getWebAppContext(ctx).getBean(UnsubCommentService.class);
	}
	return unsubCommentsDao;
}
%>
<%
	Logger logger = LogManager.getLogger("jpa.msgui.publicsite.jsp");
	ServletContext ctx = application;
	
	String encodedSbsrId = request.getParameter("sbsrid");
	//String enteredEmailAddr = request.getParameter("sbsrAddr");
	String comments = request.getParameter("comments");
	List<String> unsubedList = new ArrayList<String>();
	int unsubscribed = 0;
	EmailAddress addrVo = null;
	StringBuffer sbListNames = new StringBuffer();
	int sbsrId = 0;
	try {
		sbsrId = MsgIdCipher.decode(encodedSbsrId);
		addrVo = getEmailAddressService(ctx).getByRowId(sbsrId);
		String submit = request.getParameter("submit");
		if (submit != null && submit.length() > 0 && addrVo != null) {
	String[] chosens = request.getParameterValues("chosen");
	for (int i=0; i<chosens.length; i++) {
		String listId = chosens[i];
		Subscription sub = getSubscriptionService(ctx).unsubscribe(addrVo.getAddress(), listId);
		if (sub != null) {
			MailingList vo = getMailingListService(ctx).getByListId(listId);
			if (vo == null) {
				logger.error("unsubscribeResp.jsp - Failed to find mailing list by Id: " + listId);
			}
			else {
				unsubedList.add(listId + " - " + vo.getDisplayName());
				if (unsubscribed > 0) {
					sbListNames.append(" \n");
				}
				sbListNames.append(vo.getDisplayName());
			}
		}
		unsubscribed += 1;
	}
	pageContext.setAttribute("subedList", unsubedList);
	// add user comments
	if (unsubscribed > 0 && comments != null && comments.trim().length() > 0) {
		try {
			UnsubComment commVo = new UnsubComment();
			commVo.setEmailAddress(addrVo);
			if (unsubedList.size() > 0) {
				String unsubed = (String) unsubedList.get(0);
				String unsubdListId = unsubed.substring(0, unsubed.indexOf(" "));
				MailingList vo = getMailingListService(ctx).getByListId(unsubdListId);
				if (vo != null) {
					commVo.setMailingList(vo);
					commVo.setComments(comments.trim());
					getUnsubCommentsDao(ctx).insert(commVo);
					logger.info("unsubscribeResp.jsp - unsubcription commonts added: " + 1);
				}
			}
		}
	 	catch (Exception e) {
	 		logger.error("unsubscribeResp.jsp - add comments: " + e.toString());
	 	}
	}
		}
	}
	catch (Exception e) {
		logger.error("unsubscribeResp.jsp", e);
	}
%>

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
<%
	if (unsubscribed > 0 && addrVo != null) {
		Map<String, String> listMap = new HashMap<String, String>();
		listMap.put("_UnsubscribedMailingLists", sbListNames.toString());
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
				<th style="width: 100%;">List Name</th>
			</tr>
		<% for (int i = 0; i < unsubedList.size(); i++) { %>
			<tr>
				<td style="width: 100%;"><%= unsubedList.get(i) %></td>
			</tr>
		<%	} %>
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
				<a href="<%= renderURLVariable(ctx, "UserProfileURL", String.valueOf(sbsrId)) %>">click here.</a>
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
				<% if (addrVo != null) {
				%>
				You have already un-subscribed from the lists.
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
