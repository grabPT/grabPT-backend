package com.grabpt.apiPayload.exception.handler;

import com.grabpt.apiPayload.code.BaseErrorCode;
import com.grabpt.apiPayload.exception.GeneralException;

public class ProfileHandler extends GeneralException {
	public ProfileHandler(BaseErrorCode errorCode) {
		super(errorCode);
	}
}
