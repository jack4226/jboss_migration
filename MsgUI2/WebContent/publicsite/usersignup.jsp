<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="jpa.msgui.publicsite.messages" var="bndl"/>
<fmt:setBundle basename="jpa.msgui.messages" var="bndl2"/>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link href="./styles.css" rel="stylesheet" type="text/css">
<title><fmt:message key="userSignupTitle" bundle="${bndl}"/></title>
<script type="text/javascript" src="userprofile.js"></script>
</head>
<noscript><p><b>Javascript must be enabled to use this form.</b></p></noscript>
<body>
<div align="center">
<jsp:useBean id="customerBean"
	class="jpa.model.SubscriberData" scope="request" />
<jsp:setProperty name="customerBean" property="*" />

<jsp:useBean id="staticCodes"
	class="jpa.msgui.util.StaticCodes" scope="request" />

<%@page import="jpa.service.maillist.MailingListBo"%>

<%@ include file="./loadSbsrDaos.jsp" %>

<form action="usersignup.jsp" method="post" onsubmit="return validateInputs(this);">
<input type="hidden" name="frompage" value="<c:out value="${param.frompage}"/>">

<%@page import="jpa.model.EmailAddress"%>
<%@page import="jpa.model.SubscriberData"%>
<%@page import="javax.faces.model.SelectItem"%>
<%
	Logger logger = LogManager.getLogger("jpa.service.jsp");
	//String serverInfo = application.getServerInfo();
	ServletContext ctx = application;
	String errorMsg = "";
	String submitButtonText = "Submit";
 	if (submitButtonText.equals(request.getParameter("submit"))) {
 		SubscriberData custVo = getSubscriberDataService(ctx).getByEmailAddress(request.getParameter("emailAddr"));
 		if (custVo != null) {
 			errorMsg = "The email address you entered is already used by another user.";
 		}
 		else {
 			EmailAddress addrVo = getEmailAddressService(ctx).findSertAddress(request.getParameter("emailAddr"));
 			customerBean.setFirstName(request.getParameter("firstName"));
 			customerBean.setLastName(request.getParameter("lastName"));
 			customerBean.setEmailAddress(addrVo);
 			customerBean.setUserPassword(request.getParameter("userPswd"));
 			customerBean.setStreetAddress(blankToNull(request.getParameter("streetAddress")));
 			customerBean.setCityName(blankToNull(request.getParameter("cityName")));
 			customerBean.setStateCode(blankToNull(request.getParameter("stateCode")));
 			customerBean.setZipCode5(blankToNull(request.getParameter("zipCode5")));
 			customerBean.setZipCode4(blankToNull(request.getParameter("zipCode4")));
 			customerBean.setCountry(request.getParameter("countryCode"));
 			customerBean.setSecurityQuestion(request.getParameter("securityQuestion"));
 			customerBean.setSecurityAnswer(request.getParameter("securityAnswer"));
 			getSubscriberDataService(ctx).insert(customerBean);
 			logger.info("usersignup.jsp - customer record inserted: " + 1 + ", email address: " + addrVo.getAddress());
 			pageContext.forward("subscribe.jsp?sbsrid=" + addrVo.getRowId());
 			//response.sendRedirect("subscribe.jsp?sbsrid=" + addrVo.getEmailAddrId());
 			//return;
 		}
 	}
 	boolean hasError = errorMsg.trim().length() > 0;
 	%>

<table width="100%" class="headerMenuContent" style="background: white;" border="0" cellspacing="0" cellpadding="0">
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
		 	<table width="80%" border="0" class="inputTable">
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
						value="<%= hasError?request.getParameter("firstName"):"" %>">
				</td>
			</tr>
			<tr>
				<td class="promptColumn">
					<fmt:message key="lastNamePrompt" bundle="${bndl2}"/>
					<span style="color: red;">*</span>
				</td>
				<td class="inputColumn">
					<input type="text" name="lastName" id="lastName" maxlength="32" size="32"
						value="<%= hasError?request.getParameter("lastName"):"" %>">
				</td>
			</tr>
			<tr>
				<td class="promptColumn">
					<fmt:message key="emailAddrPrompt" bundle="${bndl2}"/>
					<span style="color: red;">*</span>
				</td>
				<td class="inputColumn">
					<input type="text" name="emailAddr" id="emailAddr" maxlength="255" size="50"
						value="<%= hasError?request.getParameter("emailAddr"):"" %>">
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
						value="<%= hasError?request.getParameter("userPswd"):"" %>">
				</td>
			</tr>
			<tr>
				<td class="promptColumn">
					<fmt:message key="userReenterPasswordPrompt" bundle="${bndl}"/>
					<span style="color: red;">*</span>
				</td>
				<td class="inputColumn">
					<input type="password" name="userPswd2" id="userPswd2" maxlength="32" size="32"
						value="<%= hasError?request.getParameter("userPswd2"):"" %>">
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
						value="<%= hasError?request.getParameter("streetAddress"):"" %>">
				</td>
			</tr>
			<tr>
				<td class="promptColumn">
					<fmt:message key="cityNamePrompt" bundle="${bndl2}"/>
				</td>
				<td class="inputColumn">
					<input type="text" name="cityName" id="cityName" maxlength="32" size="32"
						value="<%= hasError?request.getParameter("cityName"):"" %>">
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
						%>
						<option value="<%= stateCode %>" <% if(stateCode.equals(request.getParameter("stateCode"))) out.print("selected");%>><%= codeMap.get(stateCode) %></option>
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
						value="<%= hasError?request.getParameter("zipCode5"):"" %>">
					<input type="text" name="zipCode4" id="zipCode4" maxlength="4" size="4"
						value="<%= hasError?request.getParameter("zipCode4"):"" %>">
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
						%>
						<option value="<%= country.getValue() %>" <% if(country.getValue().equals(request.getParameter("countryCode"))) out.print("selected");%>><%= country.getLabel() %></option>
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
						%>
						<option value="<%= question.getValue() %>" <% if(question.getValue().equals(request.getParameter("securityQuestion"))) out.print("selected");%>><%= question.getLabel() %></option>
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
						value="<%= hasError?request.getParameter("securityAnswer"):"" %>">
				</td>
			</tr>
			
			<tr>
				<td colspan="2"><img src="./images/space.gif" height="10" style="border: 0px"></td>
			</tr>
		<% if (errorMsg.trim().length() > 0) { %>
			<tr>
				<td colspan="2"><span class="errorMessage"><%= errorMsg %></span></td>
			</tr>
		<% } %>
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
