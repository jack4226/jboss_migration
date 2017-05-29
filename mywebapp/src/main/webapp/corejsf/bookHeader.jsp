<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>

<h:panelGrid columns="1" styleClass="bookHeader">
   <h:graphicImage value="images/#{book.image}"/>
   <h:outputText value="#{msgs[book.titleKey]}" styleClass="bookTitle"/>
   <hr>
</h:panelGrid>
