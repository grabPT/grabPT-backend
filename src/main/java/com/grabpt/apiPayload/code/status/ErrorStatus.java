package com.grabpt.apiPayload.code.status;

import org.springframework.http.HttpStatus;

import com.grabpt.apiPayload.code.BaseErrorCode;
import com.grabpt.apiPayload.code.ErrorReasonDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {

	// 가장 일반적인 응답
	_INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
	_BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),
	_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON401", "인증이 필요합니다."),
	_FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),

	// 멤버 관려 에러
	MEMBER_NOT_FOUND(HttpStatus.BAD_REQUEST, "MEMBER4001", "사용자가 없습니다."),
	NICKNAME_NOT_EXIST(HttpStatus.BAD_REQUEST, "MEMBER4002", "닉네임은 필수 입니다."),

	// 예시,,,
	ARTICLE_NOT_FOUND(HttpStatus.NOT_FOUND, "ARTICLE4001", "게시글이 없습니다."),

	// 테스트용 - flag가 2인경우 exception
	TEMP_EXCEPTION(HttpStatus.BAD_REQUEST, "TEMP4001", "이거는 테스트"),

	CATEGORY_NOT_FOUND(HttpStatus.BAD_REQUEST, "CATE4001", "해당하는 카테고리가 없습니다."),

	INVALID_JWT_ISSUE(HttpStatus.BAD_REQUEST, "JWT4001", "유효한 JWT 토큰이 아닙니다."),

	INVALID_JWT_ISSUE_REFRESH(HttpStatus.BAD_REQUEST, "JWT4002", "유효한 Refresh 토큰이 아닙니다."),

	UNAUTHORIZED_SMS(HttpStatus.BAD_REQUEST, "SMS4001", "SMS 인증 실패, 인증 번호가 일치하지 않습니다."),

	INVALID_ROLE(HttpStatus.BAD_REQUEST, "ROLE4001", "올바르지 않은 Role 인풋입니다"),

	INVALID_GENDER(HttpStatus.BAD_REQUEST, "GENDER4001", "올바르지 않은 Gender 인풋입니다"),

	CHATROOM_NOT_FOUND(HttpStatus.BAD_REQUEST, "CHAT4001", "존재하지 않는 채팅입니다."),

	PRO_NOT_FOUND(HttpStatus.BAD_REQUEST, "PRO4001", "존재하지 않는 트레이너입니다."),

	REQUESTION_NOT_FOUND(HttpStatus.BAD_REQUEST, "REQ4001", "존재하지 않는 요청서입니다."),

	SUGGESTION_NOT_FOUND(HttpStatus.BAD_REQUEST, "SUG4001", "존재하지 않는 지원서입니다."),

	REQUESTION_ALREADY_MATCHED(HttpStatus.BAD_REQUEST, "REQ4002", "요청서가 이미 매치되었습니다."),

	MATCHING_NOT_FOUND(HttpStatus.BAD_REQUEST, "REQ4002", "매칭을 찾을 수 없습니다."),

	MATCHING_ALREADY_CANCLED(HttpStatus.BAD_REQUEST, "REQ4002", "매칭이 이미 취소되었습니다."),

	;

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;

	@Override
	public ErrorReasonDTO getReason() {
		return ErrorReasonDTO.builder()
			.message(message)
			.code(code)
			.isSuccess(false)
			.build();
	}

	@Override
	public ErrorReasonDTO getReasonHttpStatus() {
		return ErrorReasonDTO.builder()
			.message(message)
			.code(code)
			.isSuccess(false)
			.httpStatus(httpStatus)
			.build()
			;
	}
}
