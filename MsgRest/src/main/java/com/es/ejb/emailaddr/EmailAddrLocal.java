package com.es.ejb.emailaddr;
import javax.ejb.Local;

import jpa.constant.StatusId;
import jpa.model.EmailAddress;

@Local
public interface EmailAddrLocal {
	public EmailAddress findSertAddress(String address);
	public int delete(String address);
	public void updateStatus(String address, StatusId status);
}
