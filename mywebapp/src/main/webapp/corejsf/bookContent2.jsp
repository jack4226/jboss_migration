<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:if test="${param.chapter == null}">
	<c:import url="${book.directory}/chapter1.html"/>
</c:if>
<c:if test="${param.chapter > ''}">
	<c:import url="${book.directory}/${param.chapter}.html"/>
</c:if>
