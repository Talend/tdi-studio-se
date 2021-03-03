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
package org.talend.webhcat.launcher.fs;

import java.io.IOException;
import java.io.InputStream;

import org.apache.hadoop.fs.Path;

public class HadoopFileSystem implements FileSystem {

	protected org.apache.hadoop.fs.FileSystem fs;

	public HadoopFileSystem(org.apache.hadoop.fs.FileSystem fs) {
		this.fs = fs;
	}

	public HadoopFileSystem(org.apache.hadoop.conf.Configuration conf)
			throws IOException {
		this.fs = org.apache.hadoop.fs.FileSystem.get(conf);
	}

	public boolean exists(String file) {
		boolean doesExist = false;
		try {
			doesExist = this.fs.exists(new org.apache.hadoop.fs.Path(file));
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}

		return doesExist;
	}

	public InputStream open(String file) {
		InputStream is = null;
		try {
			is = this.fs.open(new org.apache.hadoop.fs.Path(file));
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}

		return is;
	}

	public void delete(String folder) {
		try {
			this.fs.delete(new Path(folder), true);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public void mkdir(String folder) {
		try {
			this.fs.mkdirs(new Path(folder));
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public void copyFromLocal(String localFile, String targetFolder) {
		try {
			this.fs.copyFromLocalFile(new Path(localFile), new Path(targetFolder));
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public String getRemoteFileSystemRootPath() {
		return "";
	}

	public String getFileSystemPrefix() {
		return org.apache.hadoop.fs.FileSystem.getDefaultUri(this.fs.getConf())
				.toString();
	}
}
