package jpa.variable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import jpa.constant.CodeType;
import jpa.constant.VariableType;
import jpa.data.preload.GlobalVariableEnum;

public class RenderVariableVo implements Serializable {
	private static final long serialVersionUID = -1784984311808865823L;
	public final static String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	final static String LF = System.getProperty("line.separator", "\n");

	private final String variableName;
	private Object variableValue;
	// Data Type: T - Text, N - Numeric, D - Date time.
	private final VariableType variableType;
	// Data Format, used by Numeric and Date time data types.
	private final String variableFormat;

	private final String allowOverride;
	private final Boolean isRequired;

	/**
	 * Define a text variable.
	 * @param variableName - name
	 * @param variableValue - value
	 */
	public RenderVariableVo(String variableName, String variableValue) {
		this(variableName, variableValue, VariableType.TEXT);
	}

	/**
	 * Define a variable with specified type and default format.
	 * <ul>Variable Type vs. Variable Value:
	 * <li>Text - String</li>
	 * <li>Numeric - String or BigDecimal</li>
	 * <li>DateTime - String or java.util.Date</li>
	 * </ul>
	 * @param variableName - name
	 * @param variableValue - value
	 * @param variableType - type
	 */
	public RenderVariableVo(String variableName, Object variableValue, VariableType variableType) {
		this(variableName, variableValue, variableType, null);
	}

	/**
	 * Define a variable with specified type and format.
	 * <ul>Variable Type vs. Variable Value:
	 * <li>Text - String</li>
	 * <li>Numeric - String or BigDecimal</li>
	 * <li>DateTime - String or java.util.Date</li>
	 * </ul>
	 * @param variableName - name
	 * @param variableValue - value
	 * @param variableType - type
	 * @param variableFormat - ignored for Text type.
	 */
	public RenderVariableVo(String variableName, Object variableValue, VariableType variableType,
			String variableFormat) {
		this(variableName, variableValue, variableFormat, variableType, CodeType.YES_CODE.getValue(), Boolean.FALSE);
	}

	public RenderVariableVo(
			String variableName,
			Object variableValue,
			String variableFormat,
			VariableType variableType,
			String allowOverride,
			Boolean isRequired) {

		this.variableName = variableName;
		this.variableValue = variableValue;
		this.variableType = variableType;
		this.variableFormat = variableFormat;
		this.allowOverride = allowOverride;
		this.isRequired = isRequired;

		if (VariableType.NUMERIC.equals(variableType)) {
			if (variableFormat != null) {
				new DecimalFormat(variableFormat); // validate the format
			}
			if (variableValue != null) {
				if (!(variableValue instanceof BigDecimal) && !(variableValue instanceof String)) {
					throw new IllegalArgumentException("Invalid Value Type: "
							+ variableValue.getClass().getName() + ", for " + variableName);
				}
				if (variableValue instanceof String) {
					NumberFormat numberFormat = NumberFormat.getNumberInstance();
					try {
						numberFormat.parse((String) variableValue);
					}
					catch (ParseException e) {
						throw new IllegalArgumentException("Invalid Numeric Value: " + variableValue
								+ ", for " + variableName);
					}
				}
			}
		}
		else if (VariableType.DATETIME.equals(variableType)) {
			SimpleDateFormat fmt = new SimpleDateFormat(DEFAULT_DATETIME_FORMAT);
			if (variableFormat != null) {
				fmt.applyPattern(variableFormat); // validate the format
			}
			if (variableValue == null) { // populate "CurrentDateTime"
				if (GlobalVariableEnum.CurrentDateTime.name().equals(variableName)
						|| GlobalVariableEnum.CurrentDate.name().equals(variableName)
						|| GlobalVariableEnum.CurrentTime.name().equals(variableName)) {
					this.variableValue = variableValue = new Date();
				}
			}
			if (variableValue != null) {
				if (!(variableValue instanceof Date) && !(variableValue instanceof String)) {
					throw new IllegalArgumentException("Invalid Value Type: "
							+ variableValue.getClass().getName() + ", for " + variableName);
				}
				if (variableValue instanceof String) {
					try {
						fmt.parse((String) variableValue);
					}
					catch (ParseException e) {
						throw new IllegalArgumentException("Invalid DateTime Value: " + variableValue
								+ ", for " + variableName);
					}
				}
			}
		}
		else if (VariableType.ADDRESS.equals(variableType)) {
			if (variableValue!=null) {
				if (!(variableValue instanceof Address) && !(variableValue instanceof String)) {
					throw new IllegalArgumentException("Invalid Value Type: "
						+ variableValue.getClass().getName() + ", by " + variableName);
				}
				if (variableValue instanceof String) {
					try {
						InternetAddress.parse((String)variableValue);
					}
					catch (AddressException e) {
						throw new IllegalArgumentException("Invalid Email Address: "
								+ variableValue + ", by " + variableName);
					}
				}
			}
		}
		else if (VariableType.TEXT.equals(variableType) || VariableType.X_HEADER.equals(variableType)) {
			if (variableValue != null) {
				if (!(variableValue instanceof String)) {
					throw new IllegalArgumentException("Invalid Value Type: "
							+ variableValue.getClass().getName() + ", for " + variableName);
				}
			}
		}
		else if (VariableType.LOB.equals(variableType)) {
			if (variableValue!=null) {
				if (!(variableValue instanceof String) && !(variableValue instanceof byte[])) {
					throw new IllegalArgumentException("Invalid Value Type: "
						+ variableValue.getClass().getName() + ", by " + variableName);
				}
			}
			if (variableFormat==null) {
				throw new IllegalArgumentException(
						"VariableFormat must be provided for LOB variable, by " + variableName);
			}
		}
		else if (VariableType.COLLECTION.equals(variableType)) {
			if (variableValue!=null) {
				if (!(variableValue instanceof Collection)) {
					throw new IllegalArgumentException("Invalid Value Type: "
						+ variableValue.getClass().getName() + ", by " + variableName);
				}
			}
		}
		else {
			throw new IllegalArgumentException("Invalid Variable Type: " + variableType + ", for "
					+ variableName);
		}
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("========== RenderVariableVo Fields ==========" + LF);
		sb.append("VariableName:   " + variableName + LF);
		sb.append("VariableValue:  " + (variableValue == null ? "null" : variableValue) + LF);
		sb.append("VariableType:   " + variableType.getValue() + LF);
		sb.append("VariableFormat: " + variableFormat + LF);
		return sb.toString();
	}

	public VariableType getVariableType() {
		return variableType;
	}

	public Object getVariableValue() {
		return variableValue;
	}

	public String getVariableFormat() {
		return variableFormat;
	}

	public String getVariableName() {
		return variableName;
	}

	public String getAllowOverride() {
		return allowOverride;
	}

	public Boolean getIsRequired() {
		return isRequired;
	}
}
