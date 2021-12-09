package jpa.test.maillist;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Optional;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import jpa.constant.Constants;
import jpa.constant.StatusId;
import jpa.model.BroadcastMessage;
import jpa.model.BroadcastTracking;
import jpa.msgui.vo.PagingVo;
import jpa.service.common.EmailAddressService;
import jpa.service.maillist.BroadcastMessageService;
import jpa.service.maillist.BroadcastTrackingService;
import jpa.spring.util.BoTestBase;
import jpa.util.PrintUtil;

public class BroadcastTrackingTest extends BoTestBase {

	@BeforeClass
	public static void BroadcastTrackingPrepare() {
	}

	@Autowired
	BroadcastTrackingService service;
	@Autowired
	BroadcastMessageService bcdService;
	@Autowired
	EmailAddressService eaService;

	@Test
	public void testBroadcastTrackingService() {
		
		List<BroadcastMessage> bdlist = bcdService.getTop100();
		assertFalse(bdlist.isEmpty());
		
		BroadcastMessage bd1 = bdlist.get(0);
		
		List<BroadcastTracking> eblist = service.getByBroadcastMessageRowId(bd1.getRowId());
		assertFalse(eblist.isEmpty());
		
		BroadcastTracking eb1 = eblist.get(0);
		

		BroadcastTracking eb12 = service.getByPrimaryKey(eb1.getEmailAddress().getRowId(), eb1.getBroadcastMessage().getRowId());
		assertNotNull(eb12);
		eb12 = service.getByPrimaryKey(eb1.getEmailAddress().getRowId(), 9999);
		assertNull(eb12);
		
		java.sql.Timestamp ts = new java.sql.Timestamp(System.currentTimeMillis());
		eb1.setUpdtTime(ts);
		eb1.setClickCount(eb1.getClickCount()+1);
		eb1.setOpenCount(eb1.getOpenCount()+1);
		eb1.setSentCount(eb1.getSentCount()+1);
		eb1.setLastClickTime(ts);
		eb1.setLastOpenTime(ts);
		service.update(eb1);
		
		assertTrue(1<=service.updateSentCount(eb1.getRowId(), 2));
		assertTrue(1<=service.updateOpenCount(eb1.getRowId()));
		assertTrue(1<=service.updateClickCount(eb1.getRowId()));
		
		Optional<BroadcastTracking> eb2 = service.getByRowId(eb1.getRowId());
		assertTrue(eb2.isPresent());
		assertTrue(ts.equals(eb2.get().getUpdtTime()));
		logger.info(PrintUtil.prettyPrint(eb2, 2));
		
		List<BroadcastTracking> eblist2 = service.getByEmailAddress(eb1.getEmailAddress().getAddress());
		assertFalse(eblist2.isEmpty());
		
		List<BroadcastTracking> eblist3 = service.getByEmailAddrRowId(eb1.getEmailAddress().getRowId());
		assertFalse(eblist3.isEmpty());
		
		BroadcastTracking eb3 = new BroadcastTracking();
		eb3.setBroadcastMessage(eb1.getBroadcastMessage());
		String random_no = "1"; //String.valueOf(13 + new java.util.Random().nextInt(100));
		eb3.setEmailAddress(eaService.findSertAddress("tracking_" + random_no + "@test.com"));
		eb3.setStatusId(StatusId.ACTIVE.getValue());
		eb3.setUpdtUserId(Constants.DEFAULT_USER_ID);
		eb3.setUpdtTime(ts);
		service.insert(eb3);
		
		assertTrue(eb3.getRowId()>0  && eb3.getRowId()!=eb1.getRowId());
		service.delete(eb3);
		assert(0==service.deleteByRowId(eb3.getRowId()));

		assertFalse(service.getByRowId(eb3.getRowId()).isPresent());
	}

	@Test
	public void testBroadcastTrackingPaging() {
		
		List<BroadcastMessage> bdlist = bcdService.getTop100();
		assertFalse(bdlist.isEmpty());
		
		BroadcastMessage bd1 = bdlist.get(0);
		
		int count = service.getMessageCountForWeb(bd1.getRowId());
		assertTrue(0 <= count);
		
		PagingVo vo = new PagingVo();
		int pageSize = count > vo.getPageSize() ? vo.getPageSize() : (count > 2 ? count - 2 : count);
		vo.setPageSize(pageSize);
		
		Page<BroadcastTracking> page = service.getBroadcastTrackingsForWeb(bd1.getRowId(), vo);
		assertNotNull(page);
		
		assertTrue(pageSize >= page.getContent().size());
	}
}
