package org.apache.lucene.index.collocations;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import io.deepreader.java.commons.util.Displayer;

/**
 * Records the total number of coincidences of two terms
 * @author MAHarwood
 * 
 * @author iprovalov (minor refactoring)
 * 
 */
public class CollocationScorer {
    String term;
    String coincidentalTerm;

    // the number of shared references ie num docs "fast" coincides with "food".
    int coIncidenceDocCount = 0;

    private int termADocFreq;
    private int termBDocFreq;
    private long totalDocFreq;

    private long termBTermFreq;  // information for filter
    private long sampleNum;
    private long sampleBFreq;

    /**
     * @param coincidentalTerm
     *            the coincidental term eg "fast" (found next to
     *            term "food")
     * @param termADocFreq
     *            the document frequency of this term eg "fast"
     * @param termBDocFreq
     *            the document frequency of the other term eg "food"
     */
    public CollocationScorer(String term, String coincidentalTerm, int termADocFreq, int termBDocFreq, long termBTermFreq, long totalDocFreq) {
        this.term = term;
        this.coincidentalTerm = coincidentalTerm;
        this.termADocFreq = termADocFreq;
        this.termBDocFreq = termBDocFreq;
        this.termBTermFreq = termBTermFreq;
        this.totalDocFreq = totalDocFreq;  // using simple score
    }

    public CollocationScorer(String term, String coincidentalTerm, int termADocFreq, int termBDocFreq, long termBTermFreq, long totalDocFreq, long sampleNum, long sampleBFreq) {
        this(term, coincidentalTerm, termADocFreq, termBDocFreq, termBTermFreq, totalDocFreq);
        this.sampleNum = sampleNum;
        this.sampleBFreq = sampleBFreq;
    }

    public float getScore() {
        if(this.sampleNum==0)
            return this.getSimpleScore();
        else
            return this.getSimpleScore();
    }

    public int getCoIncidenceDocCount() {
        return this.coIncidenceDocCount;
    }
    public void incCoIncidenceDocCount() {
        this.coIncidenceDocCount += 1;
    }

    /**
     * using just the termB intersection favours common words as
     coincidents eg "new" food
     return termBIntersectionPercent;
     using just the overall intersection favours rare words as
     coincidents eg "scezchuan" food

     return overallIntersectionPercent;
     so here we take an average of the two:
     * @return
     */
    private float getSimpleScore() {
        float info = 1 / (float) (termADocFreq + termBDocFreq);
        float colloc = coIncidenceDocCount / (float) (termBDocFreq);
        return (colloc + info) / 2;
    }

    /**
     * Relative Entropy: Kullback–Leibler divergence
     * score = P(x) ln(P(x)/P(y))
     * P(x) = coincidenceDocCount / df_x
     * P(y) = df_y / DF
     *
     * score = P(x)ln P(x) - P(x)ln dfB + P(x)ln |V|
     * @return
     */
    private float getEntropyScore() {
        double P_x = this.coIncidenceDocCount / (double) this.sampleNum;
        double P_y = this.termBDocFreq/ (double) (this.totalDocFreq);  // considering termADoc frequency?
        double score = P_x * Math.log(P_x/P_y);
        return (float) score;

    }

    private float getRelativeEntropyScore() {
        double p_ab = this.coIncidenceDocCount / (double) this.sampleNum;
        double p_a = this.termADocFreq / (double) (this.totalDocFreq);
        double p_b = this.termBDocFreq / (double) (this.totalDocFreq);
        double score = pointWiseRelativeEntroy(p_ab, p_a*p_b);
        return (float) score;
    }

    private double pointWiseRelativeEntroy(double p, double q) {
        if(p==0 || q==0)
            return 0;
        return p*Math.log(p/q);
    }

    public double getCollocationness() {
//        return Math.log(this.coIncidenceDocCount * 1e6 /  this.sampleNum);
        double p = this.coIncidenceDocCount / (double) this.sampleNum;
        double q = this.sampleBFreq / (double) this.sampleNum * 1;
        return pointWiseRelativeEntroy(p, 1e-6);
    }

    public double getInformativeness() {
//        return  Math.log(((double) (this.totalDocFreq) / this.termADocFreq) *
//                ((double) (this.totalDocFreq) / this.termBDocFreq));
        double p = this.sampleBFreq / (double) this.sampleNum * 1;
        double q = this.termADocFreq / (double) this.totalDocFreq *
                this.termBDocFreq / (double) this. totalDocFreq;
        return pointWiseRelativeEntroy(p, q);

    }

    public CollocationScorer merge(CollocationScorer other) {
        if(this.coincidentalTerm.equals(other.coincidentalTerm))
            this.coIncidenceDocCount += other.coIncidenceDocCount;
        if(!this.term.equals(other.term))
            this.term = this.term+" "+other.term;  // better algorithm; does not affect scoring process; but affecting exclusion
        return this;
    }

    public String getCoincidentalTerm() {
        return coincidentalTerm;
    }

    public String getTerm() {
        return term;
    }

    public int getTermBDocFreq() {
        return termBDocFreq;
    }

    public void setCoincidentalTerm(String coincidentalTerm) {
        this.coincidentalTerm = coincidentalTerm;
    }


    @Override
    public String toString() {
        return Displayer.toString(this);
    }

    public long getTermBTermFreq() {
        return termBTermFreq;
    }

    public void setTermBTermFreq(long termBTermFreq) {
        this.termBTermFreq = termBTermFreq;
    }
}
