package km.lucene.app.rake;

import km.common.Settings;
import km.common.json.JsonReader;
import km.lucene.entities.Post;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * User: Danyang
 * Date: 12/30/2014
 * Time: 20:34
 */
public class RakeIndexingFacet {
    String postPath = Settings.SORTED_POSTS_PATH;

    /**
     * Does not need the position of the phrase, just need to store the scores of the words.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        new RakeIndexingFacet().threadedIndexing();
    }


    private void basicIndexing() throws IOException {
        JsonReader<Post> jr = new JsonReader<Post>(postPath, Post.class);
        String indexPath = Settings.RakeSettings.BASIC_INDEX_PATH;
        Iterator<String> itr = jr.getList().parallelStream()
                .map(e -> e.getContent())
                .iterator();
        jr.close();
        RakeIndexer indexer = new RakeIndexer(indexPath, itr);
        indexer.run();
    }

    private void threadedIndexing() throws IOException {
        JsonReader<Post> jr = new JsonReader<Post>(postPath, Post.class);
        String indexPath = Settings.RakeSettings.THREADED_INDEX_PATH;

        List<String> lst = new ArrayList<>();
        int prevThreadId = 0;
        Post post;
        StringBuffer sb = null;
        while ((post = jr.next())!=null) {
            if (post.getThreadId()!=prevThreadId) {
                if (sb!=null) {
                    lst.add(sb.toString());
                }
                sb = new StringBuffer();
                prevThreadId = post.getThreadId();
            }
            sb.append(post.getContent());
        }
        jr.close();
        lst.add(sb.toString());


        RakeIndexer indexer = new RakeIndexer(indexPath, lst.iterator());
        indexer.run();
    }

    private void clusteredIndexing() throws IOException {
        // Cluto interface


    }
}
