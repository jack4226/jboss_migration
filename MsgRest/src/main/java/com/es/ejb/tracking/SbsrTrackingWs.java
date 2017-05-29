package com.es.ejb.tracking;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

@WebService (targetNamespace = "http://com.es.ws.sbsrtracking/wsdl")
public interface SbsrTrackingWs {

	@WebMethod
	@WebResult(name="rowsUpdated")
	public int updateOpenCount(@WebParam(name="trackingId") int trkRowId);
	@WebMethod
	@WebResult(name="rowsUpdated")
	public int updateClickCount(@WebParam(name="trackingId") int trkRowId);
	
	@WebMethod
	@WebResult(name="rowsUpdated")
	public int updateMsgOpenCount(@WebParam(name="emailAddrId") int emailAddrRowId, @WebParam(name="listId") String listId);
	@WebMethod
	@WebResult(name="rowsUpdated")
	public int updateMsgClickCount(@WebParam(name="emailAddrId") int emailAddrRowId, @WebParam(name="listId") String listId);	
}
