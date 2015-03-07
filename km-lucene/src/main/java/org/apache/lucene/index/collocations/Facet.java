package org.apache.lucene.index.collocations;

import km.common.Config;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.util.Version;
import util.LuceneUtils;

import java.io.IOException;

/**
 * “Measuring programming progress by lines of code is like measuring aircraft building progress by weight.”
 * - Bill Gates
 * User: Danyang
 * Date: 9/26/14
 * Time: 12:28 AM
 */
public class Facet {
    private String indexDir = Config.settings.getThindexPath();
    private String collocsDir = Config.settings.getDataFolder()+"collocations/";

    public static void main(String[] args) throws IOException {
        Facet facet = new Facet();
        facet.extractCollocations();
    }
    
    private void extractCollocations() throws IOException, CorruptIndexException {
        IndexReader reader = LuceneUtils.reader(indexDir);
        CollocationExtractor collocationExtractor = new CollocationExtractor(reader);
        CollocationIndexer collocationIndexer = new CollocationIndexer(collocsDir, new StandardAnalyzer(Version.LUCENE_48));
        collocationExtractor.extract(collocationIndexer);
        collocationIndexer.close();
        reader.close();
    }
}
