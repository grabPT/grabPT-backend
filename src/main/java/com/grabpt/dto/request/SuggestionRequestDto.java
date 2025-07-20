package com.grabpt.dto.request;

import java.time.LocalDateTime;

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
public class SuggestionRequestDto {

	@NotNull
	@Schema(description = "요청서 id", example = "1")
	private Long requestionId;

	@NotNull
	@Schema(description = "가격", example = "50000")
	private Integer price;

	@NotNull
	@Schema(description = "제안서에 들어갈 설명", example = "안녕하세요, 반갑습니다.")
	private String message;

	@NotNull
	@Schema(description = "지역", example = "성북동")
	private String location;

	@Schema(description = "발송 시간", example = "2025-07-17T06:56:14.215")
	private LocalDateTime sentAt;

	@Schema(description = "수락됬는지, 대기중인지 판별하는 boolean", example = "false")
	private Boolean isAgreed;
}
