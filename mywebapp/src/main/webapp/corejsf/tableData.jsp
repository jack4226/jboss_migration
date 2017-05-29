<html>
   <%@ taglib uri="http://java.sun.com/jsf/core"  prefix="f" %>
   <%@ taglib uri="http://java.sun.com/jsf/html"  prefix="h" %>
   <f:view>
      <head>
      	<link href="styles.css" rel="stylesheet" type="text/css"/> 
         <title>
            <h:outputText value="#{msgs.windowTitle}"/>
         </title>
      </head>

      <body>
         <h:outputText value="#{msgs.pageTitle}"/>
         <p>
         <h:form>
         <h:messages errorClass="errors" layout="list" rendered="#{debug.showMessages}"/>
            <h:dataTable value="#{tableData.names}" var="name"
                  headerClass="columnHeader"
                  footerClass="columnFooter"
                  rowClasses="oddRows, evenRows">
               <h:column>
                  <f:facet name="header">
                     <h:outputText value="#{msgs.editColumn}"/>
                  </f:facet>
                  <h:selectBooleanCheckbox value="#{name.editable}" 
                     onclick="submit()"/>
               </h:column>
               
               <h:column>
                  <f:facet name="header">
                     <h:outputText value="#{msgs.lastnameColumn}"/> 
                  </f:facet>
                  
                  <h:inputText value="#{name.last}" rendered="#{name.editable}" 
                     size="10"/>
                  <h:outputText value="#{name.last}" 
                     rendered="#{not name.editable}"/>
                     
                  <f:facet name="footer">
                     <h:outputText value="#{msgs.alphanumeric}"/> 
                  </f:facet>
               </h:column>

               <h:column>
                  <f:facet name="header">
                     <h:outputText value="#{msgs.firstnameColumn}"/> 
                  </f:facet>
                  
                  <h:inputText value="#{name.first}" 
                     rendered="#{name.editable}" size="10"/>
                  <h:outputText value="#{name.first}" 
                     rendered="#{not name.editable}"/>
                     
                  <f:facet name="footer">
                     <h:outputText value="#{msgs.alphanumeric}"/> 
                  </f:facet>
               </h:column>
            </h:dataTable>
            <h:commandButton value="#{msgs.saveChangesButtonText}"/>
         </h:form>
      </body>
   </f:view>
</html>
