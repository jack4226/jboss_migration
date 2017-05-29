package com.es.ejb.client;

import java.util.List;

import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.es.ejb.idtokens.IdTokensRemote;
import com.es.tomee.util.TomeeCtxUtil;

import jpa.util.PrintUtil;

public class IdTokensClient {
	static Logger logger = Logger.getLogger(IdTokensClient.class);
	
	public static void main(String[] args) {
		try {
			IdTokensClient client = new IdTokensClient();
			client.testIdTokens();
		}
		catch (Exception e) {
			logger.error("Exception", e);
		}
	}
	
	void testIdTokens() {
		IdTokensRemote id = null;
		Context ctx = null;
		try {
			ctx = TomeeCtxUtil.getRemoteContext();
			TomeeCtxUtil.listContext(ctx, "");
			id = (IdTokensRemote) ctx.lookup("ejb/IdTokens");
		}
		catch (NamingException e) {
			logger.error("NamingException", e);
			return;
		}

		// test EJB remote access
		logger.info("IdTokensRemote instance: " + id);
		List<jpa.model.IdTokens> idlist = id.findAll();
		for (jpa.model.IdTokens it : idlist) {
			logger.info(PrintUtil.prettyPrint(it, 1));
		}
	}
}
