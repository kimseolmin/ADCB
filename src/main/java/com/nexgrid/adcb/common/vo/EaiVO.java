package com.nexgrid.adcb.common.vo;

import java.util.Date;

public class EaiVO {

	private String new_request_type;				// 청구처리
	private String new_ban_unpaid_yn_code;			// 연체여부
	private String new_account_type;				// 결제 유형
	private String new_smls_stlm_dv_cd = "95";		// 서비스 코드
	private String new_smls_stlm_cmpny_cd = "0195";	// 결제대행사코드
	private String new_cust_grd_cd;					// 고객한도등급
	private String new_prss_yymm;					// 처리년월
	private Date new_request_date;					// 거래시간
	private String new_total;						// 거래금액
	private String new_ban;							// 청구선 번호
	private String new_ace_no;						// 가입 계약 번호
	private String new_subs_no;						// 가입자번호
	private String new_request_id;					// REQUEST_ID
	private String new_merchant_id;					// 가맹점ID
	private String new_product_description;			// 구매요청 제품 설명
	
	

	
	public String getNew_request_type() {
		return new_request_type;
	}
	public void setNew_request_type(String new_request_type) {
		this.new_request_type = new_request_type;
	}
	public String getNew_ban_unpaid_yn_code() {
		return new_ban_unpaid_yn_code;
	}
	public void setNew_ban_unpaid_yn_code(String new_ban_unpaid_yn_code) {
		this.new_ban_unpaid_yn_code = new_ban_unpaid_yn_code;
	}
	public String getNew_account_type() {
		return new_account_type;
	}
	public void setNew_account_type(String new_account_type) {
		this.new_account_type = new_account_type;
	}
	public String getNew_smls_stlm_dv_cd() {
		return new_smls_stlm_dv_cd;
	}
	public void setNew_smls_stlm_dv_cd(String new_smls_stlm_dv_cd) {
		this.new_smls_stlm_dv_cd = new_smls_stlm_dv_cd;
	}
	public String getNew_smls_stlm_cmpny_cd() {
		return new_smls_stlm_cmpny_cd;
	}
	public void setNew_smls_stlm_cmpny_cd(String new_smls_stlm_cmpny_cd) {
		this.new_smls_stlm_cmpny_cd = new_smls_stlm_cmpny_cd;
	}
	public String getNew_cust_grd_cd() {
		return new_cust_grd_cd;
	}
	public void setNew_cust_grd_cd(String new_cust_grd_cd) {
		this.new_cust_grd_cd = new_cust_grd_cd;
	}
	public String getNew_prss_yymm() {
		return new_prss_yymm;
	}
	public void setNew_prss_yymm(String new_prss_yymm) {
		this.new_prss_yymm = new_prss_yymm;
	}
	public Date getNew_request_date() {
		return new_request_date;
	}
	public void setNew_request_date(Date new_request_date) {
		this.new_request_date = new_request_date;
	}
	public String getNew_total() {
		return new_total;
	}
	public void setNew_total(String new_total) {
		this.new_total = new_total;
	}
	public String getNew_ban() {
		return new_ban;
	}
	public void setNew_ban(String new_ban) {
		this.new_ban = new_ban;
	}
	public String getNew_ace_no() {
		return new_ace_no;
	}
	public void setNew_ace_no(String new_ace_no) {
		this.new_ace_no = new_ace_no;
	}
	public String getNew_subs_no() {
		return new_subs_no;
	}
	public void setNew_subs_no(String new_subs_no) {
		this.new_subs_no = new_subs_no;
	}
	public String getNew_request_id() {
		return new_request_id;
	}
	public void setNew_request_id(String new_request_id) {
		this.new_request_id = new_request_id;
	}
	public String getNew_merchant_id() {
		return new_merchant_id;
	}
	public void setNew_merchant_id(String new_merchant_id) {
		this.new_merchant_id = new_merchant_id;
	}
	public String getNew_product_description() {
		return new_product_description;
	}
	public void setNew_product_description(String new_product_description) {
		this.new_product_description = new_product_description;
	}
	
	
	
	

}
