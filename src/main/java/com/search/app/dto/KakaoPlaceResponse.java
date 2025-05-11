package com.search.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KakaoPlaceResponse {
    @JsonProperty("documents")
    private List<KakaoPlace> places;

    @JsonProperty("meta")
    private KakaoMeta meta;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KakaoPlace {
        @JsonProperty("place_name")
        private String placeName;

        @JsonProperty("address_name")
        private String addressName;

        @JsonProperty("road_address_name")
        private String roadAddressName;

        @JsonProperty("x")
        private String longitude;

        @JsonProperty("y")
        private String latitude;

        @JsonProperty("phone")
        private String phone;

        @JsonProperty("category_name")
        private String categoryName;

        @JsonProperty("place_url")
        private String placeUrl;

        public PlaceDto toPlace() {
            return PlaceDto.builder()
                    .title(placeName)
                    .address(addressName)
                    .roadAddress(roadAddressName)
                    .longitude(longitude)
                    .latitude(latitude)
                    .phone(phone)
                    .categoryName(categoryName)
                    .build();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KakaoMeta {
        @JsonProperty("total_count")
        private int totalCount;

        @JsonProperty("pageable_count")
        private int pageableCount;

        @JsonProperty("is_end")
        private boolean isEnd;
    }

    // 결과 리스트 셋팅
    public List<PlaceDto> toPlaces() {
        if (places == null) {
            return Collections.emptyList();
        }
        return places.stream()
                .map(KakaoPlace::toPlace)
                .collect(Collectors.toList());
    }

}
