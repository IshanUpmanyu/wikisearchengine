package com.wikisearchengine.wikisearcher.model;

public class SearchResult {
    private String title;
    private String url;
    private String description;
    private String date;

    private final static int MAX_DESCRIPTION_LENGTH = 200;

    public SearchResult(String title, String url, String description, String date) {
        this.title = title;
        this.url = url;
        this.description = trimDescription(description);
        this.date = date;
    }

    private String trimDescription(String description) {
        if(description != null && description.length() > MAX_DESCRIPTION_LENGTH){
            return description.substring(0, MAX_DESCRIPTION_LENGTH) + "...";
        }
        return description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
