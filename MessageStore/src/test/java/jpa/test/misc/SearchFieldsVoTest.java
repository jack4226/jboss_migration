package jpa.test.misc;

import static org.junit.Assert.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import jpa.data.preload.FolderEnum;
import jpa.data.preload.RuleNameEnum;
import jpa.msgui.vo.PagingVo;
import jpa.msgui.vo.SearchFieldsVo;
import jpa.util.PrintUtil;

public class SearchFieldsVoTest {
	static final Logger logger = LogManager.getLogger(SearchFieldsVoTest.class);

	@Test
	public void testSearchVo() {
		SearchFieldsVo vo1 = new SearchFieldsVo(new PagingVo());
		logger.info("List of methods:\n" + vo1.getPagingVo().printMethodNames());
		
		SearchFieldsVo vo2 = new SearchFieldsVo(new PagingVo());
		
		assertEquals(true, vo1.equalsLevel1(vo2));
		
		vo2.setFolderType(FolderEnum.Closed);
		assertEquals(false, vo1.equalsLevel1(vo2));
		
		vo2.resetFolderAndRule();
		assertEquals(true, vo1.equalsLevel1(vo2));
		
		vo2.setIsRead(Boolean.TRUE);
		vo2.setIsFlagged(Boolean.FALSE);
		assertEquals(false, vo1.equalsLevel1(vo2));
		
		vo2.resetFlags();
		assertEquals(true, vo1.equalsLevel1(vo2));
		
		vo1.getPagingVo().setSearchValue(PagingVo.Column.msgSubject, "auto-reply");
		vo2.setRuleName(RuleNameEnum.HARD_BOUNCE.getValue());
		vo1.getPagingVo().setSearchValue(PagingVo.Column.msgBody, "test message");
		vo2.getPagingVo().setSearchValue(PagingVo.Column.fromAddrId, 10);
		vo1.getPagingVo().setSearchValue(PagingVo.Column.fromAddr, "test@test.com");
		vo1.getPagingVo().setSearchValue(PagingVo.Column.toAddrId, 20);
		assertFalse(vo1.equalsLevel1(vo2));
		logger.info("Changes - vo1 vs vo2:\n"  + vo1.getPagingVo().listChanges());

	}
	
	@Test
	public void testCopyLevel1() {
		SearchFieldsVo vo1 = new SearchFieldsVo(new PagingVo());
		
		SearchFieldsVo vo2 = new SearchFieldsVo(new PagingVo());
		
		vo1.setFolderType(FolderEnum.Closed);
		
		vo1.getPagingVo().setSearchValue(PagingVo.Column.msgSubject, "auto-reply");
		vo1.setRuleName(RuleNameEnum.HARD_BOUNCE.getValue());
		vo1.getPagingVo().setSearchValue(PagingVo.Column.msgBody, "test message");
		vo1.getPagingVo().setSearchValue(PagingVo.Column.fromAddrId, 10);
		vo1.getPagingVo().setSearchValue(PagingVo.Column.fromAddr, "test@test.com");
		vo1.getPagingVo().setSearchValue(PagingVo.Column.toAddrId, 20);
		assertFalse(vo1.equalsLevel1(vo2));
		
		logger.info("Before Copy - vo1 vs vo2:\n"  + vo1.getPagingVo().listChanges());
		
		vo1.copyLevel1To(vo2);

		logger.info("After Copy - vo1:\n"  + PrintUtil.prettyPrint(vo1, 2));
		logger.info("After Copy - vo2:\n"  + PrintUtil.prettyPrint(vo2, 2));

		assertEquals(true, vo1.equalsLevel1(vo2));
		
	}
}
