package com.search.app.service;


import com.search.app.client.KakaoPlaceSearchClient;
import com.search.app.client.NaverPlaceSearchClient;
import com.search.app.dto.KeywordCountDto;
import com.search.app.dto.PlaceDto;

import com.search.app.dto.PlaceResponse;
import com.search.app.mapper.SearchKeywordMapper;
import com.search.common.response.ResponseObject;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class SearchService {
    private final RedisTemplate redisTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final SearchKeywordMapper searchKeywordMapper;
    private final KakaoPlaceSearchClient kakaoPlaceSearchClient;
    private final NaverPlaceSearchClient naverPlaceSearchClient;

    private final String REDIS_KEY_PREFIX = "search:place:";
    private final int MAX_SEARCH_SIZE = 5;
    private final int MAX_LIST_SIZE = 10;

    /**
     * 장소 검색
     *
     * @param originKeyword
     * @return
     */
    public ResponseObject getPlaceList(String originKeyword) {
        PlaceResponse response = new PlaceResponse();
        List<PlaceDto> placeList = new ArrayList<>();

        // keyword 공백제거
        String keyword = originKeyword.replaceAll(" ", "");

        try {
            // 키워드별 검색 횟수 update (Kafka 전송)
            kafkaTemplate.send("keyword-count", keyword);

            String redisKey = new StringBuilder().append(REDIS_KEY_PREFIX).append(keyword).toString();
            // redis에 해당 키워드에 대한 결과 있는지 조회
            placeList = (List<PlaceDto>) redisTemplate.opsForValue().get(redisKey);

            if (placeList == null) {
                // kakao api 조회
                List<PlaceDto> kakaoPlaceList = kakaoPlaceSearchClient.searchPlace(keyword, MAX_SEARCH_SIZE);
                // naver api 조회
                List<PlaceDto> naverPlaceList = naverPlaceSearchClient.searchPlace(keyword, MAX_SEARCH_SIZE);
                // 검색 결과 합치기
                placeList = mergePlaceList(kakaoPlaceList, naverPlaceList);

                // 검색 결과 redis 적재
                redisTemplate.opsForValue().set(redisKey, placeList, 3, TimeUnit.MINUTES);
            }

            response.setPlaces(placeList);
            return new ResponseObject(response);
        } catch (Exception e) {
            return new ResponseObject("Error", e.getMessage());
        }
    }

    private List<PlaceDto> mergePlaceList(List<PlaceDto> kakaoPlaceList, List<PlaceDto> naverPlaceList) {
        Map<String, PlaceDto> naverPlaceMap = new HashMap<>();
        // 동일업체 비교용 Map 생성
        for (PlaceDto naverPlace : naverPlaceList) {
            naverPlaceMap.put(getNomalizedTitle(naverPlace.getTitle()), naverPlace);
        }

        List<PlaceDto> commonPlaces = new ArrayList<>();
        List<PlaceDto> kakaoOnlyPlaces = new ArrayList<>();
        List<String> commonTitle = new ArrayList<>();

        // kakao 장소 결과로 동일 업체 추출
        for (PlaceDto kakaoPlace : kakaoPlaceList) {
            String title = getNomalizedTitle(kakaoPlace.getTitle());

            if (naverPlaceMap.containsKey(title)) {
                commonPlaces.add(kakaoPlace);
                commonTitle.add(title);
            } else {
                kakaoOnlyPlaces.add(kakaoPlace);
            }
        }

        // 동일 업체 제외한 naver 장소 결과
        List<PlaceDto> naverOnlyPlaces = naverPlaceList.stream()
                .filter(p -> !commonTitle.contains(getNomalizedTitle(p.getTitle())))
                .collect(Collectors.toList());

        List<PlaceDto> placeList = new ArrayList<>();

        placeList.addAll(commonPlaces);
        placeList.addAll(kakaoOnlyPlaces);
        placeList.addAll(naverOnlyPlaces);

        return placeList.stream()
                .limit(MAX_LIST_SIZE).collect(Collectors.toList());
    }

    // HTML 태그 제거, 공백문자 제거, 소문자처리
    private String getNomalizedTitle(String title) {
        return title.replaceAll("<[^>]*>", "")
                .replaceAll("\\s+", "")
                .toLowerCase();
    }

    /**
     * 검색 키워드 목록 조회
     *
     * @param size
     * @return
     */
    public ResponseObject getTopKeywordList(int size) {
        List<KeywordCountDto> keywordCountList = new ArrayList<>();
        String redisKey = "topKeyword";
        // redis 조회, 없을경우 db 조회
        keywordCountList = (List<KeywordCountDto>) redisTemplate.opsForValue().get(redisKey);
        if (keywordCountList == null) {
            keywordCountList = searchKeywordMapper.selectTopKeywordList(size);
            // 검색 결과 redis 적재
            redisTemplate.opsForValue().set(redisKey, keywordCountList, 10, TimeUnit.SECONDS);
        }

        return new ResponseObject(keywordCountList);
    }
}

