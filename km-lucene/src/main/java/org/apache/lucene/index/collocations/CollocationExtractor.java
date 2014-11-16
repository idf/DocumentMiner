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

import km.lucene.constants.FieldName;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.index.*;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.PriorityQueue;

import java.io.IOException;
import java.util.*;

/**
 * See:
 * http://issues.apache.org/jira/browse/LUCENE-474
 * 
 * @author iprovalov (some clean up, refactoring, unit tests, ant/maven, etc...)
 * 
 * Class used to find collocated terms in an index created with TermVector support
 * 
 * @author MAHarwood
 */
public class CollocationExtractor {
	static int DEFAULT_MAX_NUM_DOCS_TO_ANALYZE = 1200;
	static int maxNumDocsToAnalyze = DEFAULT_MAX_NUM_DOCS_TO_ANALYZE;
	String fieldName = FieldName.CONTENT;
	static float DEFAULT_MIN_TERM_POPULARITY = 0.0002f;
	float minTermPopularity = DEFAULT_MIN_TERM_POPULARITY;
	static float DEFAULT_MAX_TERM_POPULARITY = 1f;
	float maxTermPopularity = DEFAULT_MAX_TERM_POPULARITY;
	int numCollocatedTermsPerTerm = 20;
	IndexReader reader;
	int slopSize = 5;

	TermFilter filter = new TermFilter();

	public CollocationExtractor(IndexReader reader) {
		this.reader = reader;
	}


	public void extract(CollocationIndexer logger) throws IOException {
		// TermEnum te = reader.terms(new Term(fieldName, ""));
        // http://stackoverflow.com/questions/19208523/how-to-get-all-terms-in-index-directory-created-by-lucene-4-4-0
        Terms terms = MultiFields.getTerms(this.reader, this.fieldName);
        TermsEnum te = terms.iterator(null);

        BytesRef bytesRef = null;
		while (te.next()!=null) {  // iterate item A
            bytesRef = te.term();
            if(!StringUtils.isAlpha(bytesRef.utf8ToString())){
                continue;
            }
            // only process non-numbers
                /*
                if (!fieldName.equals(bytesRef.field())) {
                    break;
                }
                */
            processTerm(bytesRef, logger, slopSize);
		}
	}

    /**
     * Called for every term in the index
     * docsAndPositions, possible speed up by http://lucene.apache.org/core/4_2_0/core/org/apache/lucene/index/TermsEnum.html
     * http://stackoverflow.com/questions/15771843/get-word-position-in-document-with-lucene
     * Migration Guide: http://lucene.apache.org/core/4_8_1/MIGRATE.html
     * http://stackoverflow.com/questions/15370652/retrieving-all-term-positions-from-docsandpositionsenum
     * @param bytesRef
     * @param logger
     * @param slop
     * @throws IOException
     */
	void processTerm(BytesRef bytesRef, CollocationIndexer logger, int slop) throws IOException {
        Term term = new Term(this.fieldName, bytesRef);
		if (!filter.processTerm(term.text())) {
			return;
		}
        System.out.println("Processing term: "+term);
		// TermEnum te = reader.terms(term);
		// int numDocsForTerm = Math.min(te.docFreq(), maxNumDocsToAnalyze);
        int numDocsForTerm = Math.min(this.reader.docFreq(term), maxNumDocsToAnalyze);
		int totalNumDocs = reader.numDocs();
		float percent = (float) numDocsForTerm / (float) totalNumDocs;

		isTermTooPopularOrNotPopularEnough(term, percent);

		// get a list of all the docs with this term
        // Apache Lucene Migration Guide
		// TermDocs td = reader.termDocs(term);
        // get dpe in first hand
        DocsAndPositionsEnum dpe = MultiFields.getTermPositionsEnum(this.reader, null, this.fieldName, bytesRef);
		HashMap<String, CollocationScorer> phraseTerms = new HashMap<String, CollocationScorer>();
		int MAX_TERMS_PER_DOC = 100000;
		BitSet termPos = new BitSet(MAX_TERMS_PER_DOC);

		int numDocsAnalyzed = 0;
		// for all docs that contain this term
        int docSeq;
		while ((docSeq = dpe.nextDoc())!=DocsEnum.NO_MORE_DOCS) {
            int docId = dpe.docID();
            System.out.println("Processing docId: "+docId);
			numDocsAnalyzed++;
			if (numDocsAnalyzed > maxNumDocsToAnalyze) {
				break;
			}
			// get TermPositions for matching doc
            // TermPositionVector tpv = (TermPositionVector) reader.getTermFreqVector(docId, fieldName);
			// String[] terms_str = tpv.getTerms();
            Terms tv = this.reader.getTermVector(docId, this.fieldName);
            TermsEnum te = tv.iterator(null);
            // TODO refactor iteration
            
            List<String> terms_list = new ArrayList<>();
            while(te.next()!=null) {
                terms_list.add(te.term().utf8ToString());
            }
            String[] terms_str = terms_list.toArray(new String[terms_list.size()]);
            // System.out.println("terms_str: "+Arrays.toString(terms_str));
			termPos.clear();
			int index = recordAllPositionsOfTheTermInCurrentDocumentBitset(docSeq, term, termPos, tv, terms_str);

			// now look at all OTHER terms_str in this doc and see if they are
			// positioned in a pre-defined sized window around the current term
            /*
			for (int j = 0; j < terms_str.length; j++) {
				if (j == index) { // (item A)
					continue;
				}
				if (!filter.processTerm(terms_str[j])) {
					continue;
				}
                if (!StringUtils.isAlpha(terms_str[j])) {
                    continue;
                }
                // sequential code
				boolean matchFound = false;
				for (int k = 0; ((k < dpe.freq()) && (!matchFound)); k++) {
                    try {
                        // inefficient
                        // iterate through all other items (item B)
                        Integer position = dpe.nextPosition();
                        Integer startpos = Math.max(0, position - slop);
                        Integer endpos = position + slop;
                        matchFound = populateHashMapWithPhraseTerms(term,
                                numDocsForTerm, totalNumDocs, phraseTerms, termPos,
                                terms_str, j, matchFound, startpos, endpos);
                    }
                    catch (ArrayIndexOutOfBoundsException e) {
                        e.printStackTrace();
                        break;
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }

				}
			}
			*/

			///
            boolean[] matchFound = new boolean[terms_str.length]; // single match is sufficient, no duplicate process
            for(int j=0; j < matchFound.length; j++)
                matchFound[j] = false;

            for (int k = 0; (k < dpe.freq()); k++) {
                Integer position = dpe.nextPosition();
                Integer startpos = Math.max(0, position - slop);
                Integer endpos = position + slop;
                for (int j = 0; j < terms_str.length && !matchFound[j]; j++) {
                    if (j == index) { // (item A)
                        continue;
                    }
                    if (!filter.processTerm(terms_str[j])) {
                        continue;
                    }
                    if (!StringUtils.isAlpha(terms_str[j])) {
                        continue;
                    }
                    // inefficient
                    // iterate through all other items (item B)
                    populateHashMapWithPhraseTerms(
                            term,
                            numDocsForTerm,
                            totalNumDocs,
                            phraseTerms,
                            termPos,
                            terms_str, j,
                            matchFound,
                            startpos,
                            endpos);
                }

            }
		}// end docs loop

		sortTopTermsAndAddToCollocationsIndexForThisTerm(logger, phraseTerms);
	}

	private void populateHashMapWithPhraseTerms(Term term,
			int numDocsForTerm,
            int totalNumDocs,
            HashMap<String, CollocationScorer> phraseTerms,
			BitSet termPos,
            String[] terms,
            int j,
            boolean[] matchFound,
			int startpos,
            int endpos
    ) throws IOException {
		for (int prevpos = startpos; (prevpos <= endpos) && (!matchFound[j]); prevpos++) {
			if (termPos.get(prevpos)) {
				// Add term to hashmap containing co-occurrence
				// counts for this term
				CollocationScorer pt = (CollocationScorer) phraseTerms.get(terms[j]);
				if (pt == null) {
					// TermEnum otherTe = reader.terms(new Term(fieldName, terms[j]));

                    Term otherTe = new Term(this.fieldName, terms[j]);
					int numDocsForOtherTerm = Math.min(this.reader.docFreq(otherTe), maxNumDocsToAnalyze);

					float otherPercent = (float) numDocsForOtherTerm / (float) totalNumDocs;


					// check other term is not too rare or frequent
					if (otherPercent < minTermPopularity) {
						System.out.println(term.text() + " not popular enough " + otherPercent);
                        matchFound[j] = true;
						continue;
					}
					if (otherPercent > maxTermPopularity) {
						System.out.println(term.text() + " too popular " + otherPercent);
                        matchFound[j] = true;
						continue;
					}
                    // public CollocationScorer(String term, String coincidentalTerm, int termADocFreq, int termBDocFreq)
					pt = new CollocationScorer(term.text(), terms[j], numDocsForOtherTerm, numDocsForTerm);
					phraseTerms.put(pt.coincidentalTerm, pt);
				}
				pt.incCoIncidenceDocCount();
                matchFound[j] = true;
			}
		}
	}

	private int recordAllPositionsOfTheTermInCurrentDocumentBitset(int docSeq, Term term, BitSet termPos, Terms tv, String[] terms) throws IOException {
		// first record all of the positions of the term in a bitset which represents terms in the current doc.
		int index = Arrays.binarySearch(terms, term.text());
		if (index >= 0) {  // found
            // Bits liveDocs = MultiFields.getLiveDocs(this.reader);
            // int[] pos = tpv.getTermPositions(index);
            DocsAndPositionsEnum dpe = MultiFields.getTermPositionsEnum(this.reader, null, this.fieldName, new BytesRef(terms[index]));
            dpe.advance(docSeq);
			// remember all positions of the term in this doc
			for (int j = 0; j < dpe.freq(); j++) {
				termPos.set(dpe.nextPosition());
			}
		}
		return index;
	}

	private void sortTopTermsAndAddToCollocationsIndexForThisTerm(
			CollocationIndexer collocationIndexer, HashMap<String, CollocationScorer> phraseTerms) throws IOException {
		TopTerms topTerms = new TopTerms(numCollocatedTermsPerTerm);
		for (CollocationScorer pt: phraseTerms.values()) {
			topTerms.insertWithOverflow(pt);
		}
		CollocationScorer[] tops = new CollocationScorer[topTerms.size()];
		int tp = tops.length - 1;
		while (topTerms.size() > 0) {
			CollocationScorer top = (CollocationScorer) topTerms.pop();
			tops[tp--] = top;
		}
		for (int j = 0; j < tops.length; j++) {
			collocationIndexer.indexCollocation(tops[j]);
		}
	}

	private void isTermTooPopularOrNotPopularEnough(Term term, float percent) {
		// check term is not too rare or frequent
		if (percent < minTermPopularity) {
			System.out.println(term.text() + " not popular enough " + percent);
			return;
		}
		if (percent > maxTermPopularity) {
			System.out.println(term.text() + " too popular " + percent);
			return;
		}
	}

	static class TopTerms extends PriorityQueue<Object> {
		public TopTerms(int size) {
            super(size);
		}

		protected boolean lessThan(Object a, Object b) {
			CollocationScorer pta = (CollocationScorer) a;
			CollocationScorer ptb = (CollocationScorer) b;
			return pta.getScore() < ptb.getScore();
		}
	}

	public static int getMaxNumDocsToAnalyze() {
		return maxNumDocsToAnalyze;
	}

	public static void setMaxNumDocsToAnalyze(int maxNumDocsToAnalyze) {
		CollocationExtractor.maxNumDocsToAnalyze = maxNumDocsToAnalyze;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public float getMaxTermPopularity() {
		return maxTermPopularity;
	}

	public void setMaxTermPopularity(float maxTermPopularity) {
		this.maxTermPopularity = maxTermPopularity;
	}

	public float getMinTermPopularity() {
		return minTermPopularity;
	}

	public void setMinTermPopularity(float minTermPopularity) {
		this.minTermPopularity = minTermPopularity;
	}

	public int getNumCollocatedTermsPerTerm() {
		return numCollocatedTermsPerTerm;
	}

	public void setNumCollocatedTermsPerTerm(int numCollocatedTermsPerTerm) {
		this.numCollocatedTermsPerTerm = numCollocatedTermsPerTerm;
	}

	public int getSlopSize() {
		return slopSize;
	}

	public void setSlopSize(int slopSize) {
		this.slopSize = slopSize;
	}
}
