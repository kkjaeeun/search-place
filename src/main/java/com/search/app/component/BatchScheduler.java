package com.search.app.component;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@EnableScheduling
@Component
public class BatchScheduler {
    private final KeywordRedisComponent keywordRedisComponent;

    /**
     * 서버 시작시 키워드 데이터 load
     */
    @PostConstruct
    public void init() {
        log.info("Load keyword count data to Redis");
        keywordRedisComponent.loadKeywordCountList();
    }

    /**
     * 정각마다 키워드 redis zset 데이터 DB에 저장
     */
    @Scheduled(cron = "0 0 * * * *")
    public void syncKeywordCountToDb() {
        log.info("save keyword count redis to db");
        keywordRedisComponent.saveKeywordCountRedisToDB();
    }


}
