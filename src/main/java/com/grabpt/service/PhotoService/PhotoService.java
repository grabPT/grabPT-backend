package com.grabpt.service.PhotoService;

import com.grabpt.domain.entity.ProProfile;
import com.grabpt.domain.entity.Users;

import org.springframework.web.multipart.MultipartFile; // MultipartFile import 추가

import java.util.List;

public interface PhotoService {
	/**
	 * 전문가 프로필의 사진 정보를 업데이트합니다.
	 * @param proProfile 수정할 전문가의 프로필 엔티티
	 * @param photoFiles 업로드할 이미지 파일 리스트
	 */
	void updatePhotos(ProProfile proProfile, List<MultipartFile> photoFiles);

	String uploadProfileImage(MultipartFile profileImage);
}
