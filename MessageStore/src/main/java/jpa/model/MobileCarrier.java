package jpa.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name="mobile_carrier")
@XmlRootElement(name="mobileCarrier")
public class MobileCarrier extends BaseModel implements java.io.Serializable {
	private static final long serialVersionUID = 7097456266336019504L;
	
	@Column(nullable=false, length=20, unique=true)
	private String carrierId = "";
	@Column(nullable=false, length=50)
	private String carrierName = null;
	@Column(nullable=false, length=100)
	private String textAddress = "";
	@Column(nullable=true, length=100)
	private String multiMediaAddress = null;
	@Column(nullable=true, length=10)
	private String countryCode = null;

	public MobileCarrier() {
		// must have a no-argument constructor
	}

	public String getCarrierId() {
		return carrierId;
	}

	public void setCarrierId(String carrierId) {
		this.carrierId = carrierId;
	}

	public String getCarrierName() {
		return carrierName;
	}

	public void setCarrierName(String carrierName) {
		this.carrierName = carrierName;
	}

	public String getTextAddress() {
		return textAddress;
	}

	public void setTextAddress(String textAddress) {
		this.textAddress = textAddress;
	}

	public String getMultiMediaAddress() {
		return multiMediaAddress;
	}

	public void setMultiMediaAddress(String multiMediaAddress) {
		this.multiMediaAddress = multiMediaAddress;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}
}
