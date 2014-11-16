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
 * 
 */


import km.lucene.constants.FieldName;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;

public class DataIndexer {
	private IndexWriter writer;

	public DataIndexer(String indexDirName, Similarity sim, Analyzer analyzer) throws Exception {
		Directory dir = FSDirectory.open(new File(indexDirName));
        IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_48, analyzer);
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        iwc.setSimilarity(sim);
		writer = new IndexWriter(dir, iwc);
		// writer.setSimilarity(sim);
	}

	public void close() throws IOException {
		writer.close();
	}

	public void index(String dataDirName) throws Exception {
		File[] files = new File(dataDirName).listFiles();
        for (File file: files)
            System.out.println("Reading files: "+file.getAbsolutePath());
		if (files != null) {
			for (File f : files) {
				PlainFileParser plainFileParser = new PlainFileParser(f.getAbsolutePath(), f.getName());
				indexDoc(plainFileParser);
			}
		}

	}

	private int indexDoc(PlainFileParser plainFileParser) throws Exception {
		Document doc = new Document();
        String docId = plainFileParser.getDocId();
        // http://stackoverflow.com/questions/20424893/how-to-analyzed-field-for-using-apache-lucene-4-0
        // StringField is not analyzed, TextField is.
		doc.add(new StringField("id", docId, Field.Store.YES)); // Field.Index.NOT_ANALYZED
		// doc.add(new TextField("contents", plainFileParser.getDocument(), Field.Store.YES)); // no term vector in TextField
        // doc.add(new Field("contents", plainFileParser.getDocument(), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS));
        // To use TermsEnum.DocsAndPositionsEnum
        FieldType fieldType = new FieldType();
        fieldType.setStoreTermVectors(true);
        fieldType.setStoreTermVectorPositions(true);
        fieldType.setIndexed(true);
        doc.add(new Field(FieldName.CONTENT, plainFileParser.getDocument(), fieldType));
		writer.addDocument(doc);
		return writer.numDocs();
	}

}