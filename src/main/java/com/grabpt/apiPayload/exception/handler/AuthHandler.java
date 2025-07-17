package com.grabpt.apiPayload.exception.handler;

import com.grabpt.apiPayload.code.BaseErrorCode;
import com.grabpt.apiPayload.exception.GeneralException;

public class AuthHandler extends GeneralException {
	public AuthHandler(BaseErrorCode errorCode) {
		super(errorCode);
	}
}
