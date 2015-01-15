package km.lucene.app.cluto;

import io.deepreader.java.commons.util.Displayer;
import io.deepreader.java.commons.util.IOHandler;
import km.common.Settings;
import km.lucene.constants.FieldName;
import org.apache.lucene.index.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.File;
import java.io.IOException;

/**
 * User: Danyang
 * Date: 1/15/2015
 * Time: 15:20
 *
 * Similar to Stack Overflow: Generate Term-Document matrix using Lucene 4.4
 */
public class DocFormatter {
    public static void main(String[] args) throws IOException {
        DocFormatter docFormatter = new DocFormatter();
        docFormatter.index2matrix();

    }
    public void index2matrix() throws IOException {
        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(Settings.THINDEX_PATH)));
        int[][] mat = getVectorSpaceMatrix(reader, FieldName.CONTENT);
        IOHandler.write(Settings.ClutoSettings.DOCS_MAT, Displayer.display(mat));
    }

    /**
     * Future: streaming
     * @param reader
     * @param fieldName
     * @return
     * @throws IOException
     */
    private int[][] getVectorSpaceMatrix(IndexReader reader, String fieldName) throws IOException {
        Fields fields = MultiFields.getFields(reader);
        Terms terms = fields.terms(fieldName);
        int m = 0;
        TermsEnum te = terms.iterator(null);
        while(te.next()!=null)
            m++;
        int n = reader.numDocs();
        int[][] mat = new int[n][m];  // n*m

        int i=0;
        te = terms.iterator(te);
        BytesRef t;
        while((t=te.next())!=null) {
            DocsEnum de = te.docs(null, null, DocsEnum.FLAG_FREQS);
            while(de.nextDoc()!=DocsEnum.NO_MORE_DOCS) {
                int docId = de.docID();
                int tf = de.freq();
                mat[docId][i] = tf;
            }
            i++;
        }
        return mat;
    }
}
