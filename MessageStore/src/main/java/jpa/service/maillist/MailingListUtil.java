package jpa.service.maillist;

import java.util.HashMap;
import java.util.Map;

import jpa.constant.CodeType;
import jpa.constant.VariableName;
import jpa.constant.VariableType;
import jpa.model.MailingList;
import jpa.variable.RenderVariableVo;

public final class MailingListUtil {

	private MailingListUtil() {
		// static only
	}
	
	public static Map<String, RenderVariableVo> renderListVariables(MailingList listVo,
			String subscriberAddress, long subscriberAddressId) {
		Map<String, RenderVariableVo> variables = new HashMap<String, RenderVariableVo>();
		String varName = null;
		RenderVariableVo var = null;
		
		varName = VariableName.LIST_VARIABLE_NAME.MailingListId.name();
		var = new RenderVariableVo(
				varName,
				listVo.getListId(),
				null,
				VariableType.TEXT,
				CodeType.YES_CODE.getValue(),
				Boolean.FALSE);
		variables.put(varName, var);
		
		varName = VariableName.LIST_VARIABLE_NAME.MailingListName.toString();
		var = new RenderVariableVo(
				varName,
				listVo.getDisplayName(),
				null,
				VariableType.TEXT,
				CodeType.YES_CODE.getValue(),
				Boolean.FALSE);
		variables.put(varName, var);
		
		varName = VariableName.LIST_VARIABLE_NAME.MailingListAddress.toString();
		var = new RenderVariableVo(
				varName,
				listVo.getListEmailAddr(),
				null,
				VariableType.TEXT,
				CodeType.YES_CODE.getValue(),
				Boolean.FALSE);
		variables.put(varName, var);
		
		varName = VariableName.LIST_VARIABLE_NAME.SubscriberAddress.toString();
		var = new RenderVariableVo(
				varName,
				subscriberAddress,
				null,
				VariableType.TEXT,
				CodeType.YES_CODE.getValue(),
				Boolean.FALSE);
		variables.put(varName, var);
		
		varName = VariableName.LIST_VARIABLE_NAME.SubscriberAddressId.toString();
		var = new RenderVariableVo(
				varName,
				String.valueOf(subscriberAddressId),
				null,
				VariableType.TEXT,
				CodeType.YES_CODE.getValue(),
				Boolean.FALSE);
		variables.put(varName, var);
		
		return variables;
	}
}
