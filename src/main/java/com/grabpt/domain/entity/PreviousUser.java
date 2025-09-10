package com.grabpt.domain.entity;

import com.grabpt.domain.enums.AuthRole;
import com.grabpt.domain.enums.Gender;
import com.grabpt.domain.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class PreviousUser {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "previous_id")
	private Long id;

	private String nickname;

	private String username;

	private String profileImageUrl;

	private Role role;

	private Gender gender;

	private String phone_number;

	private String email;

	@OneToOne
	@JoinColumn(name = "user_id")
	private Users user;

	public void setUser(Users user) {
		this.user = user;
		if (user != null && user.getPreviousUser() != this) {
			user.setPreviousUser(this);
		}
	}
}
