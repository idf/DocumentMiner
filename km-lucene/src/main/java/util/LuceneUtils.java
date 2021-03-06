package util;

import io.deepreader.java.commons.util.ExceptionUtils;
import io.deepreader.java.commons.util.Generator;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;

/**
 * User: Danyang
 * Date: 1/17/2015
 * Time: 17:31
 */
public class LuceneUtils {
    /**
     * Get all the string values from the field of a document
     * @param doc
     * @param fieldName
     * @param delimiter
     * @return
     */
    public static String getAllStringValues(Document doc, String fieldName, String delimiter) {
        StringBuilder sb = new StringBuilder();
        for (IndexableField field : doc.getFields(fieldName)) {
            sb.append(field.stringValue()).append(delimiter);
        }
        return sb.toString();
    }

    /**
     * Get Index Reader From File System Directory
     * @param path
     * @return
     * @throws IOException
     */
    public static IndexReader reader(String path) throws IOException {
        return DirectoryReader.open(FSDirectory.open(new File(path)));
    }

    /**
     * Get Index Searcher from reader
     * @param reader Index reader
     * @return
     */
    public static IndexSearcher searcher(IndexReader reader) {
        return new IndexSearcher(reader);
    }

    /**
     * return index writer to add documents subsequently
     * @param indexPath
     * @param analyzer
     * @param ver
     * @return
     * @throws IOException
     */
    public static IndexWriter indexWriter(String indexPath, Analyzer analyzer, Version ver) throws IOException {
        Directory dir = FSDirectory.open(new File(indexPath));
        IndexWriterConfig iwc = new IndexWriterConfig(ver, analyzer);
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        IndexWriter indexWriter = new IndexWriter(dir, iwc);
        return indexWriter;
    }

    public static DocsAndPositionsEnum dpe(IndexReader r, Bits liveDocs, String field, BytesRef term) throws IOException {
        return MultiFields.getTermPositionsEnum(r, liveDocs, field, term);
    }

    public static Terms terms(IndexReader reader, String filedName) throws IOException {
        Fields fields = MultiFields.getFields(reader);
        Terms terms = fields.terms(filedName);
        return terms;
    }

    public static Generator<Terms, DocsEnum> iterateTermDocs(Terms term) {
        return new Generator<Terms, DocsEnum>(term) {
            @Override
            protected void run() throws InterruptedException {
                BytesRef f;
                try {
                    TermsEnum te = this.in.iterator(null);
                    while((f=te.next())!=null) {
                        DocsEnum de = te.docs(null, null, DocsEnum.FLAG_FREQS);
                        while(de.nextDoc()!=DocsEnum.NO_MORE_DOCS) {
                            yield(de);
                        }
                    }
                } catch (IOException e) {
                    ExceptionUtils.stifleCompileTime(e);
                }
            }
        };
    }
}
