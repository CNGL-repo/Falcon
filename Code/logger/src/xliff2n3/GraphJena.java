package xliff2n3;

import jena.JenaQuery;
import com.hp.hpl.jena.query.ResultSet;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.*;
import org.openrdf.repository.sparql.SPARQLRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;

/**
 * Manages the import and post-processing of XLIFF-derived triples into a Sesame
 * triple store
 *
 * @author Seb and leroy
 */
public class GraphJena {

    /**
     * Sesame repository
     */
    Repository repository;
    String sparqlEndPoint;
    String updateEndpoint;

    /**
     * Factory method that creates a remote graph
     *
     * @param url URL of the Sesame Server
     * @param repositoryId Sesame Server Repository ID
     * @return GraphJena object
     * @throws RepositoryException
     */
    public static GraphJena GetRemoteGraphJena(String url, String repositoryId) throws RepositoryException {
        String sparqlEndPoint=url+"/"+repositoryId+"/sparql";
        String updateEndpoint=url+"/"+repositoryId+"/update";
        
        Repository repository = new SPARQLRepository(sparqlEndPoint, updateEndpoint);
        repository.initialize();
        return new GraphJena(repository,sparqlEndPoint,updateEndpoint);
    }

    /**
     * Private constructor
     */
    private GraphJena(Repository repository,String sparqlEndPoint,String updateEndpoint) {
        this.repository = repository;
        this.sparqlEndPoint=sparqlEndPoint;
        this.updateEndpoint=updateEndpoint;
    }

    /**
     * Merges triples into the graph and add provenance statements to new URIs
     *
     * @param n3reader N3-formatted triples to merge
     * @throws RepositoryException
     * @throws IOException
     * @throws RDFParseException
     */
    public void takeJena(Reader n3reader,String sparqlEndpoint,String updateEndpoint) throws RepositoryException, IOException, RDFParseException, QueryEvaluationException, MalformedQueryException, DatatypeConfigurationException, Exception {
        System.getProperties().put("proxySet", "true");
        System.getProperties().put("proxyHost", "www-proxy.cs.tcd.ie");
        System.getProperties().put("proxyPort", "8080");
        SPARQLRepository sr = new SPARQLRepository(sparqlEndpoint,updateEndpoint);
        sr.initialize();
        RepositoryConnection connection = sr.getConnection();

        // Add the triples to the default and temp context.
        // In the default context, duplicates are ignored, but the new temp context will contain them all
        Resource tempContext = this.getTempBNodeJena();
        System.out.println("==TEMP CONTEXT==" + n3reader.toString() + "==" + tempContext.stringValue());
        ValueFactory factory = repository.getValueFactory();
        Resource jobUriResource = factory.createBNode("temp");
        String jobUriResourceString = jobUriResource.stringValue();
        System.out.println("jobUriResourceString==" + sparqlEndpoint + "|||" + updateEndpoint);
        
        //problem with resource
        connection.add(n3reader, "", RDFFormat.N3, (Resource) null, tempContext);

        //void	add(Reader reader, String baseURI, RDFFormat dataFormat, Resource... contexts)
        //Adds RDF data from a Reader to the repository, optionally to one or more named contexts.
        // Post-process: Add provenance
        this.addProvenance(connection, tempContext);

        //// Export all statements in the context to System.out, in RDF/XML format
        //connection.export(tempContext, new RDFXMLWriter(System.out));

        // We are done with the temp context--Remove it
        connection.clear(tempContext);
        connection.close();
    }

    /**
     * Adds provenance statements to all new URIs
     *
     * @param connection The active Sesame connection
     * @param tempContext The context to which statements were added
     */
    private void addProvenance(RepositoryConnection connection, Resource tempContext) throws RepositoryException, QueryEvaluationException, MalformedQueryException, DatatypeConfigurationException, Exception {

        // Get all the subjects we've added to the temp context
        List<URI> mergedUris = this.getUrisInContext(connection, tempContext);
        System.out.println("===getUrisInContext===" + mergedUris.toString());
        if (mergedUris.size() > 0) {

            // Follow the chain of "isPartOf" statements to find the root container
            URI container = this.getRootContainer(connection, mergedUris.get(0), null);

            // Create a unique log URI
            URI generatedByValue = this.createUniqueLogUri(connection, new Date(), container);

            // Add provenance info where not already present (change for Jena addProvenance())
            for (URI uri : mergedUris) {
                this.addProvenanceIfMissing(connection, uri, generatedByValue);
                System.out.println("=====mergedUris=======" + uri.stringValue());

            }

        }
    }

    /**
     * Gets a blank node
     *
     * @return A blank node
     */
    private URI getTempBNodeJena() {
        ValueFactory valueFactory = this.repository.getValueFactory();
        return valueFactory.createURI(Constants.test_uri);
    }

    /**
     * Gets the predicate URI for provenance statements
     *
     * @return The URI of the provenance statements
     */
    private URI getGeneratedByPredicate() {
        ValueFactory valueFactory = this.repository.getValueFactory();
        return valueFactory.createURI(Constants.PROVENANCE_URI);
    }

    private URI getGeneratedTimePredicate() {
        ValueFactory valueFactory = this.repository.getValueFactory();
        return valueFactory.createURI(Constants.PROVENANCE_DATETIME_URI);
    }

    private URI getLogInputPredicate() {
        ValueFactory valueFactory = this.repository.getValueFactory();
        return valueFactory.createURI(Constants.LOGGER_LOG_INPUT_URI);
    }

    private URI getTypePredicate() {
        ValueFactory valueFactory = this.repository.getValueFactory();
        return valueFactory.createURI(Constants.TYPE_URI);
    }

    private URI getLogType() {
        ValueFactory valueFactory = this.repository.getValueFactory();
        return valueFactory.createURI(Constants.LOGGER_LOG_TYPE_URI);
    }

    /**
     * Creates a unique URI for a new log and it associated timestamp and input
     * statements
     *
     * @return A unique log URI
     */
    private URI createUniqueLogUri(RepositoryConnection connection, Date timestamp, URI root) throws DatatypeConfigurationException, RepositoryException {

        // Create the URI based on a new UUID
        ValueFactory factory = this.repository.getValueFactory();
        String uuid = UUID.randomUUID().toString();
        URI logUri = factory.createURI(Constants.LOGGER_LOGS_URI + uuid);

        // Add a type statement
        connection.add(logUri, this.getTypePredicate(), this.getLogType());

        // Add an input statement
        connection.add(logUri, this.getLogInputPredicate(), root);

        // Add a timestamp statement
        URI generatedTime = this.getGeneratedTimePredicate();
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(timestamp);
        XMLGregorianCalendar xmlTimestamp = DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
        Value timeLiteral = factory.createLiteral(xmlTimestamp);
        connection.add(logUri, generatedTime, timeLiteral);
        return logUri;
    }

    /**
     * Gets all URIs in a context
     *
     * @param connection Active Sesame connection
     * @param context The context in which to find URIs
     * @return A list of all unique URIs in the context
     * @throws RepositoryException
     */
    private List<URI> getUrisInContext(RepositoryConnection connection, Resource context) throws RepositoryException {
        List<URI> uris = new ArrayList<URI>();

        // Get all triples in the context
        RepositoryResult<Statement> statements = connection.getStatements(null, null, null, false, context);
        while (statements.hasNext()) {
            Statement statement = statements.next();

            // If subject is a URI, add it in if not already present
            Resource subject = statement.getSubject();
            System.out.println("sample==" + subject.stringValue());
            if (subject instanceof URI) {
                URI uri = (URI) subject;

                if (!uris.contains(uri)) {
                    System.out.println("sample2==" + uri.stringValue());
                    uris.add(uri);
                }
            }

            // If object is a URI, add it in if not already present
            Value object = statement.getObject();
            if (object instanceof URI) {
                URI uri = (URI) object;
                if (!uris.contains(uri)) {
                    System.out.println("sample3==" + uri.stringValue());
                    uris.add(uri);
                }
            }
        }
        statements.close();
        System.out.println("STATEMENTS===" + uris.toString());
        return uris;
    }

    /**
     * If the URI does not already have a provenance statement, add one matching
     * the arguments
     *
     * @param connection Active Sesame connection
     * @param subject The URI to add provenance to if required
     * @param generatedByObject The provenance URI to add if required
     * @throws RepositoryException
     */
    private void addProvenanceIfMissing(RepositoryConnection connection, URI subject, Value generatedByObject) throws RepositoryException {
        URI generatedBy = this.getGeneratedByPredicate();
        System.out.println("addProvenanceIfMissing==" + subject.stringValue());
        RepositoryResult<Statement> statements = connection.getStatements(subject, generatedBy, null, false);
        System.out.println("addProvenanceIfMissing2==" + statements);
        if (!statements.hasNext()) {
            // No results: Add it in
            System.out.println("Jena++" + subject.stringValue() + "==" + generatedBy.stringValue() + "==" + generatedByObject.stringValue());
            connection.add(subject, generatedBy, generatedByObject);
        }
    }

    /**
     * Recursively goes up the chain of isPartOf statements to find the root
     * container
     *
     * @param connection Active Sesame connection
     * @param preparedQuery must be null in the initial call (in recursing
     * calls, this is the parameterized query that can be reused)
     * @param subject The URI for which to find a root container
     */
    
    //Change to simple seqerl not working
    private URI getRootContainer(RepositoryConnection connection, URI subject, TupleQuery preparedQuery) throws QueryEvaluationException, RepositoryException, MalformedQueryException, Exception {
        JenaQuery sq = new JenaQuery();
        String query = "SELECT ?sub WHERE {?sub <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.cngl.ie/LeanBackLearning>}";
        ResultSet results = sq.executeQuery(this.sparqlEndPoint, query);
        String URI=sq.singleQuery(results);
        System.out.println("++URI++"+URI);
        ValueFactory valueFactory = this.repository.getValueFactory();
        subject=valueFactory.createURI(URI);
        return subject;
    }
}