package com.nexgrid.adcb.api.charge.service;

import java.net.ConnectException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.axis2.client.ServiceClient;
import org.apache.commons.httpclient.ConnectTimeoutException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.nexgrid.adcb.api.charge.dao.ChargeDAO;
import com.nexgrid.adcb.common.exception.CommonException;
import com.nexgrid.adcb.common.vo.LogVO;
import com.nexgrid.adcb.interworking.rbp.service.RbpClientService;
import com.nexgrid.adcb.interworking.rbp.util.RbpKeyGenerator;
import com.nexgrid.adcb.interworking.rcsg.service.RcsgClientService;
import com.nexgrid.adcb.interworking.rcsg.util.RcsgKeyGenerator;
import com.nexgrid.adcb.util.EnAdcbOmsCode;
import com.nexgrid.adcb.util.Init;
import com.nexgrid.adcb.util.StringUtil;

import lguplus.u3.webservice.cm181.RetrieveMobilePayArmPsblYnServiceStub;
import lguplus.u3.webservice.cm181.RetrieveMobilePayArmPsblYnServiceStub.DsReqInVO;
import lguplus.u3.webservice.cm181.RetrieveMobilePayArmPsblYnServiceStub.DsResOutVO;
import lguplus.u3.webservice.cm181.RetrieveMobilePayArmPsblYnServiceStub.RetrieveMobilePayArmPsblYn;
import lguplus.u3.webservice.cm181.RetrieveMobilePayArmPsblYnServiceStub.RetrieveMobilePayArmPsblYnResponse;
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
	private ChargeDAO chargeDAO;

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
				||  "".equals(productDescription) || StringUtil.maxCheck(productDescription, 1255)) {
			throw new CommonException(EnAdcbOmsCode.INVALID_BODY_VALUE);
		}
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
    		// RBP 연동
    		doRbpCharge(paramMap, logVO);
    		
    		// 일반요금제일 경우 취약계층을 확인해야 함.
    		String handicapped = svc_auth.split("|")[1]; // 장애인부가서비스
    		String old = svc_auth.split("|")[2]; // 65세이상부가서비스
    		if("1".equals(handicapped) || "1".equals(old)) { // '1'일 경우 가입
    			// ESB 연동 - 취약계층 대리인의 정보를 가져오기 위함.
    			doEsbCm181(paramMap, logVO);
    		}
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
		header.setTransactionID(getEsbTransactionId());
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
		
		try {
			// ESB 호출
			UpdateLmtStlmUseDenyYnServiceStub stub = new UpdateLmtStlmUseDenyYnServiceStub(esbUrl);
			
			// ESB Timeout 셋팅
			ServiceClient serviceClient = stub._getServiceClient();
			serviceClient.getOptions().setTimeOutInMilliSeconds(esbTimeout);
			stub._setServiceClient(serviceClient);
			
			// ESB 호출 응답
			UpdateLmtStlmUseDenyYnResponse esbRes = stub.updateLmtStlmUseDenyYn(reqIn);
			
			logVO.setFlow("[ADCB] <-- [ESB]");
			
			resRecord = esbRes.getResponseRecord();
			header = resRecord.getESBHeader();
			
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
			
			if (!"N0000".equals(bizHeader.getResultCode())) {
				// 에러
				throw new CommonException(EnAdcbOmsCode.ESB_HEADER, bizHeader.getResultMessage());
			}
			
			if(resBody != null) {
				DsOutputOutVO resVO = resBody.getDsOutputOutVO();
				
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
			
		}else{
			throw new CommonException(EnAdcbOmsCode.ESB_HEADER, header.getErrMsg());
		}
		
	}
	
	
	

	/**
	 * ESB TransactionId
	 * @return String ESB TransactionId
	 */
	public String getEsbTransactionId() {
		String dTime = StringUtil.getCurrentTimeMilli();
		
		Random random = new Random();
		Integer a = random.nextInt(9999999);
		String rand01 = String.format("%07d", a);
		
		return dTime + rand01;
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
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		long tempTimeMillis = System.currentTimeMillis();
		String currentDate = dateFormat.format(new Date(tempTimeMillis));
		Map<String, Object> purchaseAmount = (HashMap<String, Object>)paramMap.get("purchaseAmount");
		String price = purchaseAmount.get("amount").toString();
		
		// RBP연동을 위한 파마리터 셋팅
		rbpReqMap.put("CTN", reqCtn);	// 과금번호
		rbpReqMap.put("SOC_CODE", fee_type); // 가입자의 요금제 코드 
		rbpReqMap.put("CDRDATA", Init.readConfig.getRbp_cdrdata()); // CDR 버전
		rbpReqMap.put("BR_ID", br_id); // Business RequestID
		rbpReqMap.put("RCVER_CTN", reqCtn); // 수신자의 전화번호
		rbpReqMap.put("SERVICE_FILTER", reqCtn); // 발신 번호
		rbpReqMap.put("START_USE_TIME", currentDate); 
		rbpReqMap.put("END_USE_TIME", currentDate);
		rbpReqMap.put("CALLED_NETWORK", Init.readConfig.getRbp_called_network()); // 착신 사업자 코드
		rbpReqMap.put("PRICE", price);
		rbpReqMap.put("PID", Init.readConfig.getRbp_pid()); // Product ID
		rbpReqMap.put("DBID", Init.readConfig.getRbp_dbid()); // DETAIL BILLING ID
		rbpReqMap.put("SVC_CTG", Init.readConfig.getRbp_svc_ctg()); // 통합한도 적용 서비스 구분
		
		
		// 즉시차감 요청 paramMap에 저장
		paramMap.put("RbpReq_114", rbpReqMap);
		
		// RBP 연동
		logVO.setFlow("[ADCB] --> [RBP]");
		rbpResMap = rbpClientService.doRequest(logVO, Init.readConfig.getRbp_opcode_charge(), rbpReqMap);
		
		// 즉시차감 결과 paramMap에 저장
		paramMap.put("RbpRes_114", rbpResMap);
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
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		long tempTimeMillis = System.currentTimeMillis();
		String currentDate = dateFormat.format(new Date(tempTimeMillis));
		String price = paramMap.get("purchaseAmount").toString();
		
		// RCSG연동을 위한 파마리터 셋팅
		rcsgReqMap.put("CTN", reqCtn);	// 과금번호
		rcsgReqMap.put("SOC_CODE", fee_type); // 가입자의 요금제 코드 
		rcsgReqMap.put("CDRDATA", Init.readConfig.getRcsg_cdrdata()); // CDR 버전
		rcsgReqMap.put("BR_ID", br_id); // Business RequestID
		rcsgReqMap.put("RCVER_CTN", reqCtn); // 수신자의 전화번호
		rcsgReqMap.put("SERVICE_FILTER", reqCtn); // 발신 번호
		rcsgReqMap.put("START_USE_TIME", currentDate); 
		rcsgReqMap.put("END_USE_TIME", currentDate);
		rcsgReqMap.put("CALLED_NETWORK", Init.readConfig.getRcsg_called_network()); // 착신 사업자 코드
		rcsgReqMap.put("PRICE", price);
		rcsgReqMap.put("PID", Init.readConfig.getRcsg_pid()); // Product ID
		rcsgReqMap.put("DBID", Init.readConfig.getRcsg_dbid()); // DETAIL BILLING ID
		
		
		// 즉시차감 요청 paramMap에 저장
		paramMap.put("RcsgReq_114", rcsgReqMap);
		
		// RCSG 연동
		logVO.setFlow("[ADCB] --> [RCSG]");
		rcsgReqMap = rcsgClientService.doRequest(logVO, Init.readConfig.getRcsg_opcode_charge(), rcsgReqMap);
		
		// 한도조회 결과 paramMap에 저장
		paramMap.put("RcsgRes_114", rcsgResMap);
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
	 * ESB 연동 - 취약계층 대리인의 정보를 가져오기 위함.
	 * @param paramMap
	 * @param logVO
	 * @throws Exception
	 */
	public void doEsbCm181(Map<String, Object> paramMap, LogVO logVO) throws Exception {
		logVO.setFlow("[ADCB] --> [ESB]");
		Map<String, String> ncasRes = (HashMap<String,String>) paramMap.get("ncasRes");
		
		String sub_no = ncasRes.get("SUB_NO");	// 고객의 가입번호
		String ctn = StringUtil.getNcas444(ncasRes.get("CTN")); // 12자리 CTN
		
		String mode = "";	// 1:장애인처리, 2:65세이상처리
		String handicapped = ncasRes.get("SVC_AUTH").split("|")[1]; // 부정사용자|장애인부가서비스|65세이상부가서비스
		if("1".equals(handicapped)) {
			mode = "1";
		}else {
			mode = "2";
		}
		
		String esbUrl = Init.readConfig.getEsb_cm181_url();
		int esbTimeout = Integer.parseInt(Init.readConfig.getEsb_time_out());
		lguplus.u3.webservice.cm181.RetrieveMobilePayArmPsblYnServiceStub.ESBHeader header = new lguplus.u3.webservice.cm181.RetrieveMobilePayArmPsblYnServiceStub.ESBHeader();
		lguplus.u3.webservice.cm181.RetrieveMobilePayArmPsblYnServiceStub.RequestRecord reqRecord = new lguplus.u3.webservice.cm181.RetrieveMobilePayArmPsblYnServiceStub.RequestRecord();
		lguplus.u3.webservice.cm181.RetrieveMobilePayArmPsblYnServiceStub.RequestBody reqBody = new lguplus.u3.webservice.cm181.RetrieveMobilePayArmPsblYnServiceStub.RequestBody();
		lguplus.u3.webservice.cm181.RetrieveMobilePayArmPsblYnServiceStub.ResponseRecord resRecord = null;
		
		
		// ESB header 
		header.setServiceID("CM181");
		header.setTransactionID(getEsbTransactionId());
		header.setSystemID("ADCB");
		header.setErrCode("");
		header.setErrMsg("");
		header.setReserved("");
		reqRecord.setESBHeader(header);
		
		// ESB Request
		DsReqInVO reqVO = new DsReqInVO();
		reqVO.setEntrNo(sub_no);
		reqVO.setCtn(ctn);
		reqVO.setMode(mode);
		reqVO.setNextOperatorId("1100000284");
		
		RetrieveMobilePayArmPsblYn reqIn = new RetrieveMobilePayArmPsblYn();
		reqIn.setRequestRecord(reqRecord);
		
		try {
			// ESB 호출
			RetrieveMobilePayArmPsblYnServiceStub stub = new RetrieveMobilePayArmPsblYnServiceStub(esbUrl);
			
			// ESB Timeout 셋팅
			ServiceClient serviceClient = stub._getServiceClient();
			serviceClient.getOptions().setTimeOutInMilliSeconds(esbTimeout);
			stub._setServiceClient(serviceClient);
			
			// ESB 호출 응답
			RetrieveMobilePayArmPsblYnResponse esbRes = stub.retrieveMobilePayArmPsblYn(reqIn);
			logVO.setFlow("[ADCB] <-- [ESB]");
			
			resRecord = esbRes.getResponseRecord();
			header = resRecord.getESBHeader();
			
			
		}catch(Exception e) {
			if (e.getCause() instanceof ConnectTimeoutException) {
				throw new CommonException(EnAdcbOmsCode.ESB_TIMEOUT);
			}else {
				throw new CommonException(EnAdcbOmsCode.ESB_INVALID_ERROR, e.getMessage());
			}
		}
		
		
		if("".equals(header.getErrCode())) {
			lguplus.u3.webservice.cm181.RetrieveMobilePayArmPsblYnServiceStub.ResponseBody resBody = resRecord.getResponseBody();
			if(resBody != null) {
				DsResOutVO resVO = resBody.getDsResOutVO();
				// ESB 결과 저장
				paramMap.put("esbCm181Res", resVO);
			}
		}else{
			throw new CommonException(EnAdcbOmsCode.ESB_HEADER, header.getErrMsg());
		}
		
	}
	
	
}
