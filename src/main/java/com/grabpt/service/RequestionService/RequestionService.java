package com.grabpt.service.RequestionService;

import com.grabpt.domain.entity.Requestions;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RequestionService {
	List<Requestions> getReqeustions(String categoryCode, Pageable pageable);
}
