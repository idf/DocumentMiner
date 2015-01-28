package km.lucene.applets.collocations;

import km.common.Settings;
import km.lucene.applets.cluto.ClutoWrapper;
import km.lucene.applets.cluto.DocFormatter;
import km.lucene.applets.rake.RakeIndexingFacet;

import java.io.IOException;
import java.util.stream.IntStream;

/**
 * User: Danyang
 * Date: 1/28/2015
 * Time: 21:39
 */
public class Driver {
    int base = 2267;
    int[] ks = IntStream.range(1, 11).map(e -> e * base).toArray();
    String [] terms = {"ntu", "sce", "nbs", "nus", "soc", "smu"};

    public String cluster(String indexPath, int k, String fileBaseName) throws IOException {
        String matPath = "";  // TODO
        String clusterPath = "";
        String rakeIndexPath = "";
        new DocFormatter(indexPath, matPath).run();
        new ClutoWrapper(matPath, clusterPath).run();
        new RakeIndexingFacet().clusteredIndexing(clusterPath, indexPath, rakeIndexPath);
        return rakeIndexPath;
    }


    public void collocate(String indexPath, String rakeIndexPath, String term) throws Exception {
        TermCollocationExtractor tce = new TermCollocationExtractor("", "", "", "");  // TODO
        tce.search(term);
    }

    public void postBasedCollocation() throws Exception {
        String indexPath = Settings.POSTINDEX_PATH;
        for(int k: ks) {
            String rakeIndexPath = cluster(indexPath, k, "post-based");
            for(String term: terms) {
                collocate(indexPath, rakeIndexPath, term);
            }
        }
    }

}
