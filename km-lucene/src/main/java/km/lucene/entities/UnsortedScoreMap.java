package km.lucene.entities;

import io.deepreader.java.commons.util.Transformer;
import org.apache.lucene.index.collocations.CollocationScorer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: Danyang
 * Date: 3/12/2015
 * Time: 13:30
 */
public class UnsortedScoreMap extends HashMap<String, CollocationScorer> {
    public UnsortedScoreMap() {
        super();
    }

    public UnsortedScoreMap(Map map) {
        super(map);
    }

    public void exclude(String t) {
        Transformer.removeByKey(this, e -> e.contains(t));
    }

    public void excludeMatchAll(List<String> t) {
        Transformer.removeByKey(this, e -> t.stream().allMatch(e::contains) && t.size()==e.split("\\s+").length);
    }

    public void excludeMatchAny(List<String> t) {
        Transformer.removeByKey(this, e -> t.stream().anyMatch(e::contains));
    }

    public UnsortedScoreMap merge(List<UnsortedScoreMap> lst) {
        UnsortedScoreMap ret = new UnsortedScoreMap(this);  // Cannot use TreeMap for ret, issue with put key
        for(UnsortedScoreMap map: lst) {
            for(Map.Entry<String, CollocationScorer> e: map.entrySet()) {
                if(ret.containsKey(e.getKey())) {
                    ret.put(e.getKey(), ret.get(e.getKey()).merge(e.getValue()));
                }
                else {
                    ret.put(e.getKey(), e.getValue());
                }
            }
        }
        return ret;
    }

    public static UnsortedScoreMap mergeMaps(List<UnsortedScoreMap> lst) {
        return  lst.get(0).merge(lst.subList(1, lst.size()));
    }
}