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
	private String rbp_opcode_cancel= "116"; // 결제 취소
	private String rbp_msg_gbn_invoke = "1"; // 연결 상태 확인 시 메세지
	private String rbp_msg_gbn_return = "2"; // 서버로부터 연결 상태 확인 시 응답에 대한 응답 메세지
	private String rbp_interface_version = ""; // 연동 버전 정의
	
	
	//RCSG 연동
	private String rcsg_primary_ip = "";
	private String rcsg_primary_port = "";
	private String rcsg_secondary_ip = "";
	private String rcsg_secondary_port = "";
	private String rcsg_connect_time_out = "";
	private String rcsg_receive_time_out = "";
	private String rcsg_reconnect_sleep_time = "";
	private String rcsg_system_id ="";
	private String rcsg_cdrdata = "";
	private String rcsg_called_network = "";
	private String rcsg_pid = "";
	private String rcsg_dbid = "";
	private String rcsg_svc_ctg = "";
	private String rcsg_opcode_con_qry = "001"; // 연결 상태 확인
	private String rcsg_opcode_select = "111"; // 한도 조회
	private String rcsg_opcode_charge = "114"; // 한도 즉시 차감
	private String rcsg_opcode_cancel= "116"; // 결제 취소
	private String rcsg_msg_gbn_invoke = "1"; // 연결 상태 확인 시 메세지
	private String rcsg_msg_gbn_return = "2"; // 서버로부터 연결 상태 확인 시 응답에 대한 응답 메세지
	private String rcsg_interface_version = ""; // 연동 버전 정의
	
	
	// ESB 연동
	private String esb_mps208_url = "";
	private String esb_cm181_url = "";
	private String esb_time_out = "";
	
	
	// SMS format
	private String charge_section_list = "";
	private String limit_excess = "";
	private String charge_complete = "";
	private String section_excess = "";
	
	
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
	public String getRbp_opcode_cancel() {
		return rbp_opcode_cancel;
	}
	public void setRbp_opcode_cancel(String rbp_opcode_cancel) {
		this.rbp_opcode_cancel = rbp_opcode_cancel;
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
	
	
	
	
	
	public String getRcsg_primary_ip() {
		return rcsg_primary_ip;
	}
	public void setRcsg_primary_ip(String rcsg_primary_ip) {
		this.rcsg_primary_ip = rcsg_primary_ip;
	}
	public String getRcsg_primary_port() {
		return rcsg_primary_port;
	}
	public void setRcsg_primary_port(String rcsg_primary_port) {
		this.rcsg_primary_port = rcsg_primary_port;
	}
	public String getRcsg_secondary_ip() {
		return rcsg_secondary_ip;
	}
	public void setRcsg_secondary_ip(String rcsg_secondary_ip) {
		this.rcsg_secondary_ip = rcsg_secondary_ip;
	}
	public String getRcsg_secondary_port() {
		return rcsg_secondary_port;
	}
	public void setRcsg_secondary_port(String rcsg_secondary_port) {
		this.rcsg_secondary_port = rcsg_secondary_port;
	}
	public String getRcsg_connect_time_out() {
		return rcsg_connect_time_out;
	}
	public void setRcsg_connect_time_out(String rcsg_connect_time_out) {
		this.rcsg_connect_time_out = rcsg_connect_time_out;
	}
	public String getRcsg_receive_time_out() {
		return rcsg_receive_time_out;
	}
	public void setRcsg_receive_time_out(String rcsg_receive_time_out) {
		this.rcsg_receive_time_out = rcsg_receive_time_out;
	}
	public String getRcsg_reconnect_sleep_time() {
		return rcsg_reconnect_sleep_time;
	}
	public void setRcsg_reconnect_sleep_time(String rcsg_reconnect_sleep_time) {
		this.rcsg_reconnect_sleep_time = rcsg_reconnect_sleep_time;
	}
	public String getRcsg_system_id() {
		return rcsg_system_id;
	}
	public void setRcsg_system_id(String rcsg_system_id) {
		this.rcsg_system_id = rcsg_system_id;
	}
	public String getRcsg_cdrdata() {
		return rcsg_cdrdata;
	}
	public void setRcsg_cdrdata(String rcsg_cdrdata) {
		this.rcsg_cdrdata = rcsg_cdrdata;
	}
	public String getRcsg_called_network() {
		return rcsg_called_network;
	}
	public void setRcsg_called_network(String rcsg_called_network) {
		this.rcsg_called_network = rcsg_called_network;
	}
	public String getRcsg_pid() {
		return rcsg_pid;
	}
	public void setRcsg_pid(String rcsg_pid) {
		this.rcsg_pid = rcsg_pid;
	}
	public String getRcsg_dbid() {
		return rcsg_dbid;
	}
	public void setRcsg_dbid(String rcsg_dbid) {
		this.rcsg_dbid = rcsg_dbid;
	}
	public String getRcsg_svc_ctg() {
		return rcsg_svc_ctg;
	}
	public void setRcsg_svc_ctg(String rcsg_svc_ctg) {
		this.rcsg_svc_ctg = rcsg_svc_ctg;
	}
	public String getRcsg_opcode_con_qry() {
		return rcsg_opcode_con_qry;
	}
	public void setRcsg_opcode_con_qry(String rcsg_opcode_con_qry) {
		this.rcsg_opcode_con_qry = rcsg_opcode_con_qry;
	}
	public String getRcsg_opcode_select() {
		return rcsg_opcode_select;
	}
	public void setRcsg_opcode_select(String rcsg_opcode_select) {
		this.rcsg_opcode_select = rcsg_opcode_select;
	}
	public String getRcsg_opcode_charge() {
		return rcsg_opcode_charge;
	}
	public void setRcsg_opcode_charge(String rcsg_opcode_charge) {
		this.rcsg_opcode_charge = rcsg_opcode_charge;
	}
	public String getRcsg_opcode_cancel() {
		return rcsg_opcode_cancel;
	}
	public void setRcsg_opcode_cancel(String rcsg_opcode_cancel) {
		this.rcsg_opcode_cancel = rcsg_opcode_cancel;
	}
	public String getRcsg_msg_gbn_invoke() {
		return rcsg_msg_gbn_invoke;
	}
	public void setRcsg_msg_gbn_invoke(String rcsg_msg_gbn_invoke) {
		this.rcsg_msg_gbn_invoke = rcsg_msg_gbn_invoke;
	}
	public String getRcsg_msg_gbn_return() {
		return rcsg_msg_gbn_return;
	}
	public void setRcsg_msg_gbn_return(String rcsg_msg_gbn_return) {
		this.rcsg_msg_gbn_return = rcsg_msg_gbn_return;
	}
	public String getRcsg_interface_version() {
		return rcsg_interface_version;
	}
	public void setRcsg_interface_version(String rcsg_interface_version) {
		this.rcsg_interface_version = rcsg_interface_version;
	}
	
	
	
	public String getEsb_mps208_url() {
		return esb_mps208_url;
	}
	public void setEsb_mps208_url(String esb_mps208_url) {
		this.esb_mps208_url = esb_mps208_url;
	}
	public String getEsb_cm181_url() {
		return esb_cm181_url;
	}
	public void setEsb_cm181_url(String esb_cm181_url) {
		this.esb_cm181_url = esb_cm181_url;
	}
	public String getEsb_time_out() {
		return esb_time_out;
	}
	public void setEsb_time_out(String esb_time_out) {
		this.esb_time_out = esb_time_out;
	}
	
	
	
	
	public String getCharge_section_list() {
		return charge_section_list;
	}
	public void setCharge_section_list(String charge_section_list) {
		this.charge_section_list = charge_section_list;
	}
	public String getLimit_excess() {
		return limit_excess;
	}
	public void setLimit_excess(String limit_excess) {
		this.limit_excess = limit_excess;
	}
	public String getCharge_complete() {
		return charge_complete;
	}
	public void setCharge_complete(String charge_complete) {
		this.charge_complete = charge_complete;
	}
	public String getSection_excess() {
		return section_excess;
	}
	public void setSection_excess(String section_excess) {
		this.section_excess = section_excess;
	}
	public String getTime_out() {
		return time_out;
	}
	public void setTime_out(String time_out) {
		this.time_out = time_out;
	}
	
	

}