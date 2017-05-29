<html>
   <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
   <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
   <f:view>
      <head>                  
         <link href="styles.css" rel="stylesheet" type="text/css"/>
         <title>${msgs.jeffersonWindowTitle}</title>
      </head>

      <body>
         <h:form>
            <h:outputText value="#{msgs.jeffersonPageTitle}" 
               styleClass="presidentPageTitle"/>
            <p/>
            <h:outputText value="#{msgs.jeffersonDiscussion}"
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

