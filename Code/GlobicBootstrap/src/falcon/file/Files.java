package falcon.file;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 
 * Handles files in L3Data logger
 * 
 * @author leroy
 */
public class Files {

    List<String> fileList;
    private static final String INPUT_ZIP_FILE = "/home/leroy/Downloads/tippexample.tipp";
    private static final String OUTPUT_FOLDER = "/home/leroy/Downloads/outputzip";
    private static final String RESOURCE_OUTPUT_FOLDER = "/home/leroy/Downloads/outputzip/resource";

      private static final int BUFFER_SIZE = 4096;
     
     /**
     * Downloads a file from a URL
     * @param fileURL HTTP URL of the file to be downloaded
     * @param saveDir path of the directory to save the file
     * @throws IOException
     */
    public static void downloadFile(String fileURL, String saveDir)
            throws IOException {
        URL url = new URL(fileURL);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        int responseCode = httpConn.getResponseCode();
 
        // always check HTTP response code first
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String fileName = "";
            String disposition = httpConn.getHeaderField("Content-Disposition");
            String contentType = httpConn.getContentType();
            int contentLength = httpConn.getContentLength();
 
            if (disposition != null) {
                // extracts file name from header field
                int index = disposition.indexOf("filename=");
                if (index > 0) {
                    fileName = disposition.substring(index + 10,
                            disposition.length() - 1);
                }
            } else {
                // extracts file name from URL
                fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1,
                        fileURL.length());
            }

            // opens input stream from the HTTP connection
            InputStream inputStream = httpConn.getInputStream();
            String saveFilePath = saveDir + File.separator + fileName;
            saveFilePath=saveFilePath.substring(0,saveFilePath.indexOf("?"));
            File f= new File(saveFilePath);
            f.createNewFile();
            // opens an output stream to save into file
            FileOutputStream outputStream = new FileOutputStream(f);

            System.out.println("Content-Type = " + contentType);
            System.out.println("Content-Disposition = " + disposition);
            System.out.println("Content-Length = " + contentLength);
            System.out.println("saveFilePath = " + saveFilePath);
            
            int bytesRead = -1;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
 
            outputStream.close();
            inputStream.close();
 
            System.out.println("File downloaded");
        } else {
            System.out.println("No file to download. Server replied HTTP code: " + responseCode);
        }
        httpConn.disconnect();
    }
    
    /**
     * Unpack TIPP file and return the XLIFF file directory in a List
     *
     * @param tippFile input zip file
     * @param outputFolder output folder
     * @return XLIFF file list from tipp
     */
    public static List<String> unpackTIPP(String tippFile, String outputFolder) throws IOException {
        List<String> fileList = new ArrayList<String>();
        byte[] buffer = new byte[1024];

        try {

            //create output directory is not exists
            File folder = new File(OUTPUT_FOLDER);
            if (!folder.exists()) {
                folder.mkdir();
            }

            //get the zip file content
            ZipInputStream zis = new ZipInputStream(new FileInputStream(tippFile));
            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();

            while (ze != null) {

                String fileName = ze.getName();
                File newFile = new File(outputFolder + File.separator + fileName);

                System.out.println("file unzip : " + newFile.getAbsoluteFile());

                //add xliff file to list
                if (newFile.getAbsoluteFile().toString().contains(".xlf")) {
                    fileList.add(newFile.getAbsoluteFile().toString());
                }

                //check for zip file
                if (newFile.getAbsoluteFile().toString().contains("resources.zip")) {
                    System.out.println("file is a zip file : " + newFile.getAbsoluteFile());
                    unpackTIPP(newFile.getAbsoluteFile().toString(), RESOURCE_OUTPUT_FOLDER);
                }

                //create all non exists folders
                //else you will hit FileNotFoundException for compressed folder
                new File(newFile.getParent()).mkdirs();

                FileOutputStream fos = new FileOutputStream(newFile);

                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }

                fos.close();
                ze = zis.getNextEntry();
            }

            zis.closeEntry();
            zis.close();

            System.out.println("Done");

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return fileList;
    }

    /*
     * Testing File code
     *
     * @param args
     */
    public static void main(String[] args) throws IOException {

        List<String> fileList = unpackTIPP(INPUT_ZIP_FILE, OUTPUT_FOLDER);

        for (int x = 0; x <= fileList.size() - 1; x++) {
            System.out.println("fileList" + x + "==" + fileList.get(x));
        }

          String fileURL = "https://raw.githubusercontent.com/CNGL-repo/IA/master/GlobicBootstrapRestApi/leanbacklearning.xml?token=497801__eyJzY29wZSI6IlJhd0Jsb2I6Q05HTC1yZXBvL0lBL21hc3Rlci9HbG9iaWNCb290c3RyYXBSZXN0QXBpL2xlYW5iYWNrbGVhcm5pbmcueG1sIiwiZXhwaXJlcyI6MTQwMzc5NDQyMH0%3D--02553aedb2a5693fe41a7d15a705946f415094e7";
        String saveDir = "C:\\Users\\Leroy\\Desktop";
        try {
            Files.downloadFile(fileURL, saveDir);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
    }
}
