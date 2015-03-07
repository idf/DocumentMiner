package km.lucene.applets.rake;

import io.deepreader.java.commons.util.Displayer;
import io.deepreader.java.commons.util.IOHandler;
import io.deepreader.java.commons.util.Timestamper;
import km.common.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rake4j.core.IndexWriter;
import rake4j.core.RakeAnalyzer;
import rake4j.core.index.Index;
import rake4j.core.model.Document;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Iterator;

/**
 * User: Danyang
 * Date: 1/13/2015
 * Time: 16:16
 */

/**
 * Build Rake Index of phrases with tf df from a list of documents
 */
public class RakeIndexer implements Runnable {
    String indexPath;
    Iterator<String> docItr;

    public RakeIndexer(String indexPath, Iterator<String> docItr) {
        this.indexPath = indexPath;
        this.docItr = docItr;
    }

    @Override
    public void run() {
        Timestamper timestamper = new Timestamper();
        Index index = new Index();
        Logger logger = LoggerFactory.getLogger(RakeIndexer.class);

        timestamper.loudStart();
        RakeAnalyzer rake = null;
        try {
            rake = new RakeAnalyzer();
            rake.setMinWordsForPhrase(2);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        IndexWriter iw = new IndexWriter(index, rake , Config.settings.getRakeSettings().getTopPercent());

        int i = 1;
        int maxDocs = 1<<31-1; //1<<31-1;
        while (docItr.hasNext() && i<maxDocs) { // debug
            String content = docItr.next();
            Document doc = new Document(content);
            iw.addDocument(doc);
            logger.trace(String.format("added document count %d", (i++)));
        }

        try {
            IOHandler.serialize(indexPath, index);
            logger.info("Serialized data is saved in " + indexPath);
        }
        catch (IOException e) {
            logger.error(Displayer.display(e));
        }

        logger.trace(index.toString());
        logger.info("Rake Indexing Completed");
        timestamper.loudEnd();
    }
}
