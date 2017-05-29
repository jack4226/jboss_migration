/*
 * net/balusc/webapp/MultipartFilter.java
 * 
 * Copyright (C) 2007 BalusC
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package jpa.msgui.filter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 * Check for multipart HttpServletRequests and parse the multipart form data so
 * that all regular form fields are available in the parameter map of the
 * HttpServletRequest and that all form file fields are available as attribute
 * of the HttpServletRequest. The attribute value of a form file field can be an
 * instance of FileItem or FileUploadException.
 */
@WebFilter(filterName="multipartFilter", urlPatterns="/upload/*",
	description="Check for multipart HttpServletRequests and parse the multipart form data",
	initParams={
		@WebInitParam(name="maxFileSize",value="5242880"),
		@WebInitParam(name="totalFileSize",value="10485760")
	}
)
public class MultipartFilter implements Filter {
	/*
	 * This filter requires that the following JAR's (newer versions are allowed) in
	 * the class path, e.g. /WEB-INF/lib
	 * <ul>
	 *  <li>commons-fileupload-1.2.jar</li>
	 * 	<li>commons-io-1.3.2.jar</li>
	 * </ul>
	 * <p>
	 * 
	 * This filter should be defined as follows in the web.xml: 
	 * <pre>
	 *  <filter>
	 * 		<description>
	 * 		Check for multipart HttpServletRequests and parse the multipart
	 * 		form data so that all regular form fields are available in the 
	 * 		parameter map of the HttpServletRequest and that all form file 
	 * 		fields are available as	attribute of the HttpServletRequest. 
	 * 		The attribute value of a form file field can be an instance of
	 * 		FileItem or FileUploadException.
	 *  	</description>
	 * 		<filter-name>multipartFilter</filter-name>
	 * 		<filter-class>jpa.msgui.filter.MultipartFilter</filter-class>
	 * 		<init-param>
	 * 			<description>
	 * 			Sets the maximum file size of the uploaded file	in bytes. Set
	 * 			to 0 to indicate an unlimited file size. The value of 1048576
	 * 			indicates a maximum file size of 1MB. This parameter is not
	 * 			required and can be removed safely.
	 * 			</description>
	 *  		<param-name>maxFileSize</param-name>
	 *			<param-value>1048576</param-value>
	 *		</init-param>
	 * 	</filter>
	 * 
	 * 	<filter-mapping>
	 * 		<filter-name>multipartFilter</filter-name>
	 *  	<url-pattern>/*</url-pattern>
	 * 	</filter-mapping>
	 * </pre>
	 */

    private long maxFileSize = 0;
    private static long totalFileSize = 0;
    private int sizeThreshold = 1024;

    public static long getTotalFileSize() {
    	return totalFileSize;
    }
    
    /**
	 * Configure the maxFileSize parameter.
	 * 
	 * @throws ServletException
	 *             If maxFileSize parameter value is not numeric.
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
    public void init(FilterConfig filterConfig) throws ServletException {
        // retrieve maxFileSize
        String maxFileSize = filterConfig.getInitParameter("maxFileSize");
        if (maxFileSize != null) {
            if (!maxFileSize.matches("^\\d+$")) {
                throw new ServletException("MultipartFilter 'maxFileSize' is not numeric.");
            }
            this.maxFileSize = Long.parseLong(maxFileSize);
        }
        String _totalFileSize = filterConfig.getInitParameter("totalFileSize");
        if (_totalFileSize != null) {
            if (!_totalFileSize.matches("^\\d+$")) {
                throw new ServletException("MultipartFilter 'totalFileSize' is not numeric.");
            }
            totalFileSize = Long.parseLong(_totalFileSize);
        }
    }

    /**
	 * Check the request type and if it is a HttpServletRequest, then parse the
	 * request.
	 * 
	 * @throws ServletException
	 *             If parsing of the given HttpServletRequest fails.
	 * @see javax.servlet.Filter#doFilter( javax.servlet.ServletRequest,
	 *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		// Check request type
		if (request instanceof HttpServletRequest) {
			// Cast to HttpServletRequest
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			// Parse HttpServletRequest
			HttpServletRequest parsedRequest = parseRequest(httpRequest);
			// Continue with filter chain
			chain.doFilter(parsedRequest, response);
		}
		else {
			// Not a HttpServletRequest.
			chain.doFilter(request, response);
		}
	}

    /**
	 * @see javax.servlet.Filter#destroy()
	 */
    public void destroy() {
        // do nothing.
    }

    /**
	 * Parse the given HttpServletRequest. If the request is a multipart
	 * request, then all multipart request items will be processed, else the
	 * request will be returned unchanged. During the processing of all
	 * multipart request items, the name and value of each regular form field
	 * will be added to the parameterMap of the HttpServletRequest. The name and
	 * File object of each form file field will be added as attribute of the
	 * given HttpServletRequest. If a FileUploadException has occurred when the
	 * file size has exceeded the maximum file size, then the
	 * FileUploadException will be added as attribute value instead of the
	 * FileItem object.
	 * 
	 * @param request
	 *            The HttpServletRequest to be checked and parsed as multipart
	 *            request.
	 * @return The parsed HttpServletRequest.
	 * @throws ServletException
	 *             If parsing of the given HttpServletRequest fails.
	 */
    private HttpServletRequest parseRequest(HttpServletRequest request) throws ServletException {
        // Check if the request is actually a multipart/form-data request
        if (!ServletFileUpload.isMultipartContent(request)) {
            // If not, return the request unchanged
            return request;
        }
        // Prepare the multipart request items.
        List<FileItem> multipartItems = null;
        try {
        	DiskFileItemFactory factory = new DiskFileItemFactory();
        	if (sizeThreshold > 0) {
        		factory.setSizeThreshold(sizeThreshold);
        	}
            // Parse the multipart request items.
            multipartItems = new ServletFileUpload(factory).parseRequest(request);
            // Note: we could use ServletFileUpload.setFileSizeMax() here, but that would throw a
            // FileUploadException immediately without processing the other fields. So we're
            // checking the file size only if the items are already parsed. See processFileField().
        } catch (FileUploadException e) {
            throw new ServletException("Cannot parse multipart request: " + e.getMessage());
        }
        // Prepare the request parameter map
        Map<String, String[]> parameterMap = new HashMap<String, String[]>();
        // Loop through multipart request items
        for (FileItem multipartItem : multipartItems) {
            if (multipartItem.isFormField()) {
                // Process regular form field (input type="text|radio|checkbox|etc", select, etc)
                processFormField(multipartItem, parameterMap);
            } else {
                // Process form file field (input type="file")
                processFileField(multipartItem, request);
            }
        }
        // Wrap the request with the parameter map that we just created and return it.
        return wrapRequest(request, parameterMap);
    }

    /**
	 * Process multipart request item as regular form field. The name and value
	 * of each regular form field will be added to the given parameterMap.
	 * 
	 * @param formField
	 *            The form field to be processed.
	 * @param parameterMap
	 *            The parameterMap to be used for the HttpServletRequest.
	 */
    private void processFormField(FileItem formField, Map<String, String[]> parameterMap) {
        String name = formField.getFieldName();
        String value = formField.getString();
        String[] values = parameterMap.get(name);
        if (values == null) {
            // Not in parameter map yet
            parameterMap.put(name, new String[] { value });
        } else {
            // Multiple field values, so add to existing array.
            int length = values.length;
            String[] newValues = new String[length + 1];
            System.arraycopy(values, 0, newValues, 0, length);
            newValues[length] = value;
            parameterMap.put(name, newValues);
        }
    }

    /**
	 * Process multipart request item as file field. The name and FileItem
	 * object of each file field will be added as attribute of the given
	 * HttpServletRequest. If a FileUploadException has occurred when the file
	 * size has exceeded the maximum file size, then the FileUploadException
	 * will be added as attribute value instead of the FileItem object.
	 * 
	 * @param fileField
	 *            The file field to be processed.
	 * @param request
	 *            The involved HttpServletRequest.
	 */
    private void processFileField(FileItem fileField, HttpServletRequest request) {
        if (fileField.getName().length() <= 0) {
            // No file uploaded.
            request.setAttribute(fileField.getFieldName(), null);
        } else if (maxFileSize > 0 && fileField.getSize() > maxFileSize) {
            // File size exceeded maximum file size.
            request.setAttribute(fileField.getFieldName(), new FileUploadException(
                "File size exceeded maximum file size of " + maxFileSize + " bytes."));
            // Immediately delete temporary file to free up memory and/or disk space.
            fileField.delete();
        } else {
            // File uploaded with good size.
            request.setAttribute(fileField.getFieldName(), fileField);
        }
    }

    /**
	 * Wrap the given HttpServletRequest with the given parameterMap.
	 * 
	 * @param request
	 *            An HttpServletRequest instance
	 * @param parameterMap
	 *            The parameterMap to be wrapped in the given
	 *            HttpServletRequest.
	 * @return The HttpServletRequest with the parameterMap wrapped in.
	 */
    private static HttpServletRequest wrapRequest(HttpServletRequest request,
			final Map<String, String[]> parameterMap) {
		return new HttpServletRequestWrapper(request) {
			public Map<String, String[]> getParameterMap() {
				return parameterMap;
			}
			public String[] getParameterValues(String name) {
				return parameterMap.get(name);
			}
			public String getParameter(String name) {
				String[] params = getParameterValues(name);
				return params != null && params.length > 0 ? params[0] : null;
			}
			public Enumeration<String> getParameterNames() {
				return Collections.enumeration(parameterMap.keySet());
			}
		};
	}

}