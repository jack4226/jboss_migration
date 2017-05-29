<html>
   <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
   <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
   <f:view>
      <head>
         <title>
            <h:outputText value="#{msgs.thankYouWindowTitle}"/>
         </title>
      </head>
      <body>
      <h:messages errorClass="errors" layout="list" rendered="#{debug.showMessages}"/>
         <h:outputText value="#{msgs.namePrompt}" style="font-style: italic"/>
         <h:outputText value="#{user.name}"/>
         <br/>
         <h:outputText value="#{msgs.aboutYourselfPrompt}" 
            style="font-style: italic"/>
         <br/>
         <pre><h:outputText value="#{user.aboutYourself}"/></pre>
      </body>
   </f:view>
</html>
