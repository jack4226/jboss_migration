package jpa.msgui.vo;

import java.util.List;

import jpa.constant.Constants;
import jpa.constant.StatusId;

public final class PagingSubscriberData implements java.io.Serializable, Cloneable {
	private static final long serialVersionUID = 2702501767172625606L;
	
	private final PagingVo pagingVo;
	
	public PagingSubscriberData(PagingVo pagingVo) {
		this.pagingVo = pagingVo;
		init();
	}
	
	public static void main(String[] args) {
		PagingSubscriberData vo1 = new PagingSubscriberData(new PagingVo());
		System.out.println(vo1.getPagingVo().printMethodNames());
		PagingSubscriberData vo2 = new PagingSubscriberData(new PagingVo());
		vo2.getPagingVo().setSearchValue(PagingVo.Column.senderId, Constants.DEFAULT_SENDER_ID);
		vo1.getPagingVo().setSearchValue(PagingVo.Column.ssnNumber, " 123-45-6789 ");
		vo2.getPagingVo().setStatusId(StatusId.ACTIVE.getValue());

		System.out.println(vo1.toString());
		System.out.println(vo1.getPagingVo().equalsToSearch(vo2.getPagingVo()));
		System.out.println(vo1.getPagingVo().listChanges());
	}

	private void init() {
	}
	
	public void resetSearchFields() {
		List<PagingVo.Column> list = PagingVo.Column.getColumnsByType(PagingVo.Type.subrdata);
		for (PagingVo.Column c : list) {
			PagingVo.Criteria criteria = getPagingVo().getSearchCriteria(c);
			if (criteria != null) {
				criteria.setValue(null);
			}
		}
	}
	
	public void resetAll() {
		init();
		pagingVo.resetPageContext();
		resetSearchFields();
	}

	public PagingVo getPagingVo() {
		return pagingVo;
	}

}
