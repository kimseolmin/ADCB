package com.nexgrid.adcb.common.service;

import java.net.ConnectException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.axis2.client.ServiceClient;
import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.UnknownHttpStatusCodeException;

import com.nexgrid.adcb.common.dao.CommonDAO;
import com.nexgrid.adcb.common.exception.CommonException;
import com.nexgrid.adcb.common.vo.LogVO;
import com.nexgrid.adcb.common.vo.SmsSendVO;
import com.nexgrid.adcb.interworking.rbp.message.EnRbpResultCode;
import com.nexgrid.adcb.interworking.rbp.service.RbpClientService;
import com.nexgrid.adcb.interworking.rbp.util.RbpKeyGenerator;
import com.nexgrid.adcb.interworking.rcsg.service.RcsgClientService;
import com.nexgrid.adcb.util.EnAdcbOmsCode;
import com.nexgrid.adcb.util.Init;
import com.nexgrid.adcb.util.LogUtil;
import com.nexgrid.adcb.util.SendUtil;
import com.nexgrid.adcb.util.StringUtil;

import lguplus.u3.webservice.cm181.RetrieveMobilePayArmPsblYnServiceStub;
import lguplus.u3.webservice.cm181.RetrieveMobilePayArmPsblYnServiceStub.DsReqInVO;
import lguplus.u3.webservice.cm181.RetrieveMobilePayArmPsblYnServiceStub.DsResOutVO;
import lguplus.u3.webservice.cm181.RetrieveMobilePayArmPsblYnServiceStub.ESBHeader;
import lguplus.u3.webservice.cm181.RetrieveMobilePayArmPsblYnServiceStub.RequestBody;
import lguplus.u3.webservice.cm181.RetrieveMobilePayArmPsblYnServiceStub.RequestRecord;
import lguplus.u3.webservice.cm181.RetrieveMobilePayArmPsblYnServiceStub.ResponseBody;
import lguplus.u3.webservice.cm181.RetrieveMobilePayArmPsblYnServiceStub.ResponseRecord;
import lguplus.u3.webservice.cm181.RetrieveMobilePayArmPsblYnServiceStub.RetrieveMobilePayArmPsblYn;
import lguplus.u3.webservice.cm181.RetrieveMobilePayArmPsblYnServiceStub.RetrieveMobilePayArmPsblYnResponse;



@Service("commonService")
public class CommonServiceImpl implements CommonService{

	@Autowired
	private CommonDAO commonDAO;
	
	@Autowired
	private RbpClientService rbpClientService;
	
	@Autowired
	private RcsgClientService rcsgClientService;
	
	private org.slf4j.Logger serviceLog = LoggerFactory.getLogger(getClass());


	@Override
	public String getIpAddr (HttpServletRequest request) {
		// TODO Auto-generated method stub
		
		String ip = null;
        ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() < 0 || "unknown".equalsIgnoreCase(ip)) { 
            ip = request.getHeader("Proxy-Client-IP"); 
        } 
        if (ip == null || ip.length() < 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getHeader("WL-Proxy-Client-IP"); 
        } 
        if (ip == null || ip.length() < 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getHeader("HTTP_CLIENT_IP"); 
        } 
        if (ip == null || ip.length() < 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getHeader("HTTP_X_FORWARDED_FOR"); 
        }
        if (ip == null || ip.length() < 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getHeader("X-Real-IP"); 
        }
        if (ip == null || ip.length() < 0 || "unknown".equalsIgnoreCase(ip)) { 
            ip = request.getHeader("X-RealIP"); 
        }
        if (ip == null || ip.length() < 0 || "unknown".equalsIgnoreCase(ip)) { 
            ip = request.getHeader("REMOTE_ADDR");
        }
        if (ip == null || ip.length() < 0 || "unknown".equalsIgnoreCase(ip)) { 
            ip = request.getRemoteAddr(); 
        }
        return ip;
	}
	
	
	
	@Override
	public void contentTypeCheck(HttpServletRequest request, LogVO logVO) throws CommonException{
		// TODO Auto-generated method stub
		
		String content_type = request.getHeader("Content-Type");
		
		if(content_type == null) {
			throw new CommonException(EnAdcbOmsCode.INVALID_HEADER_KEY);
		}
		
		if((content_type.indexOf("application/json") < 0)){
			throw new CommonException(EnAdcbOmsCode.INVALID_HEADER_VALUE);
		}
		
	}
	
	
	
	@Override
	public void getNcasGetMethod(Map<String, Object> paramMap, LogVO logVO) throws Exception{
		
		String msisdn = StringUtil.getNcas444(paramMap.get("msisdn").toString());
		//String msisdn = paramMap.get("msisdn").toString();
		String ncasUrl = Init.readConfig.getNcas_url() + msisdn;
		HttpHeaders headers = new HttpHeaders();
		int connTimeout = Integer.parseInt(Init.readConfig.getNcas_connect_time_out());
		int readTimeout = Integer.parseInt(Init.readConfig.getNcas_read_time_out());
		
		ResponseEntity<String> resEntity = null;
		
		logVO.setNcasReqTime();
		logVO.setFlow("[ADCB] --> [NCAS]");
		try {
			
			resEntity = SendUtil.requestUrl(HttpMethod.GET, headers, null, ncasUrl, "NCAS", connTimeout, readTimeout, logVO);
			logVO.setFlow("[ADCB] <-- [NCAS]");
			logVO.setNcasResTime();	// ncas 연동 종료
			
			//NCAS연동 결과값
			Map<String, String> ncasRes = getNcasResHeader(resEntity);
			
	    	// paramMap에 NCAS 결과값 저장
	    	paramMap.put("ncasRes", ncasRes);
			
			String ctn = ncasRes.get("CTN");
	    	String respcode = ncasRes.get("RESPCODE"); // RESPCODE
	    	String res_msg = ncasRes.get("RESPMSG"); // 처리 결과 내용
	    	String sub_no = ncasRes.get("SUB_NO");	// 고객의 가입번호
	    	String pers_name = ncasRes.get("PERS_NAME"); // 실사용자명
	    	String fee_type = ncasRes.get("FEE_TYPE"); //요금제 타입    	
	    	String ban_unpaid_yn_code = ncasRes.get("BAN_UNPAID_YN_CODE"); // 연체여부
	    	String unit_loss_yn_code = ncasRes.get("UNIT_LOSS_YN_CODE"); // 분실여부
	    	String cust_type_code = ncasRes.get("CUST_TYPE_CODE"); // 개인,법인 구분 (I : 개인 / G : 법인)
	    	String ctn_stus_code = ncasRes.get("CTN_STUS_CODE"); // CTN 상태코드 (A:정상 / S:일시 중지)
	    	String pre_pay_code = StringUtils.defaultIfEmpty(ncasRes.get("PRE_PAY_CODE"), ""); // 선불가입코드(P:국제전화차단 / C:국제전화허용) 
	    																					//-> NULL이 아닌 경우 선불가입자, NULL인 경우 선불가입자 아님.
	    	String unit_mdl = ncasRes.get("UNIT_MDL"); // 단말기명
	    	String aceno = ncasRes.get("ACENO"); // 가입자 계약번호(기기변경, 번호변경 시에는 유지되나, 명의변경 시 변경됨.)
	    	String ban = ncasRes.get("BAN"); // 청구선 번호
	    	String svc_auth = ncasRes.get("SVC_AUTH"); // 부정사용자|장애인부가서비스|65세이상부가서비스
	    											// 입력정보: LRZ0001705|LRZ0003849|LRZ0003850
	    											// 출력정보: 0|1 (가입은 '1', 미가입은 '0')
	    	String young_fee_yn = ncasRes.get("YOUNG_FEE_YN"); // 실시간과금대상요금제(RCSG연동대상)
	    													// 실시간과금대상요금제에 가입되어있는 경우 'Y', 미가입은 'N'
	    	String frst_entr_dttm = ncasRes.get("FRST_ENTR_DTTM"); // 최초 개통일자
	    	String sub_birth_pers_id = ncasRes.get("SUB_BIRTH_PERS_ID"); // 명의자 생년월일
	    	String sub_sex_pers_id = ncasRes.get("SUB_SEX_PERS_ID"); // 명의자 성별
	    	String cust_flag = ncasRes.get("CUST_FLAG"); //고객정보 구분값 (ex: YL00000000)
	    											// 1번째 byte: 결제차단여부 ('Y':결제차단->결제이용동의 필요, 'N':결제가능->결제이용동의 완료)
	    											// 2번째 byte: PIN번호 설정여부 ('Y':PIN번호사용, 'N':PIN번호사용안함, '0'(숫자):PIN번호미설정, 'L':5회실패로 잠금상태)
	    	String law1HomeTelno = StringUtil.checkTrim(ncasRes.get("LAW1_HOME_TELNO")); //법정 대리인 전화번호
	    	String law1PersName = ncasRes.get("LAW1_PERS_NAME"); //법정 대리인 이름
	    	
	    	//NCAS 응답코드 저장
	    	logVO.setNcasResultCode(respcode);
	    	
	    	
	    	//고객정보가 없거나 번호 이동된  사용자 차단 - 해지된 사용자는 고객정보 없음으로 나옴
	    	//70 : 고객정보 없음  
			if("70".equals(respcode) ) {
				throw new CommonException(EnAdcbOmsCode.NCAS_70, res_msg);
			}
			

			// 71 : SKT로 번호이동  
			if("71".equals(respcode)) {
				throw new CommonException(EnAdcbOmsCode.NCAS_71, res_msg);
			}
			
			// 76 : KTF로 번호이동
			if("76".equals(respcode)) {
				throw new CommonException(EnAdcbOmsCode.NCAS_76, res_msg);
			}
			
			if(!"00".equals(respcode)) {
				throw new CommonException(EnAdcbOmsCode.NCAS_API.status(), EnAdcbOmsCode.NCAS_API.mappingCode(),  EnAdcbOmsCode.NCAS_API.value() + respcode, res_msg);
			}
			
			try {
				//CTN 값이 정상값이 아닐경우 차단
		    	int blockctn = 0;
		    	if(ctn.length() == 12){
		    		blockctn = commonDAO.getBlockCTN(ctn);
		    	}else {
		    		blockctn = 1;
		    	}
		    	if(blockctn != 0 ) {
		    		logVO.setFlow("[ADCB] <-- [DB]");
		    		throw new CommonException(EnAdcbOmsCode.DB_BLOCK_CTN);
		    	}
		    	
		    	//정상 요금제가 아니면 차단
		    	int blockfeetype = 0;
		    	if(!"".equals(fee_type)){
		    		blockfeetype = commonDAO.getBlockFeeType(fee_type);
		    	}else {
		    		blockfeetype = 1;
		    	}
		    	if(blockfeetype != 0) {
		    		logVO.setFlow("[ADCB] <-- [DB]");
		    		throw new CommonException(EnAdcbOmsCode.DB_BLOCK_FEETYPE);
		    	}
			}catch(DataAccessException adcbExc){
				/*SQLException se = (SQLException) adcbExc.getRootCause();
				logVO.setRsCode(Integer.toString(se.getErrorCode()));*/
				logVO.setFlow("[ADCB] --> [DB]");
				throw new CommonException(EnAdcbOmsCode.DB_ERROR, adcbExc.getMessage());
				
			}catch(ConnectException adcbExc) {
				logVO.setFlow("[ADCB] --> [DB]");
				throw new CommonException(EnAdcbOmsCode.DB_CONNECT_ERROR, adcbExc.getMessage());
			}catch(CommonException common) {
				throw common;
			}catch (Exception adcbExc) {
				throw new CommonException(EnAdcbOmsCode.DB_INVALID_ERROR, adcbExc.getMessage());
			}
			
		}catch(HttpClientErrorException adcbExc){
			
			logVO.setFlow("[ADCB] <-- [NCAS]");
			throw new CommonException(EnAdcbOmsCode.NCAS_INVALID_ERROR, adcbExc.getMessage());
		
		}catch(HttpServerErrorException adcbExc) {
			
			logVO.setFlow("[ADCB] <-- [NCAS]");
			throw new CommonException(EnAdcbOmsCode.NCAS_INVALID_ERROR, adcbExc.getMessage());
			
		}catch(UnknownHttpStatusCodeException adcbExc) {
			
			logVO.setFlow("[ADCB] <-- [NCAS]");
			throw new CommonException(EnAdcbOmsCode.NCAS_INVALID_ERROR, adcbExc.getMessage());
			
		}catch (ResourceAccessException adcbExc) {
			
			// connect, read time out
			if(adcbExc.getMessage().indexOf("Read") > 0) {
				throw new CommonException(EnAdcbOmsCode.NCAS_READ_TIMEOUT, adcbExc.getMessage());
			}else {
				throw new CommonException(EnAdcbOmsCode.NCAS_CONNECT_TIMEOUT, adcbExc.getMessage());
			}
		
		}catch(CommonException adcbExc) {
			
			throw adcbExc;
			
		}catch (Exception adcbExc) {
			
			throw new CommonException(EnAdcbOmsCode.NCAS_INVALID_ERROR, adcbExc.getMessage());
		}
		
		
	}



	@Override
	public Map<String,String> getNcasResHeader(ResponseEntity<String> responseEntity) throws Exception {
		
		Map<String, String> ncasRes = new HashMap<String, String>();
		
		String headerName = Init.readConfig.getNcas_header_name();
		String charset = Init.readConfig.getNcas_charset();
		String headerMsg = URLDecoder.decode(responseEntity.getHeaders().get(headerName).get(0), charset);
		StringTokenizer st = new StringTokenizer(headerMsg, "&amp;");
		
		while (st.hasMoreElements()) {
			String param = (String) st.nextElement();			
			String[] token = param.split("=");
			String key = "";
			String value = "";	
			// value값이 없는 것을 대비
			for (int i = 0; i < token.length; i++) {
				if(i==0)
					key = token[i];
				if(i==1)
					value = token[i];
			}	
			ncasRes.put(key, value);
		}
		
		return ncasRes;
	}
	
	
	
	

	@Override
	public void omsLogWrite(LogVO logVO) {
		// TODO Auto-generated method stub
		try {
			
			StringBuilder sb = new StringBuilder();
			
			LogUtil.setOmsLog(logVO);
			
			/*************************필수값*************************************/
			sb.append("SEQ_ID=").append(logVO.getSeqId()).append("|");
			sb.append("LOG_TIME=").append(logVO.getLogTime()).append("|");
			sb.append("LOG_TYPE=").append(logVO.getLogType()).append("|");
			sb.append("SID=").append(logVO.getSid()).append("|");
			sb.append("RESULT_CODE=").append(logVO.getResultCode()).append("|");
			sb.append("REQ_TIME=").append(logVO.getReqTime()).append("|");
			sb.append("RES_TIME=").append(logVO.getResTime()).append("|");
			sb.append("CLIENT_IP=").append(logVO.getClientIp()).append("|");
			sb.append("DEV_INFO=").append(logVO.getDevInfo()).append("|");
			sb.append("OS_INFO=").append(logVO.getOsInfo()).append("|");
			sb.append("NW_INFO=").append(logVO.getNwInfo()).append("|");
			sb.append("SVC_NAME=").append(logVO.getSvcName()).append("|");
			sb.append("DEV_MODEL=").append(logVO.getDevModel()).append("|");
			sb.append("CARRIER_TYPE=").append(logVO.getDevModel()).append("|");
			
			/*************************필수값 끝*************************************/
			
			

			sb.append("API_TYPE=").append(logVO.getApiType()).append("|");
			sb.append("API_RESULT=").append(logVO.getApiResultCode()).append("|");
			
			sb.append("NCAS_REQ_TIME=").append(logVO.getNcasReqTime()).append("|");
			sb.append("NCAS_RES_TIME=").append(logVO.getNcasResTime()).append("|");
			sb.append("NCAS_RESULT_CODE=").append(logVO.getNcasResultCode()).append("|");
			
			sb.append("RCSG_REQ_TIME=").append(logVO.getRcsgReqTime()).append("|");
			sb.append("RCSG_RES_TIME=").append(logVO.getRcsgResTime()).append("|");
			sb.append("RCSG_RESULT_CODE=").append(logVO.getRcsgResultCode()).append("|");
			
			sb.append("RBP_REQ_TIME=").append(logVO.getRbpReqTime()).append("|");
			sb.append("RBP_RES_TIME=").append(logVO.getRbpResTime()).append("|");
			sb.append("RBP_RESULT_CODE=").append(logVO.getRbpResultCode()).append("|");

			sb.append("ESB_MPS208_REQ_TIME=").append(logVO.getEsbMps208ReqTime()).append("|");
			sb.append("ESB_MPS208_RES_TIME=").append(logVO.getEsbMps208ResTime()).append("|");
			sb.append("ESB_MPS208_RESULT_CODE=").append(logVO.getEsbMps208ResultCode()).append("|");

			sb.append("ESB_CM181_REQ_TIME=").append(logVO.getEsbCm181ReqTime()).append("|");
			sb.append("ESB_CM181_RES_TIME=").append(logVO.getEsbCm181ResTime()).append("|");
			sb.append("ESB_CM181_RESULT_CODE=").append(logVO.getEsbCm181ResultCode()).append("|");

			sb.append("API_MPS208_REQ_TIME=").append(logVO.getApiMps208ReqTime()).append("|");
			sb.append("API_MPS208_RES_TIME=").append(logVO.getApiMps208ResTime()).append("|");
			sb.append("API_MPS208_RESULT_CODE=").append(logVO.getApiMps208ResultCode());


			
			Logger log = LogManager.getLogger("oms");
			log.info(sb);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	
	
	@Override
	public Map<String,Object> getSuccessResult() throws Exception {
		
		Map<String,Object> successResult = new HashMap<String,Object>();
		
		successResult.put("reasonCode", 0);
		successResult.put("message", "Success");
		
		return successResult;
	};
	
	
	@Override
	public boolean userEligibilityCheck(Map<String, Object> paramMap, LogVO logVO) throws Exception{
		
		Map<String, String> ncasRes = (HashMap<String,String>) paramMap.get("ncasRes");
		
    	String unit_loss_yn_code = ncasRes.get("UNIT_LOSS_YN_CODE"); // 분실여부
    	String cust_type_code = ncasRes.get("CUST_TYPE_CODE"); // 개인,법인 구분 (I : 개인 / G : 법인)
    	String ctn_stus_code = ncasRes.get("CTN_STUS_CODE"); // CTN 상태코드 (A:정상 / S:일시 중지)
    	String pre_pay_code = StringUtils.defaultIfEmpty(ncasRes.get("PRE_PAY_CODE"), ""); // 선불가입코드(P:국제전화차단 / C:국제전화허용) 
    																					//-> NULL이 아닌 경우 선불가입자, NULL인 경우 선불가입자 아님.
    	String svc_auth = ncasRes.get("SVC_AUTH"); // 부정사용자|장애인부가서비스|65세이상부가서비스
												// 입력정보: LRZ0001705|LRZ0003849|LRZ0003850
												// 출력정보: 0|1 (가입은 '1', 미가입은 '0')
    	String young_fee_yn = ncasRes.get("YOUNG_FEE_YN"); // 실시간과금대상요금제(RCSG연동대상)
    													// 실시간과금대상요금제에 가입되어있는 경우 'Y', 미가입은 'N'
    	String sub_birth_pers_id = ncasRes.get("SUB_BIRTH_PERS_ID"); // 명의자 생년월일
    	String sub_sex_pers_id = ncasRes.get("SUB_SEX_PERS_ID"); // 명의자 성별
    	String cust_flag = ncasRes.get("CUST_FLAG"); //고객정보 구분값 (ex: YL00000000)
    											// 1번째 byte: 결제차단여부 ('Y':결제차단->결제이용동의 필요, 'N':결제가능->결제이용동의 완료)
    											// 2번째 byte: PIN번호 설정여부 ('Y':PIN번호사용, 'N':PIN번호사용안함, '0'(숫자):PIN번호미설정, 'L':5회실패로 잠금상태)
    	String dual_ctn = ncasRes.get("DUAL_CTN"); // 듀얼넘버 확인, 듀얼넘버가 아니면 NULL
    	String ref_type_code = ncasRes.get("REF_TYPE_CODE"); // mvno: LGT가 아니면 차단
		
    	// test phone은 통과!
    	int testPhoneCnt = 0;
    	try {
    		testPhoneCnt = commonDAO.testPhoneCheck(ncasRes.get("CTN"));
		}catch(DataAccessException adcbExc){
			/*SQLException se = (SQLException) adcbExc.getRootCause();
			logVO.setRsCode(Integer.toString(se.getErrorCode()));*/
			logVO.setFlow("[ADCB] --> [DB]");
			throw new CommonException(EnAdcbOmsCode.DB_ERROR, adcbExc.getMessage());
			
		}catch(ConnectException adcbExc) {
			logVO.setFlow("[ADCB] --> [DB]");
			throw new CommonException(EnAdcbOmsCode.DB_CONNECT_ERROR, adcbExc.getMessage());
		}catch (Exception adcbExc) {
			throw new CommonException(EnAdcbOmsCode.DB_INVALID_ERROR, adcbExc.getMessage());
		}
    	if(testPhoneCnt == 0) {
    		// CUST_TYPE_CODE : 개인,법인구분(I : 개인 / G : 법인) - 법인폰 차단
        	if(!"I".equals(cust_type_code)) {
        		if("AccountProfile".equals(logVO.getApiType())) {
        			return false;
        		}else if("Charge".equals(logVO.getApiType())){	// Charge API 경우에만 ReasonCode 117 -> 104로 바꿔달라는 요청이 있었음.
        			throw new CommonException(HttpStatus.OK, EnAdcbOmsCode.NCAS_71.mappingCode(), EnAdcbOmsCode.NCAS_BLOCK_CORP.value(), EnAdcbOmsCode.NCAS_BLOCK_CORP.logMsg());
        		}else {
        			throw new CommonException(EnAdcbOmsCode.NCAS_BLOCK_CORP);
        		}
        		
        	}
        	
        	// CTN_STUS_CODE : CTN 상태 코드 (A : 정상 / S : 일시 중지) - 일시중지폰 차단
        	  if(!"A".equals(ctn_stus_code)){
        		  if("AccountProfile".equals(logVO.getApiType())) {
        			  return false;
        		  }else {
        			  throw new CommonException(EnAdcbOmsCode.NCAS_BLOCK_PAUSE);  
        		  }
        	  }
        	  
        	  // UNIT_LOSS_YN_CODE : 분실여부(Y,N) - 분실등록폰 차단
        	  if(!"N".equals(unit_loss_yn_code)) {
        		  if("AccountProfile".equals(logVO.getApiType())) {
        			  return false;
        		  }else {
        			  throw new CommonException(EnAdcbOmsCode.NCAS_BLOCK_LOSS);
        		  }
        		  
        		  
        	  }
        	  
        	  
        	  // PRE_PAY_CODE : NULL이 아닌 경우 선불가입자이며, NULL인 경우 선불가입자 아님. - 선불가입자 차단
        	  if(!"".equals(pre_pay_code)) {
        		  if("AccountProfile".equals(logVO.getApiType())) {
        			  return false;
        		  }else {
        			  throw new CommonException(EnAdcbOmsCode.NCAS_BLOCK_PREPAY);  
        		  }
        		  
        		  
        	  }
        	  
        	  // 듀얼 넘버 확인, NULL인 경우 듀얼넘버 아님. - 듀얼넘버 차단
        	  if(!"".equals(dual_ctn)) {
        		  if("AccountProfile".equals(logVO.getApiType())) {
        			  return false;
        		  }else {
        			  throw new CommonException(EnAdcbOmsCode.NCAS_BLOCK_DUAL);  
        		  }
        		  
        	  }
        	  
        	  
        	  // SVC_AUTH : LRZ0001705(부정사용자 코드) 부가서비스 가입여부 - 부정사용자 차단 (가입은'1' 미가입은'0')
        	  svc_auth = svc_auth.substring(0, 1);
        	  if(!"0".equals(svc_auth)) {
        		  if("AccountProfile".equals(logVO.getApiType())) {
        			  return false;
        		  }else {
        			  throw new CommonException(EnAdcbOmsCode.NCAS_BLOCK_IRREG);  
        		  }
        		  
        	  }
        	  
        	// REF_TYPE_CODE: LGT가 아니면 차단
        	  if(!"LGT".equals(ref_type_code)) {
        		  if("AccountProfile".equals(logVO.getApiType())) {
        			  return false;
        		  }else {
        			  throw new CommonException(EnAdcbOmsCode.NCAS_BLOCK_MVNO);
        		  }
        		  
        		  
        	  }
        	  
        	  
        	// 실사용자 만나이 구하기
      		int age = 0;
      		try {
      			age = StringUtil.calculateManAge(sub_birth_pers_id, sub_sex_pers_id);
      		} catch (Exception e) {
      			//에러가 날 경우 차단시킨다
      			age = 0;
      		}
    		// 만 14세 미만 차단
    		if(age < 14 ) {
    			if("AccountProfile".equals(logVO.getApiType())) {
      			  return false;
      		  	}else if("Charge".equals(logVO.getApiType())){	// Charge API 경우에만 ReasonCode 117 -> 104로 바꿔달라는 요청이 있었음.
        			throw new CommonException(HttpStatus.OK, EnAdcbOmsCode.NCAS_71.mappingCode(), EnAdcbOmsCode.NCAS_BLOCK_14.value(), EnAdcbOmsCode.NCAS_BLOCK_14.logMsg());
        		}else {
      		  		throw new CommonException(EnAdcbOmsCode.NCAS_BLOCK_14);
      		  	}
    			
    		}else {
    			// 14세 이상 중에 청소년요금제가 아닌 경우
    			if("N".equals(young_fee_yn)){
    				//통합한도 연동
    				return userGradeCheck(paramMap, logVO);
    			}
    		}
    	}
    	
    	
    	return true;
	};
	
	
	
	@Override
	public boolean userGradeCheck(Map<String, Object> paramMap, LogVO logVO) throws Exception{
		
		Map<String, String> ncasRes = (HashMap<String,String>) paramMap.get("ncasRes");
		String ctn = ncasRes.get("CTN");
    	String fee_type = ncasRes.get("FEE_TYPE"); //요금제 타입    	
    	
		Map<String, String> rbpReqMap = new HashMap<String, String>();	// RBP 요청
		Map<String, String> rbpResMap = null;	// RBP 응답
		
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		long tempTimeMillis = System.currentTimeMillis();
		String currentDate = dateFormat.format(new Date(tempTimeMillis));
		
		String br_id = RbpKeyGenerator.getInstance(Init.readConfig.getRbp_system_id()).generateKey();
		String reqCtn = StringUtil.getCtn344(ctn);
		
		// RBP연동을 위한 파마리터 셋팅
		rbpReqMap.put("CTN", reqCtn);	// 과금번호
		rbpReqMap.put("SOC_CODE", fee_type); // 가입자의 요금제 코드 
		rbpReqMap.put("CDRDATA", Init.readConfig.getRbp_cdrdata()); // CDR 버전
		rbpReqMap.put("BR_ID", br_id); // Business RequestID
		rbpReqMap.put("RCVER_CTN", reqCtn); // 수신자의 전화번호
		rbpReqMap.put("SERVICE_FILTER", reqCtn); // 발신 번호
		rbpReqMap.put("START_USE_TIME", currentDate); 
		rbpReqMap.put("CALLED_NETWORK", Init.readConfig.getRbp_called_network()); // 착신 사업자 코드
		rbpReqMap.put("PID", Init.readConfig.getRbp_pid()); // Product ID
		rbpReqMap.put("DBID", Init.readConfig.getRbp_dbid()); // DETAIL BILLING ID
		rbpReqMap.put("SVC_CTG", Init.readConfig.getRbp_svc_ctg());
		
		/*if(paramMap.containsKey("purchaseAmount")) {
			Map<String, Object> purchaseAmount = (HashMap<String, Object>)paramMap.get("purchaseAmount");
			String price = purchaseAmount.get("amount").toString();
			rbpReqMap.put("PRICE", price);
			rbpReqMap.put("AMOUNT", "1");
		}
		
		if(paramMap.containsKey("refundAmount")) {
			Map<String, Object> refundAmount = (HashMap<String, Object>)paramMap.get("refundAmount");
			String price = refundAmount.get("amount").toString();
			rbpReqMap.put("PRICE", price);
			rbpReqMap.put("AMOUNT", "1");
		}*/
		
		// 한도조회 요청 paramMap에 저장
		String opCode = Init.readConfig.getRbp_opcode_select();
		paramMap.put("Req_"+opCode, rbpReqMap);
		
		// RBP 연동
		logVO.setFlow("[ADCB] --> [RBP]");
		rbpResMap = rbpClientService.doRequest(logVO, opCode, paramMap);
	
		
		// 한도조회 결과 paramMap에 저장
		paramMap.put("Res_"+opCode, rbpResMap);
		
		if("AccountProfile".equals(logVO.getApiType())) { //  charge API result와 다르게 reasonCode=0 (OK), eligibility=false_1910_PAR 추가
			if(rbpResMap.get("adcbDiffResult") != null) {
				return false;
			}
		}
		
		if(rbpResMap.containsKey("CUST_GRD_CD")) {
			if(rbpResMap.get("CUST_GRD_CD").equals("7")) { // 7등급 차단
				if("AccountProfile".equals(logVO.getApiType())) {
					return false;
				}else {
					throw new CommonException(EnAdcbOmsCode.RBP_BLOCK_GRADE);
				}
				
			}
		}
		
		return true;

		
	}
	
	
	@Override
	public void reqBodyCheck(Map<String, Object> paramMap, LogVO logVO) throws Exception {
		// body key 체크
		if( paramMap==null || paramMap.size() == 0 || !paramMap.containsKey("msisdn") ) {
			throw new CommonException(EnAdcbOmsCode.INVALID_BODY_KEY);
		}
		
		// body value 체크
		String msisdn = paramMap.get("msisdn") == null ? "" : paramMap.get("msisdn").toString();
		if( "".equals(msisdn) || StringUtil.hasSpecialCharacter(msisdn) || StringUtil.spaceCheck(msisdn) || StringUtil.maxCheck(msisdn, 15) ) {
			throw new CommonException(EnAdcbOmsCode.INVALID_BODY_VALUE);
		}
		
		logVO.setSid(paramMap.get("msisdn").toString());
	}
	
	
	@Override
	public void insertSLA(Map<String, Object> paramMap, LogVO logVO) throws Exception{
		try {
			commonDAO.insertSLA(paramMap, logVO);
		}catch(DataAccessException adcbExc){
			/*SQLException se = (SQLException) adcbExc.getRootCause();
			logVO.setRsCode(Integer.toString(se.getErrorCode()));*/
			logVO.setFlow("[ADCB] --> [DB]");
			throw new CommonException(EnAdcbOmsCode.DB_ERROR, adcbExc.getMessage());
			
		}catch(ConnectException adcbExc) {
			logVO.setFlow("[ADCB] --> [DB]");
			throw new CommonException(EnAdcbOmsCode.DB_CONNECT_ERROR, adcbExc.getMessage());
		}catch (Exception adcbExc) {
			throw new CommonException(EnAdcbOmsCode.DB_INVALID_ERROR, adcbExc.getMessage());
		}
	}
	
	
	@Override
	public void doEsbCm181(Map<String, Object> paramMap, LogVO logVO) throws Exception {
		logVO.setFlow("[ADCB] --> [ESB]");

		String sub_no = paramMap.get("SUB_NO").toString();	// 고객의 가입번호
		String ctn = paramMap.get("CTN").toString(); // 12자리 CTN
		String mode = paramMap.get("MODE").toString();	// 1:장애인처리, 2:65세이상처리

		
		String esbUrl = Init.readConfig.getEsb_cm181_url();
		int esbTimeout = Integer.parseInt(Init.readConfig.getEsb_time_out());
		ESBHeader header = new ESBHeader();
		RequestRecord reqRecord = new RequestRecord();
		RequestBody reqBody = new RequestBody();
		ResponseRecord resRecord = null;
		
		
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
		reqBody.setDsReqInVO(reqVO);
		reqRecord.setRequestBody(reqBody);
		
		RetrieveMobilePayArmPsblYn reqIn = new RetrieveMobilePayArmPsblYn();
		reqIn.setRequestRecord(reqRecord);
		
		String seq = "[" + logVO.getSeqId() + "] ";
		try {
			
			logVO.setEsbCm181ReqTime();
			serviceLog.info(seq + "---------------------------- ESB(CM181) START ----------------------------");
			serviceLog.info(seq + "ESB(CM181) Request Url : " + esbUrl);
			serviceLog.info(seq + "ESB(CM181) Request Header : " + header.toString());
			serviceLog.info(seq + "ESB(CM181) Request Body : " + reqVO.toString());
			
			// ESB 호출
			RetrieveMobilePayArmPsblYnServiceStub stub = new RetrieveMobilePayArmPsblYnServiceStub(esbUrl);
			
			// ESB Timeout 셋팅
			ServiceClient serviceClient = stub._getServiceClient();
			serviceClient.getOptions().setTimeOutInMilliSeconds(esbTimeout);
			stub._setServiceClient(serviceClient);
			
			// ESB 호출 응답
			RetrieveMobilePayArmPsblYnResponse esbRes = stub.retrieveMobilePayArmPsblYn(reqIn);
			logVO.setFlow("[ADCB] <-- [ESB]");
			logVO.setEsbCm181ResTime();
			
			resRecord = esbRes.getResponseRecord();
			header = resRecord.getESBHeader();
			
			serviceLog.info(seq + "ESB(CM181) Response Header : " + header.toString());
			
			
		}catch(Exception e) {
			if (e.getCause() instanceof ConnectTimeoutException) {
				throw new CommonException(EnAdcbOmsCode.ESB_TIMEOUT);
			}else {
				throw new CommonException(EnAdcbOmsCode.ESB_INVALID_ERROR, e.getMessage());
			}
		}
		
		
		if("".equals(header.getErrCode())) {
			ResponseBody resBody = resRecord.getResponseBody();
			if(resBody != null) {
				DsResOutVO resVO = resBody.getDsResOutVO();
				serviceLog.info(seq + "ESB(CM181) Response Body : " + resVO.toString());
				// ESB 결과 저장
				paramMap.put("esbCm181Res", resVO);
			}
		}else{
			throw new CommonException(EnAdcbOmsCode.ESB_HEADER, header.getErrMsg());
		}
		
		serviceLog.info(seq + "---------------------------- ESB(CM181) END ----------------------------");

	}
	
	
	@Override
	public String getEsbTransactionId() {
		String dTime = StringUtil.getCurrentTimeMilli();
		
		Random random = new Random();
		Integer a = random.nextInt(9999999);
		String rand01 = String.format("%07d", a);
		
		return dTime + rand01;
	}
	
	
	
	@Override
	public void doRbpCancel(Map<String, Object> paramMap, LogVO logVO) throws Exception {
		
		Map<String, Object> payInfo = (Map<String, Object>)paramMap.get("payInfo");
		String ctn = StringUtil.getCtn344(payInfo.get("CTN").toString());
		String fee_type = payInfo.get("FEE_TYPE").toString();
		String br_id = payInfo.get("BR_ID").toString();
		String refundInfo = payInfo.get("REFUNDINFO").toString();
		String start_use_time = payInfo.get("START_USE_TIME").toString();
		String price = payInfo.get("AMOUNT").toString();
		
		
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
		
		// 결제취소 요청 paramMap에 저장
		String opCode = Init.readConfig.getRbp_opcode_cancel();
		paramMap.put("Req_"+opCode, rbpReqMap);
		
		logVO.setFlow("[ADCB] --> [RBP]");
		rbpResMap = rbpClientService.doRequest(logVO, opCode, paramMap);
		
		// 즉시차감 결과 paramMap에 저장
		paramMap.put("Res_"+opCode, rbpResMap);
	}
	
	/*
	 * 2020.01.28_par 추가
	 */
	@Override
	public void doRcsgCancel(Map<String, Object> paramMap, LogVO logVO) throws Exception {
		Map<String, Object> payInfo = (Map<String, Object>)paramMap.get("payInfo");
		String ctn = StringUtil.getCtn344(payInfo.get("CTN").toString());
		String fee_type = payInfo.get("FEE_TYPE").toString();
		String br_id = payInfo.get("BR_ID").toString();
		//String refundInfo = payInfo.get("REFUNDINFO").toString();
		String start_use_time = payInfo.get("START_USE_TIME").toString(); // 결제 시 start_use_time으로 셋팅
		String price = payInfo.get("AMOUNT").toString();
		
		
		Map<String, String> rcsgReqMap = new HashMap<String, String>();	// RCSG 요청
		Map<String, String> rcsgResMap = null;	// RCSG 응답
		
		// RCSG연동을 위한 파마리터 셋팅
		rcsgReqMap.put("CTN", ctn);	// 과금번호
		rcsgReqMap.put("SOC_CODE", fee_type); // 가입자의 요금제 코드 
		rcsgReqMap.put("CDRDATA", Init.readConfig.getRcsg_cdrdata()); // CDR 버전
		rcsgReqMap.put("BR_ID", br_id); // Business RequestID
		rcsgReqMap.put("RCVER_CTN", ctn); // 수신자의 전화번호
		//rcsgReqMap.put("SERVICE_FILTER", refundInfo); // 즉시차감 return 전문의 REFUNDINFO값을 넣는다.
		rcsgReqMap.put("SERVICE_FILTER", ctn);
		rcsgReqMap.put("START_USE_TIME", start_use_time); 
		rcsgReqMap.put("END_USE_TIME", StringUtil.getCurrentTimeMilli());
		rcsgReqMap.put("CALLED_NETWORK", Init.readConfig.getRcsg_called_network()); // 착신 사업자 코드
		rcsgReqMap.put("PRICE", price);
		rcsgReqMap.put("PID", Init.readConfig.getRcsg_pid()); // Product ID
		rcsgReqMap.put("DBID", Init.readConfig.getRcsg_dbid()); // DETAIL BILLING ID
	
		// 결제취소 요청 paramMap에 저장
		String opCode = Init.readConfig.getRcsg_opcode_cancel();
		paramMap.put("Req_"+opCode, rcsgReqMap);
		
		logVO.setFlow("[ADCB] --> [RCSG]");
		rcsgResMap = rcsgClientService.doRequest(logVO, opCode, paramMap);
			
		// 즉시차감 결과 paramMap에 저장
		paramMap.put("Res_"+opCode, rcsgResMap);

	}

	
	 public void setBalance(Map<String, Object> paramMap, LogVO logVO) throws Exception{
		 logVO.setFlow("[ADCB] --> [DB]");
			try {
				commonDAO.setBalance(paramMap);
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



	@Override
	public SmsSendVO addSmsInfo(Map<String, Object> paramMap, String contentType, String to_ctn) throws Exception {
				
		SmsSendVO smsVO = new SmsSendVO();
		smsVO.setGubun("02"); // sms:01, mms:02 (SMS-> MMS 수정 20190930_PAR)
		smsVO.setTo_ctn(to_ctn);
		
		
		if("limit_excess".equals(contentType)) {
			smsVO.setContent(Init.readConfig.getLimit_excess());
			smsVO.setRequest_id(paramMap.get("requestId").toString());
		
		}else if("limit_excess2".equals(contentType)) {
			smsVO.setContent(Init.readConfig.getLimit_excess2());
			smsVO.setRequest_id(paramMap.get("requestId").toString());
		
		}else if("charge_complete".equals(contentType)) {
			Map<String, String> reqCharge = (Map<String, String>)paramMap.get("Req_114");
			Map<String, String> resCharge = (Map<String, String>)paramMap.get("Res_114");
			Map<String, String> ncasRes = (HashMap<String,String>) paramMap.get("ncasRes");
			String young_fee_yn = ncasRes.get("YOUNG_FEE_YN"); // 실시간과금대상요금제(RCSG연동대상)
																// 실시간과금대상요금제에 가입되어있는 경우 'Y', 미가입은 'N'
			
			String start_date = reqCharge.get("START_USE_TIME"); // yyyyMMddHHmmssSSS
			int price = Integer.parseInt(reqCharge.get("PRICE"));
			// RBP의 경우에는 차감된 데이터가 오고, RCSG의 경우에는 차감되지 않은 데이터가 온다.
			int svc_ctg_avail = "N".equals(young_fee_yn) ? 
										Integer.parseInt(resCharge.get("SVC_CTG_AVAIL")) : Integer.parseInt(resCharge.get("INFO_AVAIL")) - price ;
			
			
			// [LG U+ 휴대폰결제]&#10;{month}/{day} {hour}:{minute}&#10;구글Play스토어&#10;{INFO_CHARGE}원&#10;잔여한도 {SVC_CTG_AVAIL}
			String msg = Init.readConfig.getCharge_complete();
			msg = msg.replace("{month}", start_date.substring(4, 6)).replace("{day}", start_date.substring(6, 8))
						.replace("{hour}", start_date.substring(8, 10)).replace("{minute}", start_date.substring(10, 12))
						.replace("{INFO_CHARGE}", StringUtil.getMsgPrice(price)).replace("{SVC_CTG_AVAIL}", StringUtil.getMsgPrice(svc_ctg_avail));
			smsVO.setContent(msg);
			smsVO.setRequest_id(paramMap.get("requestId").toString());
		
		}else if("section_excess".equals(contentType)) {
			Map<String, String> reqCharge = (Map<String, String>)paramMap.get("Req_114");
			String ctn = reqCharge.get("CTN");
			int limit = (Integer)paramMap.get("limit");
			
			// [LG U+ 안내]&#10;{ctn} 님이&#10;사용하신 정보이용료가&#10;{limitAmount}원을 초과하였습니다.
			String msg = Init.readConfig.getSection_excess();
			msg = msg.replace("{ctn}", StringUtil.getCtnForPerson(ctn)).replace("{limitAmount}", StringUtil.getMsgPrice(limit));
			smsVO.setContent(msg);
			smsVO.setRequest_id(paramMap.get("requestId").toString());
			
		}else if("cancel_complete".equals(contentType)) {
			Map<String, String> reqCancel = (Map<String, String>)(paramMap.containsKey("Req_116") ? paramMap.get("Req_116") : paramMap.get("Req_117"));
			Map<String, String> resCancel = (Map<String, String>)(paramMap.containsKey("Res_116") ? paramMap.get("Res_116") : paramMap.get("Res_117"));
			Map<String, Object> payInfo = (Map<String, Object>)paramMap.get("payInfo");
			String young_fee_yn = payInfo.get("YOUNG_FEE_YN").toString(); // 실시간과금대상요금제(RCSG연동대상)
																			// 실시간과금대상요금제에 가입되어있는 경우 'Y', 미가입은 'N'			
			String start_date = reqCancel.get("END_USE_TIME"); // yyyyMMddHHmmssSSS
			int price = Integer.parseInt(reqCancel.get("PRICE"));

			// RBP의 경우에는 차감 취소된 데이터가 오고, RCSG의 경우에는 차감 취소되지 않은 데이터가 온다. 2019.11.01_par_수정
			int svc_ctg_avail = "N".equals(young_fee_yn) ? 
										Integer.parseInt(resCancel.get("SVC_CTG_AVAIL")) : Integer.parseInt(resCancel.get("INFO_AVAIL")) + price ;
										
			// [LG U+ 취소안내]&#10;{month}/{day} {hour}:{minute}&#10;ITUNES.COM&#10;{INFO_CHARGE}원&#10;잔여한도 {SVC_CTG_AVAIL}원
			String msg = Init.readConfig.getCancel_complete();
			msg = msg.replace("{month}", start_date.substring(4, 6)).replace("{day}", start_date.substring(6, 8))
					.replace("{hour}", start_date.substring(8, 10)).replace("{minute}", start_date.substring(10, 12))
					.replace("{INFO_CHARGE}", StringUtil.getMsgPrice(price)).replace("{SVC_CTG_AVAIL}", StringUtil.getMsgPrice(svc_ctg_avail));
			smsVO.setContent(msg);
			
			// 취소일 경우에는 requestId가 아니라 chargeRequestId (BOKU의 요청 Body)
			smsVO.setRequest_id(paramMap.containsKey("requestId") ? paramMap.get("requestId").toString() : paramMap.get("chargeRequestId").toString());
		}
		
	
		
		
		return smsVO;
	}



	@Override
	public void addCancelSuccessSMS(Map<String, Object> paramMap) throws Exception {
		Map<String, Object> payInfo = (Map<String, Object>)paramMap.get("payInfo");
		String ctn = payInfo.get("CTN").toString();
		
		List<SmsSendVO> smsList = new ArrayList<>();
		// 본인-결제취소 SMS
		smsList.add(addSmsInfo(paramMap, "cancel_complete", StringUtil.getCtn344(ctn)));
		
		// 취약계층일 경우 대리인에게 결제취소 SMS 발송
		if(paramMap.containsKey("esbCm181Res")) {
			DsResOutVO esbVO = (DsResOutVO)paramMap.get("esbCm181Res");
			if("Y".equals(esbVO.getAgntRegYn())) {
				// 대리인 - 결제취소 SMS
				smsList.add(addSmsInfo(paramMap, "cancel_complete", esbVO.getHpno()));
			}
		}
		
		// 결제 취소 SMS 전송 정보 paramMap에 저장.
		paramMap.put("smsList", smsList);
	}



	@Override
	public void insertSmsList(Map<String, Object> paramMap, LogVO logVO) throws Exception {
		try {
			List<SmsSendVO> smsList = (List<SmsSendVO>) paramMap.get("smsList");
			for(int i=0; i<smsList.size(); i++) {
				commonDAO.insertSmsList(smsList.get(i));
			}
			
		}catch(DataAccessException adcbExc){
			/*SQLException se = (SQLException) adcbExc.getRootCause();
			logVO.setRsCode(Integer.toString(se.getErrorCode()));*/
			logVO.setFlow("[ADCB] --> [DB]");
			throw new CommonException(EnAdcbOmsCode.DB_ERROR, adcbExc.getMessage());
			
		}catch(ConnectException adcbExc) {
			logVO.setFlow("[ADCB] --> [DB]");
			throw new CommonException(EnAdcbOmsCode.DB_CONNECT_ERROR, adcbExc.getMessage());
		}catch (Exception adcbExc) {
			throw new CommonException(EnAdcbOmsCode.DB_INVALID_ERROR, adcbExc.getMessage());
		}
	}



	@Override
	public void maintenanceCheck() throws Exception {

		if("Y".equals(Init.readConfig.getMaintenance_yn())) {
			throw new CommonException(EnAdcbOmsCode.MAINTENANCE);
		}
	}


	 
	 
	
	
	
	 
	
}
