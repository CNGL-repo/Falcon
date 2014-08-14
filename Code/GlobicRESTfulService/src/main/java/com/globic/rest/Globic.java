package com.globic.rest;

import alfresco.repo.RepositoryHandler;
import com.xmlintl.projectmanagergui.webservice.v2.customer.api.common.XTMFileDescriptorAPI;
import com.xmlintl.projectmanagergui.webservice.v2.customer.api.common.XTMJobDescriptorAPI;
import com.xmlintl.projectmanagergui.webservice.v2.customer.api.project.download.response.XTMJobFileBaseResponseAPI;
import com.xmlintl.projectmanagergui.webservice.v2.customer.api.project.template.response.XTMTemplateResponseAPI;
import customer.configuration.XTMAPICustomerConfiguration;
import customer.impl.XTMAPICustomerScenarios;
import falcon.file.Files;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.xml.transform.*;
import org.apache.commons.validator.routines.UrlValidator;
import twitter4j.internal.org.json.JSONException;
import xliff2n3.ContentLogging;
//import xliff2n3.Main;


@Path("/linkedData")
public class Globic {

    @POST
    @Path("/postXTMData")
    public Response getMsg(
            @DefaultValue("4292") @QueryParam("jobId") String jobId,
            @DefaultValue("tcd") @QueryParam("username") String username,
            @DefaultValue("cngl1234") @QueryParam("password") String password,
            @DefaultValue("18") @QueryParam("userid") String userid)
            throws IOException, JSONException, TransformerConfigurationException, TransformerException {
        String output="";
        System.getProperties().put("proxySet", "true");
        System.getProperties().put("proxyHost", "www-proxy.cs.tcd.ie");
        System.getProperties().put("proxyPort", "8080");

      
        Long jL = Long.parseLong(jobId);
        Long uL = Long.parseLong(userid);
        //=============GET FILE FROM XTM
        XTMAPICustomerConfiguration configuration = new XTMAPICustomerConfiguration(username, uL, password);
        XTMAPICustomerScenarios scenarios = new XTMAPICustomerScenarios(configuration);

        //////////////////////// SCENARIOS ////////////////////////

        scenarios.getXTMInfo();
        scenarios.getSupportedFilesInfo();

        final XTMTemplateResponseAPI templateResponse = scenarios.createTemplate();

        if (templateResponse != null && templateResponse.getTemplateDescriptor() != null) {
            scenarios.obtainPMTemplateEditorLink(templateResponse.getTemplateDescriptor());
        }

        //List of job
        List<XTMJobDescriptorAPI> jobsDescriptors = new ArrayList<XTMJobDescriptorAPI>();
        XTMJobDescriptorAPI xjda = new XTMJobDescriptorAPI();

        //set job id describing desired job 4292L
        xjda.setId(jL);

        //add to job List
        jobsDescriptors.add(xjda);

        //download desired file  
        final XTMJobFileBaseResponseAPI jobFile = scenarios.generateJobFile(jobsDescriptors);
        final List<XTMFileDescriptorAPI> fileDescriptors = Collections.singletonList(jobFile.getFileDescriptor());
        scenarios.checkJobFileCompletion(fileDescriptors);
        scenarios.downloadJobFileMTOM(fileDescriptors);

        //unzip    
        //parse file and log
        File folder = new File("/home/finnle/falcon");
        File[] listOfFiles = folder.listFiles();

        for (File listOfFile : listOfFiles) {
            if (listOfFile.isFile()) {
                System.out.println("File " + listOfFile.getName());
            } else if (listOfFile.isDirectory()) {
                output = "Directory==" + listOfFile.getName();
                System.out.println(output);
            }
        }

        
        String tippFile=listOfFiles[0].toString(); 
        List<String> tippFileContents=Files.unpackTIPP(tippFile, "/home/finnle/falcon");
        String xliffFilename=tippFileContents.get(0);
        
        //convert to rdf
        output=ContentLogging.logging(xliffFilename, "falcon");
        
        //remove files

        return Response.status(200).entity(output).build();
    }

    /**
     * PUT method for registering Globic Components
     *
     * @param componentName Here you specify what name you want to call your
     * component. Component name must be unique. It is not possible to have two
     * or more components with the same name registered with the service.
     *
     * @param password Here you specify your password which will be used to verify
     * your component when making requests to the system. It is recommended not
     * use a password you have for any other accounts.
     *
     * @param partition Here you can specify a partition in the triple-store in which
     * your component will store data to and retrieve data from. This is an
     * optional token in the request. If it is not specified, you will record
     * data to and retrieve data from a default partition in the triple-store.
     * This option can be useful if you want your own 'sandbox' so provenance
     * data from other components will not be appear in your partition. It is
     * also possible for multiple components to specify the partition name if
     * they wish to share the same provenance data.
     *
     * @return an HTTP response with content of the updated or created resource.
     */
    @PUT
    @Path("/registerGlobicComponent")
    public String registerGlobicComponent(
            @DefaultValue("MockComponent_1") @QueryParam("componentName") String componentName,
            @DefaultValue("123456") @QueryParam("password") String password,
            @DefaultValue("") @QueryParam("partition") String partition) {
        String output = alfresco.globic.Globic.registerComponent(componentName, password, partition);
        return output;
    }

    /**
     * GET method for retrieving Globic data from the triple store
     *
     *
     * @param componentName Here you specify what name you want to call your
     * component. Component name must be unique. It is not possible to have two
     * or more components with the same name registered with the service.
     *
     *
     * @param password Here you specify your password which will be used to verify
     * your component when making requests to the system. @retrieveData Here it
     * is possible to specify the name of some data for which you wish to
     * retrieve provenance records about. It is possible to retrieve provenance
     * records about a piece of content, a component, an activity or a user. For
     * example: To retrieve provenance records about a piece content, you would
     * add the following to the request:
     *
     * @param retrieveData=<http://www.example.com/filestore/document_1>. To retrieve
     * provenance records about a component, an activity or a user, you would
     * add the following to the request: Component:
     * &retrieveData=gic:MockComponent_1 Activity:
     * &retrieveData=gic:translateActivity101 User: &retrieveData=gic:Joe_Bloggs
     *
     * Note the difference between retrieving provenance records about a piece
     * of content and a component, activity or a user. Content is always
     * declared as a URI and a component, activity or a user always use the gic:
     * namespace.
     * @return Response
     */
    @GET
    @Path("/retrieveGlobicData")
    public Response retrieveGlobicData(
            @DefaultValue("MockComponent_1") @QueryParam("componentName") String componentName,
            @DefaultValue("123456") @QueryParam("password") String password,
            @DefaultValue("gic:my_translate") @QueryParam("retrieveData") String retrieveData) throws IOException, JSONException {
        twitter4j.internal.org.json.JSONObject output = alfresco.globic.Globic.retrieveGlobicData(componentName, password, retrieveData);
        return Response.status(200).entity(output.toString()).build();
    }

    /**
     * POST content file to Alfresco and then transform file data into Globic
     * Model.
     *
     * @param componentName Here you specify what name you want to call your
     * component. Component name must be unique. It is not possible to have two
     * or more components with the same name registered with the service.
     * @param partitionName
     *
     * @param password Here you enter your password.
     *
     * @param activity Here you enter the activity that is being carried out by your
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
     * @param user Here you can specify if a user was involved in an activity along
     * with a component. This is an optional token in the request as a user does
     * not always have to be involved in an activity. The user can be the name
     * of a person or a worker ID number etc. If specifying the user as the name
     * of a person with a first and last name, do not use a space between the
     * names, instead use humpback notation or use an underscore, for example:
     * JoeBloggs or Joe_Bloggs.
     *
     * @param contentConsumed1 Here it is possible to specify up to 10 pieces of
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
     * @param contentConsumed1Type Here it is possible to specify up to 10 pieces of
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
     * @param contentGenerated1 Here it is possible to specify up to 10 pieces of
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
     * @param contentGenerated1Type Here it is possible to specify the type of any of
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
     * @param contentJobType
     *
     * @return an HTTP response with content of the updated or created resource.
     */
    @POST
    @Path("/postGlobicData")
    public Response postGlobicData(
            @DefaultValue("2070_test") @QueryParam("componentName") String componentName,
            @DefaultValue("test") @QueryParam("partitionName") String partitionName,
            @DefaultValue("cngl1") @QueryParam("password") String password,
            @DefaultValue("translate_12345") @QueryParam("activity") String activity,
            @DefaultValue("finnle") @QueryParam("user") String user,
            @DefaultValue("http://kdeg-vm-13.scss.tcd.ie/2070.xlf") @QueryParam("contentConsumed1") String contentConsumed1,
            @DefaultValue("nif:String") @QueryParam("contentConsumed1Type") String contentConsumed1Type,
            @QueryParam("contentGenerated1") String contentGenerated1,
            @QueryParam("contentGenerated1Type") String contentGenerated1Type,
            @DefaultValue("consumed") @QueryParam("contentJobType") String contentJobType) throws IOException, JSONException {
        

        String output = "empty";
        
        System.getProperties().put("proxySet", "true");
        System.getProperties().put("proxyHost", "www-proxy.cs.tcd.ie");
        System.getProperties().put("proxyPort", "8080");
        
        UrlValidator uv=new UrlValidator();
        boolean isValidURL=uv.isValid(contentConsumed1);
        
        if(isValidURL==true){
            try {
                RepositoryHandler handler = new RepositoryHandler("http://phaedrus.scss.tcd.ie/CS3BC2/group6/tomcat/alfresco/cmisatom", "admin", "admin", 1);
                output=handler.sendContentToCMS(contentConsumed1, activity, user, password, contentJobType, contentConsumed1Type, componentName,partitionName);
            } catch (IOException ex) {
                return Response.status(400).entity(ex.toString()).build();

            }

            if(output.equals("empty")){
                return Response.status(400).entity("Please check file link is correct").build();
            }else

            if(output.contains("Current stage:Parsing")){
                return Response.status(400).entity("Please check the input file as there is a parse error with the file").build();
            }else{
                 return Response.status(200).entity(output).build();
            }

        }else{
            return Response.status(400).entity("URL is not valid. Please make sure URL is of the form http://www.example.com/filename.xml or https://www.example.com/filename.xml").build();
        }
    } 
}