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

	public JobContextBuilder jobName(String job_name) {
		builder.with("job_name", job_name);
		return this;
	}

	public JobContextBuilder jobVersion(String job_version) {
		builder.with("job_version", job_version);
		return this;
	}

	public JobContextBuilder jobId(String job_id) {
		builder.with("job_id", job_id);
		return this;
	}

	/**
	 * component label, default is unique name like "tXMLMap_1", but user can adjust
	 * it in studio to any value
	 * 
	 * @param connectorType
	 * @return self
	 */
	public JobContextBuilder connectorLabel(String connector_label) {
		builder.with("connector_label", connector_label);
		return this;
	}

	/**
	 * component type like "tXMLMap"
	 * 
	 * @param connectorType
	 * @return self
	 */
	public JobContextBuilder connectorType(String connector_type) {
		builder.with("connector_type", connector_type);
		return this;
	}

	/**
	 * component unique name like "tXMLMap_1"
	 * 
	 * @param connectorType
	 * @return self
	 */
	public JobContextBuilder connectorId(String connector_id) {
		builder.with("connector_id", connector_id);
		return this;
	}

	public JobContextBuilder rows(long rows) {
		builder.with("rows", String.valueOf(rows));
		return this;
	}

	// output or reject
	public JobContextBuilder connectionType(String connection_type) {
		builder.with("connection_type", connection_type);
		return this;
	}

	// like "row1"
	public JobContextBuilder connectionName(String connection_name) {
		builder.with("connection_name", connection_name);
		return this;
	}

	// 100s
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
	 * 
	 * @return
	 */
	public JobContextBuilder sourceId(String source_id) {
		builder.with("source_id", source_id);
		return this;
	}

	/**
	 * source connector label
	 * 
	 * @return
	 */
	public JobContextBuilder sourceLabel(String source_label) {
		builder.with("source_label", source_label);
		return this;
	}

	/**
	 * source connector name
	 * 
	 * @return
	 */
	public JobContextBuilder sourceConnectorType(String source_connector_type) {
		builder.with("source_connector_type", source_connector_type);
		return this;
	}

	/**
	 * target connector id
	 * 
	 * @return
	 */
	public JobContextBuilder targetId(String target_id) {
		builder.with("target_id", target_id);
		return this;
	}

	/**
	 * target connector label
	 * 
	 * @return
	 */
	public JobContextBuilder targetLabel(String target_label) {
		builder.with("target_label", target_label);
		return this;
	}

	/**
	 * target connector name
	 * 
	 * @return
	 */
	public JobContextBuilder targetConnectorType(String target_connector_type) {
		builder.with("target_connector_type", target_connector_type);
		return this;
	}

	public JobContextBuilder connectorParameters(String connector_parameters) {
        builder.with("connector_parameters", connector_parameters);
        return this;
    }
    
    public JobContextBuilder schema(String schema) {
        builder.with("schema", schema);
        return this;
	}
	
}
