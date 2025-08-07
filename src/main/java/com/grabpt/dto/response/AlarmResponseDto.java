package com.grabpt.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AlarmResponseDto {
	Long id;
	String type;
	String title;
	String content;
	String redirectUrl;
	LocalDateTime createdAt;
	boolean isRead;
}
