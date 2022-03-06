package com.wikisearchengine.wikisearcher.model;

import java.util.List;

public class SearchResponse {
    private long total;
    private List<SearchResult> results;

    public SearchResponse(long total, List<SearchResult> results) {
        this.total = total;
        this.results = results;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<SearchResult> getResults() {
        return results;
    }

    public void setResults(List<SearchResult> results) {
        this.results = results;
    }
}
