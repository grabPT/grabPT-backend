package com.grabpt.service.RequestionService;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.grabpt.domain.entity.Requestions;
import com.grabpt.dto.request.RequestionRequestDto;
import com.grabpt.dto.response.RequestionResponseDto;

import jakarta.servlet.http.HttpServletRequest;

public interface RequestionService {
	List<Requestions> getReqeustions(String categoryCode, Pageable pageable);

	Requestions save(RequestionRequestDto dto, String email);

	RequestionResponseDto.RequestionDetailResponseDto getDetail(Long requestionId);

	Page<RequestionResponseDto.RequestionResponsePagingDto> getNearbyRequestions(
		HttpServletRequest request, String sortBy, Pageable pageable) throws
		IllegalAccessException;
}
