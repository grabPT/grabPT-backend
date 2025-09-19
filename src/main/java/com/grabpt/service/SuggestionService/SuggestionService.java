package com.grabpt.service.SuggestionService;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.grabpt.domain.entity.Suggestions;
import com.grabpt.dto.request.SuggestionRequestDto;
import com.grabpt.dto.response.SuggestionResponseDto;

import jakarta.servlet.http.HttpServletRequest;

public interface SuggestionService {

	// Suggestions save(SuggestionRequestDto dto, String email);

	Suggestions save(SuggestionRequestDto dto, String email, List<MultipartFile> photos);

	SuggestionResponseDto.SuggestionDetailResponseDto getDetail(Long suggestionId);

	Page<SuggestionResponseDto.SuggestionResponsePagingDto> getSuggestionsByRequestionId(Long requestionId, int page);

	Page<SuggestionResponseDto.MySuggestionPagingDto> getMySuggestions(HttpServletRequest request, int page) throws
		IllegalAccessException;

	void updateSuggestion(Long suggestionId, SuggestionRequestDto dto, String email);

	void deleteSuggestion(Long suggestionId, String email);

	boolean canEditSuggestion(Long suggestionId, String email);

	Optional<Object> findById(Long suggestionId);

}
