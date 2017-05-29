package jpa.test.common;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jpa.constant.MobileCarrierEnum;
import jpa.model.MobileCarrier;
import jpa.service.common.MobileCarrierService;
import jpa.spring.util.BoTestBase;

public class MobileCarrierTest extends BoTestBase {

	@BeforeClass
	public static void MobileCarrierPrepare() {
	}

	@Autowired
	MobileCarrierService service;

	@Test
	public void mobileCarrierService() {
		MobileCarrier mc1 = service.getByCarrierId(MobileCarrierEnum.TMobile.name());
		if (mc1 == null) {
			mc1 = new MobileCarrier();
			mc1.setCarrierId(MobileCarrierEnum.TMobile.name());
			mc1.setCarrierName(MobileCarrierEnum.TMobile.getValue());
			mc1.setTextAddress(MobileCarrierEnum.TMobile.getText());
			mc1.setMultiMediaAddress(MobileCarrierEnum.TMobile.getMmedia());
			service.insert(mc1);
		}
		
		List<MobileCarrier> list = service.getAll();
		assertFalse(list.isEmpty());
		
		MobileCarrier tkn0 = service.getByCarrierId(list.get(0).getCarrierId());
		assertNotNull(tkn0);
		
		tkn0 = service.getByRowId(list.get(0).getRowId());
		assertNotNull(tkn0);
		
		// test update
		tkn0.setUpdtUserId("JpaTest");
		service.update(tkn0);
		
		MobileCarrier tkn1 = service.getByRowId(tkn0.getRowId());
		assertTrue("JpaTest".equals(tkn1.getUpdtUserId()));
		// end of test update
		
		// test insert
		MobileCarrier tkn2 = new MobileCarrier();
		try {
			BeanUtils.copyProperties(tkn2, tkn1);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		tkn2.setCarrierId(tkn1.getCarrierId()+"_v2");
		service.insert(tkn2);
		
		MobileCarrier tkn3 = service.getByCarrierId(tkn2.getCarrierId());
		assertTrue(tkn3.getRowId()!=tkn1.getRowId());
		// end of test insert
		
		// test select with No Result
		service.delete(tkn3);
		assertNull(service.getByCarrierId(tkn2.getCarrierId()));
		
		assertTrue(0==service.deleteByCarrierId(tkn3.getCarrierId()));
		assertTrue(0==service.deleteByRowId(tkn3.getRowId()));
	}
}
