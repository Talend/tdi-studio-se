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
package org.talend.designer.components.tsort.io.util;

import java.io.Closeable;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * This class holds a set of filenames to be deleted on VM exit through a shutdown hook. A set is used both to prevent
 * double-insertion of the same file as well as offer quick removal.
 */

class DeleteOnExitHook extends Thread {

    // WARNING: there can't add this statement here, because DeleteOnExitHook.hook().add(file.getAbsolutePath(), is);
    // in fact, they are not the same instance.
    // static {
    // Runtime.getRuntime().addShutdownHook(DeleteOnExitHook.hook());
    // }

    private static DeleteOnExitHook instance = null;

    private LinkedHashSet<String> files = new LinkedHashSet<String>();

    private Map<String, Closeable> streams = new HashMap<String, Closeable>();

    static DeleteOnExitHook hook() {
        if (instance == null)
            instance = new DeleteOnExitHook();

        return instance;
    }

    private DeleteOnExitHook() {
    }

    void add(String file, Closeable stream) {
        synchronized (files) {
            if (files == null)
                throw new IllegalStateException("Shutdown in progress");

            files.add(file);
            streams.put(file, stream);
        }
    }

    public void run() {
        LinkedHashSet<String> theFiles;

        synchronized (files) {
            theFiles = files;
            files = null;
        }

        ArrayList<String> toBeDeleted = new ArrayList<String>(theFiles);

        // reverse the list to maintain previous jdk deletion order.
        // Last in first deleted.
        Collections.reverse(toBeDeleted);
        for (String filename : toBeDeleted) {
            // step1, close the related stream.
            Closeable stream = streams.get(filename);
            if (stream != null) {
                try {
                    stream.close();
                } catch (Exception e) {

                }
            }

            // step2, delete the file.
            (new File(filename)).delete();
        }
    }
}
