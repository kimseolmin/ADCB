package com.nexgrid.adcb.api.paymentStatus.service;

import java.net.ConnectException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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

@Service("paymentStatusService")
public class PaymentStatusService {
	
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
		if( paramMap==null || paramMap.size() == 0 || !paramMap.containsKey("paymentRequestId") ) {
			throw new CommonException(EnAdcbOmsCode.INVALID_BODY_KEY);
		}
		
		// body value 체크
		String paymentRequestId = paramMap.get("paymentRequestId") == null ? "" : paramMap.get("paymentRequestId").toString();
		if( "".equals(paymentRequestId) || StringUtil.hasSpecialCharacter(paymentRequestId) || StringUtil.spaceCheck(paymentRequestId) || StringUtil.maxCheck(paymentRequestId, 50) ) {
			throw new CommonException(EnAdcbOmsCode.INVALID_BODY_VALUE);
		}
	}
	
	
	
	/**
	 * payment 상태 가져오기
	 * @param paramMap
	 * @param logVO
	 * @throws Exception
	 */
	public void getPaymentStatus(Map<String, Object> paramMap, LogVO logVO) throws Exception {
		paramMap.put("requestId", paramMap.get("paymentRequestId"));
		Calendar cal = Calendar.getInstance();
		paramMap.put("current_month", new SimpleDateFormat("yyyyMM").format(cal.getTime()));
		cal.add(Calendar.MONTH, -1);
		paramMap.put("last_month", new SimpleDateFormat("yyyyMM").format(cal.getTime()));
		Map<String, String> paymentStatus = null;
		
		logVO.setFlow("[ADCB] --> [DB]");
		
		try {
			paymentStatus = commonDAO.reqDuplicateCheck(paramMap);
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
		
		
		if(paymentStatus != null) {
			if(paymentStatus.get("ISSUER_PAYMENTID") != null) { // 요청에 대한 응답이 있었을 경우 
				// 이미 줬던 응답을 다시 준다.
				Map<String, Object> resMap = new HashMap<>();
				resMap.put("issuerPaymentId", paymentStatus.get("ISSUER_PAYMENTID"));
				
				Map<String, Object> result = new HashMap<>();
				result.put("reasonCode", Integer.parseInt(paymentStatus.get("RESULT")));
				result.put("message", paymentStatus.get("RESULT_MSG"));
				resMap.put("result", result);
				
				paramMap.put("paymentResponse", resMap);
			}else {
				throw new CommonException(EnAdcbOmsCode.UNKNOWN_STATUS);
			}
		}else {
			throw new CommonException(EnAdcbOmsCode.TRANSACTION_NOT_FOUND);
		}
		
		
		
		
	}

}
