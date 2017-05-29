package jpa.message.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jpa.message.MsgHeader;
import jpa.model.msg.MessageHeader;

import org.apache.log4j.Logger;

public class MsgHeaderUtil {
	static final Logger logger = Logger.getLogger(MsgHeaderUtil.class);
	static boolean isDebugEnabled = false; //logger.isDebugEnabled();
	static final String LF = System.getProperty("line.separator","\n");
	
	public static List<MsgHeader> messageHeaderList2MsgHeaderList(List<MessageHeader> messageHeaderList) {
		List<MsgHeader> list = new ArrayList<MsgHeader>();
		for (MessageHeader header : messageHeaderList) {
			MsgHeader msgHeader = new MsgHeader();
			msgHeader.setName(header.getHeaderName());
			msgHeader.setValue(header.getHeaderValue());
			list.add(msgHeader);
		}
		return list;
	}
	
	public static List<MsgHeader> parseRfc822Headers(String dsnRfc822) {
		Pattern p = Pattern.compile("^([\\w\\-]{2,40})\\:\\s(.*)");
		ByteArrayInputStream bais = new ByteArrayInputStream(dsnRfc822.getBytes());
		InputStreamReader isr = new InputStreamReader(bais);
		BufferedReader br = new BufferedReader(isr);
		List<MsgHeader> list = new ArrayList<MsgHeader>();
		String line = null;
		try {
			MsgHeader msgHeader = null;
			while ((line = br.readLine())!=null) {
				Matcher m = p.matcher(line);
				if (m.find() && m.groupCount()>=2) {
					for (int i=0; i<=m.groupCount(); i++) {
						if (isDebugEnabled) {
							logger.debug("[" + i +"]: " +m.group(i));
						}
					}
					msgHeader = new MsgHeader();
					msgHeader.setName(m.group(1));
					msgHeader.setValue(m.group(2));
					list.add(msgHeader);
				}
				else {
					if (isDebugEnabled) {
						logger.debug(line);
					}
					if (msgHeader!=null) {
						msgHeader.setValue(msgHeader.getValue() + LF + line);
					}
				}
			}
			return list;
		}
		catch (IOException e) {
			throw new RuntimeException("Failed to parse headers", e);
		}
	}

	public static String printHeaders(List<MsgHeader> list) {
		StringBuffer sb = new StringBuffer();
		for (MsgHeader header : list) {
			sb.append(header.getName() + ": " + header.getValue() + LF);
		}
		return sb.toString();
	}

	public static void main(String[] args) {
		String rfc822Headers = 
			"Received: from asp-6.reflexion.net ([205.237.99.181]) by MELMX.synnex.com.au with Microsoft SMTPSVC(6.0.3790.3959);" + LF +
			"	 Wed, 14 May 2008 08:50:31 +1000" + LF +
			"Received: (qmail 22433 invoked from network); 13 May 2008 22:47:49 -0000" + LF +
			"Received: from unknown (HELO asp-6.reflexion.net) (127.0.0.1)" + LF +
			"  by 0 (rfx-qmail) with SMTP; 13 May 2008 22:47:49 -0000" + LF +
			"Received: by asp-6.reflexion.net" + LF +
			"        (Reflexion email security v5.40.3) with SMTP;" + LF +
			"        Tue, 13 May 2008 18:47:49 -0400 (EDT)" + LF +
			"Received: (qmail 22418 invoked from network); 13 May 2008 22:47:48 -0000" + LF +
			"Received: from unknown (HELO WWW-2D1D2A59B52) (124.228.102.160)" + LF +
			"  by 0 (rfx-qmail) with SMTP; 13 May 2008 22:47:48 -0000" + LF +
			"Received:from $FROM_NAME $FROM_NAME(10.17.18.16) by WWW-2D1D2A59B52 (PowerMTA(TM) v3.2r4) id hfp02o32d12j39 for <jackwnn@synnex.com.au>; Wed, 14 May 2008 06:47:43 +0800 (envelope-from <jackwng@gmail.com>)" + LF +
			"Message-Id: <03907644185382.773588432734.799319-7043@cimail571.msn.com>" + LF +
			"To: <jackwnn@synnex.com.au>" + LF +
			"Subject: May 74% OFF" + LF +
			"From: Viagra � Official Site <jackwnn@synnex.com.au>" + LF +
			"MIME-Version: 1.0" + LF +
			"Importance: High" + LF +
			"Content-Type: text/html; charset=\"iso-8859-1\"" + LF +
			"Content-Transfer-Encoding: 8bit" + LF +
			"X-Rfx-Unknown-Address: Address <jackwnn@synnex.com.au> is not protected by Reflexion." + LF +
			"Return-Path: jackwng@gmail.com" + LF +
			"X-OriginalArrivalTime: 13 May 2008 22:50:31.0508 (UTC) FILETIME=[BF33D940:01C8B54B]" + LF +
			"Date: 14 May 2008 08:50:31 +1000";
		
		List<MsgHeader> list = parseRfc822Headers(rfc822Headers);
		logger.info(LF+printHeaders(list));
		logger.info("Number of headers: " + list.size());
	}
}
