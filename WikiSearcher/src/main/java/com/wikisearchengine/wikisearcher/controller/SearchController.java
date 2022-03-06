package com.wikisearchengine.wikisearcher.controller;

import com.wikisearchengine.wikisearcher.model.SearchRequest;
import com.wikisearchengine.wikisearcher.model.SearchResponse;
import com.wikisearchengine.wikisearcher.service.SearchService;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/wikisearcher/search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @PostMapping
    public SearchResponse search(@RequestBody SearchRequest request) {
        return searchService.search(request);
    }

    @GetMapping
    public String hello(){
        return "Hello! Welcome to Wiki Searcher!";
    }
}
