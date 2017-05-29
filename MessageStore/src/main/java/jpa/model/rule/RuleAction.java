package jpa.model.rule;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.faces.component.UISelectOne;
import javax.faces.event.AjaxBehaviorEvent;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import jpa.data.preload.RuleDataTypeEnum;
import jpa.model.BaseModel;
import jpa.model.SenderData;
import jpa.service.common.SenderDataService;
import jpa.service.rule.RuleActionDetailService;
import jpa.service.rule.RuleDataValueService;
import jpa.spring.util.SpringUtil;

@Entity
@Table(name="rule_action", 
	uniqueConstraints=@UniqueConstraint(columnNames = {"RuleLogicRowId", "actionSequence", "startTime", "SenderDataRowId"}))
public class RuleAction extends BaseModel implements java.io.Serializable {
	private static final long serialVersionUID = 6097614369008930898L;
	protected static final Logger logger = Logger.getLogger(RuleAction.class);

	@Embedded
	private RuleActionPK ruleActionPK;
	
	@ManyToOne(targetEntity=RuleActionDetail.class, fetch=FetchType.EAGER, optional=false)
	@JoinColumn(name="RuleActionDetailRowId", insertable=true, updatable=true, referencedColumnName="row_id", nullable=false)
	@XmlTransient
	private RuleActionDetail ruleActionDetail;

	@Column(nullable=true, length=4054)
	private String fieldValues = null;

	public RuleAction() {
		// must have a no-argument constructor
	}

	public RuleAction(RuleLogic ruleLogic, int actionSequence,
			Timestamp startTime, SenderData senderData,
			RuleActionDetail ruleActionDetail, String fieldValues) {
		ruleActionPK = new RuleActionPK();
		ruleActionPK.setRuleLogic(ruleLogic);
		ruleActionPK.setActionSequence(actionSequence);
		ruleActionPK.setStartTime(startTime);
		ruleActionPK.setSenderData(senderData);
		this.ruleActionDetail = ruleActionDetail;
		this.fieldValues = fieldValues;
	}

	public RuleActionPK getRuleActionPK() {
		return ruleActionPK;
	}

	public void setRuleActionPK(RuleActionPK ruleActionPK) {
		this.ruleActionPK = ruleActionPK;
	}

	public RuleActionDetail getRuleActionDetail() {
		return ruleActionDetail;
	}

	public void setRuleActionDetail(RuleActionDetail ruleActionDetail) {
		this.ruleActionDetail = ruleActionDetail;
	}

	public String getFieldValues() {
		return fieldValues;
	}

	public void setFieldValues(String fieldValues) {
		this.fieldValues = fieldValues;
	}
	
	/** Define methods for UI */
	
	public boolean isHasDataTypeValues() {
		return (ruleActionDetail!=null && ruleActionDetail.getRuleDataType()!=null);
	}
	public boolean isDataTypeEmailAddress() {
		return (RuleDataTypeEnum.EMAIL_ADDRESS.name().equals(getCurrentDataType()));
	}
	public String[] getFieldValuesUI() {
		if (StringUtils.isBlank(getFieldValues())) return new String[0];
		StringTokenizer st = new StringTokenizer(getFieldValues(), ",");
		String[] tokens = new String[st.countTokens()];
		int i=0;
		while (st.hasMoreTokens()) {
			tokens[i++] = st.nextToken();
		}
		return tokens;
	}
	public void setFieldValuesUI(String[] values) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; values != null && i < values.length; i++) {
			if (i == 0) {
				sb.append(values[i]);
			}
			else {
				sb.append("," + values[i]);
			}
		}
		if (sb.length() > 0) {
			setFieldValues(sb.toString());
		}
		else {
			setFieldValues(null);
		}
	}

	public List<String> getRuleDataValueList() {
		List<RuleDataValue> list = getRuleDataValueService().getByDataType(getCurrentDataType());
		List<String> values = new ArrayList<String>();
		for (int i = 0; i < list.size(); i++) {
			String dataValue = list.get(i).getRuleDataValuePK().getDataValue();
			values.add(dataValue);
		}
		return values;
	}
	
	public String getSenderId() {
		return (getRuleActionPK().getSenderData()==null?null:getRuleActionPK().getSenderData().getSenderId());
	}
	
	public void setSenderId(String senderId) {
		if (StringUtils.isNotBlank(senderId)) {
			if (getRuleActionPK().getSenderData()!=null) {
				if (!senderId.equals(getRuleActionPK().getSenderData().getSenderId())) {
					SenderData sender = getSenderDataService().getBySenderId(senderId);
					getRuleActionPK().setSenderData(sender);
				}
			}
			else {
				SenderData sender = getSenderDataService().getBySenderId(senderId);
				getRuleActionPK().setSenderData(sender);
			}
		}
		else {
			if (getRuleActionPK().getSenderData()!=null) {
				getRuleActionPK().setSenderData(null);
			}
		}
	}

	private String getCurrentDataType() {
		if (ruleActionDetail == null) return null;
		RuleDataType vo = ruleActionDetail.getRuleDataType();
		if (vo != null) {
			return vo.getDataType();
		}
		else {
			return null;
		}
	}
	
	/*
	 * define ajax listener for ruleActionBuiltinEdit.xhtml
	 */
	public void changedActionId(AjaxBehaviorEvent event) {
		logger.info("changeActionId() - Action Id: " + getRuleActionDetail().getActionId());
		UISelectOne select = (UISelectOne) event.getSource();
        if (select.getValue() == null || select.getValue().toString().isEmpty()) {
            logger.info("Selected value is blank");
            return;
        }
        String actionId = select.getValue().toString();
        logger.info("ActionID selected: " + actionId);
    	RuleActionDetail detail = getRuleActionDetailService().getByActionId(actionId);
    	if (detail == null) {
    		logger.error("ActionDetail not found by ActionId (" + actionId + ").");
    	}
    	else {
    		setRuleActionDetail(detail);
    	}
	}
	
	@Transient
	private RuleDataValueService dataValueService;

	private RuleDataValueService getRuleDataValueService() {
		if (dataValueService == null) {
			dataValueService = SpringUtil.getAppContext().getBean(RuleDataValueService.class);
		}
		return dataValueService;
	}
	
	@Transient
	private SenderDataService senderDataService;
	
	private SenderDataService getSenderDataService() {
		if (senderDataService == null) {
			senderDataService = SpringUtil.getAppContext().getBean(SenderDataService.class);
		}
		return senderDataService;
	}
	
	@Transient
	private RuleActionDetailService actionDetailService;
	
	private RuleActionDetailService getRuleActionDetailService() {
		if (actionDetailService == null) {
			actionDetailService = SpringUtil.getAppContext().getBean(RuleActionDetailService.class);
		}
		return actionDetailService;
	}
	/** End of UI */
	
}
