package com.es.ejb.senderdata;
import java.util.List;

import javax.ejb.Local;

@Local
public interface SenderDataLocal {
	public jpa.model.SenderData findBySenderId(String senderId);
	public List<jpa.model.SenderData> findAll();
	public void insert(jpa.model.SenderData sender);
	public void update(jpa.model.SenderData sender);
	public int deleteBySenderId(String senderId);
}
