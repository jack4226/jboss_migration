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
         <h:messages errorClass="errors" layout="list" rendered="#{debug.showMessages}"/>
            <h1><h:outputText value="#{msgs.paymentInformation}"/></h1>
            <h:panelGrid columns="2">
               <h:outputText value="#{msgs.amount}"/>
               <h:outputText value="#{payment.amount}"> 
                  <f:convertNumber type="currency"/>
               </h:outputText>

               <h:outputText value="#{msgs.creditCard}"/>
               <h:outputText value="#{payment.card}">
               	<f:converter converterId="ltj.msgui.CreditCard"/>
               	<f:attribute name="separator" value="-"/>
               </h:outputText>

               <h:outputText value="#{msgs.expirationDate}"/>
               <h:outputText value="#{payment.date}"> 
                  <f:convertDateTime pattern="MM/yyyy"/>
               </h:outputText>
            </h:panelGrid>
            <h:commandButton value="Back" action="back"/>
         </h:form>
      </body>
   </f:view>
</html>
