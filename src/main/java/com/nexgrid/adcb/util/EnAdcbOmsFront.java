package com.nexgrid.adcb.util;

public enum EnAdcbOmsFront {
	
	SUCCESS("20000") 			// 성공
	, SERVICE_URL("30100") 			// 서비스: URL관련
	, DB("40000")
	, NCAS_API("511000")		// [NCAS 연동]: NCAS가 주는 응답코드 일 경우 (NCAS 응답코드는 2자리.- 511000XX)
	, NCAS_ADCB("51000")		// [NCAS 연동]: ADCB에서 정의한 코드일 경우
	, RBP_API("5210")			// [RBP 연동]: RBP가 주는 응답코드 일 경우 (RBP 응답코드는 4자리. - 5210XXXX)
	, RBP_ADCB("52000");		// [RBP 연동]: ADCB에서 정의한 코드일 경우
	
	private String defaultCode = "";
	
	EnAdcbOmsFront(String defaultCode){
		this.defaultCode = defaultCode;
	}

	public String getDefaultCode() {
		return defaultCode;
	}

}
