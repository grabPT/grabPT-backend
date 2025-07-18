package com.grabpt.apiPayload.exception.handler;

import com.grabpt.apiPayload.code.BaseErrorCode;
import com.grabpt.apiPayload.exception.GeneralException;

public class SuggestionHandler extends GeneralException {

	public SuggestionHandler(BaseErrorCode errorCode) {
		super(errorCode);
	}
}
