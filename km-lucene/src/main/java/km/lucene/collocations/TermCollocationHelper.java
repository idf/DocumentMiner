package km.lucene.collocations;

import io.deepreader.java.commons.util.Sorter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.collocations.CollocationScorer;

import java.util.*;

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

    public HashMap<String, CollocationScorer> filterCollocationCount(HashMap<String, CollocationScorer> phraseTerms, int count) {
        HashMap<String, CollocationScorer> result = new HashMap<>();
        Iterator it = phraseTerms.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            if(((CollocationScorer) pair.getValue()).getCoIncidenceDocCount()>count) {
                result.put((String) pair.getKey(), (CollocationScorer) pair.getValue());
            }
        }
        return result;
    }

    public void sortScores(HashMap<String, CollocationScorer> phraseTerms) {
        TreeMap<String, CollocationScorer> sortedMap = Sorter.sortByValues(phraseTerms, new Sorter.ValueComparator<String, CollocationScorer>(phraseTerms) {
            @Override
            public int compare(String a, String b) {
                if (base.get(a).getScore()<base.get(b).getScore()) {
                    return 1;
                } else {
                    return -1;
                } // returning 0 would merge keys
            }
        });

        Iterator it = sortedMap.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            System.out.println(pair.getKey()+" = "+((CollocationScorer) pair.getValue()).getScore());
            // System.out.println(pair.getKey()+" = "+pair.getValue());  // details
        }
    }


    public boolean isTermTooPopularOrNotPopularEnough(Term term, float percent) {
        // check term is not too rare or frequent
        if (percent < minTermPopularity) {
            // System.out.println(term.text() + " not popular enough " + percent);
            return true;
        }
        if (percent > maxTermPopularity) {
            // System.out.println(term.text() + " too popular " + percent);
            return true;
        }
        return false;
    }
}
