package com.nexgrid.adcb.common.vo;

import java.util.Date;

import com.nexgrid.adcb.util.StringUtil;

public class SmsSendVO {

	
	//--------------------BOKU에서 들어오는 요청값이 저장될 변수--------------
	private String messageId;
	private String message;
	private String msisdn=null;
	private String originator;
	private int validity;
	
	
	
	
	//----------------------DB에 저장될 때 사용할 변수------------------------
	private Date create_dt;					// 등록일시	 
	private String seq = "";				// SMS발송키값
	private String try_cnt = "0";			// 재전송횟수
	private String gubun = "";				// SMS발송구분
	private String fail_rsn_cd = "";		// 전송실패사유코드
	private String from_ctn = "019-114";	// 송신자휴대폰번호
	private String to_ctn = "";				// 수신자휴대폰번호
	private String request_id = "";			// BOKU의 RequestID
	private String content = "";			// 내용
	
	
	public String getMessageId() {
		return messageId;
	}
	public void setMessageId(String messageId) {
		this.messageId = messageId;
		this.request_id = messageId;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
		this.content = message;
	}
	public String getMsisdn() {
		return msisdn;
	}
	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
		if(msisdn != null) {
			this.to_ctn = StringUtil.getCtn344(msisdn);
		}
		
	}
	public String getOriginator() {
		return originator;
	}
	public void setOriginator(String originator) {
		this.originator = originator;
	}
	public int getValidity() {
		return validity;
	}
	public void setValidity(int validity) {
		this.validity = validity;
	}
	@Override
	public String toString() {
		return "SmsSendVO [messageId=" + messageId + ", message=" + message + ", msisdn=" + msisdn + ", originator="
				+ originator + ", validity=" + validity + "]";
	}
	
	
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
	public Date getCreate_dt() {
		return create_dt;
	}
	public void setCreate_dt(Date create_dt) {
		this.create_dt = create_dt;
	}
	public String getSeq() {
		return seq;
	}
	public void setSeq(String seq) {
		this.seq = seq;
	}
	public String getTry_cnt() {
		return try_cnt;
	}
	public void setTry_cnt(String try_cnt) {
		this.try_cnt = try_cnt;
	}
	public String getFail_rsn_cd() {
		return fail_rsn_cd;
	}
	public void setFail_rsn_cd(String fail_rsn_cd) {
		this.fail_rsn_cd = fail_rsn_cd;
	}
	
	
	
	
}
