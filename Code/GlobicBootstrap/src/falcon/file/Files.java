package falcon.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

    /**
     * The given zip fie and output the folder to the output folder
     *
     * @param zipFile input zip file
     * @param outputFolder output folder
     * @return
     */
    public static List<String> unZipIt(String zipFile, String outputFolder) {
        List<String> fileList = new ArrayList<String>();
        byte[] buffer = new byte[1024];

        try {

            //create output directory is not exists
            File folder = new File(OUTPUT_FOLDER);
            if (!folder.exists()) {
                folder.mkdir();
            }

            //get the zip file content
            ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();

            while (ze != null) {

                String fileName = ze.getName();
                File newFile = new File(outputFolder + File.separator + fileName);

                System.out.println("file unzip : " + newFile.getAbsoluteFile());

                if (newFile.getAbsoluteFile().toString().contains(".xlf")) {
                    fileList.add(newFile.getAbsoluteFile().toString());
                }

                //check for zip file
                if (newFile.getAbsoluteFile().toString().contains("resources.zip")) {
                    System.out.println("file is a zip file : " + newFile.getAbsoluteFile());

                    unZipIt(newFile.getAbsoluteFile().toString(), RESOURCE_OUTPUT_FOLDER);
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
    public static void main(String[] args) {

        List<String> fileList = unZipIt(INPUT_ZIP_FILE, OUTPUT_FOLDER);

        for (int x = 0; x <= fileList.size() - 1; x++) {
            System.out.println("fileList" + x + "==" + fileList.get(x));
        }

    }
}
