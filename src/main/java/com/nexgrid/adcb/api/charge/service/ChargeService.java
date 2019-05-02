package com.nexgrid.adcb.api.charge.service;

import java.net.ConnectException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.inject.Inject;

import org.apache.axis2.client.ServiceClient;
import org.apache.commons.httpclient.ConnectTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.nexgrid.adcb.api.charge.dao.ChargeDAO;
import com.nexgrid.adcb.common.dao.CommonDAO;
import com.nexgrid.adcb.common.exception.CommonException;
import com.nexgrid.adcb.common.service.CommonService;
import com.nexgrid.adcb.common.vo.EaiVO;
import com.nexgrid.adcb.common.vo.LogVO;
import com.nexgrid.adcb.common.vo.SmsSendVO;
import com.nexgrid.adcb.interworking.rbp.message.EnRbpResultCode;
import com.nexgrid.adcb.interworking.rbp.service.RbpClientService;
import com.nexgrid.adcb.interworking.rbp.util.RbpKeyGenerator;
import com.nexgrid.adcb.interworking.rcsg.message.EnRcsgResultCode;
import com.nexgrid.adcb.interworking.rcsg.service.RcsgClientService;
import com.nexgrid.adcb.interworking.rcsg.util.RcsgKeyGenerator;
import com.nexgrid.adcb.util.EnAdcbOmsCode;
import com.nexgrid.adcb.util.Init;
import com.nexgrid.adcb.util.StringUtil;

import lguplus.u3.webservice.cm181.RetrieveMobilePayArmPsblYnServiceStub.DsResOutVO;
import lguplus.u3.webservice.mps208.UpdateLmtStlmUseDenyYnServiceStub;
import lguplus.u3.webservice.mps208.UpdateLmtStlmUseDenyYnServiceStub.BusinessHeader;
import lguplus.u3.webservice.mps208.UpdateLmtStlmUseDenyYnServiceStub.DsInputInVO;
import lguplus.u3.webservice.mps208.UpdateLmtStlmUseDenyYnServiceStub.DsOutputOutVO;
import lguplus.u3.webservice.mps208.UpdateLmtStlmUseDenyYnServiceStub.ESBHeader;
import lguplus.u3.webservice.mps208.UpdateLmtStlmUseDenyYnServiceStub.RequestBody;
import lguplus.u3.webservice.mps208.UpdateLmtStlmUseDenyYnServiceStub.RequestRecord;
import lguplus.u3.webservice.mps208.UpdateLmtStlmUseDenyYnServiceStub.ResponseBody;
import lguplus.u3.webservice.mps208.UpdateLmtStlmUseDenyYnServiceStub.ResponseRecord;
import lguplus.u3.webservice.mps208.UpdateLmtStlmUseDenyYnServiceStub.UpdateLmtStlmUseDenyYn;
import lguplus.u3.webservice.mps208.UpdateLmtStlmUseDenyYnServiceStub.UpdateLmtStlmUseDenyYnResponse;

@Service("chargeService")
public class ChargeService {
	
	@Autowired
	private RbpClientService rbpClientService;
	
	@Autowired
	private RcsgClientService rcsgClientService;
	
	@Autowired
	private CommonService commonService;
	
	@Inject
	private ChargeDAO chargeDAO;
	
	@Inject
	private CommonDAO commonDAO;
	
	private static final Logger logger = LoggerFactory.getLogger(ChargeService.class);

	/**
	 * Charge API body 필수값 체크
	 * @param paramMap
	 * @param logVO
	 * @throws Exception
	 */
	public void reqBodyCheck(Map<String, Object> paramMap, LogVO logVO) throws Exception {
		// body key 체크
		if( paramMap == null || paramMap.size() == 0 || !paramMap.containsKey("msisdn")
				|| !paramMap.containsKey("requestId") || !paramMap.containsKey("clientTransactionId") 
				|| !paramMap.containsKey("purchaseAmount") || !paramMap.containsKey("productDescription")) {
			throw new CommonException(EnAdcbOmsCode.INVALID_BODY_KEY);
		}
		Map<String, Object> purchaseAmount = (HashMap<String, Object>)paramMap.get("purchaseAmount");
		if( purchaseAmount == null || purchaseAmount.size() == 0 || !purchaseAmount.containsKey("amount")
				|| !purchaseAmount.containsKey("currency")) {
			throw new CommonException(EnAdcbOmsCode.INVALID_BODY_KEY);
		}
		
		// body value 체크
		String msisdn = paramMap.get("msisdn") == null ? "" : paramMap.get("msisdn").toString();
		String requestId = paramMap.get("requestId") == null ? "" : paramMap.get("requestId").toString();
		String clientTransactionId = paramMap.get("clientTransactionId") == null ? "" : paramMap.get("clientTransactionId").toString();
		Integer amount = purchaseAmount.get("amount") == null ? 0 : (Integer)purchaseAmount.get("amount");
		String currency = purchaseAmount.get("currency") == null ? "" : purchaseAmount.get("currency").toString().toUpperCase();
		String productDescription = paramMap.get("productDescription") == null ? "" : paramMap.get("productDescription").toString();
		if( "".equals(msisdn) || StringUtil.hasSpecialCharacter(msisdn) || StringUtil.spaceCheck(msisdn) || StringUtil.maxCheck(msisdn, 15)
				||  "".equals(requestId) || StringUtil.hasSpecialCharacter(requestId) || StringUtil.spaceCheck(requestId) || StringUtil.maxCheck(requestId, 50) 
				||  "".equals(clientTransactionId) || StringUtil.hasSpecialCharacter(clientTransactionId) || StringUtil.spaceCheck(clientTransactionId) || StringUtil.maxCheck(clientTransactionId, 50)
				||  amount == 0 || !"KRW".equals(currency)
				||  "".equals(productDescription) || StringUtil.maxCheck(productDescription, 255)) {
			throw new CommonException(EnAdcbOmsCode.INVALID_BODY_VALUE);
		}
		
		logVO.setSid(requestId);
	}
	
	
	
	/**
	 * Charge
	 * @param paramMap
	 * @param logVO
	 * @throws Exception
	 */
	public void charge(Map<String, Object> paramMap, LogVO logVO) throws Exception {
		
		Map<String, String> ncasRes = (HashMap<String,String>) paramMap.get("ncasRes");
		
		String cust_flag = ncasRes.get("CUST_FLAG"); //고객정보 구분값 (ex: YL00000000)
													// 1번째 byte: 결제차단여부 ('Y':결제차단->결제이용동의 필요, 'N':결제가능->결제이용동의 완료)
													// 2번째 byte: PIN번호 설정여부 ('Y':PIN번호사용, 'N':PIN번호사용안함, '0'(숫자):PIN번호미설정, 'L':5회실패로 잠금상태)
		String young_fee_yn = ncasRes.get("YOUNG_FEE_YN"); // 실시간과금대상요금제(RCSG연동대상)
														// 실시간과금대상요금제에 가입되어있는 경우 'Y', 미가입은 'N'
		String svc_auth = ncasRes.get("SVC_AUTH"); // 부정사용자|장애인부가서비스|65세이상부가서비스
													// 입력정보: LRZ0001705|LRZ0003849|LRZ0003850
													// 출력정보: 0|1 (가입은 '1', 미가입은 '0')
		String handicapped = svc_auth.split("\\|")[1]; // 장애인부가서비스
		String old = svc_auth.split("\\|")[2]; // 65세이상부가서비스
		
		// 결제이용동의 정보
		String terms_deny_yn = "";
    	if(!StringUtil.nullCheck(cust_flag)) {
    		terms_deny_yn = "Y";
    	} else {
    		terms_deny_yn = cust_flag.substring(0, 1);
    	}
    	
    	// 약관동의가 필요한 경우 ESB 연동
    	if("Y".equals(terms_deny_yn)) {
    		// ESB 연동
    		doEsbMps208(paramMap, logVO);
    	}
    	
    	//청소년요금제와 일반 구분
    	if("N".equals(young_fee_yn)){ // 14세 이상 중에 청소년요금제가 아닌 경우
    		
    		// 일반요금제일 경우 취약계층인지를 확인하여 취약계층이면 취약계층 대리인 정보를 가져옴.
    		if("1".equals(handicapped) || "1".equals(old)) {
    			paramMap.put("SUB_NO", ncasRes.get("SUB_NO"));
    			paramMap.put("CTN", ncasRes.get("CTN"));
    			String mode = "";	// 1:장애인처리, 2:65세이상처리
    			if("1".equals(handicapped)) {
    				mode = "1";
    			}else {
    				mode = "2";
    			}
    			paramMap.put("MODE", mode);
    			commonService.doEsbCm181(paramMap, logVO);
    		}
    		
    		
    		// RBP 연동
    		doRbpCharge(paramMap, logVO);
    		
    	}else {
    		// RCSG 연동
    		doRcsgCharge(paramMap, logVO);
    	}
    	
	}
	
	
	
	/**
	 * ESB(MPS208) 연동 - 한도결제이용거부여부 변경
	 * @param paramMap
	 * @param logVO
	 * @throws Exception
	 */
	public void doEsbMps208(Map<String, Object> paramMap, LogVO logVO) throws Exception {
		
		logVO.setFlow("[ADCB] --> [ESB]");
		Map<String, String> ncasRes = (HashMap<String,String>) paramMap.get("ncasRes");
		
		String sub_no = ncasRes.get("SUB_NO");	// 고객의 가입번호
		
		ESBHeader header = new ESBHeader();
		RequestRecord reqRecord = new RequestRecord();
		RequestBody reqBody = new RequestBody();
		ResponseRecord resRecord = null;
		BusinessHeader bizHeader = null;
		String esbUrl = Init.readConfig.getEsb_mps208_url();
		int esbTimeout = Integer.parseInt(Init.readConfig.getEsb_time_out());
		
		
		// ESB 헤더 셋팅
		header.setServiceID("MPS208");
		header.setTransactionID(commonService.getEsbTransactionId());
		header.setSystemID("ADCB");
		header.setErrCode("");
		header.setErrMsg("");
		header.setReserved("");
		reqRecord.setESBHeader(header);
		
		// ESB 요청값 셋팅
		DsInputInVO reqVO = new DsInputInVO();
		reqVO.setEntrNo(sub_no); // 가입번호
		reqVO.setLmtStlmUseDenyYn("N"); // 한도결제이용거부여부
		reqVO.setChngRsnCd("LCR1011");	// 변경사유코드(LCR1011:고객요청)
		reqVO.setLendDvVlue("400"); // 인입구분값(400:PG사이용동의)
		reqVO.setNextOperatorId("1100000284");	//처리자ID
		reqBody.addDsInputInVO(reqVO);
		reqRecord.setRequestBody(reqBody);
		
		UpdateLmtStlmUseDenyYn reqIn = new UpdateLmtStlmUseDenyYn();
		reqIn.setRequestRecord(reqRecord);
		String seq = "[" + logVO.getSeqId() + "] ";
		
		try {
			logVO.setEsbMps208ReqTime();
			logger.info(seq + "---------------------------- ESB(MPS208) START ----------------------------");
			logger.info(seq + "ESB(MPS208) Request Url : " + esbUrl);
			logger.info(seq + "ESB(MPS208) Request Header : " + header.toString());
			logger.info(seq + "ESB(MPS208) Request Body : " + reqVO.toString());
			
			// ESB 호출
			UpdateLmtStlmUseDenyYnServiceStub stub = new UpdateLmtStlmUseDenyYnServiceStub(esbUrl);
			
			// ESB Timeout 셋팅
			ServiceClient serviceClient = stub._getServiceClient();
			serviceClient.getOptions().setTimeOutInMilliSeconds(esbTimeout);
			stub._setServiceClient(serviceClient);
			
			// ESB 호출 응답
			UpdateLmtStlmUseDenyYnResponse esbRes = stub.updateLmtStlmUseDenyYn(reqIn);
			
			logVO.setFlow("[ADCB] <-- [ESB]");
			logVO.setEsbMps208ResTime();
			
			resRecord = esbRes.getResponseRecord();
			header = resRecord.getESBHeader();
			logger.info(seq + "ESB(MPS208) Response Header : " + header.toString());
			
		}catch(Exception e) {
			if (e.getCause() instanceof ConnectTimeoutException) {
				throw new CommonException(EnAdcbOmsCode.ESB_TIMEOUT);
			}else {
				throw new CommonException(EnAdcbOmsCode.ESB_INVALID_ERROR, e.getMessage());
			}
		}
		
		
		if("".equals(header.getErrCode())) { // 성공
			ResponseBody resBody = resRecord.getResponseBody();
			bizHeader = resRecord.getBusinessHeader();
			logger.info(seq + "ESB(MPS208) Response BusinessHeader : " + bizHeader.toString());
			
			if (!"N0000".equals(bizHeader.getResultCode())) {
				// 에러
				throw new CommonException(EnAdcbOmsCode.ESB_HEADER, bizHeader.getResultMessage());
			}
			
			if(resBody != null) {
				DsOutputOutVO resVO = resBody.getDsOutputOutVO();
				logger.info(seq + "ESB(MPS208) Response Body : " + resVO.toString());
				
				if("0000".equals(resVO.getResultCode())) {
					// 한도결제이용거부여부가 정상적으로 변경된 경우
					paramMap.put("esbMps208", resVO);
					
				}else {
					if("4004".equals(resVO.getResultCode())) {
						throw new CommonException(EnAdcbOmsCode.ESB_4004);
					}else {
						throw new CommonException(EnAdcbOmsCode.ESB_API.status(), EnAdcbOmsCode.ESB_API.mappingCode(), EnAdcbOmsCode.ESB_API.value() + resVO.getResultCode(), resVO.getResultMsg());
					}
				}
				
			}
			
			logger.info(seq + "---------------------------- ESB(MPS208) END ----------------------------");
			
		}else{
			throw new CommonException(EnAdcbOmsCode.ESB_HEADER, header.getErrMsg());
		}
		
	}
	
	

	
	/**
	 * 통합한도 연동: 즉시차감
	 * @param paramMap
	 * @param logVO
	 * @throws Exception
	 */
	public void doRbpCharge(Map<String, Object> paramMap, LogVO logVO) throws Exception {
		
		Map<String, String> ncasRes = (HashMap<String,String>) paramMap.get("ncasRes");
		String ctn = ncasRes.get("CTN");
    	String fee_type = ncasRes.get("FEE_TYPE"); //요금제 타입
    	String svc_auth = ncasRes.get("SVC_AUTH"); // 장애인부가서비스|65세이상부가서비스
													// 입력정보: LRZ0003849|LRZ0003850
													// 출력정보: 0|1 (가입은 '1', 미가입은 '0')
    	
    	Map<String, String> rbpReqMap = new HashMap<String, String>();	// RBP 요청
		Map<String, String> rbpResMap = null;	// RBP 응답
		
		String br_id = RbpKeyGenerator.getInstance(Init.readConfig.getRbp_system_id()).generateKey();
		String reqCtn = StringUtil.getCtn344(ctn);
		Map<String, Object> purchaseAmount = (HashMap<String, Object>)paramMap.get("purchaseAmount");
		String price = purchaseAmount.get("amount").toString();
		
		// RBP연동을 위한 파마리터 셋팅
		rbpReqMap.put("CTN", reqCtn);	// 과금번호
		rbpReqMap.put("SOC_CODE", fee_type); // 가입자의 요금제 코드 
		rbpReqMap.put("CDRDATA", Init.readConfig.getRbp_cdrdata()); // CDR 버전
		rbpReqMap.put("BR_ID", br_id); // Business RequestID
		rbpReqMap.put("RCVER_CTN", reqCtn); // 수신자의 전화번호
		rbpReqMap.put("SERVICE_FILTER", reqCtn); // 발신 번호
		rbpReqMap.put("START_USE_TIME", StringUtil.getCurrentTimeMilli()); 
		rbpReqMap.put("END_USE_TIME", StringUtil.getCurrentTimeMilli());
		rbpReqMap.put("CALLED_NETWORK", Init.readConfig.getRbp_called_network()); // 착신 사업자 코드
		rbpReqMap.put("PRICE", price);
		rbpReqMap.put("PID", Init.readConfig.getRbp_pid()); // Product ID
		rbpReqMap.put("DBID", Init.readConfig.getRbp_dbid()); // DETAIL BILLING ID
		rbpReqMap.put("SVC_CTG", Init.readConfig.getRbp_svc_ctg()); // 통합한도 적용 서비스 구분
		
		
		// 즉시차감 요청 paramMap에 저장
		String opCode = Init.readConfig.getRbp_opcode_charge();
		paramMap.put("Req_" + opCode, rbpReqMap);
		
		// RBP 연동
		logVO.setFlow("[ADCB] --> [RBP]");
		try {
			rbpResMap = rbpClientService.doRequest(logVO, opCode, paramMap);
		}catch (CommonException common) {
			
			String firstCode = common.getOmsErrCode().substring(0, 4);
			String rbpRsCode = common.getOmsErrCode().substring(4);
			
			// RBP의 RESULT가 "0000"이 아니기 때문에 생겨난 exception일 경우에만
			if(EnAdcbOmsCode.RBP_API.value().equals(firstCode)) {
				if(EnRbpResultCode.RS_4008.getDefaultValue().equals(rbpRsCode)) { // 한도초과일 경우 
					
		    		// 한도초과 SMS 정보 paramMap에 저장
					addChargeFailSMS(paramMap);
				}
			}
			
			throw common;
			
		}
		
		
		// 즉시차감 결과 paramMap에 저장
		paramMap.put("Res_"+opCode, rbpResMap);
		
		// 결제 성공 SMS 정보 paramMap에 저장
		
	}
	
	
	
	/**
	 * RCSG 연동: 즉시차감
	 * @param paramMap
	 * @param logVO
	 * @throws Exception
	 */
	public void doRcsgCharge(Map<String, Object> paramMap, LogVO logVO) throws Exception {
		
		Map<String, String> ncasRes = (HashMap<String,String>) paramMap.get("ncasRes");
		String ctn = ncasRes.get("CTN");
    	String fee_type = ncasRes.get("FEE_TYPE"); //요금제 타입 
    	
    	Map<String, String> rcsgReqMap = new HashMap<String, String>();	// RCSG 요청
		Map<String, String> rcsgResMap = null;	// RCSG 응답
		
		String br_id = RcsgKeyGenerator.getInstance(Init.readConfig.getRcsg_system_id()).generateKey();
		String reqCtn = StringUtil.getCtn344(ctn);
		Map<String, Object> purchaseAmount = (HashMap<String, Object>)paramMap.get("purchaseAmount");
		String price = purchaseAmount.get("amount").toString();
		
		// RCSG연동을 위한 파마리터 셋팅
		rcsgReqMap.put("CTN", reqCtn);	// 과금번호
		rcsgReqMap.put("SOC_CODE", fee_type); // 가입자의 요금제 코드 
		rcsgReqMap.put("CDRDATA", Init.readConfig.getRcsg_cdrdata()); // CDR 버전
		rcsgReqMap.put("BR_ID", br_id); // Business RequestID
		rcsgReqMap.put("RCVER_CTN", reqCtn); // 수신자의 전화번호
		rcsgReqMap.put("SERVICE_FILTER", reqCtn); // 발신 번호
		rcsgReqMap.put("START_USE_TIME", StringUtil.getCurrentTimeMilli()); 
		rcsgReqMap.put("END_USE_TIME", StringUtil.getCurrentTimeMilli());
		rcsgReqMap.put("CALLED_NETWORK", Init.readConfig.getRcsg_called_network()); // 착신 사업자 코드
		rcsgReqMap.put("PRICE", price);
		rcsgReqMap.put("PID", Init.readConfig.getRcsg_pid()); // Product ID
		rcsgReqMap.put("DBID", Init.readConfig.getRcsg_dbid()); // DETAIL BILLING ID
		
		
		// 즉시차감 요청 paramMap에 저장
		String opCode = Init.readConfig.getRcsg_opcode_charge();
		paramMap.put("Req_"+opCode, rcsgReqMap);
		
		// RCSG 연동
		logVO.setFlow("[ADCB] --> [RCSG]");
		try {
			rcsgResMap = rcsgClientService.doRequest(logVO, opCode, paramMap);
		}catch (CommonException common) {
			
			String firstCode = common.getOmsErrCode().substring(0, 4);
			String rcsgRsCode = common.getOmsErrCode().substring(4);
			
			// RCSG의 RESULT가 "0000"이 아니기 때문에 생겨난 exception일 경우에만
			if(EnAdcbOmsCode.RCSG_API.value().equals(firstCode)) {
				if(EnRcsgResultCode.RS_4008.getDefaultValue().equals(rcsgRsCode)) { // 한도초과일 경우 
					
					// 한도초과 SMS 정보 paramMap에 저장
					addChargeFailSMS(paramMap);
				}
				
			}
			throw common;
		}
		
		
		// 차감 결과 paramMap에 저장
		paramMap.put("Res_"+opCode, rcsgResMap);
	}
	
	
	
	/**
	 * BOKU의 청구 API 최초 요청 데이터 INSERT
	 * @param paramMap
	 * @param logVO
	 * @throws Exception
	 */
	public void insertChargeReq(Map<String, Object> paramMap, LogVO logVO) throws Exception {
		
		logVO.setFlow("[ADCB] --> [DB]");
		try {
			chargeDAO.insertChargeReq(paramMap);
		}catch(DataAccessException adcbExc){
//			SQLException se = (SQLException) adcbExc.getRootCause();
//			logVO.setRsCode(Integer.toString(se.getErrorCode()));
			
			throw new CommonException(EnAdcbOmsCode.DB_ERROR, adcbExc.getMessage());
		}catch(ConnectException adcbExc) {
			throw new CommonException(EnAdcbOmsCode.DB_CONNECT_ERROR, adcbExc.getMessage());
		}catch (Exception adcbExc) {
			throw new CommonException(EnAdcbOmsCode.DB_INVALID_ERROR, adcbExc.getMessage());
		}
		logVO.setFlow("[ADCB] <-- [DB]");
		
	}
	
	
	
	/**
	 * 결제 완료 또는 실패 정보 UPDATE
	 * @param paramMap
	 * @param logVO
	 * @throws Exception
	 */
	public void updateChargeInfo(Map<String, Object> paramMap, LogVO logVO) throws Exception{
		
		// update를 위한 data
		paramMap.put("TRANSACTION_TYPE", "B"); // BOKU 요청구분 (B:구매, C:취소)
		paramMap.put("PAY_DT", new Date()); // 결제완료일시
		Calendar cal = Calendar.getInstance();
		paramMap.put("current_month", new SimpleDateFormat("yyyyMM").format(cal.getTime()));
		cal.add(Calendar.MONTH, -1);
		paramMap.put("last_month", new SimpleDateFormat("yyyyMM").format(cal.getTime()));
		
		logVO.setFlow("[ADCB] --> [DB]");
		try {
			chargeDAO.updateChargeInfo(paramMap);
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
	 * CHARGE API 중복 요청 체크
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
		Map<String, String> chargeReq = null;
		
		logVO.setFlow("[ADCB] --> [DB]");
		
		try {
			chargeReq = commonDAO.reqDuplicateCheck(paramMap);
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
		if(chargeReq == null) {
			return false;
		}else {
			if(chargeReq.get("ISSUER_PAYMENTID") != null) { // 요청에 대한 응답이 있었을 경우 
				// 이미 줬던 응답을 다시 준다.
				Map<String, Object> resMap = new HashMap<>();
				resMap.put("issuerPaymentId", chargeReq.get("ISSUER_PAYMENTID"));

				Map<String, Object> result = new HashMap<>();
				result.put("reasonCode", Integer.parseInt(chargeReq.get("RESULT")));
				result.put("message", chargeReq.get("RESULT_MSG"));
				resMap.put("result", result);
				
				paramMap.put("duplicateRes", resMap);
				paramMap.put("http_status", chargeReq.get("HTTP_STATUS"));
				
				return true;
			}else {
				throw new CommonException(EnAdcbOmsCode.CHARGE_DUPLICATE_REQ);
			}
		}
	}
	
	
	
	/**
	 * EAI 연동
	 * @param paramMap
	 * @param logVO
	 * @throws Exception
	 */
	public void insertEAI(Map<String, Object> paramMap, LogVO logVO) throws Exception{
		
		Map<String, String> ncasRes = (HashMap<String,String>) paramMap.get("ncasRes");
		Map<String, String> Res_111 = (HashMap<String,String>) paramMap.get("Res_111");
		Map<String, String> Req_114 = (HashMap<String,String>) paramMap.get("Req_114");
		Map<String, Object> purchaseAmount = (HashMap<String, Object>)paramMap.get("purchaseAmount");
		
		
		EaiVO eaiVO = new EaiVO();
		eaiVO.setNew_ban_unpaid_yn_code(ncasRes.get("BAN_UNPAID_YN_CODE"));
		eaiVO.setNew_account_type("02");
		eaiVO.setNew_cust_grd_cd(Res_111 == null ? "" : Res_111.get("CUST_GRD_CD"));
		eaiVO.setNew_prss_yymm(Req_114.get("START_USE_TIME").substring(0, 6));
		eaiVO.setNew_request_date(new SimpleDateFormat("yyyyMMddHHmmssSSS").parse(Req_114.get("START_USE_TIME")));
		eaiVO.setNew_total(purchaseAmount.get("amount").toString());
		eaiVO.setNew_ban(ncasRes.get("BAN"));
		eaiVO.setNew_ace_no(ncasRes.get("ACENO"));
		eaiVO.setNew_subs_no(ncasRes.get("SUB_NO"));
		eaiVO.setNew_request_id(paramMap.get("requestId").toString());
		eaiVO.setNew_merchant_id(paramMap.containsKey("merchantId") ? paramMap.get("merchantId").toString() : "");
		eaiVO.setNew_product_description(paramMap.get("productDescription").toString());
		
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
	 * 구간한도 체크
	 * @param paramMap
	 * @return 0이면 구간한도 SMS X
	 * @throws Exception
	 */
	public int getLimitCharge(Map<String, Object> paramMap) throws Exception{
		int result = 0;
		Map<String, String> reqCharge = (Map<String, String>)paramMap.get("Req_114");
		Map<String, String> resCharge = (Map<String, String>)paramMap.get("Res_114");
		Map<String, String> ncasRes = (HashMap<String,String>) paramMap.get("ncasRes");
		int price = Integer.parseInt(reqCharge.get("PRICE"));
		String young_fee_yn = ncasRes.get("YOUNG_FEE_YN"); // 실시간과금대상요금제(RCSG연동대상)
															// 실시간과금대상요금제에 가입되어있는 경우 'Y', 미가입은 'N'
		
		int ctgLimt = "N".equals(young_fee_yn) ? 
						Integer.parseInt(resCharge.get("SVC_CTG_LIMIT")) : Integer.parseInt(resCharge.get("INFO_LIMIT"));
		int ctgAvail = "N".equals(young_fee_yn) ? 
						Integer.parseInt(resCharge.get("SVC_CTG_AVAIL")) : Integer.parseInt(resCharge.get("INFO_AVAIL"));
		
		// RBP의 경우에는 차감된 데이터가 오고, RCSG의 경우에는 차감되지 않은 데이터가 온다.
		int preChargeTotal = "N".equals(young_fee_yn) ? ctgLimt - ctgAvail - price : ctgLimt - ctgAvail;
		int postChargeTotal = preChargeTotal + price;
		
		String[] arrLimit = Init.readConfig.getCharge_section_list().split(",");
		for(String strLimit : arrLimit) {
			Integer limit = Integer.parseInt(strLimit);
			if (preChargeTotal <= limit && limit < postChargeTotal) {
				result = limit;
				paramMap.put("limit", limit);
			}
		}
		
		return result;
	}
	
	
	
	/**
	 * 결제 성공시 SMS 발송
	 * @param paramMap
	 * @throws Exception
	 */
	public void addChargeSuccessSMS(Map<String, Object> paramMap) throws Exception{
		
		Map<String, String> ncasRes = (HashMap<String,String>) paramMap.get("ncasRes");
		String ctn = ncasRes.get("CTN");
		String law1HomeTelno = StringUtil.checkTrim(ncasRes.get("LAW1_HOME_TELNO")); //법정 대리인 전화번호
		String sub_birth_pers_id = ncasRes.get("SUB_BIRTH_PERS_ID"); // 명의자 생년월일
    	String sub_sex_pers_id = ncasRes.get("SUB_SEX_PERS_ID"); // 명의자 성별
    	// 실사용자 만나이 구하기
  		int age = 0;
  		try {
  			age = StringUtil.calculateManAge(sub_birth_pers_id, sub_sex_pers_id);
  		} catch (Exception e) {
  			//에러가 날 경우 차단시킨다
  			age = 0;
  		}
    	
    	
		
		// 결제 성공 SMS 전송 정보 리스트
		List<SmsSendVO> smsList = new ArrayList<>();
		
		// 본인-결제 성공 SMS
		smsList.add(commonService.addSmsInfo(paramMap, "charge_complete", StringUtil.getCtn344(ctn)));
		
		// 만 14세 이상, 만 19세 미만일 경우에만 구간한도 초과 SMS 발송
		if(age >= 14 && age < 19) {
			// 구간한도 초과 시
			if(0 < getLimitCharge(paramMap)) {
				// 본인-구간한도 SMS
				smsList.add(commonService.addSmsInfo(paramMap, "section_excess", StringUtil.getCtn344(ctn)));
				// 법정 대리인이 있을 경우
				if(!"".equals(law1HomeTelno)) {
					// 법정대리인의 번호가 010으로 시작할 때만 문자를 보낸다.
					if("010".equals(law1HomeTelno.substring(0, 3)) ) {
						// 대리인-구간한도 SMS
						smsList.add(commonService.addSmsInfo(paramMap, "section_excess", StringUtil.getCtn344(law1HomeTelno.replaceAll("[^0-9]", ""))));
					}
				}
			}
		}
		
		// 취약계층일 경우 대리인에게 성공 SMS 발송
		if(paramMap.containsKey("esbCm181Res")) {
			DsResOutVO esbVO = (DsResOutVO)paramMap.get("esbCm181Res");
			if("Y".equals(esbVO.getAgntRegYn())) {
				// 대리인 - 결제 성공 SMS
				smsList.add(commonService.addSmsInfo(paramMap, "charge_complete", esbVO.getHpno()));
			}
		}
		
		// 결제 성공 SMS 전송 정보 paramMap에 저장.
		paramMap.put("smsList", smsList);
	}
	
	
	/**
	 * 결제 실패시(한도초과) SMS 발송
	 * @param paramMap
	 * @throws Exception
	 */
	public void addChargeFailSMS(Map<String, Object> paramMap) throws Exception{
		Map<String, String> ncasRes = (HashMap<String,String>) paramMap.get("ncasRes");
		String ctn = ncasRes.get("CTN");
		
		List<SmsSendVO> smsList = new ArrayList<>();
		
		// 본인-이용한도 초과 SMS
		smsList.add(commonService.addSmsInfo(paramMap, "limit_excess", StringUtil.getCtn344(ctn)));
		
		// 본인-고객센터앱 선결제 바로가기 SMS
		smsList.add(commonService.addSmsInfo(paramMap, "limit_excess2", StringUtil.getCtn344(ctn)));
		
		//한도초과 SMS 전송정보 paramMap에 저장
		paramMap.put("smsList", smsList);
	}
	
}
