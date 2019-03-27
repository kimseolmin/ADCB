package com.nexgrid.adcb.common.dao;

import org.springframework.stereotype.Repository;

@Repository
public interface CommonDAO {
	

	/**
	 * CTN 정상 여부
	 * @param ctn
	 * @return 해당하는 ctn의 차단 건수
	 * @throws Exception
	 */
	public int getBlockCTN(String ctn) throws Exception;
	

	
	/**
	 * 요금제 정상 여부
	 * @param feetype
	 * @return 해당하는 요금제의 차단 건수
	 * @throws Exception
	 */
	public int getBlockFeeType(String feetype) throws Exception;

}
