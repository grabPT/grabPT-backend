package com.grabpt.dto.request;

import com.grabpt.domain.enums.SortType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProSearchRequest {
    private String keyword;
    private String categoryCode;
    private String city;
    private String district;
    private Integer minPrice;
    private Integer maxPrice;
    private Double minRating;
    private SortType sortBy = SortType.RATING;
}
