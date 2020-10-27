package org.talend.aws;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.event.ProgressListenerChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.internal.FileLocks;
import com.amazonaws.services.s3.internal.RequestCopyUtils;
import com.amazonaws.services.s3.internal.ServiceUtils;
import com.amazonaws.services.s3.model.GetObjectMetadataRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.transfer.TransferManagerConfiguration;
import com.amazonaws.services.s3.transfer.TransferProgress;
import com.amazonaws.services.s3.transfer.exception.FileLockException;
import com.amazonaws.services.s3.transfer.internal.S3ProgressListener;
import com.amazonaws.services.s3.transfer.internal.S3ProgressListenerChain;
import com.amazonaws.services.s3.transfer.internal.TransferManagerUtils;
import com.amazonaws.services.s3.transfer.internal.TransferStateChangeListener;
import com.amazonaws.services.s3.transfer.internal.TransferProgressUpdatingListener;
import com.amazonaws.util.VersionInfoUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.Date;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TransferManager {

	private static final Log log = LogFactory.getLog(TransferManager.class);

	private final AmazonS3 s3;
	private final ExecutorService executorService;
	private final TransferManagerConfiguration configuration;
	private final boolean shutDownThreadPools;

	public TransferManager(AmazonS3 s3) {
		this.s3 = s3;
		this.executorService = TransferManagerUtils.createDefaultExecutorService();
		this.configuration = resolveConfiguration();
		this.shutDownThreadPools = true;
	}

	private TransferManagerConfiguration resolveConfiguration() {
		TransferManagerConfiguration configuration = new TransferManagerConfiguration();
		configuration.setDisableParallelDownloads(false);
		return configuration;
	}

	public Download download(GetObjectRequest getObjectRequest, File file, S3ProgressListener progressListener,
							 long timeoutMillis, boolean resumeOnRetry) {
		return doDownload(getObjectRequest, file, null, progressListener, ServiceUtils.OVERWRITE_MODE, timeoutMillis, null, 0L,
				resumeOnRetry);
	}

	private Download doDownload(final GetObjectRequest getObjectRequest,
								final File file, final TransferStateChangeListener stateListener,
								final S3ProgressListener s3progressListener,
								final boolean resumeExistingDownload,
								final long timeoutMillis,
								final Integer lastFullyDownloadedPart,
								final long lastModifiedTimeRecordedDuringPause,
								final boolean resumeOnRetry)
	{
		assertParameterNotNull(getObjectRequest,
				"A valid GetObjectRequest must be provided to initiate download");
		assertParameterNotNull(file,
				"A valid file must be provided to download into");

		appendSingleObjectUserAgent(getObjectRequest);
		String description = "Downloading from " + getObjectRequest.getBucketName() + "/" + getObjectRequest.getKey();

		TransferProgress transferProgress = new TransferProgress();
		// S3 progress listener to capture the persistable transfer when available
		S3ProgressListenerChain listenerChain = new S3ProgressListenerChain(
				// The listener for updating transfer progress
				new TransferProgressUpdatingListener(transferProgress),
				getObjectRequest.getGeneralProgressListener(),
				s3progressListener); // Listeners included in the original request
		// The listener chain used by the low-level GetObject request.
		// This listener chain ignores any COMPLETE event, so that we could
		// delay firing the signal until the high-level download fully finishes.
		getObjectRequest
				.setGeneralProgressListener(new ProgressListenerChain(new TransferCompletionFilter(), listenerChain));

		GetObjectMetadataRequest getObjectMetadataRequest = RequestCopyUtils.createGetObjectMetadataRequestFrom(getObjectRequest);
		final ObjectMetadata objectMetadata = s3.getObjectMetadata(getObjectMetadataRequest);

		// Used to check if the object is modified between pause and resume
		long lastModifiedTime = objectMetadata.getLastModified().getTime();

		long startingByte = 0;
		long lastByte;

		long[] range = getObjectRequest.getRange();
		if (range != null && range.length == 2) {
			startingByte = range[0];
			lastByte = range[1];
		} else {
			lastByte = objectMetadata.getContentLength() - 1;
		}

		final long origStartingByte = startingByte;
		final boolean isDownloadParallel = !configuration.isDisableParallelDownloads()
				&& TransferManagerUtils.isDownloadParallelizable(s3, getObjectRequest, ServiceUtils.getPartCount(getObjectRequest, s3));

		// We still pass the unfiltered listener chain into DownloadImpl
		final DownloadImpl download = new DownloadImpl(description, transferProgress, listenerChain, null,
				stateListener, getObjectRequest, file, objectMetadata, isDownloadParallel);

		long totalBytesToDownload = lastByte - startingByte + 1;
		transferProgress.setTotalBytesToTransfer(totalBytesToDownload);

		// Range information is needed for auto retry of downloads so a retry
		// request can start at the last downloaded location in the range.
		//
		// For obvious reasons, setting a Range header only makes sense if the
		// object actually has content because it's inclusive, otherwise S3
		// responds with 4xx
		//
		// In addition, we only set the range if the download was *NOT*
		// determined to be parallelizable above. One of the conditions for
		// parallel downloads is that getRange() returns null so preserve that.
		if (totalBytesToDownload > 0 && !isDownloadParallel) {
			getObjectRequest.withRange(startingByte, lastByte);
		}

		long fileLength = -1;

		if (resumeExistingDownload) {
			if (isS3ObjectModifiedSincePause(lastModifiedTime, lastModifiedTimeRecordedDuringPause)) {
				throw new AmazonClientException("The requested object in bucket " + getObjectRequest.getBucketName()
						+ " with key " + getObjectRequest.getKey() + " is modified on Amazon S3 since the last pause.");
			}
			// There's still a chance the object is modified while the request
			// is in flight. Set this header so S3 fails the request if this happens.
			getObjectRequest.setUnmodifiedSinceConstraint(new Date(lastModifiedTime));

			if (!isDownloadParallel) {
				if (!FileLocks.lock(file)) {
					throw new FileLockException("Fail to lock " + file + " for resume download");
				}
				try {
					if (file.exists()) {
						fileLength = file.length();
						startingByte = startingByte + fileLength;
						getObjectRequest.setRange(startingByte, lastByte);
						transferProgress.updateProgress(Math.min(fileLength, totalBytesToDownload));
						totalBytesToDownload = lastByte - startingByte + 1;
						if (log.isDebugEnabled()) {
							log.debug("Resume download: totalBytesToDownload=" + totalBytesToDownload
									+ ", origStartingByte=" + origStartingByte + ", startingByte=" + startingByte
									+ ", lastByte=" + lastByte + ", numberOfBytesRead=" + fileLength + ", file: "
									+ file);
						}
					}
				} finally {
					FileLocks.unlock(file);
				}
			}
		}

		if (totalBytesToDownload < 0) {
			throw new IllegalArgumentException(
					"Unable to determine the range for download operation.");
		}

		final CountDownLatch latch = new CountDownLatch(1);
		Future<?> future = executorService.submit(
				new DownloadCallable(s3, latch,
						getObjectRequest, resumeExistingDownload,
						download, file, origStartingByte, fileLength, timeoutMillis, timedThreadPool,
						executorService, lastFullyDownloadedPart, isDownloadParallel, resumeOnRetry));
		download.setMonitor(new DownloadMonitor(download, future));
		latch.countDown();
		return download;
	}

	public void shutdownNow(boolean shutDownS3Client) {
		if (shutDownThreadPools) {
			executorService.shutdownNow();
			timedThreadPool.shutdownNow();
		}

		if (shutDownS3Client) {
			s3.shutdown();
		}
	}

	private void assertParameterNotNull(Object parameterValue, String errorMessage) {
		if (parameterValue == null) throw new IllegalArgumentException(errorMessage);
	}

	public static <X extends AmazonWebServiceRequest> X appendSingleObjectUserAgent(X request) {
		request.getRequestClientOptions().appendUserAgent(USER_AGENT);
		return request;
	}

	private static final String USER_AGENT = TransferManager.class.getName() + "/" + VersionInfoUtils.getVersion();

	private boolean isS3ObjectModifiedSincePause(final long lastModifiedTimeRecordedDuringResume,
												 long lastModifiedTimeRecordedDuringPause) {
		return lastModifiedTimeRecordedDuringResume != lastModifiedTimeRecordedDuringPause;
	}

	private final ScheduledExecutorService timedThreadPool = new ScheduledThreadPoolExecutor(1, daemonThreadFactory);

	private static final ThreadFactory daemonThreadFactory = new ThreadFactory() {
		final AtomicInteger threadCount = new AtomicInteger( 0 );
		public Thread newThread(Runnable r) {
			int threadNumber = threadCount.incrementAndGet();
			Thread thread = new Thread(r);
			thread.setDaemon(true);
			thread.setName("S3TransferManagerTimedThread-" + threadNumber);
			return thread;
		}
	};

	@Override
	protected void finalize() throws Throwable {
		shutdownThreadPools();
	}

	private void shutdownThreadPools() {
		if (shutDownThreadPools) {
			executorService.shutdown();
			timedThreadPool.shutdown();
		}
	}

}
