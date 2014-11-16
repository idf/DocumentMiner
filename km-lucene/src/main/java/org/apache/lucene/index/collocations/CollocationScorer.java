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


/**
 * Records the total number of coincidences of two terms
 * @author MAHarwood
 * 
 * @author iprovalov (minor refactoring)
 * 
 */
public class CollocationScorer
{
    String term;
    String coincidentalTerm;

    // the number of shared references ie num docs "fast" coincides with "food".
    int coIncidenceDocCount = 0;

    private int termADocFreq;
    private int termBDocFreq;
    private long totalVocabulary;

    /**
     * @param coincidentalTerm
     *            the coincidental term eg "fast" (found next to
     *            term "food")
     * @param termADocFreq
     *            the document frequency of this term eg "fast"
     * @param termBDocFreq
     *            the document frequency of the other term eg "food"
     */
    public CollocationScorer(String term,String coincidentalTerm, int termADocFreq, int termBDocFreq) {
        this.term = term;
        this.coincidentalTerm = coincidentalTerm;
        this.termADocFreq = termADocFreq;
        this.termBDocFreq = termBDocFreq;
        this.totalVocabulary = 0;
    }

    public CollocationScorer(String term,String coincidentalTerm, int termADocFreq, int termBDocFreq, long totalVocabulary) {
        this(term, coincidentalTerm, termADocFreq, termBDocFreq);
        this.totalVocabulary = totalVocabulary;
    }

    public float getScore() {
        if(this.totalVocabulary==0)
            return this.getSimpleScore();
        else
            return this.getEntropyScore();
    }

    public void incCoIncidenceDocCount() {
        this.coIncidenceDocCount += 1;
    }

    private float getSimpleScore() {
        float overallIntersectionPercent = coIncidenceDocCount / (float) (termADocFreq + termBDocFreq);
        float termBIntersectionPercent = coIncidenceDocCount / (float) (termBDocFreq);

        //using just the termB intersection favours common words as
        // coincidents eg "new" food
        //      return termBIntersectionPercent;
        //using just the overall intersection favours rare words as
        // coincidents eg "scezchuan" food
        //        return overallIntersectionPercent;
        // so here we take an average of the two:
        return (termBIntersectionPercent + overallIntersectionPercent) / 2;
    }

    /**
     * Relative Entropy
     * score = P(x) log(P(x)/P(y))
     * P(x) = coincidenceDocCount / tf_x
     * P(y) = tf_y / |V|
     * @return
     */
    private float getEntropyScore() {
        double P_x = this.coIncidenceDocCount / (double) this.termADocFreq; // 1.0
        double P_y = this.termBDocFreq/ (double) this.totalVocabulary;
        double score = P_x * Math.log(P_x/P_y);
        return (float) score;  // 5.0588846?
    }





    public String getCoincidentalTerm() {
        return coincidentalTerm;
    }

    public void setCoincidentalTerm(String coincidentalTerm) {
        this.coincidentalTerm = coincidentalTerm;
    }

    @Override
    public String toString() {
        return ""+this.getScore();
    }
}
