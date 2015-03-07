package km.lucene.indexing;

import io.deepreader.java.commons.util.Displayer;
import km.common.Config;
import km.common.json.JsonReader;
import km.lucene.entities.DocWithTopic;
import km.lucene.entities.Post;
import km.lucene.services.DocWithTopicParser;
import km.lucene.services.ThreadService;
import org.apache.lucene.document.Document;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * User: Danyang
 * Date: 1/28/2015
 * Time: 20:46
 */
public class PostIndexer extends AbstractIndexer {
    public static void main(String[] args) {
        new PostIndexer().run();
    }

    public PostIndexer() {
        super();
        ThreadService.init(Config.settings.getThreadsPath());
    }

    @Override
    public void run() {
        try {
            Map<Integer, DocWithTopic> docTopics = DocWithTopicParser.parse(Config.settings.getMalletSettings().getTopicsPath());
            TaxonomyWriter taxoWriter = new DirectoryTaxonomyWriter(FSDirectory.open(new File(taxoPath)));

            logger.info(String.format("Indexing to directory '%s'...", indexPath));
            Directory dir = FSDirectory.open(new File(indexPath));
            IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_48, analyzer);
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            IndexWriter indexWriter = new IndexWriter(dir, iwc);

            JsonReader<Post> jr = new JsonReader<>(postPath, Post.class);
            Post post;
            int i = 1;
            while ((post = jr.next())!=null) {
                Document doc = DocumentFactory.newInstance(post, docTopics);
                indexWriter.addDocument(config.build(taxoWriter, doc));
                if(i%1000==0) logger.info(String.format("added post %d, %d", (i++), post.getId()));
            }  // END for posts
            jr.close();
            logger.info(String.format("Number of files indexed: %d", indexWriter.numDocs()));
            indexWriter.close();
        }
        catch (IOException e) {
            logger.error(Displayer.display(e));
        }
    }
}
