package com.grabpt.service.SuggestionService;

import com.grabpt.domain.entity.Suggestions;
import com.grabpt.dto.request.SuggestionRequestDto;
import com.grabpt.dto.response.SuggestionResponseDto;

public interface SuggestionService {

	Suggestions save(SuggestionRequestDto dto, String email);

	SuggestionResponseDto.SuggestionDetailResponseDto getDetail(Long suggestionId);
}
