package com.nexgrid.adcb.common.vo;

import java.util.Random;

import com.nexgrid.adcb.util.StringUtil;


public class LogVO {

	
	/***********************필수로그 Start************************/
	private String seqId = "";						// YYYYMMDDHHmmSSsss(17자리)+RANDOM(4자리)+SVC Name(4자리)
	private String logTime = "";					// 로그를 파일에 Write 시점 시간 (14)
	private String logType = "";					// SVC : 서비스   (3)
	private String sid = "";						// 동합 ID (CTN 또는 전사 서비스 공동 ID)  (32)
	private String resultCode = "";					// 서비스 상태 코드  (8)
	private String reqTime = "";					// 서비스 전체 요청 완료 시간 (17)
	private String resTime = "";					// 서비스 전체 응답 완료 시간 (17)
	private String clientIp = "";					// 접속 클라이언트 IP (40)
	private String devInfo = "";					// 접속 단말 타입 (5)
	private String osInfo = "";						// os 정보 (50)
	private String nwInfo = "";						// 네트워크 정보 (5)
	private String svcName = "";					// 서비스 명 (32)
	private String devModel = "";					// 단말 모델명 (50)
	private String carrierType = "";				// 통신사 구분 (1)
	/***********************필수로그 End************************/
	
	
	private String apiType = "";					// ADCB 서비스 코드
	private String connectionFlow = "";				// 접속 API 및 DB 호출 순서(-로 구분)
	private String apiResultCode = "";				// ADCB API 결과 코드
	private String rsCode = "";						// DB 에러 코드
	
	private String ncasReqTime = "";				// NCAS 접속 요청 발생 시간			
	private String ncasResTime = "";				// NCAS 접속 응답 발생 시간
	private String ncasResultCode = "";				// NCAS 결과 코드
	
	
	private String rbpReqTime = "";					// RBP 접속 요청 발생 시간
	private String rbpResTime = "";					// RBP 접속 요청 발생 시간
	private String rbpResultCode = "";				// RBP 결과 코드
	
	
	
	/*****************service log********************/
	private String flow;							//Error Occurrence Flow
	private String conApi;							// Connection API
	

	
	
	
	public LogVO(String apiType) {
		setSeqId();
		setReqTime();
		
		setApiType(apiType);
	}
	
	
	public String getSeqId() {
		return seqId;
	}
	


	public void setSeqId() {
		if (this.seqId.equalsIgnoreCase("") ) {
			String dTime = StringUtil.getCurrentTimeMilli();
			Random random = new Random();
			
			
			Integer a = random.nextInt(10000);
			
			String rand01 = String.format("%04d", a);
			
			this.seqId = dTime + "." + rand01 + ".ADCB";
		}
	}

	public String getLogTime() {
		return logTime;
	}

	public void setLogTime() {
		this.logTime = StringUtil.getCurrentTime();
	}

	public String getLogType() {
		return logType;
	}

	public void setLogType() {
		this.logType = "SVC";
	}

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	public String getResultCode() {
		return resultCode;
	}

	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}

	public String getReqTime() {
		return reqTime;
	}

	public void setReqTime() {
		this.reqTime = StringUtil.getCurrentTimeMilli();
	}

	public String getResTime() {
		return resTime;
	}

	public void setResTime() {
		this.resTime = StringUtil.getCurrentTimeMilli();
	}

	public String getClientIp() {
		return clientIp;
	}

	public void setClientIp(String clientIp) {
		this.clientIp = clientIp;
	}

	public String getDevInfo() {
		return devInfo;
	}

	public void setDevInfo(String devInfo) {
		this.devInfo = devInfo;
	}

	public String getOsInfo() {
		return osInfo;
	}

	public void setOsInfo(String osInfo) {
		this.osInfo = osInfo;
	}

	public String getNwInfo() {
		return nwInfo;
	}

	public void setNwInfo(String nwInfo) {
		this.nwInfo = nwInfo;
	}

	public String getSvcName() {
		return svcName;
	}


	public void setSvcName(String svcName) {
		this.svcName = svcName;
	}


	public String getDevModel() {
		return devModel;
	}

	public void setDevModel(String devModel) {
		this.devModel = devModel;
	}

	public String getCarrierType() {
		return carrierType;
	}

	public void setCarrierType(String carrierType) {
		this.carrierType = carrierType;
	}
	
	
	
	
	public String getApiType() {
		return apiType;
	}
	
	public void setApiType(String apiType) {
		this.apiType = apiType;
	}
	
	public String getConnectionFlow() {
		return connectionFlow;
	}

	public void setConnectionFlow(String connectionFlow) {
		this.connectionFlow = this.connectionFlow.equals("") ? connectionFlow : this.connectionFlow + "-" + connectionFlow;
	}
	
	public String getApiResultCode() {
		return apiResultCode;
	}


	public void setApiResultCode(String apiResultCode) {
		this.apiResultCode = apiResultCode;
	}
	
	public String getRsCode() {
		return rsCode;
	}

	public void setRsCode(String rsCode) {
		this.rsCode = rsCode;
	}

	public String getNcasReqTime() {
		return ncasReqTime;
	}


	public void setNcasReqTime() {
		this.ncasReqTime = StringUtil.getCurrentTimeMilli();
	}


	public String getNcasResTime() {
		return ncasResTime;
	}


	public void setNcasResTime() {
		this.ncasResTime = StringUtil.getCurrentTimeMilli();
	}


	public String getNcasResultCode() {
		return ncasResultCode;
	}


	public void setNcasResultCode(String ncasResultCode) {
		this.ncasResultCode = ncasResultCode;
	}
	
	public String getRbpReqTime() {
		return rbpReqTime;
	}


	public void setRbpReqTime() {
		this.rbpReqTime = StringUtil.getCurrentTimeMilli();
	}


	public String getRbpResTime() {
		return rbpResTime;
	}


	public void setRbpResTime() {
		this.rbpResTime = StringUtil.getCurrentTimeMilli();
	}


	public String getRbpResultCode() {
		return rbpResultCode;
	}


	public void setRbpResultCode(String rbpResultCode) {
		this.rbpResultCode = rbpResultCode;
	}


	public String getFlow() {
		return flow;
	}


	public void setFlow(String flow) {
		this.flow = flow;
	}


	public String getConApi() {
		return conApi;
	}


	public void setConApi(String conApi) {
		this.conApi = conApi;
	}
	


	
	
	
	


}
