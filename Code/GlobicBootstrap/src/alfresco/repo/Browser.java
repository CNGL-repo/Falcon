package alfresco.repo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 
 * Acts as a debugging browser for the Alfresco store
 * 
 * @author Leroy
 */
public class Browser {
    
    /**
     * 
     * 
     * Main menu state
     * 
     */
    public static final int CONTEXT_MAIN = 0;
    
    /**
     * 
     * 
     * Browsing state
     * 
     */
    public static final int CONTEXT_BROWSE = 1;
    
    /**
     * 
     * 
     * Notification state
     * 
     */
    public static final int CONTEXT_NOTIFICATIONS = 2;
    
    /**
     * 
     * 
     * Readiness state
     * 
     */
    public static final int CONTEXT_READINESS = 3;
    
    /**
     * 
     * 
     * Translation rule state
     * 
     */
    public static final int CONTEXT_TRANSLATERULES = 4;
    
    /**
     * 
     * Exit state
     * 
     */
    public static final int CONTEXT_EXIT = 999;
    
    /**
     * 
     * Main menu options description for the user
     * 
     */
    public static final String MENU_MAIN = "\t" + "-- MAIN MENU --\n"
            + "1.\tBrowse repository\n" + "2.\tConfigure notifications\n"
            + "3.\tList files by readiness properties\n"
            + "4.\tList TranslateRules\n" + "quit\tExit application\n"
            + "help \tHelp information\n\n" + "Enter your selection: ";
    /**
     * 
     * Current state in browser
     * 
     */
    public static int state;
    
    /**
     * 
     * Repository handler object
     * 
     */
    public static RepositoryHandler handler;

     /**
     * 
     * Create translation rules on the files in the Alfresco store
     * 
     */
    private static void browseTranslateRules() {
        handler.listTranslateRules();

        System.out.print("Enter option (new, remove, main, or quit): ");
        String userInput = readString();

        while ((!(userInput.equalsIgnoreCase("quit")))
                && (state == CONTEXT_TRANSLATERULES)) {
            if (userInput == "quit") {
                state = CONTEXT_EXIT;
            } else if (userInput.equalsIgnoreCase("main")) {
                System.out.println(MENU_MAIN);
                state = CONTEXT_MAIN;
            } else if (userInput.equalsIgnoreCase("list")) {
                handler.listTranslateRules();
            } else {
                String[] tokens = userInput.split(" ", 2);
                if (tokens.length == 2) {
                    if (tokens[0].equalsIgnoreCase("new")) {
                        System.out.print("Enter the rule string: ");
                        String ruleString = readString();
                        handler.rulesNew(tokens[1], ruleString);
                    } else if (tokens[0].equalsIgnoreCase("remove")) {
                        handler.rulesDelete(tokens[1]);
                    } else {
                        System.out.println("Input not recognised");
                    }
                } else {
                    System.out.println("Input not recognised");
                }
            }

            System.out.print("Enter option (new, remove, main, or quit): ");
            userInput = readString();
        }
    }

    /**
     * 
     * List CMIS details
     * 
     */
    public static void listReadiness() {
        handler.browserGetRoot();
        System.out.print("Enter the readiness property you wish to search by (e.g. loc:readytoprocess): ");
        String readinessProperty = readString();
        System.out.print("Enter the value you wish to find: ");
        String readinessValue = readString();
        //handler.listReadiness(readinessProperty, readinessValue);
        state = CONTEXT_MAIN;
        System.out.print("Select another option from the main menu (enter 'main' to see the menu): ");

    }

    /**
     * 
     * This is the main part of the browser and contains the logic around 
     * what occurs depending on the user input.
     * 
     * @throws IOException
     */
    public static void browserRepository() throws IOException {
        handler.browserGetRoot();
        String userInput = "";
        while ((!(userInput.equalsIgnoreCase("quit")))
                && (state == CONTEXT_BROWSE)) {
            System.out.print(handler.browserGetPath() + ">");
            userInput = readString();

            if (userInput == "quit") {
                state = CONTEXT_EXIT;
            } else if (userInput.equalsIgnoreCase("main")) {
                System.out.println(MENU_MAIN);
                state = CONTEXT_MAIN;
            } else if ((userInput.equalsIgnoreCase("ls"))
                    || (userInput.equalsIgnoreCase("dir"))) {
                System.out.print(handler.browserFolderToString());
            } else if (userInput.equalsIgnoreCase("pwd")) {
                System.out.println(handler.browseFolder.getPath());
            } else {
                String[] tokens = userInput.split(" ", 2);
                if (tokens.length == 2) {
                    if (tokens[0].equalsIgnoreCase("cd")) {
                        handler.browserChangePath(tokens[1]);
                    } else if (tokens[0].equalsIgnoreCase("rm")) {
                        //handler.browserDeleteObject(tokens[1]);
                        //  handler.test(tokens[1]);
                    } else if (tokens[0].equalsIgnoreCase("mkdir")) {
                        handler.browserMakeFolder(tokens[1]);
                    } else if (tokens[0].equalsIgnoreCase("mkfile")) {
                        //handler.browserMakeFile(tokens[1]);
                        //send content to alfresco

                        BufferedReader in=new BufferedReader(new InputStreamReader(System.in)); 
                        
                        System.out.println("Please enter the activity:");
                        String activity=in.readLine();
                        System.out.println("Please enter the user:");
                        String user=in.readLine(); 
                        System.out.println("Please enter the password:");
                        String password=in.readLine(); 
                        System.out.println("Please enter the content:");
                        String content=in.readLine(); 
                        System.out.println("Please enter the contentType:");
                        String contentType=in.readLine();
                        System.out.println("Please enter the component:");
                        String component=in.readLine();
                        
                       /* if (GlobicBootstrap.isWindows()) {
                            handler.sendContentToCMS(tokens[1], activity, user, password, content, contentType, component);
                        } else if (GlobicBootstrap.isMac()) {
                            handler.sendContentToCMS(tokens[1], activity, user, password, content, contentType, component);
                        } else if (GlobicBootstrap.isUnix()) {
                            handler.sendContentToCMS(tokens[1], activity, user, password, content, contentType, component);
                        } else if (GlobicBootstrap.isSolaris()) {
                            //System.out.println("This is Solaris");
                        } else {
                            System.out.println("Your OS is not support!!");
                        }*/
                        
                    } else if (tokens[0].equalsIgnoreCase("more")) {
                        handler.browserShowFile(tokens[1]);
                    } else if (tokens[0].equalsIgnoreCase("set")) {
                        handler.browserSetFile(tokens[1]);
                    } else if (tokens[0].equalsIgnoreCase("properties")) {
                        tokens = userInput.split(" ", 3);
                        if (tokens.length == 3) {
                            if (tokens[1].equalsIgnoreCase("set")) {
                            } else if (tokens[1].equalsIgnoreCase("view")) {
                                //handler.browserShowReadiness(tokens[2]);
                                handler.getGlobicProperties(tokens[2]);
                            }

                        }
                    } else if (tokens[0].equalsIgnoreCase("rules")) {
                        tokens = userInput.split(" ", 3);
                        if (tokens.length == 3) {
                            if (tokens[1].equalsIgnoreCase("add")) {
                                handler.rulesAdd(tokens[2]);
                            } else if (tokens[1].equalsIgnoreCase("view")) {
                                handler.rulesView(tokens[2]);
                            }

                        }
                    } else if (tokens[0].equalsIgnoreCase("mv")) {
                        tokens = userInput.split(" ", 3);
                        if (tokens.length == 3) {
                            handler.browserMoveObject(tokens[1], tokens[2]);
                        }
                    } else if (tokens[0].equalsIgnoreCase("add")) {
                        tokens = userInput.split(" ", 3);
                        if (tokens.length == 3) {
                            handler.browserAddObject(tokens[1], tokens[2]);
                        }
                    } else {
                        System.out.println("Input not recognised");
                    }
                } else {
                    System.out.println("Input not recognised");
                }
            }
        }

    }

    /**
     * Read in user input int to browser
     * 
     * @return users input int
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
     * Read in user input string to browser
     * 
     * @return users input string
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
     * Main sample code
     * 
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        
        System.getProperties().put("proxySet", "true");
        System.getProperties().put("proxyHost", "www-proxy.cs.tcd.ie");
        System.getProperties().put("proxyPort", "8080");

        String repositoryUsername;
        String repositoryPasword;
        int repositoryType;
        String repositoryURL;

        System.out.println("==CMIS client for localisation extensions==");
        repositoryURL = "http://kdeg-vm-13.scss.tcd.ie:8080/alfresco/cmisatom";
        repositoryUsername = "admin";
        repositoryPasword = "admin";
        repositoryType = 1;
        handler = new RepositoryHandler(repositoryURL, repositoryUsername,
                repositoryPasword, repositoryType);
        state = CONTEXT_MAIN;
        System.out.print(MENU_MAIN);
        String userInput = "";
        while ((!(userInput.equalsIgnoreCase("quit")))
                && (state != CONTEXT_EXIT)) {
            userInput = readString();

            if (userInput.equalsIgnoreCase("main")) {
                System.out.print(MENU_MAIN);
                state = CONTEXT_MAIN;
            } else if (state == CONTEXT_MAIN) {
                if (userInput.equalsIgnoreCase("1")) {
                    state = CONTEXT_BROWSE;
                    browserRepository();
                } else if (userInput.equalsIgnoreCase("2")) {
                    //  updateReadinessProperties("","","","","","");
                } else if (userInput.equalsIgnoreCase("3")) {
                    state = CONTEXT_READINESS;
                    listReadiness();
                } else if (userInput.equalsIgnoreCase("4")) {
                    state = CONTEXT_TRANSLATERULES;
                    browseTranslateRules();
                } else if (userInput.equalsIgnoreCase("quit")) {
                } else {
                    System.out.print("Select another option from the main menu (enter 'main' to see the menu): ");
                }
            }
        }

        System.out.println("-- Exiting application");

    } 
}
