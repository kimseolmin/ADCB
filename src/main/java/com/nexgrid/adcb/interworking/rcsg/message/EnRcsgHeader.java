package com.nexgrid.adcb.interworking.rcsg.message;

import com.nexgrid.adcb.util.Init;

public enum EnRcsgHeader {
	
	PROTOCOL_GBN(3, "OCS")
	, INTERFACE_VERSION(3, Init.readConfig.getRbp_interface_version()) // 프로토콜 구분자
	, SOURCE_SYSTEM_ID(6, Init.readConfig.getRbp_system_id()) // 연동버전 정의
	, MESSAGE_GBN(1) //1:Invoke, 2:Return
	, SEQUENCE_NO(8)
	, OP_CODE(3)
	, BODY_LENGTH(4);
	
	private int tagLength = 0;
	private String defaultValue = null;
	
	EnRcsgHeader(int tagLength) {
		this.tagLength = tagLength;
	}
	
	EnRcsgHeader(int tagLength, String defaultValue) {
		this.tagLength = tagLength;
		this.defaultValue = defaultValue;
	}
	
	public int getTagLength() {
		return tagLength;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

}
