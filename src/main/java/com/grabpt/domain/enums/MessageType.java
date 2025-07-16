package com.grabpt.domain.enums;

import java.util.Locale;

public enum MessageType {
	TEXT, IMAGE, FILE;
	// 문자열을 ENUM으로 안전하게 변환하는 메서드
	public static MessageType fromString(String type) {
		try {
			return MessageType.valueOf(type.toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException | NullPointerException e) {
			throw new IllegalArgumentException("유효하지 않은 메시지 타입입니다: " + type);
		}
	}
}
