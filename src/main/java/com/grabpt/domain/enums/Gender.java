package com.grabpt.domain.enums;

public enum Gender {
	MALE("남자"),
	FEMALE("여자");

	private final String korean;

	Gender(String korean) {
		this.korean = korean;
	}

	public String getKorean() {
		return korean;
	}

	public static Gender fromKorean(String korean) {
		if ("남자".equals(korean))
			return MALE;
		if ("여자".equals(korean))
			return FEMALE;
		throw new IllegalArgumentException("성별은 '남자' 또는 '여자'만 입력해주세요.");
	}
}
