package km.lucene.applets.cluto;

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
public class DocFormatter implements Runnable {
    public static void main(String[] args) {
        DocFormatter docFormatter = new DocFormatter();
        docFormatter.run();

    }

    @Override
    public void run() {
        try {
            IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(Settings.THINDEX_PATH)));
            String ret = getVectorSpaceMatrix(reader, FieldName.CONTENT);
            IOHandler.write(Settings.ClutoSettings.DOCS_MAT, ret);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 12GB Barely helps for sparse matrix with all entries
     * 4GB is more than sufficient for sparse matrix with non-zero entries
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
        int max_col = Integer.MAX_VALUE;  // for debug
        m = Math.min(m, max_col);
        int n = reader.numDocs();
        int[][] mat = new int[n][2*m];  // n*m
        for(int i=0; i<n; i++) {
            for(int j=0; j<m; j++) {
                mat[i][2*j] = j+1;
                mat[i][2*j+1] = 0; // must be non-zero entries.
            }
        }

        int i=0;
        int cntNonZero = 0;
        te = terms.iterator(te);
        BytesRef t;
        while((t=te.next())!=null && i<max_col) {
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
        return String.format("%d %d %d\n", n, m, cntNonZero)+display(mat);
    }

    /**
     * Get all non-zero entries
     * for sparse matrix, it can significantly reduce the file size
     * @param mat
     * @return
     */
    String display(int[][] mat) {
        StringBuilder sb = new StringBuilder();  // faster than StringBuffer
        int n = mat.length;
        if(n==0)
            return null;
        int m = mat[0].length/2;
        for(int i=0; i<n; i++) {
            for(int j=0; j<m; j++) {
                if(mat[i][2*j+1]!=0) {
                    sb.append(mat[i][2*j])
                            .append(" ")
                            .append(mat[i][2*j+1]);
                    if(j<m-1)
                        sb.append(" ");
                }
            }
            sb.append("\n");
        }
        return sb.toString().trim();
    }
}
