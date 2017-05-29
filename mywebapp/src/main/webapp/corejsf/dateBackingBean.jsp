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
            <h1><h:outputText value="#{msgs.enterDate}"/></h1>
            <h:panelGrid columns="3">
               <h:outputText value="#{msgs.day}"/>
               <h:inputText value="#{datebb.day}" binding="#{datebb.dayInput}" 
                  size="2" required="true"/>
               <h:panelGroup/>
               
               <h:outputText value="#{msgs.month}"/>
               <h:inputText value="#{datebb.month}" binding="#{datebb.monthInput}" 
                  size="2" required="true"/>
               <h:panelGroup/>
               
               <h:outputText value="#{msgs.year}"/>
               <h:inputText id="year" value="#{datebb.year}" 
                  binding="#{datebb.yearInput}" size="4" required="true" 
                  validator="#{datebb.validateDate}"/>
               <h:message for="year" styleClass="errorMessage"/>
            </h:panelGrid>
            <h:commandButton value="#{msgs.submit}" action="submit"/>
         </h:form>
      </body>
   </f:view>
</html>
