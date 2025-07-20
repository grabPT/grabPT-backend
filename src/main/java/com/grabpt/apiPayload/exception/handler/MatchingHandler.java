package com.grabpt.apiPayload.exception.handler;

import com.grabpt.apiPayload.code.BaseErrorCode;
import com.grabpt.apiPayload.exception.GeneralException;

public class MatchingHandler extends GeneralException {
	public MatchingHandler(BaseErrorCode errorCode) {
		super(errorCode);
	}
}
