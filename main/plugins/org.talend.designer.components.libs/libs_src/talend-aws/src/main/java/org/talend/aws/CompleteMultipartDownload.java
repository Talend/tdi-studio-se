package org.talend.aws;

import com.amazonaws.annotation.SdkInternalApi;
import com.amazonaws.services.s3.internal.ServiceUtils;
import com.amazonaws.services.s3.transfer.Transfer;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * Helper class to merge all the individual part files into a destinationFile.
 */
@SdkInternalApi
public class CompleteMultipartDownload implements Callable<File> {
    private final List<Future<File>> partFiles;
    private final File destinationFile;
    private final DownloadImpl download;
    private Integer currentPartNumber;

    public CompleteMultipartDownload(List<Future<File>> files, File destinationFile, DownloadImpl download, Integer currentPartNumber) {
        this.partFiles = files;
        this.destinationFile = destinationFile;
        this.download = download;
        this.currentPartNumber = currentPartNumber;
    }

    @Override
    public File call() throws Exception {
        for (Future<File> file : partFiles) {
            ServiceUtils.appendFile(file.get(), destinationFile);
            download.updatePersistableTransfer(currentPartNumber++);
        }

        download.setState(Transfer.TransferState.Completed);
        return destinationFile;
    }
}
