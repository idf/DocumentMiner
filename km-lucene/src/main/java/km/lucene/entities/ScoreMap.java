package km.lucene.entities;

import io.deepreader.java.commons.util.Transformer;
import org.apache.lucene.index.collocations.CollocationScorer;

import java.util.Comparator;
import java.util.List;
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
}
