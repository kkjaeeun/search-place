package com.search.app.component;

import com.search.app.dto.KeywordCountDto;
import com.search.app.mapper.SearchKeywordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Component
public class KeywordRedisComponent {
    private final RedisTemplate redisTemplate;
    private final SearchKeywordMapper searchKeywordMapper;

    private final String KEYWORD_REDIS_KEY = "keywords";

    /**
     * keyword 전체 리스트 적재
     */
    public void loadKeywordCountList() {
        try {
            List<KeywordCountDto> keywordList = searchKeywordMapper.selectKeywordList();

            for (KeywordCountDto keyword : keywordList) {
                redisTemplate.opsForZSet().add(KEYWORD_REDIS_KEY, keyword.getKeyword(), keyword.getCount());
            }
        } catch (RedisConnectionFailureException e) {
            log.error("Redis Connection failed : {}", e.getMessage());
        }
    }

    /**
     * 키워드 카운트 증가
     *
     * @param keyword
     */
    public void addKeywordCount(String keyword) {
        try {
            redisTemplate.opsForZSet().incrementScore(KEYWORD_REDIS_KEY, keyword, 1);
        } catch (RedisConnectionFailureException e) {
            log.error("Redis Connection failed : {}", e.getMessage());
        }
    }

    /**
     * 키워드 순위 조회
     *
     * @param size
     * @return
     */
    public List<KeywordCountDto> getTopKeywordList(int size) {
        try {
            Set<ZSetOperations.TypedTuple<String>> tuples = redisTemplate.opsForZSet().reverseRangeWithScores(KEYWORD_REDIS_KEY, 0, size - 1);

            if (tuples == null) {
                // 키워드 데이터 redis 적재
                return Collections.emptyList();
            }

            List<KeywordCountDto> keywordCountList = new ArrayList<>();
            for (ZSetOperations.TypedTuple<String> tuple : tuples) {
                keywordCountList.add(new KeywordCountDto(tuple.getValue(), tuple.getScore().intValue()));
            }

            return keywordCountList;
        } catch (RedisConnectionFailureException e) {
            log.error("Redis Connection failed : {}", e.getMessage());
            return Collections.emptyList();
        }

    }

    @Transactional
    public void saveKeywordCountRedisToDB() {
        Set<ZSetOperations.TypedTuple<String>> tuples = redisTemplate.opsForZSet().rangeWithScores(KEYWORD_REDIS_KEY, 0, -1);

        if (tuples == null) {
            return;
        }

        for (ZSetOperations.TypedTuple<String> tuple : tuples) {
            String keyword = tuple.getValue();
            int count = tuple.getScore().intValue();

            // keyword count 조회
            Integer keywordCount = searchKeywordMapper.selectKeywordCount(keyword);

            if (keywordCount != null) {
                // 있을 경우 update
                searchKeywordMapper.updateKeywordCount(keyword, count);
            } else {
                // 없을 경우 insert
                searchKeywordMapper.insertKeywordWithCount(keyword, count);
            }
        }
    }
}
