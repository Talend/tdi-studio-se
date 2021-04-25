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
package org.talend.repository.ui.actions.importproject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.wizards.datatransfer.IImportStructureProvider;
import org.talend.commons.utils.io.FilesUtils;

/**
 * This class provides information regarding the structure and content of specified file system File objects.
 */
public class FilterFileSystemStructureProvider implements IImportStructureProvider {

    /**
     * Holds a singleton instance of this class.
     */
    public final static FilterFileSystemStructureProvider INSTANCE = new FilterFileSystemStructureProvider();

    /**
     * Creates an instance of <code>FileSystemStructureProvider</code>.
     */
    private FilterFileSystemStructureProvider() {
        super();
    }

    /*
     * (non-Javadoc) Method declared on IImportStructureProvider
     */
    public List getChildren(Object element) {
        File folder = (File) element;

        File[] files = folder.listFiles(new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return !FilesUtils.isSVNFolder(name);
            }
        });
        int childrenLength = files == null ? 0 : files.length;
        List<File> result = new ArrayList(childrenLength);
        for (int i = 0; i < childrenLength; i++) {
            result.add(files[i]);
        }

        return result;
    }

    /*
     * (non-Javadoc) Method declared on IImportStructureProvider
     */
    public InputStream getContents(Object element) {
        try {
            return new FileInputStream((File) element);
        } catch (FileNotFoundException e) {
            IDEWorkbenchPlugin.log(e.getLocalizedMessage(), e);
            return null;
        }
    }

    /*
     * (non-Javadoc) Method declared on IImportStructureProvider
     */
    public String getFullPath(Object element) {
        return ((File) element).getPath();
    }

    /*
     * (non-Javadoc) Method declared on IImportStructureProvider
     */
    public String getLabel(Object element) {

        // Get the name - if it is empty then return the path as it is a file root
        File file = (File) element;
        String name = file.getName();
        if (name.length() == 0) {
            return file.getPath();
        }
        return name;
    }

    /*
     * (non-Javadoc) Method declared on IImportStructureProvider
     */
    public boolean isFolder(Object element) {
        return ((File) element).isDirectory();
    }
}
