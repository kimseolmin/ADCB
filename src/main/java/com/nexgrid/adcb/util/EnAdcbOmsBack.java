package com.nexgrid.adcb.util;

public enum EnAdcbOmsBack {

	SUCCESS("000")
	, INVALID_URL("001");
	
	private String defaultCode = "";
	
	EnAdcbOmsBack(String defaultCode){
		this.defaultCode = defaultCode;
	}

	public String getDefaultCode() {
		return defaultCode;
	}
	
	
}
