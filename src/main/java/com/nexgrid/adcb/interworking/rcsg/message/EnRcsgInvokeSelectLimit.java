package com.nexgrid.adcb.interworking.rcsg.message;

public enum EnRcsgInvokeSelectLimit {
	CTN
	, SOC_CODE
	, CDRDATA
	, BR_ID
	, RETRY_CNT
	, SVC_ID
	, MSG_RCVER_CNT("1")
	, RCVER_CTN
	, MSG_REPEAT_CNT("1")
	, SERVICE_TYPE("00")
	, SERVICE_FILTER
	, START_USE_TIME
	, CALL_REFERENCE
	, AMOUNT("1")
	, PRICE
	, BILL_FLAG("00")
	, SUB_FLAG
	, CALLED_NETWORK
	, RSRC_FTR_CD("ZZ")
	, PID
	, SUBPID
	, DBID
	;
	
	private int tagLength = 3;
	private String defaultValue = null;
	
	EnRcsgInvokeSelectLimit(){
		
	}
	
	EnRcsgInvokeSelectLimit(String defaultValue){
		this.defaultValue = defaultValue;
	}
	
	public int getTagLength() {
		return tagLength;
	}

	public String getDefaultValue() {
		return defaultValue;
	}
}
