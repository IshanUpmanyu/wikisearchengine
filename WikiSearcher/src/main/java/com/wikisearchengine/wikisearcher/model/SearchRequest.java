package com.wikisearchengine.wikisearcher.model;

public class SearchRequest {
    private String query;
    private int pageNum;
    private int resultsPerPage;

    public SearchRequest(String query, int pageNum, int resultsPerPage) {
        this.query = query;
        this.pageNum = pageNum;
        this.resultsPerPage = resultsPerPage;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getResultsPerPage() {
        return resultsPerPage;
    }

    public void setResultsPerPage(int resultsPerPage) {
        this.resultsPerPage = resultsPerPage;
    }
}
