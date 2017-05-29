<html>
   <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
   <%@ taglib uri="http://java.sun.com/jsf/core"  prefix="f" %>
   <%@ taglib uri="http://java.sun.com/jsf/html"  prefix="h" %>

   <f:view>
      <head>
         <link href="styles.css" rel="stylesheet" type="text/css"/>
         <title><h:outputText value="#{msgs.bookWindowTitle}"/></title>
      </head>

      <body>
         <h:form>
            <h:panelGrid columns="2" styleClass="book"
                   columnClasses="menuColumn, chapterColumn">
               <f:facet name="header">
                  <h:panelGrid columns="1" styleClass="bookHeader">
                     <h:graphicImage value="images/#{book.image}"/>
                     <h:outputText value="#{msgs[book.titleKey]}" 
                              styleClass='bookTitle'/>
                     <hr/>
                  </h:panelGrid>
               </f:facet>

               <h:dataTable value="#{book.chapterKeys}" var="chapterKey" 
                         styleClass="links" columnClasses="linksColumn">
                  <h:column>
                     <h:commandLink>
                        <h:outputText value="#{msgs[chapterKey]}"/>
                        <f:param name="chapter" value="#{chapterKey}"/>
                     </h:commandLink>
                  </h:column>
               </h:dataTable>
               
               <c:if test="${param.chapter == null}">
               	<c:import url="htmls/chapter1.html"/>
               </c:if>
               <c:if test="${param.chapter > ''}">
               	<c:import url="htmls/${param.chapter}.html"/>
               </c:if>
               <%-- c:import url="htmls/${param.chapter}.html"/ --%>
               <%-- jsp:include page="htmls/chapter1.html"/ --%>
               <%-- @ include file="htmls/chapter1.html" --%>
            </h:panelGrid>
         </h:form>
      </body>
   </f:view>
</html>
