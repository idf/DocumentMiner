package km.lucene.applets.collocations;

import io.deepreader.java.commons.util.Sorter;
import io.deepreader.java.commons.util.Timestamper;
import km.common.Settings;
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
    // refactor: later
    IndexReader reader;
    IndexSearcher searcher;
    private Logger logger = LoggerFactory.getLogger(TermCollocationExtractor.class);

    // collocation
    static int DEFAULT_MAX_NUM_DOCS_TO_ANALYZE = 120000;
    static int maxNumDocsToAnalyze = DEFAULT_MAX_NUM_DOCS_TO_ANALYZE;
    String fieldName = FieldName.CONTENT;
    int slopSize = 10;
    long totalVocabulary = 0;
    TermFilter filter = new TermFilter();

    // rake
    RakeCollocationMgr rakeMgr;

    // top k
    BitSet liveDocs = new BitSet();
    int k = 100;

    TermCollocationHelper helper = new TermCollocationHelper();

    public static void main(String[] args) throws Exception {
        // test parameters
        args = new String[4];
        args[0] = Settings.INDEX_PATH;
        // args[1] = Settings.THINDEX_PATH;
        args[1] = Settings.POSTINDEX_PATH;
        args[2] = Settings.TAXOINDEX_PATH;
        args[3] = Settings.RakeSettings.BASIC_INDEX_PATH;

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
        Map<String, ScoreMap> sorts = tce.search("ntu sce");
        sorts.entrySet().forEach(e -> tce.helper.display(Sorter.topEntries(e.getValue(), 10, tce.helper.getComparator())));
    }

    public TermCollocationExtractor(String indexPath, String mainIndexPath, String taxoPath, String rakeIndexPath) throws IOException, ClassNotFoundException, URISyntaxException {
        this.reader = LuceneUtils.getReader(mainIndexPath);
        this.searcher = new IndexSearcher(this.reader);

        Fields fields = MultiFields.getFields(this.reader);
        Terms terms = fields.terms(this.fieldName);
        TermsEnum iterator = terms.iterator(null);
        BytesRef byteRef = null;
        while((byteRef = iterator.next()) != null) {
            this.totalVocabulary++;
        }
        this.liveDocs = new BitSet(this.reader.numDocs());
        this.rakeMgr = new RakeCollocationMgr(rakeIndexPath);
    }


    public Map<String, ScoreMap> search(String queryString) throws ParseException, IOException, URISyntaxException {
        Timestamper timestamper = new Timestamper();
        timestamper.loudStart();
        QueryParser queryParser = new QueryParser(Version.LUCENE_48, this.fieldName, new CustomAnalyzer(Version.LUCENE_48));
        Query query = queryParser.parse(queryString);
        TopScoreDocCollector collector = TopScoreDocCollector.create(this.k, true);
        this.searcher.search(query, collector);
        Set<Term> terms = new HashSet<>();
        query.extractTerms(terms);
        TopDocs topDocs = collector.topDocs();

        for(int j=0; j<Math.min(this.k, topDocs.totalHits); j++) {
            this.liveDocs.set(topDocs.scoreDocs[j].doc);
        }

        List<Map<String, ScoreMap>> rets = new ArrayList<>();
        for(Term t : terms) {
            // rakePreprocess(topDocs);
            Map<String, ScoreMap> ret = collocateIndividualTerm(t, topDocs);
            rets.add(ret);
        }
        Map<String, ScoreMap> ret = mergeSearch(rets);

        List<String> termStrs = terms.stream().map(Term::text).collect(Collectors.toList());
        ret.get("terms").excludeMathAny(termStrs);
        ret.get("phrases").excludeMathAll(termStrs);
        timestamper.loudEnd();
        return ret;
    }

    /**
     * Merge either phrases map or terms map
     * @param t
     * @param topDocs
     * @return
     * @throws IOException
     */
    private Map<String, ScoreMap> collocateIndividualTerm(Term t, TopDocs topDocs) throws IOException {
        Map<String, CollocationScorer> termBScores = new HashMap<>();
        Map<String, CollocationScorer> phraseBScores = new HashMap<>();
        for (int j = 0; j < Math.min(this.k, topDocs.totalHits); j++) {
            DocsAndPositionsEnum dpe = MultiFields.getTermPositionsEnum(this.reader, null, this.fieldName, t.bytes());
            int docID = topDocs.scoreDocs[j].doc;
            dpe.advance(docID);
            this.processDocForTerm(t, dpe, termBScores, phraseBScores, true);
        }
        termBScores = this.helper.filterByTermFreq(termBScores, 5);
        phraseBScores = this.helper.filterByTermFreq(phraseBScores, 5);
        ScoreMap sortedTermBScores = ScoreMap.sortScores(termBScores);
        ScoreMap sortedPhraseBScores = ScoreMap.sortScores(phraseBScores);

        Map<String, ScoreMap> ret = new HashMap<>();
        ret.put("terms", sortedTermBScores);
        ret.put("phrases", sortedPhraseBScores);
        logger.info("Search " + t.text() + " completed");
        return ret;
    }

    /**
     * Merge the search results of phrase query
     * @param rets
     * @return
     */
    private Map<String, ScoreMap> mergeSearch(List<Map<String, ScoreMap>> rets) {
        Map<String, ScoreMap> ret = new HashMap<>();
        List<ScoreMap> terms = new ArrayList<>();
        List<ScoreMap> phrases = new ArrayList<>();
        for(Map<String, ScoreMap> r: rets) {
            terms.add(r.get("terms"));
            phrases.add(r.get("phrases"));
        }
        ret.put("terms", ScoreMap.mergeMaps(terms));
        ret.put("phrases", ScoreMap.mergeMaps(phrases));
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
     * extract collocations from all the documents
     * @param t
     * @throws IOException
     * @throws ParseException
     */
    private void extract(Term t) throws IOException, ParseException {
        Timestamper timestamper = new Timestamper();
        timestamper.loudStart();
        Map<String, CollocationScorer> termBScores = processTerm(t);
        termBScores = this.helper.filterByCollocationCount(termBScores, 5);
        ScoreMap.sortScores(termBScores);
        timestamper.loudEnd();
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
        HashMap<String, CollocationScorer> termBScores = new HashMap<>();
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
        int docId = dpeA.docID();
        this.logger.trace("Processing docId: "+docId);

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

        // rake
        rake4j.core.model.Document rake_doc = this.rakeMgr.analyze(this.searcher.doc(docId).get(FieldName.CONTENT));
        // update scorer
        Set<Term> termsFound = new HashSet<>();
        Set<String> phraseFound = new HashSet<>();
        for (int k = 0; (k < dpeA.freq()); k++) {  // for term A
            Integer position = dpeA.nextPosition();
            Integer startpos = Math.max(0, position - this.slopSize);
            Integer endpos = position + this.slopSize;
            for(int curpos = startpos; curpos<=endpos; curpos++) {  // for term B
                if(curpos!=position)
                    incrementCollocationScore(term, termBScores, top, pos2term, termsFound, curpos);
                if(pos2offset.containsKey(curpos))
                    incrementCollocationScore(term, phraseBScores, top, rake_doc, phraseFound, pos2offset.get(curpos).getLeft());
            }
        }
    }

    private void incrementCollocationScore(Term term, Map<String, CollocationScorer> scores, boolean top, Map<Integer, Term> map, Set<Term> termsFound, int cur_position) throws IOException {
        Term termB = map.get(cur_position);  // how to get termB
        if(termB==null)
            return;
        if(termB.bytes().equals(term.bytes()))
            return;
        if(CustomStopAnalyzer.ENGLISH_STOP_WORDS_SET.contains(termB.text()))
            return;
        if (!this.filter.processTerm(termB.bytes().utf8ToString())) {
            return;
        }
        /*
        if (!StringUtils.isAlpha(termB.bytes().utf8ToString())) {
            return;
        }
        */
        if(termsFound.contains(termB))
            return;
        CollocationScorer pt = scores.get(termB.bytes().utf8ToString());

        if (pt==null) {  // if not exist
            float percentB = (float) this.reader.docFreq(termB) / (float) this.reader.numDocs();
            if(this.helper.isTooPopularOrNotPopularEnough(percentB)) {
                termsFound.add(termB);
                return;
            }
            if(top) {
                /* top 100 strategy */
                int dfB = 0;
                DocsAndPositionsEnum dpeB = MultiFields.getTermPositionsEnum(this.reader, null, this.fieldName, termB.bytes());
                while (dpeB.nextDoc()!= DocsEnum.NO_MORE_DOCS) {
                    if(this.liveDocs.get(dpeB.docID()))
                        dfB ++;
                }
                pt = new CollocationScorer(term.text(), termB.text(), this.k,  this.reader.docFreq(termB), this.reader.totalTermFreq(termB), this.reader.numDocs());
            }
            else {
                pt = new CollocationScorer(term.text(), termB.text(), this.reader.docFreq(term), this.reader.docFreq(termB), this.reader.totalTermFreq(termB), this.reader.numDocs());
            }
            scores.put(pt.getCoincidentalTerm(), pt);
        }

        pt.incCoIncidenceDocCount();
        termsFound.add(termB);  // whether to check the same doc multiple times
    }

    private void incrementCollocationScore(Term term, Map<String, CollocationScorer> scores, boolean top, rake4j.core.model.Document doc, Set<String> phrasesFound, int cur_offset) throws IOException {
        if(!doc.getTermMap().containsKey(cur_offset))
            return ;
        String phraseB = doc.getTermMap().get(cur_offset).getTermText();
        this.logger.trace("collocation: "+phraseB);
        if(phraseB==null)
            return;
        if(this.rakeMgr.index.docFreq(phraseB)<=0)
            return ;
        if(phraseB.equals(term.bytes()))
            return;
        if(phrasesFound.contains(phraseB))
            return;
        CollocationScorer pt = scores.get(phraseB);

        if (pt==null) {  // if not exist
            if(top) {
                pt = new CollocationScorer(term.text(), phraseB, this.k, this.rakeMgr.index.docFreq(phraseB), this.rakeMgr.index.totalTermFreq(phraseB), this.reader.numDocs());
            }
            else {
                pt = new CollocationScorer(term.text(), phraseB, this.reader.docFreq(term), this.rakeMgr.index.docFreq(phraseB), this.rakeMgr.index.totalTermFreq(phraseB), this.reader.numDocs());
            }
            scores.put(pt.getCoincidentalTerm(), pt);
        }
        pt.incCoIncidenceDocCount();
        phrasesFound.add(phraseB);  // whether to check the same doc multiple times
    }
}
