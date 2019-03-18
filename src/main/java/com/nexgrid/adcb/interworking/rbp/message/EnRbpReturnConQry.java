package com.nexgrid.adcb.interworking.rbp.message;

public enum EnRbpReturnConQry {
	
	CON_STS(1);
	
	private int tagLength = 0;
	
	EnRbpReturnConQry(int tagLength) {
		this.tagLength = tagLength;
	}

	public int getTagLength() {
		return tagLength;
	}
	

}
