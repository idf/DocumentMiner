package util;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;

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
}
