<html>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="jpa.msgui.messages" var="bndl"/>
<head>
<link href="../styles.css" rel="stylesheet" type="text/css" />
<title><fmt:message key="uploadEmailAddrsToList" bundle="${bndl}"/></title>
</head>
<body>
<div align="center">
<jsp:useBean id="uploadForm"
	class="jpa.msgui.bean.FileUploadForm" scope="request" />
<jsp:setProperty name="uploadForm" property="*" />
<jsp:useBean id="dynamicCodes"
	class="jpa.msgui.util.DynamicCodes" scope="page" />
<jsp:useBean id="staticCodes"
	class="jpa.msgui.util.StaticCodes" scope="page" />

<form action="uploadServlet" method="post" enctype="multipart/form-data">
<input type="hidden" name="frompage" value="<c:out value="${param.frompage}"/>"/>
<table class="headerMenuContent">
	<tr>
		<td>
		<table class="gettingStartedHeader">
			<tr>
				<td width="15%">&nbsp;</td>
				<td width="70%"><span class="gettingStartedTitle">
					<fmt:message key="${param.titleKey}" bundle="${bndl}"/></span>
				</td>
				<td width="15%" style="text-align: right; vertical-align: bottom;">&nbsp;<br/>
					<a style="color: blue;" class="headerLinkText" href="../main.faces">Main</a>
					<img src="/MsgUI2/images/space.gif" height="1" style="border: 0px" width="40" />
				</td>
			</tr>
		</table>
		</td>
	</tr>
	<tr>
		<td>
			<span style="text-align: left;">
			<img src="/MsgUI2/images/space.gif" height="1" style="border: 0px" width="10" />
			</span>
			<span class="gridHeader">
			<fmt:message key="uploadEmailAddressFile" bundle="${bndl}"/>
			</span><p/>
		</td>
	</tr>
	<tr>
		<td>
			<span style="text-align: left;">
			<img src="/MsgUI2/images/space.gif" height="1" style="border: 0px" width="10" />
			</span>
			<span style="color: black; font-size: 1.1em;">
			<fmt:message key="uploadFileToListLabel" bundle="${bndl}"/>
			</span><p/>
	</tr>
	<!-- import from file section -->
	<tr>
		<td>
		<table class="smtpBody">
			<tbody style="padding: 2px;">
				<tr>
					<td colspan="3" class="alignCenter">&nbsp;<br></td>
				</tr>
				<tr>
					<td style="width: 10%; text-align: right; font-weight: bold;">Import to:</td>
					<td style="width: 40%;">
						<select name="listid">
						<c:forEach items="${dynamicCodes.mailingListIdItems}" var="listId">
							<option <c:if test="${listId.value == param.listid}">selected</c:if> value="${listId.value}"><c:out value="${listId.label}"/></option> 
						</c:forEach>
						</select>
					</td>
					<td style="width: 50%; text-align: left;">&nbsp;</td>
				</tr>
				<tr>
					<td colspan="3">
					<img src="/MsgUI2/images/space.gif" height="10" style="border: 0px"/>
					</td>
				</tr>
				<tr>
					<td colspan="3">
						<span style="text-align: left;">
						<img src="/MsgUI2/images/space.gif" height="1" style="border: 0px" width="10" />
						</span>
						<span style="font-size: large;">
						<fmt:message key="browseAndSelectFileLabel" bundle="${bndl}"/>
						</span><p/>
					</td>
				</tr>
				<tr>
					<td colspan="3">
					<img src="/MsgUI2/images/space.gif" height="10" style="border: 0px"/>
					</td>
				</tr>
				<tr>
					<td style="width: 10%; text-align: right; font-weight: bold;">File:</td>
					<td style="width: 60%;">
					<input type="file" name="file1" value="${uploadForm.pathes.file1}" size="40" /></td>
					<td style="width: 30%; text-align: left;">
					<c:if test="${uploadForm.errors.file1 != null}">
						<span style="color: red;">${uploadForm.errors.file1}</span>
					</c:if>
					<c:if test="${uploadForm.messages.file1 != null}">
						<span style="color: green;">${uploadForm.messages.file1}</span>
					</c:if></td>
				</tr>
				<c:if test="${fn:length(uploadForm.errors) > 0 and param.submit == 'Import From File'}">
					<tr>
						<td colspan="3">
						<table class="errorMessage">
							<c:forEach var="error" items="${uploadForm.errors}">
							<tr>
								<td width="30%"><c:out value="${error.key}"/>:&nbsp;</td>
								<td width="70%"><c:out value="${error.value}"/></td>
							</tr>
							</c:forEach>
							<tr style="fond-size: 1.1em;">
								<td width="20%">Total Number of Errors:&nbsp;</td>
								<td width="80%">${fn:length(uploadForm.errors)}</td>
							</tr>
						</table>
						</td>
					</tr>
				</c:if>
				<c:if test="${fn:length(uploadForm.messages) > 0 and param.submit == 'Import From File'}">
					<tr>
						<td colspan="3">
						<table class="messages">
							<c:forEach var="msg" items="${uploadForm.messages}">
							<tr>
								<td><c:out value="${msg.key}"/>:&nbsp;</td>
								<td><c:out value="${msg.value}"/></td>
							</tr>
							</c:forEach>
						</table>
						</td>
					</tr>
				</c:if>
				<tr>
					<td colspan="3" class="alignCenter">&nbsp;<br></td>
				</tr>
			</tbody>
		</table>
		</td>
	</tr>
	<tr>
		<td>
		<table class="commandBar">
			<tr>
				<td style="width: 10%;">&nbsp;</td>
				<td style="width: 90%; align: left;">
					<input type="submit" name="submit" value="Import From File" />&nbsp;
					<c:if test="${fn:length(uploadForm.errors) == 0 and fn:length(uploadForm.messages) == 0}">
						<input type="submit" name="submit" value="Cancel"/>
					</c:if>
					<c:if test="${fn:length(uploadForm.errors) > 0 or fn:length(uploadForm.messages) > 0}">
						<input type="submit" name="submit" value="Done"/>
					</c:if>
				</td>
			</tr>
		</table>
		</td>
	</tr>
	<!-- import from mailing list section -->
	<tr>
		<td>
		<table class="smtpBody" style="padding: 2px;">
			<tr>
				<td colspan="3">
				<img src="/MsgUI2/images/space.gif" height="10" style="border: 0px"/>
				</td>
			</tr>
			<tr>
				<td colspan="3">
					<span style="text-align: left;">
					<img src="/MsgUI2/images/space.gif" height="1" style="border: 0px" width="10" />
					</span>
					<span style="font-size: large;">
					<fmt:message key="importFromListLabel" bundle="${bndl}"/>
					</span><p/>
				</td>
			</tr>
			<tr>
				<td colspan="3">
				<img src="/MsgUI2/images/space.gif" height="10" style="border: 0px"/>
				</td>
			</tr>
			<tr>
				<td style="width: 10%; text-align: right; font-weight: bold;">Import from:</td>
				<td style="width: 60%;">
					<select name="fromlistid">
					<c:forEach items="${dynamicCodes.mailingListIdItems}" var="listId">
						<option value="${listId.value}"><c:out value="${listId.label}"/></option> 
					</c:forEach>
					</select>
				</td>
				<td style="width: 30%; text-align: left;">&nbsp;</td>
			</tr>
			<tr>
				<td colspan="3">
				<img src="/MsgUI2/images/space.gif" height="10" style="border: 0px"/>
				</td>
			</tr>
			<c:if test="${fn:length(uploadForm.errors) > 0 and param.submit == 'Import From List'}">
				<tr>
					<td colspan="3">
					<table class="errorMessage">
						<c:forEach var="error" items="${uploadForm.errors}">
						<tr>
							<td width="30%"><c:out value="${error.key}"/>:&nbsp;</td>
							<td width="70%"><c:out value="${error.value}"/></td>
						</tr>
						</c:forEach>
						<tr style="fond-size: 1.1em;">
							<td width="20%">Total Number of Errors:&nbsp;</td>
							<td width="80%">${fn:length(uploadForm.errors)}</td>
						</tr>
					</table>
					</td>
				</tr>
			</c:if>
			<c:if test="${fn:length(uploadForm.messages) > 0 and param.submit == 'Import From List'}">
				<tr>
					<td colspan="3">
					<table class="messages">
						<c:forEach var="msg" items="${uploadForm.messages}">
						<tr>
							<td><c:out value="${msg.key}"/>:&nbsp;</td>
							<td><c:out value="${msg.value}"/></td>
						</tr>
						</c:forEach>
					</table>
					</td>
				</tr>
			</c:if>
		</table>
		</td>
	</tr>
	<tr>
		<td>
		<table class="commandBar">
			<tr>
				<td style="width: 10%;">&nbsp;</td>
				<td style="width: 90%; align: left;">
					<input type="submit" name="submit" value="Import From List" />&nbsp;
					<c:if test="${fn:length(uploadForm.errors) == 0 and fn:length(uploadForm.messages) == 0}">
						<input type="submit" name="submit" value="Cancel"/>
					</c:if>
					<c:if test="${fn:length(uploadForm.errors) > 0 or fn:length(uploadForm.messages) > 0}">
						<input type="submit" name="submit" value="Done"/>
					</c:if>
				</td>
			</tr>
		</table>
		</td>
	</tr>
	<tr>
		<td>
		<table class="gettingStartedFooter">
			<tr>
				<td style="width: 50%; text-align: left;" class="headerLinkText">&copy; <fmt:message key="copyrightText" bundle="${bndl}"/></td>
				<td style="width: 50%; text-align: right;" class="headerLinkText"><c:out value="${staticCodes.poweredByHtmlTag}" escapeXml="false"/></td>
			</tr>
		</table>
		</td>
	</tr>
</table>
</form>
</div>
</body>
</html>
