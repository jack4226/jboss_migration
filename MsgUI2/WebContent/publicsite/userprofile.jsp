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
<title><fmt:message key="userProfileDetailsTitle" bundle="${bndl}"/></title>
<script type="text/javascript">
function checkInput(myform) {
	var userId = document.getElementById('userid');
	if (userId.value <= '') {
		alert("You must enter a user id.");
		userid.focus();
		return false;
	}
	return true;
}
</script>
</head>
<noscript><p><b>Javascript must be enabled to use this form.</b></p></noscript>
<body>
<div align="center">

<%@page import="jpa.service.maillist.MailingListBo"%>

<%@ include file="./loadSbsrDaos.jsp" %>

<form action="userprofile.jsp" method="post" onsubmit="return checkInput(this);">
<input type="hidden" name="frompage" value="<c:out value="${param.frompage}"/>">

<%@page import="jpa.util.StringUtil"%>
<%@page import="jpa.util.EmailAddrUtil"%>
<%@page import="jpa.model.SubscriberData"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%!
boolean isEmpty(String str) {
	return StringUtils.isEmpty(str);
}

String getSbsrAddrIdFromCookie(HttpServletRequest request) {
	 String cookieName = "sbsraddrid";
	 Cookie cookies[] = request.getCookies();
	 Cookie myCookie = null;
	 if (cookies != null) {
	 	for (int i = 0; i < cookies.length; i++) {
	 		if (cookies[i].getName().equals(cookieName)) {
	 			myCookie = cookies[i];
	 			return myCookie.getValue();
	 		}
	 	}
	 }
	 return null;
}
%>
<%
	Logger logger = LogManager.getLogger("jpa.msgui.publicsite.jsp");
	//String serverInfo = application.getServerInfo();
	ServletContext ctx = application;
	String errorMsg = "";
	String loginButtonText = "Login";
	String signUpButtonText = "Signup Now";
	
	String invalidate = request.getParameter("sbsrid");
	if (!StringUtils.isEmpty(invalidate)) {
		request.getSession().invalidate();
		logger.info("userprofile.jsp: invalidated the current session.");
	}
	
	String timeout = request.getParameter("timeout");
	if ("yes".equalsIgnoreCase(timeout)) {
		errorMsg = "Session has timed out, please login again.";
	}

	String error = request.getParameter("error");
	if (!StringUtils.isEmpty(error)) {
		if ("sbsrnotfound".equals(error))
			errorMsg = "User Email Address not found, please login again.";
		else if ("usernotfound".equals(error))
			errorMsg = "User account not found, please login again.";
		else
			errorMsg = "An error has occured during the process, please login again.";
	}

	String emailAddr = request.getParameter("userid");
	if (StringUtils.isEmpty(emailAddr)) {
		// populate emailAddr from cookie
		String sbsrAddrid = getSbsrAddrIdFromCookie(request);
		if (!StringUtils.isEmpty(sbsrAddrid)) {
			logger.info("userprofile.jsp - got sbsraddrid from cookie: " + sbsrAddrid);
			try {
				EmailAddress addrVo = getEmailAddressService(ctx).getByRowId(Integer.parseInt(sbsrAddrid));
				if (addrVo != null) {
					emailAddr = EmailAddrUtil.removeDisplayName(addrVo.getAddress());
				}
			}
			catch (Exception e) {
				logger.error("Exception caught", e);
			}
		}
	}
	
	String submit = request.getParameter("submit");
	if (loginButtonText.equals(submit)) { // Login button pressed
		request.getSession().invalidate();
		String userPswd = request.getParameter("userpswd");
		SubscriberData vo = getSubscriberDataService(ctx).getByEmailAddress(emailAddr);
		if (vo != null) {
			String pswd = vo.getUserPassword();
			if (isEmpty(pswd) && isEmpty(userPswd) || !isEmpty(pswd) && pswd.equals(userPswd)) {
				// login successful, set seesion attribute
				EmailAddress addrVo = getEmailAddressService(ctx).findSertAddress(emailAddr);
				request.getSession().setAttribute("sbsrId", Long.valueOf(addrVo.getRowId()));
			}
			else {
				errorMsg = "The Password entered is invalied!";
			}
		}
		else {
			errorMsg = "The system could not find the User Id you entered.";
		}
	}
	
	Object loggedin = request.getSession().getAttribute("sbsrId");
	if (errorMsg.trim().length() == 0 && loggedin != null && loggedin instanceof Long) {
		// logged in, redirect to edit page
		response.sendRedirect("userupdate.jsp?remember=" + request.getParameter("remember"));
		return;
	}
 	%>

<table width="100%" class="headerMenuContent" style="background: white;" border="0" cellspacing="0" cellpadding="0">
	<tr>
		<td colspan="2">
		<table class="gettingStartedHeader">
			<tr>
				<td><span class="gettingStartedTitle">
				<fmt:message key="userProfileDetailsTitle" bundle="${bndl}"/>
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
					<b><fmt:message key="userProfileLoginLabel" bundle="${bndl}"/></b>
				</td>
			</tr>
			<tr>
				<td colspan="2"><img src="./images/space.gif" height="10" style="border: 0px"></td>
			</tr>
			<tr>
				<td colspan="2"><img src="./images/space.gif" height="10" style="border: 0px"></td>
			</tr>
			</table>
	 	</td>
 	</tr>
	<tr>
		<td style="width: 80%;" align="center" colspan="2">
		<table style="width: 80%;" class="jsfDataTable" border="0" cellspacing="10" cellpadding="8">
			<tr>
			<td style="width: 50%; height: 100%;">
				<table width="100%" height="100%" border="0" style="background: #E5F3FF;" cellspacing="6" cellpadding="6">
					<tr><th style="background: #CCE7FF;">New User</th></tr>
					<tr>
						<td>Sign up today to receive personalized newsletters and product related updates.</td>
					</tr>
					<tr>
						<td>&nbsp;</td>
					</tr>
					<tr>
						<td align="right"><input type="button" name="signup" value="<%= signUpButtonText %>" onclick="javascript:window.location='usersignup.jsp'"></td>
					</tr>
				</table>
			</td>
			<td style="width: 50%; height: 100%">
				<table width="100%" height="100%" border="0" style="background: #E5F3FF;" cellspacing="6" cellpadding="6">
					<tr><th style="background: #CCE7FF;">Returning User</th></tr>
					<tr>
						<td align="right">User Id: 
						<input type="text" name="userid" id="userid" value="<%=nullToBlank(emailAddr)%>" size="38" maxlength="255"><br/>
						(Usually Your Email Address)</td>
					</tr>
					<tr>
						<td align="right">Password: <input type="password" name="userpswd" value="" size="38" maxlength="32"><br/>
						<input type="checkbox" name="remember" value="yes">&nbsp;Remember my ID on this computer</td>
					</tr>
				<% if (errorMsg.trim().length() > 0) { %>
					<tr>
						<td align="center"><font class="errorMessage"><%= errorMsg %></font></td>
					</tr>
				<% } %>
					<tr>
						<td align="right"><input type="submit" name="submit" value="<%= loginButtonText %>"></td>
					</tr>
				</table>
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
