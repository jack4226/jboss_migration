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
      <body style="background: #eee">
         <h:form>
         <h:messages errorClass="errors" layout="list" rendered="#{debug.showMessages}"/>
            <h:dataTable value="#{numberList}" var="number">
               <h:column>
                  <f:facet name="header">
                     <h:outputText value="#{msgs.numberHeader}"/>
                  </f:facet>
                  <h:outputText value="#{number}"/>
               </h:column>
               <h:column>
                  <f:facet name="header">
                     <h:outputText value="#{msgs.textfieldHeader}"/>
                  </f:facet>
                  <h:inputText value="#{number}" size="3"/>
               </h:column>
               <h:column>
                  <f:facet name="header">
                     <h:outputText value="#{msgs.buttonHeader}"/>
                  </f:facet>
                  <h:commandButton value="#{number}"/>
               </h:column>
               <h:column>
                  <f:facet name="header">
                     <h:outputText value="#{msgs.checkboxHeader}"/>
                  </f:facet>
                  <h:selectBooleanCheckbox value="false"/>
               </h:column>
               <h:column>
                  <f:facet name="header">
                     <h:outputText value="#{msgs.linkHeader}"/>
                  </f:facet>
                  <h:commandLink>
                     <h:outputText value="#{number}"/>
                  </h:commandLink>
               </h:column>
               <h:column>
                  <f:facet name="header">
                     <h:outputText value="#{msgs.graphicHeader}"/>
                  </f:facet>
                  <h:graphicImage value="images/dice#{number}.gif" 
                     style="border: 0px"/>
               </h:column>
               <h:column>
                  <f:facet name="header">
                     <h:outputText value="#{msgs.menuHeader}"/>
                  </f:facet>
                  <h:selectOneMenu>
                     <f:selectItem itemLabel="#{number}" itemValue="#{number}"/>
                  </h:selectOneMenu>
               </h:column>
               <h:column>
                  <f:facet name="header">
                     <h:outputText value="#{msgs.radioHeader}"/>
                  </f:facet>
                  <h:selectOneRadio layout="LINE_DIRECTION" value="nextMonth">
                     <f:selectItem itemValue="yes" itemLabel="yes"/>
                     <f:selectItem itemValue="no" itemLabel="no" />
                  </h:selectOneRadio>
               </h:column>
               <h:column>
                  <f:facet name="header">
                     <h:outputText value="#{msgs.listboxHeader}"/>
                  </f:facet>
                  <h:selectOneListbox size="3">
                     <f:selectItem itemValue="yes" itemLabel="yes"/>
                     <f:selectItem itemValue="maybe" itemLabel="maybe"/>
                     <f:selectItem itemValue="no" itemLabel="no" />
                     <f:selectItem itemValue="ok" itemLabel="ok" />
                  </h:selectOneListbox>
               </h:column>
            </h:dataTable>
         </h:form>
      </body>
   </f:view>
</html>
