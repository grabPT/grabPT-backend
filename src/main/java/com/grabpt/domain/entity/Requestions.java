package com.grabpt.domain.entity;

import static java.util.List.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.grabpt.domain.common.BaseEntity;
import com.grabpt.domain.enums.Gender;
import com.grabpt.domain.enums.RequestStatus;
import com.grabpt.dto.request.RequestionUpdateDto;

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

	@ElementCollection
	@CollectionTable(name = "requestion_purposes", joinColumns = @JoinColumn(name = "requestion_id"))
	@Column(name = "purpose")
	private List<String> purpose = new ArrayList<>();

	//사용자가 목적에 '기타'를 선택할 경우 input 값을 포함시켜 전송해야함
	private String etcPurposeContent;

	//기타 세부사항
	private String content;

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

	private LocalDate startPreference;

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

	public void setPurpose(List<String> purpose) {
		this.purpose = new ArrayList<>(purpose);
	}

	public void setAvailableDays(List<String> availableDays) {
		this.availableDays = new ArrayList<>(availableDays);
	}

	public void setAvailableTimes(List<String> availableTimes) {
		this.availableTimes = new ArrayList<>(availableTimes);
	}

	public void applyUpdate(RequestionUpdateDto u) {
		Objects.requireNonNull(u, "update dto is null");

		// requireEditable(); // 편집 가능 검증 로직 추가 가능

		if (u.category() != null)
			this.category = u.category();
		if (u.price() != null)
			this.price = u.price();
		if (u.sessionCount() != null)
			this.sessionCount = u.sessionCount();

		if (u.purpose() != null)
			this.purpose = copyOf(u.purpose());
		if (u.etcPurposeContent() != null)
			this.etcPurposeContent = u.etcPurposeContent();
		if (u.content() != null)
			this.content = u.content();

		if (u.ageGroup() != null)
			this.ageGroup = normalize(u.ageGroup());
		if (u.userGender() != null)
			this.userGender = u.userGender();

		if (u.availableDays() != null)
			this.availableDays = copyOf(u.availableDays());
		if (u.availableTimes() != null)
			this.availableTimes = copyOf(u.availableTimes());

		if (u.trainerGender() != null)
			this.trainerGender = u.trainerGender();
		if (u.startPreference() != null)
			this.startPreference = u.startPreference();
		if (u.location() != null)
			this.location = normalize(u.location());
	}

	private String normalize(String s) {
		return (s == null) ? null : s.trim().replaceAll("\\s+", " ");
	}

	/// 편집 가능 검증 로직, 나중에 추가 가능
	// private void requireEditable() {
	// 	// 예: 매칭 진행 전(MATCHING)일 때만 수정 가능
	// 	if (this.status != null && this.status != RequestStatus.MATCHING) {
	// 		throw new IllegalStateException("Requestion is not editable in status: " + this.status);
	// 	}
	// }
}
