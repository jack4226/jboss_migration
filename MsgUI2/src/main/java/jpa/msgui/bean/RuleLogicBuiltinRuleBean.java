package jpa.msgui.bean;

import java.util.List;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import jpa.model.rule.RuleLogic;
import jpa.msgui.util.FacesUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@javax.inject.Named("builtinRule")
@javax.enterprise.context.RequestScoped
public class RuleLogicBuiltinRuleBean extends RuleLogicBean {
	private static final long serialVersionUID = -498930141487046944L;
	protected static final Logger logger = LogManager.getLogger(RuleLogicBuiltinRuleBean.class);
	protected static final boolean isDebugEnabled = logger.isDebugEnabled();

	@Override
	public DataModel<RuleLogic> getAll() {
		String fromPage = FacesUtil.getRequestParameter("frompage");
		if (fromPage != null && fromPage.equals("main")) {
			refresh();
		}
		if (ruleLogics == null) {
			List<RuleLogic> ruleLogicList = getRuleLogicService().getAll(true);
			for (RuleLogic rc : ruleLogicList) {
				boolean hasActions = getRuleActionService().getHasActions(rc.getRuleName());
				hasActionsMap.put(rc.getRuleName(), hasActions);
			}
			ruleLogics = new ListDataModel<RuleLogic>(ruleLogicList);
		}
		return ruleLogics;
	}
	
}
