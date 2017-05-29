package jpa.model;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name="reload_flags")
/*
 * XXX !!!!! Rollback will not work since this table is defined with MyISAM engine
 */
@XmlRootElement(name="reloadFlags")
public class ReloadFlags extends BaseModel {	
	private static final long serialVersionUID = -5657762883755527124L;

	private int senders = 0;
	private int rules = 0;
	private int actions = 0;
	private int templates = 0;
	private int schedules = 0;
	
	public ReloadFlags() {
		// must have a no-argument constructor
	}

	public Integer getRowId() {
		return rowId;
	}

	public int getSenders() {
		return senders;
	}
	public void setSenders(int senders) {
		this.senders = senders;
	}
	public int getRules() {
		return rules;
	}
	public void setRules(int rules) {
		this.rules = rules;
	}
	public int getActions() {
		return actions;
	}
	public void setActions(int actions) {
		this.actions = actions;
	}
	public int getTemplates() {
		return templates;
	}
	public void setTemplates(int templates) {
		this.templates = templates;
	}
	public int getSchedules() {
		return schedules;
	}
	public void setSchedules(int schedules) {
		this.schedules = schedules;
	}
}
