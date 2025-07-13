package com.grabpt.apiPayload.exception.handler;

import com.grabpt.apiPayload.code.BaseErrorCode;
import com.grabpt.apiPayload.exception.GeneralException;

public class UserHandler extends GeneralException {

	public UserHandler(BaseErrorCode errorCode) {
		super(errorCode);
	}
}
