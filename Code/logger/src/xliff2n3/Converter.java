package xliff2n3;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;

/**
 * Performs conversions from XLIFF to N3 (using a built-in XSLT)
 * @author Seb
 */
public class Converter {

    /** Java XSLT transformer used to execute the transform */
    protected Transformer transformer;

    /**
     * Creates a Converter object
     */
   /* public Converter() {
        try {
            InputStream xslt = getClass().getResourceAsStream(Constants.XSLT_FILENAME);
            Source xsltSource = new StreamSource(xslt);
            TransformerFactory factory = TransformerFactory.newInstance();
            Templates template = factory.newTemplates(xsltSource);
            this.transformer = template.newTransformer();
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(Converter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }*/
    
    public Converter(String filename) {
        try {
            InputStream xslt = getClass().getResourceAsStream(filename);
            Source xsltSource = new StreamSource(xslt);
            TransformerFactory factory = TransformerFactory.newInstance();
            Templates template = factory.newTemplates(xsltSource);
            this.transformer = template.newTransformer();
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(Converter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Converts an XLIFF document to N3-encoded statements
     * @param input XLIFF to convert
     * @param xliffFilename name of the original XLIFF file
     * @param outputWriter Output writer
     */
    public void convert(Source input, String xliffFilename, Writer outputWriter) {
        try {
            Result result = new StreamResult(outputWriter);
            this.transformer.setParameter("xliffFileName", xliffFilename);  // Set the XSLT parameter
            this.transformer.transform(input, result);
        } catch (TransformerException ex) {
            Logger.getLogger(Converter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
