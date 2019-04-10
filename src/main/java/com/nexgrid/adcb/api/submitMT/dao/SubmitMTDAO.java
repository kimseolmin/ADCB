package com.nexgrid.adcb.api.submitMT.dao;

import com.nexgrid.adcb.common.vo.SmsSendVO;

public interface SubmitMTDAO {

	/**
	 * SMS 정보 INSERT
	 * @param smsList
	 * @return
	 * @throws Exception
	 */
	int insertSmsSend(SmsSendVO smsVO) throws Exception;
}
