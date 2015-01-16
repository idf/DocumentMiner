package km.lucene.app.rake;

import io.deepreader.java.commons.util.Displayer;
import io.deepreader.java.commons.util.IOHandler;
import io.deepreader.java.commons.util.Transformer;
import km.common.Settings;
import km.common.json.JsonReader;
import km.lucene.constants.FieldName;
import km.lucene.entities.Post;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * User: Danyang
 * Date: 12/30/2014
 * Time: 20:34
 */
public class RakeIndexingFacet {
    String postPath = Settings.SORTED_POSTS_PATH;
    private Logger logger = LoggerFactory.getLogger(RakeIndexingFacet.class);
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

        logger.debug("Total document length: "+lst.parallelStream().map(String::length).reduce(0, Integer::sum).toString());
        RakeIndexer indexer = new RakeIndexer(indexPath, lst.iterator());
        indexer.run();
    }

    /**
     * Notice the Cluto output file format as specified in Cluto manual
     * Cluster -1 means no group
     * @throws IOException
     */
    private void clusteredIndexing() throws IOException {
        // Cluto interface
        Stream<String> lines = IOHandler.getLines(Settings.ClutoSettings.OUTPUT);
        List<Integer> nums = lines.map(Integer::parseInt).collect(Collectors.toList());
        Map<Integer, List<Integer>> cluster2docs = Transformer.groupListToMap(nums);
        logger.trace(Displayer.display(cluster2docs));

        // indexing
        List<String> lst = new ArrayList<>();
        String indexPath = Settings.THINDEX_PATH;
        String rakeIndexPath = Settings.RakeSettings.CLUSTERED_INDEX_PATH;
        IndexReader reader =  DirectoryReader.open(FSDirectory.open(new File(indexPath)));
        for(Map.Entry<Integer, List<Integer>> e: cluster2docs.entrySet()) {
            if(e.getKey()==-1) { // unclustered
                for(Integer i: e.getValue()) {
                    lst.add(reader.document(i).get(FieldName.CONTENT));  // TODO original document text, 924256<<41903447
                }
            }
            else {
                StringBuilder sb = new StringBuilder();
                for(Integer i: e.getValue()) {
                    sb.append(reader.document(i).get(FieldName.CONTENT)).append("\n");
                }
                lst.add(sb.toString());
            }

        }

        logger.debug("Total document length: "+lst.parallelStream().map(String::length).reduce(0, Integer::sum).toString());
        RakeIndexer indexer = new RakeIndexer(rakeIndexPath, lst.iterator());
        indexer.run();
    }
}
