/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package xliff2n3;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.*;
import org.xml.sax.*;

/**
 * Validates XLIFF files against the official XLIFF schema
 * @author seb
 */
public class XliffValidator {

    private Validator validator;

    /**
     * Validates the XLIFF input against the XLIFF 1.2 transitional schema
     * @param xliff The XLIFF file to validate
     * @return True if conforms to the schema, false otherwise
     */
    public XliffValidator() throws SAXException, MalformedURLException, ParserConfigurationException {

        this.applyProxySettings();
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        InputStream xsd = getClass().getResourceAsStream(Constants.XLIFF_XSD_FILENAME);
        Source xsdSource = new StreamSource(xsd);
        Schema schema = schemaFactory.newSchema(xsdSource); // Or get it from web: Schema schema = schemaFactory.newSchema(new URL("http://docs.oasis-open.org/xliff/v1.2/cs02/xliff-core-1.2-transitional.xsd"));
        
        this.validator = schema.newValidator();
    }



    /**
     * Validates the XLIFF input against the XLIFF 1.2 transitional schema
     * @param xliff The XLIFF input to validate
     * @return Null if validation passed, description of the errors otherwise
     */
    public String validate(Source source) throws SAXException, ParserConfigurationException, IOException {

        ValidationErrorHandler errors = new ValidationErrorHandler();
        this.validator.setErrorHandler(errors);
        this.validator.validate(source);
        return errors.getErrorsText();
    }

    /**
     * Applies the HTTP Proxy settings as per the contents of the properties file
     */
    public final void applyProxySettings() {
        String proxyUrl = Configuration.getProperty("HTTP_PROXY_URL");
        if (proxyUrl != null) {
            System.getProperties().put("http.proxyHost", proxyUrl);
        }
        String proxyPort = Configuration.getProperty("HTTP_PROXY_PORT");
        if (proxyPort != null) {
            System.getProperties().put("http.proxyPort", proxyPort);
        }
    }

    /**
     * Handles validation errors
     */
    public static class ValidationErrorHandler implements ErrorHandler {

        List<SAXParseException> exceptions = new ArrayList<SAXParseException>();

        public void warning(SAXParseException exception) throws SAXException {
            exceptions.add(exception);
        }

        public void error(SAXParseException exception) throws SAXException {
            exceptions.add(exception);
        }

        public void fatalError(SAXParseException exception) throws SAXException {
            exceptions.add(exception);
        }

        public boolean isClear() {
            return (exceptions.isEmpty());
        }

        public String getErrorsText() {
            String ret = null;
            for (SAXParseException exception: exceptions) {
                if (ret == null) {
                    ret = exception.getMessage();
                }
                else {
                    ret = String.format("%s\n", exception.getMessage());
                }
            }
            return ret;
        }
    }
}
