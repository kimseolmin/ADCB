package com.nexgrid.adcb.util;

import java.io.InputStream;
import java.util.Properties;
import java.util.Timer;

import org.apache.ibatis.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.nexgrid.adcb.common.vo.ReadConfigVO;


@Service
public class Init{
	
	static public ReadConfigVO readConfig = new ReadConfigVO();
	
	private Logger log = LoggerFactory.getLogger(getClass());
	
	 /**
	 * @summury 서블릿 시작시 프로퍼티 및 로그 셋팅
	 */
//	@PostConstruct
	 public void init() {
		 
		 log.info("########## ADCB Server Start ##########");
		 
		 Properties props = new Properties();
		 
		    try {
		    	InputStream isU = Resources.getResourceAsStream("/conf/properties/config_properties.xml");
		    	props.loadFromXML(isU); 
		    	
		    	//ADCB
		    	readConfig.setAdcb_config_path(props.getProperty("ADCB_CONFIG_PATH"));
		    	
		    	// OMS 서버번호
		    	readConfig.setServer_num(props.getProperty("SERVER_NUM"));
		    	
		    	
		    	//NCAS 연동
		    	readConfig.setNcas_url(props.getProperty("NCAS_URL"));
		    	readConfig.setNcas_charset(props.getProperty("NCAS_CHARSET"));
		    	readConfig.setNcas_connect_time_out(props.getProperty("NCAS_CONNECT_TIME_OUT"));
		    	readConfig.setNcas_read_time_out(props.getProperty("NCAS_READ_TIME_OUT"));
		    	readConfig.setNcas_header_name(props.getProperty("NCAS_HEADER_NAME"));
		    	
		    	
		    	//RBP 연동
		    	readConfig.setRbp_primary_ip(props.getProperty("RBP_PRIMARY_IP"));
		    	readConfig.setRbp_primary_port(props.getProperty("RBP_PRIMARY_PORT"));
		    	readConfig.setRbp_secondary_ip(props.getProperty("RBP_SECONDARY_IP"));
		    	readConfig.setRbp_secondary_port(props.getProperty("RBP_SECONDARY_PORT"));
		    	readConfig.setRbp_connect_time_out(props.getProperty("RBP_CONNECT_TIME_OUT"));
		    	readConfig.setRbp_receive_time_out(props.getProperty("RBP_RECEIVE_TIME_OUT"));
		    	readConfig.setRbp_reconnect_sleep_time(props.getProperty("RBP_RECONNECT_SLEEP_TIME"));
		    	readConfig.setRbp_system_id(props.getProperty("RBP_SYSTEM_ID"));
		    	readConfig.setRbp_cdrdata(props.getProperty("RBP_CDRDATA"));
		    	readConfig.setRbp_called_network(props.getProperty("RBP_CALLED_NETWORK"));
		    	readConfig.setRbp_pid(props.getProperty("RBP_PID"));
		    	readConfig.setRbp_dbid(props.getProperty("RBP_DBID"));
		    	readConfig.setRbp_svc_ctg(props.getProperty("RBP_SVC_CTG"));
		    	readConfig.setRbp_interface_version(props.getProperty("RBP_INTERFACE_VERSION"));
		    	
		    	
		    	//RCSG 연동
		    	readConfig.setRcsg_primary_ip(props.getProperty("RCSG_PRIMARY_IP"));
		    	readConfig.setRcsg_primary_port(props.getProperty("RCSG_PRIMARY_PORT"));
		    	readConfig.setRcsg_secondary_ip(props.getProperty("RCSG_SECONDARY_IP"));
		    	readConfig.setRcsg_secondary_port(props.getProperty("RCSG_SECONDARY_PORT"));
		    	readConfig.setRcsg_connect_time_out(props.getProperty("RCSG_CONNECT_TIME_OUT"));
		    	readConfig.setRcsg_receive_time_out(props.getProperty("RCSG_RECEIVE_TIME_OUT"));
		    	readConfig.setRcsg_reconnect_sleep_time(props.getProperty("RCSG_RECONNECT_SLEEP_TIME"));
		    	readConfig.setRcsg_system_id(props.getProperty("RCSG_SYSTEM_ID"));
		    	readConfig.setRcsg_cdrdata(props.getProperty("RCSG_CDRDATA"));
		    	readConfig.setRcsg_called_network(props.getProperty("RCSG_CALLED_NETWORK"));
		    	readConfig.setRcsg_pid(props.getProperty("RCSG_PID"));
		    	readConfig.setRcsg_dbid(props.getProperty("RCSG_DBID"));
		    	readConfig.setRcsg_svc_ctg(props.getProperty("RCSG_SVC_CTG"));
		    	readConfig.setRcsg_interface_version(props.getProperty("RCSG_INTERFACE_VERSION"));
		    	
		    	
		    	//ESB 연동
		    	readConfig.setEsb_mps208_url(props.getProperty("ESB_MPS208_URL"));
		    	readConfig.setEsb_cm181_url(props.getProperty("ESB_CM181_URL"));
		    	readConfig.setEsb_time_out(props.getProperty("ESB_TIME_OUT"));
		    	
		    	
		    	//SMS format
		    	readConfig.setCharge_section_list(props.getProperty("CHARGE_SECTION_LIST"));
		    	readConfig.setLimit_excess(props.getProperty("LIMIT_EXCESS"));
		    	readConfig.setLimit_excess2(props.getProperty("LIMIT_EXCESS2"));
		    	readConfig.setCharge_complete(props.getProperty("CHARGE_COMPLETE"));
		    	readConfig.setSection_excess(props.getProperty("SECTION_EXCESS"));
		    	readConfig.setCancel_complete(props.getProperty("CANCEL_COMPLETE"));
		    	
		    	readConfig.setTime_out(props.getProperty("time_out"));
		    	
		    	
		    } catch (Exception e) {
				// TODO: handle exception
		    	log.error("########## Server Start Error ##########", e);
		    	destroy();
		    	System.exit(1);
			}
		 
		 
			Timer time = new Timer();
			SchedulerLog sl = new SchedulerLog();
			// Run scheduler
			time.schedule(sl, 0, 1000 * 1);
	 }
	
	 public void destroy() {

		 log.info("########## ADCB Server END ##########");
		 
	}
	
/*	public static void logException (Exception e, String seq) throws IOException {
		
		try {
			
			StringBuilder sb = new StringBuilder();
			
			Logger log = Logger.getLogger("oms");
			log.info(sb);

		} catch (Exception el) {
			// TODO Auto-generated catch block
			el.printStackTrace();
		}
	}*/
	
}
