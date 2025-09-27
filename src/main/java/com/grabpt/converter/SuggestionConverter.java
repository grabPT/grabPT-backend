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
				.userNickname(user.getNickname())
				.centerName(pro.getCenter())
				.location(address != null ? address.getFullAddress() : "")
				.suggestedPrice(s.getPrice())
				.averageRating(pro.getAverageRating())
				.sessionCount(requestion != null ? requestion.getSessionCount() : null)
				.profileImageUrl(user.getProfileImageUrl())
				.suggestionId(s.getId())
				.build();
		});
	}
}
