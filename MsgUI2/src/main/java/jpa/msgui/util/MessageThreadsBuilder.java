package jpa.msgui.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jpa.model.msg.MessageInbox;
import jpa.service.msgdata.MessageInboxService;
import jpa.util.StringUtil;

public class MessageThreadsBuilder implements java.io.Serializable {
	private static final long serialVersionUID = 2580221539624931900L;
	static final Logger logger = LogManager.getLogger(MessageThreadsBuilder.class);
	/**
	 * Build a list of threaded messages from a message list.
	 * 
	 * @param messages -
	 *            a list of messages that associated to a lead thread
	 *            (identified by LeadMsgId)
	 * @return a threaded message list
	 */
	public static List<MessageInbox> buildThreads(List<MessageInbox> messages) {
		List<MessageInbox> threads = new ArrayList<MessageInbox>();
		if (messages == null || messages.isEmpty()) {
			return threads;
		}
		Map<Integer, List<Reply>> map = buildMap(messages);
		Stack<Integer> keyStack = new Stack<Integer>();
		if (map.containsKey(null)) {
			// originating message thread found
			List<Reply> root = map.get(null);
			buildTreeLevel(root, map, messages, threads, 0, keyStack);
		}
		else {
			// missing originating message, look for the oldest thread
			// messages list is sorted by MsgId in ascending order
			for (int i = 0; i < messages.size(); i++) {
				MessageInbox vo = messages.get(i);
				if (map.containsKey(vo.getReferringMessageRowId())) {
					List<Reply> root = map.get(vo.getReferringMessageRowId());
					keyStack.push(vo.getReferringMessageRowId());
					buildTreeLevel(root, map, messages, threads, 0, keyStack);
					keyStack.pop();
					break;
				}
			}
		}
		// in case there were missing links due to message deletion
		for (MessageInbox vo : messages) {
			if (vo.getThreadLevel() < 0) {
				List<Reply> root = map.get(vo.getReferringMessageRowId());
				keyStack.push(vo.getReferringMessageRowId());
				buildTreeLevel(root, map, messages, threads, 1, keyStack);
				keyStack.pop();
			}
		}
		return threads;
	}
	
	/**
	 * build a list of threaded messages with indentation (identified by
	 * MessageInbox.threadLevel).
	 * 
	 * @param root -
	 *            the leading thread
	 * @param map -
	 *            MsgRefId to a list of associated messages
	 * @param messages -
	 *            list of messages to be threaded
	 * @param threads -
	 *            list of threaded of messages with indentation
	 * @param level -
	 *            starting offset from left
	 */
	private static void buildTreeLevel(List<Reply> root, Map<Integer, List<Reply>> map,
			List<MessageInbox> messages, List<MessageInbox> threads, int level, Stack<Integer> keyStack) {
		for (int i = 0; root != null && i < root.size(); i++) {
			MessageInbox vo = messages.get(root.get(i).index);
			vo.setThreadLevel(level);
			threads.add(vo);
			if (keyStack.contains(root.get(i).msgId)) {
				logger.error("!!!!! Loop on next push key: " + root.get(i).msgId + ", Current Stack: " + keyStack);
			} 
			else {
				keyStack.push(root.get(i).msgId);
				buildTreeLevel(map.get(root.get(i).msgId), map, messages, threads, level + 1, keyStack);
				keyStack.pop();
			}
		}
	}
	
	/**
	 * build a map that maps each MsgRefId to a list of its associated MsgId's.
	 * 
	 * @param messages -
	 *            list of messages to be threaded
	 * @return a map that maps each MsgRefId to its associated messages
	 */
	private static Map<Integer, List<Reply>> buildMap(List<MessageInbox> messages) {
		Map<Integer, List<Reply>> map = new HashMap<Integer, List<Reply>>();
		for (int i = 0; i < messages.size(); i++) {
			MessageInbox vo = messages.get(i);
			if (map.containsKey(vo.getReferringMessageRowId())) {
				List<Reply> replies = map.get(vo.getReferringMessageRowId());
				replies.add(new Reply(vo.getRowId(), i));
			}
			else {
				List<Reply> replies = new ArrayList<Reply>();
				replies.add(new Reply(vo.getRowId(), i));
				map.put(vo.getReferringMessageRowId(), replies);
			}
		}logger.info("Message Threads map: " + map);
		return map;
	}
	
	private static class Reply implements Serializable {
		private static final long serialVersionUID = -7593101377038853095L;
		int msgId;
		int index;
		Reply (int msgId, int index) {
			this.msgId = msgId;
			this.index = index;
		}
		public String toString() {
			return msgId + "";
		}
	}
	
	public static void main(String[] args) {
		try {
			int threadId = 14;
			MessageInboxService msgInboxDao = jpa.spring.util.SpringUtil.getAppContext().getBean(MessageInboxService.class);
			List<MessageInbox> list = msgInboxDao.getByLeadMsgId(threadId);
			System.out.println("Number of threads retrieved: " + list.size());
			List<MessageInbox> threads = buildThreads(list);
			for (int i = 0; i < threads.size(); i++) {
				MessageInbox vo = threads.get(i);
				String dispLevel = StringUtil.getDots(vo.getThreadLevel()) + vo.getRowId();
				System.out.println(dispLevel + " - " + vo.getMsgSubject());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
