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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.ui.internal.wizards.datatransfer.ILeveledImportStructureProvider;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class DirImportStructureProvider implements ILeveledImportStructureProvider {

    private final File root;

    public DirImportStructureProvider(File folder) {
        this.root = folder;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.internal.wizards.datatransfer.ILeveledImportStructureProvider#getRoot()
     */
    @Override
    public Object getRoot() {
        return root;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.wizards.datatransfer.IImportStructureProvider#getChildren(java.lang.Object)
     */
    @Override
    public List getChildren(Object element) {
        if (element instanceof File) {
            File file = (File) element;
            if (file.isDirectory()) {
                File[] listFiles = file.listFiles();
                if (listFiles != null) {
                    return Arrays.asList(listFiles);
                }
            }
        }
        return Collections.emptyList();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.wizards.datatransfer.IImportStructureProvider#getContents(java.lang.Object)
     */
    @Override
    public InputStream getContents(Object element) {
        if (element instanceof File) {
            File file = (File) element;
            if (file.isFile()) {
                try {
                    return new BufferedInputStream(new FileInputStream(file));
                } catch (FileNotFoundException e) {
                    //
                }
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.wizards.datatransfer.IImportStructureProvider#getFullPath(java.lang.Object)
     */
    @Override
    public String getFullPath(Object element) {
        if (element instanceof File) {
            File file = (File) element;
            return file.toPath().relativize(((File) getRoot()).toPath()).toString();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.wizards.datatransfer.IImportStructureProvider#getLabel(java.lang.Object)
     */
    @Override
    public String getLabel(Object element) {
        if (element instanceof File) {
            File file = (File) element;
            return file.getName();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.wizards.datatransfer.IImportStructureProvider#isFolder(java.lang.Object)
     */
    @Override
    public boolean isFolder(Object element) {
        if (element instanceof File) {
            File file = (File) element;
            return file.isDirectory();
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.internal.wizards.datatransfer.ILeveledImportStructureProvider#setStrip(int)
     */
    @Override
    public void setStrip(int level) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.internal.wizards.datatransfer.ILeveledImportStructureProvider#getStrip()
     */
    @Override
    public int getStrip() {
        // TODO Auto-generated method stub
        return 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.internal.wizards.datatransfer.ILeveledImportStructureProvider#closeArchive()
     */
    @Override
    public boolean closeArchive() {
        return false;
    }

}
