package km.lucene.applets.cluto;

import io.deepreader.java.commons.util.IOHandler;
import km.common.Config;
import km.lucene.constants.FieldName;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.LuceneUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User: Danyang
 * Date: 1/15/2015
 * Time: 15:20
 *
 */

/**
 * Construct Cluto readable matrix format from Lucene index directory
 */
public class Index2ClutoFormatter implements Runnable {
    private String indexPath;
    private String matOutputPath;

    protected Logger logger = LoggerFactory.getLogger(Index2ClutoFormatter.class);
    public static void main(String[] args) {
        new Index2ClutoFormatter(Config.settings.getThindexPath(),  // or getPostindexPath(), which is set by Driver
                Config.settings.getClutoSettings().getDocsMat())
                .run();
    }

    /**
     *
     * @param indexPath input path, the path to Lucene index directory
     * @param matOutputPath output path, the path to the readable matrix file
     */
    public Index2ClutoFormatter(String indexPath, String matOutputPath) {
        this.indexPath = indexPath;
        this.matOutputPath = matOutputPath;
    }

    @Override
    public void run() {
        try {
            IndexReader reader = LuceneUtils.reader(indexPath);
            String ret = getVectorSpaceMatrix(reader, FieldName.CONTENT);
            IOHandler.write(matOutputPath, ret);
            logger.info("Matrix written to "+matOutputPath);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 12GB Barely helps for sparse matrix with all entries
     * 4GB is more than sufficient for sparse matrix with non-zero entries
     *
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
        Terms terms = LuceneUtils.terms(reader, fieldName);
        TermsEnum te = terms.iterator(null);
        int m = 0;
        while(te.next()!=null)
            m++;
        assert m>0;
        int n = reader.numDocs();
        logger.info("Number of files: "+n);
        List<List<Pair<Integer, Float>>> docs = new ArrayList<>(n);
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
                // docs.get(docId).add(new ImmutablePair<>(i+1, simpleWeight(tf)));
                docs.get(docId).add(new ImmutablePair<>(i+1, tfIdfWeight(tf, te.docFreq(), n)));
                if(tf!=0)
                    cntNonZero++;
            }
            i++;
        }
        logger.info("Converted Lucene Index to Sparse Matrix");
        return String.format("%d %d %d\n", n, m, cntNonZero)+display_pair(docs);
    }

    /**
     * No weighting
     * @param tf
     * @return
     */
    private float simpleWeight(int tf) {
        return tf;
    }

    /**
     * tf-idf weighting
     * @param tf_td
     * @param df_t
     * @param D
     * @return
     */
    private float tfIdfWeight(int tf_td, int df_t, int D) {
        if(tf_td==0 || df_t==0)
            return 0;
        double score = (1+Math.log(tf_td))*Math.log(D/df_t);
        return (float) score;
    }

    /**
     * Get all non-zero entries
     * for sparse matrix, it can significantly reduce the file size
     * @param mat
     * @return
     */
    @Deprecated
    String display(int[][] mat) {
        StringBuilder sb = new StringBuilder();
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
    @Deprecated
    String display(List<List<Integer>> docs) {
        return docs.parallelStream()
                .map(e -> e.stream().map(Object::toString).collect(Collectors.joining(" ")))
                .collect(Collectors.joining("\n"));
    }

    /**
     *
     * @param docs
     * @return string as the sparse matrix
     */
    String display_pair(List<List<Pair<Integer, Float>>> docs) {
        return docs.parallelStream()
                .map(e -> e.stream().map(ee -> String.format("%d %f", ee.getKey(), ee.getValue())).collect(Collectors.joining(" ")))
                .collect(Collectors.joining("\n"));
    }
}
