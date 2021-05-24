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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlobDirectory;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;

public class AzureFileSystem implements FileSystem {

	protected CloudBlobContainer container;
	private static CloudStorageAccount connection = null;

	public AzureFileSystem(CloudBlobContainer container) {
		this.container = container;
	}

	public AzureFileSystem(String connectionString, String container) {
		try {
			connection = AzureFileSystem.getConnection(connectionString);
			this.container = connection.createCloudBlobClient()
					.getContainerReference(container);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public static CloudStorageAccount getConnection(String connectionString)
			throws InvalidKeyException, URISyntaxException {
		if (connection == null) {
			connection = CloudStorageAccount.parse(connectionString);
		}
		return connection;
	}

	public void deleteFolder(String folder, CloudBlobContainer container)
			throws StorageException, URISyntaxException {
		for (ListBlobItem blobItem : container.listBlobs(folder)) {
			if (blobItem instanceof CloudBlob) {
				CloudBlob blob = (CloudBlob) blobItem;
				blob.delete();
			} else {
				if (blobItem instanceof CloudBlobDirectory)
					deleteFolder(((CloudBlobDirectory) blobItem).getPrefix(),
							container);
			}
		}
	}

	public boolean exists(String file) {
		boolean doesExist = false;
		try {
			CloudBlockBlob blob = this.container.getBlockBlobReference(file);
			doesExist = blob.exists();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}

		return doesExist;
	}

	public InputStream open(String file) {
		InputStream is = null;
		try {
			CloudBlockBlob blob = this.container.getBlockBlobReference(file);
			is = blob.openInputStream();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}

		return is;
	}

	public void delete(String folder) {
		try {
			deleteFolder(folder, this.container);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public void mkdir(String folder) {
		try {
			InputStream in = new ByteArrayInputStream(new String().getBytes());
			this.container.getBlockBlobReference(folder).upload(in, 0L);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public void copyFromLocal(String localFile, String targetFolder) {
		try {
			File file = new File(localFile);
			CloudBlockBlob remotefile = this.container
					.getBlockBlobReference(targetFolder + "/" + file.getName());
			if (!file.exists())
				throw new FileNotFoundException();
			remotefile.upload(new FileInputStream(file), file.length());
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public String getFileSystemPrefix() {
		return "wasb://" + this.container.getName() + "@" + this.container.getStorageUri().getPrimaryUri().getHost() + "/";
	}
}
