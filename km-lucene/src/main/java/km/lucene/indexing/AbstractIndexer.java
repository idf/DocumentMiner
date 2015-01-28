package km.lucene.indexing;

import km.lucene.analysis.CustomAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: Danyang
 * Date: 1/28/2015
 * Time: 20:47
 */
public abstract class AbstractIndexer implements Runnable {
    protected Analyzer analyzer = new CustomAnalyzer(Version.LUCENE_48);
    protected Logger logger = LoggerFactory.getLogger(AbstractIndexer.class);
}
