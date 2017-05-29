<html>
   <%@ taglib uri="http://java.sun.com/jsf/core"  prefix="f" %>
   <%@ taglib uri="http://java.sun.com/jsf/html"  prefix="h" %>
   <%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

   <f:view>
      <head>               
         <link href="styles.css" rel="stylesheet" type="text/css"/>
         <title><h:outputText value="#{msgs.libraryWindowTitle}"/></title>
      </head>

      <body>
         <f:subview id="library">
            <h:form>
               <tiles:insertDefinition name="library" flush="false"/>
            </h:form>
         </f:subview>
      </body>
   </f:view>
</html>
