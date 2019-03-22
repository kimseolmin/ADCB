package com.nexgrid.adcb.common.error.controller;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nexgrid.adcb.common.service.CommonService;
import com.nexgrid.adcb.common.vo.LogVO;
import com.nexgrid.adcb.util.EnAdcbOmsBack;
import com.nexgrid.adcb.util.EnAdcbOmsFront;
import com.nexgrid.adcb.util.LogUtil;



/**
 * HTTP Status 에러 페이지 
 * 
 * @author Daehong
 */
@Controller
public class ErrorPageController {
	
	
	private Logger log = LoggerFactory.getLogger(getClass());	
	
	@Autowired
	private CommonService commonService;
	
	//@Autowired
	//private MessageSourceAccessor messageSource;
	
//	@Autowired
//	private MappingJackson2JsonView jsonView;
	
	
	
	
	/**
	 * @param url
	 * @return mav
	 * @throws Exception
	 * @summary 에러처리 컨트롤러
	 */
	/**
	 * @param url
	 * @return
	 * @throws Exception
	 * @summury 404 url 처리 에러
	 */
	@RequestMapping("/error/{url}")
	@ResponseBody
	public Map<String, Object> errorController(@PathVariable String url, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		//Create a seqId and reqTime and serviceType concurrently with declaration
		LogVO logVO = new LogVO("INVAILD");
		
		//Service Start Log Print
		LogUtil.startServiceLog(logVO, request);
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		
		paramMap.put("sCode", "400");
		paramMap.put("eCode", EnAdcbOmsFront.SERVICE_URL.getDefaultCode() + EnAdcbOmsBack.INVALID_URL.getDefaultCode());
		paramMap.put("apiResultCode", "4");
		
		Map<String, Object> errMap = new LinkedHashMap<String, Object>();
		
		/*errMap.put("errId", logVO.getSeqId());
		errMap.put("errCode", "4002");
		errMap.put("errMsg", "Invalid Request Body");*/
		
		response.setStatus(400);
		
		//request.getHeader(HttpHeaders.REFERER);
		
		try {
			
			logVO.setLogTime();
			logVO.setLogType();
			logVO.setResultCode("40000000");
			logVO.setApiResultCode("4002");
			logVO.setResTime();
			logVO.setClientIp(commonService.getIpAddr(request));
			//logVO.setSvcName();
			
			//commonService.omsLogWrite(errMap, logVO);
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		
		LogUtil.EndServiceLog(errMap, logVO);
		return errMap;
	}

}

