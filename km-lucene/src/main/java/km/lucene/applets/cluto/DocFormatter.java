package km.lucene.applets.cluto;

import io.deepreader.java.commons.util.IOHandler;
import km.common.Settings;
import km.lucene.constants.FieldName;
import org.apache.lucene.index.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User: Danyang
 * Date: 1/15/2015
 * Time: 15:20
 *
 * Similar to Stack Overflow: Generate Term-Document matrix using Lucene 4.4
 */
public class DocFormatter implements Runnable {
    private String indexPath;
    private String matOutputPath;

    public static void main(String[] args) {
        new DocFormatter(Settings.THINDEX_PATH, Settings.ClutoSettings.DOCS_MAT).run();
    }

    public DocFormatter(String indexPath, String matOutputPath) {
        this.indexPath = indexPath;
        this.matOutputPath = matOutputPath;
    }

    @Override
    public void run() {
        try {
            IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexPath)));
            String ret = getVectorSpaceMatrix(reader, FieldName.CONTENT);
            IOHandler.write(matOutputPath,ret);
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
     * memory problem when n is set to the number of posts rather than thread.
     * use linked list as sparse matrix rather than int[n][2*m]
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
        List<List<Integer>> docs = new ArrayList<>(n);
        for(int i=0; i<n; i++) {
            docs.add(new ArrayList<>());
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
                docs.get(docId).add(i + 1);
                docs.get(docId).add(tf);
                if(tf!=0)
                    cntNonZero++;
            }
            i++;
        }
        return String.format("%d %d %d\n", n, m, cntNonZero)+display(docs);
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

    /**
     *
     * @param docs
     * @return string as the sparse matrix
     */
    String display(List<List<Integer>> docs) {
        return docs.parallelStream()
                .map(e -> e.stream().map(Object::toString).collect(Collectors.joining(" ")))
                .collect(Collectors.joining("\n"));
    }
}
