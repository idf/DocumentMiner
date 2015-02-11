package km.lucene.entities;

import org.apache.lucene.index.collocations.CollocationScorer;

import java.util.Comparator;
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
}
