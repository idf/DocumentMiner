package km.lucene.app.rake;

import io.deepreader.java.commons.util.Displayer;
import io.deepreader.java.commons.util.IOHandler;
import io.deepreader.java.commons.util.Timestamper;
import km.common.Setting;
import km.common.json.JsonReader;
import km.lucene.entities.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rake4j.core.IndexWriter;
import rake4j.core.RakeAnalyzer;
import rake4j.core.index.Index;
import rake4j.core.model.Document;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * User: Danyang
 * Date: 12/30/2014
 * Time: 20:34
 */
public class RakeIndexer {
    /**
     * Does not need the position of the phrase, just need to store the scores of the words.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        String postPath = Setting.SORTED_POSTS_PATH;
        String indexPath = Setting.RakeSetting.INDEX_PATH; // storing path
        Timestamper timestamper = new Timestamper();
        Index index = new Index();
        Logger logger = LoggerFactory.getLogger(RakeIndexer.class);

        timestamper.start();
        RakeAnalyzer rake = null;
        try {
            rake = new RakeAnalyzer();
            rake.setMinWordsForPhrase(2);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        IndexWriter iw = new IndexWriter(index, rake , (float) 0.8);
        JsonReader<Post> jr = new JsonReader<Post>(postPath, Post.class);
        Post post;

        int i = 1;
        int maxDocs = 1<<31-1;
        while ((post = jr.next()) != null && i<maxDocs) { // debug
            Document doc = new Document(post.getContent());
            iw.addDocument(doc);
            System.out.println(String.format("added post %d, %d", (i++), post.getId()));
        }


        try {
            IOHandler.serialize(indexPath, index);
            logger.info("Serialized data is saved in " + indexPath);
        }
        catch (IOException e) {
            logger.error(Displayer.display(e));
        }

        try {
            Index index1 = (Index) IOHandler.deserialize(indexPath);
            logger.info("Deserialized data is from " + indexPath);
            System.out.println(index1);
        }
        catch (Exception e) {
            logger.error(Displayer.display(e));
        }
        timestamper.end();
    }
}
