package com.grabpt.domain.entity;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.grabpt.domain.common.BaseEntity;
import com.grabpt.domain.enums.AuthRole;
import com.grabpt.domain.enums.Gender;
import com.grabpt.domain.enums.Role;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
public class Users extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Long id;

	@Column(nullable = false, length = 20)
	private String nickname;

	@Column(nullable = false, length = 50)
	private String username;

	private String profileImageUrl;

	@Enumerated(EnumType.STRING)
	private Role role;

	@Enumerated(EnumType.STRING)
	private Gender gender;

	@Column(nullable = false)
	private String phone_number;

	@Column(name = "oauth_provider")
	private String oauthProvider;

	@Column(name = "oauth_id")
	private String oauthId;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(nullable = true)
	private String password;

	@Enumerated(EnumType.STRING)
	private AuthRole authRole;

	@Column(length = 500)
	private String refreshToken;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<UserChatRoom> userChatRooms = new ArrayList<>();

	@OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private Address address;

	// 양방향 연관관계 매핑
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Requestions> requestions = new ArrayList<>();

	@OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private UserProfile userProfile;

	@OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private ProProfile proProfile;

	public void setAddress(Address address) {
		this.address = address;
		if (address != null && address.getUser() != this) {
			address.setUser(this);  // 연관관계 편의 메서드
		}
	}

	// 편의 메서드
	public void setUserProfile(UserProfile userProfile) {
		this.userProfile = userProfile;
		if (userProfile != null && userProfile.getUser() != this) {
			userProfile.setUser(this);
		}
	}

	public void setProProfile(ProProfile proProfile) {
		this.proProfile = proProfile;
		if (proProfile != null && proProfile.getUser() != this) {
			proProfile.setUser(this);
		}
	}

	public void addRequestion(Requestions requestion) {
		this.requestions.add(requestion);
		if (requestion.getUser() != this) {
			requestion.setUser(this);
		}
	}

	public void removeRequestion(Requestions requestion) {
		this.requestions.remove(requestion);
		requestion.setUser(null);
	}

	public void addUserChatRoom(UserChatRoom userChatRoom) {
		userChatRooms.add(userChatRoom);
		userChatRoom.setUser(this);
	}

	public void encodePassword(String password) {
		this.password = password;
	}

}
