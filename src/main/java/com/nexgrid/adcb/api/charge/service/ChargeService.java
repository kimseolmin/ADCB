package com.nexgrid.adcb.api.charge.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.axis2.client.ServiceClient;
import org.apache.commons.httpclient.ConnectTimeoutException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.nexgrid.adcb.common.exception.CommonException;
import com.nexgrid.adcb.common.vo.LogVO;
import com.nexgrid.adcb.util.EnAdcbOmsCode;
import com.nexgrid.adcb.util.Init;
import com.nexgrid.adcb.util.StringUtil;

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

	// Charge API body 필수값 체크
	public void reqBodyCheck(Map<String, Object> paramMap, LogVO logVO) throws Exception {
		// body key 체크
		if( paramMap==null || paramMap.size() == 0 || !paramMap.containsKey("msisdn")
				|| !paramMap.containsKey("requestId") || !paramMap.containsKey("clientTransactionId") 
				|| !paramMap.containsKey("purchaseAmount") || !paramMap.containsKey("productDescription")) {
			throw new CommonException(HttpStatus.BAD_REQUEST.value(), EnAdcbOmsCode.INVALID_BODY_KEY.mappingCode(), EnAdcbOmsCode.INVALID_BODY_KEY.value(), EnAdcbOmsCode.INVALID_BODY_KEY.logMsg(), logVO.getFlow());
		}
		
		// body value 체크
		if( paramMap.get("msisdn") == null || paramMap.get("msisdn").equals("")
				||  paramMap.get("requestId") == null || paramMap.get("requestId").equals("")
				||  paramMap.get("clientTransactionId") == null || paramMap.get("clientTransactionId").equals("")
				||  paramMap.get("purchaseAmount") == null || paramMap.get("purchaseAmount").equals("")
				||  paramMap.get("productDescription") == null || paramMap.get("productDescription").equals("")) {
			throw new CommonException(HttpStatus.BAD_REQUEST.value(), EnAdcbOmsCode.INVALID_BODY_VALUE.mappingCode(), EnAdcbOmsCode.INVALID_BODY_VALUE.value(), EnAdcbOmsCode.INVALID_BODY_VALUE.logMsg(), logVO.getFlow());
		}
	}
	
	
	// Charge
	public void charge(Map<String, Object> paramMap, LogVO logVO) throws Exception {
		
		Map<String, String> ncasRes = (HashMap<String,String>) paramMap.get("ncasRes");
		
		String cust_flag = ncasRes.get("CUST_FLAG"); //고객정보 구분값 (ex: YL00000000)
													// 1번째 byte: 결제차단여부 ('Y':결제차단->결제이용동의 필요, 'N':결제가능->결제이용동의 완료)
													// 2번째 byte: PIN번호 설정여부 ('Y':PIN번호사용, 'N':PIN번호사용안함, '0'(숫자):PIN번호미설정, 'L':5회실패로 잠금상태)
	
		
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
    		logVO.setFlow("[ADCB] --> [ESB]");
    	}
    	
	}
	
	
	
	
	public void doEsbMps208(Map<String, Object> paramMap, LogVO logVO) throws Exception {
		
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
		header.setServiceID("Mps208");
		header.setTransactionID(getEsbTransactionId());
		header.setSystemID("ADCB001");
		header.setErrCode("");
		header.setErrMsg("");
		header.setReserved("");
		reqRecord.setESBHeader(header);
		
		// ESB 요청값 셋팅
		DsInputInVO reqVO = new DsInputInVO();
		reqVO.setEntrNo(sub_no);
		reqVO.setLmtStlmUseDenyYn("N");
		reqVO.setChngRsnCd("LCR1011");
		reqVO.setLendDvVlue("400"); // 인입구분값(400:PG사이용동의)
		reqVO.setNextOperatorId("999999");	//처리자ID
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
				
			}else {
				
			}
		}
		
		
		if("".equals(header.getErrCode())) {
			ResponseBody resBody = resRecord.getResponseBody();
			bizHeader = resRecord.getBusinessHeader();
			
			if (!"N0000".equals(bizHeader.getResultCode())) {
				// 에러
			}
			
			if(resBody != null) {
				DsOutputOutVO resVO = resBody.getDsOutputOutVO();
			}
			
		}else{
			
		}
		
	}
	
	
	
	
	// ESB TransactionId
	public String getEsbTransactionId() {
		String dTime = StringUtil.getCurrentTimeMilli();
		
		Random random = new Random();
		Integer a = random.nextInt(9999999);
		String rand01 = String.format("%07d", a);
		
		return dTime + rand01;
	}
	
}
