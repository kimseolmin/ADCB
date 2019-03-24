package com.nexgrid.adcb.util;

public enum EnAdcbOmsCode {
	
	SUCCESS("20000000")
	, INVALID_URL_KEY("30100001", "2", "요청 URL 전문 형식 불일치")
	, INVALID_URL_VALUE("30100002", "2", "요청 URL 필수 파라미터 값 오류")
	, INVALID_HEADER_KEY("30200001", "2", "요청 HEADER 전문 형식 불일치")
	, INVALID_HEADER_VALUE("30200002", "2", "요청 HEADER 필수 파라미터 값 오류")
	, INVALID_BODY_KEY("30300001", "2", "요청 BODY 전문 형식 불일치")
	, INVALID_BODY_VALUE("30300002", "2", "요청 BODY 필수 파라미터 값 오류")
	, DB("40000")
	, NCAS_API("511000")		// [NCAS 연동]: NCAS가 주는 응답코드 일 경우 (NCAS 응답코드는 2자리.- 511000XX)
	, RBP_API("5210");			// [RBP 연동]: RBP가 주는 응답코드 일 경우 (RBP 응답코드는 4자리. - 5210XXXX)
	
	
	
	private String value = "";
	private String mappingCode = "";
	private String logMsg = "";
	
	
	EnAdcbOmsCode(String value){
		this.value =  value;
	}
	
	EnAdcbOmsCode(String value, String mappingCode, String logMsg){
		this.value = value;
		this.mappingCode = mappingCode;
		this.logMsg = logMsg;
	}
	
	public String value() {
		return this.value;
	}
	
	public String mappingCode() {
		return this.mappingCode;
	}
	
	public String logMsg() {
		return this.logMsg;
	}

}
