package km.lucene.applets.collocations;

import io.deepreader.java.commons.util.Sorter;
import km.common.Config;
import km.lucene.entities.ScoreMap;
import org.apache.lucene.queryparser.classic.ParseException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * User: Danyang
 * Date: 3/15/2015
 * Time: 19:22
 */
public class Evaluator {
    Driver driver = new Driver();
    final String [] TERMS = {"ntu"};
    String rakeIndexFormat = Config.settings.getDriverSettings().getRootFolder()+"rakeIndex-post-clustered-weighted-%d.ser";
    String indexPath = Config.settings.getPostindexPath();

    int baselineCluster = 2288;
    String baselineRakeIndex = String.format(rakeIndexFormat, baselineCluster);

    public static void main(String[] args) throws Exception {
        Evaluator evaluator = new Evaluator();
        evaluator.varyingPostThreadCluster();
    }

    void varyingWindowSize() throws Exception {
        System.out.println("Evaluation of Varying Window Sizes");

        int [] slopSizes = new int[] {2, 6, 18};
        TermCollocationExtractor tce = driver.getTCE(indexPath, baselineRakeIndex);
        for(int slopSize: slopSizes) {
            System.out.println(slopSize);
            tce.slopSize = slopSize;
            run(tce);
        }
    }

    void varyingTopDocumentK() throws Exception {
        System.out.println("Evaluation of Varying Top Document K");

        int [] topKs = new int[] {100, 300, 1000};
        TermCollocationExtractor tce = driver.getTCE(indexPath, baselineRakeIndex);
        for(int topK: topKs) {
            System.out.println(topK);
            tce.k = topK;
            run(tce);
        }
    }

    void varyingScoreAlgo() throws Exception {
        // need to modify the code directly
        TermCollocationExtractor tce = driver.getTCE(indexPath, baselineRakeIndex);
        run(tce);
    }

    void varyingPostThreadCluster() throws Exception {
        System.out.println("Evaluation of per post, per thread, and per cluster");

        TermCollocationExtractor tce;
        System.out.println("per post");
        tce = driver.getTCE(indexPath, Config.settings.getRakeSettings().getBasicIndexPath());
        run(tce);

        System.out.println("per thread");
        tce = driver.getTCE(indexPath, Config.settings.getRakeSettings().getThreadedIndexPath());
        run(tce);

        System.out.println("per cluster");
        tce = driver.getTCE(indexPath, baselineRakeIndex);
        run(tce);
    }

    void varyingClusterNumber() throws Exception {
        System.out.println("Evaluation of Varying Cluster Number");

        int [] clusterNums = new int[] {572, 2288, 4576};
        for(int clusterNum: clusterNums) {
            System.out.println(clusterNum);
            TermCollocationExtractor tce = driver.getTCE(indexPath, String.format(rakeIndexFormat, clusterNum));
            run(tce);
        }
    }

    private void run(TermCollocationExtractor tce) throws ParseException, IOException, URISyntaxException {
        int topCollocations = 20;
        for(String term: TERMS) {
            Map<String, ScoreMap> sorts = tce.search(term);
            System.out.println(tce.TERMS_STR);
            tce.helper.display(Sorter.topEntries(sorts.get(tce.TERMS_STR), topCollocations));
            System.out.println(tce.PHRASES_STR);
            tce.helper.display(Sorter.topEntries(sorts.get(tce.PHRASES_STR), topCollocations));
        }
        System.out.println("=================================");
    }
}
