package util;

import org.junit.jupiter.api.Test;
import org.talend.gs.util.GSObjectUtil;

import java.io.File;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GSObjectUtilTest {

    @Test
    void checkSizeOfFilesInFolderTest() {
        String pathToDirectory = "src/test/resources/dirWithThreeFiles";
        String pathToFile1 = "src/test/resources/dirWithThreeFiles/1.csv";
        String pathToFile2 = "src/test/resources/dirWithThreeFiles/2.csv";
        String pathToFile3 = "src/test/resources/dirWithThreeFiles/3.csv";
        String pathToFile4 = "src/test/resources/dirWithThreeFiles/inner/1.csv";

        String expectedRelativePathToFile1 = "/1.csv";
        String expectedRelativePathToFile2 = "/2.csv";
        String expectedRelativePathToFile3 = "/3.csv";
        String expectedRelativePathToFile4 = "/inner/1.csv";

        File rootFile = new File(pathToDirectory);
        GSObjectUtil gsObjectUtil = new GSObjectUtil();
        Map<String, File> files = gsObjectUtil.generateFileMap(rootFile, "/");
        assertEquals(4, files.size());

        File expectedFile1 = new File(pathToFile1);
        File expectedFile2 = new File(pathToFile2);
        File expectedFile3 = new File(pathToFile3);
        File expectedFile4 = new File(pathToFile4);

        assertTrue(files.containsKey(expectedRelativePathToFile1));
        assertTrue(files.containsKey(expectedRelativePathToFile2));
        assertTrue(files.containsKey(expectedRelativePathToFile3));
        assertTrue(files.containsKey(expectedRelativePathToFile4));

        assertEquals(expectedFile1.getAbsoluteFile(), files.get(expectedRelativePathToFile1).getAbsoluteFile());
        assertEquals(expectedFile2.getAbsoluteFile(), files.get(expectedRelativePathToFile2).getAbsoluteFile());
        assertEquals(expectedFile3.getAbsoluteFile(), files.get(expectedRelativePathToFile3).getAbsoluteFile());
        assertEquals(expectedFile4.getAbsoluteFile(), files.get(expectedRelativePathToFile4).getAbsoluteFile());
    }

    @Test
    void checkSizeOfFilesInFolder2Test() {
        String pathToDirectory = "src/test/resources/dirWithThreeFiles";
        String pathToFile1 = "src/test/resources/dirWithThreeFiles/1.csv";
        String pathToFile2 = "src/test/resources/dirWithThreeFiles/2.csv";
        String pathToFile3 = "src/test/resources/dirWithThreeFiles/3.csv";
        String pathToFile4 = "src/test/resources/dirWithThreeFiles/inner/1.csv";

        String expectedRelativePathToFile1 = "1.csv";
        String expectedRelativePathToFile2 = "2.csv";
        String expectedRelativePathToFile3 = "3.csv";
        String expectedRelativePathToFile4 = "inner/1.csv";

        File rootFile = new File(pathToDirectory);
        GSObjectUtil gsObjectUtil = new GSObjectUtil();
        Map<String, File> files = gsObjectUtil.generateFileMap(rootFile, "");
        assertEquals(4, files.size());

        File expectedFile1 = new File(pathToFile1);
        File expectedFile2 = new File(pathToFile2);
        File expectedFile3 = new File(pathToFile3);
        File expectedFile4 = new File(pathToFile4);

        assertTrue(files.containsKey(expectedRelativePathToFile1));
        assertTrue(files.containsKey(expectedRelativePathToFile2));
        assertTrue(files.containsKey(expectedRelativePathToFile3));
        assertTrue(files.containsKey(expectedRelativePathToFile4));

        assertEquals(expectedFile1.getAbsoluteFile(), files.get(expectedRelativePathToFile1).getAbsoluteFile());
        assertEquals(expectedFile2.getAbsoluteFile(), files.get(expectedRelativePathToFile2).getAbsoluteFile());
        assertEquals(expectedFile3.getAbsoluteFile(), files.get(expectedRelativePathToFile3).getAbsoluteFile());
        assertEquals(expectedFile4.getAbsoluteFile(), files.get(expectedRelativePathToFile4).getAbsoluteFile());
    }

    @Test
    void oneFileWithCustomPathTest(){
        String pathToFile = "src/test/resources/empty.csv";
        String pathToFolder = "/folder/folder";
        File expectedFile = new File(pathToFile);
        GSObjectUtil gsObjectUtil = new GSObjectUtil();

        Map<String, File> files = gsObjectUtil.generateFileMap(expectedFile, pathToFolder);

        String expectedRelativePathToFile = "/folder/folder/empty.csv";

        assertEquals(1, files.size());
        assertTrue(files.containsKey(expectedRelativePathToFile));
        assertEquals(expectedFile.getAbsoluteFile(), files.get(expectedRelativePathToFile).getAbsoluteFile());
    }

    @Test
    void oneFileWithOtherCustomPathTest(){
        String pathToFile = "src/test/resources/empty.csv";
        String pathToFolder = "folder/folder";
        File expectedFile = new File(pathToFile);
        GSObjectUtil gsObjectUtil = new GSObjectUtil();

        Map<String, File> files = gsObjectUtil.generateFileMap(expectedFile, pathToFolder);

        String expectedRelativePathToFile = "folder/folder/empty.csv";

        assertEquals(1, files.size());
        assertTrue(files.containsKey(expectedRelativePathToFile));
        assertEquals(expectedFile.getAbsoluteFile(), files.get(expectedRelativePathToFile).getAbsoluteFile());
    }

    @Test
    void folderWithOtherCustomPathTest(){
        String pathToFolder = "src/test/resources/dirWithThreeFiles/inner";
        String pathToGSFolder = "folder";
        File folder = new File(pathToFolder);
        GSObjectUtil gsObjectUtil = new GSObjectUtil();

        Map<String, File> files = gsObjectUtil.generateFileMap(folder, pathToGSFolder);

        String expectedRelativePathToFile = "folder/1.csv";
        File expectedFile = new File(pathToFolder + "/1.csv");

        assertEquals(1, files.size());
        assertTrue(files.containsKey(expectedRelativePathToFile));
        assertEquals(expectedFile.getAbsoluteFile(), files.get(expectedRelativePathToFile).getAbsoluteFile());
    }
}