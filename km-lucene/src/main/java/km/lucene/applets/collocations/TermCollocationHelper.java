package km.lucene.applets.collocations;

import km.lucene.entities.ScoreMap;
import org.apache.lucene.index.collocations.CollocationScorer;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * “Measuring programming progress by lines of code is like measuring aircraft building progress by weight.”
 * - Bill Gates
 * User: Danyang
 * Date: 12/12/14
 * Time: 11:33 AM
 */
public class TermCollocationHelper {
    static float DEFAULT_MIN_TERM_POPULARITY = 0.0044f;  // 10/2267, at least 10 documents out of 2267
    float minTermPopularity = DEFAULT_MIN_TERM_POPULARITY;
    static float DEFAULT_MAX_TERM_POPULARITY = 1f;
    float maxTermPopularity = DEFAULT_MAX_TERM_POPULARITY;

    public Map<String, CollocationScorer> filterByCollocationCount(Map<String, CollocationScorer> map, int count) {
        return map.entrySet()
                .parallelStream()
                .filter(e -> e.getValue().getCoIncidenceDocCount()>=count)
                .collect(Collectors.toConcurrentMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<String, CollocationScorer> filterByDocFreq(Map<String, CollocationScorer> map, int count) {
        return map.entrySet()
                .parallelStream()
                .filter(e -> e.getValue().getTermBDocFreq()>=count)
                .collect(Collectors.toConcurrentMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<String, CollocationScorer> filterByTermFreq(Map<String, CollocationScorer> map, int count) {
        return map.entrySet()
                .parallelStream()
                .filter(e -> e.getValue().getTermBTermFreq()>=count)
                .collect(Collectors.toConcurrentMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public void display(ScoreMap sortedMap) {
        Iterator it = sortedMap.entrySet().iterator();
        int i = 0;
        while(it.hasNext()) {
            Map.Entry<String, CollocationScorer> pair = (Map.Entry) it.next();
            i++;
            fineGrainedDisplay(i, pair);
        }
    }

    private void fineGrainedDisplay(int i, Map.Entry<String, CollocationScorer> pair) {
        System.out.println("# "+i);
        System.out.println(pair.getKey()+" = "+pair.getValue().getScore());
        System.out.println(pair.getKey()+" = "+pair.getValue());  // details
    }

    private void tabularDisplay(int i, Map.Entry<String, CollocationScorer> pair) {
        String s = String.format("%d. %s\t%f\t%f", i, pair.getKey(), pair.getValue().getInformativeness(), pair.getValue().getCollocationness());
        System.out.println(s);
    }

    public Comparator<Map.Entry<String, CollocationScorer>> getComparator() {
        return (e1, e2) -> Float.compare(e1.getValue().getScore(), e2.getValue().getScore());
    }

    public boolean isTooPopularOrNotPopularEnough(float percent) {
        // check term is not too rare or frequent
        if (percent < minTermPopularity) {
            return true;
        }
        if (percent > maxTermPopularity) {
            return true;
        }
        return false;
    }
}
