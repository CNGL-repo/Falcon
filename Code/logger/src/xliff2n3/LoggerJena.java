/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xliff2n3;

import java.io.*;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author seb
 */
public class LoggerJena {
    private GraphJena graph;
    private Converter converter;
    private XliffValidator xliffValidator;
    private boolean validate;
    public String url;
    public String repositoryId;
    
    public LoggerJena(GraphJena graph,String filename) {
        this.graph = graph;
        this.converter = new Converter(filename);
    }

    public void setValidating(boolean validate) throws SAXException, MalformedURLException, ParserConfigurationException {

        // Create the validator on demand (it takes a while and requires connectivity)
        if (validate && (xliffValidator == null)) {
            this.xliffValidator = new XliffValidator();
        }

        // Only do this after intializing the validator (so that state is consistent if an exception occurs)
        this.validate = validate;
    }

    public LoggerJena(String url, String repositoryId,String filename) throws RepositoryException {
        this(GraphJena.GetRemoteGraphJena(url, repositoryId),filename);
        this.url=url;
        this.repositoryId=repositoryId;
    }
    
    public String getUrl(){
        return this.url;
    }
    
    public String getrepositoryId(){
        return this.repositoryId;
    }

    /**
     * Logs an XLIFF file
     * @param xliff the XLIFF content
     * @param filename name of the XLIFF file (will be added as metadata)
     * @return Metadata related to the success and processing of the logging
     */
    public LoggerStatus log(InputStream xliff, String filename,LoggerJena loggerJena) {

        LoggerStatus results = new LoggerStatus();

        try {
            // Parse the xliff into an XML DOM
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder parser = factory.newDocumentBuilder();

            results.enterStage(LoggerStatus.Stage.Parsing);
            Document xliffDoc = parser.parse(xliff);
            DOMSource xliffSource = new DOMSource(xliffDoc);

            // Validate it, if required
            if (this.validate) {
                results.enterStage(LoggerStatus.Stage.Validating);

                String validationErrors = xliffValidator.validate(xliffSource);
                if (validationErrors != null) {
                    results.append(validationErrors);
                    return results;
                }
            }

            // Convert the XLIFF to N3
            results.enterStage(LoggerStatus.Stage.Converting);
            StringWriter out = new StringWriter();
            converter.convert(xliffSource, filename, out);
            String n3Output = out.toString();
            results.append(n3Output);
            
             //Process the N3
            results.enterStage(LoggerStatus.Stage.Merging);
          //  graph.take(new StringReader(n3Output), RDFFormat.N3);
            System.out.println("===========n3Output==========="+n3Output+loggerJena.getUrl()+"/"+loggerJena.getrepositoryId()+"/sparql".trim());
             graph.takeJena(new StringReader(n3Output),loggerJena.getUrl()+"/"+loggerJena.getrepositoryId()+"/sparql",loggerJena.getUrl()+"/"+loggerJena.getrepositoryId()+"/update");
            results.setSucceeded(true);
            
        } catch (Exception ex) {
            results.reportException(ex);
            Logger.getLogger(LoggerJena.class.getName()).log(Level.SEVERE, null, ex);
        }

        return results;
    }

    
    
    //Uploading just a string and adding 
    public void annotation(String upload,LoggerJena loggerJena ) throws RepositoryException, IOException, RDFParseException, QueryEvaluationException, MalformedQueryException, DatatypeConfigurationException {
        try {
            graph.takeJena(new StringReader(upload),loggerJena.getUrl()+"/"+loggerJena.getrepositoryId()+"/sparql",loggerJena.getUrl()+"/"+loggerJena.getrepositoryId()+"/update");
            // graph.take(new StringReader(upload), RDFFormat.RDFXML);
        } catch (Exception ex) {
            Logger.getLogger(LoggerJena.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
