package com.grabpt.domain.entity;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.grabpt.domain.common.BaseEntity;
import com.grabpt.domain.enums.Gender;
import com.grabpt.domain.enums.RequestStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
public class Requestions extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "requestions_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private Users user;

	@OneToMany(mappedBy = "requestion", cascade = CascadeType.ALL,
		orphanRemoval = true, fetch = FetchType.LAZY)
	private List<Suggestions> suggestions = new ArrayList<>();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id")
	private Category category;

	@Column(nullable = false)
	private Integer price;

	@Column(nullable = false)
	private Integer sessionCount; // 추가

	@Column(nullable = false)
	private String purpose;

	@Column(nullable = false)
	private String ageGroup;

	@Enumerated(EnumType.STRING)
	private Gender userGender;

	@ElementCollection
	@CollectionTable(name = "requestion_available_days", joinColumns = @JoinColumn(name = "requestion_id"))
	@Column(name = "available_day")
	private List<String> availableDays;

	@ElementCollection
	@CollectionTable(name = "requestion_available_times", joinColumns = @JoinColumn(name = "requestion_id"))
	@Column(name = "available_time")
	private List<String> availableTimes;

	@Enumerated(EnumType.STRING)
	private Gender trainerGender;

	private String startPreference;

	@Column(nullable = false)
	private String location;

	@Enumerated(EnumType.STRING)
	private RequestStatus status;

	public void addSuggestion(Suggestions suggestion) {
		this.suggestions.add(suggestion);
		if (suggestion.getRequestion() != this) {
			suggestion.setRequestion(this);
		}
	}

	public void removeSuggestion(Suggestions suggestion) {
		this.suggestions.remove(suggestion);
		if (suggestion.getRequestion() == this) {
			suggestion.setRequestion(null);
		}
	}

	// 편의 메서드
	public void setUser(Users user) {
		this.user = user;
		if (user != null && !user.getRequestions().contains(this)) {
			user.addRequestion(this);
		}
	}
}
