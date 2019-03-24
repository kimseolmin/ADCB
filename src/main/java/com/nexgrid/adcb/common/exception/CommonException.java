package com.nexgrid.adcb.common.exception;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.ibatis.io.Resources;
import org.slf4j.LoggerFactory;


public class CommonException extends Exception{
	
	private static org.slf4j.Logger serviceLog = LoggerFactory.getLogger(CommonException.class);
	
	String resReasonCode = "";	// 응답 reason코드
	String resMsg = "";			// 응답 message
	String omsErrCode = "";		// OMS Result Code
	String logMsg = "";			// Service log message
	String flow = "";			
	
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

	public String getFlow() {
		return flow;
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
	 * @param sCode
	 * @summary 다이렉트 에러 발생 처리
	 */
	public CommonException (int statusCode, String mappingCode, String eCode, String msg, String flow) {
	
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
		try {
			byte[] resBody = msg==null?null:msg.getBytes("UTF-8");
			byteToString = new String(resBody,0,resBody.length);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.logMsg = byteToString;
		this.flow = flow;
//		this.serviceRs = prop.getProperty(key)
	}
	
	
	/**
	 * @param sCode
	 * @summary 다이렉트 에러 발생 처리
	 */
	public CommonException (int statusCode, String mappingCode, String eCode, String flow) {
	
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
		//String msg = this.resMsg;
		this.omsErrCode = eCode; //result_code
		try {
			byte[] resBody = logMsg==null?null:logMsg.getBytes("iso-8859-1");
			byteToString = new String(resBody,0,resBody.length);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.logMsg = byteToString;
		this.flow = flow;
//		this.serviceRs = prop.getProperty(key)
	}


	
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
	
	public static Map<String, Object> checkException(Map<String, Object> paramMap, String seq, String flow) {
		// TODO Auto-generated method stub
		Map<String, Object> excMap = new HashMap<String, Object>();
		
		CommonException adcbEx = new CommonException((int)paramMap.get("sCode"), paramMap.get("apiResultCode").toString(), paramMap.get("eCode").toString(), flow);
		
		excMap.put("reasonCode", Integer.parseInt(adcbEx.resReasonCode));
		excMap.put("message", adcbEx.resMsg);
		
		return excMap;
		
	}
	
}
