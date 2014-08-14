package alfresco.repo;

import alfresco.globic.Globic;
import java.io.*;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import xliff2n3.Constants;
//import xliff2n3.Main;

/**
 *
 * This class is responsible for connecting and interacting with the Alfresco
 * CMS
 *
 * @author Leroy
 */
public class RepositoryHandler {

    /**
     *
     * Repository user name
     *
     */
    private String repositoryUsername;
    /**
     *
     * Repository password
     *
     */
    private String repositoryPasword;
    /**
     *
     * Repository type
     *
     */
    public int repositoryType;
    /**
     *
     * Repository URL location
     *
     */
    public String repositoryURL;
    /**
     *
     * Repository session variable
     *
     */
    public Session session;
    /**
     *
     * Alfresco storage number
     *
     */
    public static final int REP_TYPE_ALFRESCO = 1;
    /**
     *
     * Nuxeo storage number
     *
     */
    public static final int REP_TYPE_NUXEO = 2;
    /**
     *
     * Folder to browse
     *
     */
    public Folder browseFolder;
    /**
     *
     * Translate Rules Folder
     *
     */
    public Folder translateRulesFolder;

    /**
     *
     * RepositoryHandler object for connecting to an Alfresco store
     *
     * @param repositoryURL Alfresco repository URL location
     * @param repositoryUsername Alfresco repository user name
     * @param repositoryPassword Alfresco repository password
     * @param repositoryType Alfresco repository type
     */
    public RepositoryHandler(String repositoryURL, String repositoryUsername, String repositoryPassword, int repositoryType) {
        this.repositoryUsername = repositoryUsername;
        this.repositoryPasword = repositoryPassword;
        this.repositoryType = repositoryType;
        this.repositoryURL = repositoryURL;

        SessionFactory sessionFactory = SessionFactoryImpl.newInstance();
        Map<String, String> parameter = new HashMap<String, String>();

        parameter.put(SessionParameter.USER, repositoryUsername);
        parameter.put(SessionParameter.PASSWORD, repositoryPasword);

        if (repositoryType == REP_TYPE_ALFRESCO) {
            parameter.put(SessionParameter.OBJECT_FACTORY_CLASS,
                    "org.alfresco.cmis.client.impl.AlfrescoObjectFactoryImpl");
        }
        parameter.put(SessionParameter.ATOMPUB_URL, repositoryURL);
        parameter.put(SessionParameter.BINDING_TYPE,
                BindingType.ATOMPUB.value());

        System.out.println("Accessing ATOMPUB_URL: "
                + parameter.get(SessionParameter.ATOMPUB_URL) + " userid: "
                + parameter.get(SessionParameter.USER) + " password: "
                + parameter.get(SessionParameter.PASSWORD));

        List<Repository> repositories = new ArrayList<Repository>();
        repositories = sessionFactory.getRepositories(parameter);
        for (Repository r : repositories) {
            System.out.println("-- Found repository: " + r.getName());
        }

        Repository repository = repositories.get(0);
        parameter.put(SessionParameter.REPOSITORY_ID, repository.getId());
        session = sessionFactory.createSession(parameter);

        System.out.println("-- Connected to repository: "
                + repository.getName() + ", with id: " + repository.getId());

        //get workflow engines
        // ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();    
    }

    /**
     *
     * Create empty file in repository
     *
     * @param filename The name of the file to be created
     */
    public void createEmptyFile(String filename) {
        try {
            String command =
                    "java -jar /home/finnle/AlfrescoSample.jar " + filename;
            final Process process = Runtime.getRuntime().exec(command);
            BufferedReader buf = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));
            buf.close();
            int returnCode = process.waitFor();
            System.out.println("Return code = " + returnCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * Read file and return contents as a string
     *
     * @param filename The name of the file to read
     * @return
     */
    public static String readFile(String filename) {

        //read content from file
        BufferedReader br = null;
        String data = "";
        try {

            String sCurrentLine = "";

            br = new BufferedReader(new FileReader("" + filename));

            while ((sCurrentLine = br.readLine()) != null) {
                System.out.println(sCurrentLine);
                data = data + "\n" + sCurrentLine;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return data;

    }

    /**
     *
     * @param fileDir -(This is for testing remove)
     *
     * @@param componentName Here you specify what name you want to call your
     * component. Component name must be unique. It is not possible to have two
     * or more components with the same name registered with the service.
     *
     * @param password Here you enter your password.
     *
     * @param activity Here you enter the activity that is being carried out by
     * your component. An activity is limited to the list of content processing
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
     * @param user Here you can specify if a user was involved in an activity
     * along with a component. This is an optional token in the request as a
     * user does not always have to be involved in an activity. The user can be
     * the name of a person or a worker ID number etc. If specifying the user as
     * the name of a person with a first and last name, do not use a space
     * between the names, instead use humpback notation or use an underscore,
     * for example: JoeBloggs or Joe_Bloggs.
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
     * @param contentConsumed1Type Here it is possible to specify up to 10
     * pieces of content that were consumed or used by an activity. This is an
     * optional token in the request as not all activities consume content.
     * Pieces of content consumed are specified by appending a number (1-10) at
     * the end of the token, where the hash symbol is located. The Globic Log
     * Service deals with content from a file oriented perspective, so what you
     * specify is a URI of the files location.
     *
     * An example to specify two pieces of consumed content, document_1 and
     * document_2 with a URI location of http://www.example.com/filestore/,
     * would look as follows:
     * &contentConsumed1=<http://www.example.com/filestore/document_1>&contentConsumed2=<http://www.example.com/filestore/docuemnt_2>
     * .
     *
     * @param contentGenerated1 Here it is possible to specify up to 10 pieces
     * of content that were generated by an activity. This is an optional token
     * in the request as not all activities generate content. Pieces of content
     * are specified by appending a number (1-10) at the end of the token, where
     * the hash symbol is located. The Globic Log Service deals with content
     * from a file oriented perspective, so what you specify is a URI of the
     * files location.
     *
     * An example to specify two pieces of content that were generated,
     * document_3 and document_4 with a URI location of
     * http://www.example.com/filestore/, would look as follows:
     * &contentGenerated1=<http://www.example.com/filestore/document_3>&contentGenerated2=<http://www.example.com/filestore/docuemnt_4>
     * .
     *
     * @param contentGenerated1Type Here it is possible to specify the type of
     * any of the generated content similar to how you can do it for any content
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
     *
     */
    public String sendContentToCMS(String fileDir, String activity, String user, String password, String contentJobType, String contentType, String component,String partitionName) throws MalformedURLException, IOException {
        
        Properties prop = new Properties();
	InputStream inputConfig = null;
 
        inputConfig = new FileInputStream(Constants.CONFIG_FILENAME);
        
        // load a properties file
	prop.load(inputConfig);
        
        String pushReport ="";
        //check for tipp or zip
        String filename = "";
        if (fileDir.contains("/")) {
            //linux filepath
            filename = fileDir.substring(fileDir.lastIndexOf("/"), fileDir.length()).replace("/", "");
            System.out.println(filename);
        } else if (fileDir.contains("\\")) {
            //windows filepath
            filename = fileDir.substring(fileDir.lastIndexOf("\\"), fileDir.length()).replace("\\", "");
            System.out.println(filename);
        }
        
        
        //RepositoryHandler handler = new RepositoryHandler(repositoryURL, repositoryUsername,
          //      repositoryPasword, repositoryType);

        //upload to place holder folder
      //  Folder root = session.getRootFolder();
        Path target = Paths.get(prop.getProperty("globicLocalFolder")+filename);
        URL website = new URL(fileDir);
        Files.copy(website.openStream(), target, StandardCopyOption.REPLACE_EXISTING);
        
        
        //check if file is downloaded
        File f = new File(prop.getProperty("globicLocalFolder")+filename);
	  if(f.exists()){
		  System.out.println("File existed");
	  }else{
                   return "File not downloaded";
	  }
        
        //read content from file
        /*BufferedReader br = null;
        String data = readFile(prop.getProperty("globicLocalFolder")+filename);
      
        //create new file with properties else
        byte[] buf = null;
        try {
            buf = data.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        ByteArrayInputStream input = new ByteArrayInputStream(buf);

        ContentStream cs = session.getObjectFactory().createContentStream(filename, buf.length, "text/xml", input);
        System.out.println("Step 1: Write CMIS properties to the file");
        Map<String, Object> folderProps = new HashMap<String, Object>();
        folderProps.put(PropertyIds.OBJECT_TYPE_ID, "D:loc:doc");
        folderProps.put(PropertyIds.NAME, filename);
        folderProps.put("globic:user", user);
        folderProps.put("globic:password", password);
        folderProps.put("globic:activity", activity);
        folderProps.put("globic:content", contentJobType);
        folderProps.put("globic:contentType", contentType);
        folderProps.put("globic:processed", "no");

        org.apache.chemistry.opencmis.client.api.Document doc;
        try {
            System.out.println("Step 2: Put content in the file and place in the CMS");
            doc = root.createDocument(folderProps, cs, VersioningState.MAJOR);
            doc.updateProperties(folderProps, true);
            System.out.println("File added to alfresco with properties");
        } catch (Exception ex) {

            System.out.println("File exists then just update the content in the file");
            System.out.println("Step 2: Update content in the file");
            data = readFile(filename);
            CmisObject cmisFile = session.getObjectByPath("/" + filename);

            byte[] fileContent = null;
            try {
                fileContent = data.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            ByteArrayInputStream filestream = new ByteArrayInputStream(fileContent);

            ContentStream filecontentStream = new ContentStreamImpl(filename, BigInteger.valueOf(data.length()), "text/xml", filestream);
            doc = (Document) cmisFile;
            doc = (Document) session.getObject(doc.checkOut());
            doc.checkIn(false, null, filecontentStream, "just a minor change");
        }

        System.out.println("Step 3: get spaceStore ID");
        String spaceStore = handler.getAlfrescoProperty(filename, "alfcmis:nodeRef");
*/
      //  String contentConsumed = "http://kdeg-vm-13.scss.tcd.ie:8080/alfresco/d/d/workspace/" + spaceStore.replace("workspace://", "") + "/" + filename;
        String contentConsumed="http://kdeg-vm-13.scss.tcd.ie:8080/alfresco/d/d/workspace/" + "/" + filename;
          System.out.println("spaceStore===" + contentConsumed);

        System.out.println("Step 4:Push content to globic using the REST API");
        pushReport = Globic.postGlobicContent(component, user, password, activity, contentConsumed, contentType, contentJobType,prop.getProperty("globicLocalFolder")+filename,partitionName);
        System.out.println(pushReport);
        if (!pushReport.contains("Error")) {
  //          handler.setProperty(filename, user, password, activity, contentJobType, contentType, "yes");
        }
        
        //deletefile
        Files.delete(target);
        
        return pushReport;
    }

    /**
     *
     * Get Alfresco CMIS properties
     *
     * @param filename documents filename
     * @param property the property of the document that you want
     * @return the CMIS property
     */
    public String getAlfrescoProperty(String filename, String property) {

        Document doc = (Document) session.getObjectByPath("/" + filename);
        List<Property<?>> properties = doc.getProperties();
        String propertyValue = "";
        for (Property<?> p : properties) {
            if (p.getFirstValue() == null) {
                if (p.getId().equals(property)) {
                    System.out.println(p.getId() + "\t" + p.getLocalName() + "=" + p.getFirstValue());
                    propertyValue = p.getFirstValue() + "";
                }
            } else {
                if (p.getId().equals(property)) {
                    System.out.println(p.getId() + "\t" + p.getLocalName() + "=" + p.getFirstValue());
                    propertyValue = p.getFirstValue() + "";
                }
            }
        }
        return propertyValue;
    }

    /**
     * Get the Globic CMIS properties
     *
     * @param filename the file name of the document
     * 
     * @return an ArrayList containing all the Globic CMIS property values
     */
    public ArrayList<String> getGlobicProperties(String filename) {

        ArrayList<String> globicProperties = new ArrayList<String>();

        Document doc = (Document) session.getObjectByPath("/" + filename);
        List<Property<?>> properties = doc.getProperties();
        
        for (Property<?> p : properties) {
            if (p.getFirstValue() == null) {
                if (p.getId().contains("globic")) {
                    System.out.println(p.getId() + "\t" + p.getLocalName() + "=" + p.getFirstValue());
                }
            } else {
                if (p.getId().contains("globic")) {
                    System.out.println(p.getId() + "\t" + p.getLocalName() + "=" + p.getFirstValue());
                }
            }
        }
        return globicProperties;
    }

    /**
     *
     * Get all CMIS properties
     * 
     * @param filename the file name of the document
     * 
     * @returnan ArrayList containing all the CMIS property values
     */
    public ArrayList<String> getAllProperties(String filename) {

        ArrayList<String> globicProperties = new ArrayList<String>();

        Document doc = (Document) session.getObjectByPath("/" + filename);
        List<Property<?>> properties = doc.getProperties();
        for (Property<?> p : properties) {
            if (p.getFirstValue() == null) {
                if (p.getId().contains("globic")) {
                    System.out.println(p.getId() + "\t" + p.getLocalName() + "=" + p.getFirstValue());
                }
            } else {
                if (p.getId().contains("globic")) {
                    System.out.println(p.getId() + "\t" + p.getLocalName() + "=" + p.getFirstValue());
                }
            }
        }
        return globicProperties;
    }

    /**
     *
     * @param filename
     */
    public void viewExampleOProperties(String filename) {

        Document doc = (Document) session.getObjectByPath("/" + filename);
        List<Property<?>> properties = doc.getProperties();

        for (Property<?> p : properties) {
            if (p.getFirstValue()
                    == null) {
                System.out.println(p.getId() + "\t" + p.getLocalName()
                        + "=" + p.getFirstValue());
            } else {
                System.out.println(p.getId()
                        + "\t" + p.getLocalName() + "=" + p.getFirstValue());
            }
        }

    }

    /**
     *
     * Set the CMIS Globic properties
     * 
     * @param filename file name
     * @param user Globic username
     * @param password Globic password 
     * @param activity Globic activity
     * @param content the Globic content
     * @param contentType the Globic content type
     * @param processed Globic was it processed sucessfully or not
     */
    public void setProperty(String filename, String user, String password, String activity, String content, String contentType, String processed) {

        Document doc = (Document) session.getObjectByPath("/" + filename);
        Map<String, Object> folderProps = new HashMap<String, Object>();
        folderProps.put(PropertyIds.OBJECT_TYPE_ID, "D:loc:doc");
        folderProps.put(PropertyIds.NAME, filename);
        folderProps.put("globic:user", user);
        folderProps.put("globic:password", password);
        folderProps.put("globic:activity", activity);
        folderProps.put("globic:content", content);
        folderProps.put("globic:contentType", contentType);
        folderProps.put("globic:processed", processed);

        doc.updateProperties(folderProps, true);

    }

    /**
     *
     * Get the Alfresco root folder
     * 
     */
    public void browserGetRoot() {
        browseFolder = session.getRootFolder();
    }

    /**
     *
     * Get the Alfresco path
     * 
     * @return the Alfresco path
     */
    public String browserGetPath() {
        return browseFolder.getPath();
    }

    /**
     *
     * @return
     */
    public String browserFolderToString() {
        StringBuffer returnValue = new StringBuffer("");
        ItemIterable<CmisObject> children = browseFolder.getChildren();
        for (CmisObject o : children) {
            returnValue.append(o.getBaseTypeId());
            returnValue.append("\t");
            returnValue.append(o.getName());
            returnValue.append("\n");
        }
        return returnValue.toString();
    }

    /**
     *
     * @param subFolder
     */
    public void browserChangePath(String subFolder) {
        if ((subFolder.equalsIgnoreCase(".."))
                || (subFolder.equalsIgnoreCase("../"))) {
            List<Folder> parents = browseFolder.getParents();
            if (parents.isEmpty()) {
                System.out.println("Folder has no parents.");
            } else if (parents.size() > 1) {
                int selection = -1;
                while ((selection < 0) || (selection >= parents.size())) {
                    System.out.print("Folder has multiple parents. Please select which one: ");
                    int i = 0;
                    for (Folder f : parents) {
                        System.out.println(i + "\t" + f.getPath() + "/"
                                + f.getName());
                        i++;
                    }
                    selection = readInt();
                    if (!((selection < 0) || (selection >= parents.size()))) {
                        browseFolder = parents.get(selection);
                    }
                }
            } else {
                browseFolder = parents.get(0);
            }
        } else {
            String newPath;
            if (browseFolder.isRootFolder()) {
                newPath = browseFolder.getPath() + subFolder;
            } else {
                newPath = browseFolder.getPath() + "/" + subFolder;
            }
            try {
                browseFolder = (Folder) session.getObjectByPath(newPath);
            } catch (CmisObjectNotFoundException e) {
                System.out.println("Folder not found: " + newPath);
            } catch (ClassCastException e) {
                System.out.println("Object is not a CMIS folder: " + newPath);
            }
        }
    }

    /**
     *
     * @return
     */
    public static int readInt() {
        String line = null;
        int value = 0;
        try {
            BufferedReader is = new BufferedReader(new InputStreamReader(
                    System.in));
            line = is.readLine();
            value = Integer.parseInt(line);
        } catch (NumberFormatException ex) {
            System.err.println("Not a valid number: " + line);
        } catch (IOException e) {
            System.err.println("Unexpected IO ERROR: " + e);
        }
        return value;
    }

    /**
     *
     * @param objectName
     */
    public void browserDeleteObject(String objectName) {
        String objectPath;
        CmisObject object;
        if (browseFolder.isRootFolder()) {
            objectPath = browseFolder.getPath() + objectName;
        } else {
            objectPath = browseFolder.getPath() + "/" + objectName;
        }
        try {
            object = session.getObjectByPath(objectPath);
            object.delete(true);
        } catch (CmisObjectNotFoundException e) {
            System.out.println("Object not found: " + objectPath);
        } catch (CmisConstraintException e) {
            System.out.println("Could not delete '" + objectPath + "': "
                    + e.getMessage());
        }
    }

    /**
     *
     * @param objectName
     * @param objectPath
     */
    public void browserDeleteObject(String objectName, String objectPath) {
        CmisObject object;
        try {
            object = session.getObjectByPath(objectPath);
            object.delete(true);
        } catch (CmisObjectNotFoundException e) {
            System.out.println("Object not found: " + objectPath);
        } catch (CmisConstraintException e) {
            System.out.println("Could not delete '" + objectPath + "': "
                    + e.getMessage());
        }
    }

    /**
     *
     * @param objectName
     */
    public void rulesDelete(String objectName) {
        String objectPath;
        Folder ruleFolder;
        objectPath = "/TranslateRules/" + objectName;
        try {
            ruleFolder = (Folder) session.getObjectByPath(objectPath);
            ItemIterable<CmisObject> children = ruleFolder.getChildren();
            for (CmisObject o : children) {
                FileableCmisObject child = (FileableCmisObject) o;
                child.removeFromFolder(ruleFolder);
            }
            ruleFolder.delete(true);
        } catch (CmisObjectNotFoundException e) {
            System.out.println("Object not found: " + objectPath);
        } catch (CmisConstraintException e) {
            System.out.println("Could not delete '" + objectPath + "': "
                    + e.getMessage());
        }
    }

    /**
     *
     * @param folderName
     */
    public void browserMakeFolder(String folderName) {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
        properties.put(PropertyIds.NAME, folderName);
        try {
            browseFolder.createFolder(properties);
        } catch (CmisContentAlreadyExistsException e) {
            System.out.println("Could not create folder '" + folderName + "': "
                    + e.getMessage());
        }
    }

    /**
     *
     * @param fileName
     */
    public void browserMakeFile(String fileName) {
        Map<String, Object> properties = getDefaultFileProperties();
        properties.put(PropertyIds.NAME, fileName);
        properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");

        try {
            Document doc = browseFolder.createDocument(properties, null, null);
            doc.updateProperties(properties, true);
        } catch (CmisContentAlreadyExistsException e) {
            System.out.println("Could not create file '" + fileName + "': "
                    + e.getMessage());
        }
    }

    /**
     *
     * @param fileName
     */
    public void browserMakeSampleFile(String fileName) {
        Map<String, Object> properties = getDefaultFileProperties();
        properties.put(PropertyIds.NAME, fileName);
        properties.put(PropertyIds.OBJECT_TYPE_ID, "D:loc:doc");
        try {
            String docText = "This is a sample document";
            byte[] content = docText.getBytes();
            InputStream stream = new ByteArrayInputStream(content);
            ContentStream contentStream = session.getObjectFactory().createContentStream(fileName, Long.valueOf(content.length), "text/plain", stream);


            //doc.updateProperties(properties, true);

            Map<String, Object> folderProps = new HashMap<String, Object>();
            folderProps.put(PropertyIds.OBJECT_TYPE_ID, "D:loc:doc");
            folderProps.put(PropertyIds.NAME, "testApload");
            folderProps.put("my:firstField", "testAplodFile");
            folderProps.put("my:id", "testAplodFile");
            folderProps.put("my:sourceId", "testAplodFile");

            Document doc = browseFolder.createDocument(folderProps, contentStream, VersioningState.MAJOR);
            //org.apache.chemistry.opencmis.client.api.Document doc2 = parrent.createDocument(folderProps, cs, VersioningState.MAJOR);


            doc.updateProperties(folderProps, true);

        } catch (CmisContentAlreadyExistsException e) {
            System.out.println("Could not create file '" + fileName + "': "
                    + e.getMessage());
        }
    }

    private Map<String, Object> getDefaultFileProperties() {
        Map<String, Object> properties = new HashMap<String, Object>();
        if (repositoryType == REP_TYPE_ALFRESCO) {
            properties.put(PropertyIds.OBJECT_TYPE_ID,
                    "cmis:document,P:loc:readiness,P:notification:notified");
        } else if (repositoryType == REP_TYPE_NUXEO) {
            properties.put(PropertyIds.OBJECT_TYPE_ID, "File");

        }

        GregorianCalendar publishDate = new GregorianCalendar(2012, 4, 1, 5, 0);
        properties.put("loc:readytoprocess", "notready");
        properties.put("loc:processref", "sampleref");
        properties.put("loc:readyat", publishDate);
        properties.put("loc:revised", false);
        properties.put("loc:notified", false);
        properties.put("loc:priority", "low");
        properties.put("loc:completeby", publishDate);
        properties.put("loc:globix", "sample");
        // properties.put("notification:notificationsent", false);
        return properties;
    }

    /**
     *
     * @param fileName
     */
    public void browserShowFile(String fileName) {
        Document doc;
        String objectPath;
        String fileContents;
        if (browseFolder.isRootFolder()) {
            objectPath = browseFolder.getPath() + fileName;
        } else {
            objectPath = browseFolder.getPath() + "/" + fileName;
        }
        try {
            doc = (Document) session.getObjectByPath(objectPath);
            try {
                fileContents = getContentAsString(doc.getContentStream());
                if (fileContents == null) {
                    System.out.println("Document '" + objectPath
                            + "' is empty.");
                } else {
                    System.out.println("-- " + objectPath + " --");
                    System.out.println(fileContents);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (CmisObjectNotFoundException e) {
            System.out.println("Document not found: " + objectPath);
        }
    }

    /**
     *
     * @param fileName
     */
    public void browserSetFile(String fileName) {
        Document doc;
        String objectPath;

        final String textFileName = fileName;
        String mimetype = "text/plain; charset=UTF-8";

        System.out.print("Enter content: ");
        String content = readString();

        String filename = textFileName;

        byte[] buf = null;
        try {
            buf = content.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        ByteArrayInputStream input = new ByteArrayInputStream(buf);
        ContentStream contentStream = session.getObjectFactory().createContentStream(filename, buf.length, mimetype, input);

        if (browseFolder.isRootFolder()) {
            objectPath = browseFolder.getPath() + fileName;
        } else {
            objectPath = browseFolder.getPath() + "/" + fileName;
        }

        try {
            doc = (Document) session.getObjectByPath(objectPath);
            doc.setContentStream(contentStream, true);
        } catch (CmisObjectNotFoundException e) {
            System.out.println("Document not found: " + objectPath);
        }
    }

    /**
     *
     * @return
     */
    public static String readString() {
        String line = null;
        try {
            BufferedReader is = new BufferedReader(new InputStreamReader(
                    System.in));
            line = is.readLine();
        } catch (NumberFormatException ex) {
            System.err.println("Not a valid number: " + line);
        } catch (IOException e) {
            System.err.println("Unexpected IO ERROR: " + e);
        }
        return line;
    }

    /**
     *
     * @param content
     * @return
     */
    public static String readString(String content) {
        String line = null;
        try {
            BufferedReader is = new BufferedReader(new InputStreamReader(
                    System.in));
            line = is.readLine();
        } catch (NumberFormatException ex) {
            System.err.println("Not a valid number: " + line);
        } catch (IOException e) {
            System.err.println("Unexpected IO ERROR: " + e);
        }
        return line;
    }

    /**
     *
     * @return
     */
    public static boolean readBoolean() {
        String line = null;
        try {
            BufferedReader is = new BufferedReader(new InputStreamReader(
                    System.in));
            line = is.readLine();
        } catch (NumberFormatException ex) {
            System.err.println("Not a valid number: " + line);
        } catch (IOException e) {
            System.err.println("Unexpected IO ERROR: " + e);
        }
        if (line.equalsIgnoreCase("true")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     *
     * @return
     */
    public static GregorianCalendar readGregorian() {
        int year;
        int month;
        int dayOfMonth;
        int hourOfDay;
        int minute;

        System.out.print("Enter year: ");
        year = readInt();
        System.out.print("Enter month: ");
        month = readInt() - 1;
        System.out.print("Enter day of the month: ");
        dayOfMonth = readInt();
        System.out.print("Enter hour of the day: ");
        hourOfDay = readInt();
        System.out.print("Enter minute: ");
        minute = readInt();

        return new GregorianCalendar(year, month, dayOfMonth, hourOfDay, minute);
    }

    private static String getContentAsString(ContentStream stream)
            throws IOException {
        // from Jeff Potts guide
        InputStream in2 = stream.getStream();
        StringBuffer sbuf = null;
        sbuf = new StringBuffer(in2.available());
        int count;
        byte[] buf2 = new byte[100];
        while ((count = in2.read(buf2)) != -1) {
            for (int i = 0; i < count; i++) {
                sbuf.append((char) buf2[i]);
            }
        }
        in2.close();
        return sbuf.toString();
    }

    /**
     *
     * @param objectName
     * @param targetPath
     */
    public void browserMoveObject(String objectName, String targetPath) {
        String objectPath;
        FileableCmisObject object;
        if (browseFolder.isRootFolder()) {
            objectPath = browseFolder.getPath() + objectName;
        } else {
            objectPath = browseFolder.getPath() + "/" + objectName;
        }
        try {
            object = (FileableCmisObject) session.getObjectByPath(objectPath);
            Folder target;
            try {
                target = (Folder) session.getObjectByPath(targetPath);
                object.move(browseFolder, target);
            } catch (CmisObjectNotFoundException e) {
                System.out.println("Folder not found: " + e.getMessage());
            } catch (ClassCastException e) {
                System.out.println("Object is not a CMIS folder: "
                        + e.getMessage());
            }

        } catch (CmisObjectNotFoundException e) {
            System.out.println("Object not found: '" + objectPath + "': "
                    + e.getMessage());
        }
    }

    /**
     *
     * @param objectName
     * @param targetPath
     */
    public void browserAddObject(String objectName, String targetPath) {
        String objectPath;
        FileableCmisObject object;
        if (browseFolder.isRootFolder()) {
            objectPath = browseFolder.getPath() + objectName;
        } else {
            objectPath = browseFolder.getPath() + "/" + objectName;
        }
        try {
            object = (FileableCmisObject) session.getObjectByPath(objectPath);
            Folder target;
            try {
                target = (Folder) session.getObjectByPath(targetPath);
                object.addToFolder(target, true);
            } catch (CmisObjectNotFoundException e) {
                System.out.println("Folder not found: " + e.getMessage());
            } catch (ClassCastException e) {
                System.out.println("Object is not a CMIS folder: "
                        + e.getMessage());
            } catch (CmisInvalidArgumentException e) {
                System.out.println("Cannot add to folder: " + e.getMessage());
            }

        } catch (CmisObjectNotFoundException e) {
            System.out.println("Object not found: '" + objectPath + "': "
                    + e.getMessage());
        }
    }

    /**
     *
     * @param objectName
     * @param pollingScheme
     */
    public void addToPollingScheme(String objectName, String pollingScheme) {
        String objectPath;
        Document doc;
        if (browseFolder.isRootFolder()) {
            objectPath = browseFolder.getPath() + objectName;
        } else {
            objectPath = browseFolder.getPath() + "/" + objectName;
        }
        try {
            doc = (Document) session.getObjectByPath(objectPath);
            Folder target;
            try {
                target = (Folder) session.getObjectByPath("/PollingSchemes/"
                        + pollingScheme);

                if (repositoryType == REP_TYPE_ALFRESCO) {
                    doc.addToFolder(target, true);
                } else if (repositoryType == REP_TYPE_NUXEO) {
                    List<Object> newPollingSchemes;
                    Property<Object> pollingSchemes = doc.getProperty("loc:pollingschemes");
                    Map<String, Object> newProps = new HashMap<String, Object>();
                    if (pollingSchemes.isMultiValued()) {
                        newPollingSchemes = (List<Object>) pollingSchemes.getValues();
                        if (!(newPollingSchemes.contains(target.getPath()))) {
                            newPollingSchemes.add(target.getPath());
                        }
                        newProps.put("loc:pollingschemes", newPollingSchemes);
                        doc.updateProperties(newProps, true);
                    }

                }

            } catch (CmisObjectNotFoundException e) {
                System.out.println("Folder not found: " + e.getMessage());
            } catch (ClassCastException e) {
                System.out.println("Object is not a CMIS folder: "
                        + e.getMessage());
            } catch (CmisInvalidArgumentException e) {
                System.out.println("Cannot add to folder: " + e.getMessage());
            }

        } catch (CmisObjectNotFoundException e) {
            System.out.println("Object not found: '" + objectPath + "': "
                    + e.getMessage());
        }
    }

    /**
     *
     * @param queryString1
     * @param queryString2
     * @return
     */
    public List<CmisObject> getIntersectingQueries(String queryString1,
            String queryString2) {
        List<CmisObject> objList = new ArrayList<CmisObject>();
        HashSet<String> results1 = getQueryIDs(queryString1);
        HashSet<String> results2 = getQueryIDs(queryString2);

        Set<String> results = new HashSet<String>(results1);
        results.retainAll(results2);

        for (String objectId : results) {
            CmisObject obj = session.getObject(session.createObjectId(objectId));
            objList.add(obj);
        }

        return objList;
    }

    /**
     *
     * @param queryString1
     * @param queryString2
     * @return
     */
    public List<CmisObject> getQueriesExcept(String queryString1,
            String queryString2) {
        List<CmisObject> objList = new ArrayList<CmisObject>();
        HashSet<String> results1 = getQueryIDs(queryString1);
        HashSet<String> results2 = getQueryIDs(queryString2);

        Set<String> results = new HashSet<String>(results1);
        results.removeAll(results2);

        for (String objectId : results) {
            CmisObject obj = session.getObject(session.createObjectId(objectId));
            objList.add(obj);
        }
        return objList;
    }

    /**
     *
     * @param queryString1
     * @return
     */
    public List<CmisObject> getQueries(String queryString1) {
        List<CmisObject> objList = new ArrayList<CmisObject>();
        HashSet<String> results1 = getQueryIDs(queryString1);

        Set<String> results = new HashSet<String>(results1);

        for (String objectId : results) {
            CmisObject obj = session.getObject(session.createObjectId(objectId));
            objList.add(obj);
        }
        return objList;
    }

    /**
     *
     * @param queryString1
     * @param queryString2
     * @return
     */
    public List<CmisObject> getSubQueryResults(String queryString1,
            String queryString2) {
        List<CmisObject> objList = new ArrayList<CmisObject>();

        // execute first query
        ItemIterable<QueryResult> results = session.query(queryString1, false);
        List<String> objIdList = new ArrayList<String>();
        for (QueryResult qResult : results) {
            String objectId = "";
            PropertyData<?> propData = qResult.getPropertyById("cmis:objectId"); // Atom
            // Pub
            // Binding
            if (propData != null) {
                objectId = (String) propData.getFirstValue();
            } else {
                objectId = qResult.getPropertyValueByQueryName("d.cmis:objectId"); // Web
                // Services
                // Binding
            }
            objIdList.add(objectId);
        }

        if (objIdList.isEmpty()) {
            return objList;
        }

        String queryString = queryString2 + " AND d.cmis:objectId IN "
                + getPredicate(objIdList);
        return getQueryResults(queryString);
    }

    /**
     *
     * @param objectIdList
     * @return
     */
    public String getPredicate(List<String> objectIdList) {
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < objectIdList.size(); i++) {
            sb.append("'");
            sb.append(objectIdList.get(i));
            sb.append("'");
            if (i >= objectIdList.size() - 1) {
                break;
            }
            sb.append(",");
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     *
     * @param queryString
     * @return
     */
    public List<CmisObject> getQueryResults(String queryString) {
        List<CmisObject> objList = new ArrayList<CmisObject>();
        System.out.println(queryString);
        // execute query
        ItemIterable<QueryResult> results = session.query(queryString, false);

        for (QueryResult qResult : results) {
            String objectId = "";
            PropertyData<?> propData = qResult.getPropertyById("cmis:objectId"); // Atom
            // Pub
            // binding
            if (propData != null) {
                objectId = (String) propData.getFirstValue();
            } else {
                objectId = qResult.getPropertyValueByQueryName("d.cmis:objectId"); // Web
                // Services
                // binding
            }
            CmisObject obj = session.getObject(session.createObjectId(objectId));
            objList.add(obj);
        }

        return objList;
    }

    /**
     *
     * @param queryString
     * @return
     */
    public HashSet<String> getQueryIDs(String queryString) {
        HashSet<String> objList = new HashSet<String>();
        //System.out.println(queryString);
        // execute query
        ItemIterable<QueryResult> results = session.query(queryString, false);

        for (QueryResult qResult : results) {
            String objectId = "";
            PropertyData<?> propData = qResult.getPropertyById("cmis:objectId"); // Atom
            // Pub
            // binding
            if (propData != null) {
                objectId = (String) propData.getFirstValue();
            } else {
                objectId = qResult.getPropertyValueByQueryName("d.cmis:objectId"); // Web
                // Services
                // binding
            }

            objList.add(objectId);
        }

        return objList;
    }

    /**
     *
     */
    public void listTranslateRules() {

        try {
            translateRulesFolder = (Folder) session.getObjectByPath("/TranslateRules");
        } catch (CmisObjectNotFoundException e) {
            System.out.println("translateRules folder not found, creating one.");
            Map<String, String> properties = new HashMap<String, String>();
            properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
            properties.put(PropertyIds.NAME, "TranslateRules");
            try {
                session.getRootFolder().createFolder(properties);
            } catch (CmisContentAlreadyExistsException f) {
                System.out.println("Could not create folder '"
                        + "TranslateRules" + "': " + f.getMessage());
            }
        } catch (ClassCastException e) {
            System.out.println("Object is not a CMIS folder.");
        }

        String ruleString = "";
        StringBuffer returnValue = new StringBuffer("");
        ItemIterable<CmisObject> children = translateRulesFolder.getChildren();
        for (CmisObject o : children) {
            returnValue.append(o.getName());
            List<Property<?>> ruleProperies = o.getProperties();
            for (Property<?> p : ruleProperies) {
                if (p.getId().equalsIgnoreCase("trans:rule")) {
                    ruleString = (String) p.getFirstValue();
                }
            }
            returnValue.append("\t");
            returnValue.append(ruleString);
            returnValue.append("\n");
        }

        System.out.println("List of translateRules: ");
        System.out.println("--------------------------------------------");
        System.out.println(returnValue.toString());
        System.out.println("--------------------------------------------");

    }

    /**
     *
     * @param ruleName
     * @param ruleString
     */
    public void rulesNew(String ruleName, String ruleString) {
        try {
            translateRulesFolder = (Folder) session.getObjectByPath("/TranslateRules");
            Map<String, String> properties = new HashMap<String, String>();
            properties.put(PropertyIds.OBJECT_TYPE_ID,
                    "cmis:folder,P:trans:translateRule");
            // properties.put(PropertyIds.OBJECT_TYPE_ID,
            // "cmis:folder,P:trans:folder");
            properties.put(PropertyIds.NAME, ruleName);
            properties.put("trans:rule", ruleString);
            try {
                translateRulesFolder.createFolder(properties);
            } catch (CmisContentAlreadyExistsException e) {
                System.out.println("Could not create rule '" + ruleName + "': "
                        + e.getMessage());
            }

        } catch (CmisObjectNotFoundException e) {
            System.out.println("TranslateRules folder not found: "
                    + e.getMessage());
        } catch (ClassCastException e) {
            System.out.println("Object is not a CMIS folder.");
        }

    }

    /**
     *
     * @param fileName
     */
    public void rulesAdd(String fileName) {
        String ruleName;
        System.out.print("Enter the name of the rule you wish to add: ");
        ruleName = readString();

        browserAddObject(fileName, "/TranslateRules/" + ruleName);

    }

    /**
     *
     * @param fileName
     */
    public void rulesView(String fileName) {
        Document doc;
        String objectPath;
        if (browseFolder.isRootFolder()) {
            objectPath = browseFolder.getPath() + fileName;
        } else {
            objectPath = browseFolder.getPath() + "/" + fileName;
        }
        try {
            doc = (Document) session.getObjectByPath(objectPath);
            List<Folder> parents = doc.getParents();
            for (Folder o : parents) {
                if (o.getPath().startsWith("/TranslateRules")) {
                    String ruleString = "";
                    List<Property<?>> ruleProperies = o.getProperties();
                    for (Property<?> p : ruleProperies) {
                        if (p.getId().equalsIgnoreCase("trans:rule")) {
                            ruleString = (String) p.getFirstValue();
                        }
                    }
                    System.out.println(o.getName() + "\t" + ruleString);
                }
            }

        } catch (CmisObjectNotFoundException e) {
            System.out.println("Document not found: " + objectPath);
        } catch (ClassCastException e) {
            System.out.println("Object is not a document '" + objectPath + "'"
                    + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     *
     * @param schemeName
     * @param interval
     * @param notificationMethod
     * @param notificationHost
     * @param port
     * @param readinessProperty
     * @param readinessValue
     */
    public void createPollingScheme(String schemeName, int interval,
            String notificationMethod, String notificationHost, int port,
            String readinessProperty, String readinessValue) {
        Folder pollingSchemeFolder;

        try {

            try {
                pollingSchemeFolder = (Folder) session.getObjectByPath("/PollingSchemes");
            } catch (CmisObjectNotFoundException e) {
                System.out.println("PollingSchemes folder not found: "
                        + e.getMessage());
                Map<String, String> properties = new HashMap<String, String>();
                properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
                properties.put(PropertyIds.NAME, "PollingSchemes");
                session.getRootFolder().createFolder(properties);
                pollingSchemeFolder = (Folder) session.getObjectByPath("/PollingSchemes");
            }
            Map<String, Object> properties = new HashMap<String, Object>();
            if (repositoryType == REP_TYPE_ALFRESCO) {
                properties.put(PropertyIds.OBJECT_TYPE_ID,
                        "cmis:folder,P:poll:scheme");
            } else if (repositoryType == REP_TYPE_NUXEO) {
                properties.put(PropertyIds.OBJECT_TYPE_ID, "PollingScheme");
            }
            properties.put(PropertyIds.NAME, schemeName);
            properties.put("poll:interval", interval);
            properties.put("poll:notificationmethod", notificationMethod);
            properties.put("poll:notificationhost", notificationHost);
            properties.put("poll:notificationport", port);
            properties.put("poll:readinessproperty", readinessProperty);
            properties.put("poll:readinessvalue", readinessValue);

            try {
                if (repositoryType == REP_TYPE_ALFRESCO) {
                    pollingSchemeFolder.createFolder(properties);
                } else if (repositoryType == REP_TYPE_NUXEO) {
                    pollingSchemeFolder.createFolder(properties);
                }
            } catch (CmisContentAlreadyExistsException e) {
                System.out.println("Could not create polling scheme '"
                        + schemeName + "': " + e.getMessage());

            }

        } catch (ClassCastException e) {
            System.out.println("Object is not a CMIS folder.");
        }
    }

    /**
     *
     * @param objectName
     */
    public void deletePollingScheme(String objectName) {
        String objectPath;
        Folder schemeFolder;
        objectPath = "/PollingSchemes/" + objectName;
        try {
            schemeFolder = (Folder) session.getObjectByPath(objectPath);
            ItemIterable<CmisObject> children = schemeFolder.getChildren();
            for (CmisObject o : children) {
                FileableCmisObject child = (FileableCmisObject) o;
                child.removeFromFolder(schemeFolder);
            }
            schemeFolder.delete(true);
        } catch (CmisObjectNotFoundException e) {
            System.out.println("Object not found: " + objectPath);
        } catch (CmisConstraintException e) {
            System.out.println("Could not delete '" + objectPath + "': "
                    + e.getMessage());
        }
    }

    /**
     *
     * @param path
     */
    public void deleteObjectRecursively(String path) {
        System.out.println("Deleting " + path);
        try {
            CmisObject object = session.getObjectByPath(path);
            deleteObjectRecursively(object);
        } catch (CmisObjectNotFoundException e) {
        }
    }

    /**
     *
     * @param object
     */
    public void deleteObjectRecursively(CmisObject object) {
        try {
            if (object.getBaseType().getBaseTypeId() == BaseTypeId.CMIS_FOLDER) {
                Folder folder = (Folder) object;
                ItemIterable<CmisObject> children = folder.getChildren();
                for (CmisObject child : children) {
                    deleteObjectRecursively(child);
                }
                while (children.getHasMoreItems()) {
                    System.out.println("Getting more children...");
                    ItemIterable<CmisObject> subChildren = children.getPage();
                    for (CmisObject child : subChildren) {
                        deleteObjectRecursively(child);
                    }
                }
            }
            try {
                System.out.println("Deleting " + object.getName());
                object.delete(true);
            } catch (CmisConstraintException f) {
                deleteObjectRecursively(object);
            }
        } catch (java.lang.NullPointerException e) {
        }
    }

    /**
     *
     * @param query
     * @return
     */
    public String queryCMS(String query) {
        String result = "";
        System.out.println("\nQuery...");
        System.out.println("--------");

        // Query 
        ItemIterable<QueryResult> q = session.query(query, false);
        System.out.println("***results from query " + query);

        int i = 1;
        for (QueryResult qr : q) {
            System.out.println("--------------------------------------------\n" + i + " , "
                    + qr.getPropertyByQueryName("cmis:objectTypeId").getFirstValue() + " , "
                    + qr.getPropertyByQueryName("cmis:name").getFirstValue() + " , "
                    + qr.getPropertyByQueryName("cmis:createdBy").getFirstValue() + " , "
                    + qr.getPropertyByQueryName("cmis:objectId").getFirstValue() + " , "
                    + qr.getPropertyByQueryName("cmis:contentStreamFileName").getFirstValue()
                    + " , "
                    + qr.getPropertyByQueryName("cmis:contentStreamMimeType").getFirstValue()
                    + " , "
                    + qr.getPropertyByQueryName("cmis:contentStreamLength").getFirstValue());
            System.out.println(qr.getProperties().toString());
            result = qr.getPropertyByQueryName("cmis:objectId").getFirstValue().toString();

            i++;
        }
        return result;
    }

    //
    /**
     *
     * @param folderName
     * @return
     */
    public Map<Integer, List<String>> getFolderContentsInfo(String folderName) {
        Map<Integer, List<String>> map = new HashMap<Integer, List<String>>();

        ArrayList<String> result = new ArrayList<String>();
        //Folder root = session.getRootFolder();
        Folder root = null;
        if (!folderName.equalsIgnoreCase("Company Home")) {
            try {
                root = (Folder) session.getObjectByPath("/" + folderName);
            } catch (Exception ex) {
            }
        } else {
            root = (Folder) session.getObjectByPath("/" + "");
        }

        if (root != null) {
            ItemIterable<CmisObject> children = root.getChildren();
            int i = 0;
            for (CmisObject o : children) {
                List<String> valList = new ArrayList<String>();
                valList.add(o.getName());
                valList.add(o.getId());
                valList.add(o.toString());
                map.put(i, valList);
                i++;
            }
        }
        return map;
    }
    
    public static void main(String args[]) throws MalformedURLException, IOException{
        String data=RepositoryHandler.readFile("");
        System.out.println(data);
    }
    
}