package jpa.msgui.bean;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UISelectOne;
import javax.faces.component.html.HtmlCommandButton;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.validator.ValidatorException;

import jpa.constant.CodeType;
import jpa.constant.RuleCategory;
import jpa.constant.RuleCriteria;
import jpa.constant.RuleDataName;
import jpa.constant.RuleType;
import jpa.model.rule.RuleAction;
import jpa.model.rule.RuleActionDetail;
import jpa.model.rule.RuleActionPK;
import jpa.model.rule.RuleElement;
import jpa.model.rule.RuleElementPK;
import jpa.model.rule.RuleLogic;
import jpa.model.rule.RuleSubruleMap;
import jpa.model.rule.RuleSubruleMapPK;
import jpa.msgui.util.FacesUtil;
import jpa.msgui.util.SpringUtil;
import jpa.service.rule.RuleActionDetailService;
import jpa.service.rule.RuleActionService;
import jpa.service.rule.RuleElementService;
import jpa.service.rule.RuleLogicService;
import jpa.service.rule.RuleSubruleMapService;
import jpa.util.BlobUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@javax.inject.Named("ruleLogic")
@javax.enterprise.context.SessionScoped
public class RuleLogicBean implements java.io.Serializable {
	private static final long serialVersionUID = 4143310214559095471L;
	protected static final Logger logger = LogManager.getLogger(RuleLogicBean.class);
	protected static final boolean isDebugEnabled = logger.isDebugEnabled();

	protected transient RuleLogicService ruleLogicDao = null;
	protected transient RuleElementService ruleElementDao = null;
	protected transient RuleSubruleMapService ruleSubRuleMapDao = null;
	protected transient RuleActionService msgActionDao = null;
	protected transient RuleActionDetailService actionDetailDao = null;
		
	protected transient DataModel<RuleLogic> ruleLogics = null;
	protected Map<String, Boolean> hasActionsMap = new LinkedHashMap<>();
	protected Map<String, Boolean> hasSubruleMap = new LinkedHashMap<>();
	
	protected RuleLogic ruleLogic = null;
	protected boolean editMode = true;
	protected BeanMode beanMode = BeanMode.list;
	
	protected String testResult = null;
	protected String actionFailure = null;
	
	protected transient UIInput ruleNameInput = null;
	protected transient UIInput startDateInput = null;
	
	protected transient DataModel<RuleElement> ruleElements = null;
	protected transient DataModel<RuleSubruleMap> subRules = null;
	protected transient DataModel<RuleAction> ruleActions = null;
	
	protected RuleElement ruleElement = null;
	protected RuleElement origRuleElement = null;
	
	/* use navigation rules in faces-config.xml */
	protected static final String TO_SELF = null;
	protected static final String TO_CANCELED = "cancel";
	protected static final String TO_FAILED = null;
	protected static final String TO_EDIT_LOGIC = "ruleLogicEdit.xhtml";
	protected static final String TO_EDIT_ELEMENT = "ruleElementEdit.xhtml";
	protected static final String TO_EDIT_SUBRULE = "ruleSubruleEdit.xhtml";
	protected static final String TO_EDIT_ACTION = "ruleActionEdit.xhtml";
	
	protected static final String TO_CONFIG_CUSTOM_RULES = "configureCustomRules.xhtml";
	protected static final String TO_CUSTOMIZE_BUILTIN_RULES = "customizeBuiltinRules.xhtml";
	protected static final String RULE_ACTION_SAVED = TO_CONFIG_CUSTOM_RULES;
	
	protected String sourcePage; // f:setPropertyActionListener tag
	
	public String getSourcePage() {
		return sourcePage;
	}

	public void setSourcePage(String sourcePage) {
		this.sourcePage = sourcePage;
	}

	protected RuleLogicService getRuleLogicService() {
		if (ruleLogicDao == null) {
			ruleLogicDao = SpringUtil.getWebAppContext().getBean(RuleLogicService.class);
		}
		return ruleLogicDao;
	}

	protected RuleElementService getRuleElementService() {
		if (ruleElementDao == null) {
			ruleElementDao = SpringUtil.getWebAppContext().getBean(RuleElementService.class);
		}
		return ruleElementDao;
	}

	protected RuleSubruleMapService getRuleSubruleMapService() {
		if (ruleSubRuleMapDao == null) {
			ruleSubRuleMapDao = SpringUtil.getWebAppContext().getBean(RuleSubruleMapService.class);
		}
		return ruleSubRuleMapDao;
	}

	protected RuleActionService getRuleActionService() {
		if (msgActionDao == null) {
			msgActionDao = SpringUtil.getWebAppContext().getBean(RuleActionService.class);
		}
		return msgActionDao;
	}

	private RuleActionDetailService getRuleActionDetailService() {
		if (actionDetailDao == null) {
			actionDetailDao = SpringUtil.getWebAppContext().getBean(RuleActionDetailService.class);
		}
		return actionDetailDao;
	}

	/*
	 * Main Page Section 
	 */
	
	public DataModel<RuleLogic> getAll() {
		String fromPage = FacesUtil.getRequestParameter("frompage");
		if (fromPage != null && fromPage.equals("main")) {
			refresh();
		}
		logger.info("getAll() - From page: " + fromPage + ", ruleLogics==null? " + (ruleLogics==null));
		if (ruleLogics == null) {
			List<RuleLogic> ruleLogicList = getRuleLogicService().getAll(false);
			hasSubruleMap.clear();
			hasActionsMap.clear();
			for (RuleLogic rc : ruleLogicList) {
				boolean hasSubrule = getRuleLogicService().getHasSubrules(rc.getRuleName());
				hasSubruleMap.put(rc.getRuleName(), hasSubrule);
				boolean hasActions = getRuleActionService().getHasActions(rc.getRuleName());
				hasActionsMap.put(rc.getRuleName(), hasActions);
			}
			ruleLogics = new ListDataModel<RuleLogic>(ruleLogicList);
		}
		return ruleLogics;
	}
	
	/*
	 * Use String signature for rowId to support JSF script.
	 */
	public String findRuleNameByRowId(String rowId) {
		RuleLogic rl = getRuleLogicService().getByRowId(Integer.parseInt(rowId));
		if (rl == null) {
			return TO_SELF;
		}
		return rl.getRuleName();
	}

	public void refreshListener(AjaxBehaviorEvent event) {
		refresh();
	}
	
	public String refresh() {
		if (isDebugEnabled)
			logger.debug("refresh() - Entering...");
		ruleLogics = null;
		ruleElements = null;
		subRules = null;
		ruleActions = null;
		return TO_SELF;
	}
	
	public void viewRuleLogicListener(AjaxBehaviorEvent event) {
		viewRuleLogic();
	}
	
	public String viewRuleLogic() {
		if (isDebugEnabled)
			logger.debug("viewRuleLogic() - Entering...");
		if (ruleLogics == null) {
			logger.warn("viewRuleLogic() - RuleLogic List is null.");
			return TO_FAILED;
		}
		if (!ruleLogics.isRowAvailable()) {
			logger.warn("viewRuleLogic() - RuleLogic Row not available.");
			return TO_FAILED;
		}
		reset();
		// clean up
		ruleElements = null;
		startDateInput = null; // so it could be rebound to a new record
		// end of clean up
		this.ruleLogic = ruleLogics.getRowData();
		logger.info("viewRuleLogic() - RuleLogic to be edited: " + ruleLogic.getRuleName());
		ruleLogic.setMarkedForEdition(true);
		editMode = true;
		beanMode = BeanMode.edit;
		if (isDebugEnabled)
			logger.debug("viewRuleLogic() - RuleLogic to be passed to jsp: " + ruleLogic);
		
		return TO_EDIT_LOGIC;
	}
	
	public void viewSubRulesListener(AjaxBehaviorEvent event) {
		viewSubRules();
	}
	
	public String viewSubRules() {
		if (isDebugEnabled)
			logger.debug("viewSubRules() - Entering...");
		if (ruleLogics == null) {
			logger.warn("viewSubRules() - RuleLogic List is null.");
			return TO_FAILED;
		}
		if (!ruleLogics.isRowAvailable()) {
			logger.warn("viewSubRules() - RuleLogic Row not available.");
			return TO_FAILED;
		}
		reset();
		subRules = null;
		this.ruleLogic = ruleLogics.getRowData();
		ruleLogic.setMarkedForEdition(true);
		beanMode = BeanMode.subrules;
		return TO_EDIT_SUBRULE;
	}
	
	public void viewMsgActionsListener(AjaxBehaviorEvent event) {
		viewMsgActions();
	}
	
	public String viewMsgActions() {
		if (isDebugEnabled)
			logger.debug("viewMsgActions() - Entering...");
		if (ruleLogics == null) {
			logger.warn("viewMsgActions() - RuleLogic List is null.");
			return TO_FAILED;
		}
		if (!ruleLogics.isRowAvailable()) {
			logger.warn("viewMsgActions() - RuleLogic Row not available.");
			return TO_FAILED;
		}
		reset();
		ruleActions = null;
		this.ruleLogic = ruleLogics.getRowData();
		ruleLogic.setMarkedForEdition(true);
		beanMode = BeanMode.actions;
		return TO_EDIT_ACTION;
	}
	
	public void viewRuleElementListener(AjaxBehaviorEvent event) {
		viewRuleElement();
	}
	
	public String viewRuleElement() {
		if (isDebugEnabled)
			logger.debug("viewRuleElement() - Entering...");
		if (ruleElements == null) {
			logger.warn("viewRuleElement() - RuleElement List is null.");
			return TO_FAILED;
		}
		if (!ruleElements.isRowAvailable()) {
			logger.warn("viewRuleElement() - RuleElement Row not available.");
			return TO_FAILED;
		}
		reset();
		origRuleElement = ruleElements.getRowData();
		ruleElement = (RuleElement) BlobUtil.deepCopy(origRuleElement);
		ruleElement.setMarkedForEdition(true);
		beanMode = BeanMode.elements;
		return TO_EDIT_ELEMENT;
	}
	
	public void doneRuleElementEditListener(AjaxBehaviorEvent event) {
		doneRuleElementEdit();
	}
	
	public String doneRuleElementEdit() {
		if (isDebugEnabled)
			logger.debug("doneRuleElementEdit() - Entering...");
		if (ruleElement == null) {
			logger.warn("doneRuleElementEdit() - RuleElement is null.");
			return TO_FAILED;
		}
		copyProperties(origRuleElement, ruleElement);
		if (StringUtils.isNotBlank(origRuleElement.getExclusions())) {
			if (StringUtils.isBlank(origRuleElement.getDelimiter())) {
				origRuleElement.setDelimiter(",");
			}
		}
		beanMode = BeanMode.edit;
		return TO_EDIT_LOGIC;
	}

	private void copyProperties(RuleElement dest, RuleElement src) {
//		RuleElementPK pk = new RuleElementPK();
//		pk.setRuleLogic(src.getRuleElementPK().getRuleLogic());
//		pk.setElementSequence(src.getRuleElementPK().getElementSequence());
//		dest.setRuleElementPK(pk);
		dest.setDataName(src.getDataName());
		dest.setHeaderName(src.getHeaderName());
		dest.setCriteria(src.getCriteria());
		dest.setCaseSensitive(src.isCaseSensitive());
		dest.setTargetText(src.getTargetText());
		dest.setTargetProcName(src.getTargetProcName());
		dest.setExclusions(src.getExclusions());
		dest.setExclListProcName(src.getExclListProcName());
		dest.setDelimiter(src.getDelimiter());
	}
	
	public void saveRuleElementListener(AjaxBehaviorEvent event) {
		saveRuleElement();
	}
	
	public String saveRuleElement() {
		if (isDebugEnabled)
			logger.debug("saveRuleElement() - Entering...");
		if (ruleElement == null) {
			logger.warn("saveRuleElement() - RuleElement is null.");
			return TO_FAILED;
		}
		reset();
		// update database
		if (StringUtils.isNotBlank(FacesUtil.getLoginUserId())) {
			ruleLogic.setUpdtUserId(FacesUtil.getLoginUserId());
		}
		// first delete the rule element
		getRuleElementService().deleteByPrimaryKey(ruleElement.getRuleElementPK());
		// insert the record
		getRuleElementService().insert(ruleElement);
		logger.info("saveRuleElement() - Element Rows Deleted: " + 1);
		//beanMode = BeanMode.edit;
		return "msgrule.ruleelement.saved";
	}

	public void saveRuleLogicListener(AjaxBehaviorEvent event) {
		saveRuleLogic();
	}

	public String saveRuleLogic() {
		if (isDebugEnabled)
			logger.debug("saveRuleLogic() - Entering...");
		if (getRuleLogic() == null) {
			logger.warn("saveRuleLogic() - RuleLogic is null.");
			return TO_FAILED;
		}
		reset();
		// set startTime from startDate, startHour and startMinute
		Calendar cal = Calendar.getInstance();
		cal.setTime(ruleLogic.getStartTime());
		ruleLogic.setStartTime(new Timestamp(cal.getTimeInMillis()));
		// end of startTime
		// update database
		if (StringUtils.isNotBlank(FacesUtil.getLoginUserId())) {
			ruleLogic.setUpdtUserId(FacesUtil.getLoginUserId());
		}
		if (editMode == true) {
			getRuleLogicService().update(ruleLogic);
			logger.info("saveRuleLogic() - Rows Updated: " + 1);
			int rowsDeleted = getRuleElementService().deleteByRuleName(ruleLogic.getRuleName());
			logger.info("saveRuleLogic() - Element Rows Deleted: " + rowsDeleted);
			int rowsInserted = insertRuleElements(ruleLogic.getRuleName());
			logger.info("saveRuleLogic() - Element Rows Inserted: " + rowsInserted);
		}
		else {
			List<RuleElement> elements = ruleLogic.getRuleElements();
			if (elements!=null) {
				for (RuleElement element : elements) {
					element.getRuleElementPK().setRuleLogic(ruleLogic);
				}
			}
			getRuleLogicService().insert(ruleLogic);
			logger.info("saveRuleLogic() - Rows Inserted: " + 1);
			addToRuleList(ruleLogic);
//			int elementsInserted = insertRuleElements(ruleLogic.getRuleName());
//			logger.info("saveRuleLogic() - Element Rows Inserted: " + elementsInserted);
		}
		//beanMode = BeanMode.list;
		return TO_CONFIG_CUSTOM_RULES;
	}

	protected int insertRuleElements(String _ruleName) {
		List<RuleElement> list = getRuleElementList();
		int rowsInserted = 0;
		for (int i=0; i<list.size(); i++) {
			RuleElement ruleElementVo = list.get(i);
			RuleElement vo = new RuleElement();
			ruleElementVo.copyPropertiesTo(vo);
			RuleElementPK pk = vo.getRuleElementPK();
			RuleLogic ruleLogic = getRuleLogicService().getByRuleName(_ruleName);
			pk.setRuleLogic(ruleLogic);
			pk.setElementSequence(i);
			getRuleElementService().insert(vo);
		}
		return rowsInserted;
	}
	
	@SuppressWarnings("unchecked")
	protected void addToRuleList(RuleLogic vo) {
		List<RuleLogic> list = (List<RuleLogic>) ruleLogics.getWrappedData();
		list.add(vo);
	}

	public void deleteRuleLogicsListener(AjaxBehaviorEvent event) {
		deleteRuleLogics();
	}

	public String deleteRuleLogics() {
		if (isDebugEnabled)
			logger.debug("deleteRuleLogics() - Entering...");
		if (ruleLogics == null) {
			logger.warn("deleteRuleLogics() - RuleLogic List is null.");
			return TO_FAILED;
		}
		reset();
		List<RuleLogic> list = getRuleLogicList();
		for (int i = 0; i < list.size(); i++) {
			RuleLogic vo = list.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getRuleLogicService().deleteByRuleName(vo.getRuleName());
				if (rowsDeleted > 0) {
					logger.info("deleteRuleLogics() - RuleLogic deleted: " + vo.getRuleName());
				}
				list.remove(vo);
			}
		}
		return "msgrule.deleted";
	}
	
	public String testRuleLogic() {
		if (isDebugEnabled)
			logger.debug("testRuleLogic() - Entering...");
		if (getRuleLogic() == null) {
			logger.warn("testRuleLogic() - RuleLogic is null.");
			return TO_FAILED;
		}
		return TO_SELF;
	}
	
	public void copyRuleLogicListener(AjaxBehaviorEvent event) {
		copyRuleLogic();
	}
	
	public String copyRuleLogic() {
		if (isDebugEnabled)
			logger.debug("copyRuleLogic() - Entering...");
		if (ruleLogics == null) {
			logger.warn("copyRuleLogic() - RuleLogic List is null.");
			return TO_FAILED;
		}
		reset();
		List<RuleLogic> list = getRuleLogicList();
		for (int i=0; i<list.size(); i++) {
			RuleLogic vo = list.get(i);
			if (vo.isMarkedForDeletion()) {
				this.ruleLogic = new RuleLogic();
				try {
					vo.copyPropertiesTo(this.ruleLogic);
					this.ruleLogic.setMarkedForDeletion(false);
					vo.setMarkedForDeletion(false);
				}
				catch (Exception e) {
					logger.error("BeanUtils.copyProperties() failed: ", e);
				}
				ruleElements = null;
				getRuleElements();
				// set processor fields to null as they are invisible from UI
				List<RuleElement> elements = getRuleElementList();
				for (RuleElement element : elements) {
					element.setTargetProcName(null);
					element.setExclListProcName(null);
				}
				// end of null
				ruleElements.getWrappedData();
				ruleLogic.setRuleName(null);
				ruleLogic.setEvalSequence(getRuleLogicService().getNextEvalSequence());
				ruleLogic.setRuleType(RuleType.SIMPLE.getValue());
				ruleLogic.setMarkedForEdition(true);
				editMode = false;
				beanMode = BeanMode.insert;
				return TO_EDIT_LOGIC;
			}
		}
		return TO_SELF;
	}
	
	public void addRuleLogicListener(AjaxBehaviorEvent event) {
		addRuleLogic();
	}
	
	public String addRuleLogic() {
		if (isDebugEnabled)
			logger.debug("addRuleLogic() - Entering...");
		reset();
		ruleElements = null;
		this.ruleLogic = new RuleLogic();
		ruleLogic.setMarkedForEdition(true);
		//ruleLogic.setUpdtUserId(Constants.DEFAULT_USER_ID);
		ruleLogic.setEvalSequence(getRuleLogicService().getNextEvalSequence());
		ruleLogic.setRuleType(RuleType.SIMPLE.getValue());
		ruleLogic.setRuleCategory(RuleCategory.MAIN_RULE.getValue());
		ruleLogic.setStartTime(new Timestamp(new java.util.Date().getTime()));
		editMode = false;
		beanMode = BeanMode.insert;
		return TO_EDIT_LOGIC;
	}
	
	public void cancelEditListener(AjaxBehaviorEvent event) {
		String sourceId = "";
		if (event != null) {
			logger.info("cancelEditListener() - " + event.getSource());
			Object obj = event.getSource();
			if (obj instanceof HtmlCommandButton) {
				HtmlCommandButton cmdbtn = (HtmlCommandButton) obj;
				logger.info("HtmlCommandButton value: " + cmdbtn.getValue() + ", Id: " + cmdbtn.getId());
				sourceId = cmdbtn.getId();
			}
		}
		cancelEdit(sourceId);
	}
	
	public void cancelEdit(String sourceId) {
		if (isDebugEnabled)
			logger.debug("cancelEdit() - source id = " + sourceId);
		refresh();
		if (StringUtils.contains(sourceId, "LogicEdit")) {
			beanMode = BeanMode.list;
		}
		else if (StringUtils.contains(sourceId, "ElementEdit")) {
			beanMode = BeanMode.edit;
		}
		else if (StringUtils.contains(sourceId, "ActionEdit")) {
			beanMode = BeanMode.list;
		}
		else if (StringUtils.contains(sourceId, "SubruleEdit")) {
			beanMode = BeanMode.list;
		}
		else {
			beanMode = BeanMode.list;
		}
	}
	
	public String cancelEdit() {
		if (isDebugEnabled)
			logger.debug("cancelEdit() - Entering...");
		refresh();
		String viewId = FacesUtil.getCurrentViewId();
		if (StringUtils.contains(viewId, TO_EDIT_ELEMENT)) {
			beanMode = BeanMode.edit;
			return TO_EDIT_LOGIC;
		}
		else if (StringUtils.contains(viewId, TO_EDIT_ACTION)) {
			beanMode = BeanMode.list;
			return TO_CONFIG_CUSTOM_RULES;
		}
		else if (StringUtils.contains(viewId, TO_EDIT_SUBRULE)) {
			beanMode = BeanMode.edit;
			return TO_CONFIG_CUSTOM_RULES;
		}
		beanMode = BeanMode.list;
		return TO_CANCELED;
	}
	
	public boolean getCanMoveUp() {
		if (ruleLogics == null || !ruleLogics.isRowAvailable()) {
			return false;
		}
		RuleLogic vo = ruleLogics.getRowData();
		int idx = ruleLogics.getRowIndex();
		if (idx > 0) {
			RuleLogic up = getRuleLogicList().get(idx - 1);
			if (vo.getRuleCategory().equals(up.getRuleCategory())) {
				return true;
			}
		}
		return false;
	}
	
	public boolean getCanMoveDown() {
		if (ruleLogics == null || !ruleLogics.isRowAvailable()) {
			return false;
		}
		RuleLogic vo = ruleLogics.getRowData();
		int idx = ruleLogics.getRowIndex();
		if (idx < (ruleLogics.getRowCount() + 1)) {
			RuleLogic down = getRuleLogicList().get(idx + 1);
			if (vo.getRuleCategory().equals(down.getRuleCategory())) {
				return true;
			}
		}
		return false;
	}
	
	public String moveUp() {
		if (isDebugEnabled)
			logger.debug("moveUp() - Entering...");
		moveUpDownRule(-1);
		return TO_SELF;
	}
	
	public String moveDown() {
		if (isDebugEnabled)
			logger.debug("moveDown() - Entering...");
		moveUpDownRule(1);
		return TO_SELF;
	}
	
	public void moveUpListener(AjaxBehaviorEvent event) {
		logger.info("moveUpListener() - Event Source: " + event.getSource());
		moveUp();
	}

	public void moveDownListener(AjaxBehaviorEvent event) {
		logger.info("moveDownListener() - Event Source: " + event.getSource());
		moveDown();
	}

	/*
	 * @param updown - move current rule up or down:
	 * 		-1 -> move up
	 * 		+1 -> move down
	 */
	protected void moveUpDownRule(int updown) {
		reset();
		RuleLogic currVo = getAll().getRowData();
		int index = ruleLogics.getRowIndex();
		List<RuleLogic> list = getRuleLogicList();
		RuleLogic prevVo = list.get(index + updown);
		if (currVo.getRuleCategory().equals(prevVo.getRuleCategory())) {
			int currSeq = currVo.getEvalSequence();
			int prevSeq = prevVo.getEvalSequence();
			currVo.setEvalSequence(prevSeq);
			prevVo.setEvalSequence(currSeq);
			getRuleLogicService().update(currVo);
			getRuleLogicService().update(prevVo);
			refresh();
		}
	}
	
	/*
	 * Rule Elements Section
	 */
	
	public void refreshElementsListener(AjaxBehaviorEvent event) {
		refreshElements();
	}
	
	public String refreshElements() {
		if (isDebugEnabled)
			logger.debug("refreshElements() - Entering...");
		ruleElements = null;
		getRuleElements();
		return TO_SELF;
	}
	
	public DataModel<RuleElement> getRuleElements() {
		if (isDebugEnabled)
			logger.debug("getRuleElement() - Entering...");
		if (getRuleLogic() == null) {
			logger.warn("getRuleElements() - RuleLogic is null.");
			return null;
		}
		if (ruleElements == null) {
			String key = ruleLogic.getRuleName();
			List<RuleElement> list = getRuleElementService().getByRuleName(key);
			ruleElements = new ListDataModel<RuleElement>(list);
		}
		return ruleElements;
	}
	
	@SuppressWarnings("unchecked")
	protected List<RuleElement> getRuleElementList() {
		List<RuleElement> list = (List<RuleElement>) getRuleElements().getWrappedData();
		return list;
	}
	
	public void deleteRuleElementsListener(AjaxBehaviorEvent event) {
		deleteRuleElements();
	}

	public String deleteRuleElements() {
		if (isDebugEnabled)
			logger.debug("deleteRuleElements() - Entering...");
		if (ruleElements == null) {
			logger.warn("deleteRuleElements() - RuleElement List is null.");
			return TO_FAILED;
		}
		reset();
		List<RuleElement> list = getRuleElementList();
		for (int i = 0; i < list.size(); i++) {
			RuleElement vo = list.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getRuleElementService().deleteByPrimaryKey(vo.getRuleElementPK());
				if (rowsDeleted > 0) {
					logger.info("deleteRuleElements() - RuleElement deleted: " + vo.getRuleElementPK());
				}
				list.remove(vo);
			}
		}
		return TO_SELF;
	}
	
	public void copyRuleElementListener(AjaxBehaviorEvent event) {
		copyRuleElement();
	}

	public String copyRuleElement() {
		if (isDebugEnabled)
			logger.debug("copyRuleElement() - Entering...");
		if (ruleElements == null) {
			logger.warn("copyRuleElement() - RuleElement List is null.");
			return TO_FAILED;
		}
		reset();
		List<RuleElement> list = getRuleElementList();
		for (int i=0; i<list.size(); i++) {
			RuleElement vo = list.get(i);
			if (vo.isMarkedForDeletion()) {
				RuleElement vo2 = new RuleElement();
				try {
					vo.copyPropertiesTo(vo2);
					vo2.setMarkedForDeletion(false);
					vo.setMarkedForDeletion(false);
				}
				catch (Exception e) {
					logger.error("BeanUtils.copyProperties() failed: ", e);
				}
				vo2.getRuleElementPK().setElementSequence(getNextRuleElementSeq());
				vo2.setMarkedForEdition(true);
				list.add(vo2);
				break;
			}
		}
		return TO_SELF;
	}
	
	public void addRuleElementListener(AjaxBehaviorEvent event) {
		addRuleElement();
	}

	public String addRuleElement() {
		if (isDebugEnabled)
			logger.debug("addRuleElement() - Entering...");
		reset();
		List<RuleElement> list = getRuleElementList();
		RuleElement vo = new RuleElement();
		RuleElementPK pk = new RuleElementPK();
		vo.setRuleElementPK(pk);
		pk.setRuleLogic(ruleLogic);
		vo.getRuleElementPK().setElementSequence(getNextRuleElementSeq());
		vo.setDataName(RuleDataName.BCC_ADDR.getValue());
		vo.setCriteria(RuleCriteria.STARTS_WITH.getValue());
		vo.setMarkedForEdition(true);
		list.add(vo);
		return TO_SELF;
	}
	
	private int getNextRuleElementSeq() {
		List<RuleElement> list = getRuleElementList();
		if (list == null || list.isEmpty()) {
			return 0;
		}
		else {
			int seq = list.size() - 1;
			for (RuleElement vo : list) { // just for safety
				if (vo.getRuleElementPK().getElementSequence() > seq) {
					seq = vo.getRuleElementPK().getElementSequence();
				}
			}
			return seq + 1;
		}
	}
	
	/*
	 * Sub-Rules Section
	 */
	
	public void refreshSubRulesListener(AjaxBehaviorEvent event) {
		refreshSubRules();
	}
	
	public String refreshSubRules() {
		if (isDebugEnabled)
			logger.debug("refreshSubRules() - Entering...");
		subRules = null;
		getSubRules();
		return TO_SELF;
	}
	
	public DataModel<RuleSubruleMap> getSubRules() {
		if (isDebugEnabled)
			logger.debug("getSubRules() - Entering...");
		if (getRuleLogic() == null) {
			logger.warn("getSubRules() - RuleLogic is null.");
			return null;
		}
		if (subRules == null) {
			String key = ruleLogic.getRuleName();
			List<RuleSubruleMap> list = getRuleSubruleMapService().getByRuleName(key);
			subRules = new ListDataModel<RuleSubruleMap>(list);
		}
		return subRules;
	}
	
	@SuppressWarnings("unchecked")
	protected List<RuleSubruleMap> getSubRuleList() {
		List<RuleSubruleMap> list = (List<RuleSubruleMap>) getSubRules().getWrappedData();
		return list;
	}
	
	public void deleteSubRulesListener(AjaxBehaviorEvent event) {
		deleteSubRules();
	}

	public String deleteSubRules() {
		if (isDebugEnabled)
			logger.debug("deleteSubRules() - Entering...");
		if (subRules == null) {
			logger.warn("deleteSubRules() - SubRule List is null.");
			return TO_FAILED;
		}
		reset();
		List<RuleSubruleMap> list = getSubRuleList();
		for (int i=0; i<list.size(); i++) {
			RuleSubruleMap vo = list.get(i); 
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getRuleSubruleMapService().deleteByPrimaryKey(vo.getRuleSubruleMapPK());
				if (rowsDeleted > 0) {
					logger.info("deleteSubRules() - SubRule deleted: " + vo.getRuleSubruleMapPK());
				}
				vo.setMarkedForDeletion(false);
				list.remove(vo);
			}
		}
		refreshSubRules();
		return TO_SELF;
	}
	
	public void copySubRuleListener(AjaxBehaviorEvent event) {
		copySubRule();
	}

	public String copySubRule() {
		if (isDebugEnabled)
			logger.debug("copySubRule() - Entering...");
		if (subRules == null) {
			logger.warn("copySubRule() - SubRule List is null.");
			return TO_FAILED;
		}
		reset();
		List<RuleSubruleMap> list = getSubRuleList();
		for (int i=0; i<list.size(); i++) {
			RuleSubruleMap vo = list.get(i);
			if (vo.isMarkedForDeletion()) {
				RuleSubruleMap vo2 = new RuleSubruleMap();
				try {
					vo.copyPropertiesTo(vo2);
					vo2.setMarkedForDeletion(false);
					vo.setMarkedForDeletion(false);
				}
				catch (Exception e) {
					logger.error("BeanUtils.copyProperties() failed: ", e);
				}
				vo2.setMarkedForEdition(true);
				list.add(vo2);
				break;
			}
		}
		return TO_SELF;
	}
	
	public void addSubRuleListener(AjaxBehaviorEvent event) {
		addSubRule();
	}

	public String addSubRule() {
		if (isDebugEnabled)
			logger.debug("addSubRule() - Entering...");
		reset();
		List<RuleSubruleMap> list = getSubRuleList();
		RuleSubruleMap vo = new RuleSubruleMap();
		RuleSubruleMapPK pk = new RuleSubruleMapPK();
		pk.setRuleLogic(ruleLogic);
		vo.setRuleSubruleMapPK(pk);
		vo.setMarkedForEdition(true);
		List<RuleLogic> subrules = getRuleLogicService().getSubrules(false);
		if (!subrules.isEmpty()) { // set a default rule name
			vo.setSubruleName(subrules.get(0).getRuleName());
		}
		list.add(vo);
		return TO_SELF;
	}
	
	public void saveSubRulesListener(AjaxBehaviorEvent event) {
		saveSubRules();
	}

	public String saveSubRules() {
		if (isDebugEnabled)
			logger.debug("saveSubRules() - Entering...");
		if (getRuleLogic() == null) {
			logger.warn("saveSubRules() - RuleLogic is null.");
			return TO_FAILED;
		}
		reset();
		// update database
		int rowsDeleted = getRuleSubruleMapService().deleteByRuleName(ruleLogic.getRuleName());
		logger.info("saveSubRules() - SubRule Rows Deleted: " + rowsDeleted);
		
		List<RuleSubruleMap> list = getSubRuleList();
		if (hasDuplicateSubRules(list)) {
			testResult = "duplicateSubRuleFound";
			/* Add to Face message queue. Not working. */
	        FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
					"jpa.msgui.messages", testResult, null);
			FacesContext.getCurrentInstance().addMessage(null, message);
			return null;
		}
		for (int i=0; i<list.size(); i++) {
			RuleSubruleMap ruleSubRuleMapVo = list.get(i);
			RuleLogic subrule = getRuleLogicService().getByRuleName(ruleSubRuleMapVo.getSubruleName());
			if (subrule != null) { // should never happen
				ruleSubRuleMapVo.getRuleSubruleMapPK().setSubruleLogic(subrule);
				ruleSubRuleMapVo.setSubruleSequence(i);
				getRuleSubruleMapService().insert(ruleSubRuleMapVo);
			}
		}
		logger.info("saveSubRules() - SubRule Rows Inserted: " + list.size());
		//beanMode = BeanMode.subrules;
		return TO_CONFIG_CUSTOM_RULES;
	}

	public void moveUpSubRuleListener(AjaxBehaviorEvent event) {
		if (isDebugEnabled)
			logger.debug("moveUpSubRule() - Entering...");
		moveSubRule(-1);
	}
	
	public void moveDownSubRuleListener(AjaxBehaviorEvent event) {
		if (isDebugEnabled)
			logger.debug("moveDownSubRule() - Entering...");
		moveSubRule(1);
	}
	
	protected boolean hasDuplicateSubRules(List<RuleSubruleMap> list) {
		if (list == null || list.size() <= 1) {
			return false;
		}
		for (int i=0; i<list.size(); i++) {
			RuleSubruleMap vo1 = list.get(i);
			for (int j=i+1; j<list.size(); j++) {
				RuleSubruleMap vo2 = list.get(j);
				if (StringUtils.equals(vo1.getSubruleName(), vo2.getSubruleName())) {
					return true;
				}
			}
		}
		return false;
	}
	
	/*
	 * @param updown - move current sub-rule up or down:
	 * 		-1 -> move up
	 * 		+1 -> move down
	 */
	protected void moveSubRule(int updown) {
		reset();
		RuleSubruleMap currVo = getSubRules().getRowData();
		int index = subRules.getRowIndex();
		List<RuleSubruleMap> list = getSubRuleList();
		RuleSubruleMap prevVo = list.get(index + updown);
		int currSeq = currVo.getSubruleSequence();
		int prevSeq = prevVo.getSubruleSequence();
		currVo.setSubruleSequence(prevSeq);
		prevVo.setSubruleSequence(currSeq);
		getRuleSubruleMapService().update(currVo);
		getRuleSubruleMapService().update(prevVo);
		refreshSubRules();
	}
	
	/*
	 * Message Actions Section
	 */
	
	public void refreshMsgActionsListener(AjaxBehaviorEvent event) {
		refreshMsgActions();
	}
	
	public String refreshMsgActions() {
		if (isDebugEnabled)
			logger.debug("refreshMsgActions() - Entering...");
		ruleActions = null;
		getMsgActions();
		return TO_SELF;
	}
	
	public DataModel<RuleAction> getMsgActions() {
		if (isDebugEnabled)
			logger.debug("getMsgActions() - Entering...");
		if (getRuleLogic() == null) {
			logger.warn("getMsgActions() - RuleLogic is null.");
			return null;
		}
		if (ruleActions == null) {
			String key = ruleLogic.getRuleName();
			List<RuleAction> list = getRuleActionService().getByRuleName(key);
			ruleActions = new ListDataModel<RuleAction>(list);
		}
		return ruleActions;
	}
	
	protected List<RuleAction> getMsgActionList() {
		@SuppressWarnings("unchecked")
		List<RuleAction> list = (List<RuleAction>) getMsgActions().getWrappedData();
		return list;
	}
	
	public void deleteMsgActionsListener(AjaxBehaviorEvent event) {
		deleteMsgActions();
	}

	public String deleteMsgActions() {
		if (isDebugEnabled)
			logger.debug("deleteMsgActions() - Entering...");
		if (ruleActions == null) {
			logger.warn("deleteMsgActions() - MsgAction List is null.");
			return TO_FAILED;
		}
		reset();
		List<RuleAction> list = getMsgActionList();
		for (int i = 0; i < list.size(); i++) {
			RuleAction vo = list.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getRuleActionService().deleteByPrimaryKey(vo.getRuleActionPK());
				if (rowsDeleted > 0) {
					logger.info("deleteMsgActions() - MsgAction deleted: " + vo.getRuleActionPK().getRuleLogic().getRuleName() + "."
							+ vo.getRuleActionPK().getActionSequence() + "." + vo.getRuleActionPK().getStartTime() +
							"." + (vo.getRuleActionPK().getSenderData()==null?"":vo.getRuleActionPK().getSenderData().getSenderId()));
				}
				list.remove(vo);
			}
		}
		return TO_SELF;
	}
	
	public void copyMsgActionListener(AjaxBehaviorEvent event) {
		copyMsgAction();
	}

	public String copyMsgAction() {
		if (isDebugEnabled)
			logger.debug("copyMsgAction() - Entering...");
		if (ruleActions == null) {
			logger.warn("copyMsgAction() - MsgAction List is null.");
			return TO_FAILED;
		}
		reset();
		List<RuleAction> list = getMsgActionList();
		for (int i=0; i<list.size(); i++) {
			RuleAction vo = list.get(i);
			if (vo.isMarkedForDeletion()) {
				RuleAction vo2 =  new RuleAction();
				try {
					vo.copyPropertiesTo(vo2);
					vo2.setMarkedForDeletion(false);
					vo.setMarkedForDeletion(false);
				}
				catch (Exception e) {
					logger.error("BeanUtils.copyProperties() failed: ", e);
				}
				vo2.setMarkedForEdition(true);
				list.add(vo2);
				break;
			}
		}
		return TO_SELF;
	}
	
	public void addMsgActionListener(AjaxBehaviorEvent event) {
		addMsgAction();
	}

	public String addMsgAction() {
		if (isDebugEnabled)
			logger.debug("addMsgAction() - Entering...");
		reset();
		List<RuleAction> list = getMsgActionList();
		RuleAction vo = new RuleAction();
		RuleActionPK pk = new RuleActionPK();
		vo.setRuleActionPK(pk);
		vo.getRuleActionPK().setActionSequence(list.size() + 1);
		vo.getRuleActionPK().setStartTime(new Timestamp(System.currentTimeMillis()));
		vo.setStatusId(CodeType.YES_CODE.getValue());
		vo.setMarkedForEdition(true);
		List<String> actionIdList = getRuleActionDetailService().getActionIdList();
		RuleActionDetail detail = getRuleActionDetailService().getByActionId(actionIdList.get(0));
		vo.setRuleActionDetail(detail);
		list.add(vo);
		return TO_SELF;
	}
	
	public void saveMsgActionsListener(AjaxBehaviorEvent event) {
		saveMsgActions();
	}

	public String saveMsgActions() {
		if (isDebugEnabled)
			logger.debug("saveMsgActions() - Entering..., sourcePage = " + sourcePage);
		if (getRuleLogic() == null) {
			logger.warn("saveMsgActions() - RuleLogic is null.");
			return TO_FAILED;
		}
		reset();
		// update database
		int rowsDeleted = getRuleActionService().deleteByRuleName(ruleLogic.getRuleName());
		logger.info("saveMsgActions() - MsgAction Rows Deleted: " + rowsDeleted);
		
		List<RuleAction> list = getMsgActionList();
		for (int i=0; i<list.size(); i++) {
			RuleAction ruleAction = list.get(i);
			ruleAction.getRuleActionPK().setRuleLogic(ruleLogic);
			// set startTime from startDate and startHour
			Calendar cal = Calendar.getInstance();
			cal.setTime(ruleAction.getRuleActionPK().getStartTime());
			ruleAction.getRuleActionPK().setStartTime(new Timestamp(cal.getTimeInMillis()));
			// end of startTime
			getRuleActionService().insert(ruleAction);
		}
		logger.info("saveMsgActions() - MsgAction Rows Inserted: " + list.size());
		logger.info("saveMsgActions() - View Id: " + FacesUtil.getCurrentViewId());
		if ("rule-action-built-in".equals(sourcePage)) {
			//beanMode = BeanMode.actions;
			return TO_CUSTOMIZE_BUILTIN_RULES;
		}
		else if ("rule-action".equals(sourcePage)) {
			//beanMode = BeanMode.actions;
			return RULE_ACTION_SAVED;
		}
		return TO_SELF;
	}

	/*
	 * @deprecated - not used by any JSF pages
	 * define value change listener
	 */
	public void actionIdChanged(ValueChangeEvent event) {
		if (getRuleLogic() == null) {
			logger.warn("actionIdChanged() - RuleLogic is null.");
		}
		logger.info("actionIdChanged() - " + event.getOldValue() + " -> " + event.getNewValue());
		for (Iterator<RuleAction> it=ruleActions.iterator(); it.hasNext();) {
			RuleAction ra = it.next();
			logger.info("actionIdChanged() - RuleAction Id: " + ra.getRuleActionDetail().getActionId());
		}
	}

	public void changedActionId(AjaxBehaviorEvent event) {
		if (getRuleLogic() == null) {
			logger.warn("changedActionId() - RuleLogic is null.");
		}
		logger.info("changedActionId() - " + event);
		for (Iterator<RuleAction> it=ruleActions.iterator(); it.hasNext();) {
			RuleAction ra = it.next();
			logger.info("changedActionId() - RuleAction Id: " + ra.getRuleActionDetail().getActionId());
		}
		if (event == null) return;
		UISelectOne select = (UISelectOne) event.getSource();
        if (select.getValue() == null || select.getValue().toString().isEmpty()) {
            logger.info("Selected value is blank");
        }
        else {
            String value = select.getValue().toString();
            logger.info("Selected value: " + value);
        }
	}

	/*
	 * define ajax listener for ruleActionBuiltinEdit.xhtml
	 * jsf2 ajax event list:
	  	blur
		change
		click
		dblclick
		focus
		keydown
		keypress
		keyup
		mousedown
		mousemove
		mouseout
		mouseover
		mouseup
		select
	 */
	public void changedSenderId(AjaxBehaviorEvent event) {
		if (getRuleLogic() == null) {
			logger.warn("changeSenderId() - RuleLogic is null.");
		}
		for (Iterator<RuleAction> it=ruleActions.iterator(); it.hasNext();) {
			RuleAction ra = it.next();
			logger.info("changeSenderId() - Sender Id: " + ra.getSenderId());
		}
		if (event == null) return;
		UISelectOne select = (UISelectOne) event.getSource();
		//UIComponent component = event.getComponent();
        if (select.getValue() == null || select.getValue().toString().isEmpty()) {
            logger.info("Selected value is blank");
        }
        else {
            String value = select.getValue().toString();
            logger.info("Selected value: " + value);
        }
	}


	/*
	 * Logic Evaluation Section 
	 */
	
	public boolean getAnyRulesMarkedForDeletion() {
		if (isDebugEnabled)
			logger.debug("getAnyRulesMarkedForDeletion() - Entering...");
		List<RuleLogic> list = getRuleLogicList();
		for (Iterator<RuleLogic> it=list.iterator(); it.hasNext();) {
			RuleLogic vo = it.next();
			if (vo.isMarkedForDeletion()) {
				return true;
			}
		}
		return false;
	}

	public boolean getAnyElementsMarkedForDeletion() {
		if (isDebugEnabled)
			logger.debug("getAnyElementsMarkedForDeletion() - Entering...");
		List<RuleElement> list = getRuleElementList();
		for (Iterator<RuleElement> it=list.iterator(); it.hasNext();) {
			RuleElement vo = it.next();
			if (vo.isMarkedForDeletion()) {
				return true;
			}
		}
		return false;
	}

	public boolean getAnySubRulesMarkedForDeletion() {
		if (isDebugEnabled)
			logger.debug("getAnySubRulesMarkedForDeletion() - Entering...");
		List<RuleSubruleMap> list = getSubRuleList();
		for (Iterator<RuleSubruleMap> it=list.iterator(); it.hasNext();) {
			RuleSubruleMap vo = it.next();
			if (vo.isMarkedForDeletion()) {
				return true;
			}
		}
		return false;
	}

	public boolean getAnyMsgActionsMarkedForDeletion() {
		if (isDebugEnabled)
			logger.debug("getAnyMsgActionsMarkedForDeletion() - Entering...");
		List<RuleAction> list = getMsgActionList();
		for (Iterator<RuleAction> it=list.iterator(); it.hasNext();) {
			RuleAction vo = it.next();
			if (vo.isMarkedForDeletion()) {
				return true;
			}
		}
		return false;
	}

	/*
	 * used by ruleLogicEdit.xhtml, where ruleLogic instance has been instantiated.
	 */
	public boolean isHasSubrules() {
		if (isDebugEnabled)
			logger.debug("isHasSubrules() - Entering...");
		if (getRuleLogic() != null) {
			return getRuleLogicService().getHasSubrules(ruleLogic.getRuleName());
		}
		return false;
	}

	public boolean hasSubrule(String ruleName) {
		if (isDebugEnabled)
			logger.debug("isHasSubrule() - Entering...");
		if (hasSubruleMap.containsKey(ruleName)) {
			return hasSubruleMap.get(ruleName);
		}
		return getRuleLogicService().getHasSubrules(ruleName);
	}

	/*
	 * used by customizeBuiltinRules.xhtml, inside DataTable loop where RowData is available.
	 */
	public boolean getHasMsgActions() {
		if (isDebugEnabled)
			logger.debug("getHasMsgActions() - Entering...");
		if (ruleLogics != null) {
			RuleLogic vo = ruleLogics.getRowData();
			return getRuleActionService().getHasActions(vo.getRuleName());
		}
		return false;
	}

	public boolean hasMsgActions(String ruleName) {
		if (isDebugEnabled)
			logger.debug("hasMsgActions(\"" + ruleName + "\") - Entering...");
		if (hasActionsMap.containsKey(ruleName)) {
			return hasActionsMap.get(ruleName);
		}
		return getRuleActionService().getHasActions(ruleName);
	}

	/*
	 * Validation Section
	 */
	
	/**
	 * check primary key
	 * @param context
	 * @param component
	 * @param value
	 */
	public void validatePrimaryKey(FacesContext context, UIComponent component, Object value) {
		if (isDebugEnabled)
			logger.debug("validatePrimaryKey() - ruleName: " + value);
		String ruleName = (String) value;
		if (getRuleLogic() != null) {
			int seq = ruleLogic.getEvalSequence();
			logger.debug("RuleLogic sequence: " + seq);
		}
		else {
			logger.error("validatePrimaryKey(() - RuleLogic is null");
			return;
		}

		RuleLogic vo = getRuleLogicService().getByRuleName(ruleName);
		if (editMode == false && vo != null) {
			// ruleLogic already exist
	        FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
					"jpa.msgui.messages", "ruleLogicAlreadyExist", new String[] {ruleName});
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}

		if (editMode == true && vo == null) {
			// ruleLogic does not exist
	        FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
					"jpa.msgui.messages", "ruleLogicDoesNotExist", new String[] {ruleName});
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}
	
	/**
	 * check regular expression
	 * @param context
	 * @param component
	 * @param value
	 */
	public void validateRegex(FacesContext context, UIComponent component, Object value) {
		if (isDebugEnabled)
			logger.debug("validateRegex() - regex: " + value);
		String regex = (String) value;
		try {
			Pattern.compile(regex);
		}
		catch (PatternSyntaxException e) {
			FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
					"jpa.msgui.messages", "invalidRegex", new String[] {regex});
			message.setSeverity(FacesMessage.SEVERITY_ERROR);
			//context.addMessage(component.getClientId(context), message);
			throw new ValidatorException(message);
		}
	}
	
	/**
	 * check start date
	 * 
	 * @param context
	 * @param component
	 * @param value
	 */
	public void checkStartDate(FacesContext context, UIComponent component, Object value) {
		if (isDebugEnabled)
			logger.debug("checkStartDate() - startDate = " + value);
		if (value instanceof Date) {
		    Calendar cal = Calendar.getInstance();
		    cal.setTime((Date)value);
			return;
		}
		((UIInput)component).setValid(false);
		FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
				"jpa.msgui.messages", "invalidDate", new Object[] {value});
		message.setSeverity(FacesMessage.SEVERITY_ERROR);
		context.addMessage(component.getClientId(context), message);
	}
	
	/*
	 * "testResult" and "actionFailure" are messages generated by action
	 * commands, set them to null so they will no longer be rendered.
	 */
	void reset() {
		testResult = null;
		actionFailure = null;
		ruleNameInput = null;
		startDateInput = null;
	}
	
	@SuppressWarnings({ "unchecked" })
	protected List<RuleLogic> getRuleLogicList() {
		if (ruleLogics == null) {
			return new ArrayList<RuleLogic>();
		}
		else {
			return (List<RuleLogic>)ruleLogics.getWrappedData();
		}
	}
	
	public RuleLogic getRuleLogic() {
		return ruleLogic;
	}

	public void setRuleLogic(RuleLogic ruleLogic) {
		this.ruleLogic = ruleLogic;
	}

	public boolean isEditMode() {
		return editMode;
	}

	public void setEditMode(boolean editMode) {
		this.editMode = editMode;
	}

	public String getBeanMode() {
		return beanMode == null ? "" : beanMode.name();
	}

	public void setBeanMode(String beanMode) {
		try {
			this.beanMode = BeanMode.valueOf(beanMode);
		}
		catch (Exception e) {}
	}

	public UIInput getRuleNameInput() {
		return ruleNameInput;
	}

	public void setRuleNameInput(UIInput smtpHostInput) {
		this.ruleNameInput = smtpHostInput;
	}

	public String getTestResult() {
		return testResult;
	}

	public void setTestResult(String testResult) {
		this.testResult = testResult;
	}

	public String getActionFailure() {
		return actionFailure;
	}

	public void setActionFailure(String actionFailure) {
		this.actionFailure = actionFailure;
	}

	public DataModel<RuleLogic> getRuleLogics() {
		return ruleLogics;
	}

	public UIInput getStartDateInput() {
		return startDateInput;
	}

	public void setStartDateInput(UIInput startDateInput) {
		this.startDateInput = startDateInput;
	}

	public RuleElement getRuleElement() {
		return ruleElement;
	}

	public void setRuleElement(RuleElement ruleElement) {
		this.ruleElement = ruleElement;
	}
}
