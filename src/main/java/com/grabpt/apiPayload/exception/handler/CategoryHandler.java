package com.grabpt.apiPayload.exception.handler;

import com.grabpt.apiPayload.code.BaseErrorCode;
import com.grabpt.apiPayload.exception.GeneralException;

public class CategoryHandler extends GeneralException {
	public CategoryHandler(BaseErrorCode errorCode) {
		super(errorCode);
	}
}
