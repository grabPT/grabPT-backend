package com.grabpt.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.grabpt.domain.common.BaseEntity;
import com.grabpt.domain.enums.MatchingStatus;

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
public class Matching extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "requestion_id", unique = true)
	private Requestions requestion; // 어떤 요청서에 의한 계약인지

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "suggestion_id", unique = true)
	private Suggestions suggestion; // 어떤 제안서를 채택했는지

	@OneToOne(mappedBy = "matching", cascade = CascadeType.ALL, orphanRemoval = true)
	private Contract contract;

	@Column(nullable = false)
	private Integer agreedPrice; // 실제 확정된 가격

	@Column(nullable = false)
	private LocalDateTime matchedAt; // 계약 성사 시간

	@Enumerated(EnumType.STRING)
	@Column(length = 30)
	private MatchingStatus status; // 계약 상태 - MATCHED, CANCELLED, COMPLETED
}
