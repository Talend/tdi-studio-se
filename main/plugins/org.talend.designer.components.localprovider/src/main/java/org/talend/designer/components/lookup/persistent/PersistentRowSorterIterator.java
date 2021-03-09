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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.jboss.serial.io.JBossObjectInputStream;
import org.jboss.serial.io.JBossObjectOutputStream;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.designer.components.persistent.utils.FileUtils;

import routines.system.IPersistableRow;

/**
 *
 * <code>PersistentRowSorterIterator</code>. Allow to serialize objects sequentially and be able to iterate on them.
 *
 * JBoss library is used to avoid memory leaks noticed with Sun ObjectInputStream class.
 *
 * Warning: JBossObjectInputStream may not deserialize any objects such as for example java.io.File, you could encounter
 * the following error:
 *
 * <pre>
 * Caused by: java.lang.reflect.InvocationTargetException
 *     at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
 *     at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
 *     at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
 *     at java.lang.reflect.Method.invoke(Method.java:597)
 *     at org.jboss.serial.persister.RegularObjectPersister.readSlotWithMethod(RegularObjectPersister.java:103)
 *     ... 32 more
 * Caused by: java.io.EOFException
 *     at java.io.DataInputStream.readFully(DataInputStream.java:180)
 *     at java.io.DataInputStream.readLong(DataInputStream.java:399)
 *     at org.jboss.serial.util.StringUtil.readString(StringUtil.java:212)
 *     at org.jboss.serial.objectmetamodel.DataContainer$DataContainerDirectInput.readUTF(DataContainer.java:757)
 *     at org.jboss.serial.persister.ObjectInputStreamProxy.readUTF(ObjectInputStreamProxy.java:196)
 *     at org.jboss.serial.objectmetamodel.FieldsContainer.readField(FieldsContainer.java:147)
 *     at org.jboss.serial.objectmetamodel.FieldsContainer.readMyself(FieldsContainer.java:218)
 *     at org.jboss.serial.persister.ObjectInputStreamProxy.readFields(ObjectInputStreamProxy.java:224)
 *     at java.io.File.readObject(File.java:1927)
 *     ... 37 more
 *</pre>
 *
 * @see http://www.talendforge.org/bugs/view.php?id=6780#bugnotes
 *
 * @param <V> object value to sort
 */
public abstract class PersistentRowSorterIterator<V extends IPersistableRow> implements IPersistentRowManager<V>, Iterator<V> {

    private static final int INIT_BUFFER_INDEX = -1;

    /** This is 0 when using Sun stream library and 1 when using JBoss implementation */
    private static final int JBOSS_EOF = 1;

    private static final float MARGIN_MAX = 0.35f;

    int[] bwPositionArray = null;

    boolean readonly;

    RandomAccessFile[] raArray = null;

    Object[] lastRetrievedObjectArray = null;

    long[] lastRetrievedCursorPositionArray = null;

    int countUniqueGet;

    private int bufferSize = 10000000;

    private int bufferBeanIndex = INIT_BUFFER_INDEX;

    private V[] buffer;

    IPersistableRow<V> persistableRow = null;// Change this based on the Bean class;

    private int beansCount;

    private StreamContainer[] scArray;

    private ArrayList<File> files = new ArrayList<File>();

    private String container;

    private int count = 0;

    private boolean someFileStillHasRows = false;

    private V[] datas;

    private V currentObject;

    private boolean isFirstNext = true;

    private int bufferMarkLimit = -1;

    private boolean bufferIsMarked;

    private boolean firstUnsifficientMemory = true;

    private boolean waitingHeapException;

    private boolean nextFreeRowCalled;

    private boolean sortEnabled = true;

    /**
     *
     * StreamContainer.
     */
    static class StreamContainer {

        private ObjectInputStream objectInputStream;

        private BufferedInputStream bufferedInputStream;

        public StreamContainer(ObjectInputStream ois, BufferedInputStream bis) {
            super();
            this.objectInputStream = ois;
            this.bufferedInputStream = bis;
        }

        public void close() {
            try {
                this.objectInputStream.close();
            } catch (IOException e) {
                // e.printStackTrace();
                ExceptionHandler.process(e);
            }
            try {
                this.bufferedInputStream.close();
            } catch (IOException e) {
                // e.printStackTrace();
                ExceptionHandler.process(e);
            }
        }
    }

    /**
     * DOC amaumont SortedMultipleHashFile constructor comment.
     *
     * @throws IOException
     */
    public PersistentRowSorterIterator(String container) throws IOException {
        super();
        this.container = container;
        FileUtils.createParentFolderIfNotExists(this.container);
    }

    /**
     * DOC amaumont SortedMultipleHashFile constructor comment.
     *
     * @throws IOException
     */
    public PersistentRowSorterIterator(String container, int bufferSize) throws IOException {
        this(container);
        this.bufferSize = bufferSize;
    }

    protected abstract V createRowInstance();

    // ///////////////////////

    @SuppressWarnings("unchecked")
    public void initPut() {
        // System.out.println("bufferSize="+bufferSize +" objects");
        buffer = (V[]) new IPersistableRow[bufferSize];
    }

    public void put(V bean) throws IOException {

        if (!MemoryHelper.hasFreeMemory(MARGIN_MAX)) {
            if (!bufferIsMarked) {
                if (firstUnsifficientMemory) {
                    firstUnsifficientMemory = false;
                    MemoryHelper.gc();
                    if (bufferBeanIndex == 0) {
                        waitingHeapException = true;
                    }
                }
                if (!waitingHeapException && !MemoryHelper.hasFreeMemory(MARGIN_MAX)) {
                    float v10P = ((float) bufferSize) * 0.1f;
                    if ((float) bufferBeanIndex >= v10P) {
                        bufferMarkLimit = bufferBeanIndex;
                    } else {
                        bufferMarkLimit = (int) v10P;
                    }
                    System.out
                            .println("Warning: to avoid a Memory heap space error the buffer of the flow has been limited to a size of " + bufferMarkLimit + " , try to reduce the advanced parameter \"Max buffer size\" (~100000 or at least less than " + bufferMarkLimit + "), then if needed try to increase the JVM Xmx parameter."); //$NON-NLS-1$
                    bufferIsMarked = true;
                }
            }
        }

        if (nextFreeRowCalled) {
            nextFreeRowCalled = false;
        } else {
            bufferBeanIndex++;
        }

        buffer[bufferBeanIndex] = bean;

        if (bufferBeanIndex + 1 == bufferSize || bufferIsMarked && bufferBeanIndex == bufferMarkLimit) {// buffer is
            /* full do sort and write. */
            writeBuffer();
            if (!bufferIsMarked) {
                bufferMarkLimit = bufferBeanIndex;
                // System.out.println("Buffer marked at index (2-Row) " + bufferMarkLimit);
                bufferIsMarked = true;
            }
            bufferBeanIndex = INIT_BUFFER_INDEX;
        }

        someFileStillHasRows = true;

        beansCount++;
    }

    public void endPut() throws IOException {
        if (bufferBeanIndex > INIT_BUFFER_INDEX) {
            writeBuffer();
        }
        buffer = null;
    }

    /**
     * sort list and then use light serialization to store Data.
     *
     * @param list
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void writeBuffer() throws IOException {
        // long time1 = System.currentTimeMillis();
        // System.out.println("Sorting buffer...");

        if (this.sortEnabled) {
            Arrays.sort(buffer, 0, bufferBeanIndex + 1);
        }

        // long time2 = System.currentTimeMillis();
        // long deltaTimeSort = (time2 - time1);
        // int length = bufferBeanIndex + 1;
        // int itemsPerSecSort = (int) ((float) length / (float) deltaTimeSort * 1000f);
        // System.out.println(deltaTimeSort + " milliseconds for " + length + " objects to sort in memory. " +
        // itemsPerSecSort
        // + " items/s ");

        // time1 = System.currentTimeMillis();
        // System.out.println("Writing ordered buffer in file...");

        File file = new File(buildFilePath());

        file.deleteOnExit();

        count++;
        // ObjectOutputStream rw = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
        ObjectOutputStream rw = new JBossObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
        // System.out.println("Start write buffer ");
        for (int i = 0; i < bufferBeanIndex + 1; i++) {
            buffer[i].writeData(rw);
            // System.out.println(buffer[i]);
        }
        rw.close();
        files.add(file);

        // time2 = System.currentTimeMillis();
        // long deltaTimeWrite = (time2 - time1);
        // int itemsPerSecWrite = (int) ((float) length / (float) deltaTimeWrite * 1000f);
        // System.out.println(deltaTimeWrite + " milliseconds for " + length + " objects to write in file. " +
        // itemsPerSecWrite
        // + " items/s ");

    }

    private String buildFilePath() {
        return container + "_TEMP_" + count + ".bin"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void initGet() {
        bufferBeanIndex = INIT_BUFFER_INDEX;
    }

    public boolean hasNext() {
        return someFileStillHasRows;
    }

    public V next() {

        try {
            if (isFirstNext) {
                beforeLoopFind();
                isFirstNext = false;
            }
            findNextData();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        V objectToReturn = currentObject;

        currentObject = null;

        return objectToReturn;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * DOC amaumont Comment method "beforeLoopFind".
     *
     * @throws IOException
     *
     * @throws IOException
     */
    private void beforeLoopFind() throws IOException {
        int numFiles = files.size();
        List<V> datasList = new ArrayList<V>();
        List<StreamContainer> scList = new ArrayList<StreamContainer>();

        boolean someFileStillHasRows = false;

        bufferBeanIndex = INIT_BUFFER_INDEX;

        for (int i = 0; i < numFiles; i++) {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(files.get(i)));
            // ObjectInputStream ois = new ObjectInputStream(bufferedInputStream);
            ObjectInputStream ois = new JBossObjectInputStream(bufferedInputStream);
            scList.add(new StreamContainer(ois, bufferedInputStream));
            V bean = createRowInstance();
            bean.readData(ois);
            if (!someFileStillHasRows) {
                someFileStillHasRows = true;
            }
            datasList.add(bean);
        }

        int size = datasList.size();
        datas = (V[]) datasList.toArray(new IPersistableRow[size]);
        scArray = (StreamContainer[]) scList.toArray(new StreamContainer[size]);
    }

    /**
     * DOC amaumont Comment method "findNextData".
     *
     * @throws IOException
     *
     * @throws IOException
     */
    private void findNextData() throws IOException {
        V min = null;
        int minIndex = 0;

        if (datas.length == 0) {
            currentObject = null;
            return;
        }

        V bean = datas[0];

        if (bean != null) {
            min = bean;
            minIndex = 0;
        } else {
            min = null;
            minIndex = -1;
        }

        // check which one is min
        for (int i = 1; i < datas.length; i++) {
            bean = datas[i];

            if (min != null) {
                if (bean != null && ((Comparable) (bean)).compareTo(min) < 0) {
                    minIndex = i;
                    min = bean;
                }
            } else {
                if (bean != null) {
                    min = bean;
                    minIndex = i;
                }
            }
        }

        if (minIndex < 0) {
            someFileStillHasRows = false;
        } else {

            currentObject = min;

            // get another data from the file
            StreamContainer sc = scArray[minIndex];
            if (sc.objectInputStream.available() > JBOSS_EOF || sc.bufferedInputStream.available() > 0) {
                bean = createRowInstance();
                bean.readData(sc.objectInputStream);
                datas[minIndex] = bean;
            } else {
                datas[minIndex] = null;
            }

            // check if one still has data
            someFileStillHasRows = false;
            for (int i = 0; i < datas.length; i++) {
                if (datas[i] != null) {
                    someFileStillHasRows = true;
                    break;
                }
            }
        }
    }

    /**
     * DOC amaumont Comment method "afterLoopFind".
     *
     * @throws IOException
     */
    private void afterLoopFind() {
        // close all the streams
        // dos.close();

        buffer = null;

        if (scArray != null) {
            for (int i = 0; i < scArray.length; i++) {
                scArray[i].close();
            }
        }
        scArray = null;

        // delete files
        for (int i = 0; i < files.size(); i++) {
            files.get(i).delete();
        }

    }

    public V getNextFreeRow() {

        if (buffer.length > 0 && bufferBeanIndex + 1 < buffer.length) {
            bufferBeanIndex++;
            nextFreeRowCalled = true;
            V nextBean = (V) buffer[bufferBeanIndex];
            if (nextBean == null) {
                return createRowInstance();
            } else {
                return nextBean;
            }
        } else {
            return createRowInstance();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.components.thash.io.IMapHashFile#endGet(java.lang.String)
     */
    public void endGet() {
        afterLoopFind();
    }

    public static void main(String[] args) throws IOException {
        new PersistentRowSorterIterator<IPersistableRow>("/home/amaumont/data/dev/projets/Talend/hashfile/sort") { //$NON-NLS-1$

            protected IPersistableRow createRowInstance() {
                return null;
            }

        };
    }

    /**
     * Getter for sortEnabled.
     *
     * @return the sortEnabled
     */
    public boolean isSortEnabled() {
        return sortEnabled;
    }

    /**
     * Sets the sortEnabled.
     *
     * @param sortEnabled the sortEnabled to set
     */
    public void setSortEnabled(boolean sortEnabled) {
        this.sortEnabled = sortEnabled;
    }

}
