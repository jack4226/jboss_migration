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
<script type="text/javascript">
function checkEmail(myform) {
	var email = document.getElementById('sbsrAddr');
	var regex = /^([a-z0-9\.\_\%\+\-])+\@([a-z0-9\-]+\.)+[a-z0-9]{2,4}$/i;
	if (!regex.test(email.value)) {
		alert('Please provide a valid email address');
		email.focus;
		return false;
	}
	return validateSbsrAddress(myform);
}

function validateSbsrAddress(myform) {
	var realSbsrAddr = document.getElementById('realSbsrAddr');
	var sbsrAddr = document.getElementById('sbsrAddr');
	if (realSbsrAddr.value > '') {
		if (realSbsrAddr.value != sbsrAddr.value) {
			alert("Email address entered does not match your subscription.");
			return false;
		}
	}
	return true;
}

function checkLength(element, maxvalue) {
	var lenEntered = element.value.length;
	var reduce = lenEntered - maxvalue;
	var msg = "Sorry, you have entered " + lenEntered +  " characters into the "+
		"text area box you just completed. The System can save no more than " +
		maxvalue + " characters to the database. Please abbreviate " +
		"your text by at least " + reduce + " characters.";
	if (lenEntered > maxvalue) {
		alert(msg);
		return false;
	}
	return true;
}
</script>
</head>
<noscript><p><b>Javascript must be enabled to use this form.</b></p></noscript>
<body>

<div align="center">

<%@ include file="./loadSbsrDaos.jsp" %>

<form action="MsgUnsubRespPage.jsp" method="post" onsubmit="return checkEmail(this);">
<input type="hidden" name="frompage" value="<c:out value="${param.frompage}"/>">
<input type="hidden" name="sbsrid" value="<%= request.getParameter("sbsrid") %>">
<input type="hidden" name="listid" value="<%= request.getParameter("listid") %>">
<input type="hidden" name="msgid" value="<%= request.getParameter("msgid") %>">

<table width="100%" class="headerMenuContent" style="background: white;" border="0" cellspacing="0" cellpadding="0">
	<tr>
		<td colspan="2">
		<table class="gettingStartedHeader">
			<tr>
				<td><span class="gettingStartedTitle">
				<fmt:message key="unsubscribeServiceTitle" bundle="${bndl}"/>
				</span></td>
			</tr>
		</table>
		</td>
	</tr>
<%@page import="jpa.model.EmailAddress"%>
<%@page import="jpa.model.BroadcastMessage"%>
<%@page import="jpa.model.BroadcastTracking"%>
<%@page import="jpa.service.common.UnsubCommentService"%>
<%
	Logger logger = Logger.getLogger("jpa.msgui.publicsite.jsp");
	ServletContext ctx = application;
 	
	String sbsrId = request.getParameter("sbsrid"); // subscriber email address id
	EmailAddress addrVo = getEmailAddressService(ctx).getByRowId(Integer.parseInt(sbsrId));
 	if (addrVo == null) {
 		logger.error("MsgUnsubPage.jsp - Subscriber Id (email address) " + sbsrId + " not found");
 	}

	String listId = request.getParameter("listid");
	String listName = listId;
	String listDesc = "";
	MailingList listVo = null;
	try {
		if (listId != null && listId.trim().length() > 0) {
			listVo = getMailingListService(ctx).getByListId(listId);
			if (listVo == null) {
				logger.error("MsgUnsubPage.jsp - Failed to find mailing list by Id: " + listId);
			}
			else {
				listName = listVo.getDisplayName();
				listDesc = " - " + listVo.getDescription();
			}
 		}
 		else {
 			logger.error("MsgUnsubPage.jsp - Mailing List Id " + listId + " is blank");
 		}
 	}
 	catch (Exception e) {
 		logger.error("MsgUnsubPage.jsp - listid: " + e.toString());
 	}
	pageContext.setAttribute("listName", listName);
	pageContext.setAttribute("listDesc", listDesc);

	String msgId = request.getParameter("msgid"); // broadcast message id
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
 	
 	if (addrVo != null && listVo != null) {
%>
 	<tr>
	 	<td align="center" colspan="2">
		 	<table width="90%" border="0">
			<tr>
				<td colspan="2"><img src="./images/space.gif" height="10" style="border: 0px"></td>
			</tr>
			<tr>
				<td colspan="2">
					<b><fmt:message key="unsubscribeConfirmPrompt" bundle="${bndl}"/></b>
				</td>
			</tr>
			<tr>
				<td colspan="2"><img src="./images/space.gif" height="10" style="border: 0px"></td>
			</tr>
			</table>
	 	</td>
 	</tr>
	<tr>
		<td style="width: 80%;" align="center" colspan="2">
		<table style="width: 80%; background: #E5F3FF;" class="jsfDataTable" border="1" cellspacing="0" cellpadding="8">
			<tr>
				<th style="width: 80%;">List name to be un-subscribed:</th>
			</tr>
			<tr>
				<td style="width: 80%;"><b>${listName}</b>${listDesc}</td>
			</tr>
		</table>
		</td>
	</tr>
 	<tr>
	 	<td align="center" colspan="2">
		 	<table width="80%" border="0">
			<tr>
				<td colspan="2"><img src="./images/space.gif" height="10" style="border: 0px"></td>
			</tr>
			<tr>
				<td>
					<span style="font-weight: bold; font-size: 1.0em;">
					<fmt:message key="enterEmailAddressPrompt" bundle="${bndl}"/></span>&nbsp;
					<input type="text" name="sbsrAddr" id="sbsrAddr" value="" size="50" maxlength="255">
				</td>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td colspan="2"><img src="./images/space.gif" height="10" style="border: 0px"></td>
			</tr>
			</table>
		 	<table width="80%" border="0">
			<tr valign="top">
				<td width="18%" valign="top" align="left">
					<span style="font-size: 1.0em; font-weight: bold;">Comments:</span><br/>
					<span style="font-size: 0.8em;">not required</span>
				</td>
				<td width="82%" align="left">
				<textarea rows="6" cols="80" name="comments" onchange="javacript:checkLength(this, 500);"></textarea>
				</td>
			</tr>
			<tr>
				<td colspan="2"><img src="./images/space.gif" height="10" style="border: 0px"></td>
			</tr>
			</table>
	 	</td>
 	</tr>
	<tr>
		<td colspan="2" align="center">
		<table style="width: 100%;" class="commandBar">
			<tr>
				<td width="10%">&nbsp;</td>
				<td width="90%" align="left">
					<input type="submit" name="submit" value="Unsubscribe">&nbsp;
					<input type="button" value="Cancel" onclick="javascript:history.back();">
				</td>
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
				<% if (addrVo == null) { %>
				Subscriber code is blank or invalid.
				<% } else if (listVo == null) { %>
				Mailing List code is blank or invalid.
				<% } else { %>
				Invalide HTTP Request.
				<% } %>
				</td>
			</tr>
			<tr>
				<td><img src="./images/space.gif" height="10" style="border: 0px"></td>
			</tr>
		</table>
		</td>
	</tr>
	<tr>
		<td colspan="2" align="center">
		<table style="width: 100%;" class="commandBar">
			<tr>
				<td width="10%">&nbsp;</td>
				<td width="90%" align="left">
					<input type="button" value="Return" onclick="javascript:history.back();">
				</td>
			</tr>
		</table>
		</td>
	</tr>
<%	} %>
</table>
<input type="hidden" id="realSbsrAddr" value="<%= addrVo == null ? "" : addrVo.getAddress() %>">
</form>
</div>
</body>
</html>
