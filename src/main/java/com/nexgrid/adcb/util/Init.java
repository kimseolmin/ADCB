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
		    	
		    	// OMS 서버번호
		    	readConfig.setServer_num(props.getProperty("SERVER_NUM"));
		    	
		    	
		    	//NCAS 연동
		    	readConfig.setNcas_url(props.getProperty("NCAS_URL"));
		    	readConfig.setNcas_charset(props.getProperty("NCAS_CHARSET"));
		    	readConfig.setNcas_connect_time_out(props.getProperty("NCAS_CONNECT_TIME_OUT"));
		    	readConfig.setNcas_read_time_out(props.getProperty("NCAS_READ_TIME_OUT"));
		    	readConfig.setNcas_header_name(props.getProperty("NCAS_HEADER_NAME"));
		    	
		    	
		    	//RBP 연동
		    	readConfig.setRbp_system_id(props.getProperty("RBP_SYSTEM_ID"));
		    	readConfig.setRbp_cdrdata(props.getProperty("RBP_CDRDATA"));
		    	readConfig.setRbp_called_network(props.getProperty("RBP_CALLED_NETWORK"));
		    	readConfig.setRbp_pid(props.getProperty("RBP_PID"));
		    	readConfig.setRbp_dbid(props.getProperty("RBP_DBID"));
		    	readConfig.setRbp_svc_ctg(props.getProperty("RBP_SVC_CTG"));
		    	readConfig.setRbp_interface_version(props.getProperty("INTERFACE_VERSION"));
		    	
		    	
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
