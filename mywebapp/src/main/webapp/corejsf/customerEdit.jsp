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
      <h:form>
        <h:messages errorClass="errors" layout="list" rendered="#{debug.showMessages}"/>
         <h:outputFormat value="#{msgs.thankYouLabel}">
            <f:param value="#{customers.customer.firstName}"/>
         </h:outputFormat>
         <p>
         <table>
            <tr>
               <td><h:outputText value="#{msgs.customerIdLabel}"/></td>
               <td><h:inputText id="custid" value="#{customers.customer.custId}"
               		readonly="#{customers.editMode}" required="true"
               		label="#{msgs.customerIdLabel}"/>
               	<h:message for="custid" styleClass="errorMessage"/></td>
            </tr>
            
            <tr>
               <td><h:outputText value="#{msgs.firstNameLabel}"/></td>
               <td><h:inputText value="#{customers.customer.firstName}"/></td>
            </tr>
            
            <tr>
               <td><h:outputText value="#{msgs.lastNameLabel}"/></td>
               <td><h:inputText id="lastname" value="#{customers.customer.lastName}"
               		required="true" label="#{msgs.lastNameLabel}"/>
               	<h:message for="lastname" styleClass="errorMessage"/></td>
            </tr>
            
            <tr>
               <td><h:outputText value="#{msgs.dayPhoneLabel}"/></td>
               <td><h:inputText value="#{customers.customer.dayPhone}"/></td>
            </tr>
            
            <tr>
               <td><h:outputText value="#{msgs.streetAddressLabel}"/></td>
               <td><h:inputText id="address" value="#{customers.customer.streetAddress}"
               		required="true" label="#{msgs.streetAddressLabel}"/>
               	<h:message for="address" styleClass="errorMessage"/></td>
            </tr>

            <tr>
               <td><h:outputText value="#{msgs.streetAddress2Label}"/></td>
               <td><h:inputText value="#{customers.customer.streetAddress2}"/></td>
            </tr>

            <tr>
               <td><h:outputText value="#{msgs.cityLabel}"/></td>
               <td><h:inputText id="city" value="#{customers.customer.cityName}"
               		required="true" label="#{msgs.cityLabel}"/>
               	<h:message for="city" styleClass="errorMessage"/></td>
            </tr>
            
            <tr>
               <td><h:outputText value="#{msgs.stateLabel}"/></td>
               <td><h:selectOneMenu id="state" value="#{customers.customer.stateCode}"
               		required="true" label="#{msgs.stateLabel}">
               	<f:selectItems value="#{codes.stateCodeWithAbbrItems}"/>
               </h:selectOneMenu>
               <h:message for="state" styleClass="errorMessage"/>
               </td>
            </tr>
         </table>
         <h:commandButton value="#{msgs.saveChangesButtonText}" 
            action="#{customers.saveCustomer}"/>
      </h:form>
      </body>
   </f:view>
</html>
