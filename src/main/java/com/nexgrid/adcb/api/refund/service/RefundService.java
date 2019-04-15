package com.nexgrid.adcb.api.refund.service;

import java.math.BigDecimal;
import java.net.ConnectException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.nexgrid.adcb.api.refund.dao.RefundDAO;
import com.nexgrid.adcb.common.dao.CommonDAO;
import com.nexgrid.adcb.common.exception.CommonException;
import com.nexgrid.adcb.common.service.CommonService;
import com.nexgrid.adcb.common.vo.EaiVO;
import com.nexgrid.adcb.common.vo.LogVO;
import com.nexgrid.adcb.interworking.rbp.service.RbpClientService;
import com.nexgrid.adcb.interworking.rbp.util.RbpKeyGenerator;
import com.nexgrid.adcb.interworking.rcsg.service.RcsgClientService;
import com.nexgrid.adcb.util.EnAdcbOmsCode;
import com.nexgrid.adcb.util.Init;
import com.nexgrid.adcb.util.StringUtil;

@Service("refundService")
public class RefundService {
	
	private static final Logger logger = LoggerFactory.getLogger(RefundService.class);
	
	@Autowired
	private RbpClientService rbpClientService;
	
	@Inject
	private RefundDAO refundDAO;
	
	@Autowired
	private CommonService commonService;
	
	@Inject
	private CommonDAO commonDAO;
	
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
				||  "".equals(issuerPaymentId) || StringUtil.spaceCheck(issuerPaymentId) || StringUtil.maxCheck(issuerPaymentId, 50)
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
			/*SQLException se = (SQLException) adcbExc.getRootCause();
			logVO.setRsCode(Integer.toString(se.getErrorCode()));*/
			
			throw new CommonException(EnAdcbOmsCode.DB_ERROR, adcbExc.getMessage());
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
				paramMap.put("http_status", refundReq.get("http_status"));
				
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
	 * 환불 유효기간 및 부분처리 체크
	 * @param paramMap
	 * @param logVO
	 * @throws Exception
	 */
	public void partialRefundCheck(Map<String, Object> paramMap, LogVO logVO) throws Exception {
		
		// 환불 유효기간 만료
		Calendar cal = Calendar.getInstance();
		Date payDate = new SimpleDateFormat("yyyyMMddHHmmssSSS").parse(paramMap.get("issuerPaymentId").toString().substring(0,17));
		cal.setTime(payDate);
		cal.add(Calendar.YEAR, 1);
		Date payNextYear = cal.getTime();
		if( payNextYear.compareTo(new Date()) < 0 ) {
			throw new CommonException(EnAdcbOmsCode.REFUND_WINDOW_EXPIRED);
		}
		
		
		// 우리가 준 seqId의 날짜로 거래내역 SELECT
		String payMonth = paramMap.get("issuerPaymentId").toString().substring(0,6);
		paramMap.put("month1", payMonth);
		cal.add(Calendar.YEAR, -1);
		cal.add(Calendar.MONTH, 1);
		String payNextMonth = new SimpleDateFormat("yyyyMM").format(cal.getTime());
		paramMap.put("month2", payNextMonth);
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
		
		// 응답을 주지 못한 거래로 환불 요청이 들어온 경우
		if(payInfo.get("RESULT") == null) {
			throw new CommonException(EnAdcbOmsCode.TRANSACTION_RESPONSE_FAIL);
		}
		
		// 실패로 끝난 거래로 환불을 요청한 경우
		if(!"0".equals(payInfo.get("RESULT"))) { 
			throw new CommonException(EnAdcbOmsCode.TRANSACTION_FAIL);
		}
		
		// 청소년 요금제의 거래로 환불을 요청한 경우
		if("Y".equals(payInfo.get("YOUNG_FEE_YN"))) {
			throw new CommonException(EnAdcbOmsCode.REFUND_YOUNG);
		}
		
		Map<String, Object> refundAmount = (HashMap<String, Object>)paramMap.get("refundAmount");
		int refund = (Integer)refundAmount.get("amount");
		
		if(payInfo.get("BALANCE") != null) { // 부분 환불이 있었을 경우
			
			int balance =  ((BigDecimal)payInfo.get("BALANCE")).intValue();
			
			// 이미 완전히 환불되었을 경우
			if(balance == 0) {
				throw new CommonException(EnAdcbOmsCode.ALREADY_REFUNDED);
			}
			
			// 잔액보다 요청 환불금액이 큰 경우
			if(balance < refund) {
				throw new CommonException(EnAdcbOmsCode.EXCEED_ORIGINAL_AMOUNT);
			}
		}else {	
			// 결제 금액 보다 환불요청금액이 클 경우 
			int amount =  ((BigDecimal)payInfo.get("AMOUNT")).intValue();
			if(amount < refund) {
				throw new CommonException(EnAdcbOmsCode.EXCEED_ORIGINAL_AMOUNT);
			}
		}
		
		
		
	}
	
	
	
	/**
	 * Refund
	 * @param paramMap
	 * @param logVO
	 * @throws Exception
	 */
	public void refund(Map<String, Object> paramMap, LogVO logVO) throws Exception {
		
		Map<String, Object> payInfo = (Map<String, Object>)paramMap.get("payInfo");
		String svc_auth = payInfo.get("SVC_AUTH").toString(); // 부정사용자|장애인부가서비스|65세이상부가서비스
															// 입력정보: LRZ0001705|LRZ0003849|LRZ0003850
															// 출력정보: 0|1 (가입은 '1', 미가입은 '0')
		String handicapped = svc_auth.split("|")[1]; // 장애인부가서비스
		String old = svc_auth.split("|")[2]; // 65세이상부가서비스
		
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
//			commonService.doEsbCm181(paramMap, logVO);
		}
		
		// 통합한도 연동: 차감취소
		int payAmount = ((BigDecimal)payInfo.get("AMOUNT")).intValue();
		Map<String, Object> refundAmount = (HashMap<String, Object>)paramMap.get("refundAmount");
		int refundamount = (Integer)refundAmount.get("amount");
		
		if(payAmount == refundamount) {	// 전체 환불일 경우
			commonService.doRbpCancel(paramMap, logVO);
		}else { // 부분 환불일 경우
			doRbpCancelPart(paramMap, logVO);
		}
		
		
		
		
	}
	
	
	/**
	 * 환불 완료 또는 실패 정보 UPDATE
	 * @param paramMap
	 * @param logVO
	 * @throws Exception
	 */
	 public void updateRefundInfo(Map<String, Object> paramMap, LogVO logVO) throws Exception{
			// update를 위한 data
			paramMap.put("REFUND_DT", new Date()); // 환불완료일시
			
			logVO.setFlow("[ADCB] --> [DB]");
			try {
				refundDAO.updateRefundInfo(paramMap);
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
	  * 부분취소 (RBP연동)
	  * @param paramMap
	  * @param logVO
	  * @throws Exception
	  */
	 public void doRbpCancelPart(Map<String, Object> paramMap, LogVO logVO) throws Exception {
		 
		 Map<String, Object> payInfo = (Map<String, Object>)paramMap.get("payInfo");
		 String ctn = StringUtil.getCtn344(payInfo.get("CTN").toString());
		 String fee_type = payInfo.get("FEE_TYPE").toString();
		 String br_id = payInfo.get("BR_ID").toString();
		 String refundInfo = payInfo.get("REFUNDINFO").toString();
		 String start_use_time = payInfo.get("START_USE_TIME").toString();
		 Map<String, Object> refundAmount = (HashMap<String, Object>)paramMap.get("refundAmount");
		 String price = refundAmount.get("amount").toString();
		 
		 Map<String, String> rbpReqMap = new HashMap<String, String>();	// RBP 요청
		 Map<String, String> rbpResMap = null;	// RBP 응답
		 
		// RBP연동을 위한 파마리터 셋팅
		rbpReqMap.put("CTN", ctn);	// 과금번호
		rbpReqMap.put("SOC_CODE", fee_type); // 가입자의 요금제 코드 
		rbpReqMap.put("CDRDATA", Init.readConfig.getRbp_cdrdata()); // CDR 버전
		rbpReqMap.put("BR_ID", br_id); // Business RequestID
		rbpReqMap.put("RCVER_CTN", ctn); // 수신자의 전화번호
		rbpReqMap.put("SERVICE_FILTER", refundInfo); // 즉시차감 return 전문의 REFUNDINFO값을 넣는다.
		rbpReqMap.put("START_USE_TIME", start_use_time); 
		rbpReqMap.put("END_USE_TIME", StringUtil.getCurrentTimeMilli());
		rbpReqMap.put("CALLED_NETWORK", Init.readConfig.getRbp_called_network()); // 착신 사업자 코드
		rbpReqMap.put("PRICE", price);
		rbpReqMap.put("PID", Init.readConfig.getRbp_pid()); // Product ID
		rbpReqMap.put("DBID", Init.readConfig.getRbp_dbid()); // DETAIL BILLING ID
		rbpReqMap.put("SVC_CTG", Init.readConfig.getRbp_svc_ctg()); // 통합한도 적용 서비스 구분		
	 
		// 부분취소 요청 paramMap에 저장
		String opCode = Init.readConfig.getRbp_opcode_cancel_part();
		paramMap.put("Req_"+opCode, rbpReqMap);
		
		logVO.setFlow("[ADCB] --> [RBP]");
		rbpResMap = rbpClientService.doRequest(logVO, opCode, paramMap);
		
		// 즉시차감 결과 paramMap에 저장
		paramMap.put("Res_"+opCode, rbpResMap);
		
		// 환불 성공 SMS 정보 paramMap에 저장
		
	 }
	 
	 
	 
	 
	/**
	 * EAI 연동
	 * @param paramMap
	 * @param logVO
	 * @throws Exception
	 */
	public void insertEAI(Map<String, Object> paramMap, LogVO logVO) throws Exception{
		
		Map<String, Object> payInfo = (Map<String, Object>) paramMap.get("payInfo");
		Map<String, String> reqCancel = (Map<String, String>) (paramMap.containsKey("Req_116") ? paramMap.get("Req_116") : paramMap.get("Req_117"));
		Map<String, Object> refundAmount = (HashMap<String, Object>)paramMap.get("refundAmount");
		
		
		EaiVO eaiVO = new EaiVO();
		eaiVO.setNew_request_type("0");
		eaiVO.setNew_ban_unpaid_yn_code(payInfo.get("BAN_UNPAID_YN_CODE").toString());
		eaiVO.setNew_account_type("03");
		eaiVO.setNew_cust_grd_cd(payInfo.get("CUST_GRD_CD") == null ? "" : payInfo.get("CUST_GRD_CD").toString());
		eaiVO.setNew_prss_yymm(payInfo.get("START_USE_TIME").toString().substring(0, 6));
		eaiVO.setNew_request_date(new SimpleDateFormat("yyyyMMddHHmmssSSS").parse(reqCancel.get("END_USE_TIME")));
		eaiVO.setNew_total(refundAmount.get("amount").toString());
		eaiVO.setNew_ban(payInfo.get("BAN").toString());
		eaiVO.setNew_ace_no(payInfo.get("ACE_NO").toString());
		eaiVO.setNew_subs_no(payInfo.get("SUB_NO").toString());
		eaiVO.setNew_request_id(paramMap.get("requestId").toString());
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
	 
	 
	 

}
