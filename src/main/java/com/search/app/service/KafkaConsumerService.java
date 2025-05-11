package com.search.app.service;

import com.search.app.mapper.SearchKeywordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class KafkaConsumerService {
    private final SearchKeywordMapper searchKeywordMapper;

    @KafkaListener(topics = "keyword-count", groupId = "my-consumer-group")
    public void consume(String keyword) {
        // keyword count 조회
        Integer count = searchKeywordMapper.selectKeywordCount(keyword);

        if (count != null) {
            // 있을 경우 update
            searchKeywordMapper.incrementKeywordCount(keyword);
        } else {
            // 없을 경우 insert
            searchKeywordMapper.insertKeywordCount(keyword);
        }
    }
}
