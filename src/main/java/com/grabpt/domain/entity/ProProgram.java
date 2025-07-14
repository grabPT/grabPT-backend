package com.grabpt.domain.entity;

import com.grabpt.domain.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ProProgram extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "pro_profile_id")
	private ProProfile proProfile;

	@Column(nullable = false)
	private String title; // 프로그램 이름 예 : "벌크업 코스"

	@Column(nullable = false)
	private String description; // 프로그램 상세 설명

	private Integer pricePerSession; // 1회당 가격
	private Integer totalSessions; // 총 세션 수
}
