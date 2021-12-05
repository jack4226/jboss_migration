package jpa.test.common;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jpa.constant.Constants;
import jpa.constant.MailingListDeliveryType;
import jpa.constant.MailingListType;
import jpa.model.EmailTemplate;
import jpa.model.MailingList;
import jpa.model.SenderData;
import jpa.service.common.EmailTemplateService;
import jpa.service.common.SenderDataService;
import jpa.service.maillist.MailingListService;
import jpa.spring.util.BoTestBase;
import jpa.util.PrintUtil;

public class EmailTemplateTest extends BoTestBase {
	static Logger logger = Logger.getLogger(EmailTemplateTest.class);
	
	final String testTemplateId = "jpa test template id";
	
	@BeforeClass
	public static void EmailTemplatePrepare() {
	}

	@Autowired
	EmailTemplateService service;
	@Autowired
	SenderDataService senderService;
	@Autowired
	MailingListService mlistService;

	@Test
	public void emailTemplateService() {
		SenderData sender = senderService.getBySenderId(Constants.DEFAULT_SENDER_ID);
		List<MailingList> mlist = mlistService.getAll(false);
		assertFalse(mlist.isEmpty());
		assertNotNull(mlist.get(0).getListEmailAddr());
		assertNotNull(mlist.get(0).getSenderData());
		// test insert
		EmailTemplate var1 = new EmailTemplate();
		var1.setSenderData(sender);
		var1.setMailingList(mlist.get(0));
		var1.setTemplateId(testTemplateId);
		var1.setDeliveryOption(MailingListDeliveryType.ALL_ON_LIST.getValue());
		var1.setListType(MailingListType.TRADITIONAL.getValue());
		var1.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(var1);
		
		EmailTemplate var2 = service.getByTemplateId(testTemplateId);
		assertNotNull(var2);
		logger.info("EmailTemplate: " + PrintUtil.prettyPrint(var2,1));

		List<EmailTemplate> list1 = service.getAll();
		assertFalse(list1.isEmpty());
		assertNotNull(list1.get(0).getMailingList());
		
		List<EmailTemplate> list2 = service.getByMailingListId(list1.get(0).getMailingList().getListId());
		assertFalse(list2.isEmpty());
		
		int i = 0;
		EmailTemplate testInstance = list2.get(i++);
		while (testInstance.getTemplateId().length() > 23 && i < list2.size()) {
			testInstance = list2.get(i++);
		}
		
		// test insert
		EmailTemplate var3 = createNewInstance(testInstance);
		var3.setTemplateId(var3.getTemplateId()+"_v2");
		service.insert(var3);
		assertNotNull(service.getByTemplateId(var3.getTemplateId()));
		// end of test insert
		
		service.delete(var3);
		EmailTemplate deleted1 = service.getByTemplateId(var3.getTemplateId());
		assertNull(deleted1);
		
		// test deleteByPrimaryKey
		EmailTemplate var5 = createNewInstance(var2);
		var5.setTemplateId(var2.getTemplateId() + "_v5");
		service.insert(var5);
		var5 = service.getByTemplateId(var5.getTemplateId());
		service.delete(var5);
		EmailTemplate deleted2 = service.getByTemplateId(var5.getTemplateId());
		assertNull(deleted2);
		
		// test update
		EmailTemplate var6 = createNewInstance(var2);
		var6.setTemplateId(var2.getTemplateId() + "_v6");
		service.insert(var6);
		assertNotNull((var6=service.getByTemplateId(var6.getTemplateId())));
		var6.setBodyText("new test value");
		service.update(var6);
		EmailTemplate var_updt = service.getByTemplateId(var6.getTemplateId());
		assertTrue("new test value".equals(var_updt.getBodyText()));
		logger.info("EmailTemplate: " + PrintUtil.prettyPrint(var6,1));
		// end of test update
		
		service.delete(var6);
		Optional<EmailTemplate> deleted3 = service.getByRowId(var6.getRowId());
		assertTrue(deleted3.isEmpty());
	}
	
	private EmailTemplate createNewInstance(EmailTemplate orig) {
		EmailTemplate dest = new EmailTemplate();
		try {
			BeanUtils.copyProperties(dest, orig);
			//dest.setTemplateId(StringUtils.truncate(dest.getTemplateId(), 23));
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		return dest;
	}
}
