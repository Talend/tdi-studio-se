package com.talend.compress.zip;

import java.io.File;
import java.util.List;
import java.util.Optional;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.FileHeader;

public class Unzip {

	public void setNeedPassword(boolean needPassword) {
		this.needPassword = needPassword;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setCheckArchive(boolean checkArchive) {
		this.checkArchive = checkArchive;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public void setExtractPath(boolean extractPath) {
		this.extractPath = extractPath;
	}

	public void setUtil(Util util) {
		this.util = util;
	}

	public void setSourceZip(String sourceZip) {
		this.sourceZip = sourceZip;
	}

	public void setTargetDir(String targetDir) {
		this.targetDir = targetDir;
	}

    public void setUseZip4jDecryption(boolean useZip4jDecryption) {
        this.useZip4jDecryption = useZip4jDecryption;
	}

	public void setEncording(String encording){this.encording = encording;}

	private boolean needPassword = false;
    private boolean useZip4jDecryption = false;

	private String password = null;
	private boolean checkArchive = false;
	private boolean verbose = false;
	private boolean extractPath = true;
	private Util util = null;

	private String sourceZip;
	private String targetDir;
	private String encording;


	public Unzip(String sourceZip, String targetDir) {
		this.sourceZip = sourceZip;
		this.targetDir = targetDir;
	}

	public void doUnzip() throws Exception {
		System.out.println("Processing archive " + sourceZip
				+ ", please wait...");
		System.out.println();

		if (needPassword) {
            if (useZip4jDecryption) {
				doUnzipWithAes();
			} else {
				doUnzipWithDecryption();
			}
		} else {
			doUnzipWithoutDecryption();
		}

		System.out.println();
		System.out.println("Process finished");
	}

	// zip4j compress&decryption impl
	public void doUnzipWithAes() throws Exception {
		File file = new File(sourceZip);

		if (password == null || "".equals(password)) {
			Thread.sleep(1000); // To make sure the System.out.println message
								// come before
			throw new RuntimeException(
					"Please enter the password and try again..");
		}

		ZipFile zipFile = new ZipFile(sourceZip);
		if(encording != null){
            zipFile.setFileNameCharset(encording);
        }

		if (checkArchive) {
			if (!zipFile.isValidZipFile()) {
				throw new RuntimeException("The file " + sourceZip
						+ " is corrupted, process terminated...");
			}
		}

		if (zipFile.isEncrypted()) {
			zipFile.setPassword(password);
		}

		List fileHeaderList = zipFile.getFileHeaders();

		if (fileHeaderList == null) {
			return;
		}

		for (int i = 0; i < fileHeaderList.size(); i++) {
			FileHeader fileHeader = (FileHeader) fileHeaderList.get(i);
			String filename = fileHeader.getFileName();
			if (verbose) {
				System.out.println("Source file  : " + filename);
			}

			if (!extractPath) {
				filename = filename.replaceAll("\\\\", "/");
				filename = filename.substring(filename.lastIndexOf('/') + 1);
			}

			zipFile.extractFile(fileHeader, targetDir, null, filename);
			util.addUnzippedFiles(targetDir, filename);
		}
	}

	// apache common compress&decryption impl
	public void doUnzipWithDecryption() throws Exception {
		File file = new File(sourceZip);

		if (password == null || "".equals(password)) {
			Thread.sleep(1000); // To make sure the System.out.println message
								// come before
			throw new RuntimeException(
					"Please enter the password and try again..");
		}

		if (checkArchive) {
			if (!org.talend.archive.IntegrityUtil.isEncryptedZipValid(file,
					password)) {
				throw new RuntimeException("The file " + sourceZip
						+ " is corrupted, process terminated...");
			}
		}

		java.io.InputStream is = null;
		try {
			is = new java.io.FileInputStream(sourceZip);
			is = new javax.crypto.CipherInputStream(is,
					org.talend.archive.IntegrityUtil.createCipher(
							javax.crypto.Cipher.DECRYPT_MODE, password));
			org.apache.commons.compress.archivers.zip.ZipArchiveInputStream input = new org.apache.commons.compress.archivers.zip.ZipArchiveInputStream(
					new java.io.BufferedInputStream(is),Optional.ofNullable(encording).orElse("UTF8"));
			org.apache.commons.compress.archivers.zip.ZipArchiveEntry entry;

			while ((entry = input.getNextZipEntry()) != null) {
				if (verbose) {
					System.out.println("Source file  : " + entry.getName());
				}
				boolean isDirectory = entry.isDirectory();
				String filename = entry.getName();

				util.output(targetDir, filename, isDirectory, input);
				applyLastModifiedTime(entry, filename);
			}
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}

	// apache common compress impl
	public void doUnzipWithoutDecryption() throws Exception {
		if (checkArchive
				&& !org.talend.archive.IntegrityUtil
						.isZipValid(new java.io.File(sourceZip))) {
			Thread.sleep(1000); // To make the process terminated after the
								// System.out.println
			throw new RuntimeException("The file " + sourceZip
					+ " is corrupted, process terminated...");
		}

		Thread.sleep(1000);

		org.apache.commons.compress.archivers.zip.ZipFile zip = null;
		try {
			zip = new org.apache.commons.compress.archivers.zip.ZipFile(
					sourceZip,Optional.ofNullable(encording).orElse("UTF8"));
			java.util.Enumeration enuFiles = zip.getEntries();
			java.io.InputStream is = null;

			while (enuFiles.hasMoreElements()) {
				org.apache.commons.compress.archivers.zip.ZipArchiveEntry entry = (org.apache.commons.compress.archivers.zip.ZipArchiveEntry) enuFiles
						.nextElement();
				if (verbose) {
					System.out.println("Source file  : " + entry.getName());
				}
				boolean isDirectory = entry.isDirectory();
				if (!isDirectory) {
					// get the input stream
					is = zip.getInputStream(entry);
				}
				String filename = entry.getName();

				util.output(targetDir, filename, isDirectory, is);

				applyLastModifiedTime(entry, filename);

			}
		} finally {
			if (zip != null) {
				zip.close();
			}
		}
	}

	private void applyLastModifiedTime(
			org.apache.commons.compress.archivers.zip.ZipArchiveEntry entry,
			String filename) {
		if (extractPath) {
			java.io.File f = new java.io.File(targetDir + "/" + filename);
			f.setLastModified(entry.getTime());
		} else {
			java.io.File unzippedFile = new java.io.File(targetDir
					+ util.getEntryName(filename));
			unzippedFile.setLastModified(entry.getTime());
		}
	}
}
