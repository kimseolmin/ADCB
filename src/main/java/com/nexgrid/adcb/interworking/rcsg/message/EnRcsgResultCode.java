package com.nexgrid.adcb.interworking.rcsg.message;

import org.springframework.http.HttpStatus;

import com.nexgrid.adcb.util.Init;

public enum EnRcsgResultCode {

	RS_4000("4000", "4", "SOC-CODE 미 등록", HttpStatus.OK)
	, RS_4100("4100", "4", "라우팅 정보 없음", HttpStatus.OK)
	, RS_4101("4101", "4", "RCSR TIME OUT", HttpStatus.OK)
	, RS_4102("4102", "4", "RCSR NOT IN SERVICE", HttpStatus.OK)
	, RS_4103("4103", "4", "SYSTEM ERROR", HttpStatus.OK)
	, RS_4002("4002", "4", "통합한도 ERROR", HttpStatus.OK)
	, RS_4003("4003", "104", "한도고객 아님", HttpStatus.OK)
	, RS_4004("4004", "105", "존재하지 않는 고객", HttpStatus.OK)
	, RS_4005("4005", "118", "정보료 상한 부가 서비스 미가입", HttpStatus.OK)
	, RS_4006("4006", "4", "PID 없음", HttpStatus.OK)
	, RS_4008("4008", "108", "잔여한도 부족", HttpStatus.OK)
	, RS_4009("4009", "123", "중복된 Request", HttpStatus.OK)
	, RS_4010("4010", "4", "해당 BR_ID 정보 없음.", HttpStatus.OK)
	, RS_4011_CHARGE("4011", "106", "완료된 건에 대한 요청", Init.readConfig.getRcsg_opcode_charge(), HttpStatus.OK)
	//, RS_4011_CHARGE("4011", "106", "완료된 건에 대한 요청", Init.readConfig.getRcsg_opcode_select())
	, RS_4011_REFUND("4011", "112", "완료된 건에 대한 요청", Init.readConfig.getRcsg_opcode_cancel(), HttpStatus.OK)
	, RS_4015("4015", "2", "금액오류(-금액 전송)", HttpStatus.OK)
	, RS_4016("4016", "4", "이관되지 않은 한도 요금제", HttpStatus.OK)
	, RS_4017("4017", "4", "정의되지 않은 (CSBS 서비스코드 매핑 오류)", HttpStatus.OK)
	, RS_4018("4018", "4", "중복예약", HttpStatus.OK)
	, RS_4019("4019", "4", "예약항목과 다른 차감 Data", HttpStatus.OK)
	, RS_4020("4020", "4", "동보 전송 인 경우에서 부분예약취소/부분예약 차감을 일반예약차감/취소로 보낼 때 발생", HttpStatus.OK)
	, RS_4021("4021", "4", "연동규격에 맞지 않는 DATA", HttpStatus.OK)
	, RS_4040("4040", "4", "수신 Data 항목 오류", HttpStatus.OK)
	, RS_4042("4042", "4", "CMS 타입에서 DBID의 요율이 -1일 때 PRICE 필드값 미존재", HttpStatus.OK)
	, RS_4041("4041", "4", "STATUS 값 오류", HttpStatus.OK)
	, RS_5004("5004", "4", "START-USE-TIME 없음", HttpStatus.OK)
	, RS_1001("1001", "4", "등록되지 않은 MDN", HttpStatus.OK)
	, RS_1002("1002", "4", "홀 소진", HttpStatus.OK)
	, RS_1003("1003", "4", "미등록 서비스", HttpStatus.OK)
	, RS_1004("1004", "4", "정의되지 않은 Command", HttpStatus.OK)
	, RS_1005("1005", "4", "내부처리 중 Time-Out 발생으로 10초로 설정함", HttpStatus.OK)
	, RS_1006("1006", "4", "DB 처리 외의 내부 로직 상 발생한 에러", HttpStatus.OK)
	, RS_1007("1007", "4", "DB 처리 중 발생한 에러", HttpStatus.OK)
	, RS_1008("1008", "4", "Session이 없는 경우", HttpStatus.OK)
	, RS_1009("1009", "4", "SCP connection fail", HttpStatus.OK)
	, RS_1010("1010", "108", "잔액 부족", HttpStatus.OK)
	, RS_2000("2000", "4", "기타 에러", HttpStatus.OK)
	, RS_INVALID("4", "정의되지 않은 RESULT", HttpStatus.OK);
	
	
	private String defaultValue = ""; // RCSG RESULT
	private String mappingCode = ""; // boku에게 리턴되는 reasonCode
	private String resMsg = "";
	private String opCode = "";
	private HttpStatus status = null;
	
	
	
	EnRcsgResultCode(String mappingCode, String resMsg, HttpStatus status){
		this.mappingCode = mappingCode;
		this.resMsg = resMsg;
		this.status = status;
	}
	
	EnRcsgResultCode(String defaultValue, String mappingCode, String resMsg, HttpStatus status){
		this.defaultValue = defaultValue;
		this.mappingCode = mappingCode;
		this.resMsg = resMsg;
		this.status = status;
	}
	
	EnRcsgResultCode(String defaultValue, String mappingCode, String resMsg, String opCode, HttpStatus status){
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
