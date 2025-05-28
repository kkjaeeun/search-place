package com.search.app.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KeywordCountDto {
    private String keyword;
    private int count;

    public KeywordCountDto(String keyword, int count) {
        this.keyword = keyword;
        this.count = count;
    }
}
