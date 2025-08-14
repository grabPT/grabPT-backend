package com.grabpt.domain.entity;

import com.grabpt.domain.enums.Gender;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Embeddable
public class ContractInfo {

	private String name;

	private LocalDate birth;

	private String phoneNumber;

	@Enumerated(EnumType.STRING)
	private Gender gender;

	private String address;

}
