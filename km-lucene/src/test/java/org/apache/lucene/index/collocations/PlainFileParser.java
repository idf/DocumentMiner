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


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class PlainFileParser {

	public String getDocId() {
		return docId;
	}

	public void setDocId(String docId) {
		this.docId = docId;
	}

	public void setDocument(String document) {
		this.document = document;
	}

	private String document;
	private String docId;

	public PlainFileParser(String fileName, String docId) {
		document = parseFile(fileName);
		this.docId = docId;

	}

	public static String parseFile(String fileName) {
		StringBuffer stringBuffer = new StringBuffer();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			String str;
			while ((str = reader.readLine()) != null) {
				stringBuffer.append(str + "\n");
			}
			reader.close();
		} catch (IOException e) {
			System.err.println("Couldn't read file: " + fileName);
			e.printStackTrace();
		}
		return stringBuffer.toString();
	}

	public String getDocument() {
		return document;
	}
}
