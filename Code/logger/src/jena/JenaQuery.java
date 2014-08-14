
package jena;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.hp.hpl.jena.sparql.resultset.ResultsFormat;
import com.hp.hpl.jena.update.*;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

/**
 * Jena working perfectly update and query will be extended
 *
 * @author leroy
 */
public class JenaQuery {

    String sparqlEndpoint;
    String updateEndpoint;
    
    public JenaQuery() {
    }
    
    public JenaQuery(String sparqlEndpoint,String updateEndpoint) {
        this.sparqlEndpoint=sparqlEndpoint;
        this.updateEndpoint=updateEndpoint;
    }
    
    public String getSparqlEndpoint(){
        return this.sparqlEndpoint;
    }
    
    public String getUpdateEndpoint(){
        return this.updateEndpoint;
    }

    public ResultSet executeQuery(String sparqlService, String queryString) throws Exception {
        System.getProperties().put("proxySet", "true");
        System.getProperties().put("proxyHost", "www-proxy.cs.tcd.ie");
        System.getProperties().put("proxyPort", "8080");
        Query query = QueryFactory.create(queryString);
        QueryEngineHTTP qexec = QueryExecutionFactory.createServiceRequest(sparqlService, query);
        ResultSet results = qexec.execSelect();
        return results;
    }

    public void updateFile(String updateService, String filename) throws Exception {
        UpdateRequest update = UpdateFactory.read("/home/leroy/Desktop/sample.n3");
        UpdateProcessor up = UpdateExecutionFactory.createRemote(update, "http://kdeg-vm-5.scss.tcd.ie:3030/dataset/update");
        up.execute();
    }

    public void updateQuery(String updateService, String query) throws Exception {
        UpdateRequest update = UpdateFactory.create(query);
        UpdateProcessor up = UpdateExecutionFactory.createRemote(update, "http://kdeg-vm-5.scss.tcd.ie:3030/dataset/update");
        up.execute();
    }

    public String singleQuery(ResultSet results) {
        String query = "";
        RDFNode subject =null;
         for (; results.hasNext();) {
        QuerySolution soln = results.nextSolution();
        subject = soln.get("sub");
        RDFNode object = soln.get("p");
        RDFNode predicate = soln.get("o");
       // System.out.println(subject + " ---- " + predicate + " ---- " + object);
         }
        query = subject.toString();

        return query;
    }

    public ArrayList<String> multipleQueries(ResultSet results) {
        ArrayList<String> queries = new ArrayList<String>();
        for (; results.hasNext();) {
            QuerySolution soln = results.nextSolution();
            RDFNode subject = soln.get("s");
            RDFNode object = soln.get("p");
            RDFNode predicate = soln.get("o");
            System.out.println(subject + " ---- " + predicate + " ---- " + object);
        }
        return queries;
    }

}