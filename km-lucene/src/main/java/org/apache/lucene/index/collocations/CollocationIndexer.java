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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.collocations.constants.FieldName;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;

/**
 * 
 * @author MAHarwood
 * @author iprovalov (minor changes)
 */
public class CollocationIndexer {
    public static final float SCALE = 100f;
    FSDirectory dir;
	IndexWriter writer;

	public CollocationIndexer(String newIndexDir, Analyzer analyzer)
			throws IOException {
		this.dir = FSDirectory.open(new File(newIndexDir));
        IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_48, analyzer);
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);  // otherwise increasingly more index files
        this.writer = new IndexWriter(this.dir, iwc);
		// writer = new IndexWriter(dir, analyzer, true, MaxFieldLength.UNLIMITED);
		// setMaxBufferedDocs(10000);
	}

    /*
	public void setMaxBufferedDocs(int numDocs) {
		writer.setMaxBufferedDocs(numDocs);
	}

	public int getMaxBufferedDocs() {
		return writer.getMaxBufferedDocs();
	}
	*/

	public void indexCollocation(CollocationScorer collocationScorer) throws IOException {
		Document doc = new Document();
        Field termField = new TextField(FieldName.TERM, collocationScorer.term, Field.Store.YES);  // Field.Index.ANALYZED
        // Field termField = new Field("term", collocationScorer.term, Field.Store.YES, Field.Index.ANALYZED);  //
        termField.setBoost(collocationScorer.getScore() * SCALE);
        Field coincidentalField = new TextField(FieldName.COINCIDENTALTERM, collocationScorer.coincidentalTerm, Field.Store.YES); // Field.Index.ANALYZED
        // Field coincidentalField = new Field("coincidentalTerm", collocationScorer.coincidentalTerm, Field.Store.YES, Field.Index.ANALYZED);
        coincidentalField.setBoost(collocationScorer.getScore() * SCALE);  // no use
		doc.add(termField);
		doc.add(coincidentalField);
		// doc.setBoost(collocationScorer.getScore() * 100f);
		writer.addDocument(doc);
		System.out.println(collocationScorer.term + ":" + collocationScorer.coincidentalTerm + ":" + collocationScorer.getScore() * SCALE);
	}

	public void close() throws IOException {
		// writer.optimize();
		writer.close();
		dir.close();

	}
}
