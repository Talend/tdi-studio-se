package org.talend.job.audit;

import org.talend.logging.audit.Context;
import org.talend.logging.audit.ContextBuilder;

public class JobContextBuilder {

    private final ContextBuilder builder;

    public JobContextBuilder(ContextBuilder builder) {
        this.builder = builder;
    }

    public static JobContextBuilder create() {
        return new JobContextBuilder(ContextBuilder.create());
    }

    public JobContextBuilder jobName(String jobName) {
        builder.with("jobName", jobName);
        return this;
    }
    
    public JobContextBuilder jobVersion(String jobVersion) {
        builder.with("jobVersion", jobVersion);
        return this;
    }
    
    public JobContextBuilder jobId(String jobId) {
        builder.with("jobId", jobId);
        return this;
    }
    
    /**
     * component label, default is unique name like "tXMLMap_1", but user can adjust it in studio to any value
     * @param connectorType
     * @return self
     */
    public JobContextBuilder connectorLabel(String connectorLabel) {
        builder.with("connectorLabel", connectorLabel);
        return this;
    }
    
    /**
     * component type like "tXMLMap"
     * @param connectorType
     * @return self
     */
    public JobContextBuilder connectorType(String connectorType) {
        builder.with("connectorType", connectorType);
        return this;
    }
    
    /**
     * component unique name like "tXMLMap_1"
     * @param connectorType
     * @return self
     */
    public JobContextBuilder connectorId(String connectorId) {
        builder.with("connectorId", connectorId);
        return this;
    }
    
    public JobContextBuilder rows(long rowCount) {
        builder.with("rows", String.valueOf(rowCount));
        return this;
    }
    
    //output or reject
    public JobContextBuilder connectionType(String connectionType) {
        builder.with("connectionType", connectionType);
        return this;
    }
    
    //like "row1"
    public JobContextBuilder connectionName(String connectionName) {
        builder.with("connectionName", connectionName);
        return this;
    }
    
    //100s
    public JobContextBuilder duration(String duration) {
        builder.with("duration", duration);
        return this;
    }
    
    public JobContextBuilder timestamp(String timestamp) {
        builder.with("timestamp", timestamp);
        return this;
    }
    
    public JobContextBuilder status(String status) {
        builder.with("status", status);
        return this;
    }

    public Context build() {
        return builder.build();
    }
    
    /**
     * source connector id
     * @return
     */
    public JobContextBuilder sourceId(String sourceId) {
        builder.with("sourceId", sourceId);
        return this;
    }
    
    /**
     * source connector label
     * @return
     */
    public JobContextBuilder sourceLabel(String sourceLabel) {
        builder.with("sourceLabel", sourceLabel);
        return this;
    }
    
    /**
     * source connector name
     * @return
     */
    public JobContextBuilder sourceConnectorName(String sourceConnectorName) {
        builder.with("sourceConnectorName", sourceConnectorName);
        return this;
    }
    
    /**
     * target connector id
     * @return
     */
    public JobContextBuilder targetId(String targetId) {
        builder.with("targetId", targetId);
        return this;
    }
    
    /**
     * target connector label
     * @return
     */
    public JobContextBuilder targetLabel(String targetLabel) {
        builder.with("targetLabel", targetLabel);
        return this;
    }
    
    /**
     * target connector name
     * @return
     */
    public JobContextBuilder targetConnectorName(String targetConnectorName) {
        builder.with("targetConnectorName", targetConnectorName);
        return this;
    }
    
}
