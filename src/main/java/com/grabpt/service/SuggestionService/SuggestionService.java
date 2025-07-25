package com.grabpt.service.SuggestionService;

import org.springframework.data.domain.Page;

import com.grabpt.domain.entity.Suggestions;
import com.grabpt.dto.request.SuggestionRequestDto;
import com.grabpt.dto.response.SuggestionResponseDto;

import jakarta.servlet.http.HttpServletRequest;

public interface SuggestionService {

	Suggestions save(SuggestionRequestDto dto, String email);

	SuggestionResponseDto.SuggestionDetailResponseDto getDetail(Long suggestionId);

	Page<SuggestionResponseDto.SuggestionResponsePagingDto> getSuggestionsByRequestionId(Long requestionId, int page);

	Page<SuggestionResponseDto.MySuggestionPagingDto> getMySuggestions(HttpServletRequest request, int page) throws
		IllegalAccessException;
}
