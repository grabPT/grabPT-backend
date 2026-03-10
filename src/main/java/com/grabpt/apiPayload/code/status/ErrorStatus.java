package com.grabpt.apiPayload.code.status;

import org.springframework.http.HttpStatus;

import com.grabpt.apiPayload.code.BaseErrorCode;
import com.grabpt.apiPayload.code.ErrorReasonDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {

	/// 카테고리 관련 에러
	CATEGORY_NOT_FOUND(HttpStatus.BAD_REQUEST, "CATE4001", "해당하는 카테고리가 없습니다."),

	/// SMS 인증 관련 에러
	UNAUTHORIZED_SMS(HttpStatus.BAD_REQUEST, "SMS4001", "SMS 인증 실패, 인증 번호가 일치하지 않습니다."),

	/// Role 관련 에러
	INVALID_ROLE(HttpStatus.BAD_REQUEST, "ROLE4001", "올바르지 않은 Role 인풋입니다"),

	/// Gender 관련 에러
	INVALID_GENDER(HttpStatus.BAD_REQUEST, "GENDER4001", "올바르지 않은 Gender 인풋입니다"),

	/// 계약서 관련 오류
	CONTRACT_NOT_FOUND(HttpStatus.BAD_REQUEST, "CONT4001", "계약이 존재하지 않습니다"),
	CONTRACT_DELETE_NOT_AUTHORIZED(HttpStatus.FORBIDDEN, "CONT4031", "계약서 삭제 권한이 없습니다"),
	CONTRACT_ALREADY_PAID(HttpStatus.BAD_REQUEST, "CONT4002", "이미 결제가 완료된 계약서는 수정할 수 없습니다"),

	/// 이미지 관련 오류
	NOT_IMAGE(HttpStatus.BAD_REQUEST, "IMG4001", "이미지가 없습니다."),

	/// 알람 관련 오류
	ALARM_NOT_FOUND(HttpStatus.BAD_REQUEST, "ALA4001", "알림이 존재하지 않습니다."),

	/// 프로필 관련 오류
	PROFILE_NOT_FOUND(HttpStatus.BAD_REQUEST, "PROF4001", "프로필이 존재하지 않습니다"),
	PRO_NOT_FOUND(HttpStatus.BAD_REQUEST, "PRO4002", "존재하지 않는 트레이너입니다."),

	/// 채팅 관련 에러
	CHATROOM_NOT_FOUND(HttpStatus.BAD_REQUEST, "CHAT4001", "존재하지 않는 채팅입니다."),
	MESSAGE_NOT_FOUND(HttpStatus.BAD_REQUEST, "CHAT4002", "존재하지 않는 메시지 입니다."),

	/// 일반적인 에러
	_INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
	_BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),
	_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON401", "인증이 필요합니다."),
	_FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),

	/// 멤버 관련 에러
	MEMBER_NOT_FOUND(HttpStatus.BAD_REQUEST, "MEMBER4001", "사용자가 없습니다."),
	NICKNAME_NOT_EXIST(HttpStatus.BAD_REQUEST, "MEMBER4002", "닉네임은 필수 입니다."),
	MEMBERS_ROLE_IS_USER(HttpStatus.BAD_REQUEST, "MEMBER4001", "사용자의 ROLE이 USER입니다."),
	MEMBERS_ROLE_IS_PRO(HttpStatus.BAD_REQUEST, "MEMBER4001", "사용자의 ROLE이 PRO입니다."),

	/// 요청서 관련 오류
	REQUESTION_NOT_FOUND(HttpStatus.BAD_REQUEST, "REQ4001", "존재하지 않는 요청서입니다."),
	REQUESTION_ALREADY_MATCHED(HttpStatus.BAD_REQUEST, "REQ4002", "요청서가 이미 매치되었습니다."),
	MATCHING_NOT_FOUND(HttpStatus.BAD_REQUEST, "REQ4003", "매칭을 찾을 수 없습니다."),
	MATCHING_ALREADY_CANCLED(HttpStatus.BAD_REQUEST, "REQ4004", "매칭이 이미 취소되었습니다."),
	REQUESTION_DELETE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "REQU4005", "매칭 진행/완료된 요청서는 삭제할 수 없습니다."),
	REQUESTION_DELETE_NOT_OWNER(HttpStatus.FORBIDDEN, "REQU4006", "요청서 작성자만 삭제할 수 있습니다."),
	REQUESTION_EXPIRED(HttpStatus.BAD_REQUEST, "REQU4007", "요청서가 만료되었습니다."),

	/// 지원서 관련 오류
	SUGGESTION_NOT_FOUND(HttpStatus.BAD_REQUEST, "SUGG4001", "존재하지 않는 지원서입니다."),
	INVALID_PRO(HttpStatus.BAD_REQUEST, "SUGG4002", "제안서를 등록한 트레이너와 다른 유저입니다."),
	INVALID_USER(HttpStatus.BAD_REQUEST, "SUGG4003", "요청서를 등록한 유저와 다른 유저입니다."),
	INVALID_SUGGESTION_FOR_REQUESTION(HttpStatus.BAD_REQUEST, "SUGG4004", "유효한 제안서가 아닙니다."),
	SUGGESTION_ALREADY_MATCHED(HttpStatus.BAD_REQUEST, "SUGG4005", "이미 매칭된 제안서입니다."),
	SUGGESTION_DELETE_NOT_ALLOWED_STATUS(HttpStatus.BAD_REQUEST, "SUGG4006", "매칭 진행/완료된 제안서는 삭제할 수 없습니다."),
	SUGGESTION_DELETE_NOT_OWNER(HttpStatus.FORBIDDEN, "SUGG4007", "제안서 작성자만 삭제할 수 있습니다."),

	/// 인증 관련 오류
	INVALID_JWT_ISSUE(HttpStatus.BAD_REQUEST, "JWT4001", "유효한 JWT 토큰이 아닙니다."),
	INVALID_JWT_ISSUE_REFRESH(HttpStatus.BAD_REQUEST, "JWT4002", "유효한 Refresh 토큰이 아닙니다."),
	DUPLICATE_USER_EMAIL(HttpStatus.BAD_REQUEST, "AUTH4001", "이미 가입된 이메일입니다."),
	AUTH_MISSING_REFRESH_COOKIE(HttpStatus.UNAUTHORIZED, "A001", "refreshToken을 찾을 수 없습니다."),
	AUTH_INVALID_OR_EXPIRED(HttpStatus.UNAUTHORIZED, "A002", "refreshToken이 유효하지 않습니다."),
	AUTH_EXPIRED(HttpStatus.UNAUTHORIZED, "A003", "refreshToken이 만료되었습니다."),
	AUTH_USER_NOT_FOUND(HttpStatus.UNAUTHORIZED, "A004", "refreshToken에 해당하는 유저를 찾지 못했습니다."),
	AUTH_STORED_REFRESH_NULL(HttpStatus.UNAUTHORIZED, "A005", "저장된 refreshToken이 null값입니다."),
	AUTH_REFRESH_MISMATCH(HttpStatus.UNAUTHORIZED, "A006", "refreshToken이 mismatch입니다."),
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
