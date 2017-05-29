package jpa.util;

import java.lang.reflect.Method;

import javax.validation.constraints.NotNull;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.beanutils.converters.SqlDateConverter;
import org.apache.commons.beanutils.converters.SqlTimeConverter;
import org.apache.commons.beanutils.converters.SqlTimestampConverter;
import org.apache.log4j.Logger;

import jpa.model.BaseModel;
import jpa.model.SenderData;

public class BeanCopyUtil {
	static final Logger logger = Logger.getLogger(BeanCopyUtil.class);
	
	private static DateConverter dateConverter = new DateConverter(null);
	private static SqlTimestampConverter timestampConverter = new SqlTimestampConverter(null);
	private static SqlDateConverter sqlDateConverter = new SqlDateConverter(null);
	private static SqlTimeConverter sqlTimeonverter = new SqlTimeConverter(null);

	public static void registerBeanUtilsConverters() {
		// setup for BeanUtils.copyProperties() to handle null value
		ConvertUtils.register(dateConverter, java.util.Date.class);
		ConvertUtils.register(timestampConverter, java.sql.Timestamp.class);
		ConvertUtils.register(sqlDateConverter, java.sql.Date.class);
		ConvertUtils.register(sqlTimeonverter, java.sql.Time.class);
	}

	public static void copyProperties(@NotNull BaseModel dest, @NotNull BaseModel orig) {
		Object [] params = {};
		Method[] methods = orig.getClass().getMethods();
		for (Method method : methods) {
			String methodName = method.getName();
			try {
				if ((method.getParameterCount() == 0) && (methodName.length() > 3) && ((methodName.startsWith("get")))) {
					if ((method.getReturnType().equals(Class.forName("java.lang.String")))
							|| (method.getReturnType().equals(Class.forName("java.lang.Integer")))
							|| (method.getReturnType().equals(Class.forName("java.lang.Long")))
							|| (method.getReturnType().equals(Class.forName("java.lang.Short")))
							|| (method.getReturnType().equals(Class.forName("java.lang.Float")))
							|| (method.getReturnType().equals(Class.forName("java.lang.Double")))
							|| (method.getReturnType().equals(Class.forName("java.lang.Boolean")))
							|| (method.getReturnType().equals(java.lang.Integer.TYPE))
							|| (method.getReturnType().equals(java.lang.Character.TYPE))
							|| method.getReturnType().equals(Class.forName("java.util.Date"))
							|| method.getReturnType().equals(Class.forName("java.sql.Date"))
							|| method.getReturnType().equals(Class.forName("java.util.Calendar"))
							|| method.getReturnType().equals(Class.forName("java.sql.Timestamp"))
							|| method.getReturnType().equals(Class.forName("java.math.BigDecimal"))
							|| method.getReturnType().equals(Class.forName("java.math.BigInteger"))
							|| "int".equals(method.getReturnType().getName())
							|| "short".equals(method.getReturnType().getName())
							|| "long".equals(method.getReturnType().getName())
							|| "double".equals(method.getReturnType().getName())
							|| "boolean".equals(method.getReturnType().getName())
							) {
						try {
							Object rtnObj = method.invoke(orig, params);
							String setMethodName = method.getName().replaceFirst("get", "set");
							try {
								Class<?>[] setParms = { method.getReturnType() };
								Method setMethod = dest.getClass().getMethod(setMethodName, setParms);
								Object[] objParms = { rtnObj };
								setMethod.invoke(dest, objParms);
							}
							catch (Exception e) {
								logger.error("Exception caught - setter not found, ignore. " + e.getMessage());
								// no corresponding set method, ignore.
							}
						}
						catch (Exception e) {
							logger.error("Exception caught: ", e);
						}
					}
				}
				else if ((method.getParameterCount() == 0) && (methodName.length() > 2) && ((methodName.startsWith("is")))) {
					if ((method.getReturnType().equals(Class.forName("java.lang.Boolean")))
							|| (method.getReturnType().equals(java.lang.Boolean.TYPE))
							|| "boolean".equals(method.getReturnType().getName())
							) {
						try {
							Object rtnObj = method.invoke(orig, params);
							String setMethodName = method.getName().replaceFirst("is", "set");
							try {
								Class<?>[] setParms = { method.getReturnType() };
								Method setMethod = dest.getClass().getMethod(setMethodName, setParms);
								Object[] objParms = { rtnObj };
								setMethod.invoke(dest, objParms);
							}
							catch (Exception e) {
								logger.error("Exception caught - setter not found, ignore. " + e.getMessage());
								// no corresponding set method, ignore.
							}
						} 
						catch (Exception e) {
							logger.error("Exception caught: ", e);
						}
					}
				}
			}
			catch (Exception e) {
				logger.error("Exception caught: ", e);
			}
		}
	}
	
	public static void main(String[] args) {
		try {
			SenderData orig = new SenderData();
			orig.setChaRspHndlrEmail("ChaRspHndlrEmail@localhost");
			orig.setContactName("contact name");
			orig.setContactPhone("888-888-8888");
			orig.setDomainName("domain.name");
			orig.setEditable(true);
			orig.setEmbedEmailId(true);
			orig.setIrsTaxId("irsTaxId");
			orig.setIsDikm(null);
			orig.setIsDomainKey(Boolean.TRUE);
			orig.setIsSpf(Boolean.FALSE);
			orig.setKeyFilePath("keyFilePath");
			orig.setMarkedForDeletion(true);
			orig.setMarkedForEdition(true);
			orig.setOrigSenderId("origSenderId");
			orig.setReturnPathLeft("returnPathLeft");
			orig.setRmaDeptEmail("rmaDeptEmail@localhost");
			orig.setSaveRawMsg(true);
			orig.setSecurityEmail("securityEmail@localhost");
			orig.setSenderId("senderId");
			orig.setSenderName("senderName");
			orig.setSenderType("senderType");
			orig.setSpamCntrlEmail("spamCntrlEmail@localhost");
			orig.setStatusId("A");
			orig.setSubrCareEmail("subrCareEmail@localhost");
			orig.setSystemId("systemId");
			orig.setSystemKey("systemKey");
			orig.setTestFromAddr("testFromAddr");
			orig.setTestReplytoAddr("testReplytoAddr");
			orig.setTestToAddr("testToAddr");
			orig.setUpdtTime(new java.sql.Timestamp(System.currentTimeMillis()));
			orig.setUpdtUserId("updtUserId");
			orig.setVerpEnabled(false);
			orig.setWebSiteUrl("webSiteUrl.com");
			orig.setUseTestAddr(true);
			
			SenderData dest = new SenderData();
			copyProperties(dest, orig);
			
			logger.info(PrintUtil.prettyPrint(dest));
		}
		catch (Exception e) {
			logger.error("Exception", e);
		}
		System.exit(0);
	}
}
