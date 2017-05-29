package jpa.test.misc;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.FixMethodOrder;
import org.junit.Test;

import jpa.constant.StatusId;
import jpa.msgui.vo.PagingVo;
import jpa.msgui.vo.PagingVo.Column;
import jpa.msgui.vo.PagingVo.PageAction;
import jpa.msgui.vo.PagingVo.Type;

@FixMethodOrder
public class PagingVoTest {
	static final Logger logger = Logger.getLogger(PagingVoTest.class);

	@Test
	public void testPagingVo1() {
		PagingVo vo1 = new PagingVo();
		String methodNames = vo1.printMethodNames();
		logger.info("List of methods:\n" + methodNames);
		assertTrue(StringUtils.contains(methodNames, "getRowCount"));
		assertTrue(StringUtils.contains(methodNames, "getSearchBy"));
		
		logger.info("PagingVo:\n" + vo1.toString());
		
		PagingVo vo2 = new PagingVo();
		assertTrue(vo1.equalsTo(vo2));
		assertTrue(vo1.equalsToSearch(vo2));
		
		vo2.setStatusId(StatusId.INACTIVE.getValue());
		assertFalse(vo1.equalsTo(vo2));
		assertFalse(vo1.equalsToSearch(vo2));
		
		logger.info(vo1.listChanges());
		Map<String, String> map = vo1.getSearchBy().getSearchByForPrint(true);
		for (Iterator<String> it=map.keySet().iterator(); it.hasNext();) {
			String col = it.next();
			logger.info(col + " -> " + map.get(col));
		}

		List<PagingVo.Column> msgInboxTypeAll = PagingVo.Column.getColumnsByType(Type.msginbox, true);
		assertFalse(msgInboxTypeAll.isEmpty());
		
		List<PagingVo.Column> msgInboxType = PagingVo.Column.getColumnsByType(Type.msginbox);
		assertFalse(msgInboxType.isEmpty());
		assertTrue(msgInboxType.size() < msgInboxTypeAll.size());
		
		assertTrue(msgInboxTypeAll.contains(PagingVo.Column.fromAddr));
		assertTrue(PagingVo.Column.fromAddr.isJoin());
		
		assertFalse(msgInboxType.contains(PagingVo.Column.fromAddr));
		
		assertTrue(msgInboxTypeAll.contains(PagingVo.Column.msgSubject));
		assertFalse(PagingVo.Column.msgSubject.isJoin());
		
		assertTrue(msgInboxType.contains(PagingVo.Column.msgSubject));
	}

	@Test
	public void testPagingVo2() {
		PagingVo vo1 = new PagingVo();
		
		PagingVo.OrderBy orderBy = vo1.getOrderBy();
		
		Set<Column> keySet = orderBy.getKeySet();
		assertTrue(keySet.contains(Column.rowId));
		assertTrue(keySet.contains(Column.updtTime));
		assertTrue(keySet.contains(Column.address));
		assertFalse(keySet.contains(Column.emailAddr));
		
		orderBy.setOrderBy(Column.rowId, true);
		orderBy.setOrderBy(Column.updtTime, false);
		orderBy.setOrderBy(Column.address, true);
		orderBy.setOrderBy(Column.emailAddr, null);
		
		logger.info("Order By:\n" + orderBy.toString());
		
		assertTrue(keySet.contains(Column.emailAddr));
		
		assertEquals(true, orderBy.getIsAscending(Column.rowId));
		assertEquals(false, orderBy.getIsAscending(Column.updtTime));
		assertEquals(true, orderBy.getIsAscending(Column.address));
		assertNull(orderBy.getIsAscending(Column.emailAddr));
		
		PagingVo.SearchBy searchBy = vo1.getSearchBy();
		
		logger.info("Search By:\n" + searchBy.toString());
		
		for (Column c : Column.values()) {
			assertTrue(searchBy.getKeySet().contains(c));
		}
		
		assertNull(vo1.getSearchValue(Column.emailAddr));
		vo1.setSearchValue(Column.emailAddr, "@localhost");
		assertNotNull(vo1.getSearchValue(Column.emailAddr));
		assertEquals("@localhost", vo1.getSearchValue(Column.emailAddr));
		
		assertEquals("AnyWords", vo1.getSearchMatchBy(Column.emailAddr).name());
		
		PagingVo.Criteria criteria = vo1.getSearchCriteria(Column.emailAddr);
		assertTrue(criteria.getDataType().isAssignableFrom(String.class));
		
		assertEquals("ExactPhrase", vo1.getSearchMatchBy(Column.updtTime).name());
		criteria = vo1.getSearchCriteria(Column.updtTime);
		assertTrue(criteria.getDataType().isAssignableFrom(java.sql.Timestamp.class));
		
		assertEquals("ExactPhrase", vo1.getSearchMatchBy(Column.bounceCount).name());
		criteria = vo1.getSearchCriteria(Column.bounceCount);
		assertTrue(criteria.getDataType().isAssignableFrom(Integer.class));
		assertTrue(Number.class.isAssignableFrom(criteria.getDataType()));
		
		assertEquals("ExactPhrase", vo1.getSearchMatchBy(Column.isFlagged).name());
		criteria = vo1.getSearchCriteria(Column.isFlagged);
		assertTrue(criteria.getDataType().isAssignableFrom(Boolean.class));
	}

	
	@Test
	public void testPagingVo3() {
		PagingVo vo1 = new PagingVo();
		assertEquals(20, vo1.getPageSize());
		
		vo1.setRowCount(85);
		vo1.setPageNumber(2);
		vo1.setPageAction(PageAction.NEXT);
		assertEquals(3, vo1.getPageNumber());
		
		vo1.setPageAction(PageAction.PREVIOUS);
		assertEquals(2, vo1.getPageNumber());
		
		vo1.setPageAction(PageAction.LAST);
		
		assertEquals((5 - 1), vo1.getPageNumber());
		
		vo1.setPageAction(PageAction.FIRST);
		assertEquals(0, vo1.getPageNumber());
		vo1.setPageAction(PageAction.PREVIOUS);
		assertEquals(0, vo1.getPageNumber());
		
		vo1.setPageAction(PageAction.NEXT);
		assertEquals(1, vo1.getPageNumber());
		
		vo1.resetPageContext();
		assertEquals(-1, vo1.getRowCount());
		assertEquals(PageAction.CURRENT, vo1.getPageAction());
		assertEquals(0, vo1.getPageNumber());
	}
}
