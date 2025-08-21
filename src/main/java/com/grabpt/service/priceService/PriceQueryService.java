package com.grabpt.service.priceService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grabpt.repository.RequestionRepository.RequestionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PriceQueryService {

	private final RequestionRepository requestionRepository;

	public static record IntAvgPriceResult(int avgUnitPrice, long sampleCount) {
	}

	public IntAvgPriceResult getAvgUnitPrice(String categoryName, String city, String district, String street) {
		String fullRegion = buildFullRegion(city, district, street);

		double avg = requestionRepository.avgUnitPriceByCategoryAndRegion(categoryName, fullRegion); // double
		long cnt = requestionRepository.countByCategoryAndRegion(categoryName, fullRegion);

		// 소수점 제거 방식 선택:
		int avgInt = (int)Math.floor(avg);         // ① 버림 (기본)
		// int avgInt = (int) Math.round(avg);      // ② 반올림
		// int avgInt = (int) Math.ceil(avg);       // ③ 올림

		return new IntAvgPriceResult(avgInt, cnt);
	}

	private String buildFullRegion(String city, String district, String street) {
		String c = city == null ? "" : city.trim();
		String d = district == null ? "" : district.trim();
		String s = street == null ? "" : street.trim();
		return String.join(" ", new String[] {c, d, s}).trim().replaceAll("\\s+", " ");
	}
}
