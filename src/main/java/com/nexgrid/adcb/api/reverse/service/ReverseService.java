package com.nexgrid.adcb.api.reverse.service;

import java.net.ConnectException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.nexgrid.adcb.api.reverse.dao.ReverseDAO;
import com.nexgrid.adcb.common.dao.CommonDAO;
import com.nexgrid.adcb.common.exception.CommonException;
import com.nexgrid.adcb.common.service.CommonService;
import com.nexgrid.adcb.common.vo.EaiVO;
import com.nexgrid.adcb.common.vo.LogVO;
import com.nexgrid.adcb.util.EnAdcbOmsCode;
import com.nexgrid.adcb.util.StringUtil;

@Service("reverseService")
public class ReverseService {

	@Inject
	private CommonDAO commonDAO;
	
	@Autowired
	private CommonService commonService;
	
	@Inject
	private ReverseDAO reverseDAO;
	
	
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
		
		logVO.setSid(chargeRequestId);
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
		
		
		
		if(payInfo == null) { // 거래내역 찾을 수 없을 경우에는 성공 메세지를 던져줘야 함.
			return false;
			//throw new CommonException(EnAdcbOmsCode.TRANSACTION_NOT_FOUND);
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
		
		
		// 이미 줬던 구매응답을 다시 준다.
		Map<String, Object> resMap = new HashMap<>();

		Map<String, Object> result = new HashMap<>();
		result.put("reasonCode", Integer.parseInt(payInfo.get("RESULT").toString()));
		result.put("message", payInfo.get("RESULT_MSG"));
		resMap.put("result", result);
		
		if(!EnAdcbOmsCode.SUCCESS.mappingCode().equals(payInfo.get("RESULT"))) { // 실패로 끝난 거래로 취소를 요청한 경우
			
			chargeResponse = false;
			
		}else {	// 성공적인 거래의 취소요청일 경우
			
			// 환불이 된 건인데 취소요청이 들어온 경우 -> 환불의 issuerRefundId를 reverse의 issuerReverseId로 줘야 한다.
			if(payInfo.get("BALANCE") != null && payInfo.get("REVERSE_DT") == null) {
				resMap.put("issuerPaymentId", payInfo.get("ISSUER_PAYMENTID"));
				paramMap.put("issuerPaymentId", payInfo.get("ISSUER_PAYMENTID"));
				String issuerRefundId;
				
				logVO.setFlow("[ADCB] --> [DB]");
				
				try {
					issuerRefundId = reverseDAO.getIssuerRefundId(paramMap);
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
				
				Map<String, Object> Refundresult = new HashMap<>();
				Refundresult.put("result", commonService.getSuccessResult());
				Refundresult.put("paymentResponse", resMap);
				Refundresult.put("issuerReverseId", issuerRefundId);
				
				paramMap.put("RefundRes", Refundresult);
				
				return false;
				//throw new CommonException(EnAdcbOmsCode.ALREADY_REFUNDED);
			}
			
			// 이미 취소가 된 경우
			if(payInfo.get("REVERSE_DT") != null) {
				Map<String, Object> duplicateRes = new HashMap<>();
				duplicateRes.put("issuerReverseId", payInfo.get("ISSUER_REVERSEID"));
				paramMap.put("duplicateRes", duplicateRes);
			}
			
			// 청소년요금제의 거래일 경우
			if("Y".equals(payInfo.get("YOUNG_FEE_YN"))) {
				throw new CommonException(EnAdcbOmsCode.REFUND_YOUNG);
			}
			
			resMap.put("issuerPaymentId", payInfo.get("ISSUER_PAYMENTID"));
			chargeResponse = true;
		}
		
		paramMap.put("paymentResponse", resMap);
		return chargeResponse;
	}
	
	
	
	/**
	 * EAI 연동
	 * @param paramMap
	 * @param logVO
	 * @throws Exception
	 */
	public void insertEAI(Map<String, Object> paramMap, LogVO logVO) throws Exception{
		
		Map<String, Object> payInfo = (Map<String, Object>) paramMap.get("payInfo");
		Map<String, String> reqCancel = (Map<String, String>) paramMap.get("Req_116");
		
		EaiVO eaiVO = new EaiVO();
		eaiVO.setNew_request_type("0");
		eaiVO.setNew_ban_unpaid_yn_code(payInfo.get("BAN_UNPAID_YN_CODE").toString());
		eaiVO.setNew_account_type("03");
		eaiVO.setNew_cust_grd_cd(payInfo.get("CUST_GRD_CD") == null ? "" : payInfo.get("CUST_GRD_CD").toString());
		eaiVO.setNew_prss_yymm(payInfo.get("START_USE_TIME").toString().substring(0, 6));
		eaiVO.setNew_request_date(new SimpleDateFormat("yyyyMMddHHmmssSSS").parse(reqCancel.get("END_USE_TIME")));
		eaiVO.setNew_total(payInfo.get("AMOUNT").toString());
		eaiVO.setNew_ban(payInfo.get("BAN").toString());
		eaiVO.setNew_ace_no(payInfo.get("ACE_NO").toString());
		eaiVO.setNew_subs_no(payInfo.get("CTN").toString());
		eaiVO.setNew_request_id(paramMap.get("chargeRequestId").toString());
		eaiVO.setNew_merchant_id(payInfo.get("MERCHANT_ID") == null ? "" : payInfo.get("MERCHANT_ID").toString());
		eaiVO.setNew_product_description(payInfo.get("PRODUCT_DESCRIPTION").toString());
		
		logVO.setFlow("[ADCB] --> [DB]");
		try {
			commonDAO.insertEAI(eaiVO);
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
	}
	
	
	
	/**
	 * Reverse
	 * @param paramMap
	 * @param logVO
	 * @throws Exception
	 */
	public void reverse(Map<String, Object> paramMap, LogVO logVO) throws Exception {
		
		Map<String, Object> payInfo = (Map<String, Object>)paramMap.get("payInfo");
		String svc_auth = payInfo.get("SVC_AUTH").toString(); // 부정사용자|장애인부가서비스|65세이상부가서비스
															// 입력정보: LRZ0001705|LRZ0003849|LRZ0003850
															// 출력정보: 0|1 (가입은 '1', 미가입은 '0')
		String handicapped = svc_auth.split("\\|")[1]; // 장애인부가서비스
		String old = svc_auth.split("\\|")[2]; // 65세이상부가서비스
		
		// 취약계층인지 판단하여 취약계층이면 대리인 정보 가져옴
		if("1".equals(handicapped) || "1".equals(old)) {
			paramMap.put("SUB_NO", payInfo.get("SUB_NO"));
			paramMap.put("CTN", payInfo.get("CTN"));
			String mode = "";	// 1:장애인처리, 2:65세이상처리
			if("1".equals(handicapped)) {
				mode = "1";
			}else {
				mode = "2";
			}
			paramMap.put("MODE", mode);
			// ESB 연동
			commonService.doEsbCm181(paramMap, logVO);
		}
		
		// 통합한도 연동: 차감취소
		commonService.doRbpCancel(paramMap, logVO);
	}
	
	
}
