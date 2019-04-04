package com.nexgrid.adcb.common.vo;

public class SmsSendVO {

	private String gubun = "";		// SMS발송구분
	private String from_ctn = "";	// 송신자휴대폰번호
	private String to_ctn = "";		// 수신자휴대폰번호
	private String request_id = "";	// BOKU의 RequestID
	private String content = "";	// 내용
	
	
	public String getGubun() {
		return gubun;
	}
	public void setGubun(String gubun) {
		this.gubun = gubun;
	}
	public String getFrom_ctn() {
		return from_ctn;
	}
	public void setFrom_ctn(String from_ctn) {
		this.from_ctn = from_ctn;
	}
	public String getTo_ctn() {
		return to_ctn;
	}
	public void setTo_ctn(String to_ctn) {
		this.to_ctn = to_ctn;
	}
	public String getRequest_id() {
		return request_id;
	}
	public void setRequest_id(String request_id) {
		this.request_id = request_id;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
	
	
	
}
