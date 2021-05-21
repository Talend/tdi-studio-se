package org.talend;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for FileCopy.class with diffents file size.
 * 
 * @author clesaec
 *
 */
class FileCopyTest {

    @Test
    void testCopyFile() throws Exception {
        final URL repCopy = Thread.currentThread().getContextClassLoader().getResource("copy");

        File small = this.buildFile("small.txt", 10L * 1024L);
        small.deleteOnExit();
        File smallCopy = new File(repCopy.getPath(), "small.txt");
        smallCopy.deleteOnExit();

        FileCopy.copyFile(small.getPath(), smallCopy.getPath(), false);

        Assertions.assertTrue(smallCopy.exists(), "small file fail to copy (not created)");
        Assertions.assertTrue(small.exists(), "small file : original file deleted");
        Assertions.assertEquals(smallCopy.length(), small.length(), "Size error");

        File medium = this.buildFile("medium.txt", 30L * 1024L * 1024L);
        medium.deleteOnExit();
        File mediumCopy = new File(repCopy.getPath(), "medium.txt");
        mediumCopy.deleteOnExit();

        FileCopy.copyFile(medium.getPath(), mediumCopy.getPath(), false);

        Assertions.assertTrue(mediumCopy.exists(), "medium file fail to copy (not created)");
        Assertions.assertTrue(medium.exists(), "medium file : original file deleted");
        Assertions.assertEquals(mediumCopy.length(), medium.length(), "Size error");

        File large = this.buildFile("large.txt", 110L * 1024L * 1024L);
        large.deleteOnExit();
        long startTime = System.nanoTime();
        File largeCopy = new File(repCopy.getPath(), "large.txt");
        long duration = System.nanoTime() - startTime;
        System.out.println("Duration for 110 Mo file : " + TimeUnit.NANOSECONDS.toMicros(duration) + " µs");
        largeCopy.deleteOnExit();

        FileCopy.copyFile(large.getPath(), largeCopy.getPath(), false);

        Assertions.assertTrue(largeCopy.exists(), "small file fail to copy (not created)");
        Assertions.assertTrue(large.exists(), "small file : original file deleted");
        Assertions.assertEquals(largeCopy.length(), large.length(), "Size error");
    }

    @Test
    void testCopyMv() throws Exception {
        final URL repCopy = Thread.currentThread().getContextClassLoader().getResource("copy");

        File file = this.buildFile("fileToMove.txt", 10L * 1024L);
        file.deleteOnExit();
        File copy = new File(repCopy.getPath(), "fileToMove.txt");
        long referenceSize = file.length();
        if (copy.exists()) {
            copy.delete();
        }
        copy.deleteOnExit();

        FileCopy.copyFile(file.getPath(), copy.getPath(), true);

        Assertions.assertFalse(file.exists(), "file not delete");
        Assertions.assertTrue(copy.exists(), "small file : original file deleted");
        Assertions.assertEquals(referenceSize, copy.length(), "Size error");
    }

    @Test
    void testCopyWithDelete() throws Exception {
        final URL repCopy = Thread.currentThread().getContextClassLoader().getResource("copy");

        File file = this.buildFile("fileToDelete.txt", 10L * 1024L);
        file.deleteOnExit();
        File copy = new File(repCopy.getPath(), "fileToDelete.txt");
        long referenceSize = file.length();
        if (!copy.exists()) {
            copy.createNewFile();
        }
        copy.deleteOnExit();

        FileCopy.copyFile(file.getPath(), copy.getPath(), true);

        Assertions.assertFalse(file.exists(), "file not delete");
        Assertions.assertTrue(copy.exists(), "small file : original file deleted");
        Assertions.assertEquals(referenceSize, copy.length(), "Size error");
    }

    @Test
    void testForceCopyWithDelete() throws Exception {
        final URL repCopy = Thread.currentThread().getContextClassLoader().getResource("copy");

        File file = this.buildFile("fileToDelete.txt", 10L * 1024L);
        file.deleteOnExit();
        File copy = new File(repCopy.getPath(), "fileToDelete.txt");
        long referenceSize = file.length();
        if (!copy.exists()) {
            copy.createNewFile();
        }
        copy.deleteOnExit();

        FileCopy.forceCopyAndDelete(file.getPath(), copy.getPath());

        Assertions.assertFalse(file.exists(), "file not delete");
        Assertions.assertTrue(copy.exists(), "small file : original file deleted");
        Assertions.assertEquals(referenceSize, copy.length(), "Size error");
    }

    @Test
    void testLastModifiedTime() throws Exception {
        final URL repCopy = Thread.currentThread().getContextClassLoader().getResource("copy");

        File file = this.buildFile("fileLMT.txt", 10L * 1024L);
        file.deleteOnExit();
        long referencceTime = 324723894L;
        file.setLastModified(referencceTime);

        File copy = new File(repCopy.getPath(), "fileLMTDestination.txt");
        if (copy.exists()) {
            copy.delete();
        }
        copy.deleteOnExit();
        FileCopy.copyFile(file.getPath(), copy.getPath(), true);
        Assertions.assertEquals(referencceTime, copy.lastModified(), "modified time is not idential");
    }

    /**
     * Generate a new file for testing.
     * 
     * @param name : name of file.
     * @param minSize : minimum size.
     * @return the new file.
     * @throws IOException : on IO pb.
     */
    private File buildFile(String name, long minSize) throws IOException {
        final URL repGenerated = Thread.currentThread().getContextClassLoader().getResource("generated");
        final File generatedFile = new File(repGenerated.getPath(), name);
        if (generatedFile.exists()) {
            generatedFile.delete();
        }
        final String data = "{ data to put in generated file for it have the desired sized }" + System.lineSeparator();

        long nbeIteration = (minSize / data.length()) + 1;
        try (BufferedWriter writer = Files.newBufferedWriter(generatedFile.toPath(), StandardOpenOption.CREATE)) {
            for (long i = 0; i < nbeIteration; i++) {
                writer.append(data);
            }
        }

        return generatedFile;
    }

    @Test
    void testKeepLastModifiedTime() throws Exception {
        final URL repCopy = Thread.currentThread().getContextClassLoader().getResource("copy");

        File file = this.buildFile("fileLMT.txt", 10L * 1024L);
        file.deleteOnExit();
        long referencceTime = 324723894L;
        file.setLastModified(referencceTime);

        File copy = new File(repCopy.getPath(), "fileLMTDestination.txt");
        if (copy.exists()) {
            copy.delete();
        }
        copy.deleteOnExit();
        FileCopy.copyFile(file.getPath(), copy.getPath(), true,true);
        Assertions.assertEquals(referencceTime, copy.lastModified(), "modified time is not idential");
    }
}
