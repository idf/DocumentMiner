package km.common;

/**
 * “Measuring programming progress by lines of code is like measuring aircraft building progress by weight.”
 * - Bill Gates
 * User: Danyang
 * Date: 8/27/14
 * Time: 3:50 PM
 */
public class Setting {
    public static final String DATA_FOLDER = "D:/Programming/java/kd/data/";

    public static final String POSTS_PATH = DATA_FOLDER +"posts.txt";
    public static final String SORTED_POSTS_PATH = DATA_FOLDER +"posts_sorted.txt";
    public static final String FORUMS_PATH = DATA_FOLDER +"forums.txt";
    public static final String CONTENTS_PATH = DATA_FOLDER+"contents.txt";
    public static final String THREADS_PATH = DATA_FOLDER+"threads.txt";

    public static final String RAW_HTML_FOLDER = "D:/Programming/java/kd/html/";

    public class MalletSetting {
        public static final String ROOT_FOLDER = DATA_FOLDER +"mallet/";
        public static final String POSTS_FOLDER = ROOT_FOLDER+"posts/";
        public static final String OAC_69_FOLDER = ROOT_FOLDER+"69_OAC/";
        public static final String POSTS_PATH = ROOT_FOLDER +"posts.csv";
        public static final String POSTS_MALLET_PATH = ROOT_FOLDER+"posts.mallet";

        public static final int TOPIC_CNT = 50;
        public static final String TOPICS_PATH = ROOT_FOLDER+TOPIC_CNT+"_topics.txt";
        public static final String KEYS_PATH = ROOT_FOLDER+TOPIC_CNT+"_keys.txt";
    }

    // Lucene
    public static final String INDEX_PATH = DATA_FOLDER+"index";
    public static final String TAXOINDEX_PATH = DATA_FOLDER+"taxoindex";
    public static final String THINDEX_PATH = DATA_FOLDER+"thindex";

    public class TopicSetting {
        public static final String ROOT_FOLDER = DATA_FOLDER+"topic/";
        public static final String TERMS_PATH = ROOT_FOLDER+"terms.json";
    }

    // RAKE
    public class RakeSetting {
        public static final String ROOT_FOLDER = DATA_FOLDER+"rake/";
        public static final String INDEX_PATH = ROOT_FOLDER+"index.ser";
    }

}
