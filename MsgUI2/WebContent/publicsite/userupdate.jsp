<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%
// set cookie
if ("yes".equals(request.getParameter("remember"))) {
	Object sbsrAddrId = request.getSession().getAttribute("sbsrId");
	if (sbsrAddrId != null && sbsrAddrId instanceof Long) {
		Cookie cookie = new Cookie("sbsraddrid", sbsrAddrId.toString());
		cookie.setMaxAge(365 * 24 * 60 * 60); // one year
		response.addCookie(cookie);
	}
}
%>
<html>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="jpa.msgui.publicsite.messages" var="bndl"/>
<fmt:setBundle basename="jpa.msgui.messages" var="bndl2"/>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link href="./styles.css" rel="stylesheet" type="text/css">
<title><fmt:message key="userUpdateTitle" bundle="${bndl}"/></title>
<script type="text/javascript" src="userprofile.js"></script>
</head>
<noscript><p><b>Javascript must be enabled to use this form.</b></p></noscript>
<body>
<div align="center">
<jsp:useBean id="staticCodes"
	class="jpa.msgui.util.StaticCodes" scope="request" />

<%@page import="jpa.service.maillist.MailingListBo"%>

<%@ include file="./loadSbsrDaos.jsp" %>

<form action="userupdate.jsp" method="post" onsubmit="return validateInputs(this);">
<input type="hidden" name="frompage" value="<c:out value="${param.frompage}"/>">

<%@page import="jpa.model.EmailAddress"%>
<%@page import="javax.faces.model.SelectItem"%>
<%@page import="jpa.model.SubscriberData"%>
<%@page import="jpa.util.StringUtil"%>
<%@page import="jpa.message.util.MsgIdCipher"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%
	Logger logger = Logger.getLogger("jpa.msgui.publicsite.jsp");
	//String serverInfo = application.getServerInfo();
	ServletContext ctx = application;
 	
	// get subscriber email address id from session
	Object loggedin = request.getSession().getAttribute("sbsrId");
	if (loggedin == null || !(loggedin instanceof Long)) {
		response.sendRedirect("userprofile.jsp?timeout=yes"); // session timed out
		return;
	}

	Long sbsrIdLong = (Long) loggedin;
	int emailAddrId = sbsrIdLong.intValue();
	EmailAddress sbsrAddrVo = getEmailAddressService(ctx).getByRowId(emailAddrId);
	if (sbsrAddrVo == null) {
		logger.error("userupdate.jsp - Subscriber email address Id " + emailAddrId + " not found");
		response.sendRedirect("userupdate.jsp?error=sbsrnotfound");
		return;
	}
	SubscriberData customerBean = getSubscriberDataService(ctx).getByEmailAddress(sbsrAddrVo.getAddress());
	if (customerBean == null) {
		logger.error("userupdate.jsp - Customer not found by " + sbsrAddrVo.getAddress());
		response.sendRedirect("userupdate.jsp?error=usernotfound");
		return;
	}
	
	String errorMsg = "";
	String submitButtonText = "Submit";
 	if (submitButtonText.equals(request.getParameter("submit"))) {
 		SubscriberData custVo = getSubscriberDataService(ctx).getByEmailAddress(request.getParameter("emailAddr"));
 		if (custVo != null && custVo.getRowId() != customerBean.getRowId()) {
 			errorMsg = "The email address you entered is already used by another user.";
 		}
 		else {
 			EmailAddress addrVo = getEmailAddressService(ctx).findSertAddress(request.getParameter("emailAddr"));
 			// update accept HTML flag if changed
 			boolean acceptHtml = "html".equals(request.getParameter("emailtype"))?true:false;
 			if (acceptHtml != addrVo.isAcceptHtml()) {
 				getEmailAddressService(ctx).updateAcceptHtml(addrVo.getRowId(), acceptHtml);
 				logger.info("userupdate.jsp - Accept HTML flag changed to: " + acceptHtml);
 			}
 			customerBean.setFirstName(request.getParameter("firstName"));
 			customerBean.setLastName(request.getParameter("lastName"));
 			boolean emailAddrChanged = false;
 			if (!customerBean.getEmailAddress().getAddress().equalsIgnoreCase(request.getParameter("emailAddr"))) {
 				//customerBean.setPrevEmailAddr(customerBean.getEmailAddr());
 				emailAddrChanged = true;
 				// construct email confirmation for new email address
 				Map<String, String> listMap = new HashMap<String, String>();
 				String encodedSbsrId = MsgIdCipher.encode(addrVo.getRowId());
 				listMap.put("_EncodedSubcriberId", encodedSbsrId);
 				StringBuffer subedList = new StringBuffer();
 	 			try {
 		 			int listCount = Integer.parseInt(request.getParameter("listCount"));
 					for (int i=1; i<=listCount; i++) {
 						String listId = request.getParameter("chosen_" + i);
 						if (listId.startsWith("1_")) {
							if (i > 0) {
								subedList.append(",");
							}
							subedList.append(listId.substring(2));
 						}
 					}
 	 			}
 	 			catch (Exception e) {
 	 				logger.error("Failed to process post data: ", e);
 	 			}
 	 			listMap.put("_SubscribedListIds", subedList.toString());
 	 			// send the email off
 				getMailingListBo(ctx).send(request.getParameter("emailAddr"), listMap, "EmailAddressChangeLetter");
				logger.info("userupdate.jsp - Email Address change letter sent to: " + request.getParameter("emailAddr"));
 			}
 			customerBean.setEmailAddress(addrVo);
 			if (!request.getParameter("userPswd").equals(customerBean.getUserPassword())) {
 				java.sql.Timestamp ts = new java.sql.Timestamp(new Date().getTime());
 				customerBean.setPasswordChangeTime(ts);
 			}
 			customerBean.setUserPassword(request.getParameter("userPswd"));
 			customerBean.setStreetAddress(blankToNull(request.getParameter("streetAddress")));
 			customerBean.setCityName(blankToNull(request.getParameter("cityName")));
 			customerBean.setStateCode(blankToNull(request.getParameter("stateCode")));
 			customerBean.setZipCode5(blankToNull(request.getParameter("zipCode5")));
 			customerBean.setZipCode4(blankToNull(request.getParameter("zipCode4")));
 			customerBean.setCountry(request.getParameter("countryCode"));
 			customerBean.setSecurityQuestion(request.getParameter("securityQuestion"));
 			customerBean.setSecurityAnswer(request.getParameter("securityAnswer"));
 			getSubscriberDataService(ctx).update(customerBean);
 			logger.info("userupdate.jsp - customer record inserted: " + 1 + ", email address: " + addrVo.getAddress());
 			
 			// update subscriptions
 			StringBuffer subedNames = new StringBuffer();
 			try {
	 			int listCount = Integer.parseInt(request.getParameter("listCount"));
	 			int subscribed = 0;
				int unsubscribed = 0;
				for (int i=1; i<=listCount; i++) {
					String listId = request.getParameter("chosen_" + i);
					if (listId.startsWith("1_")) {
						if (emailAddrChanged) {
							getSubscriptionService(ctx).optInRequest(addrVo.getAddress(), listId.substring(2));
							subscribed += 1;
							logger.info("userupdate.jsp - opt-in'ed to: " + listId.substring(2));
						}
						else {
							getSubscriptionService(ctx).subscribe(addrVo.getAddress(), listId.substring(2));
							subscribed += 1;
							logger.info("userupdate.jsp - subscribed to: " + listId.substring(2));
						}
						MailingList vo = getMailingListService(ctx).getByListId(listId.substring(2));
						if (vo != null) {
							if (i > 0) {
								subedNames.append(" \n");
							}
							subedNames.append(vo.getListId() + " - " + vo.getDisplayName());
						}
						else {
							logger.error("userupdate.jsp - Failed to find mailing list by Id: " + listId.substring(2));
						}
					}
					else if (listId.startsWith("2_")) {
						getSubscriptionService(ctx).unsubscribe(addrVo.getAddress(), listId.substring(2));
						unsubscribed += 1;
						logger.info("userupdate.jsp - unsubscribed from: " + listId.substring(2));
					}
				}
 			}
 			catch (Exception e) {
 				logger.error("Failed to process post data: ", e);
 			}
 			
		 	// construct email notification
			Map<String, String> listMap = new HashMap<String, String>();
		 	listMap.put("_SubscribedMailingLists", subedNames.toString());
		 	// construct user data section
		 	StringBuffer userData = new StringBuffer();
		 	userData.append("First Name:     " + customerBean.getFirstName() + "\n");
		 	userData.append("Last Name:      " + customerBean.getLastName() + "\n");
		 	userData.append("Email Address:  " + customerBean.getEmailAddress() + "\n");
		 	if (!StringUtils.isEmpty(customerBean.getStreetAddress())) {
			 	userData.append("Street Address: " + customerBean.getStreetAddress()+ "\n");
			 	userData.append("City:           " + nullToBlank(customerBean.getCityName()) + "\n");
			 	if (!StringUtils.isEmpty(customerBean.getStateCode())) {
			 		Map<String, String> stateMap = staticCodes.getStateCodeMap();
			 		String stateName = (String) stateMap.get(customerBean.getStateCode());
				 	userData.append("State:          " + stateName + "\n");
			 	}
			 	userData.append("Zip:            " + nullToBlank(customerBean.getZipCode5()));
			 	if (!StringUtils.isEmpty(customerBean.getZipCode4())) {
			 		userData.append(" - " + customerBean.getZipCode5() + "\n");
			 	}
			 	else {
			 		userData.append("\n");
			 	}
		 	}
		 	Map<String, String> countryMap = staticCodes.getCountryCodeMap();
		 	String countryName = (String) countryMap.get(customerBean.getCountry());
		 	userData.append("Country:        " + countryName + "\n");
		 	userData.append("Accept HTML?    " + acceptHtml + "\n");
		 	listMap.put("_UserProfileData", userData.toString());
		 	// send the notification email off
			getMailingListBo(ctx).send(request.getParameter("emailAddr"), listMap, "UserProfileChangeLetter");
			logger.info("userupdate.jsp - User Profile change letter sent to: " + request.getParameter("emailAddr"));
			// forward to response page
 			pageContext.forward("userupdateResp.jsp?sbsrid=" + addrVo.getRowId());
 		}
 	}
 	boolean hasError = errorMsg.trim().length() > 0;
 	%>

<table class="headerMenuContent" style="background: white; width: 100%;">
	<tr>
		<td colspan="2">
		<table class="gettingStartedHeader">
			<tr>
				<td><span class="gettingStartedTitle">
				<fmt:message key="userProfileDetails" bundle="${bndl}"/>
				</span></td>
			</tr>
		</table>
		</td>
	</tr>
 	<tr>
	 	<td align="center" colspan="2">
		 	<table style="width: 80%;">
			<tr>
				<td colspan="2"><img src="./images/space.gif" height="10" style="border: 0px"></td>
			</tr>
			<tr>
				<td colspan="2">
					<b><fmt:message key="userProfileUpdateLabel" bundle="${bndl}"/></b>
				</td>
			</tr>
			<tr>
				<td colspan="2"><img src="./images/space.gif" height="10" style="border: 0px"></td>
			</tr>

			<tr>
				<td class="promptColumn">
					<fmt:message key="firstNamePrompt" bundle="${bndl2}"/>
					<span style="color: red;">*</span>
				</td>
				<td class="inputColumn">
					<input type="text" name="firstName" id="firstName" maxlength="32" size="32"
						value="<%= hasError?request.getParameter("firstName"):customerBean.getFirstName() %>">
				</td>
			</tr>
			<tr>
				<td class="promptColumn">
					<fmt:message key="lastNamePrompt" bundle="${bndl2}"/>
					<span style="color: red;">*</span>
				</td>
				<td class="inputColumn">
					<input type="text" name="lastName" id="lastName" maxlength="32" size="32"
						value="<%= hasError?request.getParameter("lastName"):customerBean.getLastName() %>">
				</td>
			</tr>
			<tr>
				<td class="promptColumn">
					<fmt:message key="emailAddrPrompt" bundle="${bndl2}"/>
					<span style="color: red;">*</span>
				</td>
				<td class="inputColumn">
					<input type="text" name="emailAddr" id="emailAddr" maxlength="255" size="50"
						value="<%= hasError?request.getParameter("emailAddr"):customerBean.getEmailAddress() %>">
				</td>
			</tr>
			<tr>
				<td colspan="2"><img src="./images/space.gif" height="10" style="border: 0px"></td>
			</tr>
			<tr>
				<td class="promptColumn">
					<fmt:message key="userPasswordPrompt" bundle="${bndl}"/>
					<span style="color: red;">*</span>
				</td>
				<td class="inputColumn">
					<input type="password" name="userPswd" id="userPswd" maxlength="32" size="32"
						value="<%= hasError?request.getParameter("userPswd"):nullToBlank(customerBean.getUserPassword()) %>">
				</td>
			</tr>
			<tr>
				<td class="promptColumn">
					<fmt:message key="userReenterPasswordPrompt" bundle="${bndl}"/>
					<span style="color: red;">*</span>
				</td>
				<td class="inputColumn">
					<input type="password" name="userPswd2" id="userPswd2" maxlength="32" size="32"
						value="<%= hasError?request.getParameter("userPswd2"):nullToBlank(customerBean.getUserPassword()) %>">
				</td>
			</tr>
			<tr>
				<td colspan="2"><img src="./images/space.gif" height="10" style="border: 0px"></td>
			</tr>
			<tr>
				<td class="promptColumn">
					<fmt:message key="streetAddressPrompt" bundle="${bndl2}"/>
				</td>
				<td class="inputColumn">
					<input type="text" name="streetAddress" id="streetAddress" maxlength="40" size="40"
						value="<%= hasError?request.getParameter("streetAddress"):nullToBlank(customerBean.getStreetAddress()) %>">
				</td>
			</tr>
			<tr>
				<td class="promptColumn">
					<fmt:message key="cityNamePrompt" bundle="${bndl2}"/>
				</td>
				<td class="inputColumn">
					<input type="text" name="cityName" id="cityName" maxlength="32" size="32"
						value="<%= hasError?request.getParameter("cityName"):nullToBlank(customerBean.getCityName()) %>">
				</td>
			</tr>
			<tr>
				<td class="promptColumn">
					<fmt:message key="stateCodePrompt" bundle="${bndl2}"/>
				</td>
				<td class="inputColumn">
					<select name="stateCode" id="stateCode">
						<option value="">Select one</option>
					<% Map<String, String> codeMap = staticCodes.getStateCodeMap(); 
						Set<String> codeSet = codeMap.keySet();
						for (Iterator<String> it=codeSet.iterator(); it.hasNext(); ) {
							String stateCode = (String) it.next();
							String stateCodeSelected = hasError ? request.getParameter("stateCode") : customerBean.getStateCode();
						%>
						<option value="<%= stateCode %>" <% if(stateCode.equals(stateCodeSelected)) out.print("selected");%>><%= codeMap.get(stateCode) %></option>
					<%	} %>
					</select>
				</td>
			</tr>
			<tr>
				<td class="promptColumn">
					<fmt:message key="zipCodePrompt" bundle="${bndl2}"/>
				</td>
				<td class="inputColumn">
					<input type="text" name="zipCode5" id="zipCode5" maxlength="5" size="5"
						value="<%= hasError?request.getParameter("zipCode5"):nullToBlank(customerBean.getZipCode5()) %>">
					<input type="text" name="zipCode4" id="zipCode4" maxlength="4" size="4"
						value="<%= hasError?request.getParameter("zipCode4"):nullToBlank(customerBean.getZipCode4()) %>">
				</td>
			</tr>
			<tr>
				<td class="promptColumn">
					<fmt:message key="countryPrompt" bundle="${bndl2}"/>
					<span style="color: red;">*</span>
				</td>
				<td class="inputColumn">
					<select name="countryCode" id="countryCode">
						<option value="">Select one</option>
					<% SelectItem[] countrys = staticCodes.getCountryCodeItems(); 
						for (int i = 0; i < countrys.length; i++) {
							SelectItem country = countrys[i];
							String countrySelected = hasError?request.getParameter("countryCode"):customerBean.getCountry();
						%>
						<option value="<%= country.getValue() %>" <% if(country.getValue().equals(countrySelected)) out.print("selected");%>><%= country.getLabel() %></option>
					<%	} %>
					</select>
				</td>
			</tr>
			<tr>
				<td colspan="2"><img src="./images/space.gif" height="10" style="border: 0px"></td>
			</tr>
			<tr>
				<td class="promptColumn">
					<fmt:message key="securityQuestionPrompt" bundle="${bndl2}"/>
					<span style="color: red;">*</span>
				</td>
				<td class="inputColumn">
					<select name="securityQuestion" id="securityQuestion">
						<option value="">Select one</option>
					<% SelectItem[] questions = staticCodes.getSecurityQuestionItems(); 
						for (int i = 0; i < questions.length; i++) {
							SelectItem question = questions[i];
							String questionSelected = hasError?request.getParameter("securityQuestion"):customerBean.getSecurityQuestion();
						%>
						<option value="<%= question.getValue() %>" <% if(question.getValue().equals(questionSelected)) out.print("selected");%>><%= question.getLabel() %></option>
					<%	} %>
					</select>
				</td>
			</tr>
			<tr>
				<td class="promptColumn">
					<fmt:message key="securityAnswerPrompt" bundle="${bndl2}"/>
					<span style="color: red;">*</span>
				</td>
				<td class="inputColumn">
					<input type="text" name="securityAnswer" id="securityAnswer" maxlength="26" size="26"
						value="<%= hasError?request.getParameter("securityAnswer"):customerBean.getSecurityAnswer() %>">
				</td>
			</tr>
			
			<tr>
				<td colspan="2"><img src="./images/space.gif" height="10" style="border: 0px"></td>
			</tr>
			<tr>
				<td colspan="2">
					<b>Newsletters you currently subscribed. If you want to change your subscriptions, simply check the 
					"Unsubscribe" radio button to unsubscribe, and check the "Subscribe" radio button to subscribe.</b>
				</td>
			</tr>
			<tr>
				<td colspan="2"><img src="./images/space.gif" height="10" style="border: 0px"></td>
			</tr>
		</table>
<%
	pageContext.setAttribute("sbsrAddr", sbsrAddrVo.getAddress());
	List<MailingList> subList = getSbsrMailingLists(ctx, sbsrAddrVo.getAddress());
 	pageContext.setAttribute("subList", subList);
	%>
	<tr>
		<td style="width: 80%;" align="center" colspan="2">
		<table style="width: 80%; background: #E5F3FF;" class="jsfDataTable" border="1">
			<tr>
				<th style="width: 80%;">List Name</th>
				<th style="width: 20%;">Subscribe</th>
			</tr>
		<c:forEach items="${subList}" var="list" varStatus="rowCounter">
			<tr>
				<td style="width: 80%;"><b>${list.displayName}</b>&nbsp;-&nbsp;${list.description}</td>
				<td style="width: 20%;" align="center">
				<input type="radio" name="chosen_<c:out value="${rowCounter.count}"/>" value="1_${list.listId}" <c:if test="${list.isSubscribed}"><c:out value="checked"/></c:if>> 
				<input type="radio" name="chosen_<c:out value="${rowCounter.count}"/>" value="2_${list.listId}" <c:if test="${!list.isSubscribed}"><c:out value="checked"/></c:if>> 
				</td>
			</tr>
		</c:forEach>
		</table>
		</td>
	</tr>
	<tr>
		<td align="center" colspan="2">
		<table style="width: 80%;">
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
		<table style="width: 90%;">
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
<input type="hidden" name="listCount" value="<c:out value="${fn:length(subList)}"/>">
</form>
</div>
</body>
</html>
