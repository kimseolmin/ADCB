package com.nexgrid.adcb.api.refund.service;

import java.net.ConnectException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.nexgrid.adcb.api.refund.dao.RefundDAO;
import com.nexgrid.adcb.common.exception.CommonException;
import com.nexgrid.adcb.common.vo.LogVO;
import com.nexgrid.adcb.util.EnAdcbOmsCode;
import com.nexgrid.adcb.util.StringUtil;

@Service("refundService")
public class RefundService {
	
	private static final Logger logger = LoggerFactory.getLogger(RefundService.class);
	
	@Inject
	private RefundDAO refundDAO;
	
	/**
	 * Refund API body 필수값 체크
	 * @param paramMap
	 * @param logVO
	 * @throws Exception
	 */
	public void reqBodyCheck(Map<String, Object> paramMap, LogVO logVO) throws Exception {
		// body key 체크
		if( paramMap == null || paramMap.size() == 0 || !paramMap.containsKey("requestId")
				|| !paramMap.containsKey("clientRefundId") || !paramMap.containsKey("issuerPaymentId") 
				|| !paramMap.containsKey("refundAmount") ) {
			throw new CommonException(EnAdcbOmsCode.INVALID_BODY_KEY);
		}
		Map<String, Object> refundAmount = (HashMap<String, Object>)paramMap.get("refundAmount");
		if( refundAmount == null || refundAmount.size() == 0 || !refundAmount.containsKey("amount")
				|| !refundAmount.containsKey("currency")) {
			throw new CommonException(EnAdcbOmsCode.INVALID_BODY_KEY);
		}
		
		// body value 체크
		String requestId = paramMap.get("requestId") == null ? "" : paramMap.get("requestId").toString();
		String clientRefundId = paramMap.get("clientRefundId") == null ? "" : paramMap.get("clientRefundId").toString();
		String issuerPaymentId = paramMap.get("issuerPaymentId") == null ? "" : paramMap.get("issuerPaymentId").toString();
		Integer amount = refundAmount.get("amount") == null ? 0 : (Integer)refundAmount.get("amount");
		String currency = refundAmount.get("currency") == null ? "" : refundAmount.get("currency").toString().toUpperCase();
		if( "".equals(requestId) || StringUtil.hasSpecialCharacter(requestId) || StringUtil.spaceCheck(requestId) || StringUtil.maxCheck(requestId, 50)
				||  "".equals(clientRefundId) || StringUtil.hasSpecialCharacter(clientRefundId) || StringUtil.spaceCheck(clientRefundId) || StringUtil.maxCheck(clientRefundId, 50) 
				||  "".equals(issuerPaymentId) || StringUtil.hasSpecialCharacter(issuerPaymentId) || StringUtil.spaceCheck(issuerPaymentId) || StringUtil.maxCheck(issuerPaymentId, 50)
				||  amount == 0 || !"KRW".equals(currency)) {
			throw new CommonException(EnAdcbOmsCode.INVALID_BODY_VALUE);
		}
	}
	
	
	
	/**
	 * REFUND API 중복 요청 체크
	 * @param paramMap
	 * @param logVO
	 * @return
	 * @throws Exception
	 */
	public boolean reqDuplicateCheck(Map<String, Object> paramMap, LogVO logVO) throws Exception{
		
		Calendar cal = Calendar.getInstance();
		paramMap.put("current_month", new SimpleDateFormat("yyyyMM").format(cal.getTime()));
		cal.add(Calendar.MONTH, -1);
		paramMap.put("last_month", new SimpleDateFormat("yyyyMM").format(cal.getTime()));
		Map<String, String> refundReq = null;
		
		logVO.setFlow("[ADCB] --> [DB]");
		
		try {
			refundReq = refundDAO.reqDuplicateCheck(paramMap);
		}catch(DataAccessException adcbExc){
			SQLException se = (SQLException) adcbExc.getRootCause();
			logVO.setRsCode(Integer.toString(se.getErrorCode()));
			
			throw new CommonException(EnAdcbOmsCode.DB_ERROR, se.getMessage());
		}catch(ConnectException adcbExc) {
			throw new CommonException(EnAdcbOmsCode.DB_CONNECT_ERROR, adcbExc.getMessage());
		}catch (Exception adcbExc) {
			throw new CommonException(EnAdcbOmsCode.DB_INVALID_ERROR, adcbExc.getMessage());
		}
		logVO.setFlow("[ADCB] <-- [DB]");
		
		// 중복이 아닐 경우 메소드 종료
		if(refundReq == null) {
			return false;
		}else {
			if(refundReq.get("ISSUER_REFUNDID") != null) { // 요청에 대한 응답이 있었을 경우 
				// 이미 줬던 응답을 다시 준다.
				Map<String, Object> resMap = new HashMap<>();
				resMap.put("issuerRefundId", refundReq.get("ISSUER_REFUNDID"));
				
				Map<String, Object> result = new HashMap<>();
				result.put("reasonCode", Integer.parseInt(refundReq.get("RESULT")));
				result.put("message", refundReq.get("RESULT_MSG"));
				resMap.put("result", result);
				
				paramMap.put("duplicateRes", resMap);
				paramMap.put("http_status", refundReq.get("HTTP_STATUS"));
				
				return true;
			}else {
				throw new CommonException(EnAdcbOmsCode.REFUND_DUPLICATE_REQ);
			}
		}
	}
	
	
	
	/**
	 * BOKU의 환불 API 최초 요청 데이터 INSERT
	 * @param paramMap
	 * @param logVO
	 * @throws Exception
	 */
	public void insertRefundReq(Map<String, Object> paramMap, LogVO logVO) throws Exception {
		logVO.setFlow("[ADCB] --> [DB]");
		try {
			refundDAO.insertRefundReq(paramMap);
		}catch(DataAccessException adcbExc){
			SQLException se = (SQLException) adcbExc.getRootCause();
			logVO.setRsCode(Integer.toString(se.getErrorCode()));
			
			throw new CommonException(EnAdcbOmsCode.DB_ERROR, se.getMessage());
		}catch(ConnectException adcbExc) {
			throw new CommonException(EnAdcbOmsCode.DB_CONNECT_ERROR, adcbExc.getMessage());
		}catch (Exception adcbExc) {
			throw new CommonException(EnAdcbOmsCode.DB_INVALID_ERROR, adcbExc.getMessage());
		}
		logVO.setFlow("[ADCB] <-- [DB]");
	}
	
	
	/**
	 * Refund
	 * @param paramMap
	 * @param logVO
	 * @throws Exception
	 */
	public void refund(Map<String, Object> paramMap, LogVO logVO) throws Exception {
		
		Map<String, String> ncasRes = (HashMap<String,String>) paramMap.get("ncasRes");
		String young_fee_yn = ncasRes.get("YOUNG_FEE_YN"); // 실시간과금대상요금제(RCSG연동대상)
														// 실시간과금대상요금제에 가입되어있는 경우 'Y', 미가입은 'N'
		
		// 청소년 요금제는 차감 취소가 안됨.
		if("Y".equals(young_fee_yn)) {
			throw new CommonException(EnAdcbOmsCode.REFUND_YOUNG);
		}
		
	}

}
