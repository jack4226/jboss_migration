package jpa.message;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.mail.Part;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * provide utility methods to retrieve attachments from BodypartBean
 */
public final class BodypartUtil implements Serializable {
	private static final long serialVersionUID = -8920127339846912514L;
	static final Logger logger = LogManager.getLogger(BodypartUtil.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	static Logger rfcLogger = LogManager.getLogger("jpa.message.report.rfc");
	static boolean isRfcReportEnabled = rfcLogger.isInfoEnabled();

	final static String LF = System.getProperty("line.separator", "\n");
	
	private BodypartUtil() {
		// make it static only
	}
	
	/**
	 * Retrieve all attachments into a list. It also looks for delivery status
	 * report and rfc822 report. If it found a report, it saves the link to the
	 * report to a MessageBean field so it can be easily retrieved later. Please
	 * call this method before you call any other methods that will retrieve any
	 * delivery reports.
	 * 
	 * @param msgBean -
	 *            retrieving from
	 * @return a list of MessageNode's
	 */
	public static List<MessageNode> retrieveAttachments(MessageBean msgBean)	{
		msgBean.setAttachments(retrieveAttachments((BodypartBean) msgBean, msgBean, 1));
		return msgBean.getAttachments();
	}

	private static List<MessageNode> retrieveAttachments(BodypartBean aNode, MessageBean msgBean,
			int level) {
		if (level <= 1) {
			msgBean.setRfc822(null);
			msgBean.setReport(null);
		}
		
		List<MessageNode> aNodes = new ArrayList<MessageNode>();
		Iterator<BodypartBean> it = aNode.getIterator();
		while (it.hasNext()) {
			BodypartBean subNode = it.next();

			String disp = subNode.getDisposition();
			String desc = subNode.getDescription();
			String mtype = subNode.getMimeType();

			String nullInd = subNode.getValue() == null ? "null" : "not null";
			logger.info("retrieveAttachments(): level=" + level + ", mtype=" + mtype + ", "
					+ ", disp=" + disp + ", desc=" + desc + ", " + nullInd);

			if (Part.ATTACHMENT.equalsIgnoreCase(disp)
					|| (Part.INLINE.equalsIgnoreCase(disp) && desc != null)
					|| MessageBeanBuilder.getFileName(subNode.getContentType()) != null) {
				
				// this Node is an attachment
				aNodes.add(new MessageNode(subNode, level));
			}

			// find other attachments down from the Node
			List<MessageNode> subAttch = retrieveAttachments(subNode, msgBean, level + 1);
			if (subAttch!=null) {
				aNodes.addAll(subAttch);
			}

			// save the node that contains status report
			if (mtype.startsWith("message/rfc822") && msgBean.getRfc822() == null)
				msgBean.setRfc822(new MessageNode(subNode, level));
			if (mtype.startsWith("multipart/report") && msgBean.getReport() == null)
				msgBean.setReport(new MessageNode(subNode, level));
		}

		// root node could also be multipart/report content type
		String mtype = aNode.getMimeType();
		if (mtype.startsWith("multipart/report") && msgBean.getReport() == null) {
			String disp = aNode.getDisposition();
			String desc = aNode.getDescription();
			logger.info("retrieveAttachments(): level=" + (level - 1) + ", mtype=" + mtype
					+ ", disp=" + disp + ", desc=" + desc);
			msgBean.setReport(new MessageNode(aNode, level));
		}

		msgBean.setAttachments(aNodes);
		return msgBean.getAttachments();
	}

	public static List<BodypartBean> retrieveAlternatives(MessageBean msgBean) {
		List<BodypartBean> aNodes = null;
		if (msgBean.getContentType().startsWith("multipart/alternative")) {
			aNodes = msgBean.getNodes();
		}
		else if (msgBean.getContentType().startsWith("multipart/mixed")) {
			for (Iterator<BodypartBean> it = msgBean.getIterator(); it.hasNext();) {
				BodypartBean subNode = it.next();
				if (subNode.getContentType().startsWith("multipart/alternative")) {
					aNodes = subNode.getNodes();
				}
			}
		}
		if (aNodes == null) { // just for safety
			aNodes = new ArrayList<BodypartBean>();
		}
		if (aNodes.size() == 0) { // no alternatives, use body
			aNodes.add(msgBean);
		}

		return aNodes;
	}
	
	/**
	 * retrieve RFC822 component into a BodypartBean format
	 * @param aNode - root Node
	 * @param level
	 * @return a BodypartBean
	 */
	public static BodypartBean retrieveRfc822Text(BodypartBean aNode, int level) {
		List<BodypartBean> sNode = aNode.getNodes();
		if (sNode != null && sNode.size() > 0) {
			// message/rfc822 attaches a text node as its child body part.
			BodypartBean subNode = sNode.get(0); // only the first node
			String mtype = subNode.getMimeType();
			String disp = subNode.getDisposition();
			logger.info("retrieveRFC822Text() - proceeded to level " + level + ", mtype="
					+ mtype + ", disp=" + disp);
			if (mtype.startsWith("text")) {
				logger.info("retrieveRFC822Text() - found the child bodypart from level "
						+ level);
				logRfcHeadersAndText(subNode);
				return subNode;
			}
			else { // go deeper to get the text node
				return retrieveRfc822Text(subNode, level + 1);
			}
		}
		else { // message/rfc822 contains no sub nodes
			logger.info("retrieveRFC822Text() - missing the lower level node, check if it's a text/rfc822-headers.");
			String mtype = aNode.getMimeType();
			String disp = aNode.getDisposition();
			logger.info("retrieveRFC822Text() - proceeded to level " + level + ", mtype="
					+ mtype + ", disp=" + disp);
			if (mtype.startsWith("text/rfc822-headers")) {
				logger.info("retrieveRFC822Text() - found the text/rfc822-headers from level "
								+ level);
				logRfcHeadersAndText(aNode);
				return aNode;
			}
			else {
				logger.info("retrieveRFC822Text() - missing the lower level node and rfc822-headers, use it anyway.");
				logRfcHeadersAndText(aNode);
				return aNode;
			}
		}
	}

	private static void logRfcHeadersAndText(BodypartBean aNode) {
		if (isRfcReportEnabled) {
			/* write to report file */
			List<MsgHeader> vheader = aNode.getHeaders();
			if (vheader != null && !vheader.isEmpty()) {
				rfcLogger.info("=================RFC822-HEADERS BEGIN=================");
				for (int j = 0; j < vheader.size(); j++) {
					MsgHeader header = vheader.get(j);
					rfcLogger.info(header.getName() + ": " + header.getValue());
				}
				rfcLogger.info("==================RFC822-HEADERS END==================");
			}
			byte[] btext = (byte[]) aNode.getValue();
			if (btext != null && btext.length > 0) {
				rfcLogger.info("===================RFC822-TEXT BEGIN==================");
				rfcLogger.info(new String(btext));
				rfcLogger.info("====================RFC822-TEXT END===================");
			}
		}		
	}
	
	/**
	 * retrieve rfc1894/rfc1891 component in a BodypartBean format
	 * @param aNode - root Node
	 * @param level
	 * @return a BodypartBean
	 */
	public static BodypartBean retrieveDlvrStatus(BodypartBean aNode, int level) {
		// multipart/report could attach a status node as its child body part.
		List<BodypartBean> sNode = aNode.getNodes();
		for (int i = 0; sNode != null && i < sNode.size(); i++) {
			BodypartBean subNode = sNode.get(i);
			String mtype = subNode.getMimeType();
			String disp = subNode.getDisposition();
			logger.info("retrieveDlvrStatus() - proceeded to level " + level + ", mtype="
					+ mtype + ", disp=" + disp);
			if (mtype.startsWith("message/delivery-status")) {
				logger.info("retrieveDlvrStatus() - found message/delivery-status bodypart from level "
								+ level);
				if (isRfcReportEnabled) {
					/* write to report file */
					byte[] btext = (byte[]) subNode.getValue();
					if (btext != null && btext.length > 0) {
						rfcLogger.info("==================delivery-status BEGIN==================");
						rfcLogger.info(new String(btext));
						rfcLogger.info("===================delivery-status END===================");
					}
				}
				return subNode; // return delivery-status if one is found
			}
		}
		logger.info("retrieveDlvrStatus() - missing the lower level node or the lower level node has no text.");
		return null;
	}

	/**
	 * retrieve rfc3798 component in a BodypartBean format
	 * @param aNode - root Node
	 * @param level
	 * @return a BodypartBean
	 */
	public static BodypartBean retrieveMDNReceipt(BodypartBean aNode, int level) {
		// multipart/report could attach a MDN node as its child body part.
		List<BodypartBean> sNode = aNode.getNodes();
		for (int i = 0; sNode != null && i < sNode.size(); i++) {
			BodypartBean subNode = sNode.get(i);
			String mtype = subNode.getMimeType();
			String disp = subNode.getDisposition();
			logger.info("retrieveMDNReceipt() - proceeded to level " + level + ", mtype=" + mtype
					+ ", disp=" + disp);
			if (mtype.startsWith("message/disposition-notification")) {
				logger.info("retrieveMDNReceipt() - found message/disposition-notification "
						+ "bodypart from level " + level);
				if (isRfcReportEnabled) {
					/* write to report file */
					byte[] btext = (byte[]) subNode.getValue();
					if (btext != null && btext.length > 0) {
						rfcLogger.info("=============disposition-notification BEGIN=============");
						rfcLogger.info(new String(btext));
						rfcLogger.info("==============disposition-notification END==============");
					}
				}
				return subNode; // return disposition-notification if one is
								// found
			}
		}
		//logger.info("retrieveMDNReceipt() - missing the lower level node or the lower level node has no text.");
		return null;
	}

	/**
	 * retrieve rfc1894/rfc1891 components in a BodypartBean format
	 * @param aNode - root Node
	 * @param level
	 * @return a BodypartBean list
	 */
	public static List<BodypartBean> retrieveReportText(BodypartBean aNode, int level) {
		// multipart/report could attach text nodes as its sub body parts.
		List<BodypartBean> sNode = aNode.getNodes();
		List<BodypartBean> list = new ArrayList<BodypartBean>();
		for (int i = 0; sNode != null && i < sNode.size(); i++) {
			BodypartBean subNode = sNode.get(i);
			String mtype = subNode.getMimeType();
			String disp = subNode.getDisposition();
			logger.info("retrieveReportText() - proceeded to level " + level + ", mtype="
					+ mtype + ", disp=" + disp);
			if (mtype.startsWith("text") && !mtype.startsWith("text/rfc822-headers")) {
				logger.info("retrieveReportText() - found " + mtype + " bodypart from level "
						+ level);
				if (isRfcReportEnabled) {
					/* write to report file */
					byte[] btext = (byte[]) subNode.getValue();
					if (btext != null && btext.length > 0) {
						rfcLogger.info("==================ReportText BEGIN==================");
						rfcLogger.info(new String(btext));
						rfcLogger.info("===================ReportText END===================");
					}
				}
				list.add(subNode);
			}
		}
		return list;
	}

	/**
	 * retrieve rfc822 message in a BodypartBean format
	 * @param aNode - root Node
	 * @param level
	 * @return a BodypartBean
	 */
	public static BodypartBean retrieveMessageRfc822(BodypartBean aNode, int level) {
		// locate message/rfc822 section under multipart/report
		List<BodypartBean> sNode = aNode.getNodes();
		for (int i = 0; sNode != null && i < sNode.size(); i++) {
			BodypartBean subNode = sNode.get(i);
			String mtype = subNode.getMimeType();
			String disp = subNode.getDisposition();
			logger.info("retrieveMessageRFC822() - proceeded to level " + level + ", mtype="
					+ mtype + ", disp=" + disp);
			if (mtype.startsWith("message/rfc822")) {
				logger.info("retrieveMessageRFC822() - found message/rfc822 section from level "
						+ level);
				return subNode;
			}
		}
		return null;
	}

	/**
	 * retrieve RFC822 headers component in a BodypartBean format
	 * @param aNode - root Node
	 * @param level
	 * @return a BodypartBean
	 */
	public static BodypartBean retrieveRfc822Headers(BodypartBean aNode, int level) {
		// locate text/rfc822-headers section under multipart/report
		List<BodypartBean> sNode = aNode.getNodes();
		for (int i = 0; sNode != null && i < sNode.size(); i++) {
			BodypartBean subNode = sNode.get(i);
			String mtype = subNode.getMimeType();
			String disp = subNode.getDisposition();
			logger.info("retrieveRFC822Headers() - proceeded to level " + level + ", mtype="
					+ mtype + ", disp=" + disp);
			if (mtype.startsWith("text/rfc822-headers")) {
				logger.info("retrieveRFC822Headers() - found message/rfc822 section from level "
						+ level);
				return subNode;
			}
		}
		return null;
	}
}
