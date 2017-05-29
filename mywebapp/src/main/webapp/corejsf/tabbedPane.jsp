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
      <div align="center">
         <h:form id="panes">
            <h:panelGrid id="outerPane" styleClass="tabbedPane" columnClasses="displayPanel">
               <%-- Tabs --%>
               <f:facet name="header">
                <h:panelGrid columns="5" styleClass="tabbedPaneHeader">
					<h:commandLink tabindex="1" title="#{tp.jeffersonTooltip}"
						styleClass="#{tp.jeffersonStyle}"
						actionListener="#{tp.jeffersonAction}">
						<h:outputText value="#{msgs.jeffersonTabText}" />
					</h:commandLink>

					<h:commandLink tabindex="2" title="#{tp.rooseveltTooltip}"
						styleClass="#{tp.rooseveltStyle}"
						actionListener="#{tp.rooseveltAction}">
						<h:outputText value="#{msgs.rooseveltTabText}" />
					</h:commandLink>

					<h:commandLink tabindex="3" title="#{tp.lincolnTooltip}"
						styleClass="#{tp.lincolnStyle}"
						actionListener="#{tp.lincolnAction}">
						<h:outputText value="#{msgs.lincolnTabText}" />
					</h:commandLink>

					<h:commandLink tabindex="4" title="#{tp.washingtonTooltip}"
						styleClass="#{tp.washingtonStyle}"
						actionListener="#{tp.washingtonAction}">
						<h:outputText value="#{msgs.washingtonTabText}" />
					</h:commandLink>
				</h:panelGrid>
               </f:facet>

               <%-- Tabbed pane content --%>
			<%--
               <%@ include file="washington.jsp" %>   
               <%@ include file="roosevelt.jsp" %>   
               <%@ include file="lincoln.jsp" %>   
               <%@ include file="jefferson.jsp" %>
            --%>

			<h:panelGrid columns="2" columnClasses="presidentDiscussionColumn"
				styleClass="tabbedPaneBody"
				rendered="#{tp.washingtonCurrent}">
				<h:graphicImage value="/images/washington.jpg" />
				<h:outputText value="#{msgs.washingtonDiscussion}"
					styleClass="tabbedPaneContent" />
			</h:panelGrid>

			<h:panelGrid columns="2" columnClasses="presidentDiscussionColumn"
				styleClass="tabbedPaneBody"
				rendered="#{tp.rooseveltCurrent}">
				<h:graphicImage value="/images/roosevelt.jpg" />
				<h:outputText value="#{msgs.rooseveltDiscussion}"
					styleClass="tabbedPaneContent" />
			</h:panelGrid>

			<h:panelGrid columns="2" columnClasses="presidentDiscussionColumn"
				styleClass="tabbedPaneBody"
				rendered="#{tp.lincolnCurrent}">
				<h:graphicImage value="/images/lincoln.jpg" />
				<h:outputText value="#{msgs.lincolnDiscussion}"
					styleClass="tabbedPaneContent" />
			</h:panelGrid>

			<h:panelGrid columns="2" columnClasses="presidentDiscussionColumn"
				styleClass="tabbedPaneBody"
				rendered="#{tp.jeffersonCurrent}">
				<h:graphicImage value="/images/jefferson.jpg" />
				<h:outputText value="#{msgs.jeffersonDiscussion}"
					styleClass="tabbedPaneContent" />
			</h:panelGrid>

		</h:panelGrid>
         </h:form>
      </div>
      </body>
   </f:view>
</html>  
