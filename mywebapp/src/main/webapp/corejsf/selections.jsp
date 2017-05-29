<html>
   <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
   <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
   <f:view>
      <head> 
         <link href="styles.css" rel="stylesheet" type="text/css"/> 
         <title>
            <h:outputText value="#{msgs.indexWindowTitle}"/>
         </title>
      </head>

      <body>
      <h:messages errorClass="errors" layout="list" rendered="#{debug.showMessages}"/>
         <h:outputText value="#{msgs.indexPageTitle}" styleClass="emphasis"/>
         <h:form>
            <table>
               <tr>
                  <td>
                     <h:outputText value="#{msgs.namePrompt}"/>
                  </td>
                  <td>
                     <h:inputText value="#{form.name}"/>
                  </td>
               </tr>
               <tr>
                  <td>
                     <h:outputText value="#{msgs.contactMePrompt}"/>
                  </td>
                  <td>
                     <h:selectBooleanCheckbox value="#{form.contactMe}"/>
                  </td>
               </tr>
               <tr>
                  <td>
                     <h:outputText value="#{msgs.bestDayPrompt}"/>
                  </td>
                  <td>
                     <h:selectManyMenu value="#{form.bestDaysToContact}">
                        <f:selectItems value="#{form.daysOfTheWeekItems}"/>
                     </h:selectManyMenu>
                  </td>
               </tr>
               <tr>
                  <td>
                     <h:outputText value="#{msgs.yearOfBirthPrompt}"/>
                  </td>
                  <td>
                     <h:selectOneListbox size="5" value="#{form.yearOfBirth}">
                        <f:selectItems value="#{form.yearItems}"/>
                     </h:selectOneListbox>
                  </td>
               </tr>
               <tr>
                  <td>
                     <h:outputText value="#{msgs.colorPrompt}"/>
                  </td>
                  <td>
                     <h:selectManyCheckbox value="#{form.colors}">
                        <f:selectItems value="#{form.colorItems}"/>
                     </h:selectManyCheckbox>
                  </td>
               </tr>
               <tr>
                  <td>
                     <h:outputText value="#{msgs.languagePrompt}"/>
                  </td>
                  <td>
                     <h:selectManyListbox value="#{form.languages}">
                        <f:selectItems value="#{form.languageItems}"/>
                     </h:selectManyListbox>
                  </td>
               </tr>
               <tr>
                  <td>
                     <h:outputText value="#{msgs.educationPrompt}"/>
                  </td>
                  <td>
                     <h:selectOneRadio value="#{form.education}" 
                        layout="pageDirection">
                        <f:selectItems value="#{form.educationItems}"/>
                     </h:selectOneRadio>
                  </td>
               </tr>
               
               <tr>
               	<td>
               		<h:outputText value="Item Groups Example"/>
               	</td>
               	<td>
               		<h:selectManyListbox value="#{form.menus}" size="6">
               			<f:selectItems value="#{form.menuItems}"/>
               		</h:selectManyListbox>
               	</td>
               </tr>
            </table>
            <h:commandButton value="#{msgs.buttonPrompt}" 
               action="showInformation"/>
         </h:form>
         <h:messages/>
      </body>
   </f:view>
</html>
