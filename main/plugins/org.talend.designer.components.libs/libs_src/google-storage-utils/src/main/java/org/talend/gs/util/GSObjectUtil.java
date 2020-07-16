package org.talend.gs.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jets3t.service.model.GSObject;
import org.jets3t.service.utils.Mimetypes;

public class GSObjectUtil {

    public List<GSObject> genGSObjectList(List<GSObject> objects, File file, String keyParent, boolean isGenFileObject,
            boolean isGenFolderObject) throws NoSuchAlgorithmException, IOException {
        if (file.isDirectory()) {
            if (keyParent != null && !"".equals(keyParent)) {
                if (keyParent.trim().lastIndexOf("/") != keyParent.trim().length() - 1) {
                    keyParent = keyParent + "/";
                }
                if (isGenFolderObject) {
                    objects.add(new GSObject(keyParent));
                }
            }
            File[] files = file.listFiles();
            for (File f : files) {
                if (f.isDirectory()) {
                    objects = genGSObjectList(objects, f, keyParent + f.getName() + "/", isGenFileObject,
                            isGenFolderObject);
                } else {
                    objects = genGSObjectList(objects, f, keyParent + f.getName(), isGenFileObject, isGenFolderObject);
                }
            }
        } else {
            if (isGenFileObject) {
                GSObject obj = new GSObject(file);
                obj.setKey(keyParent);
                objects.add(obj);
            }
        }
        return objects;
    }

    public Map<String, File> generateFileMap(File rootFile, String directory, Map<String, String> filter){
        Map<String, File> allFiles = generateFileMap(rootFile, directory);
        Map<String, File> resultMap = new HashMap<>();
        for (Map.Entry<String, String> entry : filter.entrySet()) {
            //key is path to file on local machine
            //value is new path in GS
            File currentFile = new File(entry.getKey());
            if (allFiles.containsValue(currentFile)){
                resultMap.put(entry.getValue(), currentFile);
            }
        }
        return resultMap;
    }

    public Map<String, File> generateFileMap(File rootFile, String directory){
        if (rootFile == null){
            throw new IllegalArgumentException("File can't be null");
        }
        if (directory == null){
            throw new IllegalArgumentException("Directory name can't be null");
        }

        if (!directory.isEmpty() && !"/".equals(directory.substring(directory.length()-1))){
            directory = directory + "/";
        }

        Map<String,File> fileMap = new HashMap<>();

        if (!rootFile.isDirectory()){
            fileMap.put(directory + rootFile.getName(), rootFile);
            return fileMap;
        }

        String pathToDirectory = rootFile.getAbsolutePath();
        List<File> allFiles = listAllFiles(pathToDirectory);

        for (File currentFile : allFiles){
            if (!currentFile.isDirectory()){
                fileMap.put(combineTwoNames(pathToDirectory, directory, currentFile), currentFile);
            }
        }
        return fileMap;
    }

    public java.util.Map<String, String> genFileFilterList(java.util.List<java.util.Map<String, String>> list,
            String localdir, String remotedir) {
        if (remotedir != null && !"".equals(remotedir)) {
            if (remotedir.trim().lastIndexOf("/") != remotedir.trim().length() - 1) {
                remotedir = remotedir + "/";
            }
        }
        java.util.Map<String, String> fileMap = new HashMap<String, String>();
        for (java.util.Map<String, String> map : list) {
            java.util.Set<String> keySet = map.keySet();
            for (String key : keySet) {
                String tempdir = localdir;
                String filemask = key;
                String dir = null;
                String mask = filemask.replaceAll("\\\\", "/");
                int i = mask.lastIndexOf('/');
                if (i != -1) {
                    dir = mask.substring(0, i);
                    mask = mask.substring(i + 1);
                }
                if (dir != null && !"".equals(dir))
                    tempdir = tempdir + "/" + dir;
                mask = mask.replaceAll("\\.", "\\\\.").replaceAll("\\*", ".*");
                final String finalMask = mask;
                java.io.File[] listings = null;
                java.io.File file = new java.io.File(tempdir);
                if (file.isDirectory()) {
                    listings = file.listFiles(new java.io.FileFilter() {

                        public boolean accept(java.io.File pathname) {
                            boolean result = false;
                            if (pathname != null && pathname.isFile()) {
                                result = java.util.regex.Pattern.compile(finalMask).matcher(pathname.getName()).find();
                            }
                            return result;
                        }
                    });
                }
                if (listings == null || listings.length <= 0) {
                    System.err.println("No match file(" + key + ") exist!");
                } else {
                    String localFilePath = "";
                    String newObjectKey = "";
                    for (int m = 0; m < listings.length; m++) {
                        if (listings[m].getName().matches(mask)) {
                            localFilePath = listings[m].getAbsolutePath();
                            if (map.get(key) != null && map.get(key).length() > 0) {
                                newObjectKey = remotedir + map.get(key);
                            } else {
                                newObjectKey = remotedir + listings[m].getName();
                            }
                            fileMap.put(localFilePath, newObjectKey);
                        }
                    }

                }
            }
        }
        return fileMap;
    }

    public List<GSObject> genObjectByFileMap(java.util.Map<String, String> fileMap) throws Exception, IOException {
        Set<String> localFiles = fileMap.keySet();
        List<GSObject> objects = new ArrayList<GSObject>();
        for (String localFilePath : localFiles) {
            GSObject object = new GSObject(new File(localFilePath));
            object.setKey(fileMap.get(localFilePath));
            objects.add(object);
        }
        return objects;
    }

    public void initMimeTypes() throws IOException {
        InputStream mimetypesFile = this.getClass().getResourceAsStream("/resource/mime.types");
        Mimetypes.getInstance().loadAndReplaceMimetypes(mimetypesFile);
    }

    private List<File> listAllFiles(String directoryName) {
        File directory = new File(directoryName);
        List<File> resultList = new ArrayList<>();
        File[] fList = directory.listFiles();
        resultList.addAll(Arrays.asList(fList));
        for (File file : fList) {
            if (file.isDirectory()) {
                resultList.addAll(listAllFiles(file.getAbsolutePath()));
            }
        }
        return resultList;
    }

    private String combineTwoNames(String coreDir, String targetDir, File file){
        String filePath = file.getAbsolutePath();
        String relativePath =  targetDir + filePath.replace(coreDir + FileSystems.getDefault().getSeparator(), "");
        return relativePath;
    }
}
