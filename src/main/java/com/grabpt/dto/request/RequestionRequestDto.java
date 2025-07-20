package com.grabpt.dto.request;

import java.util.List;

import com.grabpt.domain.enums.Gender;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RequestionRequestDto {

	@NotNull
	@Schema(description = "해당 카테고리 id", example = "1")
	private Long categoryId;

	@NotNull
	@Schema(description = "가격", example = "50000")
	private Integer price;

	@NotNull
	@Schema(description = "횟수", example = "1")
	private Integer sessionCount;

	@NotNull
	@Schema(description = "목적", example = "운동")
	private String purpose;

	@NotNull
	@Schema(description = "나이대", example = "20")
	private String ageGroup;

	@Schema(description = "사용자 성별", example = "MALE")
	private Gender userGender;

	@Schema(description = "가능한 날자")
	private List<String> availableDays;

	@Schema(description = "가능한 시간대")
	private List<String> availableTimes;

	@Schema(description = "트레이너 성별", example = "MALE")
	private Gender trainerGender;

	@Schema(description = "선호 시작", example = "2025.7.20")
	private String startPreference;

	@NotNull
	@Schema(description = "지역", example = "성북동")
	private String location;
}
