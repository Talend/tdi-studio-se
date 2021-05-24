package org.talend.aws;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Encryption;
import com.amazonaws.services.s3.internal.ServiceUtils;
import com.amazonaws.services.s3.internal.SkipMd5CheckStrategy;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

final class DownloadTaskImpl implements
        ServiceUtils.RetryableS3DownloadTask
{
    private final AmazonS3 s3;
    private final DownloadImpl download;
    private final GetObjectRequest getObjectRequest;
    private final SkipMd5CheckStrategy skipMd5CheckStrategy = SkipMd5CheckStrategy.INSTANCE;

    DownloadTaskImpl(AmazonS3 s3, DownloadImpl download,
            GetObjectRequest getObjectRequest) {
        this.s3 = s3;
        this.download = download;
        this.getObjectRequest = getObjectRequest;
    }

    @Override
    public S3Object getS3ObjectStream() {
        S3Object s3Object = s3.getObject(getObjectRequest);
        download.setS3Object(s3Object);
        return s3Object;
    }

    @Override
    public boolean needIntegrityCheck() {
        // Don't perform the integrity check if the checksum won't matchup.
        return !(s3 instanceof AmazonS3Encryption) && !skipMd5CheckStrategy.skipClientSideValidationPerRequest(getObjectRequest);
    }
}
