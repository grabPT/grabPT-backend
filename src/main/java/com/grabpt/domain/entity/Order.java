package com.grabpt.domain.entity;

import com.grabpt.domain.common.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

@Entity
@Getter
@Table(name = "orders")
@NoArgsConstructor
public class Order extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long price;

	private String itemName;

	private String orderUid; // 주문 번호

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Users user;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "payment_id")
	private Payment payment;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "matching_id")
	private Matching matching; // 어떤 매칭에서 발생한 결제인지 (1:N)

	@Builder
	public Order(Long price, String itemName, String orderUid, Users user, Payment payment, Matching matching) {
		this.price = price;
		this.itemName = itemName;
		this.orderUid = orderUid;
		this.user = user;
		this.payment = payment;
		this.matching = matching;
	}
}
