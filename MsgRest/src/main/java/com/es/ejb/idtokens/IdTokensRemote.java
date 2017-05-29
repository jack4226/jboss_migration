package com.es.ejb.idtokens;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

import javax.ejb.Remote;

@Remote
public interface IdTokensRemote {
	public Future<?> stayBusy(CountDownLatch ready);
	public jpa.model.IdTokens findBySenderId(String senderId);
	public List<jpa.model.IdTokens> findAll();
    public void insert(jpa.model.IdTokens idTokens);
    public void update(jpa.model.IdTokens idTokens) ;
    public int delete(String senderId);
}
