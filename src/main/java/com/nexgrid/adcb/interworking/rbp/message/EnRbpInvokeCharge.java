package com.nexgrid.adcb.interworking.rbp.message;

public enum EnRbpInvokeCharge {
	
	  CTN
	, SOC_CODE
	, CDRDATA
	, BR_ID
	, RETRY_CNT
	, SVC_ID
	, DUAL_MODE
	, MSG_RCVER_CNT("1")
	, RCVER_CTN
	, MSG_REPEAT_CNT("1")
	, SERVICE_TYPE("00")
	, SERVICE_FILTER
	, START_USE_TIME
	, STATUS("100C")
	, END_USE_TIME
	, CALL_REFERENCE
	, CALLED_NETWORK("102200")
	, AMOUNT("1")
	, PRICE
	, SETTLEMENT_CP_CD
	, CID
	, DCID
	, MPCOUNT("0")
	, BILL_FLAG("00")
	, MENU_ID
	, SUB_FLAG
	, CHANNEL_CD
	, BUYING_TYPE
	, RSRC_FTR_CD("ZZ")
	, PID
	, SUBPID
	, DBID
	, TERM_OS
	, BUY_SRC
	, NET_CODE
	, SVC_CTG
	, UNLIMIT
	, PAY_METHOD
	, CARD_CMPY
	;
	
	private int tagLength = 3;
	private String defaultValue = null;
	
	EnRbpInvokeCharge() {
	}
	
	EnRbpInvokeCharge(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public int getTagLength() {
		return tagLength;
	}

	public String getDefaultValue() {
		return defaultValue;
	}
	
	
}
