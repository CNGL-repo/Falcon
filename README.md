##Falcon Project L3Data component

###Installation
For install and run the Globic REST service you need a **local installation** of a Tomcat server.

Download and compile the contents of the **GlobicBootstrap**, **logger** and **GlobicRESTfulService** folders. GlobicBootstrap and logger and libraries used in the GlobicRESTfulService project.

You may want to change some **configuration** of the service, which can be done by modifying the contents of the *config.properties* file on logger *logger/src/xliff2n3*.

If you have a local installation of the **globicLog**, **Apache Jena** and **Alfresco** you may want to change the target URL where the RESTful service do the calls by changing the URL's on *GlobicBootstrap/src/alfresco/globic/Globic.java*, *GlobicBootstrap/src/alfresco/repo/RepositoryHandler.java* and *GlobicBootstrap/src/alfresco/workflow/AlfrescoWorkflow.java* before compiling them.

Once everything is compiled deploy the service to the Tomcat server.

###Functionality
The service has 3 core functionality, register components, retrieve data and submit data.
Before be able to submit data, you **must** register a component. You can do so by calling *<web path>LinkedData/registerGlobicComponent* with a PUT HTTP method. The parameters are sent either on the URL or on the request body (like with POST). The parameters are as follows:

- **componentName**: The name of the component to be registered on the system
- **password**: Optional. Specify a password for the component which is being created
- **partition**: Optional. Specify a partition (graph) on the Jena triplestore where the component will be created

After the component is created, you can submit data on it by calling *<web path>LinkedData/postGlobicData* with a POST HTTP method. The parameters must be sent on the request body:

- **componentName**: The name of the component where the data will be submitted
- **password**: Optional. Specify the password of the component, if any
- **partition**: Optional. The partition from where the data will be retrieved
- **activity**: The name of the activity. You can find more info about this on the globicLog user guide
- **user**: Optional. If you want to specify an user related to the content being submitted
- **contentConsumed1**: Optional. The content to be consumed. Not all activities consume content. More info on the globicLog user guide.
- **contentConsumed1Type**: Type of content being consumed. More info on the globicLog user guide.
- **contentJobType**: This can be either consumed or generated.

After submitting the content, you can retrieve it by calling *<web path>LinkedData/retrieveGlobicData* with a GET HTTP method. The parameters must be sent on the URL of the request:

- **componentName**: The name of the component from where the data should be retrieved from
- **password**: Optional. Specify the password of the component, if any
- **retrieveData**: The name of the activity to retrieve the data from
