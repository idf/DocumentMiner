package km.lucene.collocations;

import km.common.Setting;
import km.lucene.constants.FieldName;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.index.*;
import org.apache.lucene.index.collocations.CollocationScorer;
import org.apache.lucene.index.collocations.TermFilter;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.search.spans.Spans;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.BytesRef;
import util.Timestamper;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * User: Danyang
 * Date: 10/7/14
 * Time: 2:12 PM
 */
public class TermCollocationExtractor {
    // refactor: later
    private static final FacetsConfig config = new FacetsConfig();
    private IndexReader reader;
    private IndexSearcher searcher;

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
        this.searcher = new IndexSearcher(this.reader);

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
        // termCollocationExtractor.extract(new Term(FieldName.CONTENT, "ntu"), termCollocationExtractor.reader);
        // termCollocationExtractor.atomicExtract(new Term(FieldName.CONTENT, "ntu"));
        termCollocationExtractor.readerThreadMgr(new Term(FieldName.CONTENT, "ntu"));

    }

    /**
     * spans (docs) -> doc -> terms -> term
     * @param t
     * @throws IOException
     */
    public void atomicExtract(Term t) throws IOException {
        SpanTermQuery spanTermQuery = new SpanTermQuery(t);
        //this is not the best way of doing this, but it works for the example. See http://www.slideshare.net/lucenerevolution/is-your-index-atomicReader-really-atomic-or-maybe-slow for higher performance approaches
        AtomicReader wrapper = SlowCompositeReaderWrapper.wrap(this.reader);
        Map<Term, TermContext> termContexts = new HashMap<Term, TermContext>();
        Spans spans = spanTermQuery.getSpans(wrapper.getContext(), new Bits.MatchAllBits(this.reader.numDocs()), termContexts);
        while (spans.next()) {
            // too slow
            Map<Integer, String> entries = new TreeMap<Integer, String>();
            int start = spans.start() - this.slopSize;
            int end = spans.end() + this.slopSize;
            Terms tv = this.reader.getTermVector(spans.doc(), this.fieldName);  // multiple access to the same document
            TermsEnum te = tv.iterator(null);
            BytesRef term;
            while ((term = te.next()) != null) {
                //could store the BytesRef here, but String is easier for this example
                String s = new String(term.bytes, term.offset, term.length);
                DocsAndPositionsEnum dpe = te.docsAndPositions(null, null);
                if (dpe.nextDoc() != DocIdSetIterator.NO_MORE_DOCS) {
                    int i = 0;
                    int position = -1;
                    while (i < dpe.freq() && (position = dpe.nextPosition()) != -1) {
                        if (position >= start && position <= end) {
                            entries.put(position, s);
                        }
                        i++;
                    }
                }
            }
            System.out.println("Entries:" + entries);
        }
    }

    private void readerThreadMgr(Term t) throws IOException {
        Timestamper timestamper = new Timestamper();
        timestamper.start();
        HashMap<String, CollocationScorer> phraseTerms = new HashMap<>();
        Collections.synchronizedMap(phraseTerms);
        ExecutorService executor = Executors.newFixedThreadPool(8);
        for(AtomicReaderContext atomicReaderContext: this.reader.leaves()) {
//            executor.execute(
//                    new ReaderThread(t, atomicReaderContext.reader(), phraseTerms)
//            );
            processTerm(phraseTerms, t, atomicReaderContext.reader());
        }
        executor.shutdown();
        while (!executor.isTerminated()) {};
        timestamper.end();
        this.sortPhraseTerms(phraseTerms);

    }

    class ReaderThread implements Runnable {
        private Thread thread;
        private HashMap<String, CollocationScorer> phraseTerms;
        private Term t;
        private AtomicReader atomicReader;

        ReaderThread(Term t, AtomicReader atomicReader, HashMap<String, CollocationScorer> phraseTerms){
            this.phraseTerms = phraseTerms;
            this.t = t;
            this.atomicReader = atomicReader;
        }

        public void run() {
            System.out.println("Creating "+this.atomicReader);
            try {
                TermCollocationExtractor.this.processTerm(this.phraseTerms, t, this.atomicReader);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void start () {
            System.out.println("Starting "+this.atomicReader);
            if (thread == null) {
                thread = new Thread (this, this.atomicReader.toString());
                thread.start ();
            }
        }
    }

    //// OLD
    public void extract(Term t, IndexReader reader) throws IOException, ParseException {
        Timestamper timestamper = new Timestamper();
        timestamper.start();
        HashMap<String, CollocationScorer> phraseTerms = new HashMap<>();
        processTerm(phraseTerms, t, reader);
        timestamper.end();
        this.sortPhraseTerms(phraseTerms);
    }

    private TreeMap<String,CollocationScorer> sortPhraseTerms(HashMap<String, CollocationScorer> phraseTerms) {
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
        return sortedMap;
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

    private HashMap<String, CollocationScorer> processTerm(HashMap<String, CollocationScorer> phraseTerms, Term term, IndexReader reader) throws IOException {
        System.out.println("Processing term: "+term);

        if(isTermTooPopularOrNotPopularEnough(term, reader.docFreq(term)/ (float) reader.numDocs())) {
            return null;
        }
        // get dpe in first hand
        DocsAndPositionsEnum dpe = MultiFields.getTermPositionsEnum(reader, null, this.fieldName, term.bytes());
        while (dpe.nextDoc()!=DocsEnum.NO_MORE_DOCS) {
            processDocForTerm(term, dpe, phraseTerms, reader);
        }
        return phraseTerms;
    }

    private void processDocForTerm(Term term, DocsAndPositionsEnum dpeA, HashMap<String, CollocationScorer> phraseTerms, IndexReader reader) throws IOException {
        // System.out.println("Processing docId: "+dpeA.docID());
        // now look at all OTHER terms_str in this doc and see if they are
        // restore the structure
        Terms tv = reader.getTermVector(dpeA.docID(), this.fieldName);
        TermsEnum te = tv.iterator(null);

        HashMap<Integer, Term> pos2term = new HashMap<>();
        while(te.next()!=null) {
            // DocsAndPositionsEnum dpeB = MultiFields.getTermPositionsEnum(this.atomicReader, null, this.fieldName, te.term());
            // dpeB.advance(docId);
            DocsAndPositionsEnum dpeB = te.docsAndPositions(null, null); // to speed up, rather than advance
            if (dpeB.nextDoc() != DocIdSetIterator.NO_MORE_DOCS) {
                // remember all positions of the term in this doc
                for (int j = 0; j < dpeB.freq(); j++) {
                    pos2term.put(dpeB.nextPosition(), new Term(FieldName.CONTENT, te.term().utf8ToString()));
                }
            }
        }


        // update scorer
        HashSet<Term> termsFound = new HashSet<>();
        for (int k = 0; (k < dpeA.freq()); k++) {  // for term A
            Integer position = dpeA.nextPosition();
            Integer startpos = Math.max(0, position - this.slopSize);
            Integer endpos = position + this.slopSize;
            for(int curpos = startpos; curpos<=endpos; curpos++) {  // for term B
                if(curpos==position)
                    continue;
                Term termB = pos2term.get(curpos);  // how to get termB
                if(termB==null)
                    continue;
                // System.out.println("around: "+termB);
                if(termsFound.contains(termB))
                    continue;

                if (!this.filter.processTerm(termB.bytes().utf8ToString())) {
                    continue;
                }
//                    if (!StringUtils.isAlpha(termB.bytes().utf8ToString())) {
//                        continue;
//                    }


                CollocationScorer pt = phraseTerms.get(termB.bytes().utf8ToString());

                if (pt==null) {  // if not exist
                    float percentB = (float) reader.docFreq(termB) / (float) reader.numDocs();
                    if(isTermTooPopularOrNotPopularEnough(termB, percentB)) {
                        termsFound.add(termB);
                        continue;
                    }
                    pt = new CollocationScorer(term.text(), termB.bytes().utf8ToString(), reader.docFreq(term), reader.docFreq(termB), reader.numDocs());
                    phraseTerms.put(pt.getCoincidentalTerm(), pt);
                }
                pt.incCoIncidenceDocCount();
                termsFound.add(termB);  // whether to check the same doc multiple times
            }

        } // END term positions loop for term A
    }


}
