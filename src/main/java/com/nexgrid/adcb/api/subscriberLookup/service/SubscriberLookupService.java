package com.nexgrid.adcb.api.subscriberLookup.service;

import java.net.ConnectException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.UnknownHttpStatusCodeException;

import com.nexgrid.adcb.common.dao.CommonDAO;
import com.nexgrid.adcb.common.exception.CommonException;
import com.nexgrid.adcb.common.service.CommonService;
import com.nexgrid.adcb.common.vo.LogVO;
import com.nexgrid.adcb.util.EnAdcbOmsCode;
import com.nexgrid.adcb.util.Init;
import com.nexgrid.adcb.util.SendUtil;
import com.nexgrid.adcb.util.StringUtil;

@Service
public class SubscriberLookupService {
	
	@Autowired
	private CommonService commonService;
	
	@Autowired
	private CommonDAO commonDAO;

	public void doSubscriberLookup(Map<String, Object> paramMap, LogVO logVO) throws Exception {
		
		String ctn = StringUtil.getNcas444(paramMap.get("msisdn").toString());
		String ncasUrl = Init.readConfig.getNcas_url() + ctn;
		HttpHeaders headers = new HttpHeaders();
		int connTimeout = Integer.parseInt(Init.readConfig.getNcas_connect_time_out());
		int readTimeout = Integer.parseInt(Init.readConfig.getNcas_read_time_out());
		
		
		ResponseEntity<String> resEntity = null;
		Map<String, String> ncasRes = null;
		
		logVO.setNcasReqTime();
		logVO.setFlow("[ADCB] --> [NCAS]");
		try {
			resEntity = SendUtil.requestUrl(HttpMethod.GET, headers, null, ncasUrl, "NCAS", connTimeout, readTimeout, logVO);
			logVO.setFlow("[ADCB] <-- [NCAS]");
			logVO.setNcasResTime();	// ncas 연동 종료
			
			//NCAS연동 결과값
			ncasRes = commonService.getNcasResHeader(resEntity);
			
	    	// paramMap에 NCAS 결과값 저장
	    	paramMap.put("ncasRes", ncasRes);
			
	    	String respcode = ncasRes.get("RESPCODE"); // RESPCODE
	    	
	    	//NCAS 응답코드 저장
	    	logVO.setNcasResultCode(respcode);
	    	
	    	
			String res_msg = ncasRes.get("RESPMSG"); // 처리 결과 내용
			
	    	//고객정보가 없거나 번호 이동된  사용자 차단 - 해지된 사용자는 고객정보 없음으로 나옴
	    	//70 : 고객정보 없음  
			if("70".equals(respcode) ) {
				throw new CommonException(EnAdcbOmsCode.NCAS_70, res_msg);
			}
			

			// 71 : SKT로 번호이동   -> boku에게 105 매핑코드밖에 줄 수가 없어서..
			if("71".equals(respcode)) {
				throw new CommonException(EnAdcbOmsCode.NCAS_71.status(), EnAdcbOmsCode.NCAS_70.mappingCode(),  EnAdcbOmsCode.NCAS_71.value(), res_msg);
			}
			
			// 76 : KTF로 번호이동 -> boku에게 105 매핑코드밖에 줄 수가 없어서..
			if("76".equals(respcode)) {
				throw new CommonException(EnAdcbOmsCode.NCAS_76.status(), EnAdcbOmsCode.NCAS_70.mappingCode(),  EnAdcbOmsCode.NCAS_76.value(), res_msg);
			}
			
			if(!"00".equals(respcode)) {
				throw new CommonException(EnAdcbOmsCode.NCAS_API.status(), EnAdcbOmsCode.NCAS_API.mappingCode(),  EnAdcbOmsCode.NCAS_API.value() + respcode, res_msg);
			}
			
			
			try {
				String fee_type = ncasRes.get("FEE_TYPE"); //요금제 타입
				
				//CTN 값이 정상값이 아닐경우 차단
		    	int blockctn = 0;
		    	if(ctn.length() == 12){
		    		blockctn = commonDAO.getBlockCTN(ctn);
		    	}else {
		    		blockctn = 1;
		    	}
		    	if(blockctn != 0 ) { 
		    		logVO.setFlow("[ADCB] <-- [DB]");
		    		// boku에게 105 매핑코드밖에 줄 수가 없어서..
		    		throw new CommonException(EnAdcbOmsCode.NCAS_70.status(), EnAdcbOmsCode.NCAS_70.mappingCode(),  EnAdcbOmsCode.DB_BLOCK_CTN.value(), EnAdcbOmsCode.DB_BLOCK_CTN.logMsg());
		    	}
		    	
		    	//정상 요금제가 아니면 차단
		    	int blockfeetype = 0;
		    	if(!"".equals(fee_type)){
		    		blockfeetype = commonDAO.getBlockFeeType(fee_type);
		    	}else {
		    		blockfeetype = 1;
		    	}
		    	if(blockfeetype != 0) {
		    		logVO.setFlow("[ADCB] <-- [DB]");
		    		// boku에게 105 매핑코드밖에 줄 수가 없어서..
		    		throw new CommonException(EnAdcbOmsCode.NCAS_70.status(), EnAdcbOmsCode.NCAS_70.mappingCode(),  EnAdcbOmsCode.DB_BLOCK_FEETYPE.value(), EnAdcbOmsCode.DB_BLOCK_FEETYPE.logMsg());
		    	}
			}catch(DataAccessException adcbExc){
				/*SQLException se = (SQLException) adcbExc.getRootCause();
				logVO.setRsCode(Integer.toString(se.getErrorCode()));*/
				logVO.setFlow("[ADCB] --> [DB]");
				throw new CommonException(EnAdcbOmsCode.DB_ERROR, adcbExc.getMessage());
				
			}catch(ConnectException adcbExc) {
				logVO.setFlow("[ADCB] --> [DB]");
				throw new CommonException(EnAdcbOmsCode.DB_CONNECT_ERROR, adcbExc.getMessage());
			}catch(CommonException common) {
				throw common;
			}catch (Exception adcbExc) {
				throw new CommonException(EnAdcbOmsCode.DB_INVALID_ERROR, adcbExc.getMessage());
			}
			
			
		}catch(HttpClientErrorException adcbExc){
			
			logVO.setFlow("[ADCB] <-- [NCAS]");
			throw new CommonException(EnAdcbOmsCode.NCAS_INVALID_ERROR, adcbExc.getMessage());
		
		}catch(HttpServerErrorException adcbExc) {
			
			logVO.setFlow("[ADCB] <-- [NCAS]");
			throw new CommonException(EnAdcbOmsCode.NCAS_INVALID_ERROR, adcbExc.getMessage());
			
		}catch(UnknownHttpStatusCodeException adcbExc) {
			
			logVO.setFlow("[ADCB] <-- [NCAS]");
			throw new CommonException(EnAdcbOmsCode.NCAS_INVALID_ERROR, adcbExc.getMessage());
			
		}catch (ResourceAccessException adcbExc) {
			
			// connect, read time out
			if(adcbExc.getMessage().indexOf("Read") > 0) {
				throw new CommonException(EnAdcbOmsCode.NCAS_READ_TIMEOUT, adcbExc.getMessage());
			}else {
				throw new CommonException(EnAdcbOmsCode.NCAS_CONNECT_TIMEOUT, adcbExc.getMessage());
			}
		
		}catch(CommonException adcbExc) {
			
			throw adcbExc;
			
		}catch (Exception adcbExc) {
			
			throw new CommonException(EnAdcbOmsCode.NCAS_INVALID_ERROR, adcbExc.getMessage());
		}
	}
}
