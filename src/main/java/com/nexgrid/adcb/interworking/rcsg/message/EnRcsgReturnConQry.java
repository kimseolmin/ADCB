package com.nexgrid.adcb.interworking.rcsg.message;

public enum EnRcsgReturnConQry {

	CON_STS(1);
	
	private int tagLength = 0;
	
	EnRcsgReturnConQry(int tagLength) {
		this.tagLength = tagLength;
	}

	public int getTagLength() {
		return tagLength;
	}
}
