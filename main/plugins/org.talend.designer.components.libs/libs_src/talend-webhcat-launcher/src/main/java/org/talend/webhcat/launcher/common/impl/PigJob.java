// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.webhcat.launcher.common.impl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.StringTokenizer;

import org.talend.webhcat.launcher.common.Job;
import org.talend.webhcat.launcher.fs.FileSystem;
import org.talend.webhcat.launcher.utils.JobType;

import com.microsoft.azure.storage.StorageException;

public class PigJob extends Job {

	private JobType type;

	public PigJob(FileSystem fs, JobType type) {
		this.fileSystem = fs;
		this.type = type;
	}

	public int callWS(String libjars, boolean isAzure) {
		org.apache.cxf.jaxrs.client.JAXRSClientFactoryBean factory = new org.apache.cxf.jaxrs.client.JAXRSClientFactoryBean();
		factory.setUsername(this.username);
		if (isAzure)
			factory.setPassword(this.password);
		factory.setAddress(this.webhcatEndpoint);

		org.apache.cxf.jaxrs.client.WebClient client = factory
				.createWebClient();
		if (type == JobType.PIG) {
			client.path("templeton/v1/pig");
		} else if (type == JobType.HIVE) {
			client.path("templeton/v1/hive");
		}
		client.type("application/x-www-form-urlencoded");
		client.accept("*/*");

		org.apache.cxf.jaxrs.ext.form.Form form = new org.apache.cxf.jaxrs.ext.form.Form();
		form.set("user.name", this.username);
		form.set("file", this.fileSystem.getFileSystemPrefix() + "/"
				+ this.remoteFolder + "/file/" + this.fileToExecute);
		form.set("statusdir", "/" + this.statusFolder);

		client.post(form, String.class);
		return client.getResponse().getStatus();
	}

	public String sendFiles() throws IOException, InvalidKeyException,
			URISyntaxException, StorageException {
		String remote = this.remoteFolder;
		String remotefile = remote + "/file";
		String remotelibjars = remote + "/libjars";
		String status = statusFolder;

		if (this.fileSystem.exists(remote)) {
			this.fileSystem.delete(remote);
		}

		if (this.fileSystem.exists(status)) {
			this.fileSystem.delete(status);
		}

		this.fileSystem.mkdir(remote);
		this.fileSystem.mkdir(remotefile);
		this.fileSystem.mkdir(remotelibjars);

		this.fileSystem.copyFromLocal(this.fileToExecute, remotefile);

		if (this.libjars != null) {
			StringTokenizer tokenizer = new StringTokenizer(this.libjars, ",");
			while (tokenizer.hasMoreElements()) {
				String jar = (String) tokenizer.nextElement();
				this.fileSystem.copyFromLocal(jar, remotelibjars);
			}
		}
		return null;
	}

}
