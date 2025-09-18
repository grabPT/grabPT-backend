package com.grabpt.service.RequestionService;

import java.util.List;
import java.util.Optional;

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

	void update(Long requestionId, RequestionRequestDto dto, String email);

	void delete(Long requestionId, String email);

	Page<RequestionResponseDto.UserOwnRequestionDto> getRequestionsByUser(HttpServletRequest request,
		Pageable pageable) throws IllegalAccessException;

	boolean canEditRequestion(Long requestionId, String email);

	Requestions findById(Long requestionId);


	Page<Requestions> page(Long userId,Pageable pageable);

	Optional<Object> findByIdForUpdate(Long requestionId);

	Requestions save(Requestions requestions);

}
