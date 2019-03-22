package com.nexgrid.adcb.interworking.rbp.message;

import com.nexgrid.adcb.util.Init;

public enum EnRbpResultCode {

	RS_4000("4000", "4", "SOC-CODE 미 등록")
	, RS_4100("4100", "4", "SYSTEM ERROR")
	, RS_4002("4002", "4", "통합한도 ERROR")
	, RS_4003("4003", "104", "한도고객 아님")
	, RS_4004("4004", "105", "존재하지 않는 고객")
	, RS_4008("4008", "108", "잔여한도 부족")
	, RS_4009("4009", "123", "중복된 Request")
	, RS_4010("4010", "4", "해당 BR_ID 정보 없음.")
	, RS_4011_CHARGE("4011", "106", "완료된 건에 대한 요청", Init.readConfig.getRbp_opcode_charge())
	//, RS_4011_CHARGE("4011", "106", "완료된 건에 대한 요청", Init.readConfig.getRbp_opcode_select())
	, RS_4011_REFUND("4011", "112", "완료된 건에 대한 요청", Init.readConfig.getRbp_opcode_cancel())
	, RS_4015("4015", "2", "금액오류(-금액 전송)")
	, RS_4018("4018", "4", "중복예약")
	, RS_4019("4019", "4", "예약항목과 다른 차감 Data")
	, RS_4040("4040", "4", "수신 Data 항목 오류")
	, RS_4042("4042", "4", "CMS 타입에서 DBID의 요율이 -1일 때 PRICE 필드값 미존재")
	, RS_4043("4043", "4", "Multi건 결제 취소시 에러(단건에 대한 결제 취소만 가능)")
	, RS_4044("4044", "110", "이전 월 내역에 대한 결제 취소")
	, RS_5004("5004", "4", "START-USE-TIME 없음")
	, RS_1100("1100", "4", "회선상태에 따른 서비스 임시 차단")
	, RS_INVALID("4", "정의되지 않은 RESULT");
	
	
	private String defaultValue = ""; // RBP RESULT
	private String mappingCode = ""; // boku에게 리턴되는 reasonCode
	private String resMsg = "";
	private String opCode = "";
	
	
	EnRbpResultCode(String mappingCode, String resMsg){
		this.mappingCode = mappingCode;
		this.resMsg = resMsg;
	}
	
	EnRbpResultCode(String defaultValue, String mappingCode, String resMsg){
		this.defaultValue = defaultValue;
		this.mappingCode = mappingCode;
		this.resMsg = resMsg;
	}
	
	EnRbpResultCode(String defaultValue, String mappingCode, String resMsg, String opCode){
		this.defaultValue = defaultValue;
		this.mappingCode = mappingCode;
		this.resMsg = resMsg;
		this.opCode = opCode;
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
	
	
}
