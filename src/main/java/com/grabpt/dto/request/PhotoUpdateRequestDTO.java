package com.grabpt.dto.request;

import jakarta.validation.Valid;
import java.util.List;
import lombok.Getter;

@Getter
public class PhotoUpdateRequestDTO {

	@Valid
	private List<PhotoRequestDTO> photos;
}
