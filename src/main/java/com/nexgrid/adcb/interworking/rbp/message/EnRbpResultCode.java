package com.nexgrid.adcb.interworking.rbp.message;

import org.springframework.http.HttpStatus;

import com.nexgrid.adcb.util.Init;

public enum EnRbpResultCode {

	RS_4000("4000", "4", "SOC-CODE 미 등록", HttpStatus.INTERNAL_SERVER_ERROR)
	, RS_4100("4100", "4", "SYSTEM ERROR", HttpStatus.INTERNAL_SERVER_ERROR)
	, RS_4002("4002", "4", "통합한도 ERROR", HttpStatus.INTERNAL_SERVER_ERROR)
	, RS_4003("4003", "104", "한도고객 아님", HttpStatus.BAD_REQUEST)
	, RS_4004("4004", "105", "존재하지 않는 고객", HttpStatus.BAD_REQUEST)
	, RS_4008("4008", "108", "잔여한도 부족", HttpStatus.BAD_REQUEST)
	, RS_4009("4009", "123", "중복된 Request", HttpStatus.BAD_REQUEST)
	, RS_4010("4010", "4", "해당 BR_ID 정보 없음.", HttpStatus.INTERNAL_SERVER_ERROR)
	, RS_4011_CHARGE("4011", "106", "완료된 건에 대한 요청", Init.readConfig.getRbp_opcode_charge(), HttpStatus.BAD_REQUEST)
	//, RS_4011_CHARGE("4011", "106", "완료된 건에 대한 요청", Init.readConfig.getRbp_opcode_select())
	, RS_4011_REFUND("4011", "112", "완료된 건에 대한 요청", Init.readConfig.getRbp_opcode_cancel(), HttpStatus.BAD_REQUEST)
	, RS_4015("4015", "2", "금액오류(-금액 전송)", HttpStatus.BAD_REQUEST)
	, RS_4018("4018", "4", "중복예약", HttpStatus.INTERNAL_SERVER_ERROR)
	, RS_4019("4019", "4", "예약항목과 다른 차감 Data", HttpStatus.INTERNAL_SERVER_ERROR)
	, RS_4040("4040", "4", "수신 Data 항목 오류", HttpStatus.INTERNAL_SERVER_ERROR)
	, RS_4042("4042", "4", "CMS 타입에서 DBID의 요율이 -1일 때 PRICE 필드값 미존재", HttpStatus.INTERNAL_SERVER_ERROR)
	, RS_4043("4043", "4", "Multi건 결제 취소시 에러(단건에 대한 결제 취소만 가능)", HttpStatus.INTERNAL_SERVER_ERROR)
	, RS_4044("4044", "110", "이전 월 내역에 대한 결제 취소", HttpStatus.BAD_REQUEST)
	, RS_5004("5004", "4", "START-USE-TIME 없음", HttpStatus.INTERNAL_SERVER_ERROR)
	, RS_1100("1100", "4", "회선상태에 따른 서비스 임시 차단", HttpStatus.INTERNAL_SERVER_ERROR)
	, RS_INVALID("4", "정의되지 않은 RESULT", HttpStatus.INTERNAL_SERVER_ERROR);
	
	
	private String defaultValue = ""; // RBP RESULT
	private String mappingCode = ""; // boku에게 리턴되는 reasonCode
	private String resMsg = "";
	private String opCode = "";
	private HttpStatus status = null;
	
	
	EnRbpResultCode(String mappingCode, String resMsg, HttpStatus status){
		this.mappingCode = mappingCode;
		this.resMsg = resMsg;
		this.status = status;
	}
	
	EnRbpResultCode(String defaultValue, String mappingCode, String resMsg, HttpStatus status){
		this.defaultValue = defaultValue;
		this.mappingCode = mappingCode;
		this.resMsg = resMsg;
		this.status = status;
	}
	
	EnRbpResultCode(String defaultValue, String mappingCode, String resMsg, String opCode, HttpStatus status){
		this.defaultValue = defaultValue;
		this.mappingCode = mappingCode;
		this.resMsg = resMsg;
		this.opCode = opCode;
		this.status = status;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public String getMappingCode() {
		return mappingCode;
	}

	public String getResMsg() {
		return resMsg;
	}

	public String getOpCode() {
		return opCode;
	}
	
	public HttpStatus getStatus() {
		return this.status;
	}
	
	
}
