package jpa.msgui.vo;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import jpa.constant.RuleCriteria;
import jpa.data.preload.FolderEnum;
import jpa.data.preload.RuleNameEnum;

public final class SearchFieldsVo implements Serializable {
	private static final long serialVersionUID = -4300610160368410355L;

	static Logger logger = Logger.getLogger(SearchFieldsVo.class);
	
	public static enum RuleName {All};
	
	private final PagingVo pagingVo;
	
	private FolderEnum folderType = null;
	private String ruleName = null;
	
	private Boolean isRead = null;
	private Boolean isFlagged = null;
	private Date recent = null;
	
	public static void main(String[] args) {
		SearchFieldsVo vo1 = new SearchFieldsVo(new PagingVo());
		System.out.println(vo1.getPagingVo().printMethodNames());
		System.out.println(vo1.toString());
		SearchFieldsVo vo2 = new SearchFieldsVo(new PagingVo());
		vo2.setFolderType(FolderEnum.Closed);
		System.out.println("Are SearchFieldsVo's same: " + vo1.equalsLevel1(vo2));
		vo1.getPagingVo().setSearchValue(PagingVo.Column.msgSubject, "auto-reply");
		vo2.setRuleName(RuleNameEnum.HARD_BOUNCE.getValue());
		vo1.getPagingVo().setSearchValue(PagingVo.Column.msgBody, "test message");
		vo2.getPagingVo().setSearchValue(PagingVo.Column.fromAddrId, 10);
		vo1.getPagingVo().setSearchValue(PagingVo.Column.fromAddr, "test@test.com");
		vo1.getPagingVo().setSearchValue(PagingVo.Column.toAddrId, 20);
		System.out.println(vo1.equalsLevel1(vo2));
		System.out.println(vo1.getPagingVo().listChanges());
	}
	
	public SearchFieldsVo(PagingVo pagingVo) {
		this.pagingVo = pagingVo;
		init();
	}
	
	private void init() {
		setFolderType(FolderEnum.Inbox);
		setRuleName(RuleName.All.name());
	}
	
	public void resetFlags() {
		setIsRead(null);
		setIsFlagged(null);
		setRecent(null);
	}
	
	public void resetFolderAndRule() {
		init();
	}
	
	public void resetSearchFields() {
		setRuleName(RuleName.All.name());
		List<PagingVo.Column> list = PagingVo.Column.getColumnsByType(PagingVo.Type.msginbox, true);
		for (PagingVo.Column c : list) {
			PagingVo.Criteria criteria = getPagingVo().getSearchCriteria(c);
			if (criteria != null) {
				criteria.setValue(null);
			}
		}
	}
	
	public void resetAll() {
		init();
		resetFlags();
		pagingVo.resetPageContext();
		resetSearchFields();
	}

	public PagingVo getPagingVo() {
		return pagingVo;
	}

	public FolderEnum getFolderType() {
		return folderType;
	}
	public void setFolderType(FolderEnum folderType) {
		this.folderType = folderType;
		String folder = folderType == null ? null : folderType.name();
		pagingVo.setSearchCriteria(PagingVo.Column.messageFolder, new PagingVo.Criteria(RuleCriteria.EQUALS, folder));
	}
	public Boolean getIsRead() {
		return isRead;
	}
	public void setIsRead(Boolean isRead) {
		this.isRead = isRead;
		if (isRead != null) {
			if (isRead) {
				pagingVo.setSearchCriteria(PagingVo.Column.readCount, new PagingVo.Criteria(RuleCriteria.GREATER_THAN, 0));
			}
			else {
				pagingVo.setSearchCriteria(PagingVo.Column.readCount, new PagingVo.Criteria(RuleCriteria.EQUALS, 0));
			}
		}
		else {
			pagingVo.setSearchValue(PagingVo.Column.readCount, (Integer) null);
		}
	}
	public Boolean getIsFlagged() {
		return isFlagged;
	}
	public void setIsFlagged(Boolean isFlagged) {
		this.isFlagged = isFlagged;
		pagingVo.setSearchCriteria(PagingVo.Column.isFlagged, new PagingVo.Criteria(RuleCriteria.EQUALS, isFlagged, Boolean.class));
	}
	public String getRuleName() {
		return ruleName;
	}
	public void setRuleName(String ruleName) {
		try {
			RuleNameEnum.getByValue(ruleName);
			this.ruleName = ruleName;
		}
		catch (IllegalArgumentException e) {
			this.ruleName = RuleName.All.name();
		}
		pagingVo.setSearchCriteria(PagingVo.Column.ruleName, new PagingVo.Criteria(RuleCriteria.EQUALS, ruleName));
	}
	public Date getRecent() {
		return recent;
	}
	public void setRecent(Date recent) {
		this.recent = recent;
	}
	
	public boolean equalsLevel1(SearchFieldsVo vo) {
		if (this == vo) {
			return true;
		}
		if (vo == null) {
			return false;
		}
		
		getPagingVo().equalsToSearch(vo.getPagingVo());

		String className = this.getClass().getName();
		Method thisMethods[] = this.getClass().getMethods();
		for (int i = 0; i < thisMethods.length; i++) {
			Method method = (Method) thisMethods[i];
			String methodName = method.getName();
			Class<?>[] params = method.getParameterTypes();
			if (methodName.length() > 3 && methodName.startsWith("get") && params.length == 0) {
				Method voMethod = null;
				try {
					voMethod = vo.getClass().getMethod(methodName, params);
				}
				catch (NoSuchMethodException e) {
					logger.warn(className + ".equalsToSearch(): " + e.getMessage());
					return false;
				}
				try {
					Class<?> returnType = method.getReturnType();
					String returnTypeName = returnType.getName();
					if ((returnTypeName.endsWith("java.lang.String"))
							|| (returnTypeName.endsWith("java.lang.Integer"))
							|| (returnTypeName.endsWith("java.lang.Long"))
							|| (returnTypeName.endsWith("java.sql.Timestamp"))
							|| (returnTypeName.endsWith("java.sql.Date"))
							|| (returnType.equals(java.lang.Integer.TYPE))
							|| (returnType.equals(java.lang.Long.TYPE))
							|| (returnType.equals(java.lang.Character.TYPE))
							|| (returnTypeName.endsWith("FolderEnum"))) {
						Object thisValue = method.invoke((Object)this, (Object[])params);
						Object voValue = voMethod.invoke((Object)vo, (Object[])params);
						if (thisValue == null) {
							if (voValue != null) {
								getPagingVo().addChangeLog(methodName.substring(3), thisValue, voValue);
							}
						}
						else {
							if (!thisValue.equals(voValue)) {
								getPagingVo().addChangeLog(methodName.substring(3), thisValue, voValue);
							}
						}
					}
				}
				catch (Exception e) {
					logger.warn(className + ".equalsToSearch(): " + e.getMessage());
					return false;
				}
			}
		}
		if (getPagingVo().getLogList().size() > 0) {
			return false;
		}
		else {
			return true;
		}
	}
	
	public void copyLevel1To(SearchFieldsVo vo) {
		if (vo == null) {
			return;
		}
		vo.setRuleName(this.getRuleName());
		vo.setFolderType(this.getFolderType());
		vo.setRecent(this.getRecent());
		vo.setIsFlagged(this.getIsFlagged());
		vo.setIsRead(this.getIsRead());
		List<PagingVo.Column> colList = PagingVo.Column.getColumnsByType(PagingVo.Type.msginbox, true);
		for (PagingVo.Column col : colList) {
			vo.getPagingVo().setSearchCriteria(col, getPagingVo().getSearchCriteria(col));
		}
	}
	
}
	