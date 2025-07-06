package com.grabpt.domain.entity;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.grabpt.domain.common.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
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

	private String residence;

	@ElementCollection
	private List<String> activityAreas;

	private String center;

	private String career;


	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "pro_profile_id") // Category 테이블에 외래키로 생성
	private List<Category> categories = new ArrayList<>();

	@OneToOne
	@MapsId
	@JoinColumn(name = "user_id")
	private Users user;

	@OneToMany(mappedBy = "proProfile", cascade = CascadeType.ALL,
		orphanRemoval = true, fetch = FetchType.LAZY)
	private List<Suggestions> suggestions = new ArrayList<>();

	// --- 새로 추가된 1:N 관계들 ---
	@OneToMany(mappedBy = "proProfile", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ProCertification> certifications = new ArrayList<>();

	@OneToMany(mappedBy = "proProfile", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ProPhoto> photos = new ArrayList<>();

	@OneToMany(mappedBy = "proProfile", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ProProgram> programs = new ArrayList<>();

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

