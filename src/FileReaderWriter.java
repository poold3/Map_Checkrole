import java.io.IOException;
import java.util.*;
import java.io.File;
import java.io.FileWriter;

public class FileReaderWriter {
    private final SortedMap<String, Permission> permissions;
    public FileReaderWriter() {
        this.permissions = new TreeMap<>();
    }

    public SortedMap<String, Permission> getPermissions() {
        return this.permissions;
    }

    private void createNewMapFile(File file) throws Exception {
        //Double-check that the parent directories exist
        File parentDirectory = new File(file.getParent());
        if (!parentDirectory.exists()) {
            //Store all root directories that do not exist in List rootDirectories
            List<File> rootDirectories = new ArrayList<>();
            File currentDirectory = parentDirectory;
            while (!currentDirectory.exists()) {
                rootDirectories.add(currentDirectory);
                currentDirectory = new File(currentDirectory.getParent());
            }

            //Now traverse rootDirectories backwards and add each directory
            ListIterator<File> listIterator = rootDirectories.listIterator(rootDirectories.size());
            while (listIterator.hasPrevious()) {
                File newDirectory = listIterator.previous();
                if (!newDirectory.mkdir()) {
                    throw new Exception("Unable to create directory " + newDirectory.getAbsolutePath() + " in Map Directory");
                }
            }
        }

        if (!file.createNewFile()) {
            throw new IOException("Unable to create file " + file.getAbsolutePath() + " in Map Directory");
        }
    }

    public void readWriteFile(File fileToRead, File fileToWrite) throws IOException {
        boolean newMapFileCreated = false;

        //Validate fileToRead
        if (!fileToRead.exists()) {
            throw new IOException(fileToRead.getAbsolutePath() + " does not exist");
        }
        else if (!fileToRead.canRead()) {
            throw new IOException("Can't read from " + fileToRead.getAbsolutePath());
        }

        //Begin reading and writing
        try {
            //Create myReader and myWriter
            Scanner myReader = new Scanner(fileToRead);
            FileWriter myWriter = null;

            int lineNumber = 1;

            //Loop through fileToRead
            System.out.println("Reading from " + fileToRead.getAbsolutePath() + "\n");
            while (myReader.hasNext()) {
                StringBuilder line = new StringBuilder((myReader.nextLine()).toLowerCase());

                int startIndex = 0;
                int endIndex = 0;
                while (startIndex != -1) {
                    startIndex = line.indexOf("<cf_checkrole ", endIndex);
                    if (startIndex != -1) {
                        endIndex = line.indexOf(">", startIndex);
                        int roleIndex = line.indexOf(" role", startIndex);
                        if (roleIndex != -1 && roleIndex < endIndex) {

                            int permissionStart = line.indexOf("\"", roleIndex) != -1 ?
                                    line.indexOf("\"", roleIndex) : line.indexOf("'", roleIndex);

                            int permissionEnd = line.indexOf("\"", permissionStart + 1) != -1 ?
                                    line.indexOf("\"", permissionStart + 1) :
                                    line.indexOf("'", permissionStart + 1);
                            if (permissionStart != -1 && permissionEnd != -1) {
                                if (!newMapFileCreated) {
                                    //Create fileToWrite
                                    createNewMapFile(fileToWrite);

                                    //Validate fileToWrite
                                    if (!fileToWrite.exists()) {
                                        throw new IOException(fileToWrite.getAbsolutePath() + " does not exist");
                                    }
                                    else if (!fileToWrite.canWrite()) {
                                        throw new IOException("Can't write to " + fileToWrite.getAbsolutePath());
                                    }

                                    //Tell class that we have created the newMapFile
                                    newMapFileCreated = true;

                                    //Create new FileWriter
                                    myWriter = new FileWriter(fileToWrite);

                                    //Print file name/path
                                    myWriter.write(String.format("File Name: %s\n", fileToRead.getName()));
                                    myWriter.write(String.format("File Path: %s\n", fileToRead.getAbsolutePath()));

                                    myWriter.write("Permissions Used:\n");
                                }
                                String permissionLine = line.substring(permissionStart + 1, permissionEnd);

                                //If there are multiple permissions, split them up between commas
                                ArrayList<String> permissionsToAdd = new ArrayList<>();
                                if (permissionLine.contains(",")) {
                                    int startSearchFrom = 0;
                                    int commaIndex = permissionLine.indexOf(",");
                                    while (commaIndex != -1) {
                                        permissionsToAdd.add(permissionLine.substring(startSearchFrom, commaIndex));
                                        startSearchFrom = commaIndex + 1;
                                        commaIndex = permissionLine.indexOf(",", startSearchFrom);
                                    }
                                }
                                else {
                                    permissionsToAdd.add(permissionLine);
                                }

                                for (String permission: permissionsToAdd) {
                                    myWriter.write(String.format("\tLine %d: %s\n", lineNumber, permission));

                                    //Update this.permissions
                                    if (!this.permissions.containsKey(permission)) {
                                        this.permissions.put(permission, new Permission(permission));
                                    }
                                    this.permissions.get(permission).addLocation(fileToRead.getAbsolutePath());
                                }
                            }
                        }
                    }
                }
                ++lineNumber;
            }

            myReader.close();

            if (myWriter != null) {
                myWriter.close();
            }
        }
        catch (Exception e) {
            System.out.println("An error occurred!");
        }

    }
}
