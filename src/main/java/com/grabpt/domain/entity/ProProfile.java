package com.grabpt.domain.entity;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.grabpt.domain.common.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@DynamicUpdate
@DynamicInsert
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ProProfile extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "pro_profile_id")
	private Long id;

	private String center;

	private Integer career; // 연차

	private String description; // 소개

	private String programDescription; // 프로그램 상세 설명
	private Integer pricePerSession; // 1회당 가격
	private Integer totalSessions; // 총 세션 수

	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private Category category;

	@OneToOne
	@JoinColumn(name = "user_id")
	private Users user;

	@OneToMany(mappedBy = "proProfile", cascade = CascadeType.ALL,
		orphanRemoval = true, fetch = FetchType.LAZY)
	private List<Suggestions> suggestions = new ArrayList<>();

	// --- 새로 추가된 1:N 관계들 ---
	@OneToMany(mappedBy = "proProfile", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ProCertification> certifications = new ArrayList<>();

	// 소개 사진
	@OneToMany(mappedBy = "proProfile", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ProPhoto> photos = new ArrayList<>();

	// 리뷰
	@OneToMany(mappedBy = "proProfile", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Review> reviews = new ArrayList<>();

	public void addSuggestion(Suggestions suggestion) {
		this.suggestions.add(suggestion);
		if (suggestion.getProProfile() != this) {
			suggestion.setProProfile(this);
		}
	}

	public void removeSuggestion(Suggestions suggestion) {
		this.suggestions.remove(suggestion);
		if (suggestion.getProProfile() == this) {
			suggestion.setProProfile(null);
		}
	}

	// 편의 메서드
	public void setUser(Users user) {
		this.user = user;
		if (user != null && user.getProProfile() != this) {
			user.setProProfile(this);
		}
	}
}

