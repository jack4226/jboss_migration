package jpa.dataloader;

import jpa.spring.util.SpringUtil;
import jpa.util.JpaUtil;

public class DataLoader {

	public static void main(String[] args) {
		DataLoader loader = new DataLoader();
		AlterConstraints alter = new AlterConstraints();
		try {
			loader.loadAllTables();
			if (JpaUtil.isMySQLDatabase() || JpaUtil.isEclipseLink()) {
				alter.executeQueries();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
	
	public DataLoader() {
		// to trigger JPA to generate DDL (create tables, etc.)
		SpringUtil.getDaoAppContext(); //.refresh();
	}
	
	public void loadAllTables() {
		new SenderDataLoader().loadData();
		new IdTokensDataLoader().loadData();
		new EmailAddressLoader().loadData();
		new UserDataLoader().loadData();
		new SubscriberDataLoader().loadData();
		new VariableDataLoader().loadData();
		new MailingListDataLoader().loadData();
		new EmailVariableLoader().loadData();
		new EmailTemplateLoader().loadData();
		new RuleDataLoader().loadData();
		new RuleActionLoader().loadData();
		new TemplateDataLoader().loadData();
		new MessageFolderLoader().loadData();
		new MessageInboxLoader().loadData();
		new MobileCarrierLoader().loadData();
		new MessageRenderedLoader().loadData();
		new BroadcastDataLoader().loadData();
		new MailInboxLoader().loadData();
		new SmtpServerLoader().loadData();
	}
}
