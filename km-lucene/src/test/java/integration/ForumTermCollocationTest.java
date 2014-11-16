package integration;

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

import km.common.Setting;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.collocations.CollocationExtractor;
import org.apache.lucene.index.collocations.CollocationIndexer;
import org.apache.lucene.index.collocations.CollocationSearcher;
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


public class ForumTermCollocationTest {
	private String indexDir = Setting.THINDEX_PATH;
	private String collocsDir = Setting.DATA_FOLDER+"collocations/";

	@Before
	public void setup() throws Exception {
		// extractCollocations();
	}
    @Test
    public void stub() throws Exception {

    }

	// @Test
	public void testTermCollocationFinding() throws Exception {
		Map<String, Float> expectedResults = new LinkedHashMap<String, Float>();
		expectedResults.put("shipment", 162.58849f);
		expectedResults.put("fire", 101.617805f);
		expectedResults.put("damaged", 101.617805f);
		expectedResults.put("arrived", 81.29424f);
		expectedResults.put("truck", 81.29424f);

		CollocationSearcher collocationsSearcher = new CollocationSearcher(collocsDir, 10);
		Map<String, Float> matches = collocationsSearcher.getBestMatch("ntu");
		
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


}
