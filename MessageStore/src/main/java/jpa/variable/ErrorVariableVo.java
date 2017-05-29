package jpa.variable;

import java.io.Serializable;

public class ErrorVariableVo implements Serializable {
	private static final long serialVersionUID = 7902451918468414337L;

	final static String LF = System.getProperty("line.separator", "\n");

	private final String variableName;
	private final Object variableInfo;
	private String errorMsg;

	public ErrorVariableVo(String variableName, Object variableInfo, String errorMsg) {
		this.variableName = variableName;
		this.variableInfo = variableInfo;
		this.errorMsg = errorMsg;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("========== ErrorVariableVo Fields ==========" + LF);
		sb.append("VariableName:   " + variableName + LF);
		sb.append("VariableInfo:   " + variableInfo + LF);
		sb.append("ErrorMsg:       " + errorMsg + LF);
		return sb.toString();
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public String getVariableName() {
		return variableName;
	}

	public Object getVariableInfo() {
		return variableInfo;
	}
}
