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
package org.talend.designer.components.lookup.persistent;

/**
 *
 * Memory helper.
 */
public final class MemoryHelper {

    private MemoryHelper() {
        super();
    }

    private static long startValue;
    private static String currentKey;

    private static final Runtime S_RUNTIME = Runtime.getRuntime();

    public static long usedMemory() {
        return S_RUNTIME.totalMemory() - S_RUNTIME.freeMemory();
    }

    public static long freeMemory() {
        return S_RUNTIME.freeMemory();
    }

    public static long maxMemory() {
        return S_RUNTIME.maxMemory();
    }

    public static long totalMemory() {
        return S_RUNTIME.totalMemory();
    }

    public static boolean hasFreeMemory(float margin) {
        return usedMemory() < (1f - margin) * ((float) maxMemory());
    }

    public static void gc() {
        S_RUNTIME.gc();
        // long usedMem1 = usedMemory(), usedMem2 = Long.MAX_VALUE;
        // for (int i = 0; (usedMem1 < usedMem2) && (i < 500); ++i) {
        // S_RUNTIME.runFinalization();
        // S_RUNTIME.gc();
        // Thread.yield();
        // usedMem2 = usedMem1;
        // usedMem1 = usedMemory();
        // System.out.println("totalMemory =" +totalMemory());
        // System.out.println("maxMemory =" +maxMemory());
        // System.out.println("usedMem1 =" +usedMem1);
        // System.out.println("usedMem2 =" +usedMem2);
        // }

    }

    public static void start(String key) {
        currentKey = key;
        gc();
        startValue = usedMemory();
    }

    public static void end(String key) {
        if(key != null && key.equals(currentKey)) {
            gc();
            long usedMemoryBytes = usedMemory() - startValue;
            long usedMemoryKBytes = usedMemoryBytes / 1024;
            long usedMemoryMBytes = usedMemoryKBytes / 1024;
            System.out.println(key + ": usedMemory = " + usedMemoryBytes + " bytes, " + usedMemoryKBytes + "KB, "+  usedMemoryMBytes + "MB"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        } else {
            System.err.println("Keys for memory measure do not match: currentKey=" + currentKey + " != " + key); //$NON-NLS-1$ //$NON-NLS-2$
        }

    }

    public static void displayMemory(String label, long bytes) {
        long memoryKBytes = bytes / 1024;
        long memoryMBytes = memoryKBytes / 1024;
        System.out.println(label + " = " + bytes + " bytes, " + memoryKBytes + "KB, " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + memoryMBytes + "MB"); //$NON-NLS-1$

    }

}
