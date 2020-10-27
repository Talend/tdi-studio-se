package org.talend.aws;

import java.io.IOException;

import com.amazonaws.services.s3.model.CryptoMode;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.transfer.Transfer;
import com.amazonaws.services.s3.transfer.exception.PauseException;

/**
 * Represents an asynchronous download from Amazon S3.
 */
public interface Download extends Transfer {

    /**
     * Returns the ObjectMetadata for the object being downloaded.
     *
     * @return The ObjectMetadata for the object being downloaded.
     */
    public ObjectMetadata getObjectMetadata();

    /**
     * The name of the bucket where the object is being downloaded from.
     *
     * @return The name of the bucket where the object is being downloaded from.
     */
    public String getBucketName();

    /**
     * The key under which this object was stored in Amazon S3.
     *
     * @return The key under which this object was stored in Amazon S3.
     */
    public String getKey();

    /**
     * Cancels this download.
     *
     * @throws IOException
     */
    public void abort() throws IOException;

    /**
     * Pause the current download operation and returns the information that can
     * be used to resume the download at a later time.
     *
     * Resuming a download would not perform ETag check as range get is
     * performed for downloading the object's remaining contents.
     *
     * Resuming a download for an object encrypted using
     * {@link CryptoMode#StrictAuthenticatedEncryption} would result in
     * AmazonClientException as authenticity cannot be guaranteed for a range
     * get operation.
     *
     * @throws PauseException
     *             If any errors were encountered while trying to pause the
     *             download.
     */
    public PersistableDownload pause() throws PauseException;
}
