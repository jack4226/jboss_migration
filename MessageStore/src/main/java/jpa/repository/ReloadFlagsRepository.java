package jpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import jpa.model.ReloadFlags;

public interface ReloadFlagsRepository extends JpaRepository<ReloadFlags, Integer> {
	
	public List<ReloadFlags> findTop3ByOrderByRowIdAsc();

}
