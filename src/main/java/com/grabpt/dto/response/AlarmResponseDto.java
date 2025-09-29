package com.grabpt.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AlarmResponseDto {
	Long alarmId;
	Long userId;
	String type;
	String title;
	String content;
	String redirectUrl;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
	LocalDateTime sentAt;
	@JsonProperty("isRead")
	boolean isRead;

	public Long getAlarmId() {
		return alarmId;
	}

	public Long getUserId() {
		return userId;
	}

	public String getType() {
		return type;
	}

	public String getTitle() {
		return title;
	}

	public String getContent() {
		return content;
	}

	public String getRedirectUrl() {
		return redirectUrl;
	}

	public LocalDateTime getSentAt() {
		return sentAt;
	}
	public boolean getIsRead() {
		return isRead;
	}
}
