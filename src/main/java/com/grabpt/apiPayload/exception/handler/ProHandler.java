package com.grabpt.apiPayload.exception.handler;

import com.grabpt.apiPayload.code.BaseErrorCode;
import com.grabpt.apiPayload.exception.GeneralException;

public class ProHandler extends GeneralException {
	public ProHandler(BaseErrorCode errorCode) {
		super(errorCode);
	}
}
