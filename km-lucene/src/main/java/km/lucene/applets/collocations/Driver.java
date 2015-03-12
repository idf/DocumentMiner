package km.lucene.applets.collocations;

import io.deepreader.java.commons.util.IOHandler;
import io.deepreader.java.commons.util.Sorter;
import km.common.Config;
import km.lucene.applets.cluto.ClutoWrapper;
import km.lucene.applets.cluto.Index2ClutoFormatter;
import km.lucene.applets.rake.RakeIndexingFacet;
import km.lucene.entities.ScoreMap;
import km.lucene.entities.UnsortedScoreMap;
import org.apache.lucene.index.collocations.CollocationScorer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * User: Danyang
 * Date: 1/28/2015
 * Time: 21:39
 */
public class Driver {
    final int DOC_NUM = 327454;
    final int BASE = (int) Math.sqrt(DOC_NUM);  // 572
    final int UPPER = 5;  // 5th, 9152 would be 7 hours  // (int) (Math.log(BASE)/Math.log(2))
    final int[] LST_K = IntStream.range(0, UPPER)
            .map(e -> (int) Math.pow(2, e)* BASE)
            .toArray();
    final boolean RE_RUN_CLUSTER = false;
    final boolean RE_RUN_RAKE_INDEX = false;

    final String [] TERMS = {"ntu", "sce", "nbs", "nus", "soc", "smu", "computer", "hardware", "software", "degree", "school", "food"};
    final int TOP = 10;
    protected Logger logger = LoggerFactory.getLogger(Driver.class);

    public static void main(String[] args) throws Exception {
        Driver driver = new Driver();
        driver.postClusteredCollocation();
    }

    String cluster(String indexPath, int k, String suffix) throws IOException {
        String matPath = Config.settings.getDriverSettings().getRootFolder()+String.format("docs-%s.mat", suffix);
        String clusterPath = Config.settings.getDriverSettings().getRootFolder()+String.format("cluster-%s.txt", suffix);
        String rakeIndexPath = Config.settings.getDriverSettings().getRootFolder()+String.format("rakeIndex-%s.ser", suffix);
        if(RE_RUN_CLUSTER) {
            new Index2ClutoFormatter(indexPath, matPath).run();
            new ClutoWrapper(matPath, clusterPath, k).run();
        }
        if(RE_RUN_RAKE_INDEX) {
            new RakeIndexingFacet().clusteredIndexing(clusterPath, indexPath, rakeIndexPath);
        }
        return rakeIndexPath;
    }

    /**
     * Deserialize rake index is expensive
     * @param indexPath
     * @param rakeIndexPath
     * @return
     * @throws Exception
     */
    TermCollocationExtractor getTCE(String indexPath, String rakeIndexPath) throws Exception{
        return new TermCollocationExtractor("", indexPath, "", rakeIndexPath);
    }

    ScoreMap collocate(TermCollocationExtractor tce, String term) throws Exception {
        Map<String , UnsortedScoreMap> sorts = tce.search(term);
        logger.info("Collocation scoring completed");
        return Sorter.topEntries(ScoreMap.sortScores(sorts.get("phrases")), TOP,
                (e1, e2) -> Float.compare(e1.getValue().getScore(), e2.getValue().getScore()));

    }

    void writeToFile(List<ScoreMap> lst, String suffix) {
        String resultPath = Config.settings.getDriverSettings().getRootFolder()+String.format("result-%s.md", suffix);

        StringBuilder sb = new StringBuilder();
        for(ScoreMap map: lst) {
            int i = 0;
            for(Map.Entry<String, CollocationScorer> pair: map.entrySet()) {
                if(i==0)
                    sb.append("# "+pair.getValue().getTerm()+"\n");
                sb.append("## "+(++i)+"\n");
                sb.append(pair.getKey() + " = " + pair.getValue().getScore() + "\n");
                // sb.append(pair.getKey() + " = " + pair.getValue() + "\n");  // details
            }
        }
        IOHandler.write(resultPath, sb.toString());
    }

    public void postClusteredCollocation() throws Exception {
        final String INDEX_PATH = Config.settings.getPostindexPath();
        for(int k: LST_K) {
            final String SUFFIX = String.format("%s-%d", "post-clustered", k);
            String rakeIndexPath = cluster(INDEX_PATH, k, SUFFIX);
            List<ScoreMap> lst = new ArrayList<>();
            TermCollocationExtractor tce = getTCE(INDEX_PATH, rakeIndexPath);
            for(String term: TERMS) {
                lst.add(collocate(tce, term));
            }
            writeToFile(lst, SUFFIX);
        }
    }

    public void threadCollocation() throws Exception {
        final String INDEX_PATH = Config.settings.getThindexPath();
        final String SUFFIX = String.format("%s", "thread");
        String rakeIndexPath = new RakeIndexingFacet().threadedIndexing();
        List<ScoreMap> lst = new ArrayList<>();
        TermCollocationExtractor tce = getTCE(INDEX_PATH, rakeIndexPath);
        for(String term: TERMS) {
            lst.add(collocate(tce, term));
        }
        writeToFile(lst, SUFFIX);
    }

    public void postCollection() throws Exception {
        final String INDEX_PATH = Config.settings.getPostindexPath();
        final String SUFFIX = String.format("%s", "post");
        String rakeIndexPath = new RakeIndexingFacet().basicIndexing();
        List<ScoreMap> lst = new ArrayList<>();
        TermCollocationExtractor tce = getTCE(INDEX_PATH, rakeIndexPath);
        for(String term: TERMS) {
            lst.add(collocate(tce, term));
        }
        writeToFile(lst, SUFFIX);
    }
}
