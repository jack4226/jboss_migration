package com.es.ejb.tracking;

import javax.ejb.Remote;

@Remote
public interface SbsrTrackingRemote {

	public int updateOpenCount(int trkRowId);
	public int updateClickCount(int trkRowId);
	public int updateSentCount(int trkRowId);
	
	public int updateOpenCount(int emailAddrRowId, String listId);
	public int updateClickCount(int emailAddrRowId, String listId);
}
