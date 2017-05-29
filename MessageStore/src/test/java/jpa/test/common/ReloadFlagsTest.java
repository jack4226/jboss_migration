package jpa.test.common;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.beanutils.BeanUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jpa.model.ReloadFlags;
import jpa.service.common.ReloadFlagsService;
import jpa.spring.util.BoTestBase;

public class ReloadFlagsTest extends BoTestBase {

	@BeforeClass
	public static void ReloadFlagsPrepare() {
	}

	@Autowired
	ReloadFlagsService service;

	@Test
	public void ReloadFlagsService() throws Exception {
		ReloadFlags record = service.select();
		assertNotNull(record);
		
		ReloadFlags backup = null;
		try {
			backup = (ReloadFlags) BeanUtils.cloneBean(record);
		}
		catch (Exception e) {
			throw e;
		}
		
		record.setSenders(record.getSenders() + 1);
		service.update(record);
		assertTrue(record.getSenders()==(backup.getSenders()+1));
		
		service.updateSenderReloadFlag();
		service.updateRuleReloadFlag();
		service.updateActionReloadFlag();
		service.updateTemplateReloadFlag();
		service.updateScheduleReloadFlag();
		
		ReloadFlags record2 = service.select();

		assertTrue(record2.getSenders()==backup.getSenders()+2);
		assertTrue(record2.getRules()==backup.getRules()+1);
		assertTrue(record2.getActions()==backup.getActions()+1);
		assertTrue(record2.getTemplates()==backup.getTemplates()+1);
		assertTrue(record2.getSchedules()==backup.getSchedules()+1);
	}
}
