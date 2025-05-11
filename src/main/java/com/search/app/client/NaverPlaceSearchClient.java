package com.search.app.client;

import com.search.app.dto.NaverPlaceResponse;
import com.search.app.dto.PlaceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NaverPlaceSearchClient {
    private final WebClient naverWebClient;

    @Value("${external.naver.place-search-uri}")
    private String placeSearchUri;

    public List<PlaceDto> searchPlace(String keyword, int size) {
        return naverWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(placeSearchUri)
                        .queryParam("query", keyword)
                        .queryParam("display", size)
                        .build())
                .retrieve()
                .bodyToMono(NaverPlaceResponse.class)
                .map(response -> response.toPlaces())
                .block();
    }
}
