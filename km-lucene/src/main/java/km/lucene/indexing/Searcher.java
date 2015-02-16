package km.lucene.indexing;

import io.deepreader.java.commons.util.DateUtils;
import km.common.Config;
import km.lucene.analysis.CustomAnalyzer;
import km.lucene.constants.FieldName;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.facet.*;
import org.apache.lucene.facet.taxonomy.FastTaxonomyFacetCounts;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyReader;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class Searcher {
	private static final FacetsConfig config = new FacetsConfig();
	
	public static void main(String[] args) throws IOException, ParseException {
		// test parameters
		args = new String[3];
		args[0] = Config.settings.getIndexPath();
		args[1] = Config.settings.getThindexPath();
		args[2] = Config.settings.getTaxoindexPath();

		if (args.length < 3) {
			System.out.println("Please specify data file, index folder, taxonomy index folder in sequence.");
			System.exit(1);
		}

		String indexPath = args[0];
		String thindexPath = args[1];
		String taxoPath = args[2];

		IndexReader indexReader = DirectoryReader.open(FSDirectory.open(new File(indexPath)));
		IndexSearcher searcher = new IndexSearcher(indexReader);
		IndexReader thindexReader = DirectoryReader.open(FSDirectory.open(new File(thindexPath)));
		IndexSearcher thsearcher = new IndexSearcher(thindexReader);
		TaxonomyReader taxoReader = new DirectoryTaxonomyReader(FSDirectory.open(new File(taxoPath)));

		Analyzer analyzer = new CustomAnalyzer(Version.LUCENE_48);
		
		// content
		QueryParser parserContent = new QueryParser(Version.LUCENE_48, FieldName.CONTENT, analyzer);
		Query content = parserContent.parse("nus");

		//thread title
		QueryParser parserThreadTitle = new QueryParser(Version.LUCENE_48, FieldName.THREAD_TITLE, analyzer);
		Query threadTitle = parserThreadTitle.parse("nus");

		//post date
		Date dateFrom = DateUtils.parse("2014-01-01", "yyyy-MM-dd");
		Date dateTo = DateUtils.parse("2014-08-31", "yyyy-MM-dd");
		Long start = null;
		Long end = null;
		if (dateFrom != null) {
			start = dateFrom.getTime();
		}
		if (dateTo != null) {
			end = DateUtils.addDay(dateTo, 1).getTime() - 1;
		}
		Query postDate = NumericRangeQuery.newLongRange(FieldName.POST_DATE, start, end, true, true);
		
		//post month
		Query postMonthTh = NumericRangeQuery.newIntRange(FieldName.POST_MONTH, 1, 1, true, true);
		DrillDownQuery postMonth = new DrillDownQuery(config);
		postMonth.add(FieldName.POST_MONTH, "01");

		//post year
		Query postYearTh = NumericRangeQuery.newIntRange(FieldName.POST_YEAR, 2014, 2014, true, true);
		DrillDownQuery postYear = new DrillDownQuery(config);
		postYear.add(FieldName.POST_YEAR, "2014");
		
		//topic
		Query topicTh = NumericRangeQuery.newIntRange(FieldName.TOPIC_ID, 0, 0, true, true);
		DrillDownQuery topic = new DrillDownQuery(config);
		topic.add(FieldName.TOPIC_ID, "0");
		
		//forum
		Query forumTh = NumericRangeQuery.newIntRange(FieldName.FORUM_ID, 304, 304, true, true);
		DrillDownQuery forum = new DrillDownQuery(config);
		forum.add(FieldName.FORUM_ID, "304");
		
		//thread
		Query threadTh = NumericRangeQuery.newIntRange(FieldName.THREAD_ID, 4644213, 4644213, true, true);
		DrillDownQuery thread = new DrillDownQuery(config);
		thread.add(FieldName.THREAD_ID, "4644213");
		
		//poster
		BooleanQuery posterTh = new BooleanQuery();
		//posterTh.add(new TermQuery(new Term(FieldName.POSTER, "JinYuan")), Occur.SHOULD);
		posterTh.add(new TermQuery(new Term(FieldName.POSTER, "vbhelper")), Occur.SHOULD);
		DrillDownQuery poster = new DrillDownQuery(config);
		//poster.add(FieldName.POSTER, "JinYuan");
		poster.add(FieldName.POSTER, "vbhelper");

		//thead query
		BooleanQuery thquery = new BooleanQuery();
		thquery.add(content, Occur.MUST);
		thquery.add(posterTh, Occur.MUST);

		//post query
		BooleanQuery query = new BooleanQuery();
		query.add(content, Occur.MUST);
		query.add(poster, Occur.MUST);

		TopDocs topDocs = thsearcher.search(thquery, Integer.MAX_VALUE);
		System.out.println("total threads: " + topDocs.totalHits);

		Filter filter = null;
		FacetsCollector fc = new FacetsCollector();
		searcher.search(query, filter, fc);
		FacetsConfig config = new FacetsConfig();
		Facets facets = new FastTaxonomyFacetCounts(taxoReader, config, fc);
		FacetResult fr = facets.getTopChildren(10, FieldName.THREAD_ID);
		System.out.println("total thread by facets: " + fr.childCount);
	}
}
