package jpa.model;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

public final class SchedulesBlob implements Serializable {
	private static final long serialVersionUID = 4238845382852251582L;
	static final Logger logger = Logger.getLogger(SchedulesBlob.class);

	public static enum Events {
		WEEKLY, BIWEEKLY, MONTHLY, END_OF_MONTH, EOM_MINUS_1DAY, EOM_MINUS_2DAY, DATE_LIST
	};
	private int startHour;
	private int startMinute;
	private String[] weekly;
	private String[] biweekly;
	private String[] monthly;
	private boolean endOfMonth;
	private boolean eomMinus1Day;
	private boolean eomMinus2Day;
	private final DateWrapper[] dateList;
	
	private Calendar calendar;
	private Events timerEvent;
	private String templateId;
	
	public SchedulesBlob() {
		dateList = new DateWrapper[10];
		reset();
	}
	
	public final void reset() {
		startHour = 2;
		startMinute = 0; // 2:00 AM
		endOfMonth = false;
		eomMinus1Day = false;
		eomMinus2Day = false;
		weekly = null;
		biweekly = null;
		monthly = null;
		calendar = Calendar.getInstance();
		timerEvent = null;
		templateId = null;
		for (int i = 0; i < dateList.length; i++) {
			dateList[i] = new DateWrapper();
		}
	}
	
	public static class DateWrapper implements Serializable {
		private static final long serialVersionUID = 8808716896334260740L;
		private Date date = null;
		public Date getDate() {
			return date;
		}
		public void setDate(Date date) {
			this.date = date;
		}
		public String toString() {
			return (date == null ? null : date.toString());
		}
	}
	
	public int getStartHour() {
		return startHour;
	}
	public void setStartHour(int startHour) {
		this.startHour = startHour;
	}
	public int getStartMinute() {
		return startMinute;
	}
	public void setStartMinute(int startMinute) {
		this.startMinute = startMinute;
	}
	public String[] getWeekly() {
		return weekly;
	}
	public String[] getBiweekly() {
		return biweekly;
	}
	public String[] getMonthly() {
		return monthly;
	}
	public boolean getEndOfMonth() {
		return endOfMonth;
	}
	public void setEndOfMonth(boolean endOfMonth) {
		this.endOfMonth = endOfMonth;
	}
	public boolean getEomMinus1Day() {
		return eomMinus1Day;
	}
	public void setEomMinus1Day(boolean eomMinus1Day) {
		this.eomMinus1Day = eomMinus1Day;
	}
	public boolean getEomMinus2Day() {
		return eomMinus2Day;
	}
	public void setEomMinus2Day(boolean eomMinus2Day) {
		this.eomMinus2Day = eomMinus2Day;
	}
	public DateWrapper[] getDateList() {
		return dateList;
	}
	public void setWeekly(String[] weekly) {
		this.weekly = weekly;
	}
	public void setBiweekly(String[] biweekly) {
		this.biweekly = biweekly;
	}
	public void setMonthly(String[] monthly) {
		this.monthly = monthly;
	}
	public Calendar getCalendar() {
		return calendar;
	}
	public void setCalendar(Calendar calendar) {
		this.calendar = calendar;
	}
	public Events getTimerEvent() {
		return timerEvent;
	}
	public void setTimerEvent(Events timerEvent) {
		this.timerEvent = timerEvent;
	}
	public String getTemplateId() {
		return templateId;
	}
	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}
	
	final String LF = System.getProperty("line.separator", "\n");
	final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
	public String toString() {
		return toString(this, 0);
	}

	private String toString(Object vo, int level) {
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
			String paramClassName = vo.getClass().getName();
			paramClassName = paramClassName.substring(paramClassName.lastIndexOf(".") + 1);

			if ((methodName.length() > 3) && (methodName.substring(0, 3).equals("get"))) {
				try {
					sb.append("     " + dots(level) + paramClassName + "."
							+ methodName.substring(3, 4).toLowerCase() + methodName.substring(4));
					sb.append("=");
					if ((method.getReturnType().equals(Class.forName("java.lang.String")))
							|| (method.getReturnType().equals(Class.forName("java.lang.Integer")))
							|| (method.getReturnType().equals(Class.forName("java.lang.Long")))
							|| (method.getReturnType().equals(Class.forName("java.sql.Timestamp")))
							|| (method.getReturnType().equals(Class.forName("java.sql.Date")))
							|| (method.getReturnType().equals(java.lang.Integer.TYPE))
							|| (method.getReturnType().equals(java.lang.Long.TYPE))
							|| (method.getReturnType().equals(java.lang.Boolean.TYPE))
							|| (method.getReturnType().equals(java.lang.Character.TYPE))) {

						if (method.invoke(vo, params) == null) {
							sb.append("null");
						}
						else {
							sb.append((method.invoke(vo, params)).toString().trim());
						}
					}
					else if (method.getReturnType().isArray()) {
						Object[] array = (Object[]) method.invoke(vo, params);
						if (array == null) {
							sb.append("null");
						}
						else {
							sb.append("[");
							for (int j = 0; j < array.length; j++) {
								if (j > 0)
									sb.append(", ");
								sb.append(array[j]);
							}
							sb.append("]");
						}
					}
					else if (method.getReturnType().equals(Class.forName("java.util.Calendar"))) {
						Object obj = method.invoke(vo, params);
						if (obj == null) {
							sb.append("null");
						}
						else {
							sb.append(((Calendar)obj).getTime());
						}
					}
					else if (method.getReturnType().equals(Class.forName("java.util.List"))) {
						List<?> list = (List<?>) method.invoke(vo, params);
						if (list == null) {
							sb.append("null");
						}
						else {
							sb.append("[");
							for (int j = 0; j < list.size(); j++) {
								Date date = (Date) list.get(j);
								if (j > 0)
									sb.append(", ");
								sb.append(sdf.format(date));
							}
							sb.append("]");
						}
					}
					else if (method.getReturnType().equals(Class.forName("java.lang.Class"))) {
						sb.append((((Class<?>) method.invoke(vo, params))).getName());
					}
					else {
						Object subVo = method.invoke(vo, params);
						if (subVo != null) {
							sb.append(subVo.toString());
						}
						else {
							sb.append("null");
						}
					}
					sb.append(LF);
				}
				catch (Exception e) {
					String errMsg = "error getting values in toString " + methods[i].getName() + " " + e.getMessage();
					System.err.println(errMsg);
					logger.error(errMsg);
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
	
	public static void main(String[] args) {
		try {
			SchedulesBlob blob = new SchedulesBlob();
			logger.info(blob.toString());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
