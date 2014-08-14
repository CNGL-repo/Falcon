package xliff2n3;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;

/**
 *
 * @author leroy
 */
public class ContentLogging {

    public static String logging(String xliffFilename, String applicationName) {
        System.getProperties().put("proxySet", "true");
        System.getProperties().put("proxyHost", "www-proxy.cs.tcd.ie");
        System.getProperties().put("proxyPort", "8080");

        LoggerStatus status = null;
        Properties prop = new Properties();
        InputStream inputConfig = null;
        LoggerJena loggerJena = null;

        try {
            inputConfig = new FileInputStream(Constants.CONFIG_FILENAME);
            prop.load(inputConfig);

            //Select the correct XSLT stylesheet
            switch (applicationName) {
                case "leanback_learning":
                    loggerJena = new LoggerJena(prop.getProperty("SPARQL_ENDPOINT"), prop.getProperty("LBL_PARTITION"), Constants.XSLT_GLOBIC_LBL);
                    System.out.println("leanback_learning");
                    break;
                case "falcon":
                    loggerJena = new LoggerJena(prop.getProperty("SPARQL_ENDPOINT"), prop.getProperty("FALCON_PARTITON"), Constants.XSLT_FALCON);
                    break;
                case "test":
                    loggerJena = new LoggerJena(prop.getProperty("SPARQL_ENDPOINT"), prop.getProperty("DFLT_REP_ID"), Constants.XSLT_FALCON);
                    break;
            }

            //log this file
            FileInputStream inputStream = new FileInputStream(xliffFilename);
            status = loggerJena.log(inputStream, xliffFilename, loggerJena);
            System.out.println("STATUS==" + status.getOutput());
            return status.getOutput();
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(ContentLogging.class.getName()).log(Level.SEVERE, null, ex);
        }
        return status.getOutput();
    }
}
