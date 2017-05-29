<html>
   <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
   <%@ taglib uri="http://java.sun.com/jsf/core"  prefix="f" %>
   <%@ taglib uri="http://java.sun.com/jsf/html"  prefix="h" %>

   <f:view>
      <f:loadBundle basename="ltj.msgui.messages" var="msgs"/>
      <head>
         <link href="styles.css" rel="stylesheet" type="text/css"/>
         <title><h:outputText value="#{msgs.bookWindowTitle}"/></title>
      </head>

      <body>
         <h:form>
            <h:panelGrid columns="2" styleClass="book"
                   columnClasses="menuColumn, chapterColumn">
               <f:facet name="header">
                  <f:subview id="header">
                     <c:import url="/bookHeader.jsp"/>
                  </f:subview>
               </f:facet>

               <f:subview id="menu">
                  <c:import url="/bookMenu.jsp"/>
               </f:subview>

               <c:import url="/bookContent.jsp"/>
            </h:panelGrid>
         </h:form>
      </body>
   </f:view>
</html>
