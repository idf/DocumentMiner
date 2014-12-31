package km.lucene.app.rake;

import km.common.Setting;
import km.common.json.JsonReader;
import km.lucene.entities.Post;

import java.io.IOException;

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
        String indexPath = ""; // storing path 
        
        JsonReader<Post> jr = new JsonReader<Post>(postPath, Post.class);
        Post post;
    }
}
