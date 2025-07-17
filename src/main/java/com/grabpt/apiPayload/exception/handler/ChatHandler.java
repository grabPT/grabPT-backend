package com.grabpt.apiPayload.exception.handler;

import com.grabpt.apiPayload.code.BaseErrorCode;
import com.grabpt.apiPayload.exception.GeneralException;

public class ChatHandler extends GeneralException {
	public ChatHandler(BaseErrorCode errorCode) {
		super(errorCode);
	}
}
