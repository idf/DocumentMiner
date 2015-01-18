package util;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;

/**
 * User: Danyang
 * Date: 1/17/2015
 * Time: 17:31
 */
public class LuceneUtils {
    public static String getAllStringValues(Document doc, String fieldName, String delimiter) {
        StringBuilder sb = new StringBuilder();
        for (IndexableField field : doc.getFields(fieldName)) {
            sb.append(field.stringValue()).append(delimiter);
        }
        return sb.toString();
    }

    public static IndexReader getReader(String path) throws IOException {
        return DirectoryReader.open(FSDirectory.open(new File(path)));
    }
}
