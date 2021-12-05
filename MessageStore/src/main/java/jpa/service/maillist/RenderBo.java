package jpa.service.maillist;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.Address;
import javax.mail.Part;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.constant.CarrierCode;
import jpa.constant.CodeType;
import jpa.constant.Constants;
import jpa.constant.EmailAddrType;
import jpa.constant.VariableName;
import jpa.constant.VariableType;
import jpa.constant.XHeaderName;
import jpa.exception.DataValidationException;
import jpa.exception.TemplateException;
import jpa.message.BodypartBean;
import jpa.message.MessageBean;
import jpa.message.MsgHeader;
import jpa.model.GlobalVariable;
import jpa.model.SenderData;
import jpa.model.SenderVariable;
import jpa.model.msg.MessageRendered;
import jpa.model.msg.MessageSource;
import jpa.model.msg.RenderVariable;
import jpa.model.msg.TemplateData;
import jpa.model.msg.TemplateDataPK;
import jpa.model.msg.TemplateVariable;
import jpa.service.common.GlobalVariableService;
import jpa.service.common.SenderDataService;
import jpa.service.common.SenderVariableService;
import jpa.service.msgdata.MessageRenderedService;
import jpa.service.msgdata.MessageSourceService;
import jpa.service.msgdata.TemplateDataService;
import jpa.service.msgdata.TemplateVariableService;
import jpa.service.msgout.MsgOutboxBo;
import jpa.spring.util.SpringUtil;
import jpa.variable.ErrorVariableVo;
import jpa.variable.RenderVariableVo;
import jpa.variable.Renderer;

@Component("renderBo")
@Transactional(propagation=Propagation.REQUIRED)
public class RenderBo implements java.io.Serializable {
	private static final long serialVersionUID = -8967835234168609528L;
	static final Logger logger = LogManager.getLogger(RenderBo.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	static final String LF = System.getProperty("line.separator","\n");
	
	private final Renderer render = Renderer.getInstance();
	
	@Autowired
	private MessageSourceService msgSourceDao;
	@Autowired
	private TemplateDataService templateDao;
	@Autowired
	private SenderVariableService senderVariableDao;
	@Autowired
	private GlobalVariableService globalVariableDao;
	@Autowired
	private TemplateVariableService templateVariableDao;
	@Autowired
	private SenderDataService senderService;
	
	public static void main(String[] args) {
		RenderBo bo = SpringUtil.getAppContext().getBean(RenderBo.class);
		MsgOutboxBo outboxBo = SpringUtil.getAppContext().getBean(MsgOutboxBo.class);
		MessageRenderedService rndrDao = SpringUtil.getAppContext().getBean(MessageRenderedService.class);
		SpringUtil.beginTransaction();
		try {
			MessageRendered mr = rndrDao.getFirstRecord();
			if (mr == null) {
				throw new IllegalStateException("Message_Rendered table is empty.");
			}
			RenderRequest req = outboxBo.getRenderRequestByPK(mr.getRowId());
			RenderVariableVo vo = new RenderVariableVo(
					EmailAddrType.TO_ADDR.getValue(),
					"testto@localhost",
					VariableType.ADDRESS);
			req.getVariableOverrides().put(vo.getVariableName(), vo);
			RenderResponse rsp = bo.getRenderedEmail(req);
			logger.info(req);
			logger.info(rsp);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			SpringUtil.commitTransaction();
		}
	}
	
	public RenderResponse getRenderedEmail(RenderRequest req)
			throws ParseException, AddressException, TemplateException {
		logger.info("in getRenderedEmail(RenderRequest)...");
		if (req == null) {
			throw new IllegalArgumentException("RenderRequest is null");
		}
		if (req.startTime==null) {
			req.startTime = new Timestamp(new java.util.Date().getTime());
		}
		RenderResponse rsp = initRenderResponse(req);
		buildRenderVariableVos(req, rsp);
		buildRenderedBody(req, rsp);
		buildRenderedSubj(req, rsp);
		buildRenderedAttachments(req, rsp);
		buildRenderedAddrs(req, rsp);
		buildRenderedMisc(req, rsp);
		buildRenderedXHdrs(req, rsp); // to be executed last

		return rsp;
	}
	
	private RenderResponse initRenderResponse(RenderRequest req) throws DataValidationException {
		MessageSource vo = msgSourceDao.getByMsgSourceId(req.msgSourceId);
		if (vo == null) {
			throw new DataValidationException("MsgSource record not found for " + req.msgSourceId);
		}
		RenderResponse rsp = new RenderResponse(
				vo,
				req.senderId,
				req.startTime,
				new HashMap<String, RenderVariableVo>(),
				new HashMap<String, ErrorVariableVo>(),
				new MessageBean()
				);
		return rsp;
	}
	
	RenderResponse getRenderedBody(RenderRequest req) throws ParseException, TemplateException {
		RenderResponse rsp = initRenderResponse(req);
		buildRenderVariableVos(req, rsp);
		buildRenderedBody(req, rsp);

		return rsp;
	}
	
	RenderResponse getRenderedMisc(RenderRequest req) {
		RenderResponse rsp = initRenderResponse(req);
		buildRenderVariableVos(req, rsp);
		buildRenderedMisc(req, rsp);

		return rsp;
	}
	
	RenderResponse getRenderedSubj(RenderRequest req)
			throws ParseException, TemplateException {
		RenderResponse rsp = initRenderResponse(req);
		buildRenderVariableVos(req, rsp);
		buildRenderedSubj(req, rsp);

		return rsp;
	}

	RenderResponse getRenderedAddrs(RenderRequest req) throws AddressException {
		RenderResponse rsp = initRenderResponse(req);
		buildRenderVariableVos(req, rsp);
		buildRenderedAddrs(req, rsp);

		return rsp;
	}

	RenderResponse getRenderedXHdrs(RenderRequest req) {
		RenderResponse rsp = initRenderResponse(req);
		buildRenderVariableVos(req, rsp);
		buildRenderedXHdrs(req, rsp);

		return rsp;
	}
	
	private void buildRenderedBody(RenderRequest req, RenderResponse rsp)
			throws ParseException, TemplateException {
		if (isDebugEnabled)
			logger.debug("in buildRenderedBody()...");
		MessageSource srcVo = rsp.msgSourceVo;
		
		String bodyTemplate = null;
		String contentType = null;
		// body template may come from variables
		if (rsp.variableFinal.containsKey(VariableName.BODY_TEMPLATE.getValue())
				&& CodeType.YES_CODE.getValue().equalsIgnoreCase(srcVo.getAllowOverride())) {
			RenderVariableVo var = (RenderVariableVo) rsp.variableFinal.get(VariableName.BODY_TEMPLATE.getValue());
			if (VariableType.TEXT.equals(var.getVariableType())) {
				bodyTemplate = (String) var.getVariableValue();
				contentType = var.getVariableFormat() == null ? "text/plain"
						: var.getVariableFormat();
			}
		}
		
		if (bodyTemplate == null) {
			TemplateData tmpltVo = templateDao.getByBestMatch(srcVo.getTemplateData().getTemplateDataPK());
			if (tmpltVo == null) {
				throw new DataValidationException("BodyTemplate not found for: "
						+ srcVo.getTemplateData().getTemplateDataPK());
			}
			bodyTemplate = tmpltVo.getBodyTemplate();
			contentType = tmpltVo.getContentType();
		}
		
		String body = render(bodyTemplate, rsp.variableFinal, rsp.variableErrors);
		MessageBean mBean = rsp.messageBean;
		mBean.setContentType(contentType);
		mBean.setBody(body);
	}
	
	private void buildRenderedSubj(RenderRequest req, RenderResponse rsp)
			throws ParseException, TemplateException {
		logger.info("in buildRenderedSubj()...");
		MessageSource srcVo = rsp.msgSourceVo;

		String subjTemplate = null;
		// subject template may come from variables
		if (rsp.variableFinal.containsKey(VariableName.SUBJECT_TEMPLATE.getValue())
				&& CodeType.YES_CODE.getValue().equalsIgnoreCase(srcVo.getAllowOverride())) {
			RenderVariableVo var = (RenderVariableVo) rsp.variableFinal.get(VariableName.SUBJECT_TEMPLATE.getValue());
			if (VariableType.TEXT.equals(var.getVariableType())) {
				subjTemplate = (String) var.getVariableValue();
			}
		}

		if (subjTemplate == null) {
			TemplateData tmpltVo = templateDao.getByBestMatch(srcVo.getTemplateData().getTemplateDataPK());
			if (tmpltVo == null) {
				throw new DataValidationException("SubjTemplate not found for: "
						+ srcVo.getTemplateData().getTemplateDataPK());
			}
			subjTemplate = tmpltVo.getSubjectTemplate();
		}
		
		String subj = render(subjTemplate, rsp.variableFinal, rsp.variableErrors);
		MessageBean mBean = rsp.messageBean;
		mBean.setSubject(subj);
	}

	private void buildRenderedAttachments(RenderRequest req, RenderResponse rsp) {
		logger.info("in buildRenderedAttachments()...");
		Map<String, RenderVariableVo> varbls = rsp.variableFinal;
		MessageBean mBean = rsp.messageBean;
		Collection<RenderVariableVo> c = varbls.values();
		for (Iterator<RenderVariableVo> it = c.iterator(); it.hasNext();) {
			RenderVariableVo r = it.next();
			if (VariableType.LOB.equals(r.getVariableType()) && r.getVariableValue() != null) {
				BodypartBean attNode = new BodypartBean();
				if (r.getVariableFormat() != null && r.getVariableFormat().indexOf(";") > 0
						&& r.getVariableFormat().indexOf("name=") > 0) {
					attNode.setContentType(r.getVariableFormat());
				}
				else {
					attNode.setContentType(r.getVariableFormat() + "; name=\""
							+ r.getVariableName() + "\"");
				}
				attNode.setDisposition(Part.ATTACHMENT);
				// not necessary, for consistency?
				attNode.setDescription(r.getVariableName());
				attNode.setValue(r.getVariableValue());
				mBean.put(attNode);
			}
		}
	}
	
	private String render(String templateText, Map<String, RenderVariableVo> varbls,
			Map<String, ErrorVariableVo> errors) throws TemplateException, ParseException {
		return render.render(templateText, varbls, errors);
	}
	
	private void buildRenderedAddrs(RenderRequest req, RenderResponse rsp) throws AddressException {
		logger.info("in buildRenderedAddrs()...");
		Map<String, RenderVariableVo> varbls = rsp.variableFinal;
		MessageBean mBean = rsp.messageBean;

		// variableValue could be type of: String/Address
		Collection<RenderVariableVo> c = varbls.values();
		for (Iterator<RenderVariableVo> it=c.iterator(); it.hasNext();) {
			RenderVariableVo r = it.next();
			if (VariableType.ADDRESS.equals(r.getVariableType()) && r.getVariableValue() != null) {
				if (EmailAddrType.FROM_ADDR.getValue().equals(r.getVariableName())) {
					if (r.getVariableValue() instanceof String)
						mBean.setFrom(InternetAddress.parse((String) r.getVariableValue()));
					else if (r.getVariableValue() instanceof InternetAddress) {
						mBean.setFrom(InternetAddress.parse(((Address)r.getVariableValue()).toString()));
					}
				}
				else if (EmailAddrType.REPLYTO_ADDR.getValue().equals(r.getVariableName())) {
					if (r.getVariableValue() instanceof String)
						mBean.setReplyto(InternetAddress.parse((String) r.getVariableValue()));
					else if (r.getVariableValue() instanceof Address) {
						mBean.setReplyto(InternetAddress.parse(((Address)r.getVariableValue()).toString()));
					}
				}
				else if (EmailAddrType.TO_ADDR.getValue().equals(r.getVariableName())) {
					if (r.getVariableValue() instanceof String)
						mBean.setTo(InternetAddress.parse((String) r.getVariableValue()));
					else if (r.getVariableValue() instanceof Address) {
						mBean.setTo(InternetAddress.parse(((Address)r.getVariableValue()).toString()));
					}
				}
				else if (EmailAddrType.CC_ADDR.getValue().equals(r.getVariableName())) {
					if (r.getVariableValue() instanceof String)
						mBean.setCc(InternetAddress.parse((String) r.getVariableValue()));
					else if (r.getVariableValue() instanceof Address) {
						mBean.setCc(InternetAddress.parse(((Address)r.getVariableValue()).toString()));
					}
				}
				else if (EmailAddrType.BCC_ADDR.getValue().equals(r.getVariableName())) {
					if (r.getVariableValue() instanceof String)
						mBean.setBcc(InternetAddress.parse((String) r.getVariableValue()));
					else if (r.getVariableValue() instanceof Address) {
						mBean.setBcc(InternetAddress.parse(((Address)r.getVariableValue()).toString()));
					}
				}
			}
		}
	}

	private void buildRenderedMisc(RenderRequest req, RenderResponse rsp) {
		logger.info("in buildRenderedMisc()...");
		MessageSource src = rsp.msgSourceVo;
		Map<String, RenderVariableVo> varbls = rsp.variableFinal;
		MessageBean mBean = rsp.messageBean;

		Collection<RenderVariableVo> c = varbls.values();
		for (Iterator<RenderVariableVo> it=c.iterator(); it.hasNext();) {
			RenderVariableVo r = it.next();
			if (r.getVariableValue() != null && VariableType.TEXT.equals(r.getVariableType())) {
				if (VariableName.PRIORITY.getValue().equals(r.getVariableName())) {
					String[] s = { (String) r.getVariableValue() };
					mBean.setPriority(s);
				}
				else if (VariableName.RULE_NAME.getValue().equals(r.getVariableName()))
					mBean.setRuleName((String)r.getVariableValue());
				else if (VariableName.CARRIER_CODE.getValue().equals(r.getVariableName()))
					mBean.setCarrierCode(CarrierCode.getByValue((String)r.getVariableValue()));
				else if (VariableName.MAILBOX_HOST.getValue().equals(r.getVariableName()))
					mBean.setMailboxHost((String)r.getVariableValue());
				else if (VariableName.MAILBOX_HOST.getValue().equals(r.getVariableName()))
					mBean.setMailboxHost((String)r.getVariableValue());
				else if (VariableName.MAILBOX_NAME.getValue().equals(r.getVariableName()))
					mBean.setMailboxName((String)r.getVariableValue());
				else if (VariableName.MAILBOX_USER.getValue().equals(r.getVariableName()))
					mBean.setMailboxUser((String)r.getVariableValue());
				else if (VariableName.FOLDER_NAME.getValue().equals(r.getVariableName()))
					mBean.setFolderName((String)r.getVariableValue());
				else if (VariableName.SENDER_ID.getValue().equals(r.getVariableName()))
					mBean.setSenderId((String)r.getVariableValue());
				else if (VariableName.SUBSCRIBER_ID.getValue().equals(r.getVariableName()))
					mBean.setSubrId((String)r.getVariableValue());
				else if (VariableName.TO_PLAIN_TEXT.getValue().equals(r.getVariableName()))
					mBean.setToPlainText(CodeType.YES_CODE.getValue().equals((String)r.getVariableValue()));
			}
			else if (r.getVariableValue() != null && VariableType.NUMERIC.equals(r.getVariableType())) {
				if (VariableName.MSG_REF_ID.getValue().equals(r.getVariableName())) {
					if (r.getVariableValue() instanceof BigDecimal)
						mBean.setMsgRefId(((BigDecimal) r.getVariableValue()).intValue());
					else if (r.getVariableValue() instanceof String)
						mBean.setMsgRefId(Integer.valueOf((String) r.getVariableValue()));
				}
			}
			else if (VariableType.DATETIME.equals(r.getVariableType())) {
				if (VariableName.SEND_DATE.equals(r.getVariableName())) {
					if (r.getVariableValue() == null) {
						mBean.setSendDate(new java.util.Date());
					}
					else {
						SimpleDateFormat fmt = new SimpleDateFormat(RenderVariableVo.DEFAULT_DATETIME_FORMAT);
						if (r.getVariableFormat()!=null) {
							fmt.applyPattern(r.getVariableFormat());
						}
						if (r.getVariableValue() instanceof String) {
							try {
								java.util.Date date = fmt.parse((String) r.getVariableValue());
								mBean.setSendDate(date);
							}
							catch (ParseException e) {
								logger.error("ParseException caught", e);
								mBean.setSendDate(new java.util.Date());
							}
						}
						else if (r.getVariableValue() instanceof java.util.Date) {
							mBean.setSendDate((java.util.Date) r.getVariableValue());
						}
					}
				}
			}
		}
		// make sure CarrierCode is populated
		if (mBean.getCarrierCode() == null) {
			mBean.setCarrierCode(CarrierCode.getByValue(rsp.msgSourceVo.getCarrierCode()));
		}

		if (src.isExcludingIdToken()) {
			mBean.setEmBedEmailId(Boolean.valueOf(false));
		}

		if (src.isSaveMsgStream())
			mBean.setSaveMsgStream(true);
		else
			mBean.setSaveMsgStream(false);
	}
	
	/*
	 * If MessageBean's SenderId field is not valued, and X-Sender_id header is
	 * found and valued, populate MessageBean's SenderId field with the value
	 * from X-Sender_id header. <br> 
	 */
	private void buildRenderedXHdrs(RenderRequest req, RenderResponse rsp) {
		logger.info("in buildRenderedXHdrs()...");
		// MessageSource src = rsp.msgSourceVo;
		Map<String, RenderVariableVo> varbls = rsp.variableFinal;
		MessageBean mBean = rsp.messageBean;
		List<MsgHeader> headers = new ArrayList<MsgHeader>();

		Collection<RenderVariableVo> c = varbls.values();
		for (Iterator<RenderVariableVo> it=c.iterator(); it.hasNext();) {
			RenderVariableVo r = it.next();
			if (VariableType.X_HEADER.equals(r.getVariableType()) && r.getVariableValue() != null) {
				MsgHeader msgHeader = new MsgHeader();
				msgHeader.setName(r.getVariableName());
				msgHeader.setValue((String) r.getVariableValue());
				headers.add(msgHeader);
				// set SenderId for MessageBean
				if (XHeaderName.SENDER_ID.value().equals(r.getVariableName())) {
					if (StringUtils.isBlank(mBean.getSenderId())) {
						mBean.setSenderId((String) r.getVariableValue());
					}
				}
				else if (XHeaderName.SUBSCRIBER_ID.value().equals(r.getVariableName())) {
					if (StringUtils.isBlank(mBean.getSubrId())) {
						mBean.setSubrId((String) r.getVariableValue());
					}
				}
			}
		}
		mBean.setHeaders(headers);
	}
	
	private void buildRenderVariableVos(RenderRequest req, RenderResponse rsp) {
		logger.info("in buildRenderVariableVos()...");
		
		MessageSource msgSourceVo = msgSourceDao.getByMsgSourceId(req.msgSourceId);
		rsp.msgSourceVo = msgSourceVo;
		
		// retrieve variables
		Collection<GlobalVariable> globalVariables = globalVariableDao.getCurrent();
		Collection<SenderVariable> senderVariables = senderVariableDao.getCurrentBySenderId(
				req.senderId);
		Collection<TemplateVariable> templateVariables = msgSourceVo.getTemplateVariableList();
		
		// convert variables into Map
		Map<String, RenderVariableVo> g_ht = globalVariablesToMap(globalVariables);
		Map<String, RenderVariableVo> c_ht = senderVariablesToMap(senderVariables);
		Map<String, RenderVariableVo> t_ht = templateVariablesToMap(templateVariables);
		
		// variables from req and MsgSource table
		Map<String, RenderVariableVo> s_ht = new HashMap<String, RenderVariableVo>();
		RenderVariableVo vreq = new RenderVariableVo(
				VariableName.SENDER_ID.getValue(),
				req.senderId,
				null,
				VariableType.TEXT, 
				CodeType.YES_CODE.getValue(),
				Boolean.TRUE);
		s_ht.put(vreq.getVariableName(), vreq);
		
		vreq = new RenderVariableVo(
			EmailAddrType.FROM_ADDR.getValue(),
			msgSourceVo.getFromAddress().getAddress(),
			null,
			VariableType.ADDRESS, 
			CodeType.YES_CODE.getValue(),
			Boolean.TRUE);
		s_ht.put(vreq.getVariableName(), vreq);
		
		if (msgSourceVo.getReplyToAddress()!=null) {
			vreq = new RenderVariableVo(
				EmailAddrType.REPLYTO_ADDR.getValue(),
				msgSourceVo.getReplyToAddress().getAddress(),
				null,
				VariableType.ADDRESS, 
				CodeType.YES_CODE.getValue(),
				Boolean.FALSE);
			s_ht.put(vreq.getVariableName(), vreq);
		}
		
		// get Runtime variables
		Map<String, RenderVariableVo> r_ht = req.variableOverrides;
		if (r_ht==null) {
			r_ht = new HashMap<String, RenderVariableVo>();
		}
		
		// error hash table
		Map<String, ErrorVariableVo> err_ht = new HashMap<String, ErrorVariableVo>();
		
		// merge variable tables
		mergeVariableMaps(s_ht, g_ht, err_ht);
		mergeVariableMaps(c_ht, g_ht, err_ht);
		mergeVariableMaps(t_ht, g_ht, err_ht);
		verifyVariableMap(g_ht, r_ht, err_ht);
		mergeVariableMaps(r_ht, g_ht, err_ht);
		
		rsp.variableFinal.putAll(g_ht);
		rsp.variableErrors.putAll(err_ht);
	}
	
	private void mergeVariableMaps(Map<String, RenderVariableVo> from,
			Map<String, RenderVariableVo> to, Map<String, ErrorVariableVo> error) {
		Set<String> keys = from.keySet();
		for (Iterator<String> it=keys.iterator(); it.hasNext();) {
			String name = it.next();
			if (to.get(name) != null) {
				RenderVariableVo req = (RenderVariableVo) to.get(name);
				if (CodeType.YES_CODE.getValue().equalsIgnoreCase(req.getAllowOverride())
						|| CodeType.MANDATORY_CODE.getValue().equalsIgnoreCase(req.getAllowOverride())) {
					to.put(name, from.get(name));
				}
				else {
					RenderVariableVo r = (RenderVariableVo) from.get(name);
					ErrorVariableVo err = new ErrorVariableVo(
							r.getVariableName(), 
							r.getVariableValue(), 
							"Variable Override is not allowed.");
					error.put(name, err);
				}
			}
			else {
				to.put(name, from.get(name));
			}
		}
	}
	
	private void verifyVariableMap(Map<String, RenderVariableVo> gt,
			Map<String, RenderVariableVo> rt, Map<String, ErrorVariableVo> error) {
		Set<String> keys = gt.keySet();
		for (Iterator<String> it=keys.iterator(); it.hasNext();) {
			String name = it.next();
			RenderVariableVo req = (RenderVariableVo) gt.get(name);
			if (CodeType.MANDATORY_CODE.getValue().equalsIgnoreCase(req.getAllowOverride())) {
				if (!rt.containsKey(name)) {
					ErrorVariableVo err = new ErrorVariableVo(
							req.getVariableName(),
							req.getVariableValue(),
							"Variable Override is mandatory.");
					error.put(name, err);
				}
			}
		}
	}
	
	private Map<String, RenderVariableVo> globalVariablesToMap(Collection<GlobalVariable> c) {
		Map<String, RenderVariableVo> ht = new HashMap<String, RenderVariableVo>();
		for (GlobalVariable req : c) {
			RenderVariableVo r = new RenderVariableVo(
				req.getGlobalVariablePK().getVariableName(),
				req.getVariableValue(), 
				req.getVariableFormat(), 
				VariableType.getByValue(req.getVariableType()), 
				req.getAllowOverride(), 
				req.isRequired()
				);
			ht.put(req.getGlobalVariablePK().getVariableName(), r);
		}
		return ht;
	}
	
	private Map<String, RenderVariableVo> senderVariablesToMap(Collection<SenderVariable> c) {
		Map<String, RenderVariableVo> ht = new HashMap<String, RenderVariableVo>();
		for (SenderVariable req : c) {
			RenderVariableVo r = new RenderVariableVo(
				req.getSenderVariablePK().getVariableName(),
				req.getVariableValue(), 
				req.getVariableFormat(), 
				VariableType.getByValue(req.getVariableType()), 
				req.getAllowOverride(), 
				req.isRequired()
				);
			ht.put(req.getSenderVariablePK().getVariableName(), r);
		}
		return ht;
	}
	
	private Map<String, RenderVariableVo> templateVariablesToMap(
			Collection<TemplateVariable> c) {
		Map<String, RenderVariableVo> ht = new HashMap<String, RenderVariableVo>();
		for (TemplateVariable req : c) {
			RenderVariableVo r = new RenderVariableVo(
				req.getTemplateVariablePK().getVariableName(),
				req.getVariableValue(), 
				req.getVariableFormat(), 
				VariableType.getByValue(req.getVariableType()), 
				req.getAllowOverride(), 
				req.isRequired()
				);
			ht.put(req.getTemplateVariablePK().getVariableName(), r);
		}
		return ht;
	}

	public static Map<String, RenderVariableVo> renderVariablesToMap(Collection<RenderVariable> c) {
		Map<String, RenderVariableVo> ht = new HashMap<String, RenderVariableVo>();
		for (RenderVariable req : c) {
			RenderVariableVo r = new RenderVariableVo(
				req.getRenderVariablePK().getVariableName(),
				req.getVariableValue(), 
				req.getVariableFormat(), 
				VariableType.getByValue(req.getVariableType()), 
				CodeType.YES_CODE.getValue(), 
				Boolean.FALSE
				);
			ht.put(req.getRenderVariablePK().getVariableName(), r);
		}
		return ht;
	}
	
	
	/**
	 * render a template by templateId and senderId.
	 * 
	 * @param templateId -
	 *            template id
	 * @param senderId -
	 *            sender id
	 * @param variables -
	 *            variables
	 * @return rendered text
	 * @throws DataValidationException
	 * @throws ParseException
	 * @throws TemplateException 
	 */
	public String renderTemplateById(String templateId, String senderId,
			Map<String, RenderVariableVo> variables) throws DataValidationException,
			ParseException, TemplateException {
		if (StringUtils.isBlank(senderId)) {
			senderId = Constants.DEFAULT_SENDER_ID;
		}
		SenderData sender = senderService.getBySenderId(senderId);
		Timestamp startTime = new Timestamp(System.currentTimeMillis());
		TemplateDataPK pk = new TemplateDataPK(sender,templateId,startTime);
		TemplateData tmpltVo = templateDao.getByBestMatch(pk);
		if (tmpltVo == null) {
			throw new DataValidationException("TemplateData not found by: " + templateId + "/"
					+ senderId + "/" + startTime);
		}
		if (isDebugEnabled) {
			logger.debug("Template to render:" + LF + tmpltVo.getBodyTemplate());
		}
		
		HashMap<String, RenderVariableVo> map = new HashMap<String, RenderVariableVo>();

		List<TemplateVariable> tmpltList = templateVariableDao.getByVariableId(templateId);
		for (Iterator<TemplateVariable> it = tmpltList.iterator(); it.hasNext();) {
			TemplateVariable vo = it.next();
			RenderVariableVo var = new RenderVariableVo(
					vo.getTemplateVariablePK().getVariableName(),
					vo.getVariableValue(),
					vo.getVariableFormat(),
					VariableType.getByValue(vo.getVariableType()),
					vo.getAllowOverride(),
					vo.isRequired());
			if (map.containsKey(vo.getTemplateVariablePK().getVariableName())) {
				RenderVariableVo v2 = map.get(vo.getTemplateVariablePK().getVariableName());
				if (CodeType.YES_CODE.getValue().equalsIgnoreCase(v2.getAllowOverride())) {
					map.put(vo.getTemplateVariablePK().getVariableName(), var);
				}
			}
			else {
				map.put(vo.getTemplateVariablePK().getVariableName(), var);
			}
		}
		
		if (variables != null) {
			Set<String> keys = variables.keySet();
			for (Iterator<String> it=keys.iterator(); it.hasNext(); ) {
				String key = it.next();
				if (map.containsKey(key)) {
					RenderVariableVo v2 = map.get(key);
					if (CodeType.YES_CODE.getValue().equalsIgnoreCase(v2.getAllowOverride())) {
						map.put(key, variables.get(key));
					}
				}
				else {
					map.put(key, variables.get(key));
				}
			}
		}
		
		String text = renderTemplateText(tmpltVo.getBodyTemplate(), senderId, map);
		return text;
	}

	/**
	 * render a template by template text and client id.
	 * 
	 * @param templateText -
	 *            template text
	 * @param senderId -
	 *            client id
	 * @param variables -
	 *            variables
	 * @return rendered text
	 * @throws DataValidationException
	 * @throws ParseException
	 * @throws TemplateException 
	 */
	public String renderTemplateText(String templateText, String senderId,
			Map<String, RenderVariableVo> variables) throws
			ParseException, TemplateException {
		if (templateText == null || templateText.trim().length() == 0) {
			return templateText;
		}
		if (StringUtils.isBlank(senderId)) {
			senderId = Constants.DEFAULT_SENDER_ID;
		}
		
		Map<String, RenderVariableVo> map = new HashMap<String, RenderVariableVo>();

		List<GlobalVariable> globalList = globalVariableDao.getCurrent();
		for (Iterator<GlobalVariable> it = globalList.iterator(); it.hasNext();) {
			GlobalVariable vo = it.next();
			RenderVariableVo var = new RenderVariableVo(
					vo.getGlobalVariablePK().getVariableName(),
					vo.getVariableValue(),
					vo.getVariableFormat(),
					VariableType.getByValue(vo.getVariableType()),
					vo.getAllowOverride(),
					vo.isRequired());
			map.put(vo.getGlobalVariablePK().getVariableName(), var);
		}

		List<SenderVariable> clientList = null;
		if (senderId != null) {
			clientList = senderVariableDao.getCurrentBySenderId(senderId);
			for (Iterator<SenderVariable> it = clientList.iterator(); it.hasNext();) {
				SenderVariable vo = it.next();
				RenderVariableVo var = new RenderVariableVo(
						vo.getSenderVariablePK().getVariableName(),
						vo.getVariableValue(),
						vo.getVariableFormat(),
						VariableType.getByValue(vo.getVariableType()),
						vo.getAllowOverride(),
						vo.isRequired());
				if (map.containsKey(vo.getSenderVariablePK().getVariableName())) {
					RenderVariableVo v2 = map.get(vo.getSenderVariablePK().getVariableName());
					if (CodeType.YES_CODE.getValue().equalsIgnoreCase(v2.getAllowOverride())) {
						map.put(vo.getSenderVariablePK().getVariableName(), var);
					}
				}
				else {
					map.put(vo.getSenderVariablePK().getVariableName(), var);
				}
			}
		}

		if (variables != null) {
			Set<String> keys = variables.keySet();
			for (Iterator<String> it=keys.iterator(); it.hasNext(); ) {
				String key = it.next();
				if (map.containsKey(key)) {
					RenderVariableVo v2 = map.get(key);
					if (CodeType.YES_CODE.getValue().equalsIgnoreCase(v2.getAllowOverride())) {
						map.put(key, variables.get(key));
					}
				}
				else {
					map.put(key, variables.get(key));
				}
			}
		}
		
		Map<String, ErrorVariableVo> errors = new HashMap<String, ErrorVariableVo>();
		String text = Renderer.getInstance().render(templateText, map, errors);
		return text;
	}
	
}
