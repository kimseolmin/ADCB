package com.nexgrid.adcb.common.service;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;

import com.nexgrid.adcb.common.vo.LogVO;
import com.nexgrid.adcb.common.vo.SmsSendVO;



public interface CommonService {

	String getIpAddr(HttpServletRequest request);
	
	/**
	 * Header 필수값 체크
	 * @param request
	 * @param logVO
	 * @throws Exception
	 */
	void contentTypeCheck(HttpServletRequest request, LogVO logVO) throws Exception;
	
	
	/**
	 * NCAS 연동
	 * @param paramMap
	 * @param logVO
	 * @throws Exception
	 */
	void getNcasGetMethod(Map<String, Object> paramMap, LogVO logVO) throws Exception;
	
	
	/**
	 * NCAS 헤더 응답값을 MAP으로 반환.
	 * @param responseEntity NCAS 응답 Entity
	 * @return
	 * @throws Exception
	 */
	Map<String,String> getNcasResHeader(ResponseEntity<String> responseEntity) throws Exception;

	/**
	 * OMS log write
	 * @param logVO
	 */
	void omsLogWrite(LogVO logVO);
	
	
	/**
	 * BOKU 성공 응답값 반환
	 * @return
	 * @throws Exception
	 */
	Map<String,Object> getSuccessResult() throws Exception;
	
	
	/**
	 * 사용자 청구 자격 체크
	 * @param paramMap
	 * @param logVO
	 * @return
	 * @throws Exception
	 */
	boolean userEligibilityCheck(Map<String, Object> paramMap, LogVO logVO) throws Exception;
	
	
	/**
	 * 통합한도 연동: 한도조회 -> 사용자 등급 체크 (7등급이면 차단)
	 * @param paramMap
	 * @param logVO
	 * @return
	 * @throws Exception
	 */
	boolean userGradeCheck(Map<String, Object> paramMap, LogVO logVO) throws Exception;
	
	
	/**
	 * body에 msisdn만 오는 경우 필수값 체크
	 * @param paramMap
	 * @param logVO
	 * @throws Exception
	 */
	void reqBodyCheck(Map<String, Object> paramMap, LogVO logVO) throws Exception;
	
	
	
	/**
	 * SLA INSERT
	 * @param logVO
	 * @return
	 * @throws Exception
	 */
	void insertSLA(Map<String, Object> paramMap, LogVO logVO) throws Exception;
	
	
	
	/**
	 * ESB 연동 - 취약계층인지를 판단하여 취약계층이면 ESB연동하여 연동결과를 esbCm181Res라는 이름으로 paramMap에 저장
	 * @param paramMap
	 * @param logVO
	 * @throws Exception
	 */
	void doEsbCm181(Map<String, Object> paramMap, LogVO logVO) throws Exception;
	
	
	/**
	 * ESB TransactionId
	 * @return String ESB TransactionId
	 */
	String getEsbTransactionId();
	
	
	
	/**
	 * 통합한도 연동: 차감취소
	 * @param paramMap
	 * @param logVO
	 * @throws Exception
	 */
	void doRbpCancel(Map<String, Object> paramMap, LogVO logVO) throws Exception;
	
	
	
	 /**
	  * 환불 처리 누적 금액 & 환불후 잔액 UPDATE
	  * @param paramMap
	  * @param logVO
	  * @throws Exception
	  */
	void setBalance(Map<String, Object> paramMap, LogVO logVO) throws Exception;
	
		

	/**
	 * 결제 성공,취소 또는 환불 성공시 SMS 정보 추가
	 * @param paramMap
	 * @param contentType contentType config에 있는 message의 content type
	 * @param ctn sms 수신자
	 * @return
	 * @throws Exception
	 */
	SmsSendVO addSmsInfo(Map<String, Object> paramMap, String contentType, String ctn) throws Exception;
	
	
	
	/**
	 * 결제 취소 시 SMS 발송
	 * @param paramMap
	 * @throws Exception
	 */
	void addCancelSuccessSMS(Map<String, Object> paramMap) throws Exception;
	
	
	
	/**
	 * SMS 정보 리스트 INSERT
	 * @param paramMap
	 * @param logVO
	 * @throws Exception
	 */
	void insertSmsList(Map<String, Object> paramMap, LogVO logVO) throws Exception;
	
	
	
	/**
	 * 작업공지 체크
	 * @throws Exception
	 */
	void maintenanceCheck() throws Exception;
	
	
}
