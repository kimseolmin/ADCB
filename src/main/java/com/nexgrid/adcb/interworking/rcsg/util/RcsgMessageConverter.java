package com.nexgrid.adcb.interworking.rcsg.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.nexgrid.adcb.interworking.rcsg.message.EnRcsgHeader;
import com.nexgrid.adcb.interworking.rcsg.message.EnRcsgInvokeCancel;
import com.nexgrid.adcb.interworking.rcsg.message.EnRcsgInvokeCancelPart;
import com.nexgrid.adcb.interworking.rcsg.message.EnRcsgInvokeCharge;
import com.nexgrid.adcb.interworking.rcsg.message.EnRcsgInvokeConQry;
import com.nexgrid.adcb.interworking.rcsg.message.EnRcsgInvokeSelectLimit;
import com.nexgrid.adcb.interworking.rcsg.message.EnRcsgReturnConQry;
import com.nexgrid.adcb.util.Init;
import com.nexgrid.adcb.util.StringUtil;

@Component
public class RcsgMessageConverter {

	public static final Logger logger = LoggerFactory.getLogger(RcsgMessageConverter.class);
	
	private static Map<String, String> tagMap;
	private static Map<String, String> tagValMap;
	
	static
	{
		tagMap = new HashMap<String, String>();
		tagValMap = new HashMap<String, String>();
		
		tagMap.put("AMOUNT", "11");
		tagMap.put("BILL_FLAG", "12");
		tagMap.put("BR_ID", "100");
		tagMap.put("BUYING_TYPE", "14");
		tagMap.put("CALL_REFERENCE", "15");
		tagMap.put("CALLED_NETWORK", "16");
		tagMap.put("CDRDATA", "17");
		tagMap.put("CONTENTS_UNIT", "51");
		tagMap.put("CHANNEL_CD", "18");
		tagMap.put("CID", "19");
		tagMap.put("CON_ID", "20");
		tagMap.put("CON_STS", "21");
		tagMap.put("CTN", "101");
		tagMap.put("DBID", "23");
		tagMap.put("DCID", "24");
		tagMap.put("DUAL_MODE", "25");
		tagMap.put("END_USE_TIME", "26");
		tagMap.put("MENU_ID", "28");
		tagMap.put("MPCOUNT", "30");
		tagMap.put("PID", "31");
		tagMap.put("RCVER_CTN", "32");
		tagMap.put("SERVICE_FILTER", "35");
		tagMap.put("SOC_CODE", "102");
		tagMap.put("START_USE_TIME", "37");
		tagMap.put("STATUS", "38");
		tagMap.put("SUB_FLAG", "39");
		tagMap.put("SUBPID", "40");
		tagMap.put("SVC_ID", "104");
		tagMap.put("RSRC_FTR_CD", "42");
		tagMap.put("RETRY_CNT", "43");
		tagMap.put("RBALANCE", "103");
		tagMap.put("INFO_LIMIT", "112");
		tagMap.put("INFO_AVAIL", "113");
		tagMap.put("FSMS_BASE", "114");
		tagMap.put("FSMS_AVAIL", "115");
		tagMap.put("INFO_CHARGE", "116");
		tagMap.put("RESULT", "200");
		tagMap.put("REMAIN_REQ", "45");
		tagMap.put("SERVICE_TYPE", "46");
		tagMap.put("SETTLEMENT_CP_CD", "44");
		tagMap.put("MSG_REPEAT_CNT", "48");
		tagMap.put("MSG_RCVER_CNT", "49");
		tagMap.put("DUAL_RESULT", "50");
		tagMap.put("PRICE", "51");
		tagMap.put("TERM_OS", "120");
		tagMap.put("BUY_SRC", "121");
		tagMap.put("NET_CODE", "122");

		
		String tagKey = null;
		Iterator<String> it = tagMap.keySet().iterator();
		while(it.hasNext())
		{
			tagKey = it.next();
			tagValMap.put(tagMap.get(tagKey), tagKey);
		}
	}
	
	public RcsgMessageConverter() {
		
	}
	
	
	
	/**
	 * opCode별로 RCSG 요청 message를 생성 
	 * @param msgGbn 1:invoke, 2:return
	 * @param opCode 001:연결상태확인, 111:한도조회, 114:한도즉시차감
	 * @param reqMap RCSG 요청데이터
	 * @return
	 */
	public synchronized String getInvokeMessage(String msgGbn, String opCode, ConcurrentHashMap<String, String> reqMap) {
		String header = "";
		String body = "";
		
		// 파라미터 구성부 설정
		if(Init.readConfig.getRcsg_opcode_con_qry().equals(opCode)) { // 연결 상태 확인
			if(Init.readConfig.getRcsg_msg_gbn_invoke().equals(msgGbn)) { // 상태 확인 invoke
				for(EnRcsgInvokeConQry e : EnRcsgInvokeConQry.values()) {
					body += getStrParameter(tagMap.get(e.toString()), reqMap.get(e.toString()), null);
				}
			}else { // 상태 확인 응답에 대한 invoke
				for(EnRcsgReturnConQry e : EnRcsgReturnConQry.values()) {
					body += getStrParameter(tagMap.get(e.toString()), reqMap.get(e.toString()), null);
				}
			}
		}else if(Init.readConfig.getRcsg_opcode_select().equals(opCode)) { // 한도 조회
			for(EnRcsgInvokeSelectLimit e : EnRcsgInvokeSelectLimit.values()) {
				body += getStrParameter(tagMap.get(e.toString()), reqMap.get(e.toString()), e.getDefaultValue());
			}
		}else if(Init.readConfig.getRcsg_opcode_charge().equals(opCode)) { // 한도 즉시 차감
			for(EnRcsgInvokeCharge e : EnRcsgInvokeCharge.values()) {
				body += getStrParameter(tagMap.get(e.toString()), reqMap.get(e.toString()), e.getDefaultValue());
			}
		}else if(Init.readConfig.getRbp_opcode_cancel().equals(opCode)) { // 결제 취소 (2020.01.28_par 추가)
			for(EnRcsgInvokeCancel e : EnRcsgInvokeCancel.values()) {
				body += getStrParameter(tagMap.get(e.toString()), reqMap.get(e.toString()), e.getDefaultValue());
			}
		}else if(Init.readConfig.getRbp_opcode_cancel_part().equals(opCode)) { // 결제 취소 (2020.01.28_par 추가)
			for(EnRcsgInvokeCancelPart e : EnRcsgInvokeCancelPart.values()) {
				body += getStrParameter(tagMap.get(e.toString()), reqMap.get(e.toString()), e.getDefaultValue());
			}
		}
		
		//header 설정
		reqMap.put("BODY_LENGTH", body.length()+"");
		for(EnRcsgHeader e : EnRcsgHeader.values()) {
			String defaultValue = e.getDefaultValue();
			header += StringUtil.lPad(defaultValue == null ? reqMap.get(e.toString()) : defaultValue, e.getTagLength());
		}
		
		
		return header + body;
		
	}
	
	
	/**
	 * RCSG 요청 데이터의 파라미터 구성부(TAG+LENGTH+VALUE)를 String 형태로 만듦
	 * @param tag 파마리터 구성부: TAG 구분값
	 * @param val 파라미터 구성부: VALUE
	 * @param defaultVal default값 없는 경우 null로 넘어옴
	 * @return
	 */
	private String getStrParameter(String tag, String val, String defaultVal) {
		String str = "";
		
		if(defaultVal != null) {
			val = defaultVal;
		}
		
		str += StringUtil.lPad(tag, 3); // 파마리터 구성부: TAG
		str += StringUtil.lPad((val==null ? 0 : val.length()) + "", 3); // 파라미터 구성부: LENGTH
		
		if(val != null) {
			str += val; // 파라미터 구성부: VALUE
		}
		
		return str;
	}
	
	
	
	/**
	 * RCSG 응답 메시지를  Map으로 변환
	 * @param returnMessage RCSG 응답 메시지
	 * @return
	 * @throws Exception
	 */
	public Map<String, String> parseReturnMessage(String returnMessage)throws Exception{
		
		Map<String, String> resMap = new HashMap<String, String>();
		
		// header값 추출
		int amount = 0;
		try {
			amount = parseHeaderParameter(resMap, returnMessage);
		}catch (Exception e) {
			// header형식  오류의 경우 그냥 예외를 던진다. (header가 정확하지 않으면 어떤 thread의 요청이었는지 알 수 없다.)
			throw e;
		}
		
		
		// body값 추출
		try {
			parseBodyParamter(amount, resMap, returnMessage);
		}catch(Exception e){
			// body형식의 오류인 경우 요청 thread를 알 수 있기 때문에 변환 된 것 까지만 반환하고 오류에러는 요청 thread에서 찍는다.
			return resMap;
		}
		
		
		return resMap;
	}
	
	
	
	/**
	 * header message를 분석하여 map으로 전환
	 * @param resMap header message가 저장될 맵
	 * @param returnMessage RCSG 응답 메시지
	 * @return RCSG 응답 메시지의 header length
	 */
	private int parseHeaderParameter(Map<String, String> resMap, String returnMessage) {
		
		int valLength = 0;
		String val = null;
		int amount = 0;
		
		for(EnRcsgHeader e : EnRcsgHeader.values()) {
			valLength = e.getTagLength();
			val = returnMessage.substring(amount, amount + valLength).trim();
			amount += valLength;
			resMap.put(e.toString(), val);
		}
		
		return amount;
	}
	
	
	
	/**
	 * body message를 tag로 분석하여 map으로 전환
	 * @param offset RCSG 응답 메시지의 header length
	 * @param resMap body message가 저장될 맵
	 * @param returnMessage RCSG 응답 메시지
	 */
	private void parseBodyParamter(int offset, Map<String, String> resMap, String returnMessage) {
		String tag = null;
		int valLength = 0;
		String val = null;
		int amount = offset;
		
		while(amount < returnMessage.length()) {
			// 파라미터 구성부: TAG
			tag = returnMessage.substring(amount, amount + 3).trim();
			amount += 3;
			
			// 파라미터 구성부: LENGTH
			valLength = Integer.parseInt(returnMessage.substring(amount, amount + 3).trim());
			amount += 3;
			
			// 파라미터 구성부: VALUE
			val = returnMessage.substring(amount, amount + valLength).trim();
			amount += valLength;
			
			resMap.put(tagValMap.get(tag), val);
		}
	}
}
