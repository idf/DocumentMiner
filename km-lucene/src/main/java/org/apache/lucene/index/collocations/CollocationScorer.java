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


import util.Formatter;

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
    private long totalDocFreq;

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
        this.totalDocFreq = 0;
    }

    public CollocationScorer(String term,String coincidentalTerm, int termADocFreq, int termBDocFreq, long totalDocFreq) {
        this(term, coincidentalTerm, termADocFreq, termBDocFreq);
        this.totalDocFreq = totalDocFreq;
    }

    public float getScore() {
        if(this.totalDocFreq ==0)
            return this.getSimpleScore();
        else
            return this.getEntropyScore();
    }

    public synchronized void incCoIncidenceDocCount() {
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
        float overallIntersectionPercent = coIncidenceDocCount / (float) (termADocFreq + termBDocFreq);
        float termBIntersectionPercent = coIncidenceDocCount / (float) (termBDocFreq);
        return (termBIntersectionPercent + overallIntersectionPercent) / 2;
    }

    /**
     * Relative Entropy
     * score = P(x) ln(P(x)/P(y))
     * P(x) = coincidenceDocCount / df_x
     * P(y) = df_y / DF
     * @return
     */
    private float getEntropyScore() {
        double P_x = this.coIncidenceDocCount / (double) this.termADocFreq; // 1.0
        double P_y = this.termBDocFreq/ (double) this.totalDocFreq;
        double score = P_x * Math.log(P_x/P_y);
        return (float) score;
    }


    public String getCoincidentalTerm() {
        return coincidentalTerm;
    }

    @Override
    public String toString() {
        return Formatter.toString(this);
    }
}
