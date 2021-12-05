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
<title><fmt:message key="referralPageTitle" bundle="${bndl}"/></title>
<script type="text/javascript">
function validateInputs(myform) {
	var yourName = document.getElementById('yourName');
	if (yourName.value <= '') {
		alert("Please provide your name.");
		yourName.focus();
		return false;
	}
	
	var regex = /^([a-z0-9\.\_\%\+\-])+\@([a-z0-9\-]+\.)+[a-z0-9]{2,4}$/i;
	var yourEmail = document.getElementById('yourEmail');
	if (!regex.test(yourEmail.value)) {
		alert('Please provide a valid email address');
		yourEmail.focus();
		return false;
	}
	
	var rcptEmail = document.getElementById('rcptEmail');
	if (!regex.test(rcptEmail.value)) {
		alert('Please provide a valid email address');
		rcptEmail.focus();
		return false;
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

<form action="referral.jsp" method="post" onsubmit="return validateInputs(this);">
<input type="hidden" name="frompage" value="<c:out value="${param.frompage}"/>">
<input type="hidden" name="msgid" value="<%= request.getParameter("msgid") %>">
<input type="hidden" name="sbsrid" value="<%= request.getParameter("sbsrid") %>"/>
<input type="hidden" name="listid" value="<%= request.getParameter("listid") %>"/>

<%@page import="jpa.model.EmailAddress"%>
<%@page import="jpa.util.StringUtil"%>
<%@page import="jpa.constant.Constants"%>
<%@page import="jpa.constant.EmailAddrType"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%
	Logger logger = LogManager.getLogger("jpa.msgui.publicsite.jsp");
	//String serverInfo = application.getServerInfo();
	ServletContext ctx = application;
 	
	String errorMsg = "";
	String submitButtonText = "Send";
 	if (submitButtonText.equals(request.getParameter("submit"))) {
		String rcptEmail = request.getParameter("rcptEmail");
		EmailAddress addrVo = getEmailAddressService(ctx).findSertAddress(rcptEmail);
		// update accept HTML flag if changed
		boolean acceptHtml = "html".equals(request.getParameter("emailtype"))?true:false;
		if (acceptHtml != addrVo.isAcceptHtml()) {
			getEmailAddressService(ctx).updateAcceptHtml(addrVo.getRowId(), acceptHtml);
			logger.info("referral.jsp - Accept HTML flag changed to: " + acceptHtml);
		}
		String yourName = request.getParameter("yourName");
		String yourEmail = request.getParameter("yourEmail");
		String comments = request.getParameter("comments");
		String sendCopy = request.getParameter("sendCopy");
	 	// construct email notification
		Map<String, String> listMap = new HashMap<String, String>();
	 	listMap.put("_ReferrerName", yourName);
	 	listMap.put("_ReferrerEmailAddress", yourEmail);
	 	listMap.put("_FriendsEmailAddress", rcptEmail);
	 	if (!StringUtils.isEmpty(comments)) {
	 		listMap.put("_ReferrerComments", yourName + " wrote: " + comments + "<p/>\n");
	 	}
	 	else {
	 		listMap.put("_ReferrerComments", "");
	 	}
	 	if ("yes".equals(sendCopy)) {
	 		listMap.put(EmailAddrType.CC_ADDR.getValue(), yourEmail);
	 	}
	 	// send the notification email off
		getMailingListBo(ctx).send(rcptEmail, listMap, "TellAFriendLetter");
		logger.info("referral.jsp - Referral letter sent to: " + rcptEmail);
		// update referral count
		String msgId = request.getParameter("msgid");
		if (!StringUtils.isEmpty(msgId)) {
			try {
				int rowsUpdated = getBroadcastMessageService(ctx).updateReferalCount(Integer.parseInt(msgId));
				logger.info("referralResp.jsp - updated MsgClickCounts: " + rowsUpdated);
			}
			catch (Exception e) {
				logger.error("referralResp.jsp", e);
			}
		}
		// forward to response page
		pageContext.forward("referralResp.jsp?sbsrid=" + request.getParameter("sbsrid"));
 	}
 	%>

<table width="100%" class="headerMenuContent" style="background: white;" border="0" cellspacing="0" cellpadding="0">
	<tr>
		<td colspan="2">
		<table class="gettingStartedHeader">
			<tr>
				<td><span class="gettingStartedTitle">
				<fmt:message key="tellFriendThisSite" bundle="${bndl}"/>
				</span></td>
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
				<td colspan="2">
					<b><fmt:message key="tellFriendPageLabel" bundle="${bndl}"/></b>
				</td>
			</tr>
			<tr>
				<td colspan="2"><img src="./images/space.gif" height="10" style="border: 0px"></td>
			</tr>
			</table>
			<table width="70%" border="0">
			<tr>
				<td class="promptColumnWhite">
					<fmt:message key="yourNamePrompt" bundle="${bndl}"/>
				</td>
				<td class="inputColumn">
					<input type="text" name="yourName" id="yourName" maxlength="50" size="40"
						value="<%= nullToBlank(request.getParameter("yourName")) %>">
				</td>
			</tr>
			<tr>
				<td class="promptColumnWhite">
					<fmt:message key="yourEmailPrompt" bundle="${bndl}"/>
				</td>
				<td class="inputColumn">
					<input type="text" name="yourEmail" id="yourEmail" maxlength="255" size="50"
						value="<%= nullToBlank(request.getParameter("yourEmail")) %>">
				</td>
			</tr>
			<tr>
				<td class="promptColumnWhite">
					<fmt:message key="recipientEmailPrompt" bundle="${bndl}"/>
				</td>
				<td class="inputColumn">
					<input type="text" name="rcptEmail" id="rcptEmail" maxlength="255" size="50"
						value="<%= nullToBlank(request.getParameter("rcptEmail")) %>">
				</td>
			</tr>
			<tr>
				<td colspan="2"><img src="./images/space.gif" height="10" style="border: 0px"></td>
			</tr>
			<tr>
				<td class="promptColumnWhite">
					<fmt:message key="sendMeCopyPrompt" bundle="${bndl}"/>
				</td>
				<td class="inputColumn">
					<input type="checkbox" name="sendCopy" id="sendCopy" value="yes">
				</td>
			</tr>
			<tr>
				<td class="promptColumnWhite" valign="top">
					<fmt:message key="referralCommentsPrompt" bundle="${bndl}"/><br/>
					<font size="0.8em" style="font-weight: normal">not required</font>
				</td>
				<td class="inputColumn">
					<textarea name="comments" id="comments" cols="60" rows="6" onchange="javacript:checkLength(this, 500);"></textarea>
				</td>
			</tr>
			<tr>
				<td class="promptColumnWhite"><b>Email Type:</b></td>
				<td class="inputColumn">
					<input type="radio" name="emailtype" value="html" checked>&nbsp;HTML&nbsp;&nbsp;&nbsp;&nbsp;
					<input type="radio" name="emailtype" value="text">&nbsp;Text
				</td>
			</tr>
			<tr>
				<td colspan="2"><img src="./images/space.gif" height="10" style="border: 0px"></td>
			</tr>
			</table>
	 	</td>
 	</tr>
	<tr>
		<td align="center" colspan="2">
		<table style="width: 80%;" border="0" cellspacing="2" cellpadding="2">
			<tr>
				<td>The email that will be sent to will contain your name &amp; email address.
				</td>
			</tr>
		</table>
		</td>
	</tr>
	<tr>
		<td align="center" colspan="2">
		<table style="width: 90%;" border="0" cellspacing="2" cellpadding="2">
			<tr>
				<td><img src="./images/space.gif" height="10" style="border: 0px"></td>
			</tr>
			<tr>
				<td class="finePrint">
				We respect your right to privacy and will not sell your email address to other 
				companies based on your subscription.<br/> 
				Please see our <a href="privacypolicy.jsp">privacy policy</a> for information on 
				how we protect your individual information. 
				If you have any questions, contact us.
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
					<input type="submit" name="submit" value="<%= submitButtonText %>">&nbsp;
					<input type="button" value="Cancel" onclick="javascript:history.back()">
				</td>
			</tr>
		</table>
		</td>
	</tr>
</table>
</form>
</div>
</body>
</html>
