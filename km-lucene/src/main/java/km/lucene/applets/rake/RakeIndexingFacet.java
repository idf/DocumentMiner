package km.lucene.applets.rake;

import io.deepreader.java.commons.util.Displayer;
import io.deepreader.java.commons.util.IOHandler;
import io.deepreader.java.commons.util.Transformer;
import km.common.Settings;
import km.common.json.JsonReader;
import km.lucene.constants.FieldName;
import km.lucene.entities.Post;
import org.apache.lucene.index.IndexReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.LuceneUtils;

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
 *
 * Notice:
 * 1. Added delimiters between documents since not considering offset information.
 */
public class RakeIndexingFacet implements Runnable {
    String postPath = Settings.SORTED_POSTS_PATH;
    private Logger logger = LoggerFactory.getLogger(RakeIndexingFacet.class);
    /**
     * Does not need the position of the phrase, just need to store the scores of the words.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) {
        new RakeIndexingFacet().run();
    }

    @Override
    public void run() {
        try {
            clusteredIndexing();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void basicIndexing() throws IOException {
        JsonReader<Post> jr = new JsonReader<>(postPath, Post.class);
        final String INDEX_PATH = Settings.RakeSettings.BASIC_INDEX_PATH;
        Iterator<String> itr = jr.getList().parallelStream()
                .map(e -> e.getContent())
                .iterator();
        jr.close();
        RakeIndexer indexer = new RakeIndexer(INDEX_PATH, itr);
        indexer.run();
    }

    public void threadedIndexing() throws IOException {
        JsonReader<Post> jr = new JsonReader<>(postPath, Post.class);
        final String INDEX_PATH = Settings.RakeSettings.THREADED_INDEX_PATH;

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
        RakeIndexer indexer = new RakeIndexer(INDEX_PATH, lst.iterator());
        indexer.run();
    }


    /**
     * Notice the Cluto output file format as specified in Cluto manual
     * Cluster -1 means no group
     * @throws java.io.IOException
     */
    public void clusteredIndexing() throws IOException {
        clusteredIndexing(Settings.ClutoSettings.OUTPUT,
                Settings.THINDEX_PATH,
                Settings.RakeSettings.CLUSTERED_INDEX_PATH
        );
    }

    public void clusteredIndexing(final String clusterPath, final String indexPath, final String rakeIndexPath) throws IOException {
        // Cluto interface
        Stream<String> lines = IOHandler.getLines(clusterPath);
        List<Integer> nums = lines.map(Integer::parseInt).collect(Collectors.toList());
        Map<Integer, List<Integer>> cluster2docs = Transformer.groupListToMap(nums);
        logger.trace(Displayer.display(cluster2docs));

        // indexing
        List<String> lst = new ArrayList<>();
        IndexReader reader =  LuceneUtils.getReader(indexPath);
        for(Map.Entry<Integer, List<Integer>> e: cluster2docs.entrySet()) {
            if(e.getKey()==-1) {  // unclustered
                for(Integer i: e.getValue()) {
                    lst.add(LuceneUtils.getAllStringValues(reader.document(i), FieldName.CONTENT, "\n"));
                }
            }
            else {
                StringBuilder sb = new StringBuilder();
                for(Integer i: e.getValue()) {
                    sb.append(lst.add(LuceneUtils.getAllStringValues(reader.document(i), FieldName.CONTENT, "\n")));
                }
                lst.add(sb.toString());
            }
        }
        logger.debug("Total document length: "+lst.parallelStream().map(String::length).reduce(0, Integer::sum).toString());
        RakeIndexer indexer = new RakeIndexer(rakeIndexPath, lst.iterator());
        indexer.run();
    }
}