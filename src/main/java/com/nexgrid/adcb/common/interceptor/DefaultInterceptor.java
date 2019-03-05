package com.nexgrid.adcb.common.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;


public class DefaultInterceptor extends HandlerInterceptorAdapter{

private static final Logger log = LoggerFactory.getLogger(DefaultInterceptor.class);
	

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		
		boolean returnType = false;
		log.debug("##########  preHandle Start..#############");
        log.debug("##########  preHandle End..#############");
       
        return returnType;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object hadler, ModelAndView modelAndView) throws Exception {
		
		log.debug("##########  postHandle Start..#############");
		
		String requestURI = request.getRequestURI();
		
		String requestURL = request.getRequestURL().toString();
		
		do {
			if (requestURI.matches(".*/[^.]*\\")) {
				break;
			}
			if (requestURI.matches(".*/[^.]*\\")) {
				break;
			}
			if (requestURI.matches(".*/[^.]*\\")) {
				break;
			}
			if (requestURI.matches(".*/[^.]*\\")) {
				break;
			}
			return ;
		} while (false);

		if (requestURI.indexOf(".ajax") != -1 ||
			requestURI.indexOf(".json") != -1) {
			response.setHeader("Cache-Control","no-cache");
			response.setHeader("Pragma","no-cache");
			response.setDateHeader("Expires", 0);
		}

		if (modelAndView == null) {
			return ;
		}

        
        
		log.debug("##########  postHandle End..#############");
	}
	
	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
		
		
		log.debug("##########  afterCompletion Start..#############");
		log.debug("##########  afterCompletion End..#############");
	}

}
