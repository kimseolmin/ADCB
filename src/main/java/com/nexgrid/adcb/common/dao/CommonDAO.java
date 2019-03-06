package com.nexgrid.adcb.common.dao;

import org.springframework.stereotype.Repository;

@Repository
public interface CommonDAO {
	
	// CTN 정상 여부
	public int getBlockCTN(String ctn) throws Exception;
	
	
	// 요금제 정상 여부
	public int getBlockFeeType(String feetype) throws Exception;

}
