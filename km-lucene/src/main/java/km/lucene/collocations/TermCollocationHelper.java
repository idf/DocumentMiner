package km.lucene.collocations;

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
    static float DEFAULT_MIN_TERM_POPULARITY = 0.0002f;
    float minTermPopularity = DEFAULT_MIN_TERM_POPULARITY;
    static float DEFAULT_MAX_TERM_POPULARITY = 1f;
    float maxTermPopularity = DEFAULT_MAX_TERM_POPULARITY;

    public void sortPhraseTerms(HashMap<String, CollocationScorer> phraseTerms) {
        class ValueComparator implements Comparator<String> {
            Map<String, CollocationScorer> base;
            public ValueComparator(Map<String, CollocationScorer> base) {
                this.base = base;
            }

            // Note: this comparator imposes orderings that are inconsistent with equals.
            @Override
            public int compare(String a, String b) {
                if (base.get(a).getScore() < base.get(b).getScore()) {
                    return 1;
                } else {
                    return -1;
                } // returning 0 would merge keys
            }
        }
        ValueComparator bvc = new ValueComparator(phraseTerms);
        TreeMap<String,CollocationScorer> sortedMap = new TreeMap<>(bvc);
        sortedMap.putAll(phraseTerms);

        Iterator it = sortedMap.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            System.out.println(pair.getKey()+" = "+((CollocationScorer) pair.getValue()).getScore());
            System.out.println(pair.getKey()+" = "+pair.getValue());  // details
        }
    }


    public boolean isTermTooPopularOrNotPopularEnough(Term term, float percent) {
        // check term is not too rare or frequent
        if (percent < minTermPopularity) {
            System.out.println(term.text() + " not popular enough " + percent);
            return true;
        }
        if (percent > maxTermPopularity) {
            System.out.println(term.text() + " too popular " + percent);
            return true;
        }
        return false;
    }
}
