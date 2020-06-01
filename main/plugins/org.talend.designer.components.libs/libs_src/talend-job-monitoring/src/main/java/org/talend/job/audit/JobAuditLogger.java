package org.talend.job.audit;

import org.talend.logging.audit.AuditEvent;
import org.talend.logging.audit.Context;
import org.talend.logging.audit.EventAuditLogger;
import org.talend.logging.audit.LogLevel;

public interface JobAuditLogger extends EventAuditLogger {

	@AuditEvent(category = "jobStart", message = "Job start : job_name:{jobName}, job_version:{jobVersion}, job_id:{jobId}, timestamp:{timestamp}", level = LogLevel.INFO)
	void jobstart(Context context);

	@AuditEvent(category = "jobStop", message = "Job stop : job_name:{jobName}, job_version:{jobVersion}, job_id:{jobId}, timestamp:{timestamp}, status:{status}, duration:{duration}", level = LogLevel.INFO)
	void jobstop(Context context);

	@AuditEvent(category = "runComponent", message = "Component run : job_name:{jobName}, job_version:{jobVersion}, job_id:{jobId}, connector_type:{connectorType}, connector_id:{connectorId}, connector_label:{connectorLabel}", level = LogLevel.INFO)
	void runcomponent(Context context);

	@AuditEvent(category = "flowOutput", message = "Component {connectorType} {connectorLabel} {connectionType} {rows} rows in {duration} with {connectionName} line", level = LogLevel.INFO)
	void flowOutput(Context context);

	@AuditEvent(category = "flowInput", message = "Component {connectorType} {connectorLabel} received {rows} rows in {duration} with {connectionName} line", level = LogLevel.INFO)
	void flowInput(Context context);

	@AuditEvent(category = "flowExecution", message = "connection : {connectionName}, row : {rows}, cost : {duration}", level = LogLevel.INFO)
	void flowExecution(Context context);

}
