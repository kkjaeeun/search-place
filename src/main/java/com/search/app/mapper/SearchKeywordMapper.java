package com.search.app.mapper;

import com.search.app.dto.KeywordCountDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SearchKeywordMapper {
    Integer selectKeywordCount(@Param("keyword") String keyword);

    void incrementKeywordCount(@Param("keyword") String keyword);

    void insertKeywordCount(@Param("keyword") String keyword);

    List<KeywordCountDto> selectTopKeywordList(@Param("size") int size);
}
