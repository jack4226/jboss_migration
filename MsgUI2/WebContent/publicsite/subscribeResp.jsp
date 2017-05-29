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
<title><fmt:message key="subscribeToMailingLists" bundle="${bndl}"/></title>
</head>
<noscript><p><b>Javascript must be enabled to use this form.</b></p></noscript>
<body>
<div align="center">

<%@ include file="./loadSbsrDaos.jsp" %>

<table width="100%" class="headerMenuContent" style="background: white;" border="0" cellspacing="1" cellpadding="1">
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
				<td colspan="2">
					<img src="./images/space.gif" height="10" style="border: 0px">
				</td>
			</tr>
			<tr>
				<td colspan="2">
					<b><fmt:message key="subscribeResponseLabel" bundle="${bndl}"/></b>
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
<%@page import="jpa.message.util.MsgIdCipher"%>
<%@page import="jpa.model.EmailAddress"%>
<%
	Logger logger = Logger.getLogger("jpa.msgui.publicsite.jsp");
	ServletContext ctx = application;
	
	String emailAddr = request.getParameter("sbsrAddr");
	String emailType = request.getParameter("emailtype");
	int sbsrId = 0;
	List<MailingList> subList = null;
	String submit = request.getParameter("submit");
	if (submit != null && submit.length() > 0) { // submit button pressed
		List<String> subedList = new ArrayList<String>();
		StringBuffer sbListNames = new StringBuffer();
		StringBuffer sbListIds = new StringBuffer();
		String[] chosens = request.getParameterValues("chosen");
		for (int i=0; i<chosens.length; i++) {
			String listId = chosens[i];
			try {
				if (listId != null && listId.length() > 0) {
					if (i > 0) {
						sbListIds.append(",");
					}
					sbListIds.append(listId);
					getSubscriptionService(ctx).optInRequest(emailAddr, listId);
					MailingList vo = getMailingListService(ctx).getByListId(listId);
					if (vo != null) {
						subedList.add(listId + " - " + vo.getDisplayName());
						if (i > 0) {
							sbListNames.append(" \n");
						}
						sbListNames.append(vo.getDisplayName());
					}
					else {
						logger.error("subscribeResp.jsp - Failed to find mailing list by Id: " + listId);
					}
				}
			}
			catch (NumberFormatException e) {
				logger.error("subscribeResp.jsp - " + e.toString());
			}
 		}
		EmailAddress addrVo = getEmailAddressService(ctx).getByAddress(emailAddr);
		if (chosens.length > 0 && addrVo != null) {
			// update "AcceptHTML" flag if needed
			boolean acceptHtml = true;
			if ("text".equals(emailType)) {
				acceptHtml = false;
			}
			if (acceptHtml != addrVo.isAcceptHtml()) {
				addrVo.setAcceptHtml(acceptHtml);
				getEmailAddressService(ctx).update(addrVo);
			}
			// send confirmation email
			Map<String, String> listMap = new HashMap<String, String>();
			listMap.put("_RequestedMailingLists", sbListNames.toString());
			String sbsrIdEncoded = MsgIdCipher.encode(addrVo.getRowId());
			sbsrId = addrVo.getRowId();
			listMap.put("_EncodedSubcriberId", sbsrIdEncoded);
			listMap.put("_SubscribedListIds", sbListIds.toString());
			getMailingListBo(ctx).send(emailAddr, listMap, "SubscriptionConfirmation");
		}
		pageContext.setAttribute("subedList", subedList);
	%>
	<tr>
		<td style="width: 80%;" colspan="2" align="center">
		<table style="width: 80%; background: #E5F3FF;" class="jsfDataTable" border="1" cellspacing="0" cellpadding="8">
			<tr>
				<th style="width: 100%;">List Name</th>
			</tr>
		<% for (int i = 0; i < subedList.size(); i++) { %>
			<tr>
				<td style="width: 100%;"><%= subedList.get(i) %></td>
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
					<span class="gridHeader">&nbsp;
					<fmt:message key="subscriberEmailAddress" bundle="${bndl}"/></span>&nbsp;
					<b><%= emailAddr %></b>
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
				<td style="footNote">
				To prevent third parties from subscribing you to the newsletters against your 
				will, an email message with a confirmation code will be sent to the email address
				you provided. Simply wait for this message to arrive, and then follow the 
				instructions to confirm the operation.
				</td>
			</tr>
			<tr>
				<td style="footNote">&nbsp;<br/>
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
<%	} else {
		logger.error("subscribeResp.jsp - Request was not posted through \"submit\" button");
	} %>
</table>
</div>
</body>
</html>
