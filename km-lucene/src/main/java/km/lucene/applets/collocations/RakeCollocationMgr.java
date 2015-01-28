package km.lucene.applets.collocations;

import io.deepreader.java.commons.util.IOHandler;
import km.common.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rake4j.core.IndexWriter;
import rake4j.core.RakeAnalyzer;
import rake4j.core.index.Index;
import rake4j.core.model.Document;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

/**
 * User: Danyang
 * Date: 1/9/15
 * Time: 9:41 PM
 */
public class RakeCollocationMgr {
    Index index;
    Index preIndex;  // preprocessed index
    RakeAnalyzer rake;

    private Logger logger = LoggerFactory.getLogger(RakeCollocationMgr.class);

    public RakeCollocationMgr(String rakeIndexPath) throws URISyntaxException, IOException, ClassNotFoundException {
        this.index = (Index) IOHandler.deserialize(rakeIndexPath);
        logger.info("Deserialized data is from " + rakeIndexPath);
        this.rake = new RakeAnalyzer();
        this.rake.setMinWordsForPhrase(2);
    }

    private IndexWriter getIndexWriter(Index index) {
        return new IndexWriter(index, this.rake, Settings.RakeSettings.TOP_PERCENT);
    }

    public void renewPreIndex(List<String> docs) {
        this.preIndex = new Index();
        IndexWriter iw = this.getIndexWriter(this.preIndex);
        this.logger.trace("Renewed preIndex");
        docs.forEach(e -> iw.addDocument(new Document(e)));
    }

    public Document analyze(String text) {
        Document doc = new Document(text);
        this.rake.loadDocument(doc);
        this.rake.run();
        return doc;
    }
}