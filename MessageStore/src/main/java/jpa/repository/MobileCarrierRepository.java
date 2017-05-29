package jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jpa.model.MobileCarrier;

public interface MobileCarrierRepository extends JpaRepository<MobileCarrier, Integer> {
	
	public MobileCarrier findOneByCarrierId(String carrierId);
	
	@Modifying(clearAutomatically = true)
	@Query(value="delete from MobileCarrier t where t.carrierId=?1", nativeQuery=false)
	public int deleteByCarrierId(String carrierId);
	
	@Modifying(clearAutomatically = true)
	@Query(value="delete from MobileCarrier t where t.rowId=?1", nativeQuery=false)
	public int deleteByRowId(Integer rowId);

}
