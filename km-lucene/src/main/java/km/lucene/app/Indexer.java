package km.lucene.app;

import km.common.Settings;
import km.common.json.JsonReader;
import km.lucene.analysis.CustomAnalyzer;
import km.lucene.entities.DocWithTopic;
import km.lucene.entities.Post;
import km.lucene.index.DocumentFactory;
import km.lucene.services.DocWithTopicParser;
import km.lucene.services.ThreadService;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Map;

public class Indexer {

    private final static FacetsConfig config = new FacetsConfig();

    public static void main(String[] args) throws IOException, ParseException {
        ThreadService.init(Settings.THREADS_PATH);
        Map<Integer, DocWithTopic> docTopics = DocWithTopicParser.parse(Settings.MalletSettings.TOPICS_PATH);

        // test parameters
        args = new String[3];
        args[0] = Settings.POSTS_PATH;
        args[1] = Settings.INDEX_PATH;
        args[2] = Settings.TAXOINDEX_PATH;

        if (args.length < 3) {
            System.out.println("Please specify data file, index folder, taxonomy index folder in sequence.");
            System.exit(1);
        }

        String postPath = args[0];
        String indexPath = args[1];
        String taxoPath = args[2];

        System.out.println(String.format("Indexing to directory '%s'...", indexPath));
        Directory dir = FSDirectory.open(new File(indexPath));

        Analyzer analyzer = new CustomAnalyzer(Version.LUCENE_48);
        IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_48, analyzer);
        iwc.setOpenMode(OpenMode.CREATE);
        IndexWriter indexWriter = new IndexWriter(dir, iwc);
        TaxonomyWriter taxoWriter = new DirectoryTaxonomyWriter(FSDirectory.open(new File(taxoPath)));

        JsonReader<Post> jr = new JsonReader<Post>(postPath, Post.class);
        Post post;
        int i = 1;
        while ((post = jr.next()) != null) {
            Document doc = DocumentFactory.create(post, docTopics);
            indexWriter.addDocument(config.build(taxoWriter, doc));
            System.out.println(String.format("added post %d, %d", (i++), post.getId()));
        }
        jr.close();

        System.out.println(String.format("Number of files indexed: %d", indexWriter.numDocs()));
        indexWriter.close();
        taxoWriter.close();
    }
}
