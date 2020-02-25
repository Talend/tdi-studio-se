package org.talend.job.audit;

import java.util.Properties;

import org.talend.logging.audit.Context;

public class JobAuditLoggerTest {

	public static void main(String[] args) {
		Properties props = new Properties();
		String root_logger_name = "audit";
		props.setProperty("root.logger", root_logger_name);
		props.setProperty("encoding", "UTF-8");
		props.setProperty("application.name", "Talend Studio");
		props.setProperty("service.name", "Talend Studio Job");
		props.setProperty("instance.name", "Talend Studio Job Instance");
		props.setProperty("propagate.appender.exceptions", "none");
		props.setProperty("log.appender", "file");
		props.setProperty("appender.file.path", "audit.json");
		props.setProperty("appender.file.maxsize", "52428800");
		props.setProperty("appender.file.maxbackup", "20");
		props.setProperty("host", "false");
		
		org.apache.logging.log4j.core.config.Configurator.setLevel(root_logger_name, org.apache.logging.log4j.Level.DEBUG);
		//org.apache.log4j.Logger.getLogger("audit").setLevel(org.apache.log4j.Level.DEBUG);
		
		final JobAuditLogger logger = JobEventAuditLoggerFactory.createJobAuditLogger(props);
		Context context = JobContextBuilder.create().jobName("fetch_from_s3_every_day").jobId("jobid_123")
				.jobVersion("0.1").connectorType("tXMLMAP").connectorId("tXMLMap_1")
				.connectionName("row1").connectionType("reject").duration("20s")
				.rows(10).timestamp("2019-12-12 23:23:23.111+08:00").status("end").build();

		logger.jobstart(context);
		logger.runcomponent(context);
		logger.flowInput(context);
		logger.flowOutput(context);
		logger.jobstop(context);
	}
}