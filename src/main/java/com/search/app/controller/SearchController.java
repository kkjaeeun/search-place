package com.search.app.controller;

import com.search.app.service.SearchService;
import com.search.common.response.ResponseObject;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1")
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/place")
    public ResponseObject getPlaceList(@RequestParam(value = "keyword", required = true) String keyword) {
        return searchService.getPlaceList(keyword);
    }

    @GetMapping("/keyword")
    public ResponseObject getTopKeywordList(@RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        return searchService.getTopKeywordList(size);
    }
}
