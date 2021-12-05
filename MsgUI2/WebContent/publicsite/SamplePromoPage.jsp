<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Update Click Count</title>
<style type="text/css">
<!--
-->
</style>
<link rel="stylesheet" href="styles.css">
</head>
<noscript><p><b>Javascript must be enabled to use this form.</b></p></noscript>
<body bgcolor="#ffffff" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0"
	vlink="#00364a" link="#00364a" alink="#ff9900">
<div align="left">

<%@page import="jpa.service.maillist.MailingListBo"%>
<%@ include file="./loadSbsrDaos.jsp" %>
<%@page import="jpa.util.StringUtil"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%
/**
 *	sbsrid, listid and msgid should be passed from http query string
 */
	Logger logger = LogManager.getLogger("jpa.service.jsp");
	ServletContext ctx = application;
	String sbsrId = request.getParameter("sbsrid");
	String listId = request.getParameter("listid");
	String msgId = request.getParameter("msgid");
	%>
<p/>
<table width="980" border="0" cellpadding="0" cellspacing="0">
	<tr>
		<td colspan="2" valign="top">
		<table width="820" border="0" cellspacing="0" cellpadding="0">
			<tr>
				<td width="60%"><img height="30" style="WIDTH: 10px;" src="./images/spacer.gif" border="0">
					<font size="5" color="darkblue" face="serif">This Page Updates Click Count When Loaded.</font></td>
				<td><img height="30" style="WIDTH: 100px;" src="./images/spacer.gif" border="0"></td>
			<tr>
		</table>
		<table width="800" border="0" cellspacing="5" cellpadding="25" height="270">
			<tr valign="top">
				<td class="PageText">
				<ul>
					<li><font color="darkblue"><b>Welcome to Sample Promotion Page!</b></font><p/>
					</li>
				</ul>
				<div align="center"><br>
				</div>
				</td>
			</tr>
			<tr><td colspan="2">
				<a href="javascript:window.close();">Close This Window</a><br/>
			</td></tr>
		</table>
	</tr>
</table>
<p/>
<%= renderURLVariable(ctx, "EmailClickCountImgTag", sbsrId, listId, msgId) %>
</div>
</body>
</html>
