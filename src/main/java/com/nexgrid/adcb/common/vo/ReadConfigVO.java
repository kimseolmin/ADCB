package com.nexgrid.adcb.common.vo;

public class ReadConfigVO {
	
	// OMS 서버번호
	private String server_num = "";
	
	//NCAS 연동
	private String ncas_url = "";
	private String ncas_charset = "";
	private String ncas_connect_time_out = "";
	private String ncas_read_time_out = "";
	private String ncas_header_name = "";
	
	private String time_out = "";
	

	
	
	
	public String getServer_num() {
		return server_num;
	}
	public void setServer_num(String server_num) {
		this.server_num = server_num;
	}
	public String getNcas_url() {
		return ncas_url;
	}
	public void setNcas_url(String ncas_url) {
		this.ncas_url = ncas_url;
	}
	public String getNcas_charset() {
		return ncas_charset;
	}
	public void setNcas_charset(String ncas_charset) {
		this.ncas_charset = ncas_charset;
	}
	public String getNcas_connect_time_out() {
		return ncas_connect_time_out;
	}
	public void setNcas_connect_time_out(String ncas_connect_time_out) {
		this.ncas_connect_time_out = ncas_connect_time_out;
	}
	public String getNcas_read_time_out() {
		return ncas_read_time_out;
	}
	public void setNcas_read_time_out(String ncas_read_time_out) {
		this.ncas_read_time_out = ncas_read_time_out;
	}
	public String getNcas_header_name() {
		return ncas_header_name;
	}
	public void setNcas_header_name(String ncas_header_name) {
		this.ncas_header_name = ncas_header_name;
	}
	
	
	
	
	public String getTime_out() {
		return time_out;
	}
	public void setTime_out(String time_out) {
		this.time_out = time_out;
	}
	
	

}