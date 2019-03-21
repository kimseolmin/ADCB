package com.nexgrid.adcb.common.vo;

public class ReadConfigVO {
	
	//ADCB
	private String adcb_config_path = "";
	
	// OMS 서버번호
	private String server_num = "";
	
	//NCAS 연동
	private String ncas_url = "";
	private String ncas_charset = "";
	private String ncas_connect_time_out = "";
	private String ncas_read_time_out = "";
	private String ncas_header_name = "";
	
	//RBP 연동
	private String rbp_primary_ip = "";
	private String rbp_primary_port = "";
	private String rbp_secondary_ip = "";
	private String rbp_secondary_port = "";
	private String rbp_connect_time_out = "";
	private String rbp_receive_time_out = "";
	private String rbp_reconnect_sleep_time = "";
	private String rbp_system_id ="";
	private String rbp_cdrdata = "";
	private String rbp_called_network = "";
	private String rbp_pid = "";
	private String rbp_dbid = "";
	private String rbp_svc_ctg = "";
	private String rbp_opcode_con_qry = "001"; // 연결 상태 확인
	private String rbp_opcode_select = "111"; // 한도 조회
	private String rbp_opcode_charge = "114"; // 한도 즉시 차감
	private String rbp_msg_gbn_invoke = "1"; // 연결 상태 확인 시 메세지
	private String rbp_msg_gbn_return = "2"; // 서버로부터 연결 상태 확인 시 응답에 대한 응답 메세지
	private String rbp_interface_version = ""; // 연동 버전 정의
	
	private String time_out = "";
	

	
	
	
	public String getAdcb_config_path() {
		return adcb_config_path;
	}
	public void setAdcb_config_path(String adcb_config_path) {
		this.adcb_config_path = adcb_config_path;
	}
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
	
	
	
	
	public String getRbp_primary_ip() {
		return rbp_primary_ip;
	}
	public void setRbp_primary_ip(String rbp_primary_ip) {
		this.rbp_primary_ip = rbp_primary_ip;
	}
	public String getRbp_primary_port() {
		return rbp_primary_port;
	}
	public void setRbp_primary_port(String rbp_primary_port) {
		this.rbp_primary_port = rbp_primary_port;
	}
	public String getRbp_secondary_ip() {
		return rbp_secondary_ip;
	}
	public void setRbp_secondary_ip(String rbp_secondary_ip) {
		this.rbp_secondary_ip = rbp_secondary_ip;
	}
	public String getRbp_secondary_port() {
		return rbp_secondary_port;
	}
	public void setRbp_secondary_port(String rbp_secondary_port) {
		this.rbp_secondary_port = rbp_secondary_port;
	}
	public String getRbp_connect_time_out() {
		return rbp_connect_time_out;
	}
	public void setRbp_connect_time_out(String rbp_connect_time_out) {
		this.rbp_connect_time_out = rbp_connect_time_out;
	}
	public String getRbp_receive_time_out() {
		return rbp_receive_time_out;
	}
	public void setRbp_receive_time_out(String rbp_receive_time_out) {
		this.rbp_receive_time_out = rbp_receive_time_out;
	}
	public String getRbp_reconnect_sleep_time() {
		return rbp_reconnect_sleep_time;
	}
	public void setRbp_reconnect_sleep_time(String rbp_reconnect_sleep_time) {
		this.rbp_reconnect_sleep_time = rbp_reconnect_sleep_time;
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
	public String getRbp_svc_ctg() {
		return rbp_svc_ctg;
	}
	public void setRbp_svc_ctg(String rbp_svc_ctg) {
		this.rbp_svc_ctg = rbp_svc_ctg;
	}
	public String getRbp_opcode_con_qry() {
		return rbp_opcode_con_qry;
	}
	public void setRbp_opcode_con_qry(String rbp_opcode_con_qry) {
		this.rbp_opcode_con_qry = rbp_opcode_con_qry;
	}
	public String getRbp_opcode_select() {
		return rbp_opcode_select;
	}
	public void setRbp_opcode_select(String rbp_opcode_select) {
		this.rbp_opcode_select = rbp_opcode_select;
	}
	public String getRbp_opcode_charge() {
		return rbp_opcode_charge;
	}
	public void setRbp_opcode_charge(String rbp_opcode_charge) {
		this.rbp_opcode_charge = rbp_opcode_charge;
	}
	public String getRbp_msg_gbn_invoke() {
		return rbp_msg_gbn_invoke;
	}
	public void setRbp_msg_gbn_invoke(String rbp_msg_gbn_invoke) {
		this.rbp_msg_gbn_invoke = rbp_msg_gbn_invoke;
	}
	public String getRbp_msg_gbn_return() {
		return rbp_msg_gbn_return;
	}
	public void setRbp_msg_gbn_return(String rbp_msg_gbn_return) {
		this.rbp_msg_gbn_return = rbp_msg_gbn_return;
	}
	public String getRbp_interface_version() {
		return rbp_interface_version;
	}
	public void setRbp_interface_version(String rbp_interface_version) {
		this.rbp_interface_version = rbp_interface_version;
	}
	public String getTime_out() {
		return time_out;
	}
	public void setTime_out(String time_out) {
		this.time_out = time_out;
	}
	
	

}