<%
	// The path is relative to the application context, and the file is placed
	// under the WEB-INF folder of the application
	String imageName = "space.gif";
	java.net.URL url = application.getResource("/publicsite/images/" + imageName);
	//config.getServletContext().getResourceAsStream("/publicsite/images/" + imageName);
	if (url != null) {
		String contentType = application.getMimeType(url.getFile());
		System.out.println("Image file path: " + url.getPath() + ", ContentType: " + contentType);
		java.io.InputStream input = null;
		java.io.BufferedOutputStream output = null;
		try {
			input = url.openStream();
			response.reset();
			response.setContentType(contentType);
			response.setContentLength(input.available());
			response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
			response.setHeader("Expires", "0");
			response.setHeader("Content-disposition", "attachment; filename=\"" + imageName + "\"");
			output = new java.io.BufferedOutputStream(response.getOutputStream());
			// Write file contents to response.
			byte[] buffer = new byte[256];
			for (int length; (length = input.read(buffer)) != -1;) {
				output.write(buffer, 0, length);
			}
			output.flush();
		}
		catch (java.io.IOException e) {
			System.err.println("IOException caught: " +  e.toString());
		}
		finally {
			close(output);
			close(input);
			// add the following at the end of JSP page to avoid the IllegalStateException:
			// java.lang.IllegalStateException: getOutputStream() has already been called for this response
			out.clear();
			//out = pageContext.pushBody();
		}
	}
%><%!
	private static void close(java.io.Closeable resource) {
		if (resource != null) {
			try {
				resource.close();
			}
			catch (java.io.IOException e) {
				System.err.println("IOException caught: " +  e.toString());
			}
		}
	}

	//Yet another way of opening files is by using the ClassLoader API
	//A ClassLoader instance is obtained as shown below. This instance must be the
	//one which loads the classes in the application.This is ensured by getting
	//the instance via the current thread object.
	//Note that url will be null if the file doesn't exist. We assume that there
	//is an file "test.txt" under /WEB-INF/classes
	
	//url = Thread.currentThread().getContextClassLoader().getResource("test.txt");
	%>