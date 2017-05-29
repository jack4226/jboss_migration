<html>
   <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
   <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
   <%@ taglib uri="http://msgui.legacytojava.com/tabbedpane" prefix="msgui" %>
   <f:view>
      <head>
         <link href="styles.css" rel="stylesheet" type="text/css"/>
         <title><h:outputText value="#{msgs.windowTitle}"/></title>
      </head>
      <body>
         <h:form>
            <msgui:tabbedPane styleClass="tabbedPane" 
                                  tabClass="tab"
                          selectedTabClass="selectedTab">
               <f:facet name="jefferson">
                  <h:panelGrid columns="2" styleClass="tabbedPaneBody">
                     <h:graphicImage value="/images/jefferson.jpg"/>
                     <h:outputText value="#{msgs.jeffersonDiscussion}" 
                        styleClass="tabbedPaneContent"/>
                  </h:panelGrid>
               </f:facet>
               <f:facet name="roosevelt">
                  <h:panelGrid columns="2" styleClass="tabbedPaneBody">
                     <h:graphicImage value="/images/roosevelt.jpg"/>
                     <h:outputText value="#{msgs.rooseveltDiscussion}" 
                        styleClass="tabbedPaneContent"/>
                  </h:panelGrid>
               </f:facet>
               <f:facet name="lincoln">
                  <h:panelGrid columns="2" styleClass="tabbedPaneBody">
                     <h:graphicImage value="/images/lincoln.jpg"/>
                     <h:outputText value="#{msgs.lincolnDiscussion}" 
                        styleClass="tabbedPaneContent"/>
                  </h:panelGrid>
               </f:facet>
               <f:facet name="washington">
                  <h:panelGrid columns="2" styleClass="tabbedPaneBody">
                     <h:graphicImage value="/images/washington.jpg"/>
                     <h:outputText value="#{msgs.washingtonDiscussion}" 
                        styleClass="tabbedPaneContent"/>
                  </h:panelGrid>
               </f:facet>

               <f:selectItem itemLabel="#{msgs.jeffersonTabText}"
                  itemValue="jefferson"/>
               <f:selectItem itemLabel="#{msgs.rooseveltTabText}"  
                  itemValue="roosevelt"/>
               <f:selectItem itemLabel="#{msgs.lincolnTabText}"    
                  itemValue="lincoln"/>
               <f:selectItem itemLabel="#{msgs.washingtonTabText}" 
                  itemValue="washington"/>
            </msgui:tabbedPane>
            <h:commandButton value="Refresh"/>
         </h:form>
      </body>
   </f:view>
</html>
