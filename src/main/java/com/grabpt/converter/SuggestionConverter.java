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
				.suggestUserNickName(user.getNickname())
				.suggestCenter(pro.getCenter())
				.suggestAddress(address != null ? address.getFullAddress() : "")
				.suggestPrice(s.getPrice())
				.suggestAverageRate(pro.getAverageRating())
				.suggestSessionCount(requestion != null ? requestion.getSessionCount() : null)
				.photos(user.getProfileImageUrl())
				.suggestSuggestionId(s.getId())
				.build();
		});
	}
}
