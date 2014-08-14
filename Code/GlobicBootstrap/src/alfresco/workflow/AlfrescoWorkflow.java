package alfresco.workflow;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import twitter4j.internal.org.json.JSONArray;
import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;

/**
 * 
 * This class is responsible for the using the workflow in Alfresco
 * 
 * @author leroy
 */
class AlfrescoWorkflow {

    public AlfrescoWorkflow() {
    }


    /** 
     * Method to end a workflow task
     * 
     * @taskNumber The task number
     * @alfrescoTicket This is the key to access the Alfresco store
     * 
     * @return The HTTP response
     * 
     */
    public String endTask(String taskNumber, String alfrescoTicket) {
        String line = "", response = "";
        //send cURL request
        try {
            String url = "http://phaedrus.scss.tcd.ie/CS3BC2/group6/tomcat/alfresco/service/api/workflow/task/end/" + taskNumber + "?alf_ticket=" + alfrescoTicket;

            URL obj = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");

            String data = "{\"username\":\"admin\",\"password\":\"admin\"}";
            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
            out.write(data);
            out.close();

            // Get the response
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((line = rd.readLine()) != null) {
                response = response + line;
            }
            rd.close();
        } catch (IOException e) {
        }

        return response;
    }

    /**
     * 
     * Get the workflow instance details
     * 
     * @param workflowId The workflows id
     * @return The workflow instance details
     * @throws IOException 
     */
    public String getWorkflow(String workflowId) throws IOException {
        System.getProperties().put("proxySet", "true");
        System.getProperties().put("proxyHost", "www-proxy.cs.tcd.ie");
        System.getProperties().put("proxyPort", "8080");

        URLConnection urlc = null;
        try {
            URL url = new URL("http://phaedrus.scss.tcd.ie/CS3BC2/group6/tomcat/alfresco/service/api/workflow-instances/" + workflowId);
            urlc = url.openConnection();

            //use post mode
            urlc.setDoOutput(true);
            urlc.setAllowUserInteraction(false);

            //send query
            PrintStream ps = new PrintStream(urlc.getOutputStream());
            ps.close();
        } catch (IOException ex) {
        }
        InputStreamReader isr = new InputStreamReader(urlc.getInputStream());
        String workflow = convertToString(isr);
        return workflow;
    }

    /**
     * 
     * Get a diagram of the workflow
     * 
     * @param workflowId The workflows id
     * @return
     * @throws IOException 
     */
    
    public String getWorkflowDiagram(String workflowId) throws IOException {
        System.getProperties().put("proxySet", "true");
        System.getProperties().put("proxyHost", "www-proxy.cs.tcd.ie");
        System.getProperties().put("proxyPort", "8080");

        URLConnection urlc = null;
        try {
            URL url = new URL("http://phaedrus.scss.tcd.ie/CS3BC2/group6/tomcat/alfresco/service/api/workflow-instances/" + workflowId + "/diagram");
            urlc = url.openConnection();

            //use post mode
            urlc.setDoOutput(true);
            urlc.setAllowUserInteraction(false);

            //send query
            PrintStream ps = new PrintStream(urlc.getOutputStream());
            ps.close();
        } catch (IOException ex) {
        }
        InputStreamReader isr = new InputStreamReader(urlc.getInputStream());
        String workflowDiagram = convertToString(isr);
        return workflowDiagram;
    }

    /**
     * 
     * Get the the Task via its Id
     * 
     * @param taskId The tasks id
     * @return
     * @throws IOException 
     */
    public String getTask(String taskId) throws IOException {
        System.getProperties().put("proxySet", "true");
        System.getProperties().put("proxyHost", "www-proxy.cs.tcd.ie");
        System.getProperties().put("proxyPort", "8080");

        URLConnection urlc = null;
        try {
            URL url = new URL("http://phaedrus.scss.tcd.ie/CS3BC2/group6/tomcat/alfresco/service/api/"
                    + "/" + taskId);
            urlc = url.openConnection();

            //use post mode
            urlc.setDoOutput(true);
            urlc.setAllowUserInteraction(false);

            //send query
            PrintStream ps = new PrintStream(urlc.getOutputStream());
            ps.close();
        } catch (IOException ex) {
        }
        InputStreamReader isr = new InputStreamReader(urlc.getInputStream());
        String task = convertToString(isr);
        return task;
    }

    /**
     * This is used to download files from Alfresco
     * 
     * @param storeType The Alfresco store type
     * @param storeId The Alfresco store ID
     * @param folderId The id of the folder to be used
     * @param filename The files name that you want to download
     * @throws IOException 
     */
    public void downloadFile(String storeType, String storeId, String folderId, String filename) throws IOException {
        System.getProperties().put("proxySet", "true");
        System.getProperties().put("proxyHost", "www-proxy.cs.tcd.ie");
        System.getProperties().put("proxyPort", "8080");

        URLConnection urlc = null;
        try {
            URL url = new URL("http://phaedrus.scss.tcd.ie/CS3BC2/group6/tomcat/alfresco/download/direct/" + storeType + "/" + storeId + "/" + folderId + "/" + filename);
            urlc = url.openConnection();

            //use post mode
            urlc.setDoOutput(true);
            urlc.setAllowUserInteraction(false);

            //send query
            PrintStream ps = new PrintStream(urlc.getOutputStream());
            ps.close();
        } catch (IOException ex) {
        }
    }

    //note to self don't include html characters duh
    public static String convertToString(InputStreamReader isr) {
        String sent = "";
        try {
            BufferedReader br = new BufferedReader(isr);
            String line = br.readLine();

            while (line != null) {
                String str = line;
                //sb.append(str);
                line = br.readLine();
                sent = sent + str;
            }
            br.close();

        } catch (Exception e) {
            System.err.println(e.getCause());
            System.err.println(e);
            return "";
        }
        return sent;
    }

    /**
     * Get the workflow by its id
     * 
     * @param id The workflow id
     * @param ticket The users key for accessing the workflow. This is used to check credentials
     * @return
     * @throws UnsupportedEncodingException
     * @throws JSONException 
     */
    
    public String getWorkflow(String id, String ticket) throws UnsupportedEncodingException, JSONException {
        String url = "http://phaedrus.scss.tcd.ie/CS3BC2/group6/tomcat/alfresco/service/api/node/workspace/SpacesStore/" + id + "/workflow-instances?alf_ticket=" + ticket;
        HttpClient client = new HttpClient();
        GetMethod method = new GetMethod(url);
        String response = null;
        try {
            int statusCode = client.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK) {
                System.err.println("Method failed: " + method.getStatusLine());
            }

            byte[] responseBody = method.getResponseBody();
            response = new String(responseBody);
            System.out.println(response);

        } catch (HttpException e) {
            System.err.println("Fatal protocol violation: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Fatal transport error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            method.releaseConnection();
        }
        response = response.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "");
        response = response.replace("\n<ticket>", "");
        response = response.replace("</ticket>\n", "");

        //parse JSON
        JSONObject jsonDataArray = new JSONObject(response);
        String n = jsonDataArray.getString("data");
        System.out.println("\nDATA==="+n + " " + "asdddd");
        JSONArray arr = jsonDataArray.getJSONArray("data");
        for (int i = 0; i < arr.length(); i++){
        JSONObject jsonObj = new JSONObject(arr.getString(i));
        System.out.println(arr.getString(i));
        n = jsonObj.getString("definitionUrl");
        System.out.println("n=="+n);
        }
        
        return response;
    }

    /**
     * Returns the key which the user can use with the Alfresco store
     * 
     * @param username The user name  of the Alfresco user
     * @param password The password of the Alfresco user
     * @return 
     */
    public String getAlfrescoTicket(String username, String password) {
        String url = "http://phaedrus.scss.tcd.ie/CS3BC2/group6/tomcat/alfresco" + "/service/api/login?u=" + username + "&pw=" + password;
        HttpClient client = new HttpClient();
        GetMethod method = new GetMethod(url);
        String response = null;
        try {
            int statusCode = client.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK) {
                System.err.println("Method failed: " + method.getStatusLine());
            }

            byte[] responseBody = method.getResponseBody();
            response = new String(responseBody);
            System.out.println(response);

        } catch (HttpException e) {
            System.err.println("Fatal protocol violation: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Fatal transport error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            method.releaseConnection();
        }
        response = response.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "");
        response = response.replace("\n<ticket>", "");
        response = response.replace("</ticket>\n", "");
        return response;
    }

    
    /**
     * Usage of WorkflowMethods class
     * 
     * @param args
     * @throws FileNotFoundException
     * @throws InterruptedException
     * @throws IOException
     * @throws UnsupportedEncodingException
     * @throws JSONException 
     */
    public static void main(String args[]) throws FileNotFoundException, InterruptedException, IOException, UnsupportedEncodingException, JSONException {

        AlfrescoWorkflow wm = new AlfrescoWorkflow();
        System.getProperties().put("proxySet", "true");
        System.getProperties().put("proxyHost", "www-proxy.cs.tcd.ie");
        System.getProperties().put("proxyPort", "8080");

        String username = "admin", password = "admin", taskId = "activiti%241344";

        //generate alfresco pass key
        String alfrescoTicket = wm.getAlfrescoTicket(username, password);

        //get workflow details
        wm.getWorkflow("075068ee-1979-48f2-ad8d-f704e73c7f46", alfrescoTicket);

        /* StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
         ResultSet rs = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, "PATH:\"/app:company_home/app:user_homes/sys:boris/cm:mypics\"");
         NodeRef companyHomeNodeRef = null;
         try
         {
         if (rs.length() == 0)
         {
         throw new AlfrescoRuntimeException("Didn't find Company Home");
         }
         companyHomeNodeRef = rs.getNodeRef(0);
         }
         finally
         {
         rs.close();
         }*/

    }
}
