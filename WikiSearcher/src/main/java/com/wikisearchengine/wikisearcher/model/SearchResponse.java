package com.wikisearchengine.wikisearcher.model;

import java.util.List;

public class SearchResponse {
    private long size;
    private List<SearchResult> results;

    public SearchResponse(long size, List<SearchResult> results) {
        this.size = size;
        this.results = results;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public List<SearchResult> getResults() {
        return results;
    }

    public void setResults(List<SearchResult> results) {
        this.results = results;
    }
}
