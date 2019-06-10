package com.nexgrid.adcb.api.reverse.dao;

import java.util.Map;

import org.springframework.stereotype.Repository;

@Repository
public interface ReverseDAO {

	/**
	 * 환불된 지불에 대한 취소요청 시 필요한 issuerRefundId 가져오기
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	public String getIssuerRefundId(Map<String, Object> paramMap) throws Exception;
}
