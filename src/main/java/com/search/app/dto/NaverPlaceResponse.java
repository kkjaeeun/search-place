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
public class NaverPlaceResponse {
    @JsonProperty("items")
    private List<NaverPlace> places;

    @JsonProperty("total")
    private int total;

    @JsonProperty("display")
    private int display;

    @JsonProperty("start")
    private int start;

    @JsonProperty("lastBuildDate")
    private String lastBuildDate;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NaverPlace {
        @JsonProperty("title")
        private String title;

        @JsonProperty("address")
        private String address;

        @JsonProperty("roadAddress")
        private String roadAddress;

        @JsonProperty("mapx")
        private String mapx;

        @JsonProperty("mapy")
        private String mapy;

        @JsonProperty("telephone")
        private String telephone;

        @JsonProperty("category")
        private String category;

        @JsonProperty("link")
        private String link;

        public PlaceDto toPlace() {
            return PlaceDto.builder()
                    .title(title)
                    .address(address)
                    .roadAddress(roadAddress)
                    .longitude(mapx)
                    .latitude(mapy)
                    .phone(telephone)
                    .categoryName(category)
                    .build();
        }
    }

    // 결과 리스트 셋팅
    public List<PlaceDto> toPlaces() {
        if (places == null) {
            return Collections.emptyList();
        }
        return places.stream()
                .map(NaverPlace::toPlace)
                .collect(Collectors.toList());
    }

}
