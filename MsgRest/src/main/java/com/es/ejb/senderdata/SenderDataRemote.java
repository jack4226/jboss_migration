package com.es.ejb.senderdata;
import java.util.List;

import javax.ejb.Remote;

@Remote
public interface SenderDataRemote {
	public jpa.model.SenderData findBySenderId(String senderId);
	public List<jpa.model.SenderData> findAll();
}
