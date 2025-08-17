package com.grabpt.domain.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.grabpt.domain.enums.Gender;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class ContractInfo {

	private String name ="";

	private LocalDate birth;

	private String phoneNumber="";

	@Enumerated(EnumType.STRING)
	private Gender gender;

	private String address="";

	// 추가
	private String signUrl="";

}
