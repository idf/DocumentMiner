package km.common.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jsoup.Jsoup;

public class HttpUtil {

    private static final Logger LOGGER = Logger.getLogger(HttpUtil.class.getName());

    /*
     * get html from web
     */
    public static String getHTML(String url) {
        try {
            return Jsoup.connect(url).timeout(0).get().html();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, String.format("Failed to fetch %s, %s", url, e.getMessage()));
        }
        return "";
    }

    /*
     * get html from web and store original html locally
     */
    public static String getHTML(String url, String path) {
        String fileName = url.substring(url.indexOf("//") + 2);
        fileName = fileName.substring(fileName.indexOf("/") + 1);
        if (fileName.indexOf(".html") == -1) {
            fileName += "/index.html";
        }
        File file = new File(path + "/" + fileName);
        file.getParentFile().mkdirs();
        try {
            String html = Jsoup.connect(url).timeout(0).get().html();
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
            bw.write(html);
            bw.close();
            return html;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, String.format("Failed to fetch %s, %s", url, e.getMessage()));
        }
        return "";
    }

    /*
     * get html from local file
     */
    public static String loadHTML(String url, String path) {
        String fileName = url.substring(url.indexOf("//") + 2);
        fileName = fileName.substring(fileName.indexOf("/") + 1);
        if (fileName.indexOf(".html") == -1) {
            fileName += "/index.html";
        }
        File file = new File(path + "/" + fileName);
        try {
            String html = Jsoup.parse(file, StandardCharsets.UTF_8.name()).html();
            return html;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, String.format("Failed to fetch %s, %s", url, e.getMessage()));
        }
        return "";
    }
}
