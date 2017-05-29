<html>
   <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
   <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
   <f:view>
      <head>
         <link href="styles.css" rel="stylesheet" type="text/css"/>
         <title><h:outputText value="#{msgs.title}"/></title>
      </head>
      <body>
         <h:form>
            <h1><h:outputText value="#{msgs.enterPayment}"/></h1>
         <h:messages errorClass="errors" layout="list" rendered="#{debug.showMessages}"/>
            <h:panelGrid columns="3">
               <h:outputText value="#{msgs.amount}"/>
               <h:inputText id="amount" label="#{msgs.amount}"
                     value="#{payment.amount}">
                  <f:convertNumber minFractionDigits="2"/>
                  <f:validateLongRange minimum="10" maximum="10000"/>
               </h:inputText>
               <h:message for="amount" styleClass="errorMessage"/>

               <h:outputText value="#{msgs.creditCard}"/>
               <h:inputText id="card" value="#{payment.card}" label="#{msgs.creditCard}"
               		required="true">
                  <f:validateLength minimum="13"/>
                  <f:converter converterId="ltj.msgui.CreditCard"/>
                  <f:attribute name="separator" value="-"/>
                  <f:validator validatorId="ltj.msgui.CreditCard"/>
               </h:inputText>
               <h:message for="card" styleClass="errorMessage"/>

               <h:outputText value="#{msgs.expirationDate}"/>
               <h:inputText id="date" value="#{payment.date}" label="#{msgs.expirationDate}"
               		required="true" validator="#{payment.checkExpirationDate}">
                  <f:convertDateTime pattern="MM/yyyy"/>
               </h:inputText>
               <h:message for="date" styleClass="errorMessage"/>
            </h:panelGrid>
            <h:commandButton value="#{msgs.process}" action="process"/>
            <h:commandButton value="Cancel" action="cancel" immediate="true"/>
         </h:form>
      </body>
   </f:view>
</html>
