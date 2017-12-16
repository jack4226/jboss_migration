package jpa.msgui.vo;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.johnzon.mapper.JohnzonConverter;
import org.apache.log4j.Logger;

import jpa.constant.StatusId;

@XmlAccessorType (XmlAccessType.NONE)
@XmlRootElement (name = "baseVo")
public class BaseVo implements java.io.Serializable, Cloneable {
	private static final long serialVersionUID = 2438423000525545863L;
	static Logger logger = Logger.getLogger(BaseVo.class);
	
	final static String LF = System.getProperty("line.separator", "\n");
	@XmlElement
	protected int rowId = -1;
	@XmlElement
	protected String primaryKey = null;
	@XmlElement
	private String statusId = StatusId.ACTIVE.getValue();
	@XmlElement
    @XmlJavaTypeAdapter(TimestampAdapter.class)
	@JohnzonConverter(TimestampConverter.class)
	protected Timestamp updtTime = null;
    @XmlElement
	protected String updtUserId = null;
    @XmlElement
	@XmlJavaTypeAdapter(TimestampAdapter.class)
    @JohnzonConverter(TimestampConverter.class)
	private Timestamp origUpdtTime = null;

    @XmlTransient
	private List<ChangeLog> changeLogs = null;

	/**
	 * Define fields and methods used by UI components
	 */
	@XmlTransient
	protected boolean editable = true;
	@XmlTransient
	protected boolean markedForDeletion = false;
	@XmlTransient
	protected boolean markedForEdition = false;
	
	public BaseVo() {
		rowId = -1;
		primaryKey = null;	
		statusId = StatusId.ACTIVE.getValue();
		updtTime = null;
		updtUserId = null;
		
		origUpdtTime = null;
		changeLogs = null;

		editable = true;
		markedForDeletion = false;
		markedForEdition = false;
	}

	public boolean isEditable() {
		return editable;
	}
	public void setEditable(boolean editable) {
		this.editable = editable;
	}
	public boolean isMarkedForDeletion() {
		return markedForDeletion;
	}
	public void setMarkedForDeletion(boolean markedForDeletion) {
		this.markedForDeletion = markedForDeletion;
	}
	public boolean isMarkedForEdition() {
		return markedForEdition;
	}
	public void setMarkedForEdition(boolean markedForEdition) {
		this.markedForEdition = markedForEdition;
	}
	
	public String getStatusIdDesc() {
		try {
			Method method = this.getClass().getMethod("getStatusId", (Class[])null);
			String desc = (String) method.invoke(this, (Object[])null);
			if (StatusId.ACTIVE.getValue().equals(desc)) {
				desc = "Active";
			}
			else if (StatusId.INACTIVE.getValue().equals(desc)) {
				desc = "Inactive";
			}
			else if (StatusId.SUSPENDED.getValue().equals(desc)) {
				desc = "Suspended";
			}
			return desc;
		}
		catch (Exception e) {
			logger.warn("Exception caught: " + e.getMessage());
			// ignored
			return null;
		}
	}
	/** end of UI components */
	
	public boolean equalsTo(BaseVo vo) {
		getLogList().clear();
		if (this == vo) {
			return true;
		}
		if (vo == null) {
			return false;
		}
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
					logger.error("in equalsTo(): NoSuchMethodException caught: " + e.getMessage());
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
							|| (returnTypeName.endsWith("PageAction"))) {
						Object thisValue = method.invoke((Object)this, (Object[])params);
						Object voValue = voMethod.invoke((Object)vo, (Object[])params);
						if (thisValue == null) {
							if (voValue != null) {
								addChangeLog(methodName.substring(3), thisValue, voValue);
							}
						}
						else {
							if (!thisValue.equals(voValue)) {
								addChangeLog(methodName.substring(3), thisValue, voValue);
							}
						}
					}
				}
				catch (Exception e) {
					logger.error("in equalsTo(): Exception caught: " + e.getMessage());
					return false;
				}
			}
		}
		if (getLogList().size() > 0) {
			return false;
		}
		else {
			return true;
		}
	}
	
	public String toString() {
		return toString(this);
	}

	public String toString(Object vo) {
		return toString(vo, 0);
	}

	String toString(int level) {
		return toString(this, level);
	}

	String toString(Object vo, int level) {
		if (vo == null) {
			return ("-null");
		}
		Object[] params = {};
		StringBuffer sb = new StringBuffer();
		HashMap<String, Method> methodMap = new HashMap<String, Method>();
		ArrayList<String> methodList = new ArrayList<String>();
		Method methods[] = vo.getClass().getMethods();

		// sort the attributes by name
		for (int i = 0; i < methods.length; i++) {
			methodMap.put(methods[i].getName(), methods[i]);
			methodList.add(methods[i].getName());
		}
		Collections.sort(methodList);

		for (int i = 0; i < methodList.size(); i++) {
			String methodName = (String) methodList.get(i);
			Method method = (Method) methodMap.get(methodName);
			params = method.getParameterTypes();
			String paramClassName = vo.getClass().getName();
			paramClassName = paramClassName.substring(paramClassName.lastIndexOf(".") + 1);

			if (methodName.startsWith("get") && params.length == 0) {
				try {
					sb.append("     " + dots(level) + paramClassName + "."
							+ methodName.substring(3, 4).toLowerCase() + methodName.substring(4));
					sb.append("=");
					Class<?> returnType = method.getReturnType();
					String returnTypeName = returnType.getName();
					if ((returnTypeName.endsWith("java.lang.String"))
							|| (returnTypeName.endsWith("java.lang.Integer"))
							|| (returnTypeName.endsWith("java.lang.Long"))
							|| (returnTypeName.endsWith("java.sql.Timestamp"))
							|| (returnTypeName.endsWith("java.sql.Date"))
							|| (returnType.equals(java.lang.Integer.TYPE))
							|| (returnType.equals(java.lang.Long.TYPE))
							|| (returnType.equals(java.lang.Character.TYPE))) {
						if (method.invoke(vo, params) == null) {
							sb.append("null");
						}
						else {
							sb.append((method.invoke(vo, params)).toString().trim());
						}
					}
					else if (returnTypeName.endsWith("java.lang.Class")) {
						sb.append(((Class<?>) method.invoke(vo, params)).getName());
					}
					else if (method.getName().indexOf("Vo") > 0) {
						if (method.invoke(vo, params) == null) {
							sb.append("null");
						}
						else {
							sb.append(LF);
							Object subVo = method.invoke(vo, params);
							// the call to vo.toString lets objects override
							// this default toString method....
							if (subVo != null) {
								if (subVo instanceof BaseVo) {
									sb.append(((BaseVo) subVo).toString(level + 1));
								}
								else {
									sb.append(subVo.toString());
								}
							}
							else {
								sb.append(toString(subVo));
							}
						}
					}
					else {
						Object subVo = method.invoke(vo, params);
						if (subVo != null) {
							if (!(subVo instanceof BaseVo)) {
								sb.append(subVo.toString());
							}
							else if (subVo instanceof java.util.List<?>) {
								sb.append(((java.util.List<?>)subVo).size());
							}
						}
						else {
							sb.append("null");
						}
					}
					sb.append(LF);
				}
				catch (Exception e) {
					logger.error("Exception caught: error getting values in toString " + methods[i].getName()
							+ " " + e.getMessage());
				}
			}
		}
		return sb.toString();
	}

	private String dots(int level) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < level; i++) {
			sb.append(".");
		}
		return sb.toString();
	}
	
	public Object getClone() throws CloneNotSupportedException {
		return this.clone();
	}
	
	public List<ChangeLog> getLogList() {
		if (changeLogs == null) {
			changeLogs = new ArrayList<ChangeLog>();
		}
		return changeLogs;
	}
	
	protected void addChangeLog(String fieldName, Object left, Object right) {
		getLogList().add(new ChangeLog(fieldName, left, right));
	}
	
	public String listChanges() {
		final String LF = System.getProperty("line.separator","\n");
		StringBuffer sb = new StringBuffer();
		for (ChangeLog item : getLogList()) {
			sb.append(item.printf() + LF);
		}
		return sb.toString();
	}
	
	private static class ChangeLog implements Serializable {
		private static final long serialVersionUID = -5732262969319705777L;
		String fieldName;
		Object leftValue;
		Object rightValue;
		ChangeLog(String fieldName, Object leftValue, Object rightValue) {
			this.fieldName = fieldName;
			this.leftValue = leftValue;
			this.rightValue = rightValue;
		}
		
		String printf() {
			String str = String.format("%14s", fieldName) + " : ";
			if (leftValue instanceof Long) {
				str += String.format("%14d", leftValue) + "  <->  ";
			}
			else {
				str += String.format("%14s", leftValue) + "  <->  ";
			}
			if (rightValue instanceof Long) {
				str += String.format("%-14d", rightValue);
			}
			else {
				str += String.format("%-14s", rightValue);
			}
			return str;
		}
	}
	
	public int getRowId() {
		return rowId;
	}
	public void setRowId(int rowId) {
		this.rowId = rowId;
	}
	public String getPrimaryKey() {
		return primaryKey;
	}
	public void setPrimaryKey(String primaryKey) {
		this.primaryKey = primaryKey;
	}
	public final String getStatusId() {
		return statusId;
	}
	public void setStatusId(String status) {
		this.statusId = status;
	}
	public Timestamp getUpdtTime() {
		return updtTime;
	}
	public void setUpdtTime(Timestamp updtTime) {
		this.updtTime = updtTime;
		StackTraceElement[] traces = new Throwable().getStackTrace();
		for (int i = 0; i < traces.length; i++) {
			String className = traces[i].getClassName();
			String methodName = traces[i].getMethodName();
			if (className.endsWith("Mapper") && methodName.equals("mapRow")) {
				setOrigUpdtTime(updtTime);
				break;
			}
			else if (i > 3) {
				break;
			}
		}
	}
	public String getUpdtUserId() {
		return updtUserId;
	}
	public void setUpdtUserId(String updtUserId) {
		this.updtUserId = updtUserId;
	}
	public Timestamp getOrigUpdtTime() {
		return origUpdtTime;
	}
	public void setOrigUpdtTime(Timestamp origUpdtTime) {
		this.origUpdtTime = origUpdtTime;
	}
}
