# Map_Checkrole
Searches a file/directory for BYU Alumni custom tag `<cf_checkrole></cf_checkrole>`. Documents which roles are being used and where they are located.
# How To Use
Pass in two command-line arguments to `Main.java`. The first being the file or directory to search. The second is the directory in which to store the checkrole map.
# Results
The program will generate an identical map to the directory being searched but only filled with files which contain a checkrole tag. Each file created will contain the 
checkroles used within that file and their line numbers.
Additionally, a summary file will be generated to help visualize all of the checkroles used in the entire directory.
