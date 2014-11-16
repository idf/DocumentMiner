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

import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;

/**
 * This default implementation uses a stoplist to determine uninteresting words.
 * A more sophisticated version may use a dictionary eg WordNet to remove
 * non-nouns
 * 
 * @author MAHarwood
 * 
 * @author iprovalov (minor changes)
 */
public class TermFilter {
    public static final CharArraySet STOP_WORDS_SET = StopAnalyzer.ENGLISH_STOP_WORDS_SET;  // TODO sg filter

	String stops[] = { "a", "and", "are", "as", "at", "be", "but", "by", "for",
			"if", "in", "into", "is", "it", "no", "not", "of", "on", "or", "s",
			"such", "t", "that", "the", "their", "then", "there", "these",
			"they", "this", "to", "was", "will", "with", "how", "much",
			"always", "because", "much", "many", "every", "already", "only",
			"why", "didn", "you", "we", "she", "her", "from", "us", "me", "i",
			"our", "give", "had", "have", "has", "any", "some", "who", "what",
			"when", "on", "also", "can", "which", "where", "an", "your",
			"does", "through", "here", "more", "were", "so", "do", "each",
			"get", "he", "over", "under", "below", "it", "been", "about",
			"includes", "all", "after", "shall", "should", "take", "need",
			"know", "other", "see",
			"out",
			"don",// don=don't
			"please", "other", "would", "become", "became", "most", "just",
			"other", "seem", "gave", "just", "like", "make", "now", "take",
			"than", "those", "try", "won", // won't
			"would", "them", "welcome", "around", "best", "new" };

	static int DEFAULT_MIN_TERM_LENGTH = 3;
	int minTermLength = DEFAULT_MIN_TERM_LENGTH;

	public TermFilter() {
        /*
		for (int i = 0; i < stops.length; i++) {
            STOP_WORDS_SET.add(stops[i]);
		}
		*/
	}

	public boolean processTerm(String term) {
		if ((term.length() < minTermLength) || (STOP_WORDS_SET.contains(term))) {
			return false;
		}

		return true;
	}

}
