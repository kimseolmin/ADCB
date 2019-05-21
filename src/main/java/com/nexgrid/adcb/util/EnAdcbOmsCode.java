package com.nexgrid.adcb.util;

import org.springframework.http.HttpStatus;

/**
 * OMS의 ResultCode 정의 
 */
public enum EnAdcbOmsCode {
	
	SUCCESS("20000000", "0", HttpStatus.OK)
	, INVALID_URL_KEY("30100001", "2", "요청 URL 전문 형식 불일치", HttpStatus.OK)
	, INVALID_URL_VALUE("30100002", "2", "요청 URL 필수 파라미터 값 오류", HttpStatus.OK)
	, INVALID_HEADER_KEY("30200001", "2", "요청 HEADER 전문 형식 불일치", HttpStatus.OK)
	, INVALID_HEADER_VALUE("30200002", "2", "요청 HEADER 필수 파라미터 값 오류", HttpStatus.OK)
	, INVALID_BODY_KEY("30300001", "2", "요청 BODY 전문 형식 불일치", HttpStatus.OK)
	, INVALID_BODY_VALUE("30300002", "2", "요청 BODY 필수 파라미터 값 오류", HttpStatus.OK)
	, CHARGE_DUPLICATE_REQ("30000001", "123", "청구 API 중복 요청", HttpStatus.OK)
	, TRANSACTION_NOT_FOUND("30000002", "107", "거래내역을 찾을 수 없음", HttpStatus.OK)
	, REFUND_DUPLICATE_REQ("30000003", "123", "환불 API 중복 요청", HttpStatus.OK)
	, UNKNOWN_STATUS("30000004", "7", "PaymentStatus: unknown status", HttpStatus.OK)
	, REFUND_YOUNG("30000005", "400", "청소년 요금제는 환불 불가능", HttpStatus.OK)
	, REFUND_WINDOW_EXPIRED("30000006", "110", "환불 유효기간 만료", HttpStatus.OK)
	, EXCEED_ORIGINAL_AMOUNT("30000007", "113", "환불액이 원래 거래금액 또는 부분환급 후 남은 금액보다 많음", HttpStatus.OK)
	, ALREADY_REFUNDED("30000008", "112", "이 거래는 이미 완전히 환불됨", HttpStatus.OK)
	, TRANSACTION_FAIL("30000009", "401", "실패된 거래로 환불 요청이 들어옴", HttpStatus.OK)
	, TRANSACTION_RESPONSE_FAIL("30000010", "402", "응답을 주지 못한 거래로 환불 또는 취소 요청이 들어옴", HttpStatus.OK)
	, REVERSE_WINDOW_EXPIRED("30000011", "111", "취소 유효기간 만료", HttpStatus.OK)
	, ALREADY_REVERSED("30000012", "106", "이미 취소된 요청", HttpStatus.OK)
	, INVALID_ERROR("39999999", "4", HttpStatus.OK)
	
	
	, DB_ERROR("40000000", "4", HttpStatus.OK)
	, DB_CONNECT_ERROR("40000001", "4", HttpStatus.OK)
	, DB_BLOCK_CTN("41000001", "121", "Block CTN", HttpStatus.OK)
	, DB_INVALID_ERROR("49999999", "4", HttpStatus.OK)
	, DB_BLOCK_FEETYPE("41000002", "121", "Block FeeType", HttpStatus.OK)
	, MAINTENANCE("41000003", "300", "작업공지", HttpStatus.OK)
	
	, NCAS_70("51100070", "105", HttpStatus.OK) // 고객정보가 없거나 번호 이동된  사용자 차단 - 해지된 사용자는 고객정보 없음으로 나옴
	, NCAS_71("51100071", "104", HttpStatus.OK) // 71 : SKT로 번호이동
	, NCAS_76("51100076", "104", HttpStatus.OK) // 76 : KTF로 번호이동
	, NCAS_API("511000", "4", HttpStatus.OK)		// [NCAS 연동]: NCAS가 주는 응답코드 일 경우 (NCAS 응답코드는 2자리.- 511000XX)
	
	
	, NCAS_BLOCK_CORP("51000003", "117", "법인폰 차단", HttpStatus.OK)
	, NCAS_BLOCK_PAUSE("51000003", "104", "일시중지폰 차단", HttpStatus.OK)
	, NCAS_BLOCK_LOSS("51000004", "104", "분실등록폰 차단", HttpStatus.OK)
	, NCAS_BLOCK_PREPAY("51000005", "118", "선불가입자 차단", HttpStatus.OK)
	, NCAS_BLOCK_IRREG("51000006", "118", "직권사용자 차단", HttpStatus.OK)
	, NCAS_BLOCK_14("51000007", "117", "만 14세 미만 차단", HttpStatus.OK)
	, NCAS_BLOCK_DUAL("51000008", "118", "듀얼넘버 차단", HttpStatus.OK)
	, NCAS_BLOCK_MVNO("51000009", "118", "mvno 차단", HttpStatus.OK)
	, NCAS_READ_TIMEOUT("51777777", "4", "NCAS Read Timeout", HttpStatus.OK)
	, NCAS_CONNECT_TIMEOUT("51888888", "4", "NCAS Connect Timeout", HttpStatus.OK)
	, NCAS_INVALID_ERROR("51999999", "4", HttpStatus.OK)
	
	
	, RBP_RES_BODY_KEY("52000001", "4", "RBP Response Body 형식 오류", HttpStatus.OK)
	, RBP_BLOCK_GRADE("52000002", "118", "7등급 차단", HttpStatus.OK)
	, RBP_RES_TIMEOUT("52888888", "4", "RBP Response Timeout", HttpStatus.OK)
	, RBP_INVALID_ERROR("52999999", "4", HttpStatus.OK)
	, RBP_API("5210", HttpStatus.OK)			// [RBP 연동]: RBP가 주는 응답코드 일 경우 (RBP 응답코드는 4자리. - 5210XXXX)
	
	, RCSG_RES_BODY_KEY("53000001", "4", "RCSG Response Body 형식 오류", HttpStatus.OK)
	, RCSG_RES_TIMEOUT("53888888", "4", "RCSG Response Timeout", HttpStatus.OK)
	, RCSG_INVALID_ERROR("53999999", "4", HttpStatus.OK)
	, RCSG_API("5310", HttpStatus.OK)			// [RCSG 연동]: RCSG가 주는 응답코드 일 경우 (RCSG 응답코드는 4자리. - 5310XXXX)
	
	
	
	, ESB_HEADER("54000001", "4", "ESB Header 오류", HttpStatus.OK)
	, ESB_TIMEOUT("54888888", "4", "ESB Timeout", HttpStatus.OK)
	, ESB_API("5410", "4", HttpStatus.OK)
	, ESB_4004("54104004", "105", "존재하지 않는 고객", HttpStatus.OK)
	, ESB_INVALID_ERROR("54999999", "4", HttpStatus.OK);
	
	
	
	
	private String value = "";
	private String mappingCode = "";
	private String logMsg = "";
	private HttpStatus status = null;
	
	
	EnAdcbOmsCode(String value, HttpStatus status){
		
		this.value =  value;
		this.status = status;
		this.status = status;
	}
	
	EnAdcbOmsCode(String value, String mappingCode, HttpStatus status){
		this.value = value;
		this.mappingCode = mappingCode;
		this.status = status;
	}
	
	EnAdcbOmsCode(String value, String mappingCode, String logMsg, HttpStatus status){
		this.value = value;
		this.mappingCode = mappingCode;
		this.logMsg = logMsg;
		this.status = status;
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
	
	public HttpStatus status() {
		return this.status;
	}
	
	

}
