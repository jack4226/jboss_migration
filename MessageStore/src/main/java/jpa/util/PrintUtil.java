package jpa.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PrintUtil {

	public static String prettyPrint(Object obj) {
		return prettyPrint(obj, 1); // default to 1 level
	}

	public static String prettyPrintRecursive(Object obj) {
		return prettyPrint(obj, 10); // default to maximum 10 levels
	}

	public static String prettyPrint(Object obj, int levels) {
		if (obj == null) {
			return ("-null");
		}
		Stack<Object> stack = new Stack<Object>();
		String pkgName = getPkgName(obj);
		return prettyPrint(obj, stack, 0, pkgName, levels);
	}
	
	static String prettyPrint(Object obj, Stack<Object> stack, int level, String pkgName, int levels) {
		if (obj == null) {
		   return ("-null");
		}
		boolean sortByMethodName = true;
		if (level > StringUtil.MAX_LEVELS) {
			System.err.println("StringUtil.prettyPrint - has reached nested level of " + level + ", exit.");
			return (obj.getClass().getCanonicalName() + " - (more)...");
		}
		if (level > 0) {
			for (Enumeration<Object> enu=stack.elements(); enu.hasMoreElements(); ) {
				Object sobj = enu.nextElement();
				if (obj.getClass().getCanonicalName().equals(sobj.getClass().getCanonicalName())) {
					return (obj.getClass().getCanonicalName() + " - (loop)...");
				}
			}
		}
		if (level >= levels) {
			return "";
		}
		if (isPrimitiveClass(obj)) {
			return obj.getClass().getSimpleName() + ": " + obj.toString();
		}
		stack.push(obj);
		Object [] params = {};
		StringBuffer sb = new StringBuffer();
		Map<String, Method> methodMap = new LinkedHashMap<>();
		Method[] methods = null;
		if (obj.getClass().isEnum()) {
			methods = obj.getClass().getDeclaredMethods();
			sb.append(StringUtil.LF + " ");
			sb.append("     " + dots(level) + obj.getClass().getSimpleName() + "=" + obj.toString());
		}
		else {
			methods = obj.getClass().getMethods();
		}
		
		// sort the attributes by name
		for (int i = 0; i< methods.length; i++) {
			if (methods[i].getParameterTypes().length == 0) {
				methodMap.put(methods[i].getName(), methods[i]);
			}
		}
		
		List<String> methodNamelist = new ArrayList<>(methodMap.keySet());
		
		if (sortByMethodName) {
			Collections.sort(methodNamelist);
		}
		
		if (methodNamelist.size()>0) {
			sb.append(StringUtil.LF + " ");
		}
		for (int i = 0; i < methodNamelist.size(); i++) {
			String methodName = methodNamelist.get(i);
			Method method = methodMap.get(methodName);
			String shortClassName = obj.getClass().getSimpleName();
		
			//System.err.println(method.getReturnType().getName() + " - " + methodName);
			if ((methodName.length() > 3) && ((methodName.startsWith("get")))) {
				try {
					sb.append(StringUtil.LF + " ");
					sb.append("     " + dots(level) + shortClassName + "." + methodName.substring(3, 4).toLowerCase() + methodName.substring(4));
					sb.append("=");
					if ((method.getReturnType().equals(Class.forName("java.lang.String")))
							|| (method.getReturnType().equals(Class.forName("java.lang.Integer")))
							|| (method.getReturnType().equals(Class.forName("java.lang.Long")))
							|| (method.getReturnType().equals(Class.forName("java.lang.Short")))
							|| (method.getReturnType().equals(Class.forName("java.lang.Float")))
							|| (method.getReturnType().equals(Class.forName("java.lang.Double")))
							|| (method.getReturnType().equals(Class.forName("java.lang.Boolean")))
							|| (method.getReturnType().equals(java.lang.Integer.TYPE))
							|| (method.getReturnType().equals(java.lang.Character.TYPE))) {
						Object rtnObj = null;
						try {
							rtnObj = method.invoke(obj, params);
						}
						catch (Exception e) {
							rtnObj = "Exception caught: " + e.getMessage();
						}
						if (rtnObj == null) {
							sb.append("null");
						}
						else {
							sb.append((rtnObj).toString().trim());
						}
					}
					else if ("int".equals(method.getReturnType().getName())
							|| "short".equals(method.getReturnType().getName())
							|| "long".equals(method.getReturnType().getName())
							|| "double".equals(method.getReturnType().getName())
							|| "boolean".equals(method.getReturnType().getName())) {
						Object rtnObj = method.invoke(obj, params);
						if (rtnObj == null) {
							sb.append("null");
						}
						else {
							sb.append((rtnObj).toString().trim());
						}
					}
					else if (method.getReturnType().equals(Class.forName("java.util.Date"))
							|| method.getReturnType().equals(Class.forName("java.sql.Date"))) {
						Object rtnObj = method.invoke(obj, params);
						if (rtnObj == null) {
							sb.append("null");
						}
						else {
							sb.append(StringUtil.sdf.format(((java.util.Date) rtnObj)));
						}
					}
					else if (method.getReturnType().equals(Class.forName("java.util.Calendar"))) {
						Object rtnObj = method.invoke(obj, params);
						if (rtnObj == null) {
							sb.append("null");
						}
						else {
							sb.append(StringUtil.sdf.format(((java.util.Calendar)rtnObj).getTime()));
						}						
					}
					else if (method.getReturnType().equals(Class.forName("java.sql.Timestamp"))) {
						Object rtnObj = method.invoke(obj, params);
						if (rtnObj == null) {
							sb.append("null");
						}
						else {
							java.sql.Timestamp tms = (java.sql.Timestamp) rtnObj;
							sb.append(StringUtil.sdf.format(tms) + ", Nanos: " + tms.getNanos());
						}						
					}
					else if (method.getReturnType().equals(Class.forName("java.math.BigDecimal"))) {
						Object rtnObj = method.invoke(obj, params);
						if (rtnObj == null) {
							sb.append("null");
						}
						else {
							sb.append(((java.math.BigDecimal) rtnObj).floatValue());
						}
					}
					else if (method.getReturnType().equals(Class.forName("java.math.BigInteger"))) {
						Object rtnObj = method.invoke(obj, params);
						if (rtnObj == null) {
							sb.append("null");
						}
						else {
							sb.append(((java.math.BigInteger) rtnObj).longValue());
						}
					}
					else if (method.getReturnType().equals(Class.forName("java.lang.Class"))) {
						Object rtnObj = method.invoke(obj, params);
						if (rtnObj != null) {
							if (rtnObj.getClass().getName().startsWith(pkgName)) {
								sb.append(prettyPrint(rtnObj, stack, level + 1, pkgName, levels));
							}
							else {
								sb.append(((Class<?>) rtnObj).getName());
							}
						}
					}
					else if (method.getReturnType().equals(Class.forName("java.util.ArrayList"))
							|| method.getReturnType().equals(Class.forName("java.util.List"))) {
						sb.append("a List");
						List<?> lst = (List<?>) method.invoke(obj, params);
						if (lst != null) {
							try {
								for (Iterator<?> it = lst.iterator(); it.hasNext();) {
									Object _obj = it.next();
									if (_obj == null) {
										continue;
									}
									if (_obj.getClass().getName().startsWith(pkgName)) {
										sb.append(prettyPrint(_obj, stack, level + 1, pkgName, levels));
									}
									else {
										if (_obj instanceof java.lang.String) {
											sb.append(StringUtil.LF + " ");
											sb.append("     " + dots(level+1) + _obj.getClass().getCanonicalName());
											sb.append("=" + _obj.toString());
										}
									}
								}
							}
							catch (org.hibernate.LazyInitializationException e) {
								sb.append(" - LazyInitException caught: " + e.getMessage());
							}
							catch (Exception e) {
								sb.append(" - Exception caught: " + e.getMessage());
							}
						}
					}
					else if (method.getReturnType().equals(Class.forName("java.util.Set"))) {
						sb.append("a Set");
						Set<?> set = (Set<?>) method.invoke(obj, params);
						if (set != null) {
							try {
								for (Iterator<?> it = set.iterator(); it.hasNext();) {
									Object _obj = it.next();
									if (_obj == null) {
										continue;
									}
									if (_obj instanceof java.lang.String) {
										sb.append(StringUtil.LF + " ");
										sb.append("     " + dots(level+1) + _obj.getClass().getCanonicalName());
										sb.append("=" + _obj.toString());
									}
									else if (_obj.getClass().isEnum()) { // TODO - Skip Enum type, must revisit
										continue;
									}
									else {
										if (_obj.getClass().getName().startsWith(pkgName)) {
											sb.append(prettyPrint(_obj, stack, level + 1, pkgName, levels));
										}
									}
								}
							}
							catch (Exception e) {
								sb.append(" - Exception caught: " + e.getMessage());
							}
						}
					}
					else if (method.getReturnType().equals(Class.forName("java.util.Map"))) {
						sb.append("a Map");
						Map<?, ?> map = (Map<?, ?>) method.invoke(obj, params);
						if (map != null) {
							try {
								for (Iterator<?> it = map.keySet().iterator(); it.hasNext();) {
									Object _key = it.next();
									if (_key == null) {
										continue;
									}
									Object _obj = map.get(_key);
									if (_obj != null) {
										if (_obj instanceof java.lang.String) {
											sb.append(StringUtil.LF + " ");
											sb.append("     " + dots(level+1) + _key.toString());
											sb.append("=" + _obj.toString());
										}
										else {
											if (_obj.getClass().getName().startsWith(pkgName)) {
												sb.append(prettyPrint(_obj, stack, level + 1, pkgName, levels));
											}
										}
									}
								}
							}
							catch (Exception e) {
								sb.append(" - Exception caught: " + e.getMessage());
							}
						}
					}
					else if (method.getReturnType()==byte[].class) {
						Object rtnObj = method.invoke(obj, params);
						if (rtnObj!=null) {
							sb.append(new String((byte[])rtnObj));
						}
						else {
							sb.append("null");
						}
					}
					else {
						Class<?> cls = method.getReturnType();
						if (cls.getName().startsWith("[L")) {
							String nm = cls.getComponentType().getCanonicalName();
							if (nm.startsWith(pkgName)) {
								try {
									Object[] objs = (Object[])method.invoke(obj, params);
									for (int j=0; objs!=null && j<objs.length; j++) {
										sb.append(prettyPrint(objs[j], stack, level+1, pkgName, levels));
									}
								}
								catch (Exception e) {
									sb.append(" - Exception caught: " + e.getMessage());
								}
							}
						}
						else if (cls.isEnum()) {
							try {
								Object _obj = method.invoke(obj, params);
								if (_obj != null) {
									if (_obj.getClass().getCanonicalName().startsWith(pkgName)) {
										sb.append(prettyPrint(_obj, stack, level + 1, pkgName, levels));
									}
									else {
										sb.append(StringUtil.LF + " ");
										sb.append("     " + dots(level+1) + _obj.getClass().getCanonicalName() + "=" + _obj.toString());
									}
								}
							}
							catch (Exception e) {
								sb.append(" - Exception caught: " + e.getMessage());
							}
						}
						else {
							String nm = cls.getCanonicalName();
							if (nm.startsWith(pkgName)) {
								try {
									Object rtn_obj = method.invoke(obj, params);
									sb.append(prettyPrint(rtn_obj, stack, level + 1, pkgName, levels));
								}
								catch (Exception e) {
									sb.append(" - Exception caught: " + e.getMessage());
								}
							}
						}
					}
					//sb.append(LF + " ");
				}
				catch (Exception e) {
					StringUtil.logger.error("Exception caught", e);
					System.err.println("error getting values in toString, method name: " + methodName + ", " + e.getMessage());
				}
			}
			else if ((methodName.length() > 2) && ((methodName.startsWith("is")))) {
				try {
					sb.append(StringUtil.LF + " ");
					sb.append("     " + dots(level) + shortClassName + "." + methodName);
					sb.append("=");
					if (method.getReturnType().isAssignableFrom(java.lang.Boolean.class)
							|| method.getReturnType().getName().equals("boolean")) {
						Object rtnObj = method.invoke(obj, params);
						if (rtnObj == null) {
							sb.append("null");
						}
						else {
							sb.append((rtnObj).toString().trim());
						}
					}
				}
				catch (Exception e) {
					StringUtil.logger.error("Exception caught", e);
					//logger.error("Exception caught", e);
					System.err.println("error getting values in toString, method name: " + methodName + ", " + e.getMessage());
				}
			}
		}
		stack.pop();
		return sb.toString();
	}
	
	private static String getPkgName(Object obj) {
		Pattern pkgPattern = Pattern.compile("(\\w{1,20}\\.\\w{1,20})\\..*");
		String pkgName = obj.getClass().getPackage().getName();
		Matcher m = pkgPattern.matcher(pkgName);
		if (m.matches() && m.groupCount()>=1) {
			pkgName = m.group(1);
		}
		return pkgName;
	}
	
	private static boolean isPrimitiveClass(Object obj) {
		if (obj instanceof Integer || obj instanceof Long || obj instanceof Short
				|| obj instanceof Double || obj instanceof Float || obj instanceof Boolean
				|| obj instanceof java.util.Date || obj instanceof java.sql.Date
				|| obj instanceof java.util.Calendar || obj instanceof java.sql.Timestamp
				|| obj instanceof java.math.BigInteger || obj instanceof java.math.BigDecimal) {
			return true;
		}
		return false;
	}

	private static String dots(int level) {
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<level; i++) {
			sb.append(".");
		}
		return sb.toString();
	}

}
