package com.nexgrid.adcb.common.service;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.UnknownHttpStatusCodeException;

import com.nexgrid.adcb.common.exception.CommonException;
import com.nexgrid.adcb.common.vo.LogVO;
import com.nexgrid.adcb.util.Init;
import com.nexgrid.adcb.util.LogUtil;
import com.nexgrid.adcb.util.SendUtil;
import com.nexgrid.adcb.util.StringUtil;



@Service("commonService")
public class CommonServiceImpl implements CommonService{

	/*@Resource(name = "commonDAO")
	private CommonDAO commonDAO;*/
	
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
		
		String msisdn = StringUtil.getNcas444(paramMap.get("msisdn").toString());
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
			
			//ncas연동 결과값
			Map<String, String> ncasRes = getNcasResHeader(resEntity);
			
			
			String ctn = ncasRes.get("CTN");
	    	String respcode = ncasRes.get("RESPCODE");
	    	String res_msg = ncasRes.get("RESPMSG");
	    	String sub_no = ncasRes.get("SUB_NO");
	    	String pers_name = ncasRes.get("PERS_NAME");
	    	String fee_type = ncasRes.get("FEE_TYPE");	    	
	    	String ban_unpaid_yn_code = ncasRes.get("BAN_UNPAID_YN_CODE");    	
	    	String unit_loss_yn_code = ncasRes.get("UNIT_LOSS_YN_CODE");
	    	String cust_type_code = ncasRes.get("CUST_TYPE_CODE");
	    	String ctn_stus_code = ncasRes.get("CTN_STUS_CODE");
	    	String pre_pay_code = StringUtils.defaultIfEmpty(ncasRes.get("PRE_PAY_CODE"), "");
	    	String unit_mdl = ncasRes.get("UNIT_MDL");
	    	String aceno = ncasRes.get("ACENO");
	    	String ban = ncasRes.get("BAN");	
	    	String svc_auth = ncasRes.get("SVC_AUTH");
	    	String young_fee_yn = ncasRes.get("YOUNG_FEE_YN");
	    	String frst_entr_dttm = ncasRes.get("FRST_ENTR_DTTM");
	    	String sub_birth_pers_id = ncasRes.get("SUB_BIRTH_PERS_ID");
	    	String sub_sex_pers_id = ncasRes.get("SUB_SEX_PERS_ID");
	    	String cust_flag = ncasRes.get("CUST_FLAG"); //동의여부
	    	String law1HomeTelno = StringUtil.checkTrim(ncasRes.get("LAW1_HOME_TELNO")); //법정 대리인 전화번호
	    	String law1PersName = ncasRes.get("LAW1_PERS_NAME"); //법정 대리인 이름
	    	
	    	
	    	//고객정보가 없거나 번호 이동된  사용자 차단 - 해지된 사용자는 고객정보 없음으로 나옴
	    	//70 : 고객정보 없음  71 : SKT로 번호이동  76 : KTF로 번호이동
			if("70".equals(respcode) || "71".equals(respcode) || "76".equals(respcode)) {
				throw new CommonException("400", "105", "500000" + respcode, res_msg, logVO.getFlow());
			}
					
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
				throw new CommonException("500", "4", "50000001", "NCAS ReadTimeout" + adcbExc.getMessage(), logVO.getFlow());
			}else {
				throw new CommonException("500", "4", "50000002", "NCAS ConnectTimeout" + adcbExc.getMessage(), logVO.getFlow());
			}
		}catch(CommonException adcbExc) {
			throw adcbExc;
		}
		catch (Exception adcbExc) {
			throw new CommonException("500", "4", "59999999", adcbExc.getMessage(), logVO.getFlow());
		}
		finally {
			
		}
		
		
	}



	// ncas 헤더 응답값 map으로 반환.
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

}
