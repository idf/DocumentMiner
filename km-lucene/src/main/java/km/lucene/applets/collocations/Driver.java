package km.lucene.applets.collocations;

import io.deepreader.java.commons.util.IOHandler;
import io.deepreader.java.commons.util.Sorter;
import km.common.Settings;
import km.lucene.applets.cluto.ClutoWrapper;
import km.lucene.applets.cluto.DocFormatter;
import km.lucene.applets.rake.RakeIndexingFacet;
import org.apache.lucene.index.collocations.CollocationScorer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.IntStream;

/**
 * User: Danyang
 * Date: 1/28/2015
 * Time: 21:39
 */
public class Driver {
    final int DOC_NUM = 327454;
    final int BASE = (int) Math.sqrt(DOC_NUM);
    final int UPPER = (int) (Math.log(BASE)/Math.log(2));
    final int[] LST_K = IntStream.range(0, UPPER)
            .map(e -> (int) Math.pow(2, e)* BASE)
            .toArray();
    final boolean RE_RUN_CLUSTER = true;

    final String [] TERMS = {"ntu", "sce", "nbs", "nus", "soc", "smu", "computer", "hardware", "software", "degree", "school", "food"};
    final int TOP = 10;
    protected Logger logger = LoggerFactory.getLogger(Driver.class);

    public static void main(String[] args) throws Exception {
        Driver driver = new Driver();
        driver.postClusteredCollocation();
    }

    String cluster(String indexPath, int k, String suffix) throws IOException {
        String matPath = Settings.DriverSettings.ROOT_FOLDER+String.format("docs-%s.mat", suffix);
        String clusterPath = Settings.DriverSettings.ROOT_FOLDER+String.format("cluster-%s.txt", suffix);
        String rakeIndexPath = Settings.DriverSettings.ROOT_FOLDER+String.format("rakeIndex-%s.ser", suffix);
        if(RE_RUN_CLUSTER) {
            new DocFormatter(indexPath, matPath).run();
            new ClutoWrapper(matPath, clusterPath, k).run();
            new RakeIndexingFacet().clusteredIndexing(clusterPath, indexPath, rakeIndexPath);
        }
        return rakeIndexPath;
    }


    TreeMap<String, CollocationScorer> collocate(String indexPath, String rakeIndexPath, String term) throws Exception {
        TermCollocationExtractor tce = new TermCollocationExtractor("", indexPath, "", rakeIndexPath);
        TreeMap<String, CollocationScorer> sortedPhraseBScores = tce.search(term);
        logger.info("Collocation scoring completed");
        return Sorter.topEntries(sortedPhraseBScores, TOP,
                (e1, e2) -> Float.compare(e1.getValue().getScore(), e2.getValue().getScore()));

    }

    void writeToFile(List<TreeMap<String, CollocationScorer>> lst, String suffix) {
        String resultPath = Settings.DriverSettings.ROOT_FOLDER+String.format("result-%s.txt", suffix);

        StringBuilder sb = new StringBuilder();
        for(TreeMap<String, CollocationScorer> map: lst) {
            int i = 0;
            for(Map.Entry<String, CollocationScorer> pair: map.entrySet()) {
                if(i==0)
                    sb.append("# "+pair.getValue().getTerm()+"\n");
                sb.append("## "+(++i)+"\n");
                sb.append(pair.getKey() + " = " + pair.getValue().getScore() + "\n");
                sb.append(pair.getKey() + " = " + pair.getValue() + "\n");  // details
            }
        }
        IOHandler.write(resultPath, sb.toString());
    }

    public void postClusteredCollocation() throws Exception {
        final String INDEX_PATH = Settings.POSTINDEX_PATH;
        for(int k: LST_K) {
            final String SUFFIX = String.format("%s-%d", "post-clustered", k);
            String rakeIndexPath = cluster(INDEX_PATH, k, SUFFIX);
            List<TreeMap<String, CollocationScorer>> lst = new ArrayList<>();
            for(String term: TERMS) {
                lst.add(collocate(INDEX_PATH, rakeIndexPath, term));
            }
            writeToFile(lst, SUFFIX);
        }
    }

    public void threadCollocation() throws Exception {
        final String INDEX_PATH = Settings.THINDEX_PATH;
        final String SUFFIX = String.format("%s", "thread");
        String rakeIndexPath = new RakeIndexingFacet().threadedIndexing();
        List<TreeMap<String, CollocationScorer>> lst = new ArrayList<>();
        for(String term: TERMS) {
            lst.add(collocate(INDEX_PATH, rakeIndexPath, term));
        }
        writeToFile(lst, SUFFIX);
    }

    public void postCollection() throws Exception {
        final String INDEX_PATH = Settings.POSTINDEX_PATH;
        final String SUFFIX = String.format("%s", "post");
        String rakeIndexPath = new RakeIndexingFacet().basicIndexing();
        List<TreeMap<String, CollocationScorer>> lst = new ArrayList<>();
        for(String term: TERMS) {
            lst.add(collocate(INDEX_PATH, rakeIndexPath, term));
        }
        writeToFile(lst, SUFFIX);
    }
}
