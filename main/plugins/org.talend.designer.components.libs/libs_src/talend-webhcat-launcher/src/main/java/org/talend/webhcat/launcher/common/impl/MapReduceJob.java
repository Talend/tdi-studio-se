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

import org.apache.hadoop.conf.Configuration;
import org.talend.webhcat.launcher.common.Job;
import org.talend.webhcat.launcher.fs.FileSystem;
import org.talend.webhcat.launcher.fs.HadoopFileSystem;

import com.microsoft.azure.storage.StorageException;

public class MapReduceJob extends Job {

	public MapReduceJob(FileSystem fs) {
		this.fileSystem = fs;
	}

	public int callWS(String libjars, boolean isAzure) {
		org.apache.cxf.jaxrs.client.JAXRSClientFactoryBean factory = new org.apache.cxf.jaxrs.client.JAXRSClientFactoryBean();
		factory.setUsername(this.username);
		if (isAzure)
			factory.setPassword(this.password);
		factory.setAddress(this.webhcatEndpoint);

		org.apache.cxf.jaxrs.client.WebClient client = factory
				.createWebClient();
		client.path("templeton/v1/mapreduce/jar");
		client.type("application/x-www-form-urlencoded");
		client.accept("*/*");

		org.apache.cxf.jaxrs.ext.form.Form form = new org.apache.cxf.jaxrs.ext.form.Form();
		form.set("user.name", this.username);
		form.set("jar", this.fileSystem.getFileSystemPrefix() + "/"
				+ this.remoteFolder + "/jar/" + this.jarToExecute);
		form.set("class", this.classToExecute);
		form.set("statusdir", "/" + this.statusFolder);
		form.set("libjars", libjars);
		form.set("arg", "-calledByTempleton");

		client.post(form, String.class);
		return client.getResponse().getStatus();
	}

	public String sendFiles() throws IOException, InvalidKeyException,
			URISyntaxException, StorageException {
		String remote = this.remoteFolder;
		String remotejar = remote + "/jar";
		String remotelibjars = remote + "/libjars";
		String status = statusFolder;

		if (this.fileSystem.exists(remote)) {
			this.fileSystem.delete(remote);
		}

		if (this.fileSystem.exists(status)) {
			this.fileSystem.delete(status);
		}

		this.fileSystem.mkdir(remote);
		this.fileSystem.mkdir(remotejar);
		this.fileSystem.mkdir(remotelibjars);

		this.fileSystem.copyFromLocal(this.jarToExecute, remotejar);

		StringBuilder libjars = new StringBuilder();
		StringTokenizer tokenizer = new StringTokenizer(this.libjars, ",");
		boolean firstElement = true;
		while (tokenizer.hasMoreElements()) {
			String jar = (String) tokenizer.nextElement();
			this.fileSystem.copyFromLocal(jar, remotelibjars);

			if (!firstElement)
				libjars.append(",");
			firstElement = false;
			libjars.append(this.fileSystem.getFileSystemPrefix() + "/" + this.remoteFolder + "/libjars/"
					+ new java.io.File(jar).getName());
		}
		return libjars.toString();
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		conf.set("fs.default.name", "hdfs://hdp21:8020/");
		FileSystem fs = new HadoopFileSystem(conf);
		Job J = new MapReduceJob(fs);
		J.parseArg(args);
		J.callWS(J.sendFiles(), false);
		J.execute();
	}
}
