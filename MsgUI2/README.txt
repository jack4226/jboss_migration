1) Dealing with “java.lang.OutOfMemoryError: PermGen space” error

The solution is to add these flags to JVM runtime command line

-XX:MaxPermSize=256M -XX:+CMSClassUnloadingEnabled -XX:+CMSPermGenSweepingEnabled


2) How do I enable the JULI logging in a Tomcat 7.0 Server instance?

Tomcat 7.0 comes with an enhanced implementation of java.util.logging, called JULI, which is configured
by default in a standard Tomcat 7.0 installation. This JULI logging configuration is not picked up 
automatically when creating a new Tomcat 7.0 server in WTP. Some manual steps are necessary to add this
configuration to your WTP Tomcat 7.0 server.
1.	Open the server editor for the Tomcat server and note the folder specified by the Server path field.
2.	Import the logging.properties file from the conf directory of your Tomcat 7.0 installation into this folder in your workspace.
3.	In the server editor, click on the Open launch configuration link and in the launch configuration Properties dialog, switch to the Arguments tab.
4.	In the VM Arguments field, add the following two system properties substituting the catalina.base path where noted:
     -Djava.util.logging.config.file="<put catalina.base path here>\conf\logging.properties"
     -Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager
The imported logging.properties file can be used to control the JULI logging configuration for the Tomcat server.


3) Copy jcharset.jar to <tomcat home>/endorsed folder to suppress unsupported encoding exception:

	java.io.UnsupportedEncodingException: unicode-1-1-utf-7
