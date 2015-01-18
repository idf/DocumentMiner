package km.lucene.app.cluto;

import io.deepreader.java.commons.util.CmdUtils;
import io.deepreader.java.commons.util.Displayer;
import km.common.Settings;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * User: Danyang
 * Date: 1/15/2015
 * Time: 20:29
 *
 */
public class ClutoWrapper implements Runnable {
    public static void main(String[] args) throws Exception {
        ClutoWrapper clutoWrapper = new ClutoWrapper();
        clutoWrapper.run();
    }

    @Override
    public void run() {
        File mat = new File(Settings.ClutoSettings.DOCS_MAT);
        String cmd = buildCmd(getDefaultKwargs(), mat, 226);
        try {
            CmdUtils.exec(cmd);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    Map<String, String> getDefaultKwargs() {
        Map<String, String> kwargs = new HashMap<>();
        // = in between
        kwargs.put("-clmethod", "rbr");
        kwargs.put("-sim", "cos");
        kwargs.put("-crfun", "h2");
        kwargs.put("-ntrials", "10");
        kwargs.put("-clustfile", Settings.ClutoSettings.OUTPUT);
        // kwargs.put("", "");
        // kwargs.put("-plotformat", "gif");
        return kwargs;
    }

    String buildCmd(Map<String, String> kwargs, File matrixFile, int numClusters) {
        // vcluster [optional parameters] MatrixFile NClusters
        StringBuffer cmd = new StringBuffer();
        cmd.append(Settings.ClutoSettings.VCLUSTER).append(" ")
                .append(Displayer.display(kwargs, "=", " ")).append(" ")
                .append(matrixFile.toString().replace("\\", "/")).append(" ")
                .append(numClusters);
        return cmd.toString();
    }
}
