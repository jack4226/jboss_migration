<%@ taglib uri="http://java.sun.com/jsf/core"  prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"  prefix="h" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<h:panelGrid columns="1" styleClass="book" headerClass="libraryHeader">
   <f:facet name="header">
      <f:subview id="header">
         <tiles:insertAttribute name="header" flush="false"/>
      </f:subview>
   </f:facet>

   <f:subview id="book">
      <tiles:insertAttribute name="book" flush="false"/>
   </f:subview>
</h:panelGrid>
