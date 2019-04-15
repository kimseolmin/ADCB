package com.nexgrid.adcb.common.dao;

import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.nexgrid.adcb.common.vo.EaiVO;
import com.nexgrid.adcb.common.vo.LogVO;

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
	
	
	
	/**
	 * SLA INSERT
	 * @param logVO
	 * @return
	 * @throws Exception
	 */
	public int slaInsert(@Param("param") Map<String, Object> paramMap, @Param("logVO") LogVO logVO) throws Exception;
	
	
	/**
	 * test phone 등록 여부 확인
	 * @param ctn
	 * @return
	 * @throws Exception
	 */
	public int testPhoneCheck(String ctn) throws Exception;
	
	
	/**
	 * 청구 API 요청 중복 체크 & PaymentStatus 가져오기
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	Map<String, String> reqDuplicateCheck(Map<String, Object> paramMap) throws Exception;
	
	
	
	/**
	 * 취소 및 환불 API에서 필요한 구매 정보 가져오기
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	Map<String, Object> getChargeInfo(Map<String, Object> paramMap) throws Exception;
	
	
	
	/**
	 * 환불 처리 누적 금액 & 환불후 잔액 UPDATE
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	int setBalance(@Param("param") Map<String, Object> paramMap) throws Exception;
	
	
	
	/**
	 * EAI INSERT
	 * @param eaiVO
	 * @return
	 * @throws Exception
	 */
	int insertEAI(EaiVO eaiVO) throws Exception;
	
}
