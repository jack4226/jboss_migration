<%@page import="jpa.service.maillist.MailingListService"%>
<%@page import="jpa.service.common.EmailAddressService"%>
<%@page import="jpa.service.common.SubscriptionService"%>
<%@page import="jpa.service.maillist.MailingListBo"%>
<%@page import="jpa.msgui.util.SpringUtil"%>
<%@page import="jpa.model.MailingList"%>
<%@page import="jpa.model.EmailVariable"%>
<%@page import="jpa.service.common.EmailVariableService"%>
<%@page import="jpa.service.common.SubscriberDataService"%>
<%@page import="jpa.service.maillist.BroadcastMessageService"%>
<%@page import="jpa.service.maillist.BroadcastTrackingService"%>
<%@page import="jpa.service.maillist.RenderBo"%>
<%@page import="jpa.constant.*"%>
<%@page import="jpa.variable.*"%>
<%@page import="org.apache.logging.log4j.LogManager" %>
<%@page import="org.apache.logging.log4j.Logger" %>
<%@page import="java.util.*" %>
<%@page import="org.apache.commons.lang3.StringUtils"%>

<%!MailingListService mailingListDao = null;
	MailingListService getMailingListService(ServletContext ctx) {
		if (mailingListDao == null) {
			mailingListDao = SpringUtil.getWebAppContext(ctx).getBean(MailingListService.class);
		}
		return mailingListDao;
	}

	MailingListBo mailingListBo = null;
	MailingListBo getMailingListBo(ServletContext ctx) {
		if (mailingListBo == null) {
			mailingListBo = SpringUtil.getWebAppContext(ctx).getBean(MailingListBo.class);
		}
		return mailingListBo;
	}

	EmailAddressService emailAddrDao = null;
	EmailAddressService getEmailAddressService(ServletContext ctx) {
		if (emailAddrDao == null) {
			emailAddrDao = SpringUtil.getWebAppContext(ctx).getBean(EmailAddressService.class);
		}
		return emailAddrDao;
	}

	SubscriptionService subscriptionDao = null;
	SubscriptionService getSubscriptionService(ServletContext ctx) {
		if (subscriptionDao == null) {
			subscriptionDao = SpringUtil.getWebAppContext(ctx).getBean(SubscriptionService.class);
		}
		return subscriptionDao;
	}

	EmailVariableService emailVariableDao = null;
	EmailVariableService getEmailVariableService(ServletContext ctx) {
		if (emailVariableDao == null) {
			emailVariableDao = SpringUtil.getWebAppContext(ctx).getBean(EmailVariableService.class);
		}
		return emailVariableDao;
	}

	private SubscriberDataService customerBo = null;
	SubscriberDataService getSubscriberDataService(ServletContext ctx) {
		if (customerBo == null) {
			customerBo = SpringUtil.getWebAppContext(ctx).getBean(SubscriberDataService.class);
		}
		return customerBo;
	}
	
	BroadcastMessageService broadcastMsgDao = null;
	BroadcastMessageService getBroadcastMessageService(ServletContext ctx) {
		if (broadcastMsgDao == null) {
			broadcastMsgDao = SpringUtil.getWebAppContext(ctx).getBean(BroadcastMessageService.class);
		}
		return broadcastMsgDao;
	}

	BroadcastTrackingService broadcastTrkDao = null;
	BroadcastTrackingService getBroadcastTrackingService(ServletContext ctx) {
		if (broadcastTrkDao == null) {
			broadcastTrkDao = SpringUtil.getWebAppContext(ctx).getBean(BroadcastTrackingService.class);
		}
		return broadcastTrkDao;
	}

	private RenderBo renderBo = null;
	RenderBo getRenderBo(ServletContext ctx) {
		if (renderBo == null) {
			renderBo = SpringUtil.getWebAppContext(ctx).getBean(RenderBo.class);
		}
		return renderBo;
	}

	List<MailingList> getSbsrMailingLists(ServletContext ctx, String emailAddr) {
		List<MailingList> subedList = getMailingListService(ctx).getByAddressWithCounts(emailAddr);
		HashMap<String, MailingList> map = new HashMap<String, MailingList>();
		for (Iterator<MailingList> it = subedList.iterator(); it.hasNext();) {
			MailingList vo = it.next();
			map.put(vo.getListId(), vo);
		}
		List<MailingList> allList = getMailingListService(ctx).getAll(true);
		for (int i = 0; i < allList.size(); i++) {
			MailingList vo = allList.get(i);
			if (map.containsKey(vo.getListId())) {
				allList.set(i, map.get(vo.getListId()));
			}
		}
		return allList;
	}
	
	String renderURLVariable(ServletContext ctx, String emailVariableName) {
		return renderURLVariable(ctx, emailVariableName, null);
	}
	
	String renderURLVariable(ServletContext ctx, String emailVariableName, String sbsrId) {
		return renderURLVariable(ctx, emailVariableName, sbsrId, null, null);
	}
	
	String renderURLVariable(ServletContext ctx, String emailVariableName, String sbsrId, String listId, String msgId) {
		Logger logger = LogManager.getLogger("jpa.service.jsp");
		String renderedValue = "";
		EmailVariable vo = getEmailVariableService(ctx).getByVariableName(emailVariableName);
		HashMap<String, RenderVariableVo> vars = new HashMap<String, RenderVariableVo>();
		if (StringUtils.isNotBlank(sbsrId)) {
			RenderVariableVo var = new RenderVariableVo(
					"SubscriberAddressId",
					sbsrId,
					null,
					VariableType.TEXT,
					CodeType.YES_CODE.getValue(),
					Boolean.FALSE);
			vars.put(var.getVariableName(), var);
		}
		if (StringUtils.isNotBlank(listId)) {
			RenderVariableVo var = new RenderVariableVo(
					"MailingListId",
					listId,
					null,
					VariableType.TEXT,
					CodeType.YES_CODE.getValue(),
					Boolean.FALSE);
			vars.put(var.getVariableName(), var);
		}
		if (StringUtils.isNotBlank(msgId)) {
			RenderVariableVo var = new RenderVariableVo(
					"BroadcastMsgId",
					msgId,
					null,
					VariableType.TEXT,
					CodeType.YES_CODE.getValue(),
					Boolean.FALSE);
			vars.put(var.getVariableName(), var);
		}
		if (vo != null) {
			try {
				renderedValue = getRenderBo(ctx).renderTemplateText(vo.getDefaultValue(), null, vars);
			}
			catch (Exception e) {
				logger.error("loadSbsrDaos.jsp - renderURLVariable: ", e);
			}
		}
		return renderedValue;
	}
	
	String blankToNull(String str) {
		if (str == null || str.trim().length() ==0)
			return null;
		else
			return str;
	}

	String nullToBlank(String str) {
		if (str == null)
			return "";
		else
			return str;
	}%>