package org.talend.job.audit;

import org.talend.logging.audit.AuditEvent;
import org.talend.logging.audit.Context;
import org.talend.logging.audit.EventAuditLogger;
import org.talend.logging.audit.LogLevel;

public interface JobAuditLogger extends EventAuditLogger {
	
	@AuditEvent(category = "job", message = "Job start : job_name:{jobName}, job_version:{jobVersion}, job_id:{jobId}, timestamp:{timestamp}", level = LogLevel.INFO)
    void jobstart(Context context);
	
	@AuditEvent(category = "job", message = "Job stop : job_name:{jobName}, job_version:{jobVersion}, job_id:{jobId}, timestamp:{timestamp}, status:{status}, duration:{duration}", level = LogLevel.INFO)
    void jobstop(Context context);
	
	@AuditEvent(category = "connector", message = "Component run : job_name:{jobName}, job_version:{jobVersion}, job_id:{jobId}, connector_type:{connectorType}, connector_id:{connectorId}", level = LogLevel.INFO)
    void runcomponent(Context context);

    @AuditEvent(category = "connection", message = "Component {connectorType} {connectionType} {rows} rows in {duration} with {connectionName} line", level = LogLevel.INFO)
    void flowOutput(Context context);
    
    @AuditEvent(category = "connection", message = "Component {connectorType} received {rows} rows in {duration} with {connectionName} line", level = LogLevel.INFO)
    void flowInput(Context context);

}
