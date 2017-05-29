<html>
   <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
   <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
   <f:view>
      <head>               
         <title><h:outputText value="#{msgs.thankYouWindowTitle}"/></title>
      </head>
      <body>
         <h:form>
            <h3>
               Welcome to JavaServer Faces, 
               <h:outputText value="#{user.name}"/>!
            </h3>
            <h:messages errorClass="errors" layout="list" rendered="#{debug.showMessages}"/>
            <br/>
            <h:outputText value="#{msgs.aboutYourselfPrompt}"/>
            <br/>
            <pre><h:outputText value="#{user.aboutYourself}"/></pre>
            <br/>
            <h:outputText escape="true" value='<input type="text" value="hello"/>'/>
            <br/>
            <h:outputFormat value="{0} is {1} years old.">
            	<f:param value="Daniel"/>
            	<f:param value="5"/>
            </h:outputFormat>
            <br/>
            <h:graphicImage value="/images/one.gif" styleClass="imageBorder"/>
            <br/>
            <h:outputText value="The value param['outcome']: #{param['outcome']}"/>
        </h:form>
      </body>      
   </f:view>
</html>