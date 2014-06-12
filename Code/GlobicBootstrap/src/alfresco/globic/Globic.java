package alfresco.globic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;

/**
 *
 *  This class is used for register, content provenance generation content and 
 *  retrieve content from the Globic
 * 
 *  @author leroy
 *
 */
public class Globic {

    /**
     * 
     * Method to register application with Globic (example: POST http://phaedrus.scss.tcd.ie/gls/globicLog/register/?componentName=MockComponent_1&password=123456&partitionName=partition1)
     * 
     * @componentName Here you specify what name you want to call your
     * component. Component name must be unique. It is not possible to have two
     * or more components with the same name registered with the service.
     *
     * @password Here you specify your password which will be used to verify
     * your component when making requests to the system. It is recommended not
     * use a password you have for any other accounts.
     *
     * @partitionName Here you can specify a partition in the triple-store in which
     * your component will store data to and retrieve data from. This is an
     * optional token in the request. If it is not specified, you will record
     * data to and retrieve data from a default partition in the triple-store.
     * This option can be useful if you want your own 'sandbox' so provenance
     * data from other components will not be appear in your partition. It is
     * also possible for multiple components to specify the partition name if
     * they wish to share the same provenance data.
     * 
     * @return response from register service
     */
    public static String registerComponent(String componentName, String password, String partitionName) {
        System.getProperties().put("proxySet", "true");
        System.getProperties().put("proxyHost", "www-proxy.cs.tcd.ie");
        System.getProperties().put("proxyPort", "8080");
        String output = "";
        String line = "";
        try {
            // Construct data
            String query = URLEncoder.encode("componentName", "UTF-8") + "=" + URLEncoder.encode(componentName, "UTF-8");
            query += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8");
            if (!partitionName.equals("") && !partitionName.equals(null)) {
                query += "&" + URLEncoder.encode("partitionName", "UTF-8") + "=" + URLEncoder.encode(partitionName, "UTF-8");
            }
            // Send data
            URL url = new URL("http://kdeg-vm-24.scss.tcd.ie/globicLog/register/");
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(query);
            wr.flush();

            // Get the response
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((line = rd.readLine()) != null) {
                output = output + line;
            }
            wr.close();
            rd.close();
        } catch (IOException e) {
        }
        return output;
    }

    /**
     * POST content file to Alfresco and then transform file data into Globic
     * Model.
     *
     * @componentName Here you specify what name you want to call your component. 
     * Component name must be unique. It is not possible to have two or more 
     * components with the same name registered with the service.
     * 
     * @password Here you enter your password.
     *
     * @activity Here you enter the activity that is being carried out by your
     * component. An activity is limited to the list of content processing
     * activities specified in the Globic semantic model. In order for the
     * service to be able to recognise the activity, the name you give to the
     * activity being carried out must contain the actual name of the activity
     * as specified in the Globic model. For example, two content processing
     * activities within the Globic model are TextAnalysis and Translate. Some
     * Sample activity names that would work with the service would be :
     * textanalysis_50, 5_My_TextAnalysis, translateActivity101 or
     * my_translate_92. These work as the actual activity name appears within
     * the name given. Some Sample activity names that would not work with the
     * service would be: 5_My_Text_Analysis, AnalysisText_12 or trans_7.
     *
     * @user Here you can specify if a user was involved in an activity along
     * with a component. This is an optional token in the request as a user does
     * not always have to be involved in an activity. The user can be the name
     * of a person or a worker ID number etc. If specifying the user as the name
     * of a person with a first and last name, do not use a space between the
     * names, instead use humpback notation or use an underscore, for example:
     * JoeBloggs or Joe_Bloggs.
     *
     * @contentConsumed1 Here it is possible to specify up to 10 pieces of
     * content that were consumed or used by an activity. This is an optional
     * token in the request as not all activities consume content. Pieces of
     * content consumed are specified by appending a number (1-10) at the end of
     * the token, where the hash symbol is located. The Globic Log Service deals
     * with content from a file oriented perspective, so what you specify is a
     * URI of the files location.
     *
     * An example to specify two pieces of consumed content, document_1 and
     * document_2 with a URI location of http://www.example.com/filestore/,
     * would look as follows:
     * &contentConsumed1=<http://www.example.com/filestore/document_1>&contentConsumed2=<http://www.example.com/filestore/docuemnt_2>
     * .
     *
     * @contentConsumed1Type Here it is possible to specify up to 10 pieces of
     * content that were consumed or used by an activity. This is an optional
     * token in the request as not all activities consume content. Pieces of
     * content consumed are specified by appending a number (1-10) at the end of
     * the token, where the hash symbol is located. The Globic Log Service deals
     * with content from a file oriented perspective, so what you specify is a
     * URI of the files location.
     *
     * An example to specify two pieces of consumed content, document_1 and
     * document_2 with a URI location of http://www.example.com/filestore/,
     * would look as follows:
     * &contentConsumed1=<http://www.example.com/filestore/document_1>&contentConsumed2=<http://www.example.com/filestore/docuemnt_2>
     * .
     *
     * @contentGenerated1 Here it is possible to specify up to 10 pieces of
     * content that were generated by an activity. This is an optional token in
     * the request as not all activities generate content. Pieces of content are
     * specified by appending a number (1-10) at the end of the token, where the
     * hash symbol is located. The Globic Log Service deals with content from a
     * file oriented perspective, so what you specify is a URI of the files
     * location.
     *
     * An example to specify two pieces of content that were generated,
     * document_3 and document_4 with a URI location of
     * http://www.example.com/filestore/, would look as follows:
     * &contentGenerated1=<http://www.example.com/filestore/document_3>&contentGenerated2=<http://www.example.com/filestore/docuemnt_4>
     * .
     *
     * @contentGenerated1Type Here it is possible to specify the type of any of
     * the generated content similar to how you can do it for any content
     * consumed. The Globic semantic model contains the types of content that
     * can be generated for each activity so when specifying a type for a piece
     * of generated content, it must match that in the Globic model. This is an
     * optional token in the request; it is used to make the generated
     * provenance data more accurate. If it is not set then any consumed content
     * is set to a default type. In order to specify a type for a piece of
     * generated content, the number associated with a piece of generated
     * content is placed in the token, where the hash symbol is located.
     *
     * For example, if you have already specified some generated content:
     * &contentGenereated1=<http://www.example.com/filestore/document_3> . In
     * order to declare that document_3 is of type gic:Translation, you would
     * add the following to the request: &contentGenerated1Type=gic:Translation
     * .
     *
     * @return a response whether post was successful or not
     */
    public static String postGlobicContent(String componentName, String user, String password, String activity, String contentDir, String contentType,String content) {
        System.getProperties().put("proxySet", "true");
        System.getProperties().put("proxyHost", "www-proxy.cs.tcd.ie");
        System.getProperties().put("proxyPort", "8080");
        String output = "";
        String line = "";
        try {
            // Construct data
            String query = URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8");
            query += "&" + URLEncoder.encode("activity", "UTF-8") + "=" + URLEncoder.encode(activity, "UTF-8");
            query += "&" + URLEncoder.encode("user", "UTF-8") + "=" + URLEncoder.encode(user, "UTF-8");
            if (!content.equals("") && !content.equals(null) && content.equalsIgnoreCase("consumed")) {
                query += "&" + URLEncoder.encode("contentConsumed1", "UTF-8") + "=" + URLEncoder.encode(contentDir, "UTF-8");
                query += "&" + URLEncoder.encode("contentConsumed1Type", "UTF-8") + "=" + URLEncoder.encode(contentType, "UTF-8");
            }else
                if (!content.equals("") && !content.equals(null) && content.equalsIgnoreCase("generated")) {
                     query += "&" + URLEncoder.encode("contentGenerated1", "UTF-8") + "=" + URLEncoder.encode(contentDir, "UTF-8");
                     query += "&" + URLEncoder.encode("contentGenerated1Type", "UTF-8") + "=" + URLEncoder.encode(contentType, "UTF-8");
                }
            // Send data
           // System.out.println(query+content+contentType);
            URL url = new URL("http://kdeg-vm-24.scss.tcd.ie/globicLog/service/" + componentName + "/");
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(query);
            wr.flush();

            // Get the response
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((line = rd.readLine()) != null) {
                output = output + line;
            }
            wr.close();
            rd.close();
        } catch (IOException e) {
        }
        return output;
    }


    /**
     * GET method for retrieving Globic data from the triple store
     * 
     * @componentName Here you specify what name you want to call your component. 
     * Component name must be unique. It is not possible to have two or more 
     * components with the same name registered with the service.
     * 
     * @password
     * Here you specify your password which will be used to verify your
     * component when making requests to the system. 
     * 
     * @retrieveData Here it is
     * possible to specify the name of some data for which you wish to retrieve
     * provenance records about. It is possible to retrieve provenance records
     * about a piece of content, a component, an activity or a user. For
     * example: To retrieve provenance records about a piece content, you would
     * add the following to the request:
     *
     * @retrieveData=<http://www.example.com/filestore/document_1>. To retrieve
     * provenance records about a component, an activity or a user, you would
     * add the following to the request: Component:
     * &retrieveData=gic:MockComponent_1 Activity:
     * &retrieveData=gic:translateActivity101 User: &retrieveData=gic:Joe_Bloggs
     *
     * Note the difference between retrieving provenance records about a piece
     * of content and a component, activity or a user. Content is always
     * declared as a URI and a component, activity or a user always use the gic:
     * namespace.
     * 
     * @return return the retrieved Globic data
     * 
     * @throws IOException
     * @throws JSONException
     */
    public static JSONObject retrieveGlobicData(String componentName, String password, String retrieveData) throws IOException, JSONException {
        String url = "http://kdeg-vm-24.scss.tcd.ie/globicLog/service/" + componentName + "/?password=" + password + "&retrieveData=" + retrieveData;
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = jsonToString(rd);
            JSONObject json = new JSONObject(jsonText);
            return json;
        } finally {
            is.close();
        }
    }
    
    /**
     * 
     * @rd the JSON file to be converted 
     * 
     * @return convert JSON to String
     */
    private static String jsonToString(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    /**
     * 
     * Main with example usage
     * 
     * @param args
     * @throws MalformedURLException
     * @throws IOException
     * @throws JSONException
     */
    public static void main(String args[]) throws MalformedURLException, IOException, JSONException {
        String user = "finnle";
        String password = "123456";
        String componentName = "MockComponent_1";
        String activity = "my_translate";
        String contentConsumed = "<http://kdeg-vm-13.scss.tcd.ie:8080/alfresco/d/d/workspace/SpacesStore/c8bd7f0d-3e7a-4bcd-95ca-4609a7658d37/f103941.csv>";
        String contentConsumedType = "nif:String";
        String partitionName = "";
        String content="consumed";

        //register component
        System.out.println(Globic.registerComponent(componentName, password, partitionName));

        //post globic content
        System.out.println(Globic.postGlobicContent(componentName, user, password, activity, contentConsumed, contentConsumedType,content));

        //retrieve data
        JSONObject json = retrieveGlobicData(componentName, password, "gic:my_translate");
        System.out.println(json.toString());

    }
}
