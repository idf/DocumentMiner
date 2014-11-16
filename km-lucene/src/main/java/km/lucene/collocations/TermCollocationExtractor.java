package km.lucene.collocations;

import km.common.Setting;
import km.lucene.constants.FieldName;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.index.*;
import org.apache.lucene.index.collocations.CollocationScorer;
import org.apache.lucene.index.collocations.TermFilter;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.File;
import java.io.IOException;
import java.util.*;
/**
 * User: Danyang
 * Date: 10/7/14
 * Time: 2:12 PM
 */
public class TermCollocationExtractor {
    // refactor: later
    private static final FacetsConfig config = new FacetsConfig();
    private IndexReader reader;

    // collocation
    static int DEFAULT_MAX_NUM_DOCS_TO_ANALYZE = 120000;
    static int maxNumDocsToAnalyze = DEFAULT_MAX_NUM_DOCS_TO_ANALYZE;
    String fieldName = FieldName.CONTENT;
    static float DEFAULT_MIN_TERM_POPULARITY = 0.0002f;
    float minTermPopularity = DEFAULT_MIN_TERM_POPULARITY;
    static float DEFAULT_MAX_TERM_POPULARITY = 1f;
    float maxTermPopularity = DEFAULT_MAX_TERM_POPULARITY;
    int numCollocatedTermsPerTerm = 20;
    int slopSize = 1;
    long totalVocabulary = 0;
    TermFilter filter = new TermFilter();

    public TermCollocationExtractor(String indexPath, String thindexPath, String taxoPath) throws IOException {
        this.reader = DirectoryReader.open(FSDirectory.open(new File(thindexPath)));

        Fields fields = MultiFields.getFields(this.reader);
        Terms terms = fields.terms(this.fieldName);
        // this.totalVocabulary = terms.size();  // ERROR -1
        TermsEnum iterator = terms.iterator(null);
        BytesRef byteRef = null;
        while((byteRef = iterator.next()) != null) {
            this.totalVocabulary++;
        }
    }

    public static void main(String[] args) throws IOException, ParseException {
        // test parameters
        args = new String[3];
        args[0] = Setting.INDEX_PATH;
        args[1] = Setting.THINDEX_PATH;
        args[2] = Setting.TAXOINDEX_PATH;

        if (args.length < 3) {
            System.out.println("Please specify data file, index folder, taxonomy index folder in sequence.");
            System.exit(1);
        }
        System.out.println("Started");
        String indexPath = args[0];
        String thindexPath = args[1];
        String taxoPath = args[2];

        TermCollocationExtractor termCollocationExtractor = new TermCollocationExtractor(indexPath, thindexPath, taxoPath);
        termCollocationExtractor.extract(new Term(FieldName.CONTENT, "ntu"));

    }

    public void extract(Term t) throws IOException, ParseException {
        HashMap<String, CollocationScorer> phraseTerms = processTerm(t, this.slopSize);

        class ValueComparator implements Comparator<String> {
            Map<String, CollocationScorer> base;
            public ValueComparator(Map<String, CollocationScorer> base) {
                this.base = base;
            }

            // Note: this comparator imposes orderings that are inconsistent with equals.
            @Override
            public int compare(String a, String b) {
                if (base.get(a).getScore() < base.get(b).getScore()) {
                    return 1;
                } else {
                    return -1;
                } // returning 0 would merge keys
            }
        }
        ValueComparator bvc = new ValueComparator(phraseTerms);
        TreeMap<String,CollocationScorer> sortedMap = new TreeMap<>(bvc);
        sortedMap.putAll(phraseTerms);

        Iterator it = sortedMap.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            System.out.println(pair.getKey()+" = "+((CollocationScorer) pair.getValue()).getScore());
            System.out.println(pair.getKey()+" = "+pair.getValue());
        }
    }



    private boolean isTermTooPopularOrNotPopularEnough(Term term, float percent) {
        // check term is not too rare or frequent
        if (percent < minTermPopularity) {
            System.out.println(term.text() + " not popular enough " + percent);
            return true;
        }
        if (percent > maxTermPopularity) {
            System.out.println(term.text() + " too popular " + percent);
            return true;
        }
        return false;
    }

    /**
     * TODO
     * @param docSeq: doc sequence (doc id?)
     * @param term: term interested
     * @param termPos: positions of the term
     * @param tv: NIL
     * @param terms: terms in the document
     * @return: index in terms of the term interested
     * @throws IOException
     */
    private int recordAllPositionsOfTheTermInCurrentDocumentBitset(int docSeq, Term term, BitSet termPos, Terms tv, String[] terms) throws IOException {
        int index = 0;
        while(index<terms.length && terms[index]!=term.text()) index++;
        if(index==terms.length)
            return -1;
        recordPosition(docSeq, terms[index], termPos);
        return index;
    }

    private void recordPosition(int docSeq, String str, BitSet termPos) throws IOException {
        termPos.clear();
        DocsAndPositionsEnum dpe = MultiFields.getTermPositionsEnum(this.reader, null, this.fieldName, new BytesRef(str));
        dpe.advance(docSeq);
        // remember all positions of the term in this doc
        for (int j = 0; j < dpe.freq(); j++) {
            termPos.set(dpe.nextPosition());
        }
    }

    /**
     * Core, inc collocation count
     * @param term
     * @param totalNumDocs
     * @param phraseTerms
     * @param termPos
     * @param terms
     * @param j
     * @param matchFound
     * @param startpos
     * @param endpos
     * @throws IOException
     */
    private void populateHashMapWithPhraseTerms(Term term,
                                                int totalNumDocs,
                                                HashMap<String, CollocationScorer> phraseTerms,
                                                BitSet termPos,
                                                String[] terms,
                                                int j,
                                                boolean[] matchFound,
                                                int startpos,
                                                int endpos ) throws IOException {
        for (int curpos = startpos; (curpos <= endpos) && (!matchFound[j]); curpos++) {  // iterate the positions
            if (termPos.get(curpos)) {
                // Add term to hashmap containing co-occurrence
                // counts for this term
                CollocationScorer pt = (CollocationScorer) phraseTerms.get(terms[j]);

                if (pt==null) {  // if not exist
                    Term otherTe = new Term(this.fieldName, terms[j]);
                    int numDocsForTerm = Math.min(this.reader.docFreq(term), maxNumDocsToAnalyze);
                    int numDocsForOtherTerm = Math.min(this.reader.docFreq(otherTe), maxNumDocsToAnalyze);

                    float otherPercent = (float) numDocsForOtherTerm / (float) totalNumDocs;
                    if(isTermTooPopularOrNotPopularEnough(otherTe, otherPercent)) {
                        matchFound[j] = true;
                        continue;
                    }
                    pt = new CollocationScorer(term.text(), terms[j], numDocsForTerm, numDocsForOtherTerm, this.reader.numDocs());
                    phraseTerms.put(pt.getCoincidentalTerm(), pt);
                }

                pt.incCoIncidenceDocCount();
                matchFound[j] = true;  // whether check the same doc multiple times
            }
        }
    }

    private HashMap<String, CollocationScorer> processTerm(Term term, int slopSize) throws IOException {
        BytesRef bytesRef = term.bytes();
        System.out.println("Processing term: "+term);

        int numDocsForTerm = this.reader.docFreq(term);
        int totalNumDocs = reader.numDocs();
        float percent = (float) numDocsForTerm / (float) totalNumDocs;

        if(isTermTooPopularOrNotPopularEnough(term, percent)) {
            return null;
        }
        // get a list of all the docs with this term
        // Apache Lucene Migration Guide
        // TermDocs td = reader.termDocs(term);
        // get dpe in first hand
        DocsAndPositionsEnum dpe = MultiFields.getTermPositionsEnum(this.reader, null, this.fieldName, bytesRef);
        HashMap<String, CollocationScorer> phraseTerms = new HashMap<String, CollocationScorer>();
        int MAX_TERMS_PER_DOC = 100000;

        BitSet termPos = new BitSet(MAX_TERMS_PER_DOC);


        // for all docs that CONTAIN this term
        int docSeq;
        while ((docSeq = dpe.nextDoc())!=DocsEnum.NO_MORE_DOCS) {
            int docId = dpe.docID();
            // System.out.println("Processing docId: "+docId);
            Terms tv = this.reader.getTermVector(docId, this.fieldName);
            TermsEnum te = tv.iterator(null);

            List<String> terms_list = new ArrayList<>();
            while(te.next()!=null) {
                terms_list.add(te.term().utf8ToString());
            }
            String[] terms_str = terms_list.toArray(new String[terms_list.size()]);
            // System.out.println("terms_str: "+Arrays.toString(terms_str));
            // int index = recordAllPositionsOfTheTermInCurrentDocumentBitset(docSeq, term, termPos, tv, terms_str);
            int index = -1;

            // now look at all OTHER terms_str in this doc and see if they are
            // positioned in a pre-defined sized window around the current term
            // sequential code
            boolean[] matchFound = new boolean[terms_str.length]; // single match is sufficient, no duplicate process
            for(int j=0; j < matchFound.length; j++)
                matchFound[j] = false;  // mask to whether count once or multiple times in a doc

            for (int k = 0; (k < dpe.freq()); k++) {  // for term A
                Integer position = dpe.nextPosition();
                Integer startpos = Math.max(0, position - slopSize);
                Integer endpos = position + slopSize;
                for (int j = 0; j < terms_str.length && !matchFound[j]; j++) {
                    if (j == index) { // (term A)
                        continue;
                    }
                    if (!this.filter.processTerm(terms_str[j])) {
                        continue;
                    }
                    if (!StringUtils.isAlpha(terms_str[j])) {
                        continue;
                    }
                    // inefficient, better algorithm exists
                    // iterate through all other items (item B)
                    populateHashMapWithPhraseTerms(
                            term,
                            totalNumDocs,
                            phraseTerms,
                            termPos,
                            terms_str, j,
                            matchFound,
                            startpos,
                            endpos);
                }

            }
        }// end docs loop
        return phraseTerms;
    }




}
