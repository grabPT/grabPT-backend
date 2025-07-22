package com.grabpt.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "address")
public class Address {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "address_id")
	private Long id;

	private String city; // 도/특별시/광역시 (서울시)
	private String district; // 시/군/구 (관악구)
	private String street; // 읍/면/동 (봉천동)
	private String zipcode; // 우편번호 (12345)
	private String streetCode; // 도로명주소
	private String specAddress; // 상세주소

	@OneToOne
	@JoinColumn(name = "user_id", unique = true)
	private Users user;
}
