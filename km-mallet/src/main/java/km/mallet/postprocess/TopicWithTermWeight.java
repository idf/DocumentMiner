package km.mallet.postprocess;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TopicWithTermWeight {

    private int id;
    private List<TermWithWeight> terms;

    public TopicWithTermWeight(int id, TreeMap<String, Double> sortedTerms) {
        this.id = id;
        terms = new ArrayList<>();
        addTerms(sortedTerms);
    }

    private void addTerms(TreeMap<String, Double> sortedTerms) {
        int i = 0;
        String combinedTerm = "";
        double combinedWeight = 0;
        double totalWeight = 0;
        for (Map.Entry<String, Double> entry : sortedTerms.entrySet()) {
            String term = entry.getKey();
            double weight = entry.getValue();
            totalWeight += weight;
            if (i < 20) {
                terms.add(new TermWithWeight(term, weight));
            } else if (i < 30) {
                combinedTerm = combinedTerm.isEmpty() ? term : combinedTerm + ", " + term;
                combinedWeight += weight;
            } else {
                combinedWeight += weight;
            }
            i++;
        }
        combinedTerm += "...";
        terms.add(new TermWithWeight(combinedTerm, combinedWeight));

        for (TermWithWeight term : terms) {
            term.normalize(totalWeight);
        }
    }

    public int getId() {
        return id;
    }

    public List<TermWithWeight> getTerms() {
        return terms;
    }
}
