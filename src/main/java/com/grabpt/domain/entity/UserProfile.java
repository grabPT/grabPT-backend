package com.grabpt.domain.entity;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.grabpt.domain.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
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
public class UserProfile extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_profile_id")
	private Long id;

	@OneToOne
	@JoinColumn(name = "user_id")
	private Users user;

	private String residence; // 거주지역

	@ElementCollection
	private List<String> preferredAreas; // 운동희망지역

	// 양방향 연관관계
	@ManyToMany
	@JoinTable(
		name = "user_profile_preferred_category",
		joinColumns = @JoinColumn(name = "user_profile_id"),
		inverseJoinColumns = @JoinColumn(name = "category_id")
	)
	private List<Category> categories = new ArrayList<>();

	// 편의 메서드
	public void setUser(Users user) {
		this.user = user;
		if (user != null && user.getUserProfile() != this) {
			user.setUserProfile(this);
		}
	}
}
