package com.grabpt.service.RequestionService;

import com.grabpt.domain.entity.Requestions;
import com.grabpt.repository.RequestionRepository.RequestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestionServiceImpl implements RequestionService{

	private final RequestionRepository requestionRepository;

	@Override
	public List<Requestions> getReqeustions(String categoryCode, Pageable pageable) {
		return requestionRepository.findTop6RequestionsByCategory(categoryCode, pageable);
	}
}
