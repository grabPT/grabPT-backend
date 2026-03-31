package com.grabpt.repository.ProProfileRepository;

import com.grabpt.domain.entity.ProProfile;
import com.grabpt.dto.request.ProSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProProfileRepositoryCustom {
    Page<ProProfile> searchProfiles(ProSearchRequest request, Pageable pageable);
}
