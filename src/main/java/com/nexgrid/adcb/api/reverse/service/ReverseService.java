package com.nexgrid.adcb.api.reverse.service;

import java.net.ConnectException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.nexgrid.adcb.common.dao.CommonDAO;
import com.nexgrid.adcb.common.exception.CommonException;
import com.nexgrid.adcb.common.vo.LogVO;
import com.nexgrid.adcb.util.EnAdcbOmsCode;
import com.nexgrid.adcb.util.StringUtil;

@Service("reverseService")
public class ReverseService {

	@Inject
	private CommonDAO commonDAO;
	
	/**
	 * PaymentStatus 필수 Body값 체크
	 * @param paramMap
	 * @param logVO
	 * @throws Exception
	 */
	public void reqBodyCheck(Map<String, Object> paramMap, LogVO logVO) throws Exception {
		// body key 체크
		if( paramMap==null || paramMap.size() == 0 || !paramMap.containsKey("chargeRequestId") ) {
			throw new CommonException(EnAdcbOmsCode.INVALID_BODY_KEY);
		}
		
		// body value 체크
		String chargeRequestId = paramMap.get("chargeRequestId") == null ? "" : paramMap.get("chargeRequestId").toString();
		if( "".equals(chargeRequestId) || StringUtil.hasSpecialCharacter(chargeRequestId) || StringUtil.spaceCheck(chargeRequestId) || StringUtil.maxCheck(chargeRequestId, 50) ) {
			throw new CommonException(EnAdcbOmsCode.INVALID_BODY_VALUE);
		}
	}
	
	
	
	/**
	 * Reverse 요청이 유효한지 체크
	 * @param paramMap
	 * @param logVO
	 * @return true: 구매가 제대로 이루어진 경우(차감취소 해야함), false: 구매가 실패된 경우 
	 * @throws Exception
	 */
	public boolean reverseCheck(Map<String, Object> paramMap, LogVO logVO) throws Exception{
		
		boolean chargeResponse;
		Calendar cal = Calendar.getInstance();
		paramMap.put("month1", new SimpleDateFormat("yyyyMM").format(cal.getTime()));
		cal.add(Calendar.MONTH, -1);
		paramMap.put("month2", new SimpleDateFormat("yyyyMM").format(cal.getTime()));
		Map<String, Object> payInfo = null;
		
		logVO.setFlow("[ADCB] --> [DB]");
		
		try {
			payInfo = commonDAO.getChargeInfo(paramMap);
		}catch(DataAccessException adcbExc){
			/*SQLException se = (SQLException) adcbExc.getRootCause();
			logVO.setRsCode(Integer.toString(se.getErrorCode()));*/
			
			throw new CommonException(EnAdcbOmsCode.DB_ERROR, adcbExc.getMessage());
		}catch(ConnectException adcbExc) {
			throw new CommonException(EnAdcbOmsCode.DB_CONNECT_ERROR, adcbExc.getMessage());
		}catch (Exception adcbExc) {
			throw new CommonException(EnAdcbOmsCode.DB_INVALID_ERROR, adcbExc.getMessage());
		}
		logVO.setFlow("[ADCB] <-- [DB]");
		
		
		// 거래내역 찾을 수 없음
		if(payInfo == null) {
			throw new CommonException(EnAdcbOmsCode.TRANSACTION_NOT_FOUND);
		}else {
			//paramMap에 pay정보 저장
			paramMap.put("payInfo", payInfo);
		}
		
		// 거래 후 24시간 이내로 들어온 요청인지 체크
		Date payDt = (Date)payInfo.get("REG_DT");
		cal.setTime(payDt);
		cal.add(Calendar.DATE, 1);
		Date checkDt = cal.getTime();
		if(checkDt.compareTo(new Date()) < 0) {
			throw new CommonException(EnAdcbOmsCode.REVERSE_WINDOW_EXPIRED);
		}
		
		// 응답을 주지 못한 거래로 취소 요청이 들어온 경우
		if(payInfo.get("RESULT") == null) {
			throw new CommonException(EnAdcbOmsCode.TRANSACTION_RESPONSE_FAIL);
		}
		
		
		// 이미 줬던 응답을 다시 준다.
		Map<String, Object> resMap = new HashMap<>();

		Map<String, Object> result = new HashMap<>();
		result.put("reasonCode", Integer.parseInt(payInfo.get("RESULT").toString()));
		result.put("message", payInfo.get("RESULT_MSG"));
		resMap.put("result", result);
		
		if(!EnAdcbOmsCode.SUCCESS.mappingCode().equals(payInfo.get("RESULT"))) { // 실패로 끝난 거래로 취소를 요청한 경우
			
			chargeResponse = false;
			
		}else {	// 성공적인 거래의 취소요청일 경우
			
			// 환불이 된 건인데 취소요청이 들어온 경우
			if(payInfo.get("BALANCE") != null && payInfo.get("REVERSE_DT") == null) {
				throw new CommonException(EnAdcbOmsCode.ALREADY_REFUNDED);
			}
			
			// 이미 취소가 된 경우
			if(payInfo.get("REVERSE_DT") != null) {
				throw new CommonException(EnAdcbOmsCode.ALREADY_REVERSED);
			}
			
			resMap.put("issuerPaymentId", payInfo.get("ISSUER_PAYMENTID"));
			chargeResponse = true;
		}
		
		paramMap.put("paymentResponse", resMap);
		return chargeResponse;
	}
	
	
	
	
	
}
