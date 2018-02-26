// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.salesforceBulk;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;

import com.sforce.async.AsyncApiException;
import com.sforce.async.AsyncExceptionCode;
import com.sforce.async.BatchInfo;
import com.sforce.async.BatchInfoList;
import com.sforce.async.BulkConnection;
import com.sforce.async.JobInfo;
import com.sforce.async.QueryResultList;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

/**
 * created by bchen on Jul 11, 2014 Detailled comment
 * 
 */
public abstract class SforceBulkConnection {

    protected BulkConnection connection;

    protected ConnectorConfig config;

    protected void renewSession() throws ConnectionException {
        throw new ConnectionException("Session expire, need to renew session");
    }

    protected JobInfo createJob(JobInfo job) throws AsyncApiException, ConnectionException {
        try {
            return connection.createJob(job);
        } catch (AsyncApiException sfException) {
            if (AsyncExceptionCode.InvalidSessionId.equals(sfException.getExceptionCode())) {
                renewSession();
                return createJob(job);
            }
            throw sfException;
        }
    }

    protected BatchInfo createBatchFromStream(JobInfo job, FileInputStream inputStream) throws AsyncApiException,
            ConnectionException {
        try {
            return connection.createBatchFromStream(job, inputStream);
        } catch (AsyncApiException sfException) {
            if (AsyncExceptionCode.InvalidSessionId.equals(sfException.getExceptionCode())) {
                renewSession();
                return createBatchFromStream(job, inputStream);
            }
            throw sfException;
        }
    }

    protected JobInfo updateJob(JobInfo job) throws AsyncApiException, ConnectionException {
        try {
            return connection.updateJob(job);
        } catch (AsyncApiException sfException) {
            if (AsyncExceptionCode.InvalidSessionId.equals(sfException.getExceptionCode())) {
                renewSession();
                return updateJob(job);
            }
            throw sfException;
        }
    }

    protected BatchInfoList getBatchInfoList(String jobID) throws AsyncApiException, ConnectionException {
        try {
            return connection.getBatchInfoList(jobID);
        } catch (AsyncApiException sfException) {
            if (AsyncExceptionCode.InvalidSessionId.equals(sfException.getExceptionCode())) {
                renewSession();
                return getBatchInfoList(jobID);
            }
            throw sfException;
        }
    }

    protected InputStream getBatchResultStream(String jobID, String batchID) throws AsyncApiException, ConnectionException {
        try {
            return connection.getBatchResultStream(jobID, batchID);
        } catch (AsyncApiException sfException) {
            if (AsyncExceptionCode.InvalidSessionId.equals(sfException.getExceptionCode())) {
                renewSession();
                return getBatchResultStream(jobID, batchID);
            }
            throw sfException;
        }
    }

    protected JobInfo getJobStatus(String jobID) throws AsyncApiException, ConnectionException {
        try {
            return connection.getJobStatus(jobID);
        } catch (AsyncApiException sfException) {
            if (AsyncExceptionCode.InvalidSessionId.equals(sfException.getExceptionCode())) {
                renewSession();
                return getJobStatus(jobID);
            }
            throw sfException;
        }
    }

    protected BatchInfo createBatchFromStream(JobInfo job, ByteArrayInputStream byteArrayInputStream) throws AsyncApiException,
            ConnectionException {
        try {
            return connection.createBatchFromStream(job, byteArrayInputStream);
        } catch (AsyncApiException sfException) {
            if (AsyncExceptionCode.InvalidSessionId.equals(sfException.getExceptionCode())) {
                renewSession();
                return createBatchFromStream(job, byteArrayInputStream);
            }
            throw sfException;
        }
    }

    protected BatchInfo getBatchInfo(String jobID, String batchID) throws AsyncApiException, ConnectionException {
        try {
            return connection.getBatchInfo(jobID, batchID);
        } catch (AsyncApiException sfException) {
            if (AsyncExceptionCode.InvalidSessionId.equals(sfException.getExceptionCode())) {
                renewSession();
                return getBatchInfo(jobID, batchID);
            }
            throw sfException;
        }
    }

    protected QueryResultList getQueryResultList(String jobID, String batchID) throws AsyncApiException, ConnectionException {
        try {
            return connection.getQueryResultList(jobID, batchID);
        } catch (AsyncApiException sfException) {
            if (AsyncExceptionCode.InvalidSessionId.equals(sfException.getExceptionCode())) {
                renewSession();
                return getQueryResultList(jobID, batchID);
            }
            throw sfException;
        }
    }

    protected InputStream getQueryResultStream(String jobID, String batchID, String resultID) throws AsyncApiException,
            ConnectionException {
        try {
            return connection.getQueryResultStream(jobID, batchID, resultID);
        } catch (AsyncApiException sfException) {
            if (AsyncExceptionCode.InvalidSessionId.equals(sfException.getExceptionCode())) {
                renewSession();
                return getQueryResultStream(jobID, batchID, resultID);
            }
            throw sfException;
        }
    }

}
