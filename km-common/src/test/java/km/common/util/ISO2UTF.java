package km.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ISO2UTF {

    public static void main(String[] args) throws MalformedURLException, IOException {
        String url = "http://forums.hardwarezone.com.sg/degree-programs-courses-70/unisim-july-2011-intake-lip-lai-3157598-17.html";
        InputStream is = new URL(url).openStream();
        ByteBuffer bb = ByteBuffer.allocate(102400);
        byte[] b = new byte[1024];
        int len;
        while ((len = is.read(b)) != -1) {
            bb.put(b, 0, len);
            b = new byte[1024];
        }
        byte[] ary = Arrays.copyOfRange(bb.array(), 0, bb.position());
        for (int i = 0; i < ary.length; i++) {
            if (ary[i] < 0) {
                ary[i] = 32;
            }
        }
        System.out.println(new String(ary, StandardCharsets.UTF_8));
    }
}
