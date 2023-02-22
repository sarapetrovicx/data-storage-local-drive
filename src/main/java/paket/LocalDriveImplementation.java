package paket;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.comparator.NameFileComparator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class LocalDriveImplementation extends Specification{

    private int cnt = 1;
    List<MyFile> myFileList;

    static {
        DriveManager.registerDrive(new LocalDriveImplementation());
    }

    public LocalDriveImplementation() {
        super();
        myFileList = new ArrayList<>();
    }

    @Override
    boolean init(String path){
        File file = new File(path);
        if(file.exists() && file.isDirectory()){
            File[] listOfFiles;
            listOfFiles = file.listFiles();
            boolean isRoot = false;
            if(listOfFiles != null) {
                System.out.println(listOfFiles.length);
                for (File f : listOfFiles) {
                    if (f.getName().equals("configuration.txt")) {
                        try {
                            Scanner sc = new Scanner(f);
                            int size = Integer.parseInt(sc.nextLine().split(" ")[1]);
                            int numOfFiles = Integer.parseInt( sc.nextLine().split(" ")[1]);
                            configuration = new Configuration(size, numOfFiles, parseConfigFiles(sc.nextLine()));
                            return true;
                        } catch (IOException e){
                            e.printStackTrace();
                        }
//                        isRoot = true;
                        break;
                    }
                }
            }
//            if(isRoot){
//                Scanner sc = new Scanner(config);
//                int size = Integer.parseInt(sc.nextLine().split(" ")[1]);
//                int numOfFiles = Integer.parseInt( sc.nextLine().split(" ")[2]);
//                makeConfiguration();
//                return true;
//            }
            if(listOfFiles == null){
                defaultConf();
                return true;
            }
        }
        return false;
    }

    MyFile initMyFile(File file, String type){
        MyFile myFile = new MyFile(type);
        myFile.setSize(file.length());
        myFile.setName(file.getName());

        BasicFileAttributes attr = null;
        try {
            attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
        } catch (Exception e){
            e.printStackTrace();
        }

        if (attr != null) {

            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
            myFile.setLastModified(df.format(attr.lastModifiedTime().toMillis()));
            myFile.setTimeCreated(df.format(attr.creationTime().toMillis()));
        }
        return myFile;

    }
    private static ArrayList<String> parseConfigFiles(String str) {
        ArrayList<String> arr = new ArrayList<>();
        for(int i = 1; i < str.split(" ").length; i++){
            arr.add(str.split(" ")[i]);
        }
        return arr;
    }

    @Override
    boolean checkConfiguration(String parentFilePath, String filePath) {
        File parentFile = new File(parentFilePath);
        File file = new File(filePath);
        double bytes = parentFile.length();
        double kilobytes = bytes/1024;
        double megabytes = kilobytes/ 1024; //sta cemo koristiti?
        int size = 0;
        if(parentFile.listFiles() != null)
        size = parentFile.listFiles().length;
        String extension = FilenameUtils.getExtension(file.toString());
        return !configuration.getExtensions().contains(extension) &&
                configuration.getNumOfFiles() > size && configuration.getSize() > bytes;
    }

    @Override
    void writeConfiguraton(Configuration configuration) {
        ArrayList<String> ext = configuration.getExtensions();
        StringBuilder ex = new StringBuilder();
        for(String e: ext){
            ex.append(" ");
            ex.append(e);
        }
        FileWriter myWriter;
        //TODO: Ovo treba isto da bude abstract posto se drugacije pise u GoogleDrive-u
        try {
            System.out.println("uslo" + configuration.getExtensions().toString());
            myWriter = new FileWriter(getRootPath() + "/configuration.txt");
            myWriter.write("size: " + configuration.getSize() + "\n" +
                    "numOfFiles: " + configuration.getNumOfFiles() + "\n" +
                    "extension:" + ex);
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    boolean isInStorage(String path){
        return path.contains(getRootPath());
    }

    @Override
    void createRootDirectory() {
        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString(); //current directory
        createRootDirectory(s, "Storage");
    }

    @Override
    void createRootDirectory(String path) {
        createRootDirectory(path, "Storage");
    }

    @Override
    void createRootDirectory(String path, String name) {
        File theDir = new File(path + File.separator + name);
        if (!theDir.exists()){
            theDir.mkdir();
        }
        setRootPath(theDir.getPath());
        defaultConf();
    }

    @Override
    void createRootDirectory(String path, Configuration configuration) {
        createRootDirectory(path, "Storage", configuration);
    }

    @Override
    void createRootDirectory(String path, String name, Configuration configuration) {
        File theDir = new File(path + File.separator + name);
        if (!theDir.exists()){
            theDir.mkdir();
        }
        setRootPath(theDir.getPath());
        makeConfiguration(configuration.getSize(), configuration.getNumOfFiles(), configuration.getExtensions());
    }

    @Override
    void createRootDirectory(Configuration configuration) {
        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString(); //current directory
        File theDir = new File(s + File.separator + "Storage");
        if (!theDir.exists()){
            theDir.mkdir();
        }
        setRootPath(theDir.getPath());
        makeConfiguration(configuration.getSize(), configuration.getNumOfFiles(), configuration.getExtensions());
    }

    ////DIRECTORY METHODS

    @Override
    void makeDirectory() {
        File root = new File(getRootPath());
        File theDir = new File(getRootPath() + File.separator + "Directory");
        System.out.println(theDir);
        if(checkConfiguration(root.getPath(), theDir.getPath())) {
            if (!theDir.exists()) {
                theDir.mkdir();
            } else {
                theDir = new File(getRootPath() + File.separator + "Directory" + cnt);
                while (theDir.exists()) {
                    theDir = new File(getRootPath() + File.separator + "Directory" + cnt++);
                }
                theDir.mkdir();
            }
        }
    }

    @Override
    void makeDirectory(String name) {
        File root = new File(getRootPath());
        File theDir = new File(getRootPath() + File.separator + name);
        System.out.println(theDir);
        if (!theDir.exists() && checkConfiguration(root.getPath(), theDir.getPath()))
            theDir.mkdir();
        else
            System.out.println("Another directory with this name already exists or you have passed your number of files limit");
    }

    @Override
    void makeDirectories(int numOfDir) {
        for(int i = 1; i <= numOfDir; i++){
            //File theDir = new File(getRootPath() + File.separator + "dir" + i);
            makeDirectory("dir" + i);
        }
    }

    @Override
    void makeDirectories(int numOfDir, ArrayList<String> names) {
        if(numOfDir <= names.size()){
            for(int i = 0; i < numOfDir; i++){
                makeDirectory(names.get(i));
            }
        } else {
            System.out.println("Wanted number of directories is bigger than the size of the list");
        }
    }

    //kopiranje sa jedne lokacije na drugu
    @Override
    void copyFile(String source, String target) throws IOException {
        File sourceFile = new File(source);
        File targetFile = new File(target);
        if(checkConfiguration(targetFile.getParentFile().getPath(), targetFile.getPath()) && sourceFile.exists() && sourceFile.isFile())
            FileUtils.copyFile(sourceFile, targetFile);
        else{
            if(!checkConfiguration(targetFile.getParentFile().getPath(), targetFile.getPath()))
                System.out.println("Ne odgovara konfiguraciji");
        }
    }

    @Override
    void copyFiles(ArrayList<String> sources, String targetDirectory) throws IOException {
        for(String path: sources){
            File file = new File(path);
            String dest;
            String fileNameNoExt = file.getName().substring(0, file.getName().indexOf("."));
            if(targetDirectory.endsWith("/"))
                dest = fileNameNoExt+"_copy"+file.getName().substring(file.getName().lastIndexOf("."));
            else
                dest = "/"+fileNameNoExt+"_copy"+file.getName().substring(file.getName().lastIndexOf("."));
            cnt++;
            copyFile(path, targetDirectory+""+dest);
        }
    }

    @Override
    void deleteFile(String path) {
        File file = new File(path);
            if (file.exists() && file.isFile() && isInStorage(path)) {
                file.delete();
                getConfiguration().setNumOfFiles(getConfiguration().getNumOfFiles()+1);
            }
    }

    @Override
    void deleteFiles(ArrayList<String> paths) {
        for(String path: paths)
            deleteFile(path);
    }

    @Override
    void deleteDirectory(String path) throws IOException {
        File file = new File(path);
        if(file.exists() && file.isDirectory() && isInStorage(path)) {
            FileUtils.deleteDirectory(file);
            getConfiguration().setNumOfFiles(getConfiguration().getNumOfFiles()+1);
        }
    }

    @Override
    void deleteDirectories(ArrayList<String> paths) throws IOException {
        for(String path: paths)
            deleteDirectory(path);
    }

    @Override
    void moveFile(String source, String target) {
        File file = new File(source);
        File newFile = new File(target+ File.separator +file.getName());
        if(checkConfiguration(newFile.getParentFile().getPath(), newFile.getPath()) && file.renameTo(newFile)) {
            file.delete();
            System.out.println("File moved successfully");
        } else {
            System.out.println("Failed to move the file");
        }
    }

    @Override
    void moveFiles(List<String> from, String to){
        for(String path: from)
            moveFile(path, to);
    }

    //TO putanja mora biti izvan skladista
    @Override
    void downloadFile(String from, String to) throws IOException {
        File file = new File(from);
        File newDir = new File(to);
        if(file.exists() && file.isFile() && newDir.exists() && newDir.isDirectory()){
            File targetFile = new File(to + File.separator + file.getName());
            FileUtils.copyFile(file, targetFile);
        } else {
            System.out.println("Failed to download the file");
        }
    }

    @Override
    void downloadFiles(List<String> from, String to) throws IOException{
        for(String path: from){
            downloadFile(path, to);
        }
    }

    @Override
    void renameFile(String path, String newName) {
        File file = new File(path);
        File newFile = new File(file.getParent() + File.separator + newName);
        if(file.exists() && file.isFile() && isInStorage(path)) {
            if (file.renameTo(newFile))
                System.out.println("File Successfully Renamed");
            else
                System.out.println("Failed to rename the file");
        } else {
            System.out.println("File not found");
        }
    }

    @Override
    void renameDirectory(String path, String newName) {
        File file = new File(path);
        if(file.exists() && file.isDirectory() && isInStorage(path)){
            File newFile = new File(file.getParent() + File.separator + newName);
            if (file.renameTo(newFile))
                System.out.println("Directory Successfully Renamed");
            else
                System.out.println("Operation Failed");
        } else {
            System.out.println("Directory not found");
        }
    }

    //SEARCH ROOT METHODS
    @Override
    List<MyFile> returnFilesInDirectory(String path) {
        File dir = new File(path);
        File[] listOfFiles;
        List<MyFile> list = new ArrayList<>();
        listOfFiles = dir.listFiles();
        for(File f: Objects.requireNonNull(listOfFiles)){
            if(f.isFile()){
                list.add(initMyFile(f, "file"));
            } else if(f.isDirectory()){
                list.add(initMyFile(f, "directory"));
            }
        }
        return list;
    }

    //samo 1 nivo
    @Override
    List<MyFile> filesFromDirectories(String path) {
        File dir = new File(path);
        File[] listOfFiles;
        listOfFiles = dir.listFiles();
        myFileList = new ArrayList<>();
        for (File file : Objects.requireNonNull(listOfFiles)){
            File[] list;
            list= file.listFiles();
            for (File f : Objects.requireNonNull(list)){
                if(f.isFile())
                    myFileList.add((initMyFile(f, "file")));
                else if(f.isDirectory())
                    myFileList.add((initMyFile(f, "directory")));
            }

        }
        return myFileList;
    }

    @Override
    List<MyFile> filesFromDirectoriesAndSubdirectories(String path) {
        File dir = new File(path);
        File[] listOfFiles;
        listOfFiles = dir.listFiles();
        for (File file : Objects.requireNonNull(listOfFiles)) {
            if (file.isDirectory()) {
                //System.out.println("Directory: " + file.getAbsolutePath());
                myFileList.add(initMyFile(file, "directory"));
                myFileList = filesFromDirectoriesAndSubdirectories(file.toString()); // Calls same method again.
            } else {
//                System.out.println("File: " + file.getAbsolutePath());
                myFileList.add(initMyFile(file, "file"));
            }
        }
        return myFileList;
    }

    @Override
    List<MyFile> returnFilesWithExt(String path, String extension) {
        File dir = new File(path);
        File[] listOfFiles;
        List<MyFile> files = new ArrayList<>();
        listOfFiles = dir.listFiles();
        for(File f: Objects.requireNonNull(listOfFiles)){
            String ex = FilenameUtils.getExtension(f.toString());
            System.out.println(ex);
            if(ex.equals(extension) && f.isFile()){
                files.add(initMyFile(f, "file"));
            }
        }
        return files;
    }

    @Override
    List<MyFile> returnFilesWithSubstring(String path, String substring) {
        File dir = new File(path);
        File[] listOfFiles;
        List<MyFile> files = new ArrayList<>();
        listOfFiles = dir.listFiles();
        for(File f: Objects.requireNonNull(listOfFiles)){
            if(f.getName().contains(substring) && f.isFile()){
                files.add(initMyFile(f, "file"));
            }
        }
        return files;
    }

    @Override
    boolean containsFile(String path, String fileName) {
        File dir = new File(path);
        File[] listOfFiles;
        listOfFiles = dir.listFiles();
        for(File f: Objects.requireNonNull(listOfFiles)){
            if(f.getName().equalsIgnoreCase(fileName))
                return true;
        }
        return false;
    }

    @Override
    boolean containsFiles(String path, ArrayList<String> fileNames) {
        for(String name: fileNames){
            if(!containsFile(path, name))
                return false;
        }
        return true;
    }

    @Override
    String findFile(String name) {
        List<MyFile> list = filesFromDirectoriesAndSubdirectories(getRootPath());
        for(MyFile file: list){
            if(file.getName().equals(name)) {
                File child = new File(file.getName());
                return child.getParent();
            }
        }
        return null;
    }

    @Override
    List<MyFile> sortFiles(String path, boolean asc, SortBy by) {
        File root = new File(path);
        File[] files = root.listFiles();
        ArrayList<MyFile> myFiles = new ArrayList<>();

        if(by.equals(SortBy.DATE_MODIFIED) && files!=null){
            if(asc)
                Arrays.sort(files, Comparator.comparingLong(File::lastModified));
            else
                Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
        } else if(by.equals(SortBy.NAME) && files!=null){
            if(asc)
                Arrays.sort(files, NameFileComparator.NAME_COMPARATOR);
            else
                Arrays.sort(files, NameFileComparator.NAME_COMPARATOR.reversed());
        } else if(by.equals(SortBy.DATE_CREATED) && files!=null){
            Arrays.sort(files, (f1, f2) -> {
                long l1 = getFileCreation(f1);
                long l2 = getFileCreation(f2);
                return Long.compare(l1, l2);
            });
        } else if(by.equals(SortBy.SIZE) && files!=null){
            if(asc)
                Arrays.sort(files, Comparator.comparingLong(File::length)
                    .thenComparing(f -> f.getName().substring(f.getName().lastIndexOf(".") + 1)));
            else
                Arrays.sort(files, Comparator.comparingLong(File::length)
                        .thenComparing(f -> f.getName().substring(f.getName().lastIndexOf(".") + 1)).reversed());
        }

        for(File f: Objects.requireNonNull(files)){
            if(f.isFile())
                myFiles.add(initMyFile(f, "file"));
            else if(f.isDirectory())
                myFiles.add(initMyFile(f, "directory"));
        }
        return myFiles;

    }

    public static long getFileCreation (File file) {
        try {
            BasicFileAttributes attr = Files.readAttributes(file.toPath(),
                    BasicFileAttributes.class);
            return attr.creationTime()
                    .toInstant().toEpochMilli();
        } catch (IOException e) {
            throw new RuntimeException(file.getAbsolutePath(), e);
        }
    }

    @Override
    List<MyFile> createdOrModifiedWithinTimePeriod(String path, String from, String to, String type) {
        File root = new File(path);
        File[] files = root.listFiles();
        ArrayList<MyFile> myFiles = new ArrayList<>();
        Date fromDate = null;
        Date toDate = null;
        try {
            fromDate = new SimpleDateFormat("dd/MM/yyyy").parse(from);
            toDate = new SimpleDateFormat("dd/MM/yyyy").parse(to);
        } catch (ParseException p){
            p.printStackTrace();
            System.out.println("Date formatting failed");
        }
        for(File file: Objects.requireNonNull(files)) {
            MyFile myFile = null;
            if (file.isFile())
                myFile = initMyFile(file, "file");
            else if (file.isDirectory())
                myFile = initMyFile(file, "directory");

            Date date = null;
            if (type.equalsIgnoreCase("modified"))
                date = Objects.requireNonNull(myFile).stringToDate("modified");
            else if (type.equalsIgnoreCase("created"))
                date = Objects.requireNonNull(myFile).stringToDate("created");

            if (date != null && date.after(fromDate) && date.before(toDate))
                myFiles.add(myFile);
        }
        return myFiles;
    }

    @Override
    List<String> filter(List<MyFile> results, SortBy filter) {
        List<String> list = new ArrayList<>();
        if(results.isEmpty())
            return null;
        switch (filter){
            case NAME:
                for(MyFile f:results)
                    list.add(f.getName());
                break;
            case SIZE:
                for(MyFile f:results)
                    list.add(String.valueOf(f.getSize()));
                break;
            case DATE_CREATED:
                for(MyFile f:results)
                    list.add(f.getTimeCreated());
                break;
            case DATE_MODIFIED:
                for(MyFile f:results)
                    list.add(f.getLastModified());
                break;
        }
        return list;
    }
}
