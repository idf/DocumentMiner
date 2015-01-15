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
        String ret = getVectorSpaceMatrix(reader, FieldName.CONTENT);
        IOHandler.write(Settings.ClutoSettings.DOCS_MAT, ret);
    }

    /**
     * TODO: streaming
     * 12GB Barely helps
     * VM Options -Xmx12g -d64
     *
     * The non-zero entries of each row are specified as a space-separated list of pairs. Each pair contains the column
     * number followed by the value for that particular column (i.e., feature)
     *
     * @param reader
     * @param fieldName
     * @return
     * @throws IOException
     */
    String getVectorSpaceMatrix(IndexReader reader, String fieldName) throws IOException {
        Fields fields = MultiFields.getFields(reader);
        Terms terms = fields.terms(fieldName);
        int m = 0;
        TermsEnum te = terms.iterator(null);
        while(te.next()!=null)
            m++;
        assert m>0;

        int n = reader.numDocs();
        int[][] mat = new int[n][2*m];  // n*m
        // TODO debug 1073741819
        for(int i=0; i<n; i++) {
            for(int j=0; j<m; j++) {
                mat[i][2*j] = 1;
                mat[i][2*j+1] = 0;
            }
        }

        int i=0;
        int cntNonZero = 0;
        te = terms.iterator(te);
        BytesRef t;
        while((t=te.next())!=null) {
            DocsEnum de = te.docs(null, null, DocsEnum.FLAG_FREQS);
            while(de.nextDoc()!=DocsEnum.NO_MORE_DOCS) {
                int docId = de.docID();
                int tf = de.freq();
                mat[docId][2*i] = i+1;
                mat[docId][2*i+1] = tf;
                if(tf!=0)
                    cntNonZero++;
            }
            i++;
        }
        return String.format("%d %d %d\n", n, m, cntNonZero)+Displayer.display(mat);
    }
}
