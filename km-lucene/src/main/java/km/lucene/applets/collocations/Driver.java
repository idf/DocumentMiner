package km.lucene.applets.collocations;

import io.deepreader.java.commons.util.IOHandler;
import io.deepreader.java.commons.util.Sorter;
import km.common.Settings;
import km.lucene.applets.cluto.ClutoWrapper;
import km.lucene.applets.cluto.DocFormatter;
import km.lucene.applets.rake.RakeIndexingFacet;
import org.apache.lucene.index.collocations.CollocationScorer;

import java.io.IOException;
import java.util.*;
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
    final int[] LST_K = IntStream.range(0, 1)
            .map(e -> (int) Math.pow(2, e)* BASE)
            .toArray();

    final String [] TERMS = {"ntu", "sce", "nbs", "nus", "soc", "smu"};

    public static void main(String[] args) throws Exception {
        new Driver().postClusteredCollocation();
    }

    String cluster(String indexPath, int k, String suffix) throws IOException {
        String matPath = Settings.DriverSettings.ROOT_FOLDER+String.format("docs-%s.mat", suffix);
        String clusterPath = Settings.DriverSettings.ROOT_FOLDER+String.format("cluster-%s.txt", suffix);
        String rakeIndexPath = Settings.DriverSettings.ROOT_FOLDER+String.format("rakeIndex-%s.ser", suffix);

        new DocFormatter(indexPath, matPath).run();
        new ClutoWrapper(matPath, clusterPath, k).run();
        new RakeIndexingFacet().clusteredIndexing(clusterPath, indexPath, rakeIndexPath);
        return rakeIndexPath;
    }


    TreeMap<String, CollocationScorer> collocate(String indexPath, String rakeIndexPath, String term) throws Exception {
        TermCollocationExtractor tce = new TermCollocationExtractor("", indexPath, "", rakeIndexPath);
        TreeMap<String, CollocationScorer> sortedPhraseBScores = tce.search(term);
        return Sorter.topEntries(sortedPhraseBScores, 10,
                (e1, e2) -> Float.compare(e1.getValue().getScore(), e2.getValue().getScore()));

    }

    void writeToFile(List<TreeMap<String, CollocationScorer>> lst, String suffix) {
        String resultPath = Settings.DriverSettings.ROOT_FOLDER+String.format("result-%s.txt", suffix);

        StringBuilder sb = new StringBuilder();
        for(TreeMap<String, CollocationScorer> map: lst) {
            int i = 0;
            for(Map.Entry<String, CollocationScorer> pair: map.entrySet()) {
                sb.append("# "+(++i)+"\n");
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
        new RakeIndexingFacet().threadedIndexing();  // TODO Settings parameter
        List<TreeMap<String, CollocationScorer>> lst = new ArrayList<>();
        for(String term: TERMS) {
            lst.add(collocate(INDEX_PATH, "", term));  // TODO
        }
        writeToFile(lst, SUFFIX);
    }

    public void postCollection() throws Exception {
        final String INDEX_PATH = Settings.POSTINDEX_PATH;
        final String SUFFIX = String.format("%s", "post");
        new RakeIndexingFacet().basicIndexing();  // TODO Settings parameter
        List<TreeMap<String, CollocationScorer>> lst = new ArrayList<>();
        for(String term: TERMS) {
            lst.add(collocate(INDEX_PATH, "", term));  // TODO
        }
        writeToFile(lst, SUFFIX);
    }

}