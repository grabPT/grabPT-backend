package com.grabpt.dto.request;

import java.time.LocalDate;
import java.util.List;

import com.grabpt.domain.entity.Category;
import com.grabpt.domain.enums.Gender;

import lombok.Builder;

@Builder
public record RequestionUpdateDto(
	Category category,
	Integer price,
	Integer sessionCount,
	List<String> purpose,
	String etcPurposeContent,
	String content,
	String ageGroup,
	Gender userGender,
	List<String> availableDays,
	List<String> availableTimes,
	Gender trainerGender,
	LocalDate startPreference,
	String location
) {
}
