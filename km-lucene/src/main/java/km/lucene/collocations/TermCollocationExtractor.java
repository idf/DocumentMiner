package km.lucene.collocations;

import km.common.Setting;
import km.lucene.analysis.CustomAnalyzer;
import km.lucene.constants.FieldName;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.index.*;
import org.apache.lucene.index.collocations.CollocationScorer;
import org.apache.lucene.index.collocations.TermFilter;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;
import util.Timestamper;

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
    private IndexSearcher searcher;

    // collocation
    static int DEFAULT_MAX_NUM_DOCS_TO_ANALYZE = 120000;
    static int maxNumDocsToAnalyze = DEFAULT_MAX_NUM_DOCS_TO_ANALYZE;
    String fieldName = FieldName.CONTENT;
    int slopSize = 10;
    long totalVocabulary = 0;
    TermFilter filter = new TermFilter();

    // top k
    BitSet liveDocs = new BitSet();
    int k = 100;

    // delegation
    TermCollocationHelper helper = new TermCollocationHelper();

    public TermCollocationExtractor(String indexPath, String thindexPath, String taxoPath) throws IOException {
        this.reader = DirectoryReader.open(FSDirectory.open(new File(thindexPath)));
        this.searcher = new IndexSearcher(this.reader);

        Fields fields = MultiFields.getFields(this.reader);
        Terms terms = fields.terms(this.fieldName);
        TermsEnum iterator = terms.iterator(null);
        BytesRef byteRef = null;
        while((byteRef = iterator.next()) != null) {
            this.totalVocabulary++;
        }

        this.liveDocs = new BitSet(this.reader.numDocs());
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

        TermCollocationExtractor tce = new TermCollocationExtractor(indexPath, thindexPath, taxoPath);
        // tce.extract(new Term(tce.fieldName, "ntu"));
        tce.search("ntu");


    }

    public void search(String queryString) throws ParseException, IOException {
        Timestamper timestamper = new Timestamper();
        timestamper.start();
        QueryParser queryParser = new QueryParser(Version.LUCENE_48, this.fieldName, new CustomAnalyzer(Version.LUCENE_48));
        Query query = queryParser.parse(queryString);
        TopScoreDocCollector collector = TopScoreDocCollector.create(this.k, true);
        this.searcher.search(query, collector);
        Set<Term> terms = new HashSet<>();
        query.extractTerms(terms);
        Term t = (Term) terms.toArray()[0];

        TopDocs topDocs = collector.topDocs();
        System.out.println(topDocs.totalHits);
        System.out.println(terms.size());

        for(int j=0; j<Math.min(this.k, topDocs.totalHits); j++) {
            this.liveDocs.set(topDocs.scoreDocs[j].doc);
        }

        HashMap<String, CollocationScorer> termBScores = new HashMap<>();
        for (int j = 0; j < Math.min(this.k, topDocs.totalHits); j++) {

            DocsAndPositionsEnum dpe = MultiFields.getTermPositionsEnum(this.reader, null, this.fieldName, t.bytes());
            int docID = topDocs.scoreDocs[j].doc;
            // Document d = searcher.doc(docID);
            dpe.advance(docID);
            this.processDocForTerm(t, dpe, termBScores, true);
        }
        termBScores = this.helper.filterCollocationCount(termBScores, 5);
        this.helper.sortScores(termBScores);
        timestamper.end();
    }

    public void extract(Term t) throws IOException, ParseException {
        Timestamper timestamper = new Timestamper();
        timestamper.start();
        HashMap<String, CollocationScorer> termBScores = processTerm(t);
        termBScores = this.helper.filterCollocationCount(termBScores, 5);
        this.helper.sortScores(termBScores);
        timestamper.end();
    }


    /**
     * looking at the term level
     * @param term
     * @return
     * @throws IOException
     */
    private HashMap<String, CollocationScorer> processTerm(Term term) throws IOException {
        System.out.println("Processing term: "+term);

        if(this.helper.isTermTooPopularOrNotPopularEnough(term, this.reader.docFreq(term) / (float) this.reader.numDocs())) {
            return null;
        }
        // get dpe in first hand
        DocsAndPositionsEnum dpe = MultiFields.getTermPositionsEnum(this.reader, null, this.fieldName, term.bytes());
        HashMap<String, CollocationScorer> termBScores = new HashMap<String, CollocationScorer>();

        while (dpe.nextDoc()!=DocsEnum.NO_MORE_DOCS) {
            processDocForTerm(term, dpe, termBScores, false);
        }
        return termBScores;
    }

    /**
     * looking at the document&term level
     * now look at all OTHER terms_str in this doc and see if they are collocated 
     * @param term
     * @param dpeA
     * @param termBScores
     * @param top
     * @throws IOException
     */
    private void processDocForTerm(Term term, DocsAndPositionsEnum dpeA, HashMap<String, CollocationScorer> termBScores, boolean top) throws IOException {
        int docId = dpeA.docID();
        // System.out.println("Processing docId: "+docId);
        
        // restore the structure
        Terms tv = this.reader.getTermVector(docId, this.fieldName);
        TermsEnum te = tv.iterator(null);
        HashMap<Integer, Term> pos2term = new HashMap<>();
        HashMap<Integer, Pair<Integer, Integer>> pos2offset = new HashMap<>();
        while(te.next()!=null) {
            // DocsAndPositionsEnum dpeB = MultiFields.getTermPositionsEnum(this.reader, null, this.fieldName, te.term());
            // dpeB.advance(docId);
            DocsAndPositionsEnum dpeB = te.docsAndPositions(null, null); // to speed up, rather than advance
            if (dpeB.nextDoc() != DocIdSetIterator.NO_MORE_DOCS) {
                // remember all positions of the term in this doc
                for (int j = 0; j < dpeB.freq(); j++) {
                    int pos = dpeB.nextPosition();
                    Pair<Integer, Integer> offsets = new ImmutablePair<>(dpeB.startOffset(), dpeB.endOffset());
                    pos2offset.put(pos, offsets);
                    pos2term.put(pos, new Term(FieldName.CONTENT, te.term().utf8ToString()));
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
                if(termB.bytes().equals(term.bytes()))
                    continue;
                // System.out.println("around: "+termB);
                if(termsFound.contains(termB))
                    continue;

                if(CustomStopAnalyzer.ENGLISH_STOP_WORDS_SET.contains(termB.text()))
                    continue;

                if (!this.filter.processTerm(termB.bytes().utf8ToString())) {
                    continue;
                }
//                    if (!StringUtils.isAlpha(termB.bytes().utf8ToString())) {
//                        continue;
//                    }

                CollocationScorer pt = (CollocationScorer) termBScores.get(termB.bytes().utf8ToString());

                if (pt==null) {  // if not exist
                    float percentB = (float) this.reader.docFreq(termB) / (float) this.reader.numDocs();
                    if(this.helper.isTermTooPopularOrNotPopularEnough(termB, percentB)) {
                        termsFound.add(termB);
                        continue;
                    }
                    if(top) {
                        /*
                        top 100 strategy
                         */
                        int dfB = 0;
                        DocsAndPositionsEnum dpeB = MultiFields.getTermPositionsEnum(this.reader, null, this.fieldName, termB.bytes());
                        while (dpeB.nextDoc()!=DocsEnum.NO_MORE_DOCS) {
                            if(this.liveDocs.get(dpeB.docID()))
                                dfB ++;
                        }
                        pt = new CollocationScorer(term.text(), termB.text(), this.k,  this.reader.docFreq(termB), this.reader.numDocs());
                    }
                    else {
                        pt = new CollocationScorer(term.text(), termB.bytes().utf8ToString(), this.reader.docFreq(term), this.reader.docFreq(termB), this.reader.numDocs());
                    }
                    termBScores.put(pt.getCoincidentalTerm(), pt);
                }

                pt.incCoIncidenceDocCount();
                termsFound.add(termB);  // whether to check the same doc multiple times
            }

        } // END term positions loop for term A
    }


}
