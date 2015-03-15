package km.lucene.applets.cluto;

import io.deepreader.java.commons.util.CmdUtils;
import io.deepreader.java.commons.util.Displayer;
import io.deepreader.java.commons.util.ExceptionUtils;
import km.common.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

/**
 * Wrap around cluto C++
 * Manual: http://glaros.dtc.umn.edu/gkhome/fetch/sw/cluto/manual.pdf
 */
public class ClutoWrapper implements Runnable {
    private String matPath;
    private String clusterOutputPath;
    private int k;

    protected Logger logger = LoggerFactory.getLogger(ClutoWrapper.class);

    public static void main(String[] args) throws Exception {
        new ClutoWrapper(Config.settings.getClutoSettings().getDocsMat(),
                Config.settings.getClutoSettings().getOUTPUT(),
                226)
                .run();
    }

    /**
     * Constructor
     * @param matPath  input path
     * @param clusterOutputPath output path
     * @param k number of clusters
     */
    public ClutoWrapper(String matPath, String clusterOutputPath, int k) {
        this.matPath = matPath;
        this.clusterOutputPath = clusterOutputPath;
        this.k = k;
    }

    @Override
    public void run() {
        File mat = new File(matPath);
        String cmd = buildCmd(getDefaultKwargs(), mat, k);
        try {
            CmdUtils.exec(cmd);
            logger.info("Cluto executed");
        } catch (IOException e) {
            ExceptionUtils.stifleCompileTime(e);
        }
    }

    /**
     * Construct default keyword arguments for cluto command line execution
     * @return keyword arguments
     */
    Map<String, String> getDefaultKwargs() {
        Map<String, String> kwargs = new HashMap<>();
        // = in between
        kwargs.put("-clmethod", "rbr");
        kwargs.put("-sim", "cos");
        kwargs.put("-crfun", "h2");
        kwargs.put("-ntrials", "7");
        kwargs.put("-clustfile", clusterOutputPath);
        // kwargs.put("", "");
        // kwargs.put("-plotformat", "gif");
        return kwargs;
    }

    /**
     * Build command for cluto command line execution
     * @param kwargs keyword arguments as options for cluto
     * @param matrixFile input matrix file
     * @param numClusters number of clusters for clustering
     * @return
     */
    String buildCmd(Map<String, String> kwargs, File matrixFile, int numClusters) {
        // vcluster [optional parameters] MatrixFile NClusters
        StringBuffer cmd = new StringBuffer();
        cmd.append(Config.settings.getClutoSettings().getVCLUSTER()).append(" ")
                .append(Displayer.display(kwargs, "=", " ")).append(" ")
                .append(matrixFile.toString().replace("\\", "/")).append(" ")
                .append(numClusters);
        return cmd.toString();
    }
}
