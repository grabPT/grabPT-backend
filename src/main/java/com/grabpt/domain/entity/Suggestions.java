package com.grabpt.domain.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

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
import lombok.Setter;

@Entity
@Getter
@Setter
@DynamicUpdate
@DynamicInsert
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Suggestions extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "suggestions_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "requestions_id")
	private Requestions requestion;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "pro_profile_id")
	private ProProfile proProfile;

	@Column(nullable = false)
	private Integer price;

	@Column(nullable = false)
	private String message;

	@Column(nullable = false)
	private String location;

	private LocalDateTime sentAt;

	private Boolean isAgreed;

	// 편의 메서드
	public void setProProfile(ProProfile proProfile) {
		this.proProfile = proProfile;
		if (proProfile != null && !proProfile.getSuggestions().contains(this)) {
			proProfile.addSuggestion(this);
		}
	}

	public void setRequestion(Requestions requestion) {
		this.requestion = requestion;
		if (requestion != null && !requestion.getSuggestions().contains(this)) {
			requestion.addSuggestion(this);
		}
	}
}
