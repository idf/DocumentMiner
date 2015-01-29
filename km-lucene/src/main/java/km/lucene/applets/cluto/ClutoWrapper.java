package km.lucene.applets.cluto;

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
    private String matPath;
    private String cluserOutputPath;
    private int k;

    public static void main(String[] args) throws Exception {
        new ClutoWrapper(Settings.ClutoSettings.DOCS_MAT, Settings.ClutoSettings.OUTPUT, 226).run();
    }

    public ClutoWrapper(String matPath, String cluserOutputPath, int k) {
        this.matPath = matPath;
        this.cluserOutputPath = cluserOutputPath;
        this.k = k;
    }

    @Override
    public void run() {
        File mat = new File(matPath);
        String cmd = buildCmd(getDefaultKwargs(), mat, k);
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
        kwargs.put("-clustfile", cluserOutputPath);
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