package com.grabpt.converter;

import org.springframework.data.domain.Page;

import com.grabpt.domain.entity.Suggestions;
import com.grabpt.dto.response.SuggestionResponseDto;

public class SuggestionConverter {

	public static Page<SuggestionResponseDto.SuggestionResponsePagingDto> toSuggestionResponsePageDto(
		Page<Suggestions> suggestionsPage) {

		return suggestionsPage.map(s -> {
			var pro = s.getProProfile();
			var user = pro.getUser();
			var address = user.getAddress();
			var requestion = s.getRequestion();

			return SuggestionResponseDto.SuggestionResponsePagingDto.builder()
				.nickname(user.getNickname())
				.center(pro.getCenter())
				.address(address != null ? address.getFullAddress() : "")
				.price(s.getPrice())
				.averageRate(pro.getAverageRating())
				.sessionCount(requestion != null ? requestion.getSessionCount() : null)
				.profileImageUrl(user.getProfileImageUrl())
				.requestionId(requestion != null ? requestion.getId() : null)
				.build();
		});
	}
}
