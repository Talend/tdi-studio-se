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
package org.talend.webhcat.launcher.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

import org.talend.webhcat.launcher.fs.FileSystem;
import org.talend.webhcat.launcher.utils.Command;

import com.microsoft.azure.storage.StorageException;

public abstract class Job {

	protected FileSystem fileSystem;

	protected String statusFolder;
	protected String remoteFolder;

	protected String webhcatEndpoint;
	protected String username;
	protected String password;

	protected String fileToExecute;
	protected String jarToExecute;
	protected String classToExecute;
	protected String libjars;

	protected String azureContainer;
	protected String azureStorageAddress;
	protected String azureAccountPassword;
	protected String azureAccountName;

	protected String remoteFileToExecute;

	protected String namenode;

	public void parseArg(String[] args) throws Exception {
		int argsLength = args.length;
		for (int i = 0; i < argsLength; i++) {
			if (Command.LIBJARS.equals(args[i])) {
				if (i++ < argsLength) {
					this.setLibjars(args[i]);
				}
				continue;
			}
			if (Command.USERNAME.equals(args[i])) {
				if (i++ < argsLength) {
					this.setUsername(args[i]);
				}
				continue;
			}
			if (Command.NAMENODE.equals(args[i])) {
				if (i++ < argsLength) {
					this.setNameNode(args[i]);
				}
				continue;
			}
			if (Command.STATUSFOLDER.equals(args[i])) {
				if (i++ < argsLength) {
					this.setStatusFolder(args[i]);
				}
				continue;
			}
			if (Command.REMOTEFOLDER.equals(args[i])) {
				if (i++ < argsLength) {
					this.setRemoteFolder(args[i]);
				}
				continue;
			}
			if (Command.FILE.equals(args[i])) {
				if (i++ < argsLength) {
					this.setFileToExecute(args[i]);
				}
				continue;
			}
			if (Command.JAR.equals(args[i])) {
				if (i++ < argsLength) {
					this.setJarToExecute(args[i]);
				}
				continue;
			}
			if (Command.CLASS.equals(args[i])) {
				if (i++ < argsLength) {
					this.setClassToExecute(args[i]);
				}
				continue;
			}
			if (Command.ENDPOINT.equals(args[i])) {
				if (i++ < argsLength) {
					this.webhcatEndpoint=args[i];
				}
				continue;
			}
			if (Command.AZURE_STORAGE_ADDRESS.equals(args[i])) {
				if (i++ < argsLength)
					this.setAzureStorageAddress(args[i]);
				continue;
			}
			if (Command.AZURE_STORAGE_CONTAINER.equals(args[i])) {
				if (i++ < argsLength)
					this.setAzureContainer(args[i]);
				continue;
			}
			if (Command.AZURE_STORAGE_NAME.equals(args[i])) {
				if (i++ < argsLength)
					this.setAzureAccountName(args[i]);
				continue;
			}
			if (Command.AZURE_STORAGE_PASSWORD.equals(args[i])) {
				if (i++ < argsLength)
					this.setAzureAccountPassword(args[i]);
				continue;
			}
			if (Command.HDINSIGHT_PASSWORD.equals(args[i])) {
				if (i++ < argsLength)
					this.setHdInsightPassword(args[i]);
				continue;
			}
			if (Command.HDINSIGHT_USERNAME.equals(args[i])) {
				if (i++ < argsLength)
					this.setHdInsightUsername(args[i]);
				continue;
			}
		}
	};

	public abstract String sendFiles() throws IOException, InvalidKeyException,
			URISyntaxException, StorageException;

	public abstract int callWS(String libjars, boolean requireAuthentication);

	public Integer execute() throws IOException, InterruptedException,
			InvalidKeyException, URISyntaxException, StorageException {

		String status = this.statusFolder;
		String exitCode = status + "/exit";
		final String stderr = status + "/stderr";

		final FileSystem fs = this.fileSystem;

		class StreamStrErr implements Runnable {

			public void run() {
				InputStream is = null;
				BufferedReader d = null;
				try {
					while (!fs.exists(stderr)) {
						Thread.sleep(2000);
					}
					is = fs.open(stderr);
					d = new BufferedReader(new InputStreamReader(is));

					String s;
					while ((s = d.readLine()) == null) {
						if (is != null) {
							is.close();
						}
						if (d != null) {
							d.close();
						}

						is = fs.open(stderr);
						d = new BufferedReader(new InputStreamReader(is));
					}
					do {
						System.err.println(s);
					} while ((s = d.readLine()) != null);

				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (is != null) {
						try {
							is.close();
						} catch (IOException e) {
						}
					}
					if (d != null) {
						try {
							d.close();
						} catch (IOException e) {
						}
					}
				}
			}
		}

		StreamStrErr stream = new StreamStrErr();
		stream.run();

		while (!fs.exists(exitCode)) {
			Thread.sleep(2000);
		}
		InputStream is = fs.open(exitCode);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String r = br.readLine();
		try {
			return Integer.parseInt(r);
		} catch (Exception e) {
			return 1;
		}
	}

	public InputStream getStdOut() throws InvalidKeyException,
			URISyntaxException, StorageException {
		String stdout = this.statusFolder + "/stdout";

		if (!this.fileSystem.exists(stdout))
			return null;

		return this.fileSystem.open(stdout);

	}

	public FileSystem getFileSystem() {
		return this.fileSystem;
	}

	public void setNameNode(String namenode) {
		this.namenode = namenode;
	}

	public void setStatusFolder(String folder) {
		this.statusFolder = folder;
	}

	public void setJarToExecute(String jar) {
		this.jarToExecute = jar;
	}

	public void setRemoteFolder(String folder) {
		this.remoteFolder = folder;
	}

	public void setClassToExecute(String clazz) {
		this.classToExecute = clazz;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setLibjars(String libjars) {
		this.libjars = libjars;
	}

	public void setFileToExecute(String file) {
		this.fileToExecute = file;
	}

	public void setAzureAccountName(String name) {
		this.azureAccountName = name;
	}

	public void setAzureAccountPassword(String password) {
		this.azureAccountPassword = password;
	}

	public void setAzureStorageAddress(String address) {
		this.azureStorageAddress = address;
	}

	public void setAzureContainer(String container) {
		this.azureContainer = container;
	}

	public void setHdInsightUsername(String username) {
		this.username = username;
	}

	public void setHdInsightPassword(String password) {
		this.password = password;
	}

	public void setLibJars(String libjars) {
		this.libjars = libjars;
	}

	public void setWebhcatEndpoint(String scheme, String endpoint) {
		this.webhcatEndpoint = scheme + "://" +  endpoint;
	}

	public String getStatusFolder() {
		return this.statusFolder;
	}

	public String getRemoteFolder() {
		return this.remoteFolder;
	}

	public String getAzureAccountName() {
		return this.azureAccountName;
	}

	public String getAzureAccountPassword() {
		return this.azureAccountPassword;
	}

	public String getAzureAccountContainer() {
		return this.azureContainer;
	}
}
