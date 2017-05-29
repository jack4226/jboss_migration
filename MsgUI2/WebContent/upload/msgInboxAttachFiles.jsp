<html>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="jpa.msgui.messages" var="bndl"/>
<head>
<link href="../styles.css" rel="stylesheet" type="text/css" />
<title><fmt:message key="uploadAttachmentsPageTitle" bundle="${bndl}"/></title>
</head>
<body>
<div align="center">
<jsp:useBean id="uploadForm"
	class="jpa.msgui.bean.FileUploadForm" scope="request" />
<jsp:setProperty name="uploadForm" property="*" />
<jsp:useBean id="staticCodes"
	class="jpa.msgui.util.StaticCodes" scope="page" />

<form action="uploadServlet" method="post" enctype="multipart/form-data">
<input type="hidden" name="frompage" value="<c:out value="${param.frompage}"/>"/>
<input type="hidden" name="listid" value="<c:out value="${param.listid}"/>"/>
<table class="headerMenuContent">
	<tr>
		<td>
		<table class="gettingStartedHeader">
			<tbody>
				<tr>
					<td><span class="gettingStartedTitle">
					<fmt:message key="gettingStartedHeaderText" bundle="${bndl}"/>
					</span></td>
				</tr>
			</tbody>
		</table>
		</td>
	</tr>
	<tr>
		<td>
			<span style="text-align: left;">
			<img src="/MsgUI2/images/space.gif" height="1" style="border: 0px" width="10" />
			</span>
			<span class="gridHeader">Attach Files</span><p/>
		</td>
	</tr>
	<tr>
		<td>
			<span style="text-align: left;">
			<img src="/MsgUI2/images/space.gif" height="1" style="border: 0px" width="10" />
			</span>
			<span style="font-size: large;">
			<fmt:message key="browseAndSelectAttachFilesLabel" bundle="${bndl}"/>
			</span><p/>
		</td>
	</tr>
	<tr>
		<td>
		<table class="smtpBody">
			<tbody style="padding: 2px;">
				<tr>
					<td colspan="3" class="alignCenter">&nbsp;<br></td>
				</tr>
				<tr>
					<td style="width: 10%; text-align: right; font-weight: bold;">File 1:</td>
					<td style="width: 40%;">
					<input type="file" name="file1" value="${uploadForm.pathes.file1}" size="40" /></td>
					<td style="width: 50%; text-align: left;">
					<c:if test="${uploadForm.errors.file1 != null}">
						<span style="color: red;">${uploadForm.errors.file1}</span>
					</c:if>
					<c:if test="${uploadForm.messages.file1 != null}">
						<span style="color: green;">${uploadForm.messages.file1}</span>
					</c:if></td>
				</tr>
				<tr>
					<td style="width: 10%; text-align: right; font-weight: bold;">File 2:</td>
					<td style="width: 40%;">
					<input type="file" name="file2" value="${uploadForm.pathes.file2}" size="40" /></td>
					<td style="width: 50%; text-align: left;">
					<c:if test="${uploadForm.errors.file2 != null}">
						<span style="color: red;">${uploadForm.errors.file2}</span>
					</c:if>
					<c:if test="${uploadForm.messages.file2 != null}">
						<span style="color: green;">${uploadForm.messages.file2}</span>
					</c:if></td>
				</tr>
				<tr>
					<td style="width: 10%; text-align: right; font-weight: bold;">File 3:</td>
					<td style="width: 40%;">
					<input type="file" name="file3" value="${uploadForm.pathes.file3}" size="40" /></td>
					<td style="width: 50%; text-align: left;">
					<c:if test="${uploadForm.errors.file3 != null}">
						<span style="color: red;">${uploadForm.errors.file3}</span>
					</c:if>
					<c:if test="${uploadForm.messages.file3 != null}">
						<span style="color: green;">${uploadForm.messages.file3}</span>
					</c:if></td>
				</tr>
				<tr>
					<td style="width: 10%; text-align: right; font-weight: bold;">File 4:</td>
					<td style="width: 40%;">
					<input type="file" name="file4" value="${uploadForm.pathes.file4}" size="40" /></td>
					<td style="width: 50%; text-align: left;">
					<c:if test="${uploadForm.errors.file4 != null}">
						<span style="color: red;">${uploadForm.errors.file4}</span>
					</c:if>
					<c:if test="${uploadForm.messages.file4 != null}">
						<span style="color: green;">${uploadForm.messages.file4}</span>
					</c:if></td>
				</tr>
				<tr>
					<td style="width: 10%; text-align: right; font-weight: bold;">File 5:</td>
					<td style="width: 40%;">
					<input type="file" name="file5" value="${uploadForm.pathes.file5}" size="40" /></td>
					<td style="width: 50%; text-align: left;">
					<c:if test="${uploadForm.errors.file5 != null}">
						<span style="color: red;">${uploadForm.errors.file5}</span>
					</c:if>
					<c:if test="${uploadForm.messages.file5 != null}">
						<span style="color: green;">${uploadForm.messages.file5}</span>
					</c:if></td>
				</tr>
				<tr>
					<td style="width: 10%; text-align: right; font-weight: bold;">File 6:</td>
					<td style="width: 40%;">
					<input type="file" name="file6" value="${uploadForm.pathes.file6}" size="40" /></td>
					<td style="width: 50%; text-align: left;">
					<c:if test="${uploadForm.errors.file6 != null}">
						<span style="color: red;">${uploadForm.errors.file6}</span>
					</c:if>
					<c:if test="${uploadForm.messages.file6 != null}">
						<span style="color: green;">${uploadForm.messages.file6}</span>
					</c:if></td>
				</tr>
				<tr>
					<td style="width: 10%; text-align: right; font-weight: bold;">File 7:</td>
					<td style="width: 40%;">
					<input type="file" name="file7" value="${uploadForm.pathes.file7}" size="40" /></td>
					<td style="width: 50%; text-align: left;">
					<c:if test="${uploadForm.errors.file7 != null}">
						<span style="color: red;">${uploadForm.errors.file7}</span>
					</c:if>
					<c:if test="${uploadForm.messages.file7 != null}">
						<span style="color: green;">${uploadForm.messages.file7}</span>
					</c:if></td>
				</tr>
				<tr>
					<td style="width: 10%; text-align: right; font-weight: bold;">File 8:</td>
					<td style="width: 40%;">
					<input type="file" name="file8" value="${uploadForm.pathes.file8}" size="40" /></td>
					<td style="width: 50%; text-align: left;">
					<c:if test="${uploadForm.errors.file8 != null}">
						<span style="color: red;">${uploadForm.errors.file8}</span>
					</c:if>
					<c:if test="${uploadForm.messages.file8 != null}">
						<span style="color: green;">${uploadForm.messages.file8}</span>
					</c:if></td>
				</tr>
				<tr>
					<td style="width: 10%; text-align: right; font-weight: bold;">File 9:</td>
					<td style="width: 40%;">
					<input type="file" name="file9" value="${uploadForm.pathes.file9}" size="40" /></td>
					<td style="width: 50%; text-align: left;">
					<c:if test="${uploadForm.errors.file9 != null}">
						<span style="color: red;">${uploadForm.errors.file9}</span>
					</c:if>
					<c:if test="${uploadForm.messages.file9 != null}">
						<span style="color: green;">${uploadForm.messages.file9}</span>
					</c:if></td>
				</tr>
				<tr>
					<td style="width: 10%; text-align: right; font-weight: bold;">File 10:</td>
					<td style="width: 40%;">
					<input type="file" name="file10" value="${uploadForm.pathes.file10}" size="40" /></td>
					<td style="width: 50%; text-align: left;">
					<c:if test="${uploadForm.errors.file10 != null}">
						<span style="color: red;">${uploadForm.errors.file10}</span>
					</c:if>
					<c:if test="${uploadForm.messages.file10 != null}">
						<span style="color: green;">${uploadForm.messages.file10}</span>
					</c:if></td>
				</tr>
				<c:if test="${fn:length(uploadForm.errors) > 0}">
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
					<input type="submit" name="submit" value="Attach Files" />&nbsp;
					<input type="submit" name="submit" value="Cancel"/>&nbsp;
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
