package com.grabpt.domain.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
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

	@JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
	private LocalDate birth;

	private String phoneNumber="";

	@Enumerated(EnumType.STRING)
	private Gender gender;

	private String location="";

	// 추가
	private String signImageUrl="";

}
