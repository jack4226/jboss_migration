<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Referral Thank You Page</title>
</head>
<body>
<div align="center">
<table width="100%" class="headerMenuContent" style="background: white;" border="0" cellspacing="0" cellpadding="0">
	<tr>
		<td colspan="2">
		<table class="gettingStartedHeader">
			<tr>
				<td><span class="gettingStartedTitle">
				<fmt:message key="referralThankYouTitle" bundle="${bndl}"/>
				</span></td>
			</tr>
		</table>
		</td>
	</tr>
<tr><td colspan="2">
<table border="0" cellspacing="4" cellpadding="8">
<tr><td>
Thank you for using our Tell-A-Friend page. We appreciate your referral.
</td></tr>
</table>
</td></tr>
	<tr>
		<td colspan="2" align="center">
		<table style="width: 100%;" class="commandBar">
			<tr>
				<td width="10%">&nbsp;</td>
				<td width="90%" align="left">
					<input type="button" value="Back" onclick="javascript:history.go(-2)">
				</td>
			</tr>
		</table>
		</td>
	</tr>

</table>
</div>
</body>
</html>