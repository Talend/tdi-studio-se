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
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.jboss.serial.io.JBossObjectInputStream;
import org.talend.designer.components.lookup.common.ILookupManagerUnit;
import org.talend.designer.components.persistent.IRowProvider;

import routines.system.IPersistableLookupRow;

/**
 *
 * Abstract class for ordered beans used in lookups with "Store on disk".
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
 * @param <B> bean
 */
public abstract class AbstractOrderedBeanLookup<B extends Comparable<B> & IPersistableLookupRow<B>> implements
        ILookupManagerUnit<B> {

    protected static final int MARK_READ_LIMIT = 256 * 1024 * 1024;

    protected static final int KEYS_SIZE_PLUS_VALUES_SIZE = 8;

    protected BufferedInputStream keysBufferedInStream;

    protected ObjectInputStream keysObjectInStream;

    protected DataInputStream valuesDataInStream;

    protected ObjectInputStream valuesObjectInStream;

    protected long length;

    protected B lookupInstance;

    protected int currentValuesSize;

    protected long skipValuesSize;

    protected long countBeansToSkip;

    protected boolean nextDirty = true;

    protected boolean noMoreNext;

    protected B previousAskedKey;

    protected long markCursorPosition = -1;

    protected B currentSearchedKey;

    protected boolean hasNext;

    protected boolean atLeastOneLoadkeys = false;

    protected boolean startWithNewKey;

    protected IRowProvider<B> rowProvider;

    protected boolean nextWithPreviousLookup;

    protected int remainingSkip;

    protected boolean previousCompareResultMatch;

    protected B previousLookupInstance;

    protected int sizeDataToRead;

    protected B resultLookupInstance;

    protected int fileIndex;

    private boolean skipBytesEnabled;

    /**
     *
     * DOC amaumont OrderedBeanLookup constructor comment.
     *
     * @param keysFilePath
     * @param valuesFilePath
     * @param fileIndex
     * @param skipBytesEnabled
     * @param internalKeyInstance
     * @param keys_management
     * @throws IOException
     */
    public AbstractOrderedBeanLookup(String keysFilePath, String valuesFilePath, int fileIndex, IRowProvider<B> rowProvider,
            boolean skipBytesEnabled) throws IOException {
        File keysDataFile = new File(keysFilePath);
        this.length = keysDataFile.length();

        this.fileIndex = fileIndex;

        this.keysBufferedInStream = new BufferedInputStream(new FileInputStream(keysDataFile));
        if (PersistentSortedLookupManager.USE_JBOSS_IMPLEMENTATION) {
            this.keysObjectInStream = new JBossObjectInputStream(keysBufferedInStream);
        } else {
            this.keysObjectInStream = new ObjectInputStream(keysBufferedInStream);
        }
        this.valuesDataInStream = new DataInputStream(new BufferedInputStream(new FileInputStream(valuesFilePath)));
        if (PersistentSortedLookupManager.USE_JBOSS_IMPLEMENTATION) {
            this.valuesObjectInStream = new JBossObjectInputStream(valuesDataInStream);
        } else {
            this.valuesObjectInStream = new ObjectInputStream(valuesDataInStream);
        }
        this.lookupInstance = rowProvider.createInstance();
        this.previousAskedKey = rowProvider.createInstance();
        this.rowProvider = rowProvider;

        this.skipBytesEnabled = skipBytesEnabled;

        // readDescriptors();

    }

    private void readDescriptors() throws IOException {
        int countObjects = valuesDataInStream.readInt();
        for (int i = 0; i < countObjects; i++) {
            try {
                Object readObject = valuesObjectInStream.readObject();
                System.out.println(readObject);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.components.persistent.ILookupManager#lookup(B)
     */
    public abstract void lookup(B key) throws IOException;

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.components.persistent.ILookupManager#hasNext()
     */
    public abstract boolean hasNext() throws IOException;

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.components.persistent.ILookupManager#next()
     */
    public abstract B next() throws IOException;

    protected void loadDataKeys(B lookupInstance) throws IOException {
        atLeastOneLoadkeys = true;
        lookupInstance.readKeysData(keysObjectInStream);
        currentValuesSize = keysObjectInStream.readInt();
    }

    protected boolean isEndOfKeysFile() throws IOException {
        return !(keysObjectInStream.available() > (PersistentSortedLookupManager.USE_JBOSS_IMPLEMENTATION ? 1 : 0) || keysBufferedInStream
                .available() > 0);
    }

    protected void loadDataValues(B lookupInstance, int valuesSize) throws IOException {
        if (skipValuesSize > 0) {
            skipValuesSize += remainingSkip;

            if (skipBytesEnabled) {
                int currentSkipped = 0;
                while (skipValuesSize != (currentSkipped += valuesDataInStream.skip(skipValuesSize - currentSkipped)))
                    ;
            } else {
                for (long i = 0; i < countBeansToSkip; i++) {
                    lookupInstance.readValuesData(valuesDataInStream, valuesObjectInStream);
                }
            }

            // System.out.println("Data skipped:" + skipValuesSize);
            remainingSkip = 0;
            skipValuesSize = 0;
            countBeansToSkip = 0;
        }
        lookupInstance.readValuesData(valuesDataInStream, valuesObjectInStream);

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.components.persistent.ILookupManager#close()
     */
    public void close() throws IOException {

        if (keysObjectInStream != null) {
            keysObjectInStream.close();
        }
        if (keysBufferedInStream != null) {
            keysBufferedInStream.close();
        }
        if (valuesDataInStream != null) {
            valuesObjectInStream.close();
        }
        if (valuesDataInStream != null) {
            valuesDataInStream.close();
        }
    }

}
