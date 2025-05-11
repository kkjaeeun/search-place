package com.search.app.client;

import com.search.app.dto.KakaoPlaceResponse;
import com.search.app.dto.PlaceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class KakaoPlaceSearchClient {
    private final WebClient kakaoWebClient;;

    @Value("${external.kakao.place-search-uri}")
    private String placeSearchUri;

    public List<PlaceDto> searchPlace(String keyword, int size) {
        List<PlaceDto> kakaoPlaceResponse = kakaoWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(placeSearchUri)
                        .queryParam("query", keyword)
                        .queryParam("size", size)
                        .build())
                .retrieve()
                .bodyToMono(KakaoPlaceResponse.class)
                .map(response -> response.toPlaces())
                .block();

        return kakaoPlaceResponse;
    }
}
