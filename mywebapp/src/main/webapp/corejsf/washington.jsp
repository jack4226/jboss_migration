<html>
   <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
   <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
   <f:view>
      <head>                  
         <link href="styles.css" rel="stylesheet" type="text/css"/>
         <title>
            <h:outputText value="#{msgs.washingtonWindowTitle}"/>
         </title>
      </head>

      <body>
         <h:form>
            <h:outputText value="#{msgs.washingtonPageTitle}" 
               styleClass="presidentPageTitle"/>
            <p/>
            <h:outputText value="#{msgs.washingtonDiscussion}"
               styleClass="presidentDiscussion"/>
            <p>
               <h:commandLink action="rushmore" 
                  styleClass="backLink">
                  ${msgs.indexLinkText}
               </h:commandLink>
            </p>
         </h:form>
      </body>
   </f:view>
</html>

