package km.mallet.postprocess;

import km.common.util.MathUtil;

public class TermWithWeight {

    private String term;
    private double weight;

    public TermWithWeight(String term, double weight) {
        this.term = term;
        this.weight = weight;
    }

    public void normalize(double factor) {
        weight = MathUtil.round(weight / factor, 4);
    }

    public String getTerm() {
        return term;
    }

    public double getWeight() {
        return weight;
    }
}
