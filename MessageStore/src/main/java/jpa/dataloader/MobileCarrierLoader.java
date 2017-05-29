package jpa.dataloader;

import jpa.constant.MobileCarrierEnum;
import jpa.model.MobileCarrier;
import jpa.service.common.MobileCarrierService;
import jpa.spring.util.SpringUtil;

import org.apache.log4j.Logger;

public class MobileCarrierLoader extends AbstractDataLoader {
	static final Logger logger = Logger.getLogger(MobileCarrierLoader.class);
	private MobileCarrierService service;

	public static void main(String[] args) {
		MobileCarrierLoader loader = new MobileCarrierLoader();
		loader.loadData();
	}

	@Override
	public void loadData() {
		service = SpringUtil.getAppContext().getBean(MobileCarrierService.class);
		startTransaction();
		try {
			loadMobileCarriers();
		} catch (Exception e) {
			logger.error("Exception caught", e);
		}
		finally {
			commitTransaction();
		}
	}

	private void loadMobileCarriers() {
		for (MobileCarrierEnum mc : MobileCarrierEnum.values()) {
			MobileCarrier data = new MobileCarrier();
			data.setCarrierId(mc.name());
			data.setCarrierName(mc.getValue());
			data.setTextAddress(mc.getText());
			data.setMultiMediaAddress(mc.getMmedia());
			data.setCountryCode(mc.getCountry());
			service.insert(data);
		}
		logger.info("EntityManager persisted the record.");
	}
}

