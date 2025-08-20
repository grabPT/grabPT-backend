package com.grabpt.service.SettlementService;

import com.grabpt.dto.response.TrainerDashboardDto;
import com.grabpt.dto.response.UserDashboardResponseDto;

public interface SettlementService {

	TrainerDashboardDto getTrainerDashboard(Long proProfileId, int page, int size);

	UserDashboardResponseDto getUserDashboard(Long userId, int page, int size);
}
