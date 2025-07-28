package com.grabpt.domain.entity;

import com.grabpt.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Contract extends BaseEntity {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
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
		@AttributeOverride(name = "address", column = @Column(name = "user_address"))
	})
	private ContractInfo userInfo;

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "name", column = @Column(name = "pro_name")),
		@AttributeOverride(name = "birth", column = @Column(name = "pro_birth")),
		@AttributeOverride(name = "phoneNumber", column = @Column(name = "pro_phone_number")),
		@AttributeOverride(name = "gender", column = @Column(name = "pro_gender")),
		@AttributeOverride(name = "address", column = @Column(name = "pro_address"))
	})
	private ContractInfo proInfo;

	private Integer totalSession;
	private Integer price;
	private String startDate;
	private String ptAddress;

	private LocalDate contractDate;
	private String contractFileUrl;
}
