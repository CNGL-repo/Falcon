package com.globic.rest;

import alfresco.repo.RepositoryHandler;
import java.io.IOException;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import twitter4j.internal.org.json.JSONException;

/**
 * REST Web Service
 *
 * Globic RESTful web service for document based media for audio or video based
 * media please use the direct PHP rest based apiof which this .system is based
 *
 * @author Leroy
 */
@Path("/globic")
public class Globic {

    @Context
    private UriInfo context;

    /**
     * PUT method for registering Globic Components
     *
     * @componentName Here you specify what name you want to call your
     * component. Component name must be unique. It is not possible to have two
     * or more components with the same name registered with the service.
     *
     * @password Here you specify your password which will be used to verify
     * your component when making requests to the system. It is recommended not
     * use a password you have for any other accounts.
     *
     * @partition Here you can specify a partition in the triple-store in which
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
     * @componentName Here you specify what name you want to call your
     * component. Component name must be unique. It is not possible to have two
     * or more components with the same name registered with the service.
     *
     * 
     * @password Here you specify your password which will be used to verify your
     * component when making requests to the system. @retrieveData Here it is
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
     * @componentName Here you specify what name you want to call your
     * component. Component name must be unique. It is not possible to have two
     * or more components with the same name registered with the service.
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
     * @return an HTTP response with content of the updated or created resource.
     */
    @POST
    @Path("/postGlobicData")
    @Consumes("application/x-www-form-urlencoded")
    public Response postGlobicData(
            @DefaultValue("MockComponent_1") @QueryParam("componentName") String componentName,
            @DefaultValue("123456") @QueryParam("password") String password,
            @DefaultValue("translate_1234") @QueryParam("activity") String activity,
            @DefaultValue("user") @QueryParam("user") String user,
            @DefaultValue("C:\\Users\\Leroy\\Desktop\\resttest.txt") @QueryParam("contentConsumed1") String contentConsumed1,
            @DefaultValue("gic:Translation") @QueryParam("contentConsumed1Type") String contentConsumed1Type, @QueryParam("contentGenerated1") String contentGenerated1,
            @QueryParam("contentGenerated1Type") String contentGenerated1Type) throws IOException, JSONException {

        String output = "";

        try {
            RepositoryHandler handler = new RepositoryHandler("http://phaedrus.scss.tcd.ie/CS3BC2/group6/tomcat/alfresco/cmisatom", "admin", "admin", 1);
            handler.sendContentToCMS(contentConsumed1, activity, user, password, "generated", contentConsumed1Type, componentName);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Response.status(200).entity(output).build();
    }
}
