package km.common;

import javax.xml.bind.annotation.*;

/**
 * User: Danyang
 * Date: 8/27/14
 * Time: 3:50 PM
 */
@XmlRootElement
public class Settings {
    // Raw Data
    @XmlElement
    public static final String DATA_FOLDER = "D:/Programming/java/kd/data/";
    @XmlElement
    public static final String POSTS_PATH = DATA_FOLDER +"posts.txt";
    @XmlElement
    public static final String SORTED_POSTS_PATH = DATA_FOLDER +"posts_sorted.txt";
    @XmlElement
    public static final String FORUMS_PATH = DATA_FOLDER +"forums.txt";
    @XmlElement
    public static final String CONTENTS_PATH = DATA_FOLDER+"contents.txt";
    @XmlElement
    public static final String THREADS_PATH = DATA_FOLDER+"threads.txt";
    @XmlElement
    public static final String RAW_HTML_FOLDER = "D:/Programming/java/kd/html/";

    @XmlElement
    public static final MalletSettings MalletSettings = new MalletSettings();
    @XmlElement
    public static final TopicSettings TopicSettings = new TopicSettings();
    @XmlElement
    public static final RakeSettings RakeSettings = new RakeSettings();
    @XmlElement
    public static final ClutoSettings ClutoSettings = new ClutoSettings();
    @XmlElement
    public static final DriverSettings DriverSettings = new DriverSettings();


    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class MalletSettings {
        @XmlElement
        public static final String ROOT_FOLDER = DATA_FOLDER +"mallet/";
        @XmlElement
        public static final String POSTS_FOLDER = ROOT_FOLDER+"posts/";
        @XmlElement
        public static final String OAC_69_FOLDER = ROOT_FOLDER+"69_OAC/";
        @XmlElement
        public static final String POSTS_PATH = ROOT_FOLDER +"posts.csv";
        @XmlElement
        public static final String POSTS_MALLET_PATH = ROOT_FOLDER+"posts.mallet";

        @XmlElement
        public static final int TOPIC_CNT = 50;
        @XmlElement
        public static final String TOPICS_PATH = ROOT_FOLDER+TOPIC_CNT+"_topics.txt";
        @XmlElement
        public static final String KEYS_PATH = ROOT_FOLDER+TOPIC_CNT+"_keys.txt";
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class TopicSettings {
        @XmlElement
        public static final String ROOT_FOLDER = DATA_FOLDER+"topic/";
        @XmlElement
        public static final String TERMS_PATH = ROOT_FOLDER+"terms.json";
    }

    // Lucene
    @XmlElement
    public static final String INDEX_PATH = DATA_FOLDER+"index";
    @XmlElement
    public static final String TAXOINDEX_PATH = DATA_FOLDER+"taxoindex";
    @XmlElement
    public static final String THINDEX_PATH = DATA_FOLDER+"thindex";
    @XmlElement
    public static final String POSTINDEX_PATH = DATA_FOLDER+"postindex";
    @XmlElement
    public static final int COLLO_TOP_K = 500;

    // RAKE
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class RakeSettings {
        @XmlElement
        public static final float TOP_PERCENT = 0.75f; // 0.3333f;  // one-third the number of words in the graph. (Rose et al.)
        @XmlElement
        public static final String ROOT_FOLDER = DATA_FOLDER+"rake/";
        @XmlElement
        public static final String BASIC_INDEX_PATH = ROOT_FOLDER+"basic_index.ser";
        @XmlElement
        public static final String THREADED_INDEX_PATH = ROOT_FOLDER+"threaded_index.ser";
        @XmlElement
        public static final String CLUSTERED_INDEX_PATH = ROOT_FOLDER+"clustered_index.ser";
    }

    // CLUTO
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ClutoSettings {
        @XmlElement
        public static final String ROOT_FOLDER = DATA_FOLDER+"cluto/";
        @XmlElement
        public static final String DOCS_MAT = ROOT_FOLDER+"docs.mat";
        @XmlElement
        public static final String VCLUSTER = "D:/Programming/java/kd/cluto/cluto-2.1.2/MSWIN-x86_64/vcluster.exe";
        @XmlElement
        public static final String OUTPUT = ROOT_FOLDER+"docs-output.txt";
    }

    // Driver
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class DriverSettings {
        @XmlElement
        public static final String ROOT_FOLDER = DATA_FOLDER+"expr/";
    }

}
