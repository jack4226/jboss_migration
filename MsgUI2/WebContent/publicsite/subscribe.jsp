<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<%@page import="jpa.model.EmailAddress"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="jpa.msgui.publicsite.messages" var="bndl"/>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link href="./styles.css" rel="stylesheet" type="text/css">
<title><fmt:message key="subscribeToMailingLists" bundle="${bndl}"/></title>
<script type="text/javascript">
function checkEmail(myform) {
	var email = document.getElementById('sbsrAddr');
	var regex = /^([a-z0-9\.\_\%\+\-])+\@([a-z0-9\-]+\.)+[a-z0-9]{2,4}$/i;
	if (!regex.test(email.value)) {
		alert('Please provide a valid email address');
		email.focus;
		return false;
	}
	return validateListSelection(myform);
}

function validateListSelection(myform) {
	var lists = document.getElementsByName('chosen');
	//var lists = myform.chosen;
	// count how many boxes have been checked by the reader
	var count = 0;
	for (var j=0; j<lists.length; j++) {
	   if (lists[j].checked) count++;
	}
	if (count == 0) {
		alert("No newsletter was selected, please select at least one and re-submit.");
		return false;
	}
	return true;
}
</script>
</head>
<noscript><p><b>Javascript must be enabled to use this form.</b></p></noscript>
<body>
<script type="text/javascript">
	var histLen = history.length;
	//var histCurr = history.current; 
	//var histPrev = history.previous;
	//var histNext = history.next;
		/* Error from Firefox: Permission denied to get property History.current */
	//document.write("The number of pages visited before this page is " +histLen+ " pages!!!.<br>");
</script>
<div align="center">
<jsp:useBean id="sbsrEditBean"
	class="jpa.msgui.publicsite.SubscriberEditBean" scope="request" />
<jsp:setProperty name="sbsrEditBean" property="*" />

<%@ include file="./loadSbsrDaos.jsp" %>

<form action="subscribeResp.jsp" method="post" onsubmit="return checkEmail(this);">
<input type="hidden" name="frompage" value="<c:out value="${param.frompage}"/>">
<input type="hidden" name="editMode" value="<%= sbsrEditBean.getEditMode() %>">
<input type="hidden" name="listid" value="<%= sbsrEditBean.getListid() %>">
<input type="hidden" name="sbsrid" value="<%= sbsrEditBean.getSbsrid() %>">

<%
	Logger logger = LogManager.getLogger("jpa.msgui.publicsite.jsp");
	//String serverInfo = application.getServerInfo();
	ServletContext ctx = application;
 	
	String emailAddr = "";
	EmailAddress sbsrAddrVo = null;
	try {
 		int emailAddrId = Integer.parseInt(sbsrEditBean.getSbsrid());
		sbsrAddrVo = getEmailAddressService(ctx).getByRowId(emailAddrId);
 		if (sbsrAddrVo == null) {
 			logger.error("subscribe.jsp - Subscriber Id " + emailAddrId + " not found");
 		}
 		else {
			emailAddr = sbsrAddrVo.getAddress();
			pageContext.setAttribute("sbsrAddr", sbsrAddrVo.getAddress());
 		}
 	}
 	catch (NumberFormatException e) {
 		logger.error("subscribe.jsp - " + e.toString());
 	}
 	%>

<table width="100%" class="headerMenuContent" style="background: white;" border="0" cellspacing="0" cellpadding="0">
	<tr>
		<td colspan="2">
		<table class="gettingStartedHeader">
			<tr>
				<td><span class="gettingStartedTitle">
				<fmt:message key="subscriberServiceTitle" bundle="${bndl}"/>
				</span></td>
			</tr>
		</table>
		</td>
	</tr>
 	<tr>
	 	<td align="center" colspan="2">
		 	<table width="90%" border="0">
			<tr>
				<td colspan="2"><img src="./images/space.gif" height="10" style="border: 0px"></td>
			</tr>
			<tr>
				<td colspan="2">
					<b><fmt:message key="selectMailingListsLabel" bundle="${bndl}"/></b>
				</td>
			</tr>
			<tr>
				<td colspan="2"><img src="./images/space.gif" height="10" style="border: 0px"></td>
			</tr>
			<tr>
				<td>
					<c:if test="${null != sbsrAddr}">
						<span class="gridHeader">
						<fmt:message key="subscriberEmailAddress" bundle="${bndl}"/></span>&nbsp;
						<b><%= emailAddr %></b>
						<input type="hidden" name="sbsrAddr" id="sbsrAddr" value="<c:out value="${sbsrAddr}"/>">
					</c:if>
					<c:if test="${null == sbsrAddr}">
						<span class="gridHeader">
						<fmt:message key="enterEmailAddressPrompt" bundle="${bndl}"/></span>&nbsp;
						<input type="text" name="sbsrAddr" id="sbsrAddr" value="" size="50" maxlength="255">
					</c:if>
				</td>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td colspan="2"><img src="./images/space.gif" height="10" style="border: 0px"></td>
			</tr>
			</table>
	 	</td>
 	</tr>
<%
	List<MailingList> subList = null;
	int sbsrId = 0;
 	try {
 		int emailAddrId = Integer.parseInt(sbsrEditBean.getSbsrid());
 		sbsrId = emailAddrId;
		sbsrAddrVo = getEmailAddressService(ctx).getByRowId(emailAddrId);
 		if (sbsrAddrVo == null) {
 			logger.error("subscribe.jsp - Subscriber Id " + emailAddrId + " not found");
 		}
 		else {
			emailAddr = sbsrAddrVo.getAddress();
			pageContext.setAttribute("sbsrAddr", sbsrAddrVo.getAddress());
	 		subList = getSbsrMailingLists(ctx, emailAddr);
 		}
 	}
 	catch (NumberFormatException e) {
 		logger.error("subscribe.jsp - " + e.toString());
 	}
 	if (sbsrAddrVo == null || subList == null) {
		subList = getMailingListService(ctx).getAll(true);
 	}
 	pageContext.setAttribute("subList", subList);
	%>
	<tr>
		<td style="width: 80%;" align="center" colspan="2">
		<table style="width: 80%; background: #E5F3FF;" class="jsfDataTable" border="1" cellspacing="0" cellpadding="8">
			<tr>
				<th style="width: 80%;">List Name</th>
				<th style="width: 20%;">Subscribe</th>
			</tr>
		<c:forEach items="${subList}" var="list" varStatus="rowCounter">
			<tr>
				<td style="width: 80%;"><b>${list.displayName}</b>&nbsp;-&nbsp;${list.description}</td>
				<td style="width: 20%;" align="center">
				<input type="checkbox" name="chosen" value="${list.listId}" <c:if test="${list.isSubscribed}"><c:out value="checked readonly"/></c:if>> 
				</td>
			</tr>
		</c:forEach>
		</table>
		</td>
	</tr>
	<tr>
		<td align="center" colspan="2">
		<table style="width: 80%;" border="0" cellspacing="2" cellpadding="2">
			<tr>
				<td><b>Email Type:&nbsp;</b> 
				<input type="radio" name="emailtype" value="html" checked>&nbsp;HTML&nbsp;&nbsp;&nbsp;&nbsp;
				<input type="radio" name="emailtype" value="text">&nbsp;Text
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
				how we protect your email address.
				</td>
			</tr>
			<tr>
				<td class="footNote">&nbsp;<br/>
				If you subscribed already and want to edit your profile or un-subscribe, 
				<a href="<%= renderURLVariable(ctx, "UserProfileURL", String.valueOf(sbsrId)) %>">click here.</a>
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
					<input type="submit" name="submit" value="Subscribe">&nbsp;
					<input type="button" value="Cancel" onclick="javascript:history.back()">
				</td>
			</tr>
		</table>
		</td>
	</tr>
</table>
<input type="hidden" name="listCount" value="<c:out value="${fn:length(subList)}"/>">
</form>
</div>
</body>
</html>
