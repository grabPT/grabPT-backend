package com.grabpt.domain.entity;

import java.time.LocalDate;

import com.grabpt.domain.common.BaseEntity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Contract extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne
	@JoinColumn(name = "matching_id")
	private Matching matching;

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "name", column = @Column(name = "user_name")),
		@AttributeOverride(name = "birth", column = @Column(name = "user_birth")),
		@AttributeOverride(name = "phoneNumber", column = @Column(name = "user_phone_number")),
		@AttributeOverride(name = "gender", column = @Column(name = "user_gender")),
		@AttributeOverride(name = "address", column = @Column(name = "user_address")),
		@AttributeOverride(name = "signUrl", column = @Column(name = "user_signUrl"))
	})
	private ContractInfo userInfo;

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "name", column = @Column(name = "pro_name")),
		@AttributeOverride(name = "birth", column = @Column(name = "pro_birth")),
		@AttributeOverride(name = "phoneNumber", column = @Column(name = "pro_phone_number")),
		@AttributeOverride(name = "gender", column = @Column(name = "pro_gender")),
		@AttributeOverride(name = "address", column = @Column(name = "pro_address")),
		@AttributeOverride(name = "signUrl", column = @Column(name = "pro_signUrl"))
	})
	private ContractInfo proInfo;

	private Integer totalSession;
	private Integer price;
	private LocalDate startDate;
	private String ptAddress;

	private LocalDate contractDate;
	private String contractFileUrl;
}
