package km.lucene.indexing;

import km.common.Settings;
import km.common.util.DateUtil;
import km.common.util.StringUtil;
import km.lucene.analysis.CustomAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.facet.*;
import org.apache.lucene.facet.taxonomy.FastTaxonomyFacetCounts;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyReader;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class DrillDownSearch {

	private static final FacetsConfig config = new FacetsConfig();
	private static final String[] categories = { "forumId", "poster", "postDate" };
	private static final Map<Integer, String> forums = new HashMap<>();

	public static void main(String[] args) throws IOException, ParseException {
		// test parameters
		args = new String[2];
        args[0] = Settings.INDEX_PATH;
        args[1] = Settings.TAXOINDEX_PATH;

		if (args.length < 2) {
			System.out.println("Please specify the index folder, taxonomy index folder in sequence.");
			System.exit(1);
		}

		String indexPath = args[0];
		String taxoPath = args[1];

		forums.put(215, "Post-Degree Programs & Courses");
		forums.put(70, "Degree Programs and Courses");
		forums.put(67, "Diploma Programs and Courses");
		forums.put(68, "Certified Systems, IT Security and Network Training");
		forums.put(69, "Other Academic Concerns");
		forums.put(180, "Campus Zone");
		forums.put(304, "NTU");
		forums.put(305, "NUS");
		forums.put(344, "SMU");
		forums.put(306, "Textbook Garage");

		int maxFacetItem = 10;
		int pageSize = 10;
		int page = 1;
		int topN = page * pageSize;

		IndexReader indexReader = DirectoryReader.open(FSDirectory.open(new File(indexPath)));
		IndexSearcher searcher = new IndexSearcher(indexReader);
		TaxonomyReader taxoReader = new DirectoryTaxonomyReader(FSDirectory.open(new File(taxoPath)));
		config.setHierarchical("postDate", true);

		String keyword = "ntu";
		boolean includeQuotation = true;
		int[] forumIds = {};
		Calendar c = Calendar.getInstance();
		c.set(2013, 0, 1);
		Date dateFrom = null;
		c.set(2013, 11, 31);
		Date dateTo = null;
		Map<String, String[]> drilldowns = new HashMap<>();
		drilldowns.put("postDate", new String[] { "2014" });
		// drilldowns.put("poster", new String[] { "mgx-alander" });
		Query query = createQuery(keyword, includeQuotation, forumIds, dateFrom, dateTo, drilldowns);

		FacetsCollector fc = new FacetsCollector();
		Filter filter = NumericRangeFilter.newIntRange("forumId", 0, 99999, true, true);
		// SortField formIdAsc = new SortField("forumId", SortField.Type.INT);
		// SortField formIdDesc = new SortField("forumId", SortField.Type.INT, true);
		// SortField postDateAsc = new SortField("postDate", SortField.Type.LONG);
		// SortField postDateDesc = new SortField("postDate", SortField.Type.LONG, true);
		Sort sort = new Sort(SortField.FIELD_SCORE);
		boolean doDocScores = true;
		boolean doMaxScore = false;
		TopDocs searchResults = FacetsCollector.search(searcher, query, filter, topN, sort, doDocScores, doMaxScore, fc);
		Facets facets = new FastTaxonomyFacetCounts(taxoReader, config, fc);
		List<FacetResult> taxoResults = new ArrayList<>();
		for (int i = 0; i < categories.length; i++) {
			if (categories[i].equals("postDate")) {
				if (drilldowns.containsKey("postDate")) {
					String[] values = drilldowns.get("postDate");
					if (values.length == 1) {
						String value = values[0];
						String[] items = value.split("/");
						switch (items.length) {
						case 1:
							taxoResults.add(facets.getTopChildren(maxFacetItem, categories[i], items[0]));
							break;
						case 2:
							taxoResults.add(facets.getTopChildren(maxFacetItem, categories[i], items[0], items[1]));
							break;
						case 3:
							taxoResults.add(facets.getTopChildren(maxFacetItem, categories[i], items[0], items[1], items[2]));
							break;
						}
					}
				}
				taxoResults.add(facets.getTopChildren(maxFacetItem, categories[i]));
			} else {
				taxoResults.add(facets.getTopChildren(maxFacetItem, categories[i]));
			}
		}

		printSearchResult(searcher, searchResults, pageSize, page);
		printTaxoResult(taxoResults, maxFacetItem);

		indexReader.close();
		taxoReader.close();
	}

	private static Query createQuery(String keyword, boolean includeQuotation, int[] forumIds, Date dateFrom, Date dateTo, Map<String, String[]> drilldowns) throws ParseException {

		BooleanQuery query = new BooleanQuery();

		if (keyword != null && !keyword.isEmpty()) {
			BooleanQuery queryCQC = new BooleanQuery();
			Analyzer analyzer = new CustomAnalyzer(Version.LUCENE_48);
			QueryParser parserC = new QueryParser(Version.LUCENE_48, "content", analyzer);
			Query queryC = parserC.parse(keyword);
			queryCQC.add(queryC, Occur.SHOULD);
			if (includeQuotation) {
				QueryParser parserQC = new QueryParser(Version.LUCENE_48, "quoteContent", analyzer);
				Query queryQC = parserQC.parse(keyword);
				queryCQC.add(queryQC, Occur.SHOULD);
			}
			query.add(queryCQC, Occur.MUST);
		}

		if (forumIds.length > 0) {
			BooleanQuery queryF = new BooleanQuery();
			for (int i = 0; i < forumIds.length; i++) {
				queryF.add(NumericRangeQuery.newIntRange("forumId", forumIds[i], forumIds[i], true, true), Occur.SHOULD);
			}
			query.add(queryF, Occur.MUST);
		}

		if (dateFrom != null || dateTo != null) {
			long dateStart = 0;
			if (dateFrom != null) {
				dateStart = dateFrom.getTime();
			}
			long dateEnd = Long.MAX_VALUE;
			if (dateTo != null) {
				dateEnd = dateTo.getTime() + 1000 * 60 * 60 * 24 - 1;
			}
			Query queryPD = NumericRangeQuery.newLongRange("postDate", dateStart, dateEnd, true, true);
			query.add(queryPD, Occur.MUST);
		}

		if (!drilldowns.isEmpty()) {
			DrillDownQuery queryD = new DrillDownQuery(config);
			for (Entry<String, String[]> entry : drilldowns.entrySet()) {
				String key = entry.getKey();
				String[] values = entry.getValue();
				for (int i = 0; i < values.length; i++) {
					if (key.equals("postDate")) {
						String[] items = values[i].split("/");
						switch (items.length) {
						case 1:
							queryD.add(key, items[0]);
							break;
						case 2:
							queryD.add(key, items[0], items[1]);
							break;
						case 3:
							queryD.add(key, items[0], items[1], items[2]);
							break;
						}
					} else {
						queryD.add(key, values[i]);
					}
				}
			}
			query.add(queryD, Occur.MUST);
		}

		if (query.clauses().isEmpty()) {
			return new MatchAllDocsQuery();
		}

		return query;
	}

	private static void printSearchResult(IndexSearcher searcher, TopDocs results, int pageSize, int page) throws IOException {
		ScoreDoc[] hits = results.scoreDocs;
		int start = pageSize * (page - 1);
		int end = pageSize * page;
		end = Math.min(end, hits.length);
		System.out.println(String.format("Only %d-%d of %d total matching documents collected.", (start + 1), end, results.totalHits));
		for (int i = start; i < end; i++) {
			float score = hits[i].score;
			Document doc = searcher.doc(hits[i].doc);
			int forumId = Integer.parseInt(doc.get("forumId"));
			int id = Integer.parseInt(doc.get("id"));
			long postDate = Long.parseLong(doc.get("postDate"));
			Date date = new Date(postDate);
			String dateStr = DateUtil.format(date, "yyyy-MM-dd HH:mm:ss");
			String content = doc.get("content");
			content = StringUtil.toFixLengh(content, 50);
			System.out.println(String.format("[%d] %d, %d, %s, %.2f, %s", (i + 1), forumId, id, dateStr, score, content));

			IndexableField[] quoteIds = doc.getFields("quoteId");
			if (quoteIds.length > 0) {
				System.out.println(String.format("\tquotes: %d", quoteIds.length));
				IndexableField[] quotePosters = doc.getFields("quotePoster");
				IndexableField[] quoteContents = doc.getFields("quoteContent");
				for (int j = 0; j < quoteIds.length; j++) {
					int quoteId = (Integer) quoteIds[j].numericValue();
					String quotePoster = quotePosters[j].stringValue();
					String quoteContent = quoteContents[j].stringValue();
					quoteContent = StringUtil.toFixLengh(quoteContent, 50);
					System.out.println(String.format("\t%d, %s: %s", quoteId, quotePoster, quoteContent));
				}
			}
		}
	}

	private static void printTaxoResult(List<FacetResult> taxoResults, int maxFacetItem) {
		for (FacetResult result : taxoResults) {
			if (result == null) {
				continue;
			}

			int max = Math.min(maxFacetItem, result.childCount);
			String dim = result.dim;
			String[] path = result.path;
			if (path.length > 0) {
				dim += " " + Arrays.toString(path);
			}
			System.out.println(String.format("%s children: %d of %d, %d", dim, max, result.childCount, result.value));
			for (int i = 0; i < max; i++) {
				LabelAndValue lav = result.labelValues[i];
				String label = lav.label;
				if (dim.equals("forumId")) {
					label = label + ", " + forums.get(Integer.parseInt(label));
				}
				Number value = lav.value;
				System.out.println(String.format("%s (%d)", label, value));
			}
		}
	}
}
