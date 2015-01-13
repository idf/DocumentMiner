package km.lucene.app.rake;

import io.deepreader.java.commons.util.Displayer;
import io.deepreader.java.commons.util.IOHandler;
import io.deepreader.java.commons.util.Timestamper;
import km.common.Settings;
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
public class RakeIndexer implements Runnable {
    String indexPath;
    Iterator<String> itr;

    public RakeIndexer(String indexPath, Iterator<String> itr) {
        this.indexPath = indexPath;
        this.itr = itr;
    }

    @Override
    public void run() {
        Timestamper timestamper = new Timestamper();
        Index index = new Index();
        Logger logger = LoggerFactory.getLogger(RakeIndexer.class);

        timestamper.start();
        RakeAnalyzer rake = null;
        try {
            rake = new RakeAnalyzer();
            rake.setMinWordsForPhrase(2);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        IndexWriter iw = new IndexWriter(index, rake , Settings.RakeSettings.TOP_PERCENT);

        int i = 1;
        int maxDocs = 1<<31-1; //1<<31-1;
        while (itr.hasNext() && i<maxDocs) { // debug
            String content = itr.next();
            Document doc = new Document(content);
            iw.addDocument(doc);
            logger.info(String.format("added document count %d", (i++)));
        }

        try {
            IOHandler.serialize(indexPath, index);
            logger.info("Serialized data is saved in " + indexPath);
        }
        catch (IOException e) {
            logger.error(Displayer.display(e));
        }

        logger.info(index.toString());
        timestamper.end();
    }
}
