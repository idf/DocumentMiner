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
    int slopSize = 5;
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
            // System.out.println(pair.getKey()+" = "+pair.getValue());
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

    private void recordPosition(int docSeq, String str, BitSet termPos) throws IOException {
        termPos.clear();
        DocsAndPositionsEnum dpe = MultiFields.getTermPositionsEnum(this.reader, null, this.fieldName, new BytesRef(str));
        dpe.advance(docSeq);
        // remember all positions of the term in this doc
        for (int j = 0; j < dpe.freq(); j++) {
            termPos.set(dpe.nextPosition());
        }
    }

    private HashMap<String, CollocationScorer> processTerm(Term term, int slopSize) throws IOException {
        System.out.println("Processing term: "+term);

        if(isTermTooPopularOrNotPopularEnough(term, this.reader.docFreq(term)/ (float) this.reader.numDocs())) {
            return null;
        }
        // get a list of all the docs with this term
        // Apache Lucene Migration Guide 4.0
        // get dpe in first hand
        DocsAndPositionsEnum dpe = MultiFields.getTermPositionsEnum(this.reader, null, this.fieldName, term.bytes());
        HashMap<String, CollocationScorer> phraseTerms = new HashMap<String, CollocationScorer>();

        // for all docs that CONTAIN this term
        while (dpe.nextDoc()!=DocsEnum.NO_MORE_DOCS) {
            int docId = dpe.docID();
            System.out.println("Processing docId: "+docId);
            Terms tv = this.reader.getTermVector(docId, this.fieldName);
            TermsEnum te = tv.iterator(null);

            // now look at all OTHER terms_str in this doc and see if they are
            // restore the structure
            HashMap<Integer, Term> pos2term = new HashMap<>();
            while(te.next()!=null) {
                DocsAndPositionsEnum dpeB = MultiFields.getTermPositionsEnum(this.reader, null, this.fieldName, te.term());
                dpeB.advance(docId);
                // remember all positions of the term in this doc
                for (int j = 0; j < dpeB.freq(); j++) {
                    pos2term.put(dpeB.nextPosition(), new Term(FieldName.CONTENT, te.term().utf8ToString()));
                }
            }


            HashSet<Term> termsFound = new HashSet<>();
            for (int k = 0; (k < dpe.freq()); k++) {  // for term A
                Integer position = dpe.nextPosition();
                Integer startpos = Math.max(0, position - slopSize);
                Integer endpos = position + slopSize;
                for(int curpos = startpos; curpos<=endpos; curpos++) {  // for term B
                    if(curpos==position)
                        continue;
                    Term termB = pos2term.get(curpos);
                    if(termB==null)
                        continue;
                    // System.out.println("around: "+termB);
                    if(termsFound.contains(termB))
                        continue;

                    if (!this.filter.processTerm(termB.bytes().utf8ToString())) {
                        continue;
                    }
                    if (!StringUtils.isAlpha(termB.bytes().utf8ToString())) {
                        continue;
                    }

                    CollocationScorer pt = (CollocationScorer) phraseTerms.get(termB.bytes().utf8ToString());

                    if (pt==null) {  // if not exist
                        float percentB = (float) this.reader.docFreq(termB) / (float) this.reader.numDocs();
                        if(isTermTooPopularOrNotPopularEnough(termB, percentB)) {
                            termsFound.add(termB);
                            continue;
                        }
                        pt = new CollocationScorer(term.text(), termB.bytes().utf8ToString(), this.reader.docFreq(term), this.reader.docFreq(termB), this.reader.numDocs());
                        phraseTerms.put(pt.getCoincidentalTerm(), pt);
                    }

                    pt.incCoIncidenceDocCount();
                    termsFound.add(termB);  // whether check the same doc multiple times
                }

            } // term positions loop for term A
        }// end docs loop
        return phraseTerms;
    }




}
