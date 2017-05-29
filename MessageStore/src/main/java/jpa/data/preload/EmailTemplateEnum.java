package jpa.data.preload;

import jpa.constant.MailingListDeliveryType;
import jpa.constant.MailingListType;

public enum EmailTemplateEnum implements EnumInterface {
	SampleNewsletter1(MailingListEnum.SMPLLST1,false,null,true,
			MailingListType.PERSONALIZED, MailingListDeliveryType.ALL_ON_LIST,
			"Sample newsletter to ${SubscriberAddress} with Open/Click/Unsubscribe tracking",
			"Dear ${SubscriberName},<p/>" + LF +
			"This is a sample newsletter message for a web-based mailing list. With a web-based " + LF +
			"mailing list, people who want to subscribe to the list must visit a web page and " + LF +
			"fill out a form with their email address. After submitting the form, they will " + LF +
			"receive a confirmation letter in their email and must activate the subscription " + LF +
			"by following the steps in the email (usually a simple click).<p/>" + LF +
			"Unsubscription information will be included in the newsletters they receive. People " + LF +
			"who want to unsubscribe can do so by simply following the steps in the newsletter.<p/>" + LF +
			"Date sent: ${CurrentDate} <p/>" + LF +
			"BroadcastMsgId: ${BroadcastMsgId}, ListId: ${MailingListId}, SubscriberAddressId: ${SubscriberAddressId}<p/>" + LF +
			"Contact Email: ${ContactEmailAddress}<p>" + LF +
			"<a target='_blank' href='$%7BWebSiteUrl%7D/SamplePromoPage.jsp?msgid=$%7BBroadcastMsgId%7D&listid=$%7BMailingListId%7D&sbsrid=$%7BSubscriberAddressId%7D'>Click here</a> to see our promotions<p/>" + LF +
			"${FooterWithUnsubLink}<br/>" +
			"${EmailOpenCountImgTag}"),
	
	SampleNewsletter2(MailingListEnum.SMPLLST2,false,null,true,
			MailingListType.TRADITIONAL, MailingListDeliveryType.ALL_ON_LIST,
			"Sample HTML newsletter to ${SubscriberAddress}",
			"Dear ${SubscriberAddress},<p/>" + LF +
			"This is a sample HTML newsletter message for a traditional mailing list. " + LF +
			"With a traditional mailing list, people who want to subscribe to the list " + LF +
			"must send an email from their account to the mailing list address with " + LF +
			"\"subscribe\" in the email subject.<p/>" + LF +
			"Unsubscribing from a traditional mailing list is just as easy; simply send " + LF +
			"an email to the mailing list address with \"unsubscribe\" in subject.<p/>" + LF + 
			"The mailing list address for this newsletter is: ${MailingListAddress}.<p/>" + LF +
			"Date this newsletter is sent: ${CurrentDate}.<p/>" + LF +
			"BroadcastMsgId: ${BroadcastMsgId}, ListId: ${MailingListId}<p/>" + LF +
			"Contact Email: ${ContactEmailAddress}<p/>" + LF +
			"<a target='_blank' href='$%7BWebSiteUrl%7D/SamplePromoPage.jsp?msgid=$%7BBroadcastMsgId%7D&listid=$%7BMailingListId%7D&sbsrid=$%7BSubscriberAddressId%7D'>Click here</a> to see our promotions<p/>" + LF +
			"${FooterWithUnsubAddr}<br/>" +
			"${EmailOpenCountImgTag}"),
	
	SampleNewsletter3(MailingListEnum.SMPLLST2,false,null,false,
			MailingListType.TRADITIONAL, MailingListDeliveryType.ALL_ON_LIST,
			"Sample Plain text newsletter to ${SubscriberAddress}",
			"Dear ${SubscriberAddress}," + LF + LF + 
			"This is a sample text newsletter message for a traditional mailing list." + LF +
			"With a traditional mailing list, people who want to subscribe to the list " + LF +
			"must send an email from their account to the mailing list address with " + LF +
			"\"subscribe\" in the email subject." + LF + LF + 
			"Unsubscribing from a traditional mailing list is just as easy; simply send " + LF +
			"an email to the mailing list address with \"unsubscribe\" in subject." + LF + LF +
			"Date sent: ${CurrentDate}" + LF + LF +
			"BroadcastMsgId: ${BroadcastMsgId}, ListId: ${MailingListId}" + LF + LF +
			"Contact Email: ${ContactEmailAddress}" + LF + LF +
			"To see our promotions, copy and paste the following link in your browser:" + LF +
			"${WebSiteUrl}/SamplePromoPage.jsp?msgid=${BroadcastMsgId}&listid=${MailingListId}&sbsrid=${SubscriberAddressId}" + LF +
			"${FooterWithUnsubAddr}"),
	
	SubscriptionConfirmation(MailingListEnum.SYSLIST1,true,false,true,
			MailingListType.TRADITIONAL, MailingListDeliveryType.ALL_ON_LIST,
			"Request for subscription confirmation",
			"Dear ${SubscriberAddress},<br/>" + LF +
			"This is an automatically generated message to confirm that you have " + LF +
			"submitted request to add your email address to the following mailing lists:<br/>" + LF +
			"<pre>${_RequestedMailingLists}</pre>" + LF +
			"If this is correct, please <a href='$%7BConfirmationURL%7D' target='_blank'>click here</a> " + LF +
			"to confirm your subscription.<br/>" + LF +
			"If this is incorrect, you do not need to do anything, simply delete this message.<p/>" + LF +
			"Thank you" + LF),
	
	SubscriptionWelcomeLetter(MailingListEnum.SYSLIST1,true,false,true,
			MailingListType.TRADITIONAL, MailingListDeliveryType.ALL_ON_LIST,
			"Your subscription has been confirmed",
			"Dear ${SubscriberAddress},<br/>" + LF +
			"Welcome to our mailing lists. Your email address has been added to the" + LF +
			"following mailing lists:<br/>" + LF +
			"<pre>${_SubscribedMailingLists}</pre>" + LF +
			"Please keep this email for latter reference.<p/>" + LF +
			"To unsubscribe please <a href='$%7BUnsubscribeURL%7D' target='_blank'>click here</a> " + LF +
			"and follow the steps.<br/>" + LF + LF +
			"To update your profile please <a href='$%7BUserProfileURL%7D' target='_blank'>click here</a>.<p/>" + LF +
			"Thank you<br/>" + LF),
	
	UnsubscriptionLetter(MailingListEnum.SYSLIST1,true,false,true,
			MailingListType.TRADITIONAL, MailingListDeliveryType.ALL_ON_LIST,
			"You have unsubscribed from our Newsletter",
			"Dear ${SubscriberAddress},<br/>" + LF +
			"Goodbye from our Newsletter, sorry to see you go.<br/>" + LF +
			"You have been unsubscribed from the following newsletters:<br/>" + LF +
			"<pre>${_UnsubscribedMailingLists}</pre>" + LF +
			"If this is an error, you can re-subscribe. Please " +
			"<a href='$%7BSubscribeURL%7D' target='_blank'>click here</a>" + LF +
			" and follow the steps.<p/>" + LF +
			"Thank you<br/>" + LF),
	
	UserProfileChangeLetter(MailingListEnum.SYSLIST1,true,null,true,
			MailingListType.PERSONALIZED, MailingListDeliveryType.ALL_ON_LIST,
			"[notify] Changes of user profile details",
			"Dear ${SubscriberName},<br/>" + LF +
			"This message is to inform you of a change of your user profile details" + LF +
			"on our newsletter database. You are currently subscribed to our following" + LF +
			"newsletters:<br/>" + LF +
			"<pre>${_SubscribedMailingLists}</pre>" + LF +
			"The information on our system for you is as follows:<br/>" + LF +
			"<pre>${_UserProfileData}</pre>" + LF +
			"If this is not correct, please update your information by " + LF +
			"<a href='$%7BUserProfileURL%7D' target='_blank'>visiting this web page</a>.<p/>" + LF +
			"Thank you<br/>" + LF),
	
	EmailAddressChangeLetter(MailingListEnum.SYSLIST1,true,null,true,
			MailingListType.PERSONALIZED, MailingListDeliveryType.ALL_ON_LIST,
			"[notify] Change of your email address",
			"Dear ${SubscriberName},<br/>" + LF +
			"When updating your user profile details, your email address has changed.<br/>" + LF +
			"Please confirm your new email address by " +
			"<a href='$%7BConfirmationURL%7D' target='_blank'>visiting this web page</a>.<br/>" + LF +
			"If this is not correct, " + LF +
			"<a href='$%7BUserProfileURL%7D' target='_blank'>click here</a> to update your information.<p/>" + LF +
			"Thank you<br/>" + LF),
	
	TellAFriendLetter(MailingListEnum.SYSLIST1,true,false,true,
			MailingListType.PERSONALIZED, MailingListDeliveryType.ALL_ON_LIST,
			"A web site recommendation from ${_ReferrerName}",
			"Dear ${_FriendsEmailAddress},<p/>" + LF +
			"${_ReferrerName}, whose email address is ${_ReferrerEmailAddress} thought you " + LF +
			"may be interested in this web page.<p/>" + LF +
			"<a target='_blank' href='$%7BWebSiteUrl%7D'>${WebSiteUrl}</a><p/>" + LF +
			"${_ReferrerName} has used our Tell-a-Friend form to send you this note.<p/>" + LF +
			"${_ReferrerComments}" +
			"We look forward to your visit!<br/>" + LF),
	
	SubscribeByEmailReply(MailingListEnum.SYSLIST1,true,null,false,
			MailingListType.TRADITIONAL, MailingListDeliveryType.ALL_ON_LIST,
			"You have subscribed to mailing list ${MailingListName}",
			"Dear ${SubscriberAddress}," + LF + LF +
			"This is an automatically generated message to confirm that you have" + LF +
			"subscribed to our mailing list: ${MailingListName}" + LF + LF +
			"To ensure that you continue to receive e-mails from ${DomainName} in your " + LF +
			"inbox, you can add the sender of this e-mail to your address book or white list." + LF + LF +
			"If this in incorrect, you can un-subscribe from this mailing list." + LF +
			"Simply send an e-mail to: ${MailingListAddress}" + LF +
			"with \"unsubscribe\" (no quotation marks) in your email subject." + LF),
	
	SubscribeByEmailReplyHtml(MailingListEnum.SYSLIST1,true,null,true,
			MailingListType.TRADITIONAL, MailingListDeliveryType.ALL_ON_LIST,
			"You have subscribed to ${MailingListName} at ${DomainName}",
			"Dear ${SubscriberAddress},<br>" + LF +
			"This is an automatically generated message to confirm that you have " + LF +
			"subscribed to our mailing list: <b>${MailingListName}</b>.<br>" + LF +
			"To ensure that you continue to receive e-mails from ${DomainName} in your " + LF +
			"inbox, you can add the sender of this e-mail to your address book or white list.<br>" + LF +
			"If you signed up for this subscription in error, you can un-subscribe." + LF +
			"Simply send an e-mail to <a href='mailto:$%7BMailingListAddress%7D' target='_blank'>${MailingListAddress}</a>" + LF +
			"with \"unsubscribe\" (no quotation marks) in your email subject.<br>" + LF),
	
	/*
	 * Email templates for production
	 */
	EmailsphereOrderReceipt(MailingListEnum.ORDER_LIST,false,null,false,
			MailingListType.PERSONALIZED, MailingListDeliveryType.ALL_ON_LIST,
			"Emailsphere Purchase Receipt",
			"Dear ${_BillingFirstName}," + LF + LF +
			"Thank you for your recent purchase from Emailsphere, your purchase, as described below, has been completed." + LF + LF +
			"Order number: ${_OrderNumber}" + LF +
			"Order Date: ${_OrderDate}" + LF + LF +
			"Billing Information:" + LF +
			"${_BillingName}" + LF +
			"${_BillingStreetAddress}" + LF +
			"${_BillingCityStateZip}" + LF + LF +
			"Item purchased: Emailsphere enterprise server." + LF + 
			"Price: ${_UnitPrice}" + LF +
			"Tax:   ${_Tax}" + LF +
			"Total Price: ${_TotalPrice}" + LF + LF +
			"Billed to ${_CardTypeName} ending in ${_CardNumberLast4}: ${_TotalPrice}" + LF + LF +
			"Please contact ${MailingListAddress} with any questions or concerns regarding this transaction." + LF + LF +
			"Your product key is: ${_ProductKey}" + LF +
			"Please login to your Emailsphere system management console, click \"Enter Product Key\", and copy this key to the input field and submit." + LF + LF +
			"If you have any technical questions, please visit our contact us page by point your browser to:" + LF +
			"${_ContactUsUrl}" + LF + LF +
			"Thank you for your purchase!" + LF + LF +
			"Emailsphere Team" + LF +
			"Legacy System Solutions, LLC" + LF),
	
	EmailsphereOrderException(MailingListEnum.ALERT_LIST,false,null,false,
			MailingListType.PERSONALIZED, MailingListDeliveryType.ALL_ON_LIST,
			"Important Notice: Your Emailsphere Order # ${_OrderNumber}",
			"Regarding Order ${_OrderNumber} you placed on ${_OrderDate} from Emailsphere.com" + LF +
			"1 Emailsphere Enterprise Server" + LF + LF +
			"Greetings from Emailsphere.com," + LF + LF +
			"Your credit card payment for the above transaction could not be completed." + LF +
			"An issuing bank will often decline an attempt to charge a credit card if" + LF +
			"the name, expiration date, or ZIP CodeType you entered at Emailsphere.com does" + LF +
			"not exactly match the bank's information." + LF + LF +
			"Valid payment information must be received within 3 days, otherwise your" + LF + 
			"order will be canceled." + LF + LF +
			"Once you have confirmed your account information with your issuing bank," + LF +
			"please follow the link below to resubmit your payment." + LF + LF +
			"http://www.emailsphere.com/es/edit.html/?orderID=${_OrderNumber}" + LF + LF +
			"We hope that you are able to resolve this issue promptly." + LF + LF +
			"Please note: This e-mail was sent from a notification-only address that" + LF +
			"cannot accept incoming e-mail. Please do not reply to this message." + LF + LF +
			"Thank you for shopping at Emailsphere.com." + LF + LF +
			"Emailsphere.com Subscriber Service" + LF +
			"http://www.emailsphere.com" + LF),
	
	EmailsphereInternalAlert(MailingListEnum.ALERT_LIST,false,null,false,
			MailingListType.TRADITIONAL, MailingListDeliveryType.ALL_ON_LIST,
			"Notify: Alert from Emailsphere.com",
			"Internal error or exception caught from Emailsphere.com" + LF + LF +
			"Time: ${_DateTime}" + LF +
			"Module: ${_ModuleName}" + LF +
			"Error: ${_Error}" + LF);
	
	private MailingListEnum mailingList;
	private boolean isBuiltin;
	private Boolean isEmbedEmailId;
	private boolean isHtml;
	private MailingListType listType;
	private MailingListDeliveryType deliveryType;
	private String subject;
	private String bodyText;
	private EmailTemplateEnum(MailingListEnum mailingList, boolean isBuiltin,
			Boolean isEmbedEmailId, boolean isHtml, MailingListType listType,
			MailingListDeliveryType deliveryType, String subject,
			String bodyText) {
		this.mailingList = mailingList;
		this.isBuiltin = isBuiltin;
		this.isEmbedEmailId = isEmbedEmailId;
		this.isHtml = isHtml;
		this.listType = listType;
		this.deliveryType = deliveryType;
		this.subject = subject;
		this.bodyText = bodyText;
	}
	public MailingListEnum getMailingList() {
		return mailingList;
	}
	public boolean isBuiltin() {
		return isBuiltin;
	}
	public Boolean getIsEmbedEmailId() {
		return isEmbedEmailId;
	}
	public boolean isHtml() {
		return isHtml;
	}
	public MailingListType getListType() {
		return listType;
	}
	public MailingListDeliveryType getDeliveryType() {
		return deliveryType;
	}
	public String getSubject() {
		return subject;
	}
	public String getBodyText() {
		return bodyText;
	}
}
