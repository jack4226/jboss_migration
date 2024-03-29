package jpa.msgui.bean;

import javax.faces.event.AjaxBehaviorEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jpa.msgui.vo.PagingVo;

@javax.enterprise.context.Dependent
public abstract class PaginationBean extends BaseBean {
	protected static Logger logger = LogManager.getLogger(PaginationBean.class);

	private final PagingVo pagingVo = new PagingVo();
	
	public abstract long getRowCount();
	
	/*
	 * Do NOT add "final" to the method. It broke Mockito test!
	 */
	public PagingVo getPagingVo() {
		return pagingVo;
	}
	
	protected abstract void refresh();
	
	protected void resetPagingVo() {
		pagingVo.resetPageContext();
		refresh();
	}
	
	/*
	 * ajax listeners
	 */
	public void pageFirst(AjaxBehaviorEvent event) {
		pagingVo.setPageAction(PagingVo.PageAction.FIRST);
		return; // TO_PAGING;
	}

	public void pagePrevious(AjaxBehaviorEvent event) {
		pagingVo.setPageAction(PagingVo.PageAction.PREVIOUS);
		return; // TO_PAGING;
	}

	public void pageNext(AjaxBehaviorEvent event) {
		pagingVo.setPageAction(PagingVo.PageAction.NEXT);
		return; // TO_PAGING;
	}

	public void pageLast(AjaxBehaviorEvent event) {
		if (pagingVo.getRowCount() < 0) {
			pagingVo.setRowCount(getRowCount());
		}
		pagingVo.setPageAction(PagingVo.PageAction.LAST);
		return; // TO_PAGING;
	}

	public long getLastPageRow() {
		long lastRow = (pagingVo.getPageNumber() + 1) * pagingVo.getPageSize();
		if (pagingVo.getRowCount() < 0) {
			pagingVo.setRowCount(getRowCount());
		}
		if (lastRow > pagingVo.getRowCount()) {
			return pagingVo.getRowCount();
		}
		else {
			return lastRow;
		}
	}

}
