package com.nexgrid.adcb.interworking.rbp.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nexgrid.adcb.interworking.rbp.message.EnRbpInvokeConQry;
import com.nexgrid.adcb.interworking.rbp.message.EnRbpInvokeSelectLimit;
import com.nexgrid.adcb.interworking.rbp.message.EnRbpReturnConQry;
import com.nexgrid.adcb.util.Init;
import com.nexgrid.adcb.util.StringUtil;

public class RbpMessageConverter {

public static final Logger logger = LoggerFactory.getLogger(RbpMessageConverter.class);
	
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
		//tagMap.put("CONTENTS_UNIT", "51");
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
		tagMap.put("REASON_QUOTA", "117");
		tagMap.put("SVC_CTG_LIMIT", "118");
		tagMap.put("SVC_CTG_AVAIL", "119");
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
		tagMap.put("SVC_CTG", "123");
		tagMap.put("UNLIMIT", "124");
		tagMap.put("PAY_METHOD", "125");
		tagMap.put("CARD_CMPY", "126");
		tagMap.put("MSG_ID", "127");
		tagMap.put("ETC_LIMIT", "128");
		tagMap.put("ETC_AVAIL", "129");
		tagMap.put("REFUNDINFO", "130");
		tagMap.put("LMT_USE_DENY_YN", "131");
		tagMap.put("SMLS_USE_DENY_YN", "132");
		tagMap.put("DRGT_ISOL_CD", "133");
		tagMap.put("CUST_KD_CD", "134");
		tagMap.put("CUST_GRD_CD", "135");
		
		String tagKey = null;
		Iterator<String> it = tagMap.keySet().iterator();
		while(it.hasNext())
		{
			tagKey = it.next();
			tagValMap.put(tagMap.get(tagKey), tagKey);
		}
	}
	
	
	public RbpMessageConverter() {
		
	}
	
	//opCode별로 invoke message를 생성
	public synchronized String getInvokeMessage(String msgGbn, String opCode, Map<String, String> invokeMsg) {
		String header = "";
		String body = "";
		
		
		if(Init.readConfig.getRbp_opcode_con_qry().equals(opCode)) { // 연결 상태 확인
			if(Init.readConfig.getRbp_msg_gbn_invoke().equals(msgGbn)) { // 상태 확인 invoke
				for(EnRbpInvokeConQry e : EnRbpInvokeConQry.values()) {
					body += getStrParameter(tagMap.get(e.toString()), invokeMsg.get(e.toString()), null);
				}
			}else { // 상태 확인 응답에 대한 invoke
				for(EnRbpReturnConQry e : EnRbpReturnConQry.values()) {
					body += getStrParameter(tagMap.get(e.toString()), invokeMsg.get(e.toString()), null); 
				}
			}
		}else if(Init.readConfig.getRbp_opcode_select().equals(opCode)) { // 한도 조회
			for(EnRbpInvokeSelectLimit e : EnRbpInvokeSelectLimit.values()) {
				body += getStrParameter(tagMap.get(e.toString()), invokeMsg.get(e.toString()), e.getDefaultValue());
			}
		}else if(Init.readConfig.getRbp_opcode_charge().equals(opCode)) { // 한도 즉시 차감
			
		}
		
		
		return header + body;
				
	}
	
	// 파라미터 구성부를 String 형태로 만듦
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
}
