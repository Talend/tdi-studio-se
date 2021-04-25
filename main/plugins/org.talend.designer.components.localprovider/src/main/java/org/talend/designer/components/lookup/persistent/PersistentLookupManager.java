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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.jboss.serial.io.JBossObjectInputStream;
import org.jboss.serial.io.JBossObjectOutputStream;
import org.talend.designer.components.lookup.common.ICommonLookup.MATCHING_MODE;
import org.talend.designer.components.persistent.IRowCreator;
import org.talend.designer.components.persistent.utils.FileUtils;

import routines.system.IPersistableRow;

/**
 * Manager for lookup type 'ALL_ROWS'.
 *
 * <code>PersistentLookupManager</code>. This is the API to serialize/deserialize Talend objects sequentially and be
 * able to iterate on them.
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
public class PersistentLookupManager<B extends IPersistableRow<B>> implements IPersistentLookupManager<B>, Cloneable {

    /** This is 0 when using Sun stream library and 1 when using JBoss implementation */
    private static final int JBOSS_EOF = 1;

    private String container;

    private ObjectOutputStream objectOutStream;

    private BufferedInputStream bufferedInStream;

    private ObjectInputStream objectInStream;

    private IRowCreator<B> rowCreator;

    private B dataInstance;

    /**
     * PersistentLookupManager constructor.
     *
     * @param container
     * @throws IOException
     */
    public PersistentLookupManager(MATCHING_MODE matchingMode, String container, IRowCreator<B> rowCreator) throws IOException {
        super();
        this.container = container;
        this.rowCreator = rowCreator;
        FileUtils.createParentFolderIfNotExists(container);
    }

    public void initPut() throws IOException {

        // objectOutStream = new ObjectOutputStream(new BufferedOutputStream(new
        // FileOutputStream(buildDataFilePath())));

        File file = new File(buildDataFilePath());
        file.deleteOnExit();

        objectOutStream = new JBossObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
        this.dataInstance = this.rowCreator.createRowInstance();

    }

    private String buildDataFilePath() {
        return container + "_Data.bin"; //$NON-NLS-1$
    }

    public void put(B bean) throws IOException {
        bean.writeData(objectOutStream);
    }

    public void endPut() throws IOException {

        objectOutStream.close();

    }

    public void initGet() throws IOException {

        initDataIn();
        this.dataInstance = this.rowCreator.createRowInstance();

    }

    public void lookup(B key) throws IOException {

        if (this.objectInStream != null) {
            this.objectInStream.close();
        }
        if (this.bufferedInStream != null) {
            this.bufferedInStream.close();
        }

        initDataIn();

    }

    private void initDataIn() throws IOException {
        this.bufferedInStream = new BufferedInputStream(new FileInputStream(buildDataFilePath()));
        // this.objectInStream = new ObjectInputStream(bufferedInStream);
        this.objectInStream = new JBossObjectInputStream(bufferedInStream);
    }

    public B getNextFreeRow() {
        return this.dataInstance;
    }

    public boolean hasNext() throws IOException {
        // return this.objectInStream.available() > 0 || this.bufferedInStream.available() > 0;
        return this.objectInStream.available() > JBOSS_EOF || this.bufferedInStream.available() > 0;
    }

    public B next() throws IOException {
        dataInstance.readData(this.objectInStream);
        return dataInstance;
    }

    public void endGet() throws IOException {

        if (this.objectInStream != null) {
            this.objectInStream.close();
        }
        if (this.bufferedInStream != null) {
            this.bufferedInStream.close();
        }

        File file = new File(buildDataFilePath());
        file.delete();

    }

    public void clear() throws IOException {

    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
