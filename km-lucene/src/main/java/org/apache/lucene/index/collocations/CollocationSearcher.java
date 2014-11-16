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

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.collocations.constants.FieldName;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class is responsible for returning collocated terms
 * 
 * @author iprovalov
 * 
 */
public class CollocationSearcher {

	private int numberOfPhraseMatches;
	private IndexSearcher indexSearcher;

	public CollocationSearcher(String collocDir, int numberOfPhraseMatches) {
		super();
		this.numberOfPhraseMatches = numberOfPhraseMatches;

		try {
			Directory dir = FSDirectory.open(new File(collocDir));
			// IndexReader indexReader = IndexReader.open(dir, true);
            IndexReader indexReader = DirectoryReader.open(dir);
			this.indexSearcher = new IndexSearcher(indexReader);
		} catch (IOException e) {
			throw new RuntimeException("Failed to initialize collocation dictionary: " + e);
		}
	}

	public Map<String, Float> getBestMatch(String wordToBeMatched) throws Exception {
		QueryParser queryParser = new QueryParser(Version.LUCENE_48, FieldName.TERM,
				new StandardAnalyzer(Version.LUCENE_48));

		Map<String, Float> returnValue = getCoincidentalTerms(indexSearcher, queryParser, wordToBeMatched);
		return returnValue;
	}

	private Map<String, Float> getCoincidentalTerms(IndexSearcher searcher, QueryParser queryParser, String queryString) throws Exception {
		Query query = queryParser.parse(queryString);
		TopScoreDocCollector collector = TopScoreDocCollector.create(numberOfPhraseMatches, true);
		searcher.search(query, collector);
		TopDocs topDocs = collector.topDocs();
		int numResults = topDocs.totalHits;
		Map<String, Float> returnResults = new LinkedHashMap<String, Float>();
		for (int j = 0; j < numResults; j++) {
			if (j == numberOfPhraseMatches)
				break;
			int docID = topDocs.scoreDocs[j].doc;
			Document d = searcher.doc(docID);
			String coincidentalTerm = d.get(FieldName.COINCIDENTALTERM);
			Float score = topDocs.scoreDocs[j].score;
			returnResults.put(coincidentalTerm, score);
		}
		return returnResults;
	}
}
