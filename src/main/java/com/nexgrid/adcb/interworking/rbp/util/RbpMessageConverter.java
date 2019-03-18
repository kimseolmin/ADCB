package com.nexgrid.adcb.interworking.rbp.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
	
	public RbpMessageConverter() {
		
	}
}
