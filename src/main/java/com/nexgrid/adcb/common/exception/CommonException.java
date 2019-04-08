 package com.nexgrid.adcb.common.exception;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;
import java.util.Map;
import java.util.Properties;

import org.apache.ibatis.io.Resources;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import com.nexgrid.adcb.util.EnAdcbOmsCode;


public class CommonException extends Exception{
	
	private static org.slf4j.Logger serviceLog = LoggerFactory.getLogger(CommonException.class);
	
	String resReasonCode = "";	// 응답 reason코드
	String resMsg = "";			// 응답 message
	String omsErrCode = "";		// OMS Result Code
	String logMsg = "";			// Service log message		
	
	int statusCode;
	
	Exception cause;
	
	
	
	public String getResReasonCode() {
		return resReasonCode;
	}
	
	public String getResMsg() {
		return resMsg;
	}
	
	public String getOmsErrCode() {
		return omsErrCode;
	}
	
	public int getStatusCode() {
		return statusCode;
	}

	
	public String getLogMsg() {
		return logMsg;
	}
	
	
	
	
	@Override
	public String getMessage() {
		// TODO Auto-generated method stub
		return super.getMessage() == null ? logMsg : super.getMessage();
	}

	/**
	 * EnAdcbOmsCode에 정의가 되어있지 않은 에러코드를 찍으려 할 경우
	 * @param httpStatus HTTP 상태코드
	 * @param mappingCode BOKU 응답 매핑 코드
	 * @param eCode OMS ResultCode
	 * @param msg 에러 시 서비스 로그에 찍힐 메시지
	 */
	public CommonException (HttpStatus httpStatus, String mappingCode, String eCode, String msg) {
	
		init(httpStatus.value(), mappingCode, eCode, msg);
	}
	
	
	/**
	 * CommonException 초기화: EnAdcbOmsCode에 정의가 되어있지 않은 에러코드를 찍으려 할 경우 + 예외 로그 메시지가 없을 경우
	 * @param httpStatus HTTP 상태코드
	 * @param mappingCode BOKU 응답 매핑 코드
	 * @param eCode OMS ResultCode
	 */
	public CommonException (HttpStatus httpStatus, String mappingCode, String eCode) {
	
		
		init(httpStatus.value(), mappingCode, eCode, null);
	}
	
	
	/**
	 * CommonException 초기화: EnAdcbOmsCode에 정의가 되어있지 않은 에러코드를 찍으려 할 경우 + 예외 로그 메시지가 없을 경우
	 * @param httpStatus HTTP 상태코드
	 * @param mappingCode BOKU 응답 매핑 코드
	 * @param eCode OMS ResultCode
	 */
	public CommonException (int httpStatus, String mappingCode, String eCode) {
	
		
		init(httpStatus, mappingCode, eCode, null);
	}
	
	
	
	
	/**
	 * CommonException 초기화: EnAdcbOmsCode에 정의되어 있지 않은 로그 메시지를 찍어야 할 경우
	 * @param oms OMS의 ResultCode에 대한 ENUM
	 * @param msg 에러 시 서비스 로그에 찍힐 메시지
	 */
	public CommonException (EnAdcbOmsCode oms, String msg) {
		init(oms.status().value(), oms.mappingCode(), oms.value(), msg);
	}
	
	
	
	/**
	 * CommonException 초기화: EnAdcbOmsCode에 초기화에 대한 정보가 모두 있는 경우
	 * @param oms OMS의 ResultCode에 대한 ENUM
	 */
	public CommonException (EnAdcbOmsCode oms) {
		init(oms.status().value(), oms.mappingCode(), oms.value(), oms.logMsg());
	}
	
	
	/**
	 * CommonException 초기화
	 * @param statusCode HTTP 상태코드
	 * @param mappingCode BOKU 응답 매핑 코드
	 * @param eCode OMS ResultCode
	 * @param msg 에러 시 서비스 로그에 찍힐 메시지
	 */
	private void init(int statusCode, String mappingCode, String eCode, String msg) {
    	Properties prop = new Properties(); 
    	readProp(prop);
    	String byteToString = "";
    	this.statusCode = statusCode;
    	this.resReasonCode = mappingCode;
    	/*if(prop.getProperty(mappingCode)!=null) { //매칭되는 코드 없을 시 5001 반환
			this.errCode = prop.getProperty(mappingCode);
		} else {
			this.errCode = "5001";
		}*/
    	this.resMsg = prop.getProperty(resReasonCode) == null ? prop.getProperty("4") : prop.getProperty(resReasonCode); //에러코드로 현대 에러메시지 뽑음
		this.omsErrCode = eCode; //result_code
		if(msg != null) {
			try {
				byte[] resBody = msg.getBytes("UTF-8");
				byteToString = new String(resBody,0,resBody.length);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.logMsg = byteToString;
		}
		
//		this.serviceRs = prop.getProperty(key)
	}


	
	/**
	 * BOKU에게 전달할 응답 메시지
	 * @return BOKU에게 전달할 응답 메시지
	 */
	public Map<String, Object> sendException(){
		Map<String, Object> result = new HashMap<>();
		
		result.put("reasonCode", Integer.parseInt(getResReasonCode()) );
		result.put("message", getResMsg());
		
		return result;
	}

	
	public static void readProp(Properties prop) {
		// TODO Auto-generated method stub
		
		try {
			InputStream is = Resources.getResourceAsStream("/conf/properties/error_properties.xml");
			prop.loadFromXML(is);
		} catch (InvalidPropertiesFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			serviceLog.error("config file error");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			serviceLog.error("config file error");
		}
	}
	
	
	/**
	 * 알 수 없는 예외일 경우 사용
	 * @param paramMap sCode: Http 상태코드, apiResultCode: Boku 응답 매핑 코드, eCode: OMS ResultCode
	 * @return BOKU에게 전달할 응답 메시지
	 */
	public static Map<String, Object> checkException(Map<String, Object> paramMap) {
		// TODO Auto-generated method stub
		Map<String, Object> excMap = new HashMap<String, Object>();
		
		CommonException adcbEx = new CommonException((Integer)paramMap.get("sCode"), paramMap.get("apiResultCode").toString(), paramMap.get("eCode").toString());
		
		excMap.put("reasonCode", Integer.parseInt(adcbEx.resReasonCode));
		excMap.put("message", adcbEx.resMsg);
		
		return excMap;
		
	}
	
}
