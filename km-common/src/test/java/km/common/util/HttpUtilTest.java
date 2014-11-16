package km.common.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class HttpUtilTest {

    public static void main(String[] args) throws IOException {
        String url = "http://forums.hardwarezone.com.sg/degree-programs-courses-70/unisim-july-2011-intake-lip-lai-3157598-17.html";
        File file = new File("E:/project/kd/html/test.html");
        InputStream is = new URL(url).openStream();
        ByteBuffer bb = ByteBuffer.allocate(500000);
        byte[] buffer = new byte[1024];
        int len;
        while ((len = is.read(buffer)) != -1) {
            bb.put(buffer, 0, len);
        }
        is.close();

        System.out.println(bb.position());
        System.out.println(new String(bb.array(), 43440, 10, StandardCharsets.UTF_8));

        byte[] ary = Arrays.copyOfRange(bb.array(), 43440, 43450);
        System.out.println(Arrays.toString(ary));

        OutputStream os = new FileOutputStream(file);
        os.write(ary);
        os.close();
    }
}
