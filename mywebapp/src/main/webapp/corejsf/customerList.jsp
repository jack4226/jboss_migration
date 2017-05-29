<html>
   <%@ taglib uri="http://java.sun.com/jsf/core"  prefix="f" %>
   <%@ taglib uri="http://java.sun.com/jsf/html"  prefix="h" %>
   <f:view>
      <head>
         <link href="styles.css" rel="stylesheet" type="text/css"/>
         <title>
            <h:outputText value="#{msgs.pageTitle}"/>
         </title>
      </head>
      <body>
         <h:form>
         <h:messages errorClass="errors" layout="list" rendered="#{debug.showMessages}"/>
         <!-- Scrolling with a scroll bar -->
         <div style="overflow:auto; width:100%; height:200px;">
            <h:dataTable value="#{customers.all}" var="cust" 
               styleClass="jsfDataTable" 
               headerClass="dataTableHeader" columnClasses="oddColumns, evenColumns">
               
               <h:column>
                  <f:facet name="header">
                     <h:outputText value="#{msgs.deleteColumnHeader}"/>
                  </f:facet>
                  <h:selectBooleanCheckbox value="#{cust.markedForDeletion}" 
                     disabled="#{not cust.editable}" onchange="submit()"/>
               </h:column>
               <h:column>
                  <f:facet name="header">
                     <h:outputText value="#{msgs.customerIdHeader}"/>
                  </f:facet>
                  <h:commandLink action="#{customers.viewCustomer}">
                  	<h:outputText value="#{cust.custId}"/>
                  	<f:param name="custId" value="#{cust.custId}"/>
                  </h:commandLink>
               </h:column>
               <h:column>
                  <f:facet name="header">
                     <h:outputText value="#{msgs.nameHeader}"/>
                  </f:facet>
                  <h:outputText value="#{cust.firstName} #{cust.lastName}"/>
               </h:column>
               <h:column>
                  <f:facet name="header">
                     <h:outputText value="#{msgs.phoneHeader}"/>
                  </f:facet>
                  <h:outputText value="#{cust.dayPhone}"/>
               </h:column>
               <h:column>
                  <f:facet name="header">
                     <h:outputText value="#{msgs.addressHeader}"/>
                  </f:facet>
                  <h:outputText value="#{cust.streetAddress}"/>
               </h:column>
               <h:column>
                  <f:facet name="header">
                     <h:outputText value="#{msgs.cityHeader}"/>
                  </f:facet>
                  <h:outputText value="#{cust.cityName}"/>
               </h:column>
               <h:column>
                  <f:facet name="header">
                     <h:outputText value="#{msgs.stateHeader}"/>
                  </f:facet>
                  <h:outputText value="#{cust.stateCode}"/>
               </h:column>
            </h:dataTable>
            <h:commandButton value="#{msgs.deleteButtonText}" 
               action="#{customers.deleteCustomers}" 
               disabled="#{not customers.anyCustomersMarkedForDeletion}"/>
            <f:verbatim>&nbsp;</f:verbatim>
            <h:commandButton value="#{msgs.refreshFromDB}" 
               action="#{customers.refresh}"/>
         </div>
         </h:form>
      </body>
   </f:view>
</html>
