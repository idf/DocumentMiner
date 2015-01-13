package km.crawler.multithread;

import km.common.Settings;
import km.crawler.entities.Page;
import km.crawler.enums.PageType;

public class CrawlerMT {

    private static final String URL_ROOT = "http://forums.hardwarezone.com.sg/";
    private static final Page HOMEPAGE = new Page(PageType.HOMEPAGE, "academic-concerns-66/");

    public static void main(String[] args) {
        
        // testing parameters
        args = new String[3];
        args[0] = Settings.DATA_FOLDER;
        args[1] = Settings.RAW_HTML_FOLDER;
        args[2] = "50";
        
        if (args.length < 3) {
            System.out.println("Please specify a path to store the output files and the number of threads.");
            System.exit(1);
        }

        String dataPath = args[0];
        String htmlPath = args[1];
        int threadPoolSize = Integer.parseInt(args[2]);

        Coordinator coordinator = new Coordinator(dataPath, HOMEPAGE);
        for (int i = 0; i < threadPoolSize; i++) {
            WorkerThread wt = new WorkerThread(String.format("T%d", (i + 1)), new CrawlerTask(coordinator, URL_ROOT, htmlPath));
            wt.start();
        }
    }
}
