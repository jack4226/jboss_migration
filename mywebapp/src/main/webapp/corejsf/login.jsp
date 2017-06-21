<html>
   <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
   <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
   <f:view>
      <head>
      	<link href="styles.css" rel="stylesheet" type="text/css"/> 
         <title>A Simple JavaServer Faces Application</title>
      </head>
      <body>
         <h:form>
            <h3>Please enter your name and password.</h3>
        <h:messages errorClass="errors" layout="list" rendered="#{debug.showMessages}"/>
        <!-- h:panelGrid columns="2" rowClasses="oddRows,evenRows" -->
        <h:panelGrid columns="2">
			<h:outputText value="Name:"/>
			<h:panelGroup>
				<h:inputText id="name" value="#{user.name}" required="true"
					label="#{msgs.namePrompt}"/>
				<h:message for="name" styleClass="errors"/>
			</h:panelGroup>
			
			<h:outputText value="Password:"/>
			<h:panelGroup>
				<h:inputSecret id="password" value="#{user.password}" required="true"
					label="#{msgs.passwordPrompt}"/>
				<h:message for="password" styleClass="errors"/>
			</h:panelGroup>
			<h:outputText value="#{msgs.tellUsPrompt}"/>
			<h:inputTextarea value="#{user.aboutYourself}" rows="5" cols="35"/>
        </h:panelGrid>
		<p>
               <h:commandButton value="Login" action="login"/>
            </p>
            
            <h:commandLink action="login">
            	<h:outputText value="Click here to register"/>
            	<h:graphicImage value="/images/two.gif"/>
            	<f:param name="outcome" value="welcome"/>
            </h:commandLink>
            <p>
            <h:outputLink value="http://www.java.net">
            	<h:graphicImage value="/images/header_java_net.jpg"/>
            	<h:outputText value="java.net"/>
            </h:outputLink>
            <p>
            <h:outputLink value="#conclusion" title="Go to the conclusion">
            	<h:outputText value="Conclusion"/>
            </h:outputLink>
            <p><p><p><p><p>
            <a title="conclusion">Conclusion</a>
         </h:form>
      </body>
   </f:view>
</html>