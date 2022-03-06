package com.wikisearcheninge;

import com.wikisearcheninge.indexer.EnWikiIndexer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class EnWikiIndexerApplication {
    public static void main(String[] args){
        Properties prop = new Properties();

        String confFolderPath = System.getProperty("CONFIG_PATH");
        try (InputStream input = new FileInputStream(confFolderPath + File.separator + "config.properties")) {
            prop.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        EnWikiIndexer indexer = EnWikiIndexer.getInstance(prop);
        indexer.indexData();
    }
}
