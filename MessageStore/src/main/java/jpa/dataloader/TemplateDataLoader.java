package jpa.dataloader;

import java.sql.Timestamp;
import java.util.List;

import jpa.constant.CarrierCode;
import jpa.constant.CodeType;
import jpa.constant.Constants;
import jpa.constant.VariableName;
import jpa.constant.VariableType;
import jpa.model.SenderData;
import jpa.model.msg.MessageSource;
import jpa.model.msg.TemplateData;
import jpa.model.msg.TemplateDataPK;
import jpa.model.msg.TemplateVariable;
import jpa.model.msg.TemplateVariablePK;
import jpa.model.EmailAddress;
import jpa.service.common.EmailAddressService;
import jpa.service.common.SenderDataService;
import jpa.service.msgdata.MessageSourceService;
import jpa.service.msgdata.TemplateDataService;
import jpa.service.msgdata.TemplateVariableService;
import jpa.spring.util.SpringUtil;
import jpa.util.JpaUtil;

import org.apache.log4j.Logger;

public class TemplateDataLoader extends AbstractDataLoader {
	static final Logger logger = Logger.getLogger(TemplateDataLoader.class);
	private SenderDataService senderService;
	private TemplateDataService templateService;
	private TemplateVariableService variableService;
	private MessageSourceService sourceService;
	private EmailAddressService addrService;

	public static void main(String[] args) {
		TemplateDataLoader loader = new TemplateDataLoader();
		loader.loadData();
	}

	@Override
	public void loadData() {
		senderService = SpringUtil.getAppContext().getBean(SenderDataService.class);
		templateService = SpringUtil.getAppContext().getBean(TemplateDataService.class);
		variableService = SpringUtil.getAppContext().getBean(TemplateVariableService.class);
		sourceService = SpringUtil.getAppContext().getBean(MessageSourceService.class);
		addrService = SpringUtil.getAppContext().getBean(EmailAddressService.class);
		startTransaction();
		try {
			loadTemplateData();
		} catch (Exception e) {
			logger.error("Exception caught", e);
		}
		finally {
			commitTransaction();
		}
	}

	private void loadTemplateData() {
		SenderData cd = senderService.getBySenderId(Constants.DEFAULT_SENDER_ID);
		Timestamp tms =  new Timestamp(System.currentTimeMillis());
		TemplateData data = new TemplateData();
		TemplateDataPK tpk = new TemplateDataPK(cd, "WeekendDeals", tms);

		data.setTemplateDataPK(tpk);
		data.setContentType("text/plain");
		data.setBodyTemplate("Dear subscriber, here is a list of great deals on gardening tools provided to you by mydot.com.\n" +
				"Available by ${CurrentDate}. Sponsor (${SenderId}).");
		data.setSubjectTemplate("Weekend Deals at MyBestDeals.com - ${CurrentDate}");
		templateService.insert(data);

		TemplateData tmp2 = templateService.getByPrimaryKey(tpk);
		String variableId = "WeekendDeals";

		TemplateVariable var1 = new TemplateVariable();
		TemplateVariablePK vpk1 = new TemplateVariablePK(cd, variableId, "CurrentDateTime", tms);
		var1.setTemplateVariablePK(vpk1);
		var1.setVariableType(VariableType.DATETIME.getValue());
		var1.setAllowOverride(CodeType.YES_CODE.getValue());
		var1.setRequired(false);
		variableService.insert(var1);
		
		TemplateVariable var2 = new TemplateVariable();
		TemplateVariablePK vpk2 = new TemplateVariablePK(cd, variableId, "CurrentDate", tms);
		var2.setTemplateVariablePK(vpk2);
		var2.setVariableFormat("yyyy-MM-dd");
		var2.setVariableType(VariableType.DATETIME.getValue());
		var2.setAllowOverride(CodeType.YES_CODE.getValue());
		var2.setRequired(false);
		variableService.insert(var2);
		
		TemplateVariable var3 = new TemplateVariable();
		TemplateVariablePK vpk3 = new TemplateVariablePK(cd, variableId, VariableName.SUBSCRIBER_ID.getValue(), tms);
		var3.setTemplateVariablePK(vpk3);
		var3.setVariableType(VariableType.TEXT.getValue());
		var3.setAllowOverride(CodeType.YES_CODE.getValue());
		var3.setRequired(true);
		variableService.insert(var3);
		
		EmailAddress adr1 = addrService.findSertAddress("jsmith@test.com");
		MessageSource src1 = new MessageSource();
		src1.setMsgSourceId("WeekendDeals");
		src1.setDescription("Default Message Source");
		if (JpaUtil.isMySQLDatabase() && JpaUtil.isEclipseLink()) {
			src1.setTemplateData(data);
		}
		else {
			src1.setTemplateData(tmp2);
		}
		src1.getTemplateVariableList().add(var1);
		src1.getTemplateVariableList().add(var2);
		src1.getTemplateVariableList().add(var3);
		src1.setFromAddress(adr1);
		src1.setExcludingIdToken(false);
		src1.setCarrierCode(CarrierCode.SMTPMAIL.getValue());
		src1.setAllowOverride(CodeType.YES_CODE.getValue());
		src1.setSaveMsgStream(true);
		src1.setArchiveMsg(false);
		sourceService.insert(src1);

		// test template
		TemplateData tmp3 = new TemplateData();
		TemplateDataPK tpk3 = new TemplateDataPK(cd, "testTemplate", tms);

		tmp3.setTemplateDataPK(tpk3);
		tmp3.setContentType("text/html");
		tmp3.setBodyTemplate("BeginTemplate\n"
				+ "Current DateTime: ${CurrentDate}<br>\n"
				+ "${name1}${name2} Some Text ${name3}More Text<br>\n"
				+ "${TABLE_SECTION_BEGIN}TableRowBegin &lt;${name2}&gt; TableRowEnd<br>\n" 
				+ "${TABLE_SECTION_END}text<br>\n"
				+ "${OPTIONAL_SECTION_BEGIN}Level 1-1 ${name1}<br>\n"
				+ "${OPTIONAL_SECTION_BEGIN}Level 2-1<br>\n${OPTIONAL_SECTION_END}<br>\n"
				+ "${OPTIONAL_SECTION_BEGIN}Level 2-2${dropped}<br>\n${OPTIONAL_SECTION_END}"
				+ "${OPTIONAL_SECTION_BEGIN}Level 2-3${name2}<br>\n${OPTIONAL_SECTION_END}"
				+ "${OPTIONAL_SECTION_END}"
				+ "${OPTIONAL_SECTION_BEGIN}Level 1-2<br>\n${OPTIONAL_SECTION_END}"
				+ "${name4}<br>\n"
				+ "EndTemplate<br>\n");
		tmp3.setSubjectTemplate("Test Template");
		templateService.insert(tmp3);

		List<TemplateVariable> vars = variableService.getByVariableId(variableId);
		src1 = new MessageSource();
		src1.setMsgSourceId("testMsgSource");
		src1.setDescription("Message Source");
		src1.setTemplateData(tmp3);
		for (TemplateVariable var : vars) {
			src1.getTemplateVariableList().add(var);
		}
		src1.setFromAddress(adr1);
		src1.setExcludingIdToken(false);
		src1.setCarrierCode(CarrierCode.SMTPMAIL.getValue());
		src1.setAllowOverride(CodeType.YES_CODE.getValue());
		src1.setSaveMsgStream(true);
		src1.setArchiveMsg(false);
		sourceService.insert(src1);

		logger.info("EntityManager persisted the record.");
	}
	
}

