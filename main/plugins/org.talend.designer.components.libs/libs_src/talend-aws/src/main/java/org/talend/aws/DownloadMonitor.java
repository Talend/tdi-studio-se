package org.talend.aws;

import com.amazonaws.services.s3.transfer.internal.TransferMonitor;

import java.util.concurrent.Future;

public class DownloadMonitor implements TransferMonitor {

    private Future<?> future;
    private final DownloadImpl download;

    public DownloadMonitor(DownloadImpl download, Future<?> future) {
        this.download = download;
        this.future = future;
    }

    @Override
    public synchronized Future<?> getFuture() {
        return future;
    }

    public synchronized void setFuture(Future<?> future) {
        this.future = future;
    }

    @Override
    public boolean isDone() {
        return download.isDone();
    }
}
