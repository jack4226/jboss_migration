package jpa.data.preload;

import jpa.constant.CodeType;
import jpa.constant.Constants;
import jpa.constant.VariableType;

public enum GlobalVariableEnum {
	CurrentDateTime(null,"yyyy-MM-dd HH:mm:ss",VariableType.DATETIME,CodeType.YES_CODE),
	CurrentDate(null,"yyyy-MM-dd",VariableType.DATETIME,CodeType.YES_CODE),
	CurrentTime(null,"hh:mm:ss a",VariableType.DATETIME,CodeType.YES_CODE),
	PoweredBySignature(Constants.POWERED_BY_HTML_TAG,null,VariableType.TEXT,CodeType.NO_CODE),
	To(null,null,VariableType.ADDRESS,CodeType.MANDATORY_CODE);
	
	private String value;
	private String format;
	private VariableType type;
	private CodeType allowOverride;
	private GlobalVariableEnum(String value, String format, VariableType type, CodeType allowOverride) {
		this.value=value;
		this.format=format;
		this.type=type;
		this.allowOverride=allowOverride;
	}

	public String getDefaultValue() {
		return value;
	}
	public String getVariableFormat() {
		return format;
	}
	public VariableType getVariableType() {
		return type;
	}
	public CodeType getAllowOverride() {
		return allowOverride;
	}
}
