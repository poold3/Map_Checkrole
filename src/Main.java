import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.nio.file.NoSuchFileException;
import java.util.*;

public class Main {

    private static final Set<String> VALID_FILE_TYPES = new HashSet<>(Arrays.asList(".cfm", ".cfc"));

    private static String getFileType(File file) {
        String fileName = file.getName();
        return fileName.length() >= 4 ? fileName.substring(fileName.length() - 4) : "none";
    }

    private static void deleteFiles(File[] files) throws Exception {
        for (File file: files) {
            if (!file.delete()) {
                deleteFiles(Objects.requireNonNull(file.listFiles()));
                if (!file.delete()) {
                    throw new Exception("Unable to delete " + file.getAbsolutePath());
                }
            }
        }
    }

    private static void readFiles(File[] files, File currentMapDirectory, FileReaderWriter myReaderWriter) throws Exception {
        for (File file: files) {
            if (file.isDirectory()) {
                //Create new directory in Map
                File newMapDirectory = new File(currentMapDirectory.getAbsolutePath() + "/" + file.getName());
                readFiles(Objects.requireNonNull(file.listFiles()), newMapDirectory, myReaderWriter);
            }
            else {
                //Verify that file is a valid type
                if (VALID_FILE_TYPES.contains(getFileType(file))) {
                    //Create new file in Map
                    File newMapFile = new File(currentMapDirectory.getAbsoluteFile() + "/" + file.getName() + ".txt");

                    //Read from file and write to newMapFile
                    myReaderWriter.readWriteFile(file, newMapFile);
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        File indexFile = new File(args[0]);
        File MapDirectory = new File(args[1]);

        //Validate indexFile
        if (!indexFile.exists()) {
            throw new NoSuchFileException(indexFile.getAbsolutePath() + " does not exist.");
        }

        //Validate MapDirectory
        if (!MapDirectory.exists()) {
            throw new NoSuchFileException(MapDirectory.getAbsolutePath() + " does not exist.");
        }
        if (!MapDirectory.isDirectory()) {
            throw new IOException(MapDirectory.getAbsolutePath() + " is not a directory.");
        }

        //Try to delete the Map Directory. If false, delete child files. Then try again.
        if (!MapDirectory.delete()) {
            deleteFiles(Objects.requireNonNull(MapDirectory.listFiles()));
            if (!MapDirectory.delete()) {
                throw new Exception("Unable to delete the current Map Directory");
            }
        }

        //Create new Map Directory
        if (!MapDirectory.mkdir()) {
            throw new IOException("Unable to create new Map Directory at " + MapDirectory.getAbsolutePath());
        }

        FileReaderWriter myReaderWriter = new FileReaderWriter();

        if (indexFile.isDirectory()) {
            readFiles(Objects.requireNonNull(indexFile.listFiles()), MapDirectory, myReaderWriter);
        }
        else {
            //Verify that indexFile is a valid type
            if (VALID_FILE_TYPES.contains(getFileType(indexFile))) {
                //Create new file in Map
                File newMapFile = new File(MapDirectory.getAbsoluteFile() + "/" + indexFile.getName() + ".txt");
                if (!newMapFile.createNewFile()) {
                    throw new IOException("Unable to create file " + newMapFile.getAbsolutePath() + " in Map Directory");
                }
                myReaderWriter.readWriteFile(indexFile, newMapFile);
            }
        }

        //Now create Summary.txt
        File summaryFile = new File(MapDirectory.getAbsoluteFile() + "/Summary.txt");

        //Create and Validate summaryFile
        if (!summaryFile.createNewFile()) {
            throw new IOException("Unable to create the Summary file in Map Directory");
        }

        if (!summaryFile.canWrite()) {
            throw new IOException("Unable to write to the Summary file in Map Directory");
        }

        //Write to Summary.txt
        FileWriter myWriter = new FileWriter(summaryFile);
        myWriter.write("Summary of all Permissions found within " + indexFile.getAbsolutePath() + "\n");

        Set<Map.Entry<String, Permission>> permissions = myReaderWriter.getPermissions().entrySet();
        for (Map.Entry<String, Permission> entry: permissions) {
            myWriter.write("\nLocations for '" + entry.getKey() + "'\n");
            Set<Map.Entry<String, Integer>> locations = entry.getValue().getLocations().entrySet();
            for (Map.Entry<String, Integer> location: locations) {
                myWriter.write("\t" + location.getValue() + " in " + location.getKey() + "\n");
            }

        }

        myWriter.close();
    }
}
