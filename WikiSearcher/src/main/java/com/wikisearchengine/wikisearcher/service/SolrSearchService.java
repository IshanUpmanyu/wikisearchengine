package com.wikisearchengine.wikisearcher.service;

import com.wikisearchengine.wikisearcher.model.SearchRequest;
import com.wikisearchengine.wikisearcher.model.SearchResponse;
import com.wikisearchengine.wikisearcher.model.SearchResult;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.MapSolrParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class SolrSearchService implements SearchService{

    @Value("${solr.base.url}")
    private String solrBaseUrl;

    @Value("${en.wiki.collection.name}")
    private String enWikiCollectionName;

    @Value("${solr.max.results.per.page}")
    private String maxResultsPerPage;

    private SolrClient solrClient;

    private final DateTimeFormatter SOLR_DATE_FORMAT = DateTimeFormatter.ISO_INSTANT;


    @Override
    public SearchResponse search(SearchRequest request) {
        int maxResultsPerPageInt = Integer.parseInt(maxResultsPerPage);
        try {
            int resultsPerPage = request.getResultsPerPage();
            int pageNum = request.getPageNum();
            String query = request.getQuery();
            if (resultsPerPage > maxResultsPerPageInt) {
                resultsPerPage = maxResultsPerPageInt;
            }


            final Map<String, String> queryParamMap = new HashMap<>();
            queryParamMap.put("q", query);
            queryParamMap.put("start", String.valueOf(pageNum * resultsPerPage));
            queryParamMap.put("rows", String.valueOf(resultsPerPage));
            queryParamMap.put("hl", "on");
            queryParamMap.put("hl.fl", "body_t");
            queryParamMap.put("hl.fragsize", "200");
            queryParamMap.put("defType", "dismax");
            queryParamMap.put("qf", "title_t^20 body_t^0.5");
            MapSolrParams queryParams = new MapSolrParams(queryParamMap);


            final QueryResponse response = solrClient.query(enWikiCollectionName, queryParams);

            final SolrDocumentList documents = response.getResults();
            Map<String, Map<String, List<String>>>highlights = response.getHighlighting();

            List<SearchResult> results = new ArrayList<>();
            for (SolrDocument document : documents) {
                final String id = (String) document.getFirstValue("id");
                final String url = "https://en.wikipedia.org/wiki?curid=" + id;
                final String title = (String) document.getFirstValue("title_t");
                final String body = (String) document.getFirstValue("body_t");
                String highlight = null;
                final Map<String, List<String>> highlightForDoc = highlights.get(id);
                if(highlightForDoc != null){
                    List<String> bodyHighlights = highlightForDoc.get("body_t");
                    if(bodyHighlights != null && bodyHighlights.size() > 0){
                        highlight = bodyHighlights.get(0);
                    }
                }

                String desc = highlight == null ? body == null? "": body : highlight;
                final Date date = (Date) document.getFirstValue("date_dt");
                results.add(new SearchResult(title, url, desc, SOLR_DATE_FORMAT.format(date.toInstant())));
            }
            return new SearchResponse(documents.getNumFound(), results);
        }catch (Exception e){
            throw new RuntimeException("An unexpected error occurred.", e);
        }
    }

    private String buildSolrQuery(String query) {
        String words[] = null;
        if(query != null){
             words = query.split(" ");
        }else {
           throw new RuntimeException("Query should not be null.");
        }
        StringBuilder solrQuery = new StringBuilder();
        for(String word: words){
            solrQuery.append("title_t:"+word+"^10.0 OR ");
        }

        for(String word: words){
            solrQuery.append("body_t:"+word+"^0.5 OR ");
        }

        solrQuery.delete(solrQuery.length() - 3, solrQuery.length());

        return solrQuery.toString();
    }



    @PostConstruct
    private void initSolrClient(){
        if(solrClient == null){
            solrClient = new HttpSolrClient.Builder(solrBaseUrl).build();
        }
    }
}
