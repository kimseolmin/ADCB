package com.nexgrid.adcb.util;

public enum EnAdcbOmsCode {
	
	SUCCESS("20000000")
	, INVALID_URL_KEY("30100001", "2", "요청 URL 전문 형식 불일치")
	, INVALID_URL_VALUE("30100002", "2", "요청 URL 필수 파라미터 값 오류")
	, INVALID_HEADER_KEY("30200001", "2", "요청 HEADER 전문 형식 불일치")
	, INVALID_HEADER_VALUE("30200002", "2", "요청 HEADER 필수 파라미터 값 오류")
	, INVALID_BODY_KEY("30300001", "2", "요청 BODY 전문 형식 불일치")
	, INVALID_BODY_VALUE("30300002", "2", "요청 BODY 필수 파라미터 값 오류")
	, INVALID_ERROR("39999999", "4")
	
	
	, DB_ERROR("40000000", "4")
	, DB_CONNECT_ERROR("40000001", "4")
	, DB_INVALID_ERROR("49999999", "4")
	
	
	, NCAS_70("51100070", "105") // 고객정보가 없거나 번호 이동된  사용자 차단 - 해지된 사용자는 고객정보 없음으로 나옴
	, NCAS_71("51100071", "104") // 71 : SKT로 번호이동
	, NCAS_76("51100076", "104") // 76 : KTF로 번호이동
	, NCAS_API("511000", "4")		// [NCAS 연동]: NCAS가 주는 응답코드 일 경우 (NCAS 응답코드는 2자리.- 511000XX)
	, NCAS_BLOCK_CTN("51000001", "121", "Block CTN")
	, NCAS_BLOCK_FEETYPE("51000002", "121", "Block FeeType")
	, NCAS_BLOCK_CORP("51000003", "117", "법인폰 차단")
	, NCAS_BLOCK_PAUSE("51000003", "104", "일시중지폰 차단")
	, NCAS_BLOCK_LOSS("51000004", "104", "분실등록폰 차단")
	, NCAS_BLOCK_PREPAY("51000005", "118", "선불가입자 차단")
	, NCAS_BLOCK_IRREG("51000006", "118", "부정사용자 차단")
	, NCAS_BLOCK_14("51000007", "117", "만 14세 미만 차단")
	, NCAS_BLOCK_GRADE("51000008", "118", "7등급 차단")
	, NCAS_READ_TIMEOUT("51777777", "4", "NCAS Read Timeout")
	, NCAS_CONNECT_TIMEOUT("51888888", "4", "NCAS Connect Timeout")
	, NCAS_INVALID_ERROR("51999999", "4")
	
	
	, RBP_RES_BODY_KEY("52000001", "4", "RBP Response Body 형식 오류")
	, RBP_RES_TIMEOUT("52888888", "4", "RBP Response Timeout")
	, RBP_INVALID_ERROR("52999999", "4")
	, RBP_API("5210");			// [RBP 연동]: RBP가 주는 응답코드 일 경우 (RBP 응답코드는 4자리. - 5210XXXX)
	
	
	
	
	private String value = "";
	private String mappingCode = "";
	private String logMsg = "";
	
	
	EnAdcbOmsCode(String value){
		this.value =  value;
	}
	
	EnAdcbOmsCode(String value, String mappingCode){
		this.value = value;
		this.mappingCode = mappingCode;
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
