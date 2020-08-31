package org.talend.job.audit;

import org.talend.logging.audit.AuditEvent;
import org.talend.logging.audit.Context;
import org.talend.logging.audit.EventAuditLogger;
import org.talend.logging.audit.LogLevel;

public interface JobAuditLogger extends EventAuditLogger {

	@AuditEvent(category = "jobstart", message = "Job start : job_name:{job_name}, job_version:{job_version}, job_id:{job_id}, timestamp:{timestamp}", level = LogLevel.INFO)
	void jobstart(Context context);

	@AuditEvent(category = "jobstop", message = "Job stop : job_name:{job_name}, job_version:{job_version}, job_id:{job_id}, timestamp:{timestamp}, status:{status}, duration:{duration}", level = LogLevel.INFO)
	void jobstop(Context context);

	@AuditEvent(category = "runcomponent", message = "Component run : job_name:{job_name}, job_version:{job_version}, job_id:{job_id}, connector_type:{connector_type}, connector_id:{connector_id}, connector_label:{connector_label}", level = LogLevel.INFO)
	void runcomponent(Context context);

	@AuditEvent(category = "flowoutput", message = "Component {connector_type} {connector_label} {connection_type} {rows} rows in {duration} with {connection_name} line", level = LogLevel.INFO)
	void flowOutput(Context context);

	@AuditEvent(category = "flowinput", message = "Component {connector_type} {connector_label} received {rows} rows in {duration} with {connection_name} line", level = LogLevel.INFO)
	void flowInput(Context context);

	@AuditEvent(category = "flowexecution", message = "connection : {connection_name}, row : {rows}, cost : {duration}", level = LogLevel.INFO)
	void flowExecution(Context context);
	
	@AuditEvent(category = "componentparameters", message = "Component {connector_id} parameters", level = LogLevel.INFO)
    void componentParameters(Context context);
    
    @AuditEvent(category = "schema", message = "{connection_name} : {schema} from {source_id} to {target_id}", level = LogLevel.INFO)
    void schema(Context context);

}
