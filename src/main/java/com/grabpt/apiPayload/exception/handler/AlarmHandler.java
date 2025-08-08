package com.grabpt.apiPayload.exception.handler;

import com.grabpt.apiPayload.code.BaseErrorCode;
import com.grabpt.apiPayload.exception.GeneralException;

public class AlarmHandler extends GeneralException {
	public AlarmHandler(BaseErrorCode code) {
		super(code);
	}
}
