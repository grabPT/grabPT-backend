package com.grabpt.domain.entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class PtPrice {
	private int sessionCount;
	private int price;
}

