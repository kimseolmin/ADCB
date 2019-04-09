package com.nexgrid.adcb.util;

import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.nexgrid.adcb.common.vo.LogVO;


public class LogUtil {
	
	private static org.slf4j.Logger serviceLog = LoggerFactory.getLogger(LogUtil.class);

	/**
	 * Service Start Log Print
	 * @param logVO
	 * @param request
	 */
	public static void startServiceLog(LogVO logVO, HttpServletRequest request) {

		try {
		
			//MDC.put("SeqId", logVO.getSeqId());
			String seq = "[" + logVO.getSeqId() + "] ";
			
			serviceLog.info(seq + "/********************** ADCB API " + logVO.getApiType() + " START **********************/");
			
			logVO.setClientIp(request.getRemoteAddr());
			serviceLog.info(seq + "RequestRemoteAddr : " + logVO.getClientIp());
			
			if(logVO.getApiType() == "INVAILD") {
				serviceLog.info(seq + "RequestUrl : " + request.getRequestURL().toString().replace(request.getServletPath().toString(), request.getAttribute(RequestDispatcher.FORWARD_SERVLET_PATH).toString()));
			} else {
				serviceLog.info(seq + "RequestUrl : " + request.getRequestURL());
			}
			
			serviceLog.info(seq + "RequestMethod : " + request.getMethod());
			
			Enumeration<String> parameters = request.getParameterNames();
			Enumeration<String> headers = request.getHeaderNames();
			
			String reqParam = "";
			
			if(parameters.hasMoreElements()) {
				for(; parameters.hasMoreElements();) {
					String name = (String)parameters.nextElement();
					reqParam += "[" + name + " : " + request.getParameter(name) + "]";
					if(parameters.hasMoreElements() == true) {
						reqParam += ",";
					}
				}
				
				serviceLog.info(seq + "Request Param : " + reqParam);
				
				reqParam = "";
			}
			
			
			for(; headers.hasMoreElements();) {
				String name = (String)headers.nextElement();
				reqParam += "[" + name + " : " + request.getHeader(name) + "]";
				if(headers.hasMoreElements() == true) {
					reqParam += ",";
				}
			}
			
			serviceLog.info(seq + "Request Header : " + reqParam);
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * reqBody가 있는 경우 추가
	 * @param logVO
	 * @param request
	 * @param paramMap
	 */
	public static void startServiceLog(LogVO logVO, HttpServletRequest request, String reqBody) {

		try {
		
			startServiceLog(logVO, request);
			String seq = "[" + logVO.getSeqId() + "] ";
			if(reqBody !=null && reqBody.length() > 0) {
				serviceLog.info(seq + "Request Body : " + reqBody);
			}else {
				serviceLog.info(seq + "Request Body : ");
			}
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
	/**
	 * Service Stop Log Print (client에게 응답을 주는 것과 동시에 서비스 로그가 끝날 때)
	 * @param dataMap
	 * @param logVO
	 */
	public static void EndServiceLog(Map<String, Object> dataMap, LogVO logVO) {
		
		try {
			
			String seq = "[" + logVO.getSeqId() + "] ";
			serviceLog.info(seq + "Response Data : " + dataMap);
			serviceLog.info(seq + "/********************** ADCB API "+ logVO.getApiType() +" END **********************/");
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Service Stop Log Print (client에게 먼저 응답을 주고 서비스로그는 나중에 끝날 때)
	 * @param logVO
	 */
	public static void EndServiceLog(LogVO logVO) {
		
		try {
			
			String seq = "[" + logVO.getSeqId() + "] ";
			serviceLog.info(seq + "/********************** ADCB API "+ logVO.getApiType() +" END **********************/");
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
	/**
	 * Print Reference Parameters
	 * @param logVO
	 * @param logMap
	 */
	public static void printParamLog(LogVO logVO, Map<String, Object> logMap) {
		// TODO Auto-generated method stub
		String seq = "[" + logVO.getSeqId() + "] ";
		try {
			for (String key : logMap.keySet()){
				serviceLog.info(seq + key +" = " + logMap.get(key));
		    }
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	

	
	/**
	 * Print Connect API(HTTP API) Request Data 
	 * @param httpMethod API 요청 HttpMethod
	 * @param headers API 요청 header
	 * @param reqData API 요청 데이터
	 * @param url API 요청 url
	 * @param conAPI 요청하는 API 구분
	 * @param logVO
	 */
	public static void printAPIReqData(HttpMethod httpMethod, HttpHeaders headers, String reqData, String url, String conAPI, LogVO logVO) {
		
		try {
			
			String seq = "[" + logVO.getSeqId() + "] ";
			
			serviceLog.info(seq + "---------------------------- "+ conAPI +" START ----------------------------");
			serviceLog.info(seq + conAPI + " RequestUrl : " + url);
			
			serviceLog.info(seq + conAPI + " RequestMethod : " + httpMethod);

			int count = 0;
			
			for(String key : headers.keySet()) {
				
				if(count == 0) {
					serviceLog.info(seq + conAPI + " Request Header : " + key + " : " + headers.get(key));
				} else {
					serviceLog.info(seq + new String(new char[conAPI.length()]).replace("\0", " ") + "                 " + key + " : " + headers.get(key));
				}
				
				count++;
			}
			
			if(reqData != null) {
				String data =  reqData.replace("\\\"", "\""); 
				serviceLog.info(seq + conAPI + " Request Data = " + data);
			}
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	

	/*public static void printAPIResData(LogVO logVO, String conAPI, String resData) {
		
		try {
			
			String seq = "[" + logVO.getSeqId() + "] ";
			String data = (resData==null)?"":resData.replace("\\\"", "\""); 
			serviceLog.info(seq + conAPI + " Response Data = " + data);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}*/
	
	
	/**
	 * Print Connect API Response Data
	 * @param logVO
	 * @param conAPI 요청하는 API 구분
	 * @param responseEntity API 응답 Entity
	 */
	public static void printAPIResData(LogVO logVO, String conAPI, ResponseEntity<String> responseEntity) {
		
		try {
			
			String seq = "[" + logVO.getSeqId() + "] ";
			String resHeader = responseEntity.getHeaders().toString().trim();
			String resBody = responseEntity.getBody().toString().trim();
			
			if(!"".equals(resHeader)) {
				serviceLog.info(seq + conAPI + " Response Header = " + URLDecoder.decode(resHeader, "UTF-8"));
			}
			
			if(!"".equals(resBody)) {
				serviceLog.info(seq + conAPI + " Response Body = " + URLDecoder.decode(resBody, "UTF-8"));
			}
			
			serviceLog.info(seq + "---------------------------- "+ conAPI +" END ----------------------------");
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	/*//Print Error Data 
	public static void printErrorData(LogVO logVO, String msg, Map<String, Object> dataMap) {
		
		try {
			
			String seq = "[" + logVO.getSeqId() + "] ";
			String data = (dataMap==null)?"":dataMap.toString().replace("\\\"", "\""); 
			serviceLog.info(seq + msg + data);
		
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}*/
	
	
	
	/*//print Request Body Log
	public static void printBodyLog(LogVO logVO, StringBuilder body) {
		
		try {
			
			String seq = "[" + logVO.getSeqId() + "] ";
			serviceLog.info(seq + "Request Body : " + body);
		
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}*/
	
	
	public static void setOmsLog(LogVO logVO) {
		// TODO Auto-generated method stub
		logVO.setLogTime();
		logVO.setLogType();
		logVO.setNwInfo("HTTPS");

	}
	
}