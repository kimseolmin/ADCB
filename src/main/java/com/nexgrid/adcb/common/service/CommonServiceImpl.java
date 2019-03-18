package com.nexgrid.adcb.common.service;

import java.net.ConnectException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.UnknownHttpStatusCodeException;

import com.nexgrid.adcb.common.dao.CommonDAO;
import com.nexgrid.adcb.common.exception.CommonException;
import com.nexgrid.adcb.common.vo.LogVO;
import com.nexgrid.adcb.interworking.rbp.util.RbpKeyGenerator;
import com.nexgrid.adcb.util.Init;
import com.nexgrid.adcb.util.LogUtil;
import com.nexgrid.adcb.util.SendUtil;
import com.nexgrid.adcb.util.StringUtil;



@Service("commonService")
public class CommonServiceImpl implements CommonService{

	@Inject
	private CommonDAO commonDAO;
	
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
			throw new CommonException("400", "2", "30200001", "Invalid Request Header[Key]: Content-Type", logVO.getFlow());
		}
		
		if(!(content_type.equals("application/json"))){
			throw new CommonException("400", "2", "30200002", "Invalid Request Header[Value]: Content-Type", logVO.getFlow());
		}
		
	}
	
	
	// ncas연동
	@Override
	public void getNcasGetMethod(Map<String, Object> paramMap, LogVO logVO) throws Exception{
		
		//String msisdn = StringUtil.getNcas444(paramMap.get("msisdn").toString());
		String msisdn = paramMap.get("msisdn").toString();
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
	    	String svc_auth = ncasRes.get("SVC_AUTH"); // 요금제, 부가서비스, 월정액 가입여부
	    											// 입력정보: LRZ1111111|LRZ1111112|LRZ1111113
	    											// 출력정보: 0|0|1 (가입은 '1', 미가입은 '0')
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
				throw new CommonException("400", "105", "511000" + respcode, res_msg, logVO.getFlow());
			}
			

			// 71 : SKT로 번호이동  76 : KTF로 번호이동
			if("71".equals(respcode) || "76".equals(respcode)) {
				throw new CommonException("400", "104", "511000" + respcode, res_msg, logVO.getFlow());
			}

			
			
			
			//CTN 값이 정상값이 아닐경우 차단
	    	int blockctn = 0;
	    	if(ctn.length() == 12){
	    		blockctn = commonDAO.getBlockCTN(ctn);
	    	}else {
	    		blockctn = 1;
	    	}
	    	if(blockctn != 0 ) {
	    		logVO.setFlow("[ADCB] <-- [DB]");
	    		throw new CommonException("400", "121", "51000"+"XXX", "BlockCTN", logVO.getFlow());
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
	    		throw new CommonException("400", "121", "51000"+"XXX", "BlockFeeType", logVO.getFlow());
	    	}
	    	
	    	
	    	
	    	// paramMap에 NCAS 결과값 저장
	    	paramMap.put("ncasRes", ncasRes);
	    	
	    						
		}catch(HttpClientErrorException adcbExc){
			
			logVO.setFlow("[ADCB] <-- [NCAS]");
			throw new CommonException("500", "4", "59999999", "NCAS HttpClientError" + adcbExc.getMessage(), logVO.getFlow());
		
		}catch(HttpServerErrorException adcbExc) {
			
			logVO.setFlow("[ADCB] <-- [NCAS]");
			throw new CommonException("500", "4", "59999999", "NCAS HttpServerError"+ adcbExc.getMessage(), logVO.getFlow());
			
		}catch(UnknownHttpStatusCodeException adcbExc) {
			
			logVO.setFlow("[ADCB] <-- [NCAS]");
			throw new CommonException("500", "4", "59999999", "NCAS UnknownHttpStatusCode"+ adcbExc.getMessage(), logVO.getFlow());
			
		}catch (ResourceAccessException adcbExc) {
			
			// connect, read time out
			if(adcbExc.getMessage().indexOf("Read") > 0) {
				throw new CommonException("500", "4", "50000"+"XXX", "NCAS ReadTimeout" + adcbExc.getMessage(), logVO.getFlow());
			}else {
				throw new CommonException("500", "4", "50000"+"XXX", "NCAS ConnectTimeout" + adcbExc.getMessage(), logVO.getFlow());
			}
		
		}catch(DataAccessException adcbExc){
			
			SQLException se = (SQLException) adcbExc.getRootCause();
			logVO.setRsCode(Integer.toString(se.getErrorCode()));
			logVO.setFlow("[ADCB] --> [DB]");
			throw new CommonException("500", "4", "48000000", se.getMessage(), logVO.getFlow());
			
		}catch(ConnectException adcbExc) {
			
			logVO.setFlow("[ADCB] <-- [DB]");
			throw new CommonException("500", "4", "48000000", adcbExc.getMessage(), logVO.getFlow());
			
		}catch(CommonException adcbExc) {
			
			throw adcbExc;
			
		}catch (Exception adcbExc) {
			
			throw new CommonException("500", "4", "59999999", adcbExc.getMessage(), logVO.getFlow());
		}
		finally {
			
		}
		
		
	}



	// ncas 헤더 응답값 map으로 반환.
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
			sb.append("CONN_FLOW=").append(logVO.getConnectionFlow() == null ? "" : logVO.getConnectionFlow()).append("|");
			sb.append("API_RST_CODE=").append(logVO.getApiResultCode() == null ? "" : logVO.getApiResultCode()).append("|");
			sb.append("RS_CODE=").append(logVO.getRsCode() == null ? "" : logVO.getRsCode());
			
			
			sb.append("NCAS_REQ_TIME=").append(logVO.getNcasReqTime() == null ? "" : logVO.getNcasReqTime()).append("|");
			sb.append("NCAS_RES_TIME=").append(logVO.getNcasResTime() == null ? "" : logVO.getNcasResTime()).append("|");
			sb.append("NCAS_RESULT_CODE=").append(logVO.getNcasResultCode() == null ? "" : logVO.getNcasResultCode()).append("|");
				
			
			
			
			
			Logger log = LogManager.getLogger("oms");
			log.info(sb);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	
	
	// 성공일 경우 boku 응답 결과리턴
	@Override
	public Map<String,Object> getSuccessResult() throws Exception {
		
		Map<String,Object> successResult = new HashMap<String,Object>();
		
		successResult.put("reasonCode", 0);
		successResult.put("message", "Success");
		
		return successResult;
	};
	
	
	// 사용자 청구 자격 체크
	@Override
	public boolean userEligibilityCheck(Map<String, Object> paramMap, LogVO logVO) throws Exception{
		
		Map<String, String> ncasRes = (HashMap<String,String>) paramMap.get("ncasRes");
		
		//String ctn = ncasRes.get("CTN");
    	//String respcode = ncasRes.get("RESPCODE"); // RESPCODE
    	//String res_msg = ncasRes.get("RESPMSG"); // 처리 결과 내용
    	//String sub_no = ncasRes.get("SUB_NO");	// 고객의 가입번호
    	//String pers_name = ncasRes.get("PERS_NAME"); // 실사용자명
    	//String fee_type = ncasRes.get("FEE_TYPE"); //요금제 타입    	
    	//String ban_unpaid_yn_code = ncasRes.get("BAN_UNPAID_YN_CODE"); // 연체여부
    	String unit_loss_yn_code = ncasRes.get("UNIT_LOSS_YN_CODE"); // 분실여부
    	String cust_type_code = ncasRes.get("CUST_TYPE_CODE"); // 개인,법인 구분 (I : 개인 / G : 법인)
    	String ctn_stus_code = ncasRes.get("CTN_STUS_CODE"); // CTN 상태코드 (A:정상 / S:일시 중지)
    	String pre_pay_code = StringUtils.defaultIfEmpty(ncasRes.get("PRE_PAY_CODE"), ""); // 선불가입코드(P:국제전화차단 / C:국제전화허용) 
    																					//-> NULL이 아닌 경우 선불가입자, NULL인 경우 선불가입자 아님.
    	//String unit_mdl = ncasRes.get("UNIT_MDL"); // 단말기명
    	//String aceno = ncasRes.get("ACENO"); // 가입자 계약번호(기기변경, 번호변경 시에는 유지되나, 명의변경 시 변경됨.)
    	//String ban = ncasRes.get("BAN"); // 청구선 번호
    	String svc_auth = ncasRes.get("SVC_AUTH"); // 요금제, 부가서비스, 월정액 가입여부
    											// 입력정보: LRZ1111111|LRZ1111112|LRZ1111113
    											// 출력정보: 0|0|1 (가입은 '1', 미가입은 '0')
    	String young_fee_yn = ncasRes.get("YOUNG_FEE_YN"); // 실시간과금대상요금제(RCSG연동대상)
    													// 실시간과금대상요금제에 가입되어있는 경우 'Y', 미가입은 'N'
    	//String frst_entr_dttm = ncasRes.get("FRST_ENTR_DTTM"); // 최초 개통일자
    	String sub_birth_pers_id = ncasRes.get("SUB_BIRTH_PERS_ID"); // 명의자 생년월일
    	String sub_sex_pers_id = ncasRes.get("SUB_SEX_PERS_ID"); // 명의자 성별
    	String cust_flag = ncasRes.get("CUST_FLAG"); //고객정보 구분값 (ex: YL00000000)
    											// 1번째 byte: 결제차단여부 ('Y':결제차단->결제이용동의 필요, 'N':결제가능->결제이용동의 완료)
    											// 2번째 byte: PIN번호 설정여부 ('Y':PIN번호사용, 'N':PIN번호사용안함, '0'(숫자):PIN번호미설정, 'L':5회실패로 잠금상태)
    	//String law1HomeTelno = StringUtil.checkTrim(ncasRes.get("LAW1_HOME_TELNO")); //법정 대리인 전화번호
    	//String law1PersName = ncasRes.get("LAW1_PERS_NAME"); //법정 대리인 이름
    	
 
    	// CTN_STUS_CODE : CTN 상태 코드 (A : 정상 / S : 일시 중지) - 일시중지폰 차단
    	  if(!"A".equals(ctn_stus_code)){
    		  throw new CommonException("400", "104", "51000"+"XXX", "일시중지폰 차단", logVO.getFlow());
    	  }
    	  
    	  // UNIT_LOSS_YN_CODE : 분실여부(Y,N) - 분실등록폰 차단
    	  if(!"N".equals(unit_loss_yn_code)) {
    		 throw new CommonException("400", "104", "51000"+"XXX", "분실등록폰 차단", logVO.getFlow());
    	  }
    	  
    	  
    	  // PRE_PAY_CODE : NULL이 아닌 경우 선불가입자이며, NULL인 경우 선불가입자 아님. - 선불가입자 차단
    	  if(!"".equals(pre_pay_code)) {
    		 throw new CommonException("400", "118", "51000"+"XXX", "선불가입자 차단", logVO.getFlow());
    	  }
    	  
    	  
    	  // SVC_AUTH : LRZ0001705(부정사용자 코드) 부가서비스 가입여부 - 부정사용자 차단 (가입은'1' 미가입은'0')
    	  if(!"0".equals(svc_auth)) {
    		 throw new CommonException("400", "118", "51000"+"XXX", "선불가입자 차단", logVO.getFlow());
    	  }
    	  
    	  
    	  // 결제차단여부 ('Y':결제차단->결제이용동의 필요, 'N':결제가능->결제이용동의 완료)
    	  cust_flag = cust_flag.substring(0, 1);
    	  if(!"N".equals(cust_flag)) {
    		  throw new CommonException("400", "4xx", "51000"+"XXX", "결제이용동의 필요", logVO.getFlow());
    	  }
    	  
    	  
    	// 실사용자 만나이 구하기
		int age = 0;
		if(cust_type_code.equals("I")){
			try {
				age = StringUtil.calculateManAge(sub_birth_pers_id, sub_sex_pers_id);
			} catch (Exception e) {
				//에러가 날경우 차단시킨다
				age = 0;
			}
		}
		// 만 14세 미만 차단
		if(age < 14 ) {
			throw new CommonException("400", "117", "51000"+"XXX", "만 14세 미만 차단", logVO.getFlow());
		}else {
			// 14세 이상 중에 청소년요금제가 아닌 경우
			if("N".equals(young_fee_yn)){
				//통합한도 연동
			}
			
		}
    	  
    	return true;
	};
	
	
	
	// 통합한도 연동
	public boolean userGradeCheck(Map<String, Object> paramMap, LogVO logVO) throws Exception{
		
		Map<String, String> ncasRes = (HashMap<String,String>) paramMap.get("ncasRes");
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
    	String svc_auth = ncasRes.get("SVC_AUTH"); // 요금제, 부가서비스, 월정액 가입여부
    											// 입력정보: LRZ1111111|LRZ1111112|LRZ1111113
    											// 출력정보: 0|0|1 (가입은 '1', 미가입은 '0')
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
		
		
		
    	
    	
		Map<String, String> rbpReqMap = new HashMap<String, String>();	// RBP 요청
		Map<String, String> rbpResMap = null;	// RBP 응답
		
		
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		long tempTimeMillis = System.currentTimeMillis();
		String currentDate = dateFormat.format(new Date(tempTimeMillis));
		
		String br_id = RbpKeyGenerator.getInstance(Init.readConfig.getRbp_system_id()).generateKey();
		String reqCtn = StringUtil.getCtn344(ctn);
		
		// RBP연동을 윟안 파마리터 셋팅
		rbpReqMap.put("CTN", reqCtn);	// 과금번호
		rbpReqMap.put("SOC_CODE", fee_type); // 가입자의 요금제 코드 
		rbpReqMap.put("CDRDATA", Init.readConfig.getRbp_cdrdata()); // CDR 버전
		rbpReqMap.put("BR_ID", br_id); // Business RequestID
		rbpReqMap.put("RCVER_CTN", reqCtn); // 수신자의 전화번호
		rbpReqMap.put("SERVICE_FILTER", reqCtn); // 발신 번호
		rbpReqMap.put("START_USE_TIME", currentDate); // 발신 번호
		rbpReqMap.put("CALLED_NETWORK", Init.readConfig.getRbp_called_network()); // 착신 사업자 코드
		rbpReqMap.put("PID", Init.readConfig.getRbp_pid()); // Product ID
		rbpReqMap.put("DBID", Init.readConfig.getRbp_dbid()); // DETAIL BILLING ID
		
		
		logVO.setFlow("[SVC] --> [RBP]");
		
		
		
		
		
		
		
		
		
		return true;
	}
}
