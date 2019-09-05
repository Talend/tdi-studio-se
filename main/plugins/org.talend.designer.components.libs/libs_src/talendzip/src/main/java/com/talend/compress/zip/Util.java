package com.talend.compress.zip;

public class Util {
	public java.util.List<UnzippedFile> unzippedFiles = new java.util.ArrayList<UnzippedFile>();

	private boolean extractPath = false;

	public Util(boolean extractPath) {
		this.extractPath = extractPath;
	}

	public void deleteDir(java.io.File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				deleteDir(new java.io.File(dir, children[i]));
			}
		}
		dir.delete();
	}

	public void repairDir(java.io.File dir) {
		java.io.File parentFile = dir.getParentFile();

		if (parentFile != null) {
			if (parentFile.exists() && parentFile.isFile())
				parentFile.delete();

			repairDir(parentFile);
		}
	}

	public void checkDir(java.io.File file) {
		if (file.exists()) {
			if (file.isDirectory())
				deleteDir(file);
		} else {
			repairDir(file);
		}
	}

	public void output(String path, String fileName, java.io.InputStream is)
			throws java.lang.Exception {

		// deal with the path issue and get the outputstream
		java.io.File f = null;
		if (extractPath) {
			f = new java.io.File(path, fileName);
		} else {
			String tempName = fileName.replaceAll("\\\\", "/");
			int m = tempName.lastIndexOf('/');
			String shortName = tempName.substring(m != -1 ? m + 1 : 0);
			f = new java.io.File(path, shortName);
		}

		checkDir(f);

		f.getParentFile().mkdirs();
		f.createNewFile();

		java.io.FileOutputStream fos = null;
		try {
			fos = new java.io.FileOutputStream(f);

			byte[] buffer = new byte[1024];
			for (int len = is.read(buffer, 0, 1024); len != -1; len = is.read(
					buffer, 0, 1024)) {
				fos.write(buffer, 0, len);
			}
		} finally {
			if (fos != null) {
				fos.close();
			}
		}
		unzippedFiles.add(new UnzippedFile(f.getName(), f.getAbsolutePath()));
	}

	public void output(String path, String fileName, boolean isDirectory,
			java.io.InputStream is) throws java.lang.Exception {

		// deal with the path issue and get the outputstream
		java.io.File f = null;
		if (extractPath) {
			f = new java.io.File(path, fileName);
		} else {
			String tempName = fileName.replaceAll("\\\\", "/");
			int m = tempName.lastIndexOf('/');
			String shortName = tempName.substring(m != -1 ? m + 1 : 0);
			f = new java.io.File(path, shortName);
		}

		if (isDirectory) {
			if (!f.exists()) {
				f.mkdirs();
			}
			// /
		} else {
			java.io.File parent = f.getParentFile();
			if (parent != null && !parent.exists()) {
				parent.mkdirs();
			}
			f.createNewFile();

			java.io.BufferedOutputStream bos = null;
			try {
				bos = new java.io.BufferedOutputStream(
						new java.io.FileOutputStream(f));
				byte[] buff = new byte[1024];
				int data = -1;
				while ((data = is.read(buff)) != -1) {
					bos.write(buff, 0, data);
				}
			} finally {
				if (bos != null) {
					bos.close();
				}
			}
		}
		unzippedFiles.add(new UnzippedFile(f.getName(), f.getAbsolutePath()));
	}

	// This function is used for Extract file from the path
	public String getEntryName(String fileEntry) {
		if (fileEntry != null && !fileEntry.equals("")) {
			for (int i = fileEntry.length() - 1; i > 0; i--) {
				if (fileEntry.charAt(i) == '/' || fileEntry.charAt(i) == '\\') {
					fileEntry = fileEntry.substring(i + 1, fileEntry.length());
					break;
				}
			}
		}
		return fileEntry;
	}
	
	// This function is used for extract file with password.
	public void addUnzippedFiles(String path, String fileName) 
			throws java.lang.Exception{
		java.io.File f = new java.io.File(path, fileName);
		unzippedFiles.add(new UnzippedFile(f.getName(), f.getAbsolutePath()));
	}
}