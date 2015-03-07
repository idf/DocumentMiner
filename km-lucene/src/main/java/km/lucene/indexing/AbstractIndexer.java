package km.lucene.indexing;

import km.common.Config;
import km.lucene.analysis.CustomAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: Danyang
 * Date: 1/28/2015
 * Time: 20:47
 */
public abstract class AbstractIndexer implements Runnable {
    protected Logger logger = LoggerFactory.getLogger(AbstractIndexer.class);

    Analyzer analyzer = new CustomAnalyzer(Version.LUCENE_48);
    final static FacetsConfig config = new FacetsConfig();
    String postPath;
    String indexPath;
    String taxoPath;

    public AbstractIndexer() {
        this(Config.settings.getSortedPostsPath(),
                Config.settings.getPostindexPath(),
                Config.settings.getTaxoindexPath()
        );
    }

    public AbstractIndexer(String postPath, String indexPath, String taxoPath) {
        this.postPath = postPath;
        this.indexPath = indexPath;
        this.taxoPath = taxoPath;
    }
}
