package km.lucene.applets.collocations;

import io.deepreader.java.commons.util.Displayer;
import io.deepreader.java.commons.util.ExceptionUtils;
import io.deepreader.java.commons.util.Sorter;
import io.deepreader.java.commons.util.Timestamper;
import km.common.Config;
import km.lucene.analysis.CustomAnalyzer;
import km.lucene.constants.FieldName;
import km.lucene.entities.ScoreMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.index.collocations.CollocationScorer;
import org.apache.lucene.index.collocations.TermFilter;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.LuceneUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * User: Danyang
 * Date: 10/7/14
 * Time: 2:12 PM
 */
public class TermCollocationExtractor {
    IndexReader reader;
    IndexSearcher searcher;
    private Logger logger = LoggerFactory.getLogger(TermCollocationExtractor.class);

    // collocation
    static int DEFAULT_MAX_NUM_DOCS_TO_ANALYZE = 120000;
    static int maxNumDocsToAnalyze = DEFAULT_MAX_NUM_DOCS_TO_ANALYZE;
    String fieldName = FieldName.CONTENT;
    int slopSize = Config.settings.getSlopSize();
    long totalVocabulary = 0;
    TermFilter filter = new TermFilter();
    boolean onceInDoc = true;

    // rake
    RakeCollocationMgr rakeMgr;

    // top k
    BitSet liveDocs = new BitSet();
    int k = Config.settings.getColloTopK();
    int displayTopK = Config.settings.getDisplayTopK();

    TermCollocationHelper helper = new TermCollocationHelper();

    // JSON entries
    static final String PHRASES_STR = "phrases";
    static final String TERMS_STR = "terms";
    static final String PHRASES_EXCLUDED_STR = "phrases_excluded";

    public static void main(String[] args) throws Exception {
        // test parameters
        args = new String[4];
        args[0] = Config.settings.getIndexPath();
        args[1] = Config.settings.getPostindexPath();
        args[2] = Config.settings.getTaxoindexPath();
        args[3] = Config.settings.getDriverSettings().getClusteredIndexPath();  // args[3] = Config.settings.getRakeSettings().getBasicIndexPath();

        if (args.length < 4) {
            System.out.println("Please specify 4 params.");
            System.exit(1);
        }
        System.out.println("Started");
        String indexPath = args[0];
        String mainIndexPath = args[1];
        String taxoPath = args[2];
        String rakeIndexPath = args[3];

        TermCollocationExtractor tce = new TermCollocationExtractor(indexPath, mainIndexPath, taxoPath, rakeIndexPath);
        Map<String, ScoreMap> sorts = tce.search("ntu");
        sorts.entrySet().forEach(e -> tce.helper.display(Sorter.topEntries(e.getValue(), tce.displayTopK, tce.helper.getComparator())));
    }

    public TermCollocationExtractor(String indexPath, String mainIndexPath, String taxoPath, String rakeIndexPath) {
        try {
            this.reader = LuceneUtils.reader(mainIndexPath);
            this.searcher = LuceneUtils.searcher(reader);

            Terms terms = LuceneUtils.terms(reader, fieldName);
            TermsEnum te = terms.iterator(null);
            BytesRef byteRef = null;
            while((byteRef=te.next())!=null) {
                this.totalVocabulary++;
            }
            this.liveDocs = new BitSet(this.reader.numDocs());
            this.rakeMgr = new RakeCollocationMgr(rakeIndexPath);
        }
        catch (Exception e) {
            logger.error(Displayer.display(e));
            System.exit(1);
        }
    }

    private void adjustK(int div) {
        this.k /= div;
        if(this.k<75)
            this.k = 75;
    }

    private void adjustK() {
        this.k = Config.settings.getColloTopK();
    }

    public Map<String, ScoreMap> search(String queryString) throws ParseException, IOException, URISyntaxException {
        Timestamper timestamper = new Timestamper();
        timestamper.start();

        // query
        QueryParser queryParser = new QueryParser(Version.LUCENE_48, this.fieldName, new CustomAnalyzer(Version.LUCENE_48));
        // queryParser.setAutoGeneratePhraseQueries(true);  // phrase query?
        Query query = queryParser.parse(queryString);
        Set<Term> terms = new HashSet<>();
        query.extractTerms(terms);
        adjustK(terms.size());  // dynamic adjusted; for speed performance

        TopScoreDocCollector collector = TopScoreDocCollector.create(this.k, true);
        this.searcher.search(query, collector);
        TopDocs topDocs = collector.topDocs();

        for(int j=0; j<Math.min(this.k, topDocs.totalHits); j++) {
            this.liveDocs.set(topDocs.scoreDocs[j].doc);
        }
        // pre-process
        rakePreprocess(topDocs);

        // collocation
        List<Map<String, ScoreMap>> rets = terms.stream()
                .map(e -> collocateIndividualTerm(e, topDocs))
                .collect(Collectors.toList());

        Map<String, ScoreMap> ret = mergeSearch(rets);

        // merge individual collocation results
        List<String> termStrs = terms.stream().map(Term::text).collect(Collectors.toList());
        ret.get(TERMS_STR).excludeMatchAny(termStrs);  // 200 ms
        ret.get(PHRASES_STR).excludeMatchAll(termStrs);

        // construct phrases excluding query string
        if(termStrs.size()==1) {  // single term query
            ScoreMap temp = new ScoreMap(ret.get(PHRASES_STR));
            temp.excludeMatchAny(termStrs);
            ret.put(PHRASES_EXCLUDED_STR, temp);
        }

        adjustK();
        logger.info("Search ended, elapsed: "+timestamper.end());
        return ret;
    }

    /**
     * Merge either phrases map or terms map
     * @param t
     * @param topDocs
     * @return
     * @throws IOException
     */
    private Map<String, ScoreMap> collocateIndividualTerm(Term t, TopDocs topDocs) {
        Map<String, CollocationScorer> termBScores = new HashMap<>();
        Map<String, CollocationScorer> phraseBScores = new HashMap<>();
        try {
            for(int j = 0; j < Math.min(this.k, topDocs.totalHits); j++){
                DocsAndPositionsEnum dpe = MultiFields.getTermPositionsEnum(this.reader, null, this.fieldName, t.bytes());
                int docID = topDocs.scoreDocs[j].doc;
                dpe.advance(docID);
                if(dpe.docID()!=docID)  // issue #17
                    continue;
                this.processDocForTerm(t, dpe, termBScores, phraseBScores, true);
            }
        }
        catch (IOException e) {
            ExceptionUtils.stifleCompileTime(e);
        }

        termBScores = this.helper.filterByTermFreq(termBScores, 5);
        phraseBScores = this.helper.filterByTermFreq(phraseBScores, 5);
        ScoreMap sortedTermBScores = ScoreMap.sortScores(termBScores);
        ScoreMap sortedPhraseBScores = ScoreMap.sortScores(phraseBScores);

        Map<String, ScoreMap> ret = new HashMap<>();
        ret.put(TERMS_STR, sortedTermBScores);
        ret.put(PHRASES_STR, sortedPhraseBScores);
        logger.info("Search " + t.text() + " completed");
        return ret;
    }

    /**
     * Merge the search results of phrase query
     * Merge the results form each term from the phrase query
     * @param rets List: listed by query terms; Map: Json-like; ScoreMap: storing collocation scoring results
     * @return
     */
    private Map<String, ScoreMap> mergeSearch(List<Map<String, ScoreMap>> rets) {
        Map<String, ScoreMap> ret = new HashMap<>();
        List<ScoreMap> terms = new ArrayList<>();
        List<ScoreMap> phrases = new ArrayList<>();
        for(Map<String, ScoreMap> r: rets) {  // length of rets == length of query terms
            terms.add(r.get(TERMS_STR));
            phrases.add(r.get(PHRASES_STR));
        }
        ret.put(TERMS_STR, ScoreMap.mergeMaps(terms));
        ret.put(PHRASES_STR, ScoreMap.mergeMaps(phrases));
        return ret;
    }


    /**
     * Rake, search pre-process
     * @param topDocs
     * @throws IOException
     */
    private void rakePreprocess(TopDocs topDocs) throws IOException {
        List<String> docs = new ArrayList<>();
        for (int j = 0; j < Math.min(this.k, topDocs.totalHits); j++) {
            Document doc = this.searcher.doc(topDocs.scoreDocs[j].doc);
            docs.add(doc.get(FieldName.CONTENT));
        }
        this.rakeMgr.renewPreIndex(docs);
        this.logger.debug("pre-processing");
        this.logger.debug(this.rakeMgr.preIndex.toString());
    }

    /**
     * Sample: tce.extract(new Term(tce.fieldName, "ntu"));
     * extract collocations from ALL the documents
     * @param t
     * @throws IOException
     * @throws ParseException
     */
    private void extract(Term t) throws IOException, ParseException {
        Timestamper timestamper = new Timestamper();
        timestamper.start();
        Map<String, CollocationScorer> termBScores = processTerm(t);
        termBScores = this.helper.filterByCollocationCount(termBScores, 5);
        ScoreMap.sortScores(termBScores);
        logger.info("Elpased: "+timestamper.end());
    }


    /**
     * looking at the term level,
     * @param term
     * @return
     * @throws IOException
     */
    private HashMap<String, CollocationScorer> processTerm(Term term) throws IOException {
        this.logger.debug("Processing term: "+term);
        if(this.helper.isTooPopularOrNotPopularEnough(this.reader.docFreq(term) / (float) this.reader.numDocs())) {
            return null;
        }
        // get dpe in first hand
        DocsAndPositionsEnum dpe = MultiFields.getTermPositionsEnum(this.reader, null, this.fieldName, term.bytes());
        HashMap<String, CollocationScorer> termBScores = new HashMap<>();  // variation of ScoreMap
        HashMap<String, CollocationScorer> phraseBScores = new HashMap<>();

        while (dpe.nextDoc()!=DocsEnum.NO_MORE_DOCS) {
            processDocForTerm(term, dpe, termBScores, phraseBScores, false);
        }
        return termBScores;
    }

    /**
     * looking at the document&term level
     * now look at all OTHER terms_str in this doc and see if they are collocated 
     * @param term
     * @param dpeA
     * @param termBScores
     * @param phraseBScores
     * @param top
     * @throws IOException
     */
    private void processDocForTerm(Term term,
                                   DocsAndPositionsEnum dpeA,
                                   Map<String, CollocationScorer> termBScores,
                                   Map<String, CollocationScorer> phraseBScores,
                                   boolean top) throws IOException {
        int docID = dpeA.docID();
        this.logger.trace("Processing docId: "+docID);

        // restore the structure
        Terms tv = this.reader.getTermVector(docID, this.fieldName);
        TermsEnum te = tv.iterator(null);
        HashMap<Integer, Term> pos2term = new HashMap<>();
        HashMap<Integer, Pair<Integer, Integer>> pos2offset = new HashMap<>();
        while(te.next()!=null) {
            // DocsAndPositionsEnum dpeB = MultiFields.getTermPositionsEnum(this.reader, null, this.fieldName, te.term());
            // dpeB.advance(docId);
            DocsAndPositionsEnum dpeB = te.docsAndPositions(null, null); // to speed up, rather than advance
            if (dpeB.nextDoc()!=DocIdSetIterator.NO_MORE_DOCS) {
                // remember (memory) all positions of the term in this doc
                for (int j=0; j<dpeB.freq(); j++) {
                    int pos = dpeB.nextPosition();
                    Pair<Integer, Integer> offsets = new ImmutablePair<>(dpeB.startOffset(), dpeB.endOffset());
                    pos2offset.put(pos, offsets);
                    pos2term.put(pos, new Term(FieldName.CONTENT, te.term().utf8ToString()));
                }
            }
        }

        // rake
        Map<Integer, rake4j.core.model.Term> offset2phrase = this.rakeMgr.phrases(this.searcher.doc(docID).get(FieldName.CONTENT));
        // update scorer
        Set<Term> termsFound = new HashSet<>();  // only count once for the termB in one document
        Set<String> phraseFound = new HashSet<>();
        for (int k=0; k<dpeA.freq(); k++) {  // for term A
            Integer position = dpeA.nextPosition();
            Integer startpos = Math.max(0, position - this.slopSize);
            Integer endpos = position + this.slopSize;
            for(int curpos = startpos; curpos<=endpos; curpos++) {  // for term B
                if(curpos!=position)
                    incrementCollocationScore_term(term, termBScores, top, pos2term, termsFound, curpos);
                if(pos2offset.containsKey(curpos))
                    incrementCollocationScore_phrase(term, phraseBScores, top, offset2phrase, phraseFound, pos2offset.get(curpos).getLeft());
            }
        }
    }

    private void incrementCollocationScore_term(Term term, Map<String, CollocationScorer> scores, boolean top, Map<Integer, Term> map, Set<Term> termsFound, int cur_position) throws IOException {
        // getting termB
        Term termB = map.get(cur_position);

        // filtering
        if(termB==null ||
                termB.text().length()<3 ||
                termB.bytes().equals(term.bytes()) ||
                this.rakeMgr.rake.getStopWordPat().matcher(termB.text()).find() || // filter by rake stop words
                termsFound.contains(termB)
                )
            return;

        // scoring
        CollocationScorer pt = scores.get(termB.bytes().utf8ToString());

        if (pt==null) {  // if not exist
            float percentB = (float) this.reader.docFreq(termB) / (float) this.reader.numDocs();
            if(this.helper.isTooPopularOrNotPopularEnough(percentB)) {
                termsFound.add(termB);
                return;
            }
            if(top) {
                /* top k document sampling strategy */
                int dfB = 0;
                DocsAndPositionsEnum dpeB = MultiFields.getTermPositionsEnum(this.reader, null, this.fieldName, termB.bytes());
                while (dpeB.nextDoc()!= DocsEnum.NO_MORE_DOCS) {
                    if(this.liveDocs.get(dpeB.docID()))
                        dfB ++;
                }
                pt = new CollocationScorer(term.text(), termB.text(), this.reader.docFreq(term),  this.reader.docFreq(termB), this.reader.totalTermFreq(termB), this.reader.numDocs(), this.k, dfB);
            }
            else {
                pt = new CollocationScorer(term.text(), termB.text(), this.reader.docFreq(term), this.reader.docFreq(termB), this.reader.totalTermFreq(termB), this.reader.numDocs());
            }
            scores.put(pt.getCoincidentalTerm(), pt);
        }

        pt.incCoIncidenceDocCount();
        if(this.onceInDoc)
            termsFound.add(termB);  // depends on whether to check the same doc multiple times
    }

    private void incrementCollocationScore_phrase(Term term, Map<String, CollocationScorer> scores, boolean top, Map<Integer, rake4j.core.model.Term> offset2phrase, Set<String> phrasesFound, int cur_offset) throws IOException {
        // get term B
        if(!offset2phrase.containsKey(cur_offset))
            return ;
        String phraseB = offset2phrase.get(cur_offset).getTermText();
        this.logger.trace("collocation: "+phraseB);

        // filtering
        if(phraseB==null ||
                this.rakeMgr.index.docFreq(phraseB)<=0 ||  // filtering by doc freq (will be filtered again later)
                phraseB.equals(term.bytes()) ||
                phrasesFound.contains(phraseB)
                )
            return;

        // scoring
        CollocationScorer pt = scores.get(phraseB);

        if (pt==null) {  // if not exist
            if(top) {
                pt = new CollocationScorer(term.text(), phraseB, this.reader.docFreq(term), this.rakeMgr.index.docFreq(phraseB), this.rakeMgr.index.totalTermFreq(phraseB), this.reader.numDocs(), this.k, this.rakeMgr.preIndex.docFreq(phraseB), this.rakeMgr.index.totalTermFreq());
            }
            else {
                pt = new CollocationScorer(term.text(), phraseB, this.reader.docFreq(term), this.rakeMgr.index.docFreq(phraseB), this.rakeMgr.index.totalTermFreq(phraseB), this.reader.numDocs());
            }
            scores.put(pt.getCoincidentalTerm(), pt);
        }
        pt.incCoIncidenceDocCount();
        if(this.onceInDoc)
            phrasesFound.add(phraseB);  // depends on whether to check the same doc multiple times
    }
}
