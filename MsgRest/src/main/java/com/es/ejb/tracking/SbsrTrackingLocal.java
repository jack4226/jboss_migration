package com.es.ejb.tracking;

import java.util.List;

import javax.ejb.Local;

import jpa.model.BroadcastMessage;
import jpa.model.BroadcastTracking;

@Local
public interface SbsrTrackingLocal {

	public int updateOpenCount(int trkRowId);
	public int updateClickCount(int trkRowId);
	public int updateSentCount(int trkRowId);
	
	public int updateOpenCount(int emailAddrRowId, String listId);
	public int updateClickCount(int emailAddrRowId, String listId);
	
	public List<BroadcastMessage> getByMailingListId(String listId);
	public List<BroadcastTracking> getByBroadcastMessageRowId(int bcstMsgRowId);
}
