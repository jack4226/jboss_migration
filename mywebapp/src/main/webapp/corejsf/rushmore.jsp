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
         <h:outputText value="#{msgs.instructions}"
            styleClass="instructions"/>
         <h:form>
            <h:commandButton image="images/mountrushmore.jpg"
               styleClass="imageButton"
               actionListener="#{rushmore.handleMouseClick}"
               action="#{rushmore.navigate}"/>
         </h:form>
      </body>
   </f:view>
</html>
