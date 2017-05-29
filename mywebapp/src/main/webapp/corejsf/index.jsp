<html>
   <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
   <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
   <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
   <!-- JSF 1.1
   <f:loadBundle basename="ltj.msgui.messages" var="msgs"/>
   -->
   <f:view>
      <head>
         <link href="styles.css" rel="stylesheet" type="text/css"/>
         <title>
            <h:outputText value="#{msgs.indexWindowTitle}"/>
         </title>
      </head>
      <body>
         <h:form>
            <table>
               <tr>
                  <td>
                     <h:commandLink immediate="true" 
                        action="#{localeChanger.germanAction}">
                        <h:graphicImage value="/images/german_flag.gif" 
                           style="border: 0px"/>
                     </h:commandLink>
                  </td>
                  <td>
                  	<%--
                     <h:commandLink immediate="true" 
                        action="#{localeChanger.changeLocaleByParam}">
                       <f:param name="languageCode" value="en"/>
                        <h:graphicImage value="/images/britain_flag.gif" 
                           style="border: 0px"/>
                     </h:commandLink>
                     --%>
                     <%--
                     <h:commandLink immediate="true" 
                        actionListener="#{localeChanger.changeLocaleByAttr}">
                        <f:attribute name="languageCode" value="en"/>
                        <h:graphicImage value="/images/britain_flag.gif" 
                           style="border: 0px"/>
                     </h:commandLink>
                     --%>
                     <h:commandLink immediate="true" 
                        action="#{localeChanger.changeLocale}">
						<f:setPropertyActionListener target="#{localeChanger.languageCode}" 
                           value="en"/>
                        <h:graphicImage value="/images/britain_flag.gif" 
                           style="border: 0px"/>
                     </h:commandLink>
                  </td>
               </tr>
            </table>
            <p>
               <h:outputText value="#{msgs.indexPageTitle}" 
                  style="font-style: italic; font-size: 1.3em"/>
            </p>
            <h:messages errorClass="errors" layout="list" rendered="#{debug.showMessages}"/>
            <table>
               <tr>
                  <td>
                     <h:outputText value="#{msgs.namePrompt}"/>
                  </td>
                  <td>
                     <h:inputText id="name" value="#{user.name}" required="true"
                     	label="#{msgs.namePrompt}"/>
                  	<h:message for="name" styleClass="errors"/>
                  </td>
               </tr>
               <tr>
                  <td>
                     <h:outputText value="#{msgs.passwordPrompt}"/>
                  </td>
                  <td>
                     <h:inputSecret id="password" value="#{user.password}" required="true"
                     	label="#{msgs.passwordPrompt}"/>
                     <h:message for="password" styleClass="errors"/>
                  </td>
               </tr>
               <tr>
                  <td>
                     <h:outputText value="#{msgs.yearOfBirthPrompt}"/>
                  </td>
                  <td>
                     <h:inputText id="dob" value="#{user.yearOfBirth}"
                     	label="#{msgs.yearOfBirthPrompt}"/>
                     <h:message for="dob" styleClass="errors"/>
                  </td>
               </tr>
               <tr>
                  <td style="vertical-align: top">
                     <h:outputText value="#{msgs.tellUsPrompt}"/>
                  </td>
                  <td>
                     <h:inputTextarea value="#{user.aboutYourself}" rows="5" 
                        cols="35"/>
                  </td>
               </tr>
               <tr>
                  <td>
                     <h:commandButton value="#{msgs.submitPrompt}" 
                        action="thankYou"/>
                  </td>
               </tr>
            </table>
         </h:form>
      </body>
   </f:view>
</html>
