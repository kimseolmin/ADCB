package com.nexgrid.adcb.api.charge.dao;

import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChargeDAO {

	/**
	 * BOKU의 청구 API 최초 요청 데이터 INSERT
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	int insertChargeReq(@Param("param") Map<String, Object> paramMap) throws Exception;
	
	
	
	/**
	 * 결제 완료 또는 실패 정보 UPDATE
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	int updateChargeInfo(@Param("param") Map<String, Object> paramMap) throws Exception;

}
