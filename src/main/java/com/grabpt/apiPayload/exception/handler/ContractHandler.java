package com.grabpt.apiPayload.exception.handler;

import com.grabpt.apiPayload.code.BaseErrorCode;
import com.grabpt.apiPayload.exception.GeneralException;

public class ContractHandler extends GeneralException {
	public ContractHandler(BaseErrorCode errorCode) {
		super(errorCode);
	}
}
