package com.grabpt.service.SuggestionService;

import com.grabpt.domain.entity.Suggestions;
import com.grabpt.dto.request.SuggestionRequestDto;

public interface SuggestionService {

	Suggestions save(SuggestionRequestDto dto, String email);
}
