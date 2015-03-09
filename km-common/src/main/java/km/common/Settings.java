package km.common;

import io.deepreader.java.commons.util.Displayer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.net.URISyntaxException;

/**
 * http://stackoverflow.com/questions/9660091/jaxb-is-there-some-way-to-unmarshal-static-variables
 * http://stackoverflow.com/questions/8284342/marshal-inner-class-with-jaxb-java
 *
 * User: Danyang
 * Date: 8/27/14
 * Time: 3:50 PM
 */
@XmlRootElement
public class Settings {
    Settings() {}
    // Raw Data
    static String DATA_FOLDER = "D:/Programming/java/kd/data/";
    static String POSTS_PATH = DATA_FOLDER +"posts.txt";
    static String SORTED_POSTS_PATH = DATA_FOLDER +"posts_sorted.txt";
    static String FORUMS_PATH = DATA_FOLDER +"forums.txt";
    static String CONTENTS_PATH = DATA_FOLDER+"contents.txt";
    static String THREADS_PATH = DATA_FOLDER+"threads.txt";
    static String RAW_HTML_FOLDER = "D:/Programming/java/kd/html/";

    static MalletSettings malletSettings = new MalletSettings();
    static TopicSettings topicSettings = new TopicSettings();
    static RakeSettings rakeSettings = new RakeSettings();
    static ClutoSettings clutoSettings = new ClutoSettings();
    static DriverSettings driverSettings = new DriverSettings();


    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class MalletSettings {
        static String ROOT_FOLDER = DATA_FOLDER +"mallet/";
        static String POSTS_FOLDER = ROOT_FOLDER+"posts/";
        static String OAC_69_FOLDER = ROOT_FOLDER+"69_OAC/";
        static String POSTS_PATH = ROOT_FOLDER +"posts.csv";
        static String POSTS_MALLET_PATH = ROOT_FOLDER+"posts.mallet";

        static int TOPIC_CNT = 50;
        static String TOPICS_PATH = ROOT_FOLDER+TOPIC_CNT+"_topics.txt";
        static String KEYS_PATH = ROOT_FOLDER+TOPIC_CNT+"_keys.txt";

        public String getRootFolder() {
            return ROOT_FOLDER;
        }

        @XmlElement
        public void setRootFolder(String rootFolder) {
            ROOT_FOLDER = rootFolder;
        }

        public String getPostsFolder() {
            return POSTS_FOLDER;
        }

        @XmlElement
        public void setPostsFolder(String postsFolder) {
            POSTS_FOLDER = postsFolder;
        }

        public String getOac69Folder() {
            return OAC_69_FOLDER;
        }

        @XmlElement
        public void setOac69Folder(String oac69Folder) {
            OAC_69_FOLDER = oac69Folder;
        }

        public String getPostsPath() {
            return POSTS_PATH;
        }

        @XmlElement
        public void setPostsPath(String postsPath) {
            POSTS_PATH = postsPath;
        }

        public String getPostsMalletPath() {
            return POSTS_MALLET_PATH;
        }

        @XmlElement
        public void setPostsMalletPath(String postsMalletPath) {
            POSTS_MALLET_PATH = postsMalletPath;
        }

        public int getTopicCnt() {
            return TOPIC_CNT;
        }

        @XmlElement
        public void setTopicCnt(int topicCnt) {
            TOPIC_CNT = topicCnt;
        }

        public String getTopicsPath() {
            return TOPICS_PATH;
        }

        @XmlElement
        public void setTopicsPath(String topicsPath) {
            TOPICS_PATH = topicsPath;
        }

        public String getKeysPath() {
            return KEYS_PATH;
        }

        @XmlElement
        public void setKeysPath(String keysPath) {
            KEYS_PATH = keysPath;
        }
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class TopicSettings {
        static String ROOT_FOLDER = DATA_FOLDER+"topic/";
        static String TERMS_PATH = ROOT_FOLDER+"terms.json";

        public String getRootFolder() {
            return ROOT_FOLDER;
        }

        @XmlElement
        public void setRootFolder(String rootFolder) {
            ROOT_FOLDER = rootFolder;
        }

        public String getTermsPath() {
            return TERMS_PATH;
        }

        @XmlElement
        public void setTermsPath(String termsPath) {
            TERMS_PATH = termsPath;
        }
    }

    // Lucene
    static String INDEX_PATH = DATA_FOLDER+"index";
    static String TAXOINDEX_PATH = DATA_FOLDER+"taxoindex";
    static String THINDEX_PATH = DATA_FOLDER+"thindex";
    static String POSTINDEX_PATH = DATA_FOLDER+"postindex";

    static int COLLO_TOP_K = 500;
    static int SLOP_SIZE = 10;

    // RAKE
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class RakeSettings {
        static float TOP_PERCENT = 0.75f; // 0.3333f;  // one-third the number of words in the graph. (Rose et al.)
        static String ROOT_FOLDER = DATA_FOLDER+"rake/";
        static String BASIC_INDEX_PATH = ROOT_FOLDER+"basic_index.ser";
        static String THREADED_INDEX_PATH = ROOT_FOLDER+"threaded_index.ser";
        static String CLUSTERED_INDEX_PATH = ROOT_FOLDER+"clustered_index.ser";

        public float getTopPercent() {
            return TOP_PERCENT;
        }

        @XmlElement
        public void setTopPercent(float topPercent) {
            TOP_PERCENT = topPercent;
        }

        public String getRootFolder() {
            return ROOT_FOLDER;
        }

        @XmlElement
        public void setRootFolder(String rootFolder) {
            ROOT_FOLDER = rootFolder;
        }

        public String getBasicIndexPath() {
            return BASIC_INDEX_PATH;
        }

        @XmlElement
        public void setBasicIndexPath(String basicIndexPath) {
            BASIC_INDEX_PATH = basicIndexPath;
        }

        public String getThreadedIndexPath() {
            return THREADED_INDEX_PATH;
        }

        @XmlElement
        public void setThreadedIndexPath(String threadedIndexPath) {
            THREADED_INDEX_PATH = threadedIndexPath;
        }

        public String getClusteredIndexPath() {
            return CLUSTERED_INDEX_PATH;
        }

        @XmlElement
        public void setClusteredIndexPath(String clusteredIndexPath) {
            CLUSTERED_INDEX_PATH = clusteredIndexPath;
        }
    }

    // CLUTO
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ClutoSettings {
        static String ROOT_FOLDER = DATA_FOLDER+"cluto/";
        static String DOCS_MAT = ROOT_FOLDER+"docs.mat";
        static String VCLUSTER = "D:/Programming/java/kd/cluto/cluto-2.1.2/MSWIN-x86_64/vcluster.exe";
        static String OUTPUT = ROOT_FOLDER+"docs-output.txt";

        public String getRootFolder() {
            return ROOT_FOLDER;
        }

        @XmlElement
        public void setRootFolder(String rootFolder) {
            ROOT_FOLDER = rootFolder;
        }

        public String getDocsMat() {
            return DOCS_MAT;
        }

        @XmlElement
        public void setDocsMat(String docsMat) {
            DOCS_MAT = docsMat;
        }

        public String getVCLUSTER() {
            return VCLUSTER;
        }

        @XmlElement
        public void setVCLUSTER(String VCLUSTER) {
            Settings.clutoSettings.VCLUSTER = VCLUSTER;
        }

        public String getOUTPUT() {
            return OUTPUT;
        }

        @XmlElement
        public void setOUTPUT(String OUTPUT) {
            Settings.clutoSettings.OUTPUT = OUTPUT;
        }
    }

    // Driver
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class DriverSettings {
        static String ROOT_FOLDER = DATA_FOLDER+"expr/";

        public String getRootFolder() {
            return ROOT_FOLDER;
        }

        @XmlElement
        public void setRootFolder(String rootFolder) {
            ROOT_FOLDER = rootFolder;
        }
    }

    public String getDataFolder() {
        return DATA_FOLDER;
    }

    @XmlElement
    public void setDataFolder(String dataFolder) {
        DATA_FOLDER = dataFolder;
    }

    public String getPostsPath() {
        return POSTS_PATH;
    }

    @XmlElement
    public void setPostsPath(String postsPath) {
        POSTS_PATH = postsPath;
    }

    public String getSortedPostsPath() {
        return SORTED_POSTS_PATH;
    }

    @XmlElement
    public void setSortedPostsPath(String sortedPostsPath) {
        SORTED_POSTS_PATH = sortedPostsPath;
    }

    public String getForumsPath() {
        return FORUMS_PATH;
    }

    @XmlElement
    public void setForumsPath(String forumsPath) {
        FORUMS_PATH = forumsPath;
    }

    public String getContentsPath() {
        return CONTENTS_PATH;
    }

    @XmlElement
    public void setContentsPath(String contentsPath) {
        CONTENTS_PATH = contentsPath;
    }

    public String getThreadsPath() {
        return THREADS_PATH;
    }

    @XmlElement
    public void setThreadsPath(String threadsPath) {
        THREADS_PATH = threadsPath;
    }

    public String getRawHtmlFolder() {
        return RAW_HTML_FOLDER;
    }

    @XmlElement
    public void setRawHtmlFolder(String rawHtmlFolder) {
        RAW_HTML_FOLDER = rawHtmlFolder;
    }

    public Settings.MalletSettings getMalletSettings() {
        return malletSettings;
    }

    @XmlElement
    public void setMalletSettings(Settings.MalletSettings malletSettings) {
        Settings.malletSettings = malletSettings;
    }

    public Settings.TopicSettings getTopicSettings() {
        return topicSettings;
    }

    @XmlElement
    public void setTopicSettings(Settings.TopicSettings topicSettings) {
        Settings.topicSettings = topicSettings;
    }

    public Settings.RakeSettings getRakeSettings() {
        return rakeSettings;
    }

    @XmlElement
    public void setRakeSettings(Settings.RakeSettings rakeSettings) {
        Settings.rakeSettings = rakeSettings;
    }

    public Settings.ClutoSettings getClutoSettings() {
        return clutoSettings;
    }

    @XmlElement
    public void setClutoSettings(Settings.ClutoSettings clutoSettings) {
        Settings.clutoSettings = clutoSettings;
    }

    public Settings.DriverSettings getDriverSettings() {
        return driverSettings;
    }

    @XmlElement
    public void setDriverSettings(Settings.DriverSettings driverSettings) {
        Settings.driverSettings = driverSettings;
    }

    public String getIndexPath() {
        return POSTINDEX_PATH;  // INDEX_PATH is deprecated, replaced by POSTINDEX_PATH
    }

    @XmlElement
    public void setIndexPath(String indexPath) {
        INDEX_PATH = indexPath;
    }

    public String getTaxoindexPath() {
        return TAXOINDEX_PATH;
    }

    @XmlElement
    public void setTaxoindexPath(String taxoindexPath) {
        TAXOINDEX_PATH = taxoindexPath;
    }

    public String getThindexPath() {
        return THINDEX_PATH;
    }

    @XmlElement
    public void setThindexPath(String thindexPath) {
        THINDEX_PATH = thindexPath;
    }

    public String getPostindexPath() {
        return POSTINDEX_PATH;
    }

    @XmlElement
    public void setPostindexPath(String postindexPath) {
        POSTINDEX_PATH = postindexPath;
    }

    public int getColloTopK() {
        return COLLO_TOP_K;
    }

    @XmlElement
    public void setColloTopK(int colloTopK) {
        COLLO_TOP_K = colloTopK;
    }

    public int getSlopSize() {
        return SLOP_SIZE;
    }

    @XmlElement
    public void setSlopSize(int slopSize) {
        SLOP_SIZE = slopSize;
    }

    /**
     * generate default configurations
     * @param args
     */
    public static void main(String[] args) {
        try {
            File file = new File(Settings.class.getResource("/settings.xml").toURI());
            JAXBContext jaxbContext = JAXBContext.newInstance(Settings.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            // output pretty printed
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            jaxbMarshaller.marshal(new Settings(), file);
            jaxbMarshaller.marshal(new Settings(), System.out);

        } catch (JAXBException | URISyntaxException e) {
            e.printStackTrace();
        }

    }

    static Settings newInstance() {
        try {
            File file = new File(Settings.class.getResource("/settings.xml").toURI());
            JAXBContext jaxbContext = JAXBContext.newInstance(Settings.class);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            Settings settingBuilder = (Settings) jaxbUnmarshaller.unmarshal(file);
            return settingBuilder;

        } catch (JAXBException | URISyntaxException e) {
            System.out.println(Displayer.display(e));
            return null;
        }
    }

}
