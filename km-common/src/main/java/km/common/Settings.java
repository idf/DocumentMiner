package km.common;

/**
 * User: Danyang
 * Date: 8/27/14
 * Time: 3:50 PM
 */
public class Settings {
    // Raw Data
    public static final String DATA_FOLDER = "D:/Programming/java/kd/data/";

    public static final String POSTS_PATH = DATA_FOLDER +"posts.txt";
    public static final String SORTED_POSTS_PATH = DATA_FOLDER +"posts_sorted.txt";
    public static final String FORUMS_PATH = DATA_FOLDER +"forums.txt";
    public static final String CONTENTS_PATH = DATA_FOLDER+"contents.txt";
    public static final String THREADS_PATH = DATA_FOLDER+"threads.txt";

    public static final String RAW_HTML_FOLDER = "D:/Programming/java/kd/html/";

    public class MalletSettings {
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
    public static final String POSTINDEX_PATH = DATA_FOLDER+"postindex";

    public class TopicSettings {
        public static final String ROOT_FOLDER = DATA_FOLDER+"topic/";
        public static final String TERMS_PATH = ROOT_FOLDER+"terms.json";
    }

    // RAKE
    public class RakeSettings {
        public static final float TOP_PERCENT = 0.3333f;  // one-third the number of words in the graph. (Rose et al.)
        public static final String ROOT_FOLDER = DATA_FOLDER+"rake/";
        public static final String BASIC_INDEX_PATH = ROOT_FOLDER+"basic_index.ser";
        public static final String THREADED_INDEX_PATH = ROOT_FOLDER+"threaded_index.ser";
        public static final String CLUSTERED_INDEX_PATH = ROOT_FOLDER+"clustered_index.ser";
    }

    // CLUTO
    public class ClutoSettings {
        public static final String ROOT_FOLDER = DATA_FOLDER+"cluto/";
        public static final String DOCS_MAT = ROOT_FOLDER+"docs.mat";
        public static final String VCLUSTER = "D:/Programming/java/kd/cluto/cluto-2.1.2/MSWIN-x86_64/vcluster.exe";
        public static final String OUTPUT = ROOT_FOLDER+"docs-output.txt";
    }

}
