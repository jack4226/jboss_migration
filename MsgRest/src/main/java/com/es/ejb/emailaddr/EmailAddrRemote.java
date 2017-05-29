package com.es.ejb.emailaddr;
import javax.ejb.Remote;

import jpa.constant.StatusId;
import jpa.model.EmailAddress;

@Remote
public interface EmailAddrRemote {
	public EmailAddress findSertAddress(String address);
	public int delete(String address);
	public void updateStatus(String address, StatusId status);
}
