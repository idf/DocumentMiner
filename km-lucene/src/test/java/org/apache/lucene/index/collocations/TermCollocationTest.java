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
 * @author iprovalov
 */

import km.lucene.constants.FieldName;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;


public class TermCollocationTest {
	private String indexDir = "km-lucene/data/index";
	private String dataDir = "km-lucene/data/content";
	private String collocsDir = "km-lucene/data/collocs";

	@Before
	public void setup() throws Exception {
		indexContent();
		extractCollocations();
	}
    @Test
    public void stub() throws Exception {

    }
	@Test
	public void testIndexing() throws Exception {
        IndexReader indexReader = DirectoryReader.open(FSDirectory.open(new File(indexDir)));
		IndexSearcher searcher = new IndexSearcher(indexReader);
		QueryParser parser = new QueryParser(Version.LUCENE_48, FieldName.CONTENT, new SimpleAnalyzer(Version.LUCENE_48));
		Query query = parser.parse("gold	silver	truck");
		TopDocs hits = searcher.search(query, 10);
		ScoreDoc match = hits.scoreDocs[0];
		Document doc = searcher.doc(match.doc);
		assertEquals("D2", doc.get("id"));
	}

	@Test
	public void testTermCollocationFinding() throws Exception {
		Map<String, Float> expectedResults = new LinkedHashMap<String, Float>();
        /*
		expectedResults.put("shipment", 162.58849f);
		expectedResults.put("fire", 101.617805f);
		expectedResults.put("damaged", 101.617805f);
		expectedResults.put("arrived", 81.29424f);
		expectedResults.put("truck", 81.29424f);
		*/
        expectedResults.put("shipment", 171.13449f);
        expectedResults.put("fire", 106.95906f);
        expectedResults.put("damaged", 106.95906f);
        expectedResults.put("arrived", 85.567245f);
        expectedResults.put("truck", 85.567245f);

		CollocationSearcher collocationsSearcher = new CollocationSearcher(collocsDir, 10);
		Map<String, Float> matches = collocationsSearcher.getBestMatch("gold");
		
		assertEquals(expectedResults, matches);
	}

	private void extractCollocations() throws IOException, CorruptIndexException {
		Directory dir = FSDirectory.open(new File(indexDir));
		IndexReader reader = DirectoryReader.open(dir);
		CollocationExtractor collocationExtractor = new CollocationExtractor(reader);
		CollocationIndexer collocationIndexer = new CollocationIndexer(collocsDir, new StandardAnalyzer(Version.LUCENE_48));
		collocationExtractor.extract(collocationIndexer);
		collocationIndexer.close();
		reader.close();
	}

	private void indexContent() throws Exception, IOException {
		DataIndexer indexer = new DataIndexer(indexDir, new DefaultSimilarity(), new StandardAnalyzer(Version.LUCENE_48));
		indexer.index(dataDir);
		indexer.close();
	}

}
