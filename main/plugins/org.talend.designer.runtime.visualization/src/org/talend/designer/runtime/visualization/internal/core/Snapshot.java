/*******************************************************************************
 * Copyright (c) 2010 JVM Monitor project. All rights reserved.
 * 
 * This code is distributed under the terms of the Eclipse Public License v1.0 which is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.talend.designer.runtime.visualization.internal.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.util.NLS;
import org.talend.designer.runtime.visualization.Activator;
import org.talend.designer.runtime.visualization.IJvm;
import org.talend.designer.runtime.visualization.ISnapshot;
import org.talend.designer.runtime.visualization.JvmCoreException;
import org.talend.utils.xml.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * The snapshot.
 */
public class Snapshot implements ISnapshot {

    /** The snapshot type. */
    private SnapshotType snapshotType;

    /** The JVM. */
    private IJvm jvm;

    /** The file store. */
    private IFileStore fileStore;

    /** The time stamp. */
    private String timeStamp;

    /**
     * The constructor.
     * 
     * @param fileStore The file store
     * @param jvm The JVM
     */
    public Snapshot(IFileStore fileStore, IJvm jvm) {
        this.fileStore = fileStore;
        this.jvm = jvm;
        parseFileName(fileStore.getName());
    }

    /*
     * @see ISnapshot#getFileStore()
     */
    @Override
    public IFileStore getFileStore() {
        return fileStore;
    }

    /*
     * @see ISnapshot#getJvm()
     */
    @Override
    public IJvm getJvm() {
        return jvm;
    }

    /*
     * @see ISnapshot#getTimeStamp()
     */
    @Override
    public String getTimeStamp() {
        if (timeStamp != null) {
            return timeStamp;
        }

        if (snapshotType == SnapshotType.Hprof) {
            long lastModified = fileStore.fetchInfo().getLastModified();
            if (lastModified == EFS.NONE) {
                return null;
            }
            Date currentDate = new Date(lastModified);
            String date = new SimpleDateFormat("yyyy/MM/dd").format(currentDate); //$NON-NLS-1$
            String time = new SimpleDateFormat("HH:mm:ss").format(currentDate); //$NON-NLS-1$
            timeStamp = date + " " + time; //$NON-NLS-1$
            return timeStamp;
        }

        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(fileStore.toURI().getPath()));

            Document document = XmlUtils.getSecureDocumentBuilderFactory().newDocumentBuilder().parse(inputStream);

            Element root = document.getDocumentElement();
            timeStamp = root.getAttribute("date"); //$NON-NLS-1$

            return timeStamp;
        } catch (SAXException e) {
            Activator.log(IStatus.ERROR, NLS.bind(Messages.readFileFailedMsg, fileStore.toURI().getPath()), e);
        } catch (IOException e) {
            Activator.log(IStatus.ERROR, NLS.bind(Messages.readFileFailedMsg, fileStore.toURI().getPath()), e);
        } catch (ParserConfigurationException e) {
            Activator.log(IStatus.ERROR, NLS.bind(Messages.readFileFailedMsg, fileStore.toURI().getPath()), e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        return null;
    }

    /*
     * @see ISnapshot#getType()
     */
    @Override
    public SnapshotType getType() {
        return snapshotType;
    }

    /*
     * @see ISnapshot#rename(String)
     */
    @Override
    public void rename(String newName) throws JvmCoreException {
        File file = new File(fileStore.toURI());
        String newFilePath = fileStore.getParent().toURI().getPath() + File.separator + newName;
        File destFile = new File(newFilePath);

        if (file.renameTo(destFile)) {
            if (jvm instanceof AbstractJvm) {
                ((AbstractJvm) jvm).refreshSnapshots();
            }
        } else {
            throw new JvmCoreException(IStatus.ERROR, NLS.bind(Messages.renameFileFailedMsg, fileStore.toURI().getPath()), null);
        }
    }

    /*
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return fileStore.getName();
    }

    /**
     * Checks if the given string is valid snapshot file name.
     * 
     * @param fileName The file name
     * @return True if the given string is valid snapshot file name
     */
    public static boolean isValidFile(String fileName) {
        String[] elements = fileName.split("\\."); //$NON-NLS-1$
        if (elements == null || elements.length != 2) {
            return false;
        }

        // check the file prefix
        for (SnapshotType type : SnapshotType.values()) {
            if (elements[1].equals(type.getExtension())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Parses the given file name.
     * 
     * @param fileName The file name
     */
    private void parseFileName(String fileName) {
        for (SnapshotType type : SnapshotType.values()) {
            if (fileName.endsWith(type.getExtension())) {
                snapshotType = type;
            }
        }
    }
}
