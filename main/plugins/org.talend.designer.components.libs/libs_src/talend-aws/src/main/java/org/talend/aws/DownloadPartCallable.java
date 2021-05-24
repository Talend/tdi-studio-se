package org.talend.aws;

import com.amazonaws.util.StringUtils;
import java.io.File;
import java.util.UUID;
import java.util.concurrent.Callable;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Helper class to get a part from s3,
 * write the part data to a temporary file and
 * return the temporary file.
 */
public class DownloadPartCallable implements Callable<File> {
    private static final Log LOG = LogFactory.getLog(DownloadPartCallable.class);
    private static final String TEMP_FILE_MIDDLE_NAME = ".part.";

    private final AmazonS3 s3;
    private final GetObjectRequest getPartRequest;
    private final File destinationFile;
    private final String destinationFilePath;

    public DownloadPartCallable(AmazonS3 s3, GetObjectRequest getPartRequest, File destinationFile) {
        this.s3 = s3;
        this.getPartRequest = getPartRequest;
        this.destinationFile = destinationFile;
        this.destinationFilePath = destinationFile.getAbsolutePath();
    }

    public File call() throws Exception {
        final File partFile = File.createTempFile(
                UUID.nameUUIDFromBytes(destinationFile.getName().getBytes(StringUtils.UTF8)).toString(),
                TEMP_FILE_MIDDLE_NAME + getPartRequest.getPartNumber().toString(),
                new File(destinationFilePath.substring(0, destinationFilePath.lastIndexOf(File.separator))));
        try {
            partFile.deleteOnExit();
        } catch (SecurityException exception) {
            LOG.warn("SecurityException denied delete access to file " + partFile.getAbsolutePath());
        }

        if (s3.getObject(getPartRequest, partFile) == null) {
            throw new SdkClientException(
                    "There is no object in S3 satisfying this request. The getObject method returned null");
        }
        return partFile;
    }
}
