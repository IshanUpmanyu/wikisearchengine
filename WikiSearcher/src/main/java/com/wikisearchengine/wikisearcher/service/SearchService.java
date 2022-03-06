package com.wikisearchengine.wikisearcher.service;

import com.wikisearchengine.wikisearcher.model.SearchRequest;
import com.wikisearchengine.wikisearcher.model.SearchResponse;

public interface SearchService {
    SearchResponse search(SearchRequest request);
}
