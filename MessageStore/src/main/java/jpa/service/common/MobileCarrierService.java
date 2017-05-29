package jpa.service.common;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.model.MobileCarrier;
import jpa.repository.MobileCarrierRepository;

@Component("mobileCarrierService")
@Transactional(propagation=Propagation.REQUIRED)
public class MobileCarrierService implements java.io.Serializable {
	private static final long serialVersionUID = -2043525245286817626L;

	static Logger logger = Logger.getLogger(MobileCarrierService.class);
	
	@Autowired
	MobileCarrierRepository repository;
	
	public MobileCarrier getByCarrierId(String carrierId) {
		return repository.findOneByCarrierId(carrierId);
	}
	
	public MobileCarrier getByRowId(int rowId) {
		return repository.findOne(rowId);
	}
	
	public List<MobileCarrier> getAll() {
		return repository.findAll();
	}
	
	public void delete(MobileCarrier carrier) {
		if (carrier == null) return;
		repository.delete(carrier);;
	}

	public int deleteByCarrierId(String carrierId) {
		return repository.deleteByCarrierId(carrierId);
	}

	public int deleteByRowId(int rowId) {
		return repository.deleteByRowId(rowId);
	}

	public void insert(MobileCarrier carrier) {
		repository.saveAndFlush(carrier);
	}

	public void update(MobileCarrier carrier) {
		repository.saveAndFlush(carrier);
	}
}
