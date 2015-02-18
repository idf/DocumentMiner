package km.lucene.indexing;

import io.deepreader.java.commons.util.Displayer;
import km.common.Config;
import km.common.json.JsonReader;
import km.lucene.entities.DocWithTopic;
import km.lucene.entities.Post;
import km.lucene.services.DocWithTopicParser;
import km.lucene.services.ThreadService;
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
import java.util.Map;

public class Indexer extends AbstractIndexer {
    private final static FacetsConfig config = new FacetsConfig();
    private String postPath;
    private String indexPath;
    private String taxoPath;

    public static void main(String[] args) throws IOException {
        // test parameters
        args = new String[3];
        args[0] = Config.settings.getPostsPath();
        args[1] = Config.settings.getIndexPath();
        args[2] = Config.settings.getTaxoindexPath();

        if (args.length < 3) {
            System.out.println("Please specify data file, index folder, taxonomy index folder in sequence.");
            System.exit(1);
        }

        new Indexer(args[0], args[1], args[2]).run();
    }

    public Indexer(String postPath, String indexPath, String taxoPath) {
        this.postPath = postPath;
        this.indexPath = indexPath;
        this.taxoPath = taxoPath;
    }

    @Deprecated
    @Override
    public void run() {
        try {
            ThreadService.init(Config.settings.getThreadsPath());
            Map<Integer, DocWithTopic> docTopics = DocWithTopicParser.parse(Config.settings.getMalletSettings().getTopicsPath());
            logger.info(String.format("Indexing to directory '%s'...", indexPath));
            Directory dir = FSDirectory.open(new File(indexPath));

            IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_48, analyzer);
            iwc.setOpenMode(OpenMode.CREATE);
            IndexWriter indexWriter = new IndexWriter(dir, iwc);
            TaxonomyWriter taxoWriter = new DirectoryTaxonomyWriter(FSDirectory.open(new File(taxoPath)));

            JsonReader<Post> jr = new JsonReader<Post>(postPath, Post.class);
            Post post;
            int i = 1;
            while ((post = jr.next()) != null) {
                Document doc = DocumentFactory.newInstance(post, docTopics);
                indexWriter.addDocument(config.build(taxoWriter, doc));
                logger.info(String.format("added post %d, %d", (i++), post.getId()));
            }
            jr.close();

            logger.info(String.format("Number of files indexed: %d", indexWriter.numDocs()));
            indexWriter.close();
            taxoWriter.close();
        }
        catch (IOException e) {
            logger.error(Displayer.display(e));
        }
    }
}
