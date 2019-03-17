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
	
	//RBP 연동
	private String rbp_system_id ="";
	private String rbp_cdrdata = "";
	private String rbp_called_network = "";
	private String rbp_pid = "";
	private String rbp_dbid = "";
			
	
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
	
	
	
	
	public String getRbp_system_id() {
		return rbp_system_id;
	}
	public void setRbp_system_id(String rbp_system_id) {
		this.rbp_system_id = rbp_system_id;
	}
	public String getRbp_cdrdata() {
		return rbp_cdrdata;
	}
	public void setRbp_cdrdata(String rbp_cdrdata) {
		this.rbp_cdrdata = rbp_cdrdata;
	}
	public String getRbp_called_network() {
		return rbp_called_network;
	}
	public void setRbp_called_network(String rbp_called_network) {
		this.rbp_called_network = rbp_called_network;
	}
	public String getRbp_pid() {
		return rbp_pid;
	}
	public void setRbp_pid(String rbp_pid) {
		this.rbp_pid = rbp_pid;
	}
	public String getRbp_dbid() {
		return rbp_dbid;
	}
	public void setRbp_dbid(String rbp_dbid) {
		this.rbp_dbid = rbp_dbid;
	}
	public String getTime_out() {
		return time_out;
	}
	public void setTime_out(String time_out) {
		this.time_out = time_out;
	}
	
	

}