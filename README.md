# Wiki Search Engine

Wiki Search Engine allows you to search over wikipedia english articles. This project was started to learn how to implement a search engine that shows relevant result to user.

It is made by indexing nearly 11.5 Million wikipeida articles on Apache Solr. 

**Try it at:** [wikisearchengine.com](http://wikisearchengine.com)

# Technologies used at a glance

**Backend:** Java 11, SpringBoot, Apache Solr 8

**Front End:** HTML, Javascript, JQuery, Bootstrap 5

**Infrastructure:** DigitalOcean droplets and block storage.

**Tools:** Git, Github, Maven

**Libraries Used:** WikiClean, IndexWikipedia

# How to install

## Prerequisites

- maven
- java 11 or higher
- Apache Solr 8.11

## Installation steps

### Building the release packages

- Go to the sources root and type `mvn clean package`
- Deployment zips will be available at:
  - WikiSearcher/target/WikiSearcher-0.0.1-SNAPSHOT.zip
  - WikiIndexer/target/WikiIndexer-1.0-SNAPSHOT.zip

### Get the Wikipedia XML Dumps

Download the Wikipedia page articles XML dump file. I used the enwiki-latest-pages-articles.xml.bz2 file available [here](https://dumps.wikimedia.org/enwiki/latest/)

### Run WikiIndexer

- Unzip WikiIndexer-1.0-SNAPSHOT.zip
- Edit the conf/config.properties file from the extracted zip and update:
  - wiki.file: Set the path of the Wikipedia XML Dump file
  - solr.base.url: Set the URL of the solr cluster you want to use to index wikipedia articles
- Start using bin/start.sh

### Run WikiSearcher

- Unzip WikiSearcher-0.0.1-SNAPSHOT.zip
- Edit the conf/application.properties file in from extracted zip and update:
  - solr.base.url: Point it to the same solr cluster you used in WikiIndexer.
  - server.port: Set it to the port of your choice
- Start using bin/start.sh
- Once started goto http://<IP>:<server port>. eg: http://localhost:8080 and you will get seeing the search home page.
  
  
# Supported Search Syntax:

1. Free text search: eg *tata steel*. This will search for articles mentioning either tata or steel. The documents mentioning both will be ranked higher.
2. Phrase search: eg *"tata steel"*. This will look for the exact phrase.
3. Boolean operators: eg *+tata -steel*. This will look for articles that contain the word *tata* but do not contain the work *steel*
  


