package jpa.service.msgin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import jpa.constant.StatusId;
import jpa.data.preload.MailInboxEnum;
import jpa.dataloader.MailInboxLoader;
import jpa.model.MailInbox;
import jpa.spring.util.SpringUtil;
import jpa.util.Log4jConfigUtil;

public class MailReaderMain {
	static final Logger logger = Logger.getLogger( MailReaderMain.class);

	/*
	 * EMails from custcare mailbox will loop back to the same mailbox after they have been processed,
	 * add following VM arguments to stop email looping thus purge the EMails from mailboxes:
	 * -Ddisable_email_looping=yes -Dmax_read_per_pass=20
	 * or you can stop sending EMails with following VM parameters: 
	 * -Ddisable_send_mail=true -Dmax_read_per_pass=20
	 */
	public static void main(String[] args) {
		Log4jConfigUtil.modifyLogLevel(Level.ERROR, Level.DEBUG);
		MailInboxService mailBoxDao = SpringUtil.getAppContext().getBean(MailInboxService.class);
		List<MailInbox> mboxes = mailBoxDao.getAll(true);
		// load mailbox for processing for following users
		mboxes.clear();
		mboxes.add(MailInboxLoader.buildMailBoxFromEnum(MailInboxEnum.ALERT));
		mboxes.add(MailInboxLoader.buildMailBoxFromEnum(MailInboxEnum.jwang));
		mboxes.add(MailInboxLoader.buildMailBoxFromEnum(MailInboxEnum.twang));
		mboxes.add(MailInboxLoader.buildMailBoxFromEnum(MailInboxEnum.CUSTCARE));
		for (MailInbox minbox : mboxes) {
			if (StringUtils.contains(minbox.getMailInboxPK().getHostName(), "localhost")) {
				minbox.setStatusId(StatusId.ACTIVE.getValue());
			}
		}
		// end of users
		List<Thread> threads = new ArrayList<Thread>();
		Random random = new Random();
		for (MailInbox mbox : mboxes) {
			try {
				//mbox.setFromTimer(true);
				MailReaderBo reader = new MailReaderBo(mbox);
				Thread thread = new Thread(reader, mbox.getMailInboxPK().toString());
				threads.add(thread);
				try {
					thread.start();
					Thread.sleep(1000 + random.nextInt(5000));
				}
				catch (Exception e) {
					logger.error("Exception caught", e);
				}
			}
			finally {
			}
		}
		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				logger.info("InterruptedException caught: " + e.getMessage());
			}
		}
		System.exit(0);
	}

}
