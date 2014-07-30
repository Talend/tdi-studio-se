package org.talend.salesforceBulk;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sforce.async.AsyncApiException;
import com.sforce.async.BatchInfo;
import com.sforce.async.BatchStateEnum;
import com.sforce.async.CSVReader;
import com.sforce.async.ConcurrencyMode;
import com.sforce.async.ContentType;
import com.sforce.async.JobInfo;
import com.sforce.async.JobStateEnum;
import com.sforce.async.OperationEnum;
import com.sforce.async.QueryResultList;
import com.sforce.ws.ConnectionException;

public class SalesforceBulkAPI {

    private final String FILE_ENCODING = "UTF-8";

    private String sObjectType;

    private OperationEnum operation;

    private String externalIdFieldName;

    private ContentType contentType;

    private String bulkFileName;

    private int maxBytesPerBatch;

    private int maxRowsPerBatch;

    private List<BatchInfo> batchInfoList;

    private void setBulkOperation(String sObjectType, String operationStr, String externalIdFieldName, String contentTypeStr,
            String bulkFileName, int maxBytes, int maxRows) {
        this.sObjectType = sObjectType;
        if ("insert".equals(operationStr)) {
            operation = OperationEnum.insert;
        } else if ("update".equals(operationStr)) {
            operation = OperationEnum.update;
        } else if ("upsert".equals(operationStr)) {
            operation = OperationEnum.upsert;
        } else if ("delete".equals(operationStr)) {
            operation = OperationEnum.delete;
        }

        this.externalIdFieldName = externalIdFieldName;

        if ("csv".equals(contentTypeStr)) {
            contentType = ContentType.CSV;
        } else if ("xml".equals(contentTypeStr)) {
            contentType = ContentType.XML;
        }
        this.bulkFileName = bulkFileName;

        int sforceMaxBytes = 10 * 1024 * 1024;
        int sforceMaxRows = 10000;
        maxBytesPerBatch = (maxBytes > sforceMaxBytes) ? sforceMaxBytes : maxBytes;
        maxRowsPerBatch = (maxRows > sforceMaxRows) ? sforceMaxRows : maxRows;
    }

    private SforceBulkConnection connection;

    public SalesforceBulkAPI(SforceBulkConnection connection) {
        this.connection = connection;
    }

    private JobInfo job;

    private com.talend.csv.CSVReader baseFileReader;

    private List<String> baseFileHeader;

    private int baseFileHeaderSize;

    public void executeBulk(String sObjectType, String operationStr, String externalIdFieldName, String contentTypeStr,
            String bulkFileName, int maxBytes, int maxRows) throws AsyncApiException, ConnectionException, IOException {
        setBulkOperation(sObjectType, operationStr, externalIdFieldName, contentTypeStr, bulkFileName, maxBytes, maxRows);
        job = createJob();
        batchInfoList = createBatchesFromCSVFile();
        closeJob();
        awaitCompletion();
        prepareLog();
    }

    private void prepareLog() throws IOException {
        baseFileReader = new com.talend.csv.CSVReader(new java.io.BufferedReader(new java.io.InputStreamReader(
                new java.io.FileInputStream(bulkFileName), FILE_ENCODING)), ',');
        if (baseFileReader.readNext()) {
            baseFileHeader = Arrays.asList(baseFileReader.getValues());
        }
        baseFileHeaderSize = baseFileHeader.size();
    }

    private ConcurrencyMode concurrencyMode = null;

    public void setConcurrencyMode(String mode) {
        concurrencyMode = ConcurrencyMode.valueOf(mode);
    }

    private JobInfo createJob() throws AsyncApiException, ConnectionException {
        JobInfo job = new JobInfo();
        if (concurrencyMode != null) {
            job.setConcurrencyMode(concurrencyMode);
        }
        job.setObject(sObjectType);
        job.setOperation(operation);
        if (OperationEnum.upsert.equals(operation)) {
            job.setExternalIdFieldName(externalIdFieldName);
        }
        job.setContentType(contentType);
        job = connection.createJob(job);
        // System.out.println(job);
        return job;
    }

    private int countQuotes(String value) {
        if (value == null || "".equals(value)) {
            return 0;
        } else {
            char c = '\"';
            int num = 0;
            char[] chars = value.toCharArray();
            for (char d : chars) {
                if (c == d) {
                    num++;
                }
            }
            return num;
        }
    }

    private List<BatchInfo> createBatchesFromCSVFile() throws IOException, AsyncApiException, ConnectionException {
        List<BatchInfo> batchInfos = new ArrayList<BatchInfo>();
        BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream(bulkFileName), FILE_ENCODING));
        // read the CSV header row
        byte[] headerBytes = (rdr.readLine() + "\n").getBytes("UTF-8");
        int headerBytesLength = headerBytes.length;
        File tmpFile = File.createTempFile("sforceBulkAPI", ".csv");
        // Split the CSV file into multiple batches
        try {
            FileOutputStream tmpOut = new FileOutputStream(tmpFile);
            int currentBytes = 0;
            int currentLines = 0;
            String nextLine;
            boolean needStart = true;
            boolean needEnds = true;
            while ((nextLine = rdr.readLine()) != null) {
                int num = countQuotes(nextLine);
                // nextLine is header or footer of the record
                if (num % 2 == 1) {
                    if (!needStart) {
                        needEnds = false;
                    } else {
                        needStart = false;
                    }
                } else {
                    // nextLine is a whole record or middle of the record
                    if (needEnds && needStart) {
                        needEnds = false;
                        needStart = false;
                    }
                }

                byte[] bytes = (nextLine + "\n").getBytes("UTF-8");

                // Create a new batch when our batch size limit is reached
                if (currentBytes + bytes.length > maxBytesPerBatch || currentLines > maxRowsPerBatch) {
                    createBatch(tmpOut, tmpFile, batchInfos);
                    currentBytes = 0;
                    currentLines = 0;
                }
                if (currentBytes == 0) {
                    tmpOut = new FileOutputStream(tmpFile);
                    tmpOut.write(headerBytes);
                    currentBytes = headerBytesLength;
                    currentLines = 1;
                }
                tmpOut.write(bytes);
                currentBytes += bytes.length;
                if (!needStart && !needEnds) {
                    currentLines++;
                    needStart = true;
                    needEnds = true;
                }
            }
            // Finished processing all rows
            // Create a final batch for any remaining data
            rdr.close();
            if (currentLines > 1) {
                createBatch(tmpOut, tmpFile, batchInfos);
            }
        } finally {
            tmpFile.delete();
        }
        return batchInfos;
    }

    private void createBatch(FileOutputStream tmpOut, File tmpFile, List<BatchInfo> batchInfos) throws IOException,
            AsyncApiException, ConnectionException {
        tmpOut.flush();
        tmpOut.close();
        FileInputStream tmpInputStream = new FileInputStream(tmpFile);
        try {
            BatchInfo batchInfo = connection.createBatchFromStream(job, tmpInputStream);
            // System.out.println(batchInfo);
            batchInfos.add(batchInfo);
        } finally {
            tmpInputStream.close();
        }
    }

    private void closeJob() throws AsyncApiException, ConnectionException {
        JobInfo closeJob = new JobInfo();
        closeJob.setId(job.getId());
        closeJob.setState(JobStateEnum.Closed);
        connection.updateJob(closeJob);
    }

    private long awaitTime = 10000L;

    public void setAwaitTime(long awaitTime) {
        this.awaitTime = awaitTime;
    }

    private void awaitCompletion() throws AsyncApiException, ConnectionException {
        long sleepTime = 0L;
        Set<String> incomplete = new HashSet<String>();
        for (BatchInfo bi : batchInfoList) {
            incomplete.add(bi.getId());
        }
        while (!incomplete.isEmpty()) {
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
            }
            // System.out.println("Awaiting results..." + incomplete.size());
            sleepTime = awaitTime;
            BatchInfo[] statusList = connection.getBatchInfoList(job.getId()).getBatchInfo();
            for (BatchInfo b : statusList) {
                if (b.getState() == BatchStateEnum.Completed || b.getState() == BatchStateEnum.Failed) {
                    incomplete.remove(b.getId());
                }
            }
        }
    }

    private Map<String, String> getBaseFileRow() throws IOException {
        Map<String, String> dataInfo = new HashMap<String, String>();
        if (baseFileReader.readNext()) {
            List<String> row = Arrays.asList(baseFileReader.getValues());
            for (int i = 0; i < row.size(); i++) {
                dataInfo.put(baseFileHeader.get(i), row.get(i));
            }
        }
        return dataInfo;
    }

    public List<Map<String, String>> getBatchLog(int batchNum) throws AsyncApiException, IOException, ConnectionException {
        // batchInfoList was populated when batches were created and submitted
        List<Map<String, String>> resultInfoList = new ArrayList<Map<String, String>>();
        Map<String, String> resultInfo;
        BatchInfo b = batchInfoList.get(batchNum);
        CSVReader rdr = new CSVReader(connection.getBatchResultStream(job.getId(), b.getId()));

        List<String> resultHeader = rdr.nextRecord();
        int resultCols = resultHeader.size();
        List<String> row;
        while ((row = rdr.nextRecord()) != null) {
            resultInfo = new HashMap<String, String>();
            resultInfo.putAll(getBaseFileRow());
            for (int i = 0; i < resultCols; i++) {
                resultInfo.put(resultHeader.get(i), row.get(i));
            }
            resultInfoList.add(resultInfo);
            // boolean success = Boolean.valueOf(resultInfo.get("Success"));
            // boolean created = Boolean.valueOf(resultInfo.get("Created"));
            // String id = resultInfo.get("Id");
            // String error = resultInfo.get("Error");
            // if (success && created) {
            // System.out.println("Created row with id " + id);
            // } else if (!success) {
            // System.out.println("Failed with error: " + error);
            // }
        }

        return resultInfoList;
    }

    public int getBatchCount() {
        return batchInfoList.size();
    }

    private String[] queryResultIDs = null;

    public void doBulkQuery(String moduleName, String queryStatement, int secToWait) throws AsyncApiException,
            InterruptedException, ConnectionException {
        job = new JobInfo();
        job.setObject(moduleName);
        job.setOperation(OperationEnum.query);
        if (concurrencyMode != null) {
            job.setConcurrencyMode(concurrencyMode);
        }
        job.setContentType(ContentType.CSV);
        job = connection.createJob(job);

        job = connection.getJobStatus(job.getId());
        batchInfoList = new ArrayList<BatchInfo>();
        BatchInfo info = null;
        ByteArrayInputStream bout = new ByteArrayInputStream(queryStatement.getBytes());
        info = connection.createBatchFromStream(job, bout);

        while (true) {
            Thread.sleep(secToWait * 1000); // default is 30 sec
            info = connection.getBatchInfo(job.getId(), info.getId());

            if (info.getState() == BatchStateEnum.Completed) {
                QueryResultList list = connection.getQueryResultList(job.getId(), info.getId());
                queryResultIDs = list.getResult();
                break;
            } else if (info.getState() == BatchStateEnum.Failed) {
                throw new RuntimeException("-------------- failed ----------" + info);
            } else {
                System.out.println("-------------- waiting ----------" + info);
            }
        }
        batchInfoList.add(info);
        // For TDI-27909
        closeJob();
    }

    public String[] getQueryResultIDs() {
        return queryResultIDs;
    }

    public InputStream getQueryResultStream(String resultId) throws AsyncApiException, IOException, ConnectionException {
        return connection.getQueryResultStream(job.getId(), batchInfoList.get(0).getId(), resultId);
    }

    public List<Map<String, String>> getQueryResult(String resultId) throws AsyncApiException, IOException, ConnectionException {
        // batchInfoList was populated when batches were created and submitted
        List<Map<String, String>> resultInfoList = new ArrayList<Map<String, String>>();
        Map<String, String> resultInfo;
        // fix for TDI-26533
        baseFileReader = new com.talend.csv.CSVReader(new java.io.BufferedReader(new java.io.InputStreamReader(
                connection.getQueryResultStream(job.getId(), batchInfoList.get(0).getId(), resultId), FILE_ENCODING)), ',');

        if (baseFileReader.readNext()) {
            baseFileHeader = Arrays.asList(baseFileReader.getValues());
        }
        baseFileHeaderSize = baseFileHeader.size();
        String[] row;
        while (baseFileReader.readNext()) {
            if ((row = baseFileReader.getValues()) != null) {
                resultInfo = new HashMap<String, String>();
                // resultInfo.putAll(getBaseFileRow());
                for (int i = 0; i < baseFileHeaderSize; i++) {
                    resultInfo.put(baseFileHeader.get(i), row[i]);
                }
                resultInfoList.add(resultInfo);
            }
            // boolean success = Boolean.valueOf(resultInfo.get("Success"));
            // boolean created = Boolean.valueOf(resultInfo.get("Created"));
            // String id = resultInfo.get("Id");
            // String error = resultInfo.get("Error");
            // if (success && created) {
            // System.out.println("Created row with id " + id);
            // } else if (!success) {
            // System.out.println("Failed with error: " + error);
            // }
        }
        closeFileRead();
        return resultInfoList;
    }

    // for TDI-26832
    public void closeFileRead() throws IOException {
        if (baseFileReader != null) {
            baseFileReader.close();
        }
    }
}
