package jpa.msgui.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.faces.model.SelectItem;

import jpa.constant.CarrierCode;
import jpa.constant.CodeType;
import jpa.constant.Constants;
import jpa.constant.EmailVariableType;
import jpa.constant.MailProtocol;
import jpa.constant.MailServerType;
import jpa.constant.MailingListDeliveryType;
import jpa.constant.MailingListType;
import jpa.constant.MobileCarrierEnum;
import jpa.constant.MsgStatusCode;
import jpa.constant.RuleCategory;
import jpa.constant.RuleCriteria;
import jpa.constant.RuleType;
import jpa.constant.StatusId;
import jpa.constant.VariableName;
import jpa.data.preload.FolderEnum;
import jpa.message.MessageBeanUtil;
import jpa.service.external.TargetTextProc;
import jpa.service.external.VariableResolver;

@javax.inject.Named("codes")
@javax.enterprise.context.ApplicationScoped
public class StaticCodes implements java.io.Serializable {
	private static final long serialVersionUID = 7420577459263024360L;

	public static void main(String[] args) {
		StaticCodes codes = new StaticCodes();
		
		SelectItem[] items = codes.getTargetProcItems();
		for (SelectItem item : items) {
			System.out.println(item.getValue() + ", " + item.getLabel());
		}
		items = variableResolverItems();
		for (SelectItem item : items) {
			System.out.println(item.getValue() + ", " + item.getLabel());
		}
	}
	
	// PROPERTY: Target procedure class name items
	public SelectItem[] getTargetProcItems() {
		return targetProcItems();
	}

	/*
	 * Not working yet.
	 */
	public static SelectItem[] targetProcItems() {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Enumeration<URL> urls = null;
		try {
			urls = loader.getResources("jpa/service/external");
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		List<SelectItem> list = new ArrayList<SelectItem>();
		if (urls != null && urls.hasMoreElements()) {
			URL url = urls.nextElement();
			if (url != null) {
				File dir = new File(url.getFile());
				if (dir != null && dir.isDirectory()) {
					String[] fileNames = dir.list();
					for (String fileName : fileNames) {
						String pkgName = "jpa.service.external";
						String className = fileName.replaceAll("\\.class", "");
						String fullName = pkgName + "." + className;
						try {
							Class<?> cls = Class.forName(fullName);
							if (!cls.isInterface()) {
								try {
									Object obj = cls.getDeclaredConstructor().newInstance();
									if (obj instanceof TargetTextProc) {
										list.add(new SelectItem(fullName, className));
									}
								}
								catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
						catch (ClassNotFoundException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		else { // load jar file
			URL url = loader.getResource("WEB-INF/lib");
			if (url != null) {
				File file = new File(url.getFile());
				try {
					JarFile jarFile = new JarFile(file);
					Enumeration<JarEntry> entries = jarFile.entries();
					while (entries.hasMoreElements()) {
						JarEntry entry = entries.nextElement();
						System.out.println("entry: " + entry);
					}
					jarFile.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return list.toArray(new SelectItem[0]);
	}
	
	// PROPERTY: Target procedure class name items
	public SelectItem[] getVariableResolverItems() {
		return variableResolverItems();
	}

	public static SelectItem[] variableResolverItems() {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Enumeration<URL> urls = null;
		try {
			urls = loader.getResources("jpa/service/external");
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		List<SelectItem> list = new ArrayList<SelectItem>();
		if (urls != null && urls.hasMoreElements()) {
			URL url = urls.nextElement();
			if (url != null) {
				File dir = new File(url.getFile());
				if (dir != null && dir.isDirectory()) {
					String[] fileNames = dir.list();
					for (String fileName : fileNames) {
						String pkgName = "jpa.service.external";
						String className = fileName.replaceAll("\\.class", "");
						String fullName = pkgName + "." + className;
						try {
							Class<?> cls = Class.forName(fullName);
							if (!cls.isInterface()) {
								try {
									Object obj = cls.getDeclaredConstructor().newInstance();
									if (obj instanceof VariableResolver) {
										list.add(new SelectItem(fullName, className));
									}
								}
								catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
						catch (ClassNotFoundException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		return list.toArray(new SelectItem[0]);
	}
	
	// PROPERTY: software version
	public boolean isEnterpriseVersion() {
		return false; // TODO: get from property file
	}
	
	public String getPoweredByHtmlTag() {
		return Constants.POWERED_BY_HTML_TAG;
	}
	
	private static Map<String, String> stateCodeMap;
	static {
		stateCodeMap = new LinkedHashMap<String, String>();
		stateCodeMap.put("AL", "Alabama");
		stateCodeMap.put("AK", "Alaska");
		stateCodeMap.put("AS", "American Samoa");
		stateCodeMap.put("AZ", "Arizona");
		stateCodeMap.put("AR", "Arkansas");
		stateCodeMap.put("CA", "California");
		stateCodeMap.put("CO", "Colorado");
		stateCodeMap.put("CT", "Connecticut");
		stateCodeMap.put("DE", "Delaware");
		stateCodeMap.put("DC", "District Of Columbia");
		stateCodeMap.put("FM", "Federated States Of Micronesia");
		stateCodeMap.put("FL", "Florida");
		stateCodeMap.put("GA", "Georgia");
		stateCodeMap.put("GU", "Guam");
		stateCodeMap.put("HI", "Hawaii");
		stateCodeMap.put("ID", "Idaho");
		stateCodeMap.put("IL", "Illinois");
		stateCodeMap.put("IN", "Indiana");
		stateCodeMap.put("IA", "Iowa");
		stateCodeMap.put("KS", "Kansas");
		stateCodeMap.put("KY", "Kentucky");
		stateCodeMap.put("LA", "Louisiana");
		stateCodeMap.put("ME", "Maine");
		stateCodeMap.put("MH", "Marshall Islands");
		stateCodeMap.put("MD", "Maryland");
		stateCodeMap.put("MA", "Massachusetts");
		stateCodeMap.put("MI", "Michigan");
		stateCodeMap.put("MN", "Minnesota");
		stateCodeMap.put("MS", "Mississippi");
		stateCodeMap.put("MO", "Missouri");
		stateCodeMap.put("MT", "Montana");
		stateCodeMap.put("NE", "Nebraska");
		stateCodeMap.put("NV", "Nevada");
		stateCodeMap.put("NH", "New Hampshire");
		stateCodeMap.put("NJ", "New Jersey");
		stateCodeMap.put("NM", "New Mexico");
		stateCodeMap.put("NY", "New York");
		stateCodeMap.put("NC", "North Carolina");
		stateCodeMap.put("ND", "North Dakota");
		stateCodeMap.put("MP", "Northern Mariana Islands");
		stateCodeMap.put("OH", "Ohio");
		stateCodeMap.put("OK", "Oklahoma");
		stateCodeMap.put("OR", "Oregon");
		stateCodeMap.put("PW", "Palau");
		stateCodeMap.put("PA", "Pennsylvania");
		stateCodeMap.put("PR", "Puerto Rico");
		stateCodeMap.put("RI", "Rhode Island");
		stateCodeMap.put("SC", "South Carolina");
		stateCodeMap.put("SD", "South Dakota");
		stateCodeMap.put("TN", "Tennessee");
		stateCodeMap.put("TX", "Texas");
		stateCodeMap.put("UT", "Utah");
		stateCodeMap.put("VT", "Vermont");
		stateCodeMap.put("VI", "Virgin Islands");
		stateCodeMap.put("VA", "Virginia");
		stateCodeMap.put("WA", "Washington");
		stateCodeMap.put("WV", "West Virginia");
		stateCodeMap.put("WI", "Wisconsin");
		stateCodeMap.put("WY", "Wyoming");
		};
	
	public Map<String, String> getStateCodeMap() {
		return stateCodeMap;
	}
	
	// PROPERTY: stateCodeItems
	public SelectItem[] getStateCodeItems() {
		return stateCodeItems;
	}

	// PROPERTY: stateCodeWithAbbrItems
	public SelectItem[] getStateCodeWithAbbrItems() {
		return stateCodeWithAbbrItems;
	}
	
	// PROPERTY: Yes/No Items
	public SelectItem[] getYesNoItems() {
		return yesNoItems;
	}

	private static SelectItem[] yesNoItems = {
		new SelectItem(true, CodeType.YES.getValue()),
		new SelectItem(false, CodeType.NO.getValue())
	};
	
	// PROPERTY: Y/N Items
	public SelectItem[] getYorNItems() {
		return yorNItems;
	}

	private static SelectItem[] yorNItems = {
		new SelectItem(true, "Y"),
		new SelectItem(false, "N")
	};
	
	// PROPERTY: true/false Items
	public SelectItem[] getTrueOrFalseItems() {
		return trueOrFalseItems;
	}

	private static SelectItem[] trueOrFalseItems = {
		new SelectItem(true, "True"),
		new SelectItem(false, "False")
	};
	
	// PROPERTY: Yes/No Enum Items 
	public SelectItem[] getYesNoEnumItems() {
		return yesNoEnumItems;
	}

	private static SelectItem[] yesNoEnumItems = {
		new SelectItem(CodeType.YES, "Yes"),
		new SelectItem(CodeType.NO, "No")
	};

	// PROPERTY: Mail Protocol Items
	public SelectItem[] getMailProtocolItems() {
		return mailProtocolItems;
	}
	
	private static SelectItem[] mailProtocolItems = {
		new SelectItem(MailProtocol.POP3.value(), "POP3"),
		new SelectItem(MailProtocol.IMAP.value(), "IMAP")
	};
	
	// PROPERTY: Mail Carrier Code Items
	public SelectItem[] getMailCarrierCodeItems() {
		return mailCarrierCodeItems;
	}
	
	private static SelectItem[] mailCarrierCodeItems = {
		new SelectItem(CarrierCode.SMTPMAIL.getValue(), "SMTP Mail")
		//,new SelectItem(Constants.WEBMAIL_CODE, "Web Mail")
		//,new SelectItem(Constants.READONLY_CODE, "Leave on server")
	};

	// PROPERTY: Simple StatudId Items
	public SelectItem[] getSimpleStatusIdItems() {
		return simpleStatusIdItems;
	}
	
	// PROPERTY: Mailbox StatudId Items
	public SelectItem[] getMailboxStatusIdItems() {
		return simpleStatusIdItems;
	}
	
	private static SelectItem[] simpleStatusIdItems = {
		new SelectItem(StatusId.ACTIVE.getValue(), "Active"),
		new SelectItem(StatusId.INACTIVE.getValue(), "Inactive")
	};

	// PROPERTY: Email Variable Type Items
	public SelectItem[] getEmailVariableTypeItems() {
		return emailVariableTypeItems;
	}
	
	private static SelectItem[] emailVariableTypeItems = {
		new SelectItem(EmailVariableType.System.getValue(), "System"),
		new SelectItem(EmailVariableType.Custom.getValue(), "Customer")
	};

	// PROPERTY: E-Mail Address StatudId Items
	public SelectItem[] getMailAddressStatusIdItems() {
		return mailAddressStatusIdItems;
	}
	
	private static SelectItem[] mailAddressStatusIdItems = {
		new SelectItem(StatusId.ACTIVE.getValue(), "Active"),
		new SelectItem(StatusId.INACTIVE.getValue(), "Inactive"),
		new SelectItem(StatusId.SUSPENDED.getValue(), "Suspended")
	};

	// PROPERTY: Message out-box StatudId Items
	public SelectItem[] getOutboxStatusIdItems() {
		return outboxStatusIdItems;
	}
	
	private static SelectItem[] outboxStatusIdItems = {
		new SelectItem(MsgStatusCode.PENDING.getValue(), "Pending"),
		new SelectItem(MsgStatusCode.DELIVERED.getValue(), "Delivered"),
		new SelectItem(MsgStatusCode.DELIVERY_FAILED.getValue(), "Failed")
	};

	// PROPERTY: SMTP Server Type Items
	public SelectItem[] getSmtpServerTypeItems() {
		return smtpServerTypeItems;
	}
	
	private static SelectItem[] smtpServerTypeItems = {
		new SelectItem(MailServerType.SMTP.value(), "SMTP"),
		new SelectItem(MailServerType.EXCHANGE.value(), "Exchange")
	};

	// PROPERTY: SMTP Server Type Items
	public SelectItem[] getAlertLevelItems() {
		return alertLevelItems;
	}
	
	private static SelectItem[] alertLevelItems = {
		new SelectItem("infor", "Informational"),
		new SelectItem("error", "Normal Error"),
		new SelectItem("fatal", "Fatal Error"),
		new SelectItem("nolog", "Disable Alert")
	};

	// PROPERTY: Message Rule Type Items
	public SelectItem[] getRuleTypeItems() {
		return ruleTypeItems;
	}
	
	private static SelectItem[] ruleTypeItems = {
		new SelectItem(RuleType.SIMPLE.getValue()),
		new SelectItem(RuleType.ALL.getValue()),
		new SelectItem(RuleType.ANY.getValue()),
		new SelectItem(RuleType.NONE.getValue())
	};

	// PROPERTY: Mail Type Items
	public SelectItem[] getMailTypeItems() {
		return mailTypeItems;
	}
	
	private static SelectItem[] mailTypeItems = {
		new SelectItem(Constants.SMTP_MAIL, "SMTP Mail"),
		new SelectItem(Constants.WEB_MAIL, "Web Mail")
	};

	// PROPERTY: Mailing List Type Items
	public SelectItem[] getMailingListTypeItems() {
		return mailingListTypeItems;
	}
	
	private static SelectItem[] mailingListTypeItems = {
		new SelectItem(MailingListType.TRADITIONAL.getValue()),
		new SelectItem(MailingListType.PERSONALIZED.getValue())
	};

	// PROPERTY: Mailing List Delivery Option Items
	public SelectItem[] getMailingListDeliveryOptionItems() {
		return mailingListDeliveryOptionItems;
	}
	
	private static SelectItem[] mailingListDeliveryOptionItems = {
		new SelectItem(MailingListDeliveryType.ALL_ON_LIST.getValue()),
		new SelectItem(MailingListDeliveryType.SUBSCRIBERS_ONLY.getValue()),
		new SelectItem(MailingListDeliveryType.PROSPECTS_ONLY.getValue())
	};

	// PROPERTY: Days of Week Items
	public SelectItem[] getDaysOfTheWeekItems() {
		return daysOfTheWeekItems;
	}
	private static SelectItem[] daysOfTheWeekItems;
	static {
		DateFormatSymbols symbols = new DateFormatSymbols();
		String[] weekdays = symbols.getWeekdays();
		daysOfTheWeekItems = new SelectItem[7];
		for (int i = Calendar.SUNDAY; i <= Calendar.SATURDAY; i++) {
			daysOfTheWeekItems[i - 1] = new SelectItem(Integer.valueOf(i), weekdays[i]);
		}
	}

	// PROPERTY: Days of Month Items
	public SelectItem[] getDaysOfMonthItems() {
		return daysOfMonthItems;
	}
	private static SelectItem[] daysOfMonthItems;
	static {
		String[] weekdays = {
				"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
				"11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
				"21", "22", "23", "24", "25", "26", "27", "28", "29", "30"
				};
		daysOfMonthItems = new SelectItem[30];
		for (int i = 0; i < 30; i++) {
			daysOfMonthItems[i] = new SelectItem(Integer.valueOf(i + 1), weekdays[i]);
		}
	}

	// PROPERTY: Rule Order Items
	public SelectItem[] getRuleCategoryItems() {
		return ruleCategoryItems;
	}
	
	private static SelectItem[] ruleCategoryItems = {
		new SelectItem(RuleCategory.PRE_RULE.getValue(), "Pre Scan"),
		new SelectItem(RuleCategory.MAIN_RULE.getValue(), "Main Rule"),
		new SelectItem(RuleCategory.POST_RULE.getValue(), "Post Scan")
	};

	// PROPERTY: Rule Criteria Items
	public SelectItem[] getRuleCriteriaItems() {
		return ruleCriteriaItems;
	}
	
	private static SelectItem[] ruleCriteriaItems = {
		new SelectItem(RuleCriteria.STARTS_WITH.getValue(), "Starts With"),
		new SelectItem(RuleCriteria.ENDS_WITH.getValue(), "Ends With"),
		new SelectItem(RuleCriteria.CONTAINS.getValue(), "Contains"),
		new SelectItem(RuleCriteria.EQUALS.getValue(), "Equals"),
		new SelectItem(RuleCriteria.GREATER_THAN.getValue(), "Greater Than"),
		new SelectItem(RuleCriteria.LESS_THAN.getValue(), "Less Than"),
		new SelectItem(RuleCriteria.IS_NOT_BLANK.getValue(), "Is Not Blank"),
		new SelectItem(RuleCriteria.IS_BLANK.getValue(), "Is Blank"),
		new SelectItem(RuleCriteria.REG_EX.getValue(), "Regular Expression")
	};

	private static SelectItem[] ruleDataNameItems = null;
	// PROPERTY: Rule Data Name Items
	public synchronized SelectItem[] getRuleDataNameItems() {
		if (ruleDataNameItems == null) {
			List<String> names = MessageBeanUtil.getMessageBeanMethodNames();
			names.add(VariableName.XHEADER_DATA_NAME.getValue());
			//names.add(RuleBase.FILE_NAME);
			ruleDataNameItems = new SelectItem[names.size()];
			for (int i=0; i<names.size(); i++) {
				SelectItem item = new SelectItem(names.get(i));
				ruleDataNameItems[i] = item;
			}
		}
		return ruleDataNameItems;
	}
	
	private static SelectItem[] hour24Items = null;
	// PROPERTY: Hour (1 to 24) Items
	public synchronized SelectItem[] getHour24Items() {
		if (hour24Items == null) {
			hour24Items = new SelectItem[24];
			for (int i = 0; i < 24; i++) {
				hour24Items[i] = new SelectItem(getString(i, 2));
			}
		}
		return hour24Items;
	}

	// PROPERTY: Hours (1 to 24) of Day Items
	public SelectItem[] getHoursOfDayItems() {
		return hoursOfDayItems;
	}
	private static SelectItem[] hoursOfDayItems;
	static {
		hoursOfDayItems = new SelectItem[24];
		for (int i = 0; i < 24; i++) {
			hoursOfDayItems[i] = new SelectItem(i);
		}
	}

	private static SelectItem[] hourAmPmItems = null;
	// PROPERTY: Hour (AM/PM) Items
	public synchronized SelectItem[] getHourAmPmItems() {
		if (hourAmPmItems == null) {
			hourAmPmItems = new SelectItem[24];
			for (int i = 0; i < 24; i++) {
				if (i == 0) {
					hourAmPmItems[i] = new SelectItem(i, 12 + " AM");
				}
				else if (i <= 11) {
					hourAmPmItems[i] = new SelectItem(i, i + " AM");
				}
				else if (i == 12) {
					hourAmPmItems[i] = new SelectItem(i, i + " PM");
				}
				else {
					hourAmPmItems[i] = new SelectItem(i, (i-12) + " PM");
				}
			}
		}
		return hourAmPmItems;
	}

	private SelectItem[] minuteItems = null;
	public synchronized SelectItem[] getMinuteItems() {
		if (minuteItems == null) {
			minuteItems = new SelectItem[60];
			for (int i = 0; i < 60; i++) {
				minuteItems[i] = new SelectItem(i);
			}
		}
		return minuteItems;
	}
	
	private SelectItem[] roleItems = null;
	public synchronized SelectItem[] getRoleItems() {
		if (roleItems == null) {
			roleItems = new SelectItem[2];
			roleItems[0] = new SelectItem(Constants.ADMIN_ROLE, "Administrator");
			roleItems[1] = new SelectItem(Constants.USER_ROLE, "User");
		}
		return roleItems;
	}
	
	private SelectItem[] folderTypeItems = null;
	public synchronized SelectItem[] getFolderTypeItems() {
		if (folderTypeItems == null) {
			folderTypeItems = new SelectItem[4];
			folderTypeItems[0] = new SelectItem(FolderEnum.All);
			folderTypeItems[1] = new SelectItem(FolderEnum.Inbox);
			folderTypeItems[2] = new SelectItem(FolderEnum.Sent);
			folderTypeItems[3] = new SelectItem(FolderEnum.Closed);
		}
		return folderTypeItems;
	}
	
	private static String getString(int value, int length) {
		StringBuilder blanks = new StringBuilder();
		for (int i = 0; i < length - ("" + value).length(); i++) {
			blanks.append(" ");
		}
		return blanks.toString() + value;
	}
	
	private static SelectItem[] stateCodeItems;
	static {
		stateCodeItems = new SelectItem[stateCodeMap.size()];
		Set<String> set = stateCodeMap.keySet();
		int i=0;
		for (Iterator<String> it=set.iterator(); it.hasNext(); ) {
			String code = it.next();
			String label = stateCodeMap.get(code);
			stateCodeItems[i++] = new SelectItem(code, label);
		}
	}

	private static SelectItem[] stateCodeWithAbbrItems = {
		new SelectItem("AL", "Al - Alabama"),
		new SelectItem("AK", "AK - Alaska"),
		new SelectItem("AS", "AS - American Samoa"),
		new SelectItem("AZ", "AZ - Arizona"),
		new SelectItem("AR", "AR - Arkansas"),
		new SelectItem("CA", "CA - California"),
		new SelectItem("CO", "CO - Colorado"),
		new SelectItem("CT", "CT - Connecticut"),
		new SelectItem("DE", "DE - Delaware"),
		new SelectItem("DC", "DC - District Of Columbia"),
		new SelectItem("FM", "FM - Federated States Of Micronesia"),
		new SelectItem("FL", "FL - Florida"),
		new SelectItem("GA", "GA - Georgia"),
		new SelectItem("GU", "GU - Guam"),
		new SelectItem("HI", "HI - Hawaii"),
		new SelectItem("ID", "ID - Idaho"),
		new SelectItem("IL", "IL - Illinois"),
		new SelectItem("IN", "IN - Indiana"),
		new SelectItem("IA", "IA - Iowa"),
		new SelectItem("KS", "KS - Kansas"),
		new SelectItem("KY", "KY - Kentucky"),
		new SelectItem("LA", "LA - Louisiana"),
		new SelectItem("ME", "ME - Maine"),
		new SelectItem("MH", "MH - Marshall Islands"),
		new SelectItem("MD", "MD - Maryland"),
		new SelectItem("MA", "MA - Massachusetts"),
		new SelectItem("MI", "MI - Michigan"),
		new SelectItem("MN", "MN - Minnesota"),
		new SelectItem("MS", "MS - Mississippi"),
		new SelectItem("MO", "MO - Missouri"),
		new SelectItem("MT", "MT - Montana"),
		new SelectItem("NE", "NE - Nebraska"),
		new SelectItem("NV", "NV - Nevada"),
		new SelectItem("NH", "NH - New Hampshire"),
		new SelectItem("NJ", "NJ - New Jersey"),
		new SelectItem("NM", "NM - New Mexico"),
		new SelectItem("NY", "NY - New York"),
		new SelectItem("NC", "NC - North Carolina"),
		new SelectItem("ND", "ND - North Dakota"),
		new SelectItem("MP", "MP - Northern Mariana Islands"),
		new SelectItem("OH", "OH - Ohio"),
		new SelectItem("OK", "OK - Oklahoma"),
		new SelectItem("OR", "OR - Oregon"),
		new SelectItem("PW", "PW - Palau"),
		new SelectItem("PA", "PA - Pennsylvania"),
		new SelectItem("PR", "PR - Puerto Rico"),
		new SelectItem("RI", "RI - Rhode Island"),
		new SelectItem("SC", "SC - South Carolina"),
		new SelectItem("SD", "SD - South Dakota"),
		new SelectItem("TN", "TN - Tennessee"),
		new SelectItem("TX", "TX - Texas"),
		new SelectItem("UT", "UT - Utah"),
		new SelectItem("VT", "VT - Vermont"),
		new SelectItem("VI", "VI - Virgin Islands"),
		new SelectItem("VA", "VA - Virginia"),
		new SelectItem("WA", "WA - Washington"),
		new SelectItem("WV", "WV - West Virginia"),
		new SelectItem("WI", "WI - Wisconsin"),
		new SelectItem("WY", "WY - Wyoming")
		};	

	/*
	  Table 1: Secret Questions and Ranges of Answers
	Questions 	Range of Answers
	What is the name of your favorite pet?
	 	The top 20 dog names are Max, Buddy, Molly, Bailey, Maggie, Lucy, Jake, Rocky, Sadie,
	 	Lucky, Daisy, Jack, Sam, Shadow, Bear, Buster, Lady, Ginger, Abby, and Toby.
	In what city were you born?
	 	The top 10 largest U.S. cities are New York City, Los Angeles, Chicago, Houston,
	 	Philadelphia, Phoenix, San Diego, Dallas, San Antonio, and Detroit; one in three of 
	 	all U.S. citizens live in the top 250 cities; the top 10 most common U.S. city names
	 	are Fairview, Midway, Oak Grove, Franklin, Riverside, Centerville, Mount Pleasant,
	 	Georgetown, Salem, and Greenwood.
	What high school did you attend?
	 	There are approximately 25,000 to 30,000 high schools in the United States; you can
	 	use Classmates.com to get a list by U.S. state and city.
	What is your favorite movie?
	 	For a list of the all-time top 250 films, see www.imdb.com/top_250_films.
	What is your mother's maiden name?
	 	There are approximately 25,000 common surnames; one in 10 U.S. citizens have the 
	 	surname Smith, Johnson, Williams, Jones, Brown, Davis, Miller, Wilson, Moore, Taylor,
	 	Anderson, Thomas, Jackson, White, Harris, Martin, Thompson, Garcia, Martinez, Robinson,
	 	Clark, Rodriguez, Lewis, Lee, Walker, Hall, Allen, or Young.
	What street did you grow up on?
	 	The 15 most common street names are Second/2nd, Third/3rd, First/1st, Fourth/4th, Park,
	 	Fifth/5th, Main, Sixth/6th, Oak, Seventh/7th, Pine, Maple, Cedar, Eighth/8th, and Elm.
	What was the make of your first car?
	 	Most cars are built by Acura, Audi, BMW, Buick, Cadillac, Chevrolet, Chrysler, Daewoo,
	 	Dodge, Ford, GMC, Honda, Hummer, Hyundai, Infiniti, Isuzu, Jaguar, Jeep, Kia, Land Rover,
	 	Lexus, Lincoln, Mazda, Mercedes-Benz, Mercury, Mitsubishi, Nissan, Oldsmobile, Plymouth,
	 	 Pontiac, Porsche, Saab, Saturn, Subaru, Suzuki, Toyota, Volkswagen, or Volvo.
	When is your anniversary?
	 	The average length of a marriage is 7.2 years, giving 2,628 likely dates.
	What is your favorite color?
	 	There are around 100 common colors, even considering colors such as taupe, gainsboro,
	 	and fuschia. 
	 */
	private static SelectItem[] securityQuestionItems = {
		new SelectItem("What is the name of your favorite pet?"),
		new SelectItem("In what city were you born?"),
		new SelectItem("What high school did you attend?"),
		new SelectItem("What is your favorite movie?"),
		new SelectItem("What is your mother's maiden name?"),
		new SelectItem("What street did you grow up on?"),
		new SelectItem("What was the make of your first car?"),
		new SelectItem("When is your anniversary?"),
		new SelectItem("What is your favorite color?")
	};
	
	public synchronized SelectItem[] getSecurityQuestionItems() {
		return securityQuestionItems;
	}
	
	private static Map<String, String> countrys; 
	static {
		countrys = new LinkedHashMap<String, String>();
		countrys.put("US","United States");
		countrys.put("AD","Andorra");
		countrys.put("AE","United Arab Emirates");
		countrys.put("AF","Afghanistan");
		countrys.put("AG","Antigua and Barbuda");
		countrys.put("AI","Anguilla");
		countrys.put("AL","Albania");
		countrys.put("AM","Armenia");
		countrys.put("AN","Netherlands Antilles");
		countrys.put("AO","Angola");
		countrys.put("AQ","Antarctica");
		countrys.put("AR","Argentina");
		countrys.put("AS","American Samoa");
		countrys.put("AT","Austria");
		countrys.put("AU","Australia");
		countrys.put("AW","Aruba");
		countrys.put("AX","Aland Islands");
		countrys.put("AZ","Azerbaijan");
		countrys.put("BA","Bosnia and Herzegovina");
		countrys.put("BB","Barbados");
		countrys.put("BD","Bangladesh");
		countrys.put("BE","Belgium");
		countrys.put("BF","Burkina Faso");
		countrys.put("BG","Bulgaria");
		countrys.put("BH","Bahrain");
		countrys.put("BI","Burundi");
		countrys.put("BJ","Benin");
		countrys.put("BM","Bermuda");
		countrys.put("BN","Brunei Darussalam");
		countrys.put("BO","Bolivia");
		countrys.put("BR","Brazil");
		countrys.put("BS","Bahamas");
		countrys.put("BT","Bhutan");
		countrys.put("BV","Bouvet Island");
		countrys.put("BW","Botswana");
		countrys.put("BY","Belarus");
		countrys.put("BZ","Belize");
		countrys.put("CA","Canada");
		countrys.put("CC","Cocos (Keeling) Islands");
		countrys.put("CD","Democratic Republic of the Congo");
		countrys.put("CF","Central African Republic");
		countrys.put("CG","Congo");
		countrys.put("CH","Switzerland");
		countrys.put("CI","Cote D'Ivoire (Ivory Coast)");
		countrys.put("CK","Cook Islands");
		countrys.put("CL","Chile");
		countrys.put("CM","Cameroon");
		countrys.put("CN","China");
		countrys.put("CO","Colombia");
		countrys.put("CR","Costa Rica");
		countrys.put("CS","Serbia and Montenegro");
		countrys.put("CU","Cuba");
		countrys.put("CV","Cape Verde");
		countrys.put("CX","Christmas Island");
		countrys.put("CY","Cyprus");
		countrys.put("CZ","Czech Republic");
		countrys.put("DE","Germany");
		countrys.put("DJ","Djibouti");
		countrys.put("DK","Denmark");
		countrys.put("DM","Dominica");
		countrys.put("DO","Dominican Republic");
		countrys.put("DZ","Algeria");
		countrys.put("EC","Ecuador");
		countrys.put("EE","Estonia");
		countrys.put("EG","Egypt");
		countrys.put("EH","Western Sahara");
		countrys.put("ER","Eritrea");
		countrys.put("ES","Spain");
		countrys.put("ET","Ethiopia");
		countrys.put("FI","Finland");
		countrys.put("FJ","Fiji");
		countrys.put("FK","Falkland Islands (Malvinas)");
		countrys.put("FM","Federated States of Micronesia");
		countrys.put("FO","Faroe Islands");
		countrys.put("FR","France");
		countrys.put("FX","France, Metropolitan");
		countrys.put("GA","Gabon");
		countrys.put("GB","Great Britain (UK)");
		countrys.put("GD","Grenada");
		countrys.put("GE","Georgia");
		countrys.put("GF","French Guiana");
		countrys.put("GH","Ghana");
		countrys.put("GI","Gibraltar");
		countrys.put("GL","Greenland");
		countrys.put("GM","Gambia");
		countrys.put("GN","Guinea");
		countrys.put("GP","Guadeloupe");
		countrys.put("GQ","Equatorial Guinea");
		countrys.put("GR","Greece");
		countrys.put("GS","S. Georgia and S. Sandwich Islands");
		countrys.put("GT","Guatemala");
		countrys.put("GU","Guam");
		countrys.put("GW","Guinea-Bissau");
		countrys.put("GY","Guyana");
		countrys.put("HK","Hong Kong");
		countrys.put("HM","Heard Island and McDonald Islands");
		countrys.put("HN","Honduras");
		countrys.put("HR","Croatia (Hrvatska)");
		countrys.put("HT","Haiti");
		countrys.put("HU","Hungary");
		countrys.put("ID","Indonesia");
		countrys.put("IE","Ireland");
		countrys.put("IL","Israel");
		countrys.put("IN","India");
		countrys.put("IO","British Indian Ocean Territory");
		countrys.put("IQ","Iraq");
		countrys.put("IR","Iran");
		countrys.put("IS","Iceland");
		countrys.put("IT","Italy");
		countrys.put("JM","Jamaica");
		countrys.put("JO","Jordan");
		countrys.put("JP","Japan");
		countrys.put("KE","Kenya");
		countrys.put("KG","Kyrgyzstan");
		countrys.put("KH","Cambodia");
		countrys.put("KI","Kiribati");
		countrys.put("KM","Comoros");
		countrys.put("KN","Saint Kitts and Nevis");
		countrys.put("KP","Korea (North)");
		countrys.put("KR","Korea (South)");
		countrys.put("KW","Kuwait");
		countrys.put("KY","Cayman Islands");
		countrys.put("KZ","Kazakhstan");
		countrys.put("LA","Laos");
		countrys.put("LB","Lebanon");
		countrys.put("LC","Saint Lucia");
		countrys.put("LI","Liechtenstein");
		countrys.put("LK","Sri Lanka");
		countrys.put("LR","Liberia");
		countrys.put("LS","Lesotho");
		countrys.put("LT","Lithuania");
		countrys.put("LU","Luxembourg");
		countrys.put("LV","Latvia");
		countrys.put("LY","Libya");
		countrys.put("MA","Morocco");
		countrys.put("MC","Monaco");
		countrys.put("MD","Moldova");
		countrys.put("MG","Madagascar");
		countrys.put("MH","Marshall Islands");
		countrys.put("MK","Macedonia");
		countrys.put("ML","Mali");
		countrys.put("MM","Myanmar");
		countrys.put("MN","Mongolia");
		countrys.put("MO","Macao");
		countrys.put("MP","Northern Mariana Islands");
		countrys.put("MQ","Martinique");
		countrys.put("MR","Mauritania");
		countrys.put("MS","Montserrat");
		countrys.put("MT","Malta");
		countrys.put("MU","Mauritius");
		countrys.put("MV","Maldives");
		countrys.put("MW","Malawi");
		countrys.put("MX","Mexico");
		countrys.put("MY","Malaysia");
		countrys.put("MZ","Mozambique");
		countrys.put("NA","Namibia");
		countrys.put("NC","New Caledonia");
		countrys.put("NE","Niger");
		countrys.put("NF","Norfolk Island");
		countrys.put("NG","Nigeria");
		countrys.put("NI","Nicaragua");
		countrys.put("NL","Netherlands");
		countrys.put("NO","Norway");
		countrys.put("NP","Nepal");
		countrys.put("NR","Nauru");
		countrys.put("NU","Niue");
		countrys.put("NZ","New Zealand (Aotearoa)");
		countrys.put("OM","Oman");
		countrys.put("PA","Panama");
		countrys.put("PE","Peru");
		countrys.put("PF","French Polynesia");
		countrys.put("PG","Papua New Guinea");
		countrys.put("PH","Philippines");
		countrys.put("PK","Pakistan");
		countrys.put("PL","Poland");
		countrys.put("PM","Saint Pierre and Miquelon");
		countrys.put("PN","Pitcairn");
		countrys.put("PR","Puerto Rico");
		countrys.put("PS","Palestinian Territory");
		countrys.put("PT","Portugal");
		countrys.put("PW","Palau");
		countrys.put("PY","Paraguay");
		countrys.put("QA","Qatar");
		countrys.put("RE","Reunion");
		countrys.put("RO","Romania");
		countrys.put("RU","Russian Federation");
		countrys.put("RW","Rwanda");
		countrys.put("SA","Saudi Arabia");
		countrys.put("SB","Solomon Islands");
		countrys.put("SC","Seychelles");
		countrys.put("SD","Sudan");
		countrys.put("SE","Sweden");
		countrys.put("SG","Singapore");
		countrys.put("SH","Saint Helena");
		countrys.put("SI","Slovenia");
		countrys.put("SJ","Svalbard and Jan Mayen");
		countrys.put("SK","Slovakia");
		countrys.put("SL","Sierra Leone");
		countrys.put("SM","San Marino");
		countrys.put("SN","Senegal");
		countrys.put("SO","Somalia");
		countrys.put("SR","Suriname");
		countrys.put("ST","Sao Tome and Principe");
		countrys.put("SU","USSR (former)");
		countrys.put("SV","El Salvador");
		countrys.put("SY","Syria");
		countrys.put("SZ","Swaziland");
		countrys.put("TC","Turks and Caicos Islands");
		countrys.put("TD","Chad");
		countrys.put("TF","French Southern Territories");
		countrys.put("TG","Togo");
		countrys.put("TH","Thailand");
		countrys.put("TJ","Tajikistan");
		countrys.put("TK","Tokelau");
		countrys.put("TL","Timor-Leste");
		countrys.put("TM","Turkmenistan");
		countrys.put("TN","Tunisia");
		countrys.put("TO","Tonga");
		countrys.put("TP","East Timor");
		countrys.put("TR","Turkey");
		countrys.put("TT","Trinidad and Tobago");
		countrys.put("TV","Tuvalu");
		countrys.put("TW","Taiwan");
		countrys.put("TZ","Tanzania");
		countrys.put("UA","Ukraine");
		countrys.put("UG","Uganda");
		countrys.put("UK","United Kingdom");
		countrys.put("UM","United States Minor Outlying Islands");
		countrys.put("UY","Uruguay");
		countrys.put("UZ","Uzbekistan");
		countrys.put("VA","Vatican City State (Holy See)");
		countrys.put("VC","Saint Vincent and the Grenadines");
		countrys.put("VE","Venezuela");
		countrys.put("VG","Virgin Islands (British)");
		countrys.put("VI","Virgin Islands (U.S.)");
		countrys.put("VN","Viet Nam");
		countrys.put("VU","Vanuatu");
		countrys.put("WF","Wallis and Futuna");
		countrys.put("WS","Samoa");
		countrys.put("YE","Yemen");
		countrys.put("YT","Mayotte");
		countrys.put("YU","Yugoslavia (former)");
		countrys.put("ZA","South Africa");
		countrys.put("ZM","Zambia");
		countrys.put("ZR","Zaire (former)");
		countrys.put("ZW","Zimbabwe");
	}
	
	public Map<String, String> getCountryCodeMap() {
		return countrys;
	}
	
	public SelectItem[] getCountryCodeItems() {
		SelectItem[] countryItems = new SelectItem[countrys.size()];
		Set<String> countryCodes = countrys.keySet();
		int i = 0;
		for (Iterator<String> it  = countryCodes.iterator(); it.hasNext(); ) {
			String key = it.next();
			countryItems[i++] = new SelectItem(key,countrys.get(key));
		}
		return countryItems;
	};

	public SelectItem[] getMobileCarrierItems() {
		SelectItem[] carrierItems = new SelectItem[MobileCarrierEnum.values().length];
		int i=0;
		for (MobileCarrierEnum carrier : MobileCarrierEnum.values()) {
			carrierItems[i++] = new SelectItem(carrier.getValue());
		}
		return carrierItems;
	};
}
