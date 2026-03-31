package com.grabpt.apiPayload.exception.handler;

import com.grabpt.apiPayload.code.BaseErrorCode;
import com.grabpt.apiPayload.exception.GeneralException;

public class RefundHandler extends GeneralException {
    public RefundHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
