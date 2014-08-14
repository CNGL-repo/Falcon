package xliff2n3;

/**
 * Constants
 * @author seb
 */
public class Constants {
    
    /** Name of the config file */
    public static final String CONFIG_FILENAME = "C:\\falcon\\config.properties";

    /** URI of type predicate */
    static final String TYPE_URI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
//======================first generated=========================================
    /** URI of provenance predicate */
    static final String PROVENANCE_URI = "http://www.w3.org/ns/prov#wasDerivedFrom";

    static final String test_uri = "http://www.w3.org/ns/prov#testing";
    
    /** URI of provenance timestamp predicate */
    static final String PROVENANCE_DATETIME_URI = "http://www.w3.org/ns/prov#generatedAtTime";
//==============================================================================
    /** URI for log input predicate */
   // static final String LOGGER_LOG_INPUT_URI = "http://www.cngl.ie/logger/logs/input";
    static final String LOGGER_LOG_INPUT_URI = "http://www.w3.org/ns/prov#wasGeneratedBy";
    
    /** URI used in logs statements */
   // static final String LOGGER_LOGS_URI = "http://www.cngl.ie/logger/logs/";
    static final String LOGGER_LOGS_URI = "http://www.w3.org/ns/prov#";
    
    /** URI of log type */
   // static final String LOGGER_LOG_TYPE_URI = "http://www.cngl.ie/logger/logs/log";
   static final String LOGGER_LOG_TYPE_URI = "http://www.w3.org/ns/prov#Bundle";
   //===========================================================================
   
    /** Name of the XSLT file we use to convert XLIFF files to N3 */
    static final String XSLT_FALCON = "falcon.xsl";

      /** Name of the XSLT file we use to convert XLIFF files to N3 */
    static final String XSLT_GLOBIC_LBL = "lblXSLT.xsl";
    
    /** Name of the XLIFF schema file */
    static final String XLIFF_XSD_FILENAME = "xliff-core-1.2-transitional.xsd";

    /** Path to the local store (not used if using a remote connection) */
    static final String LOCAL_STORE_FILENAME = "TripleStore.bin";
    
    static final String RDF_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
    
}
