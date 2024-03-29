package jpa.message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.Part;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Represents a message body part structure.
 * <p>
 * The construction unit of email body parts. Consists of a group of fields and
 * a list of links that points to its child body parts if any, accessible by
 * name and Iterator.
 * <p>
 */
public class BodypartBean implements Serializable {
	private static final long serialVersionUID = -6689047851876614015L;
	static final Logger logger = LogManager.getLogger(BodypartBean.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	protected static final String DEFAULT_CONTENT_TYPE =  "text/plain";
	
	protected final List<BodypartBean> attachParts = new ArrayList<BodypartBean>();
	protected final List<MsgHeader> headers = new ArrayList<MsgHeader>();
	protected int size = 0;
	protected String disposition = null, description = null;
	protected String fileName = null;
	protected byte[] value = null;
	protected String contentType = DEFAULT_CONTENT_TYPE;
	
	protected final static String LF = System.getProperty("line.separator", "\n");

	/**
	 * Constructs a body part.
	 */
	public BodypartBean() {
		this(DEFAULT_CONTENT_TYPE, null);
	}

	/**
	 * Constructs a body part of specified mime type.
	 * 
	 * @param contectType -
	 *            mime type
	 */
	public BodypartBean(String contentType) {
		this(contentType, null);
	}

	/**
	 * Constructs a body part of specified mime type and value.
	 * 
	 * @param contectType -
	 *            mime type
	 * @param value -
	 *            body part node value
	 */
	public BodypartBean(String contentType, Object value) {
		if (contentType == null)
			this.contentType = DEFAULT_CONTENT_TYPE;
		else
			this.contentType = contentType;
		setValue(value);
	}

	/**
	 * add a child body part
	 * 
	 * @param subNode -
	 *            part to be added as a child
	 */
	public void put(BodypartBean subNode) {
		attachParts.add(subNode);
	}

	/**
	 * Return an iterator of its child body parts
	 * 
	 * @return an iterator of this object
	 */
	public Iterator<BodypartBean> getIterator() {
		return attachParts.iterator();
	}

	/**
	 * Return the Content type of this body part
	 * 
	 * @return content type
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * Return the mime type of this body part, all characters are converted to lower case.
	 * 
	 * @return mime type, the first part of content type
	 */
	public String getMimeType() {
		String str = "unknown";
		StringTokenizer st = new StringTokenizer(contentType.trim(), ";");
		if (st.hasMoreTokens()) {
			str = st.nextToken();
			// str would look like:
			// text/plain, text/html, multipart/mixed, multipart/alternative,
			// multipart/related, or application/octec-stream, etc.
		}

		return str.toLowerCase(); // iPlanet uses Upper case for mime type
	}

	/**
	 * Return the mime sub-type of this body part
	 * 
	 * @return mime subtype
	 */
	public String getMimeSubType() {
		String mimeType = getMimeType();
		if (mimeType.indexOf("/") > 0 && mimeType.indexOf("/") < mimeType.length()) {
			return mimeType.substring(mimeType.indexOf("/") + 1);
		}
		else {
			return "";
		}
	}

	/**
	 * <pre>
	 * 	 Typical email message structures
	 * 	 
	 * 	 level	mime type			disposition	description		body
	 * 	 -----	------------------	-----------	-----------		----
	 * 	 
	 * 	 1) plain text email
	 * 	 0		text/plain			null		null			Yes
	 * 	 
	 * 	 2) plain text email with attachments
	 * 	 0		multipart/mixed		null		null
	 * 	 1		.text/plain			inline		null			Yes
	 * 	 1		.text/html			inline		filename
	 * 	 1		.image/gif			inline		filename
	 * 	 1		.application/msword	attachment	filename
	 * 	 
	 * 	 3) html text email
	 * 	 0		multipart/
	 * 				alternative		null		null
	 * 	 1		.text/plain			null		null			Yes
	 * 	 1		.text/html			null		null			Yes
	 * 	 
	 * 	 4) html text email with attachments
	 * 	 0		multipart/mixed		null		null
	 * 	 1		.multipart/
	 * 				alternative		null		null
	 * 	 2		..text/plain		null		null			Yes
	 * 	 2		..text/html			null		null			Yes
	 * 	 1		.text/html			inline		filename
	 * 	 1		.image/gif			inline		filename
	 * 	 
	 * 	 5) html text email with inline image
	 * 	 0		multipart/
	 * 				alternative		null		null
	 * 	 1		.text/plain			null		null			Yes
	 *   1		.multipart/related	null		null
	 * 	 2		..text/html			inline		null			Yes
	 * 	 2		..image/gif			inline		null			Yes
	 * 	 
	 * 	 6) bounced email
	 * 	 0		multipart/report	null		null
	 * 	 1		.text/plain			null		null			Yes
	 * 	 1		.message/
	 * 				delivery-status null		null			error status
	 * 	 1		.message/rfc822		null		null
	 * 	 2		..text/plain		null		null			error message
	 * 	
	 * </pre>
	 */
	/**
	 * return the message body text. The initial call must be originated from
	 * MessageBean.
	 * 
	 * @param level -
	 *            structure tree level
	 * @param hashMap -
	 *            plain text alternative is saved here
	 * @return body text
	 */
	String getBody(int level, Map<String, Object> hashMap) {
		StringBuffer sb = new StringBuffer();
		String label = LF + "content-type: ";
		boolean showInlineContentType = false;
		String this_mtype = this.getMimeType();
		if (this_mtype.startsWith("text")) {
			// exclude attachment body parts
			if (!Part.ATTACHMENT.equals(getDisposition())) {
				if (level > 0 && !this_mtype.startsWith("text/html")) {
					if (showInlineContentType)
						sb.append(label + this.getContentType() + LF + LF);
				}
				if (getValue() != null) {
					sb.append(new String(getValue()));
				}
			}
		}
		else if (this_mtype.startsWith("multipart/alternative")) {
			// alternative sub type, get the last text alternative
			String content_type = "text/plain";
			byte[] textBody = null;
			// get the last text alternative (order: text/plain -> text/html)
			for (BodypartBean subNode : getNodes()) {
				if (subNode.getMimeType().startsWith("text")) {
					content_type = subNode.getContentType();
					textBody = subNode.getValue();
					if (subNode.getMimeType().startsWith("text/plain")) {
						// save the plain text version of the email
						if (!hashMap.containsKey(MessageBean.MSG_BODY_TEXT)) {
							if (textBody != null) {
								hashMap.put(MessageBean.MSG_BODY_TEXT, new String(textBody));
							}
						}
					}
				}
				else if (subNode.getMimeType().startsWith("multipart/related")) {
					String bodyStr = subNode.getBody(level + 1, hashMap);
					content_type = subNode.getBodyContentType(level + 1);
					if (content_type != null && content_type.startsWith("text")) {
						textBody = bodyStr.getBytes();
					}
				}
			} // end for loop
			if (textBody != null) {
				String txt = new String(textBody);
				if (level > 0 && !content_type.startsWith("text/html")) {
					if (showInlineContentType)
						sb.append(label + content_type + LF + LF);
				}
				sb.append(txt);
			}
		}
		else if (this_mtype.startsWith("multipart"))  {
			for (Iterator<BodypartBean> it = getIterator(); it.hasNext();) {
				BodypartBean subNode = it.next();
				String strBody = subNode.getBody(level + 1, hashMap);
				sb.append(strBody);
			}
		}
		else if (this_mtype.startsWith("application")) {
			// application mime type, ignore
		}
		else if (this_mtype.startsWith("message")) {
			// message mime type, include all of its text contents into the
			// body.
			if (this.getHeaders() != null) {
				// include header data into the body
				for (int i = 0; i < this.getHeaders().size(); i++) {
					MsgHeader header = this.getHeaders().get(i);
					sb.append(header.getName() + ": " + header.getValue() + LF);
				}
			}
			// if it contains body text, include it into the body
			byte[] textBody = (byte[]) getValue();
			if (textBody != null) {
				String txt = new String(textBody);
				if (txt.trim().length() > 0) { // contains text
					//if (showInlineContentType) {
					//	sb.append(label + this.getContentType() + LF + LF);
					//}
					sb.append(new String(textBody));
				}
			}
			// if there are body parts, get them first (rfc822 sub type contains
			// a body part)
			for (Iterator<BodypartBean> it = getIterator(); it.hasNext();) {
				BodypartBean subNode = it.next();
				String strBody = subNode.getBody(level + 1, hashMap);
				if (strBody.trim().length() > 0) {
					//if (showInlineContentType) {
					//	sb.append(label + getContentType() + LF + LF);
					//	// XXX: in order to display "message/rfc822" (remove?)
					//}
					sb.append(strBody);
				}
			}
		}
		return sb.toString();
	}

	/**
	 * return the message body content type by looking for "text/*" content types.
	 * The initial call must be originated from MessageBean.
	 * 
	 * @param level -
	 *            structure tree level
	 * @return body content type
	 */
	String getBodyContentType(int level) {
		String bodyType = null;
		String this_mtype = this.getMimeType();
		if (this_mtype.startsWith("text")) {
			// exclude obvious body parts
			if (!Part.ATTACHMENT.equals(getDisposition())) {
				bodyType = getContentType();
			}
		}
		else if (this_mtype.startsWith("multipart/alternative")) {
			// get the last text alternative (order: text/plain -> text/html)
			for (Iterator<BodypartBean> it = getIterator(); it.hasNext();) {
				BodypartBean aNode = it.next();
				if (aNode.getMimeType().startsWith("text")) {
					bodyType = aNode.getBodyContentType(level + 1);
				}
				else if (aNode.getMimeType().startsWith("multipart/related")) {
					String type = aNode.getBodyContentType(level + 1);
					if (type != null && type.startsWith("text")) {
						bodyType = type;
					}
				}
			}
		}
		else if (this_mtype.startsWith("multipart")) {
			// if it's again a multipart, go deeper for the body content type
			for (Iterator<BodypartBean> it = getIterator(); it.hasNext();) {
				BodypartBean subNode = it.next();
				bodyType = subNode.getBodyContentType(level + 1);
				if (bodyType != null)
					break;
			}
		}
		return bodyType;
	}

	/**
	 * get a BodypartBean that holds the message text. The initial call must be
	 * originated from MessageBean.
	 * 
	 * @param level
	 * @return a BodypartBean
	 */
	BodypartBean getBodyNode(int level) {
		BodypartBean bodyNode = null;
		String this_mtype = this.getMimeType();
		if (this_mtype.startsWith("text")) {
			// exclude obvious body parts
			if (!Part.ATTACHMENT.equals(getDisposition())) {
				bodyNode = this;
			}
		}
		else if (this_mtype.startsWith("multipart/alternative")) {
			// get the last text alternative (order: text/plain -> text/html)
			for (Iterator<BodypartBean> it = getIterator(); it.hasNext();) {
				BodypartBean aNode = it.next();
				if (aNode.getMimeType().startsWith("text")) {
					bodyNode = aNode.getBodyNode(level + 1);
				}
				else if (aNode.getMimeType().startsWith("multipart/related")) {
					BodypartBean node = aNode.getBodyNode(level + 1);
					if (node != null && node.getMimeType().startsWith("text")) {
						bodyNode = node;
					}
				}
			}
		}
		else if (this_mtype.startsWith("multipart")) {
			// if it's again a multipart, go deeper for the body node
			for (Iterator<BodypartBean> it = getIterator(); it.hasNext();) {
				BodypartBean subNode = it.next();
				bodyNode = subNode.getBodyNode(level + 1);
				if (bodyNode != null)
					break;
			}
		}
		return bodyNode;
	}

	/**
	 * @return disposition
	 */
	public String getDisposition() {
		return disposition;
	}

	/**
	 * @return description
	 */
	public String getDescription() {
		return description;
	}

	public String getFileName() {
		return fileName;
	}

	/**
	 * @return value as a byte array
	 */
	public byte[] getValue() {
		return this.value;
	}

	/**
	 * @return headers of the body part
	 */
	public List<MsgHeader> getHeaders() {
		return this.headers;
	}

	/**
	 * get header by name.
	 * @param headerName
	 * @return headers matches the name.
	 */
	public List<MsgHeader> getHeader(String headerName) {
		List<MsgHeader> hdrs = new ArrayList<MsgHeader>();
		if (headers!=null) {
			for (MsgHeader hdr : headers) {
				if (headerName.equalsIgnoreCase(hdr.getName())) {
					hdrs.add(hdr);
				}
			}
		}
		return hdrs;
	}
	
	/**
	 * remove header by name.
	 * @param headerName
	 * @return headers removed
	 */
	public List<MsgHeader> removeHeader(String headerName) {
		List<MsgHeader> hdrs = new ArrayList<MsgHeader>();
		if (headers!=null) {
			for (MsgHeader hdr : headers) {
				if (headerName.equalsIgnoreCase(hdr.getName())) {
					headers.remove(hdr);
					hdrs.add(hdr);
				}
			}
		}
		return hdrs;
	}

	/**
	 * @return size of the body part
	 */
	public int getSize() {
		return this.size;
	}

	/**
	 * Set content type of this body part
	 * 
	 * @param contentType -
	 *            content type
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	/**
	 * Set the value of this body part from an input object. Valid object type:
	 * <ul>
	 * <li>String</li>
	 * <li>InputStream</li>
	 * <li>byte array (byte[])</li>
	 * <li>a null value</li>
	 * </ul>
	 * @param value -
	 *            node value
	 */
	public final void setValue(Object value) {
		if (value instanceof String)
			setValue((String) value);
		else if (value instanceof InputStream)
			setValue((InputStream) value);
		else if (value instanceof byte[])
			this.value = (byte[]) value;
		else if (value == null)
			this.value = null;
		else
			throw new IllegalArgumentException("The input was not a type as expected");
	}

	/**
	 * Set disposition
	 * 
	 * @param disposition
	 */
	public void setDisposition(String disposition) {
		this.disposition = disposition;
	}

	/**
	 * Set description
	 * 
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	/**
	 * Set a string value
	 * 
	 * @param value -
	 *            part value
	 */
	protected final void setValue(String value) {
		try {
			this.value = value.getBytes("iso-8859-1"); // mail-safe
		}
		catch (UnsupportedEncodingException uex) {
			logger.warn("UnsupportedEncodingException caught: " + uex.getMessage());
			// fall back
			this.value = value.getBytes();
		}
	}

	/**
	 * Set value from an input stream. The input stream will be read into a byte
	 * array.
	 * 
	 * @param value -
	 *            an InputStream
	 */
	protected final void setValue(InputStream value) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OutputStreamWriter osw = new OutputStreamWriter(baos);
		try {
			IOUtils.copy(value, osw, Charset.defaultCharset());
		}
		catch (IOException e) {
			logger.error("IOExcetion caught", e);
		}
		finally {
			try {
				osw.close();
			}
			catch (IOException ignore) {
				logger.warn("IOException caught: " + ignore.getMessage());
			}
		}
		this.value = baos.toByteArray();
	}

	/**
	 * Set headers for this body part
	 * 
	 * @param headers -
	 *            headers in a List
	 */
	public void setHeaders(List<MsgHeader> headers) {
		this.headers.clear();
		this.headers.addAll(headers);
	}

	/**
	 * Set headers for this body part
	 * 
	 * @param part -
	 *            Part holds the data
	 * @throws MessagingException
	 */
	public void setHeaders(Part part) throws MessagingException {
		this.headers.clear();
		if (part==null) return;
		Enumeration<?> enu = part.getAllHeaders();
		while (enu.hasMoreElements()) {
			Header jmHdr = (Header) enu.nextElement();
			MsgHeader header = new MsgHeader();
			header.setName(jmHdr.getName());
			header.setValue(jmHdr.getValue());
			this.headers.add(header);
		}
	}

	/**
	 * set the value size of this body part
	 * 
	 * @param size
	 */
	public void setSize(int size) {
		this.size = size;
	}

	/**
	 * clean up this object
	 */
	public void clearParameters() {
		disposition = null;
		description = null;
		fileName = null;
		contentType = DEFAULT_CONTENT_TYPE;
		value = null;
		headers.clear();
		size = 0;
		attachParts.clear();
	}

	/**
	 * release all references to other objects
	 */
	public void destroy() {
		this.clearParameters();
	}

	/**
	 * @return child body parts in a list
	 */
	public List<BodypartBean> getNodes() {
		return attachParts;
	}

	/**
	 * print out the body part structure
	 * 
	 * @param level -
	 *            structure tree level
	 */
	public void print(int level) {
		System.out.println(toString(level));
	}

	/**
	 * convert the body part object to a string for printing
	 * 
	 * @param level -
	 *            structure tree level
	 * @return string
	 */
	public String toString(int level) {
		StringBuffer sb = new StringBuffer();
		sb.append("-> Level(" + level + ")****** BEGIN BodypartBean ******" + LF);

		sb.append("Mime Type: " + getContentType() + ", Disposition: " + disposition
				+ ", Description: " + description + LF);
		if (!(this instanceof MessageBean)) {
			for (int i = 0; i < headers.size(); i++) {
				MsgHeader hdr = (MsgHeader) headers.get(i);
				sb.append("Header Line - " + hdr.getName() + ": " + hdr.getValue() + LF);
			}
		}
		if (value != null) {
			if (getMimeType().indexOf("text") >= 0 || getMimeType().indexOf("message") >= 0)
				sb.append(new String(value) + LF);
			else
				sb.append("### Data contains nonprintable content. ###" + LF);
		}

		Iterator<?> it = getIterator();
		while (it.hasNext()) {
			sb.append(((BodypartBean) it.next()).toString(level + 1));
		}
		sb.append("<- Level(" + level + ")****** END BodypartBean ******" + LF);
		return sb.toString();
	}
}
