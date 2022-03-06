package com.wikisearcheninge.indexer;

import com.wikisearcheninge.exceptions.NoMoreDataException;
import com.wikisearcheninge.parser.EnWikiXMLParser;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.common.SolrInputDocument;
import org.wikiclean.WikiClean;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class EnWikiIndexer {

    private SolrClient solrClient = null;

    private static volatile EnWikiIndexer instance = null;

    private WikiClean cleaner = null;

    private String wikiDumpFilePath = null;
    private int numShards = 1;
    private int numReplicas = 1;

    private final SimpleDateFormat WIKI_DATE_FORMAT = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss.sss");
    private final DateTimeFormatter SOLR_DATE_FORMAT = DateTimeFormatter.ISO_INSTANT;
    private EnWikiIndexer(){
    }

    public static EnWikiIndexer getInstance(Properties properties){
        if(instance == null){
            synchronized (EnWikiIndexer.class){
                if(instance == null){
                    instance = new EnWikiIndexer();
                    instance.wikiDumpFilePath = properties.getProperty("wiki.file");
                    instance.cleaner = new WikiClean.Builder().build();
                    String numShards = properties.getProperty("solr.shards");
                    instance.numShards = numShards == null ? 1: Integer.parseInt(numShards);
                    String numReplicas = properties.getProperty("solr.replicas");
                    instance.numReplicas = numShards == null ? 1: Integer.parseInt(numReplicas);
                    instance.initSolrClient(properties);
                }
            }
        }
        return instance;
    }

    private void initSolrClient(Properties properties){
        if(solrClient == null){
            String solrBaseUrl = properties.getProperty("solr.base.url");
            solrClient = new HttpSolrClient.Builder(solrBaseUrl).build();
        }
    }

    public void indexData(){
        try {
            EnWikiXMLParser parser = new EnWikiXMLParser(wikiDumpFilePath);
            String[] tuple;
            String xmlEndTag = "</text>";
            String xmlStartTag = "<text xml:space=\"preserve\">";
            String collectionName = createCollection();
            long docsIndexed = 1;
            System.out.println("Starting indexing documents.");
            List<SolrInputDocument> docs = new ArrayList<>();
            while (true) {
                try {
                    tuple = parser.next();
                }catch (NoMoreDataException e){
                    break;
                }
                String id = cleaner.clean(xmlStartTag + tuple[EnWikiXMLParser.ID] + xmlEndTag);
                String body = cleaner.clean(xmlStartTag + tuple[EnWikiXMLParser.BODY] + xmlEndTag);
                String date = cleaner.clean(xmlStartTag + parseDateToSolrFormat(tuple[EnWikiXMLParser.DATE]) + xmlEndTag);
                String title = cleaner.clean(xmlStartTag + tuple[EnWikiXMLParser.TITLE] + xmlEndTag);
                docs.add(createSolrDoc(id, title, body, date));
                if(docsIndexed % 500 == 0){
                    this.solrClient.add(collectionName, docs, 1000);
                    System.out.println("Indexed "+ docsIndexed+" docs.");
                    docs.clear();
                }
                docsIndexed++;
            }
            this.solrClient.add(collectionName, docs, 1000);
            docs.clear();
            System.out.println("Finished indexing documents. "+ docsIndexed+" documents indexed.");


        }catch (Exception e){
            throw new RuntimeException("An error occurred while trying to index wiki dump data for file: "+wikiDumpFilePath, e);
        }
    }

    private SolrInputDocument createSolrDoc(String id, String title, String body, String date){
        final SolrInputDocument doc = new SolrInputDocument();
        doc.addField("id",id);
        doc.addField("title_t", title);
        doc.addField("body_t", body);
        doc.addField("date_dt", date);
        return doc;
    }

    private String createCollection() throws SolrServerException, IOException {
        String collectionName = "en-wikipedia";
        System.out.println("Creating collection: " + collectionName);
        CollectionAdminRequest.Create createCollectionRequest = CollectionAdminRequest.createCollection(collectionName, numShards, numReplicas);
        this.solrClient.request(createCollectionRequest);
        return collectionName;
    }

    private String parseDateToSolrFormat(String date) throws ParseException {
        Date parsedDate = WIKI_DATE_FORMAT.parse(date);
        return SOLR_DATE_FORMAT.format(parsedDate.toInstant());
    }
}
