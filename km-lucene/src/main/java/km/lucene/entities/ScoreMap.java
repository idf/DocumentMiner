package km.lucene.entities;

import io.deepreader.java.commons.util.Sorter;
import io.deepreader.java.commons.util.Transformer;
import org.apache.lucene.index.collocations.CollocationScorer;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * User: Danyang
 * Date: 2/11/2015
 * Time: 21:55
 */
public class ScoreMap extends TreeMap<String, CollocationScorer> {
    // constructors are not inherited
    public ScoreMap(TreeMap map) {
        super(map);
    }

    public ScoreMap(Comparator<? super String> cmp) {
        super(cmp);
    }

    public void exclude(String t) {
        Transformer.removeByKey(this, e -> e.contains(t));
    }

    public void exclude(List<String> t) {
        Transformer.removeByKey(this, e -> t.stream().allMatch(e::contains));
    }

    public static ScoreMap mergeMaps(List<ScoreMap> lst) {
        ScoreMap ret = lst.get(0);  // base
        return ret.merge(lst.subList(1, lst.size()));
    }

    public ScoreMap merge(List<ScoreMap> lst) {
        ScoreMap ret = new ScoreMap(this);
        for(ScoreMap map: lst) {
            for(Map.Entry<String, CollocationScorer> e: map.entrySet()) {
                if(ret.containsKey(e.getKey())) {
                    ret.put(e.getKey(), ret.get(e.getKey()).merge(e.getValue()));
                }
                else {
                    ret.put(e.getKey(), e.getValue());
                }
            }
        }
        return ScoreMap.sortScores(ret);
    }

    public static ScoreMap sortScores(Map<String, CollocationScorer> phraseTerms) {
        TreeMap sortedMap = Sorter.sortByValues(phraseTerms, new Sorter.ValueComparator<String, CollocationScorer>(phraseTerms) {
            @Override
            public int compare(String a, String b) {
                try {
                    if (base.get(a).getScore() < base.get(b).getScore()) return 1;
                    else if (a.equals(b)) return 0;
                    else return -1;
                } catch (NullPointerException e) {
                    return -1;
                }
            }
        });
        return new ScoreMap(sortedMap);
    }


}
