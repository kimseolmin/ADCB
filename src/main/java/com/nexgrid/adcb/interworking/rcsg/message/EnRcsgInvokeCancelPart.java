package com.nexgrid.adcb.interworking.rcsg.message;

public enum EnRcsgInvokeCancelPart { // 2020.01.28_par 생성
// 필수인 항목과 amount만 보냄
	  CTN
	, SOC_CODE
	, CDRDATA
	, BR_ID
//	, RETRY_CNT
//	, SVC_ID
//	, DUAL_MODE
	, MSG_RCVER_CNT("1")
	, RCVER_CTN
	, MSG_REPEAT_CNT("1")
	, SERVICE_TYPE("00")
	, SERVICE_FILTER
	, START_USE_TIME
	, STATUS("101C") //차감취소시 101C 고정
	, END_USE_TIME
//	, CALL_REFERENCE
	, CALLED_NETWORK("102200")
	, AMOUNT("1")
	, PRICE
//	, SETTLEMENT_CP_CD
//	, CID
//	, DCID
	, MPCOUNT("0")
	, BILL_FLAG("00")
//	, MENU_ID
//	, SUB_FLAG
//	, CHANNEL_CD
//	, BUYING_TYPE
	, RSRC_FTR_CD("ZZ")
	, PID
//	, SUBPID
	, DBID
//	, TERM_OS
//	, BUY_SRC
//	, NET_CODE
	;
	
	private int tagLength = 3;
	private String defaultValue = null;
	
	EnRcsgInvokeCancelPart() {
	}
	
	EnRcsgInvokeCancelPart(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public int getTagLength() {
		return tagLength;
	}

	public String getDefaultValue() {
		return defaultValue;
	}
	
}
