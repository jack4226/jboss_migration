<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>

<h:dataTable value="#{book.chapterKeys}" var="chapterKey" 
        styleClass="links" columnClasses="linksColumn">
   <h:column>
      <h:commandLink>
         <h:outputText value="#{msgs[chapterKey]}"/>
         <f:param name="chapter" value="#{chapterKey}"/>
      </h:commandLink>
   </h:column>
</h:dataTable>
