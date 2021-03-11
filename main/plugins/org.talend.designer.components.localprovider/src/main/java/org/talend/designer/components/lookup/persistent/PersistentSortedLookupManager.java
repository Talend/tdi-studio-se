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

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.jboss.serial.io.JBossObjectOutputStream;
import org.talend.designer.components.lookup.common.ICommonLookup.MATCHING_MODE;
import org.talend.designer.components.lookup.common.ILookupManagerUnit;
import org.talend.designer.components.persistent.IRowCreator;
import org.talend.designer.components.persistent.utils.FileUtils;

import routines.system.IPersistableComparableLookupRow;
import routines.system.IPersistableLookupRow;

/**
 * Persistent Sorted Lookup Manager.
 *
 * JBoss library is used to avoid memory leaks noticed with Sun ObjectInputStream class.
 *
 * @see http://www.talendforge.org/bugs/view.php?id=6780#bugnotes
 *
 * @param <B> bean
 */
public class PersistentSortedLookupManager<B extends IPersistableComparableLookupRow<B>> extends AbstractPersistentLookup<B>
        implements IPersistentLookupManager<B>, Cloneable {

    /**
     * DOC amaumont PersistentSortedLookupManager class global comment. Detailled comment
     * Check property type
     */
    private enum CPT {
        CHECK_WHILE_NULL,
        CHECK_ALWAYS,
        CHECK_ALWAYS_INHERITED,
    }

    private static final Class<?>[] CUSTOM_SERIALIZATION_CLASSES = new Class[] { char.class, Character.class, boolean.class,
            Boolean.class, byte.class, Byte.class, byte[].class, short.class, Short.class, int.class, Integer.class, long.class,
            Long.class, float.class, Float.class, double.class, Double.class, String.class, };

    private static final Set<Class<?>> CUSTOM_SERIALIZATION_CLASSES_SET = new HashSet<Class<?>>(
            Arrays.asList(CUSTOM_SERIALIZATION_CLASSES));

    private static final Class<?>[] WRITE_WARNING_IF_INHERITED = new Class[] { BigDecimal.class };

    private static final Set<Class<?>> WRITE_WARNING_IF_INHERITED_SET = new HashSet<Class<?>>(
            Arrays.asList(WRITE_WARNING_IF_INHERITED));

    private static final Set<String> ALREADY_PROCESSED_PROPERTY_TO_WARN_CHANGE_TO_OBJECT = new HashSet<String>();

    private static final String[] FIELDS_TO_OMIT = new String[] { "hashCodeDirty" };

    private static final Set<String> FIELDS_TO_OMIT_SET = new HashSet<String>(Arrays.asList(FIELDS_TO_OMIT));

    private static final float MARGIN_MAX = 0.35f;

    private String container;

    private MATCHING_MODE matchingMode;

    //
    private ILookupManagerUnit<B>[] lookupList;

    private int bufferSize = 10000000;

    // private int bufferSize = 100;
    // private int bufferSize = 20;

    // private int bufferSize = 100;
    // private int bufferSize = 3;

    private IPersistableLookupRow<B>[] buffer = null;

    private int fileIndex = 0;

    // ////////////////////////////////////////
    private int bufferBeanIndex = 0;

    private int lookupIndex = 0;

    ILookupManagerUnit<B> currLookup;

    private int lookupListSize;

    private boolean waitingNext;

    private B lookupKey;

    private boolean noMoreNext;

    private B previousResult;

    private boolean nextIsPreviousResult;

    private IRowCreator<B> rowCreator;

    private boolean lookupKeyIsInitialized;

    private boolean previousResultRetrieved;

    private int bufferMarkLimit = -1;

    private boolean bufferIsMarked;

    private boolean firstUnsifficientMemory = true;

    private boolean waitingHeapException;

    private boolean sortEnabled = true;

    private boolean skipBytesEnabled = true;

    public static final boolean USE_JBOSS_IMPLEMENTATION = true;

    // private List<Field> propNameToCheckAtEachLine = new ArrayList<Field>();
    //
    // private List<Field> propNameToCheckWhileValueIsNull = new ArrayList<Field>();

    private List<Field> propNameToCheckIsInherited = new ArrayList<Field>();

    private Map<String, Object> objectsToWriteAtBeginningOfValuesFile = new HashMap<String, Object>();

    public PersistentSortedLookupManager(MATCHING_MODE matchingMode, String filePath, IRowCreator<B> rowCreator)
            throws IOException {
        this.matchingMode = matchingMode;
        this.container = filePath;
        this.rowCreator = rowCreator;
        FileUtils.createParentFolderIfNotExists(filePath);

        // System.out.println("skipBytesEnabled=" + skipBytesEnabled);
        // System.out.println("USE_JBOSS_IMPLEMENTATION=" + USE_JBOSS_IMPLEMENTATION);

    }

    public PersistentSortedLookupManager(MATCHING_MODE matchingMode, String container, IRowCreator<B> rowCreator, int bufferSize)
            throws IOException {
        this(matchingMode, container, rowCreator);
        this.bufferSize = bufferSize;
    }

    public void initPut() throws IOException {
        buffer = new IPersistableLookupRow[bufferSize];
        bufferBeanIndex = 0;
    }

    public void put(B bean) throws IOException {

        // if (bufferBeanIndex == 0 && fileIndex == 0) {
        // checkClassOfBeanPropertiesInit(bean);
        // }
        // checkClassOfBeanProperties(bean);

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
                    System.out.println(
                            "Warning: to avoid a Memory heap space error the buffer of the lookup has been limited to a size of " //$NON-NLS-1$
                                    + bufferMarkLimit
                                    + " , try to reduce the advanced parameter \"Max buffer size\" (~100000 or at least less than "
                                    + bufferMarkLimit + "), then if needed try to increase the JVM Xmx parameter.");
                    bufferIsMarked = true;
                }
            }
        }

        if (bufferBeanIndex == bufferSize || bufferIsMarked && bufferBeanIndex == bufferMarkLimit) {
            writeBuffer();
            if (!bufferIsMarked) {
                bufferMarkLimit = bufferBeanIndex;
                // System.out.println("Buffer marked at index (2-Lookup) " + bufferMarkLimit);
                bufferIsMarked = true;
            }
            bufferBeanIndex = 0;
        }

        buffer[bufferBeanIndex++] = bean;
    }

    private void checkClassOfBeanPropertiesInit(B bean) {
        Class<? extends IPersistableComparableLookupRow> beanClass = bean.getClass();
        Field[] declaredFields = beanClass.getDeclaredFields();

        for (int i = 0; i < declaredFields.length; i++) {
            Field propertyDescriptor = declaredFields[i];
            int fieldModifier = propertyDescriptor.getModifiers();
            if (Modifier.isPublic(fieldModifier)) {
                Class<?> clazzOfBeanProperty = propertyDescriptor.getType();
                String propertyName = propertyDescriptor.getName();
                if (!FIELDS_TO_OMIT_SET.contains(propertyName)
                        && !CUSTOM_SERIALIZATION_CLASSES_SET.contains(clazzOfBeanProperty)) {
                    // if (WRITE_WARNING_IF_INHERITED_SET.contains(clazzOfBeanProperty)) {
                    // propNameToCheckIsInherited.add(propertyDescriptor);
                    // } else {
                    // skipBytesEnabled = false;
                    // break;
                    // }
                    skipBytesEnabled = false;
                    break;
                }

                // boolean propertyIsPrimtive = clazzOfBeanProperty.isPrimitive();
                // Object[] signers = clazzOfBeanProperty.getSigners();
                // if (!propertyIsPrimtive) {
                // int modifiers = clazzOfBeanProperty.getModifiers();
                // boolean propertyHasFinalType = Modifier.isFinal(modifiers);
                // if (!propertyHasFinalType) {
                // propNameToCheckAtEachLine.add(propertyDescriptor);
                // } else {
                // Object propertyValue = null;
                // try {
                // propertyValue = propertyDescriptor.get(bean);
                // } catch (IllegalAccessException e) {
                // // TODO Auto-generated catch block
                // e.printStackTrace();
                // }
                // if (propertyValue != null) {
                // String className = clazzOfBeanProperty.getName();
                // objectsToWriteAtBeginningOfValuesFile.put(className, propertyValue);
                // } else {
                // propNameToCheckWhileValueIsNull.add(propertyDescriptor);
                // }
                // }
                // }
            }
        }

        System.out.println("skipBytesEnabled=" + skipBytesEnabled);

    }

    private void checkClassOfBeanProperties(B bean) {
        if (propNameToCheckIsInherited.size() > 0) {
            int propNameToCheckIsInheritedListSize = propNameToCheckIsInherited.size();
            for (int i = 0; i < propNameToCheckIsInheritedListSize; i++) {
                Field propertyName = propNameToCheckIsInherited.get(i);
                checkProperty(bean, propertyName, CPT.CHECK_ALWAYS_INHERITED);
            }
        }
        // if (propNameToCheckAtEachLine.size() > 0) {
        // int propNameToCheckAtEachLineListSize = propNameToCheckAtEachLine.size();
        // for (int i = 0; i < propNameToCheckAtEachLineListSize; i++) {
        // Field propertyName = propNameToCheckAtEachLine.get(i);
        // checkProperty(bean, propertyName, CPT.CHECK_ALWAYS);
        // }
        // }
        // if (propNameToCheckWhileValueIsNull.size() > 0) {
        // for (Iterator<Field> iterator = propNameToCheckWhileValueIsNull.iterator(); iterator.hasNext();) {
        // Field propertyName = iterator.next();
        // boolean propertyNameToRemoveFromList = checkProperty(bean, propertyName,
        // CPT.CHECK_WHILE_NULL);
        // if (propertyNameToRemoveFromList) {
        // iterator.remove();
        // }
        // }
        // }
    }

    /**
     *
     * DOC amaumont Comment method "checkProperty".
     *
     * @param bean
     * @param property
     * @param checkType
     * @return true if property has to be removed from list <code>propNameToCheckWhileValueIsNull</code>
     */
    private boolean checkProperty(B bean, Field property, CPT checkType) {
        Object propertyValue = null;
        try {
            propertyValue = property.get(bean);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        boolean removePropertyName = false;
        if (propertyValue != null) {
            String propertyName = property.getName();
            String propertyClassName = propertyValue.getClass().getName();
            if (checkType == CPT.CHECK_ALWAYS_INHERITED && propertyValue.getClass().isInstance(propertyName.getClass())
                    && !ALREADY_PROCESSED_PROPERTY_TO_WARN_CHANGE_TO_OBJECT.contains(propertyName)) {
                ALREADY_PROCESSED_PROPERTY_TO_WARN_CHANGE_TO_OBJECT.add(propertyName);
                System.out.println("To avoid some serialization error, we advice you to declare the field name '" + propertyName //$NON-NLS-1$
                        + "' with the type 'Object'"); //$NON-NLS-1$
            }
            if ((checkType == CPT.CHECK_ALWAYS_INHERITED || checkType == CPT.CHECK_ALWAYS)
                    && !objectsToWriteAtBeginningOfValuesFile.containsKey(propertyClassName)) {
                objectsToWriteAtBeginningOfValuesFile.put(propertyClassName, propertyValue);
            }
            // if (checkType == CPT.CHECK_WHILE_NULL
            // && !objectsToWriteAtBeginningOfValuesFile.containsKey(propertyClassName)) {
            // objectsToWriteAtBeginningOfValuesFile.put(propertyClassName, propertyValue);
            // removePropertyName = true;
            // }
        }
        return removePropertyName;
    }

    public void endPut() throws IOException {

        if (bufferBeanIndex > 0) {
            writeBuffer();
        }

        // Arrays.fill(buffer, null);

        buffer = null;

    }

    private void writeBuffer() throws IOException {
        if (this.sortEnabled) {
            Arrays.sort(buffer, 0, bufferBeanIndex);
        }
        File keysDataFile = new File(buildKeysFilePath(fileIndex));
        keysDataFile.deleteOnExit();

        File valuesDataFile = new File(buildValuesFilePath(fileIndex));
        valuesDataFile.deleteOnExit();

        BufferedOutputStream keysBufferedOutputStream = new BufferedOutputStream(new FileOutputStream(keysDataFile));
        ObjectOutputStream keysDataOutputStream = null;
        if (USE_JBOSS_IMPLEMENTATION) {
            keysDataOutputStream = new JBossObjectOutputStream(keysBufferedOutputStream);
        } else {
            keysDataOutputStream = new ObjectOutputStream(keysBufferedOutputStream);
        }

        BufferedOutputStream valuesBufferedOutputStream = new BufferedOutputStream(new FileOutputStream(valuesDataFile));
        final LongLengthOutputStream valuesLongOutputStream = new LongLengthOutputStream(valuesBufferedOutputStream);
        DataOutputStream valuesDataOutputStream = new DataOutputStream(valuesLongOutputStream);
        ObjectOutputStream valuesObjectOutputStream = null;
        if (USE_JBOSS_IMPLEMENTATION) {
            valuesObjectOutputStream = new JBossObjectOutputStream(valuesDataOutputStream);
        } else {
            valuesObjectOutputStream = new ObjectOutputStream(valuesDataOutputStream);
        }

        // System.out.println("Writing LOOKUP buffer " + fileIndex + "... ");

        long previousSize = valuesLongOutputStream.size();
        int writtenValuesDataSize = 0;
        long newSize = 0;

        // writeDescriptors(valuesDataOutputStream, valuesObjectOutputStream);

        previousSize = valuesLongOutputStream.size();

        for (int i = 0; i < bufferBeanIndex; i++) {

            IPersistableLookupRow<B> curBean = buffer[i];

            curBean.writeValuesData(valuesDataOutputStream, valuesObjectOutputStream);
            newSize = valuesLongOutputStream.size();
            writtenValuesDataSize = (int) (newSize - previousSize);
            curBean.writeKeysData(keysDataOutputStream);
            keysDataOutputStream.writeInt(writtenValuesDataSize);
            previousSize = newSize;
            // System.out.println(curBean);
        }
        // System.out.println("Write ended LOOKUP buffer " + fileIndex);
        keysDataOutputStream.close();
        valuesObjectOutputStream.close();
        fileIndex++;
    }

    private String buildValuesFilePath(int i) {
        return container + "ValuesData_" + i + ".bin"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    private String buildKeysFilePath(int i) {
        return container + "KeysData_" + i + ".bin"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void initGet() throws IOException {
        previousResultRetrieved = false;
        this.lookupKey = rowCreator.createRowInstance();
        lookupList = (ILookupManagerUnit<B>[]) new ILookupManagerUnit[fileIndex];
        for (int i = 0; i < fileIndex; i++) {
            RowProvider<B> rowProvider = new RowProvider<B>(rowCreator);
            lookupList[i] = getOrderedBeanLoohupInstance(buildKeysFilePath(i), buildValuesFilePath(i), i, rowProvider,
                    this.matchingMode);

        }
        lookupListSize = lookupList.length;
    }

    private void writeDescriptors(DataOutputStream valuesDataOutputStream, ObjectOutputStream valuesObjectOutputStream)
            throws IOException {
        Collection<Object> values = objectsToWriteAtBeginningOfValuesFile.values();
        int countObjects = values.size();
        valuesDataOutputStream.writeInt(countObjects);
        if (countObjects > 0) {
            for (Object object : values) {
                valuesObjectOutputStream.writeObject(object);
            }
        }
    }

    private ILookupManagerUnit<B> getOrderedBeanLoohupInstance(String keysFilePath, String valuesFilePath, int i,
            RowProvider<B> rowProvider, MATCHING_MODE keysManagement) throws IOException {
        switch (keysManagement) {
        case FIRST_MATCH:
            return new OrderedBeanLookupMatchFirst<B>(keysFilePath, valuesFilePath, i, rowProvider, skipBytesEnabled);

        case LAST_MATCH:
        case UNIQUE_MATCH:

            return new OrderedBeanLookupMatchLast<B>(keysFilePath, valuesFilePath, i, rowProvider, skipBytesEnabled);

        case ALL_MATCHES:

            return new OrderedBeanLookupMatchAll<B>(keysFilePath, valuesFilePath, i, rowProvider, skipBytesEnabled);

        case ALL_ROWS:

            return new OrderedBeanLookupAll<B>(valuesFilePath);

        default:
            throw new IllegalArgumentException();
        }
    }

    public void lookup(B key) throws IOException {

        waitingNext = false;
        if (matchingMode == MATCHING_MODE.ALL_MATCHES) {
            lookupIndex = 0;
            for (int lookupIndexLocal = 0; lookupIndexLocal < lookupListSize; lookupIndexLocal++) {
                ILookupManagerUnit<B> tempLookup = lookupList[lookupIndexLocal];
                tempLookup.lookup(key);
            }
        } else {
            try {
                if (lookupKey.compareTo(key) == 0 && previousResultRetrieved) {
                    nextIsPreviousResult = true;
                } else {
                    previousResultRetrieved = false;
                    previousResult = null;
                }
            } catch (NullPointerException e) {
                previousResultRetrieved = false;
                previousResult = null;
            }
            noMoreNext = false;
        }
        key.copyDataTo(lookupKey);
        lookupKeyIsInitialized = true;
    }

    public boolean hasNext() throws IOException {

        if (waitingNext || nextIsPreviousResult) {
            return true;
        }

        if (!lookupKeyIsInitialized || noMoreNext) {
            return false;
        }

        if (matchingMode == MATCHING_MODE.LAST_MATCH || matchingMode == MATCHING_MODE.UNIQUE_MATCH) {
            for (int lookupIndexLocal = lookupListSize - 1; lookupIndexLocal >= 0; lookupIndexLocal--) {
                ILookupManagerUnit<B> tempLookup = lookupList[lookupIndexLocal];
                // System.out.println("########################################");
                // System.out.println(lookupKey);
                // System.out.println("lookupIndexLocal=" + lookupIndexLocal);
                tempLookup.lookup(lookupKey);
                if (tempLookup.hasNext()) {
                    // System.out.println("Found in " + lookupIndexLocal);
                    currLookup = tempLookup;
                    waitingNext = true;
                    noMoreNext = true;
                    previousResultRetrieved = false;
                    return true;
                }
            }
            noMoreNext = true;
            return false;

        } else if (matchingMode == MATCHING_MODE.ALL_MATCHES) {
            for (; lookupIndex < lookupListSize; lookupIndex++) {
                ILookupManagerUnit<B> tempLookup = lookupList[lookupIndex];
                if (tempLookup.hasNext()) {
                    currLookup = tempLookup;
                    waitingNext = true;
                    return true;
                }
            }
            return false;

        } else if (matchingMode == MATCHING_MODE.FIRST_MATCH) {
            for (int lookupIndexLocal = 0; lookupIndexLocal < lookupListSize; lookupIndexLocal++) {
                ILookupManagerUnit<B> tempLookup = lookupList[lookupIndexLocal];
                tempLookup.lookup(lookupKey);
                if (tempLookup.hasNext()) {
                    currLookup = tempLookup;
                    waitingNext = true;
                    noMoreNext = true;
                    previousResultRetrieved = false;
                    return true;
                }
            }
            noMoreNext = true;
            return false;

        } else {

            if (currLookup.hasNext()) {
                waitingNext = true;
                return true;
            }
            lookupIndex++;
            return false;

        }

    }

    public B next() throws IOException {

        if (nextIsPreviousResult) {
            nextIsPreviousResult = false;
            noMoreNext = true;
            return previousResult;
        }

        if (waitingNext) {
            waitingNext = false;
            previousResult = currLookup.next();

            if (matchingMode == MATCHING_MODE.LAST_MATCH || matchingMode == MATCHING_MODE.FIRST_MATCH) {
                previousResultRetrieved = true;
                noMoreNext = true;
            }

            return previousResult;
        } else {
            throw new NoSuchElementException();
        }
    }

    public void endGet() throws IOException {
        for (ILookupManagerUnit<B> orderedBeanLookup : lookupList) {
            orderedBeanLookup.close();
        }
        clear();
        lookupList = null;
    }

    public void clear() throws IOException {
        for (int i = 0; i < fileIndex; i++) {
            (new File(buildKeysFilePath(i))).delete();
            (new File(buildValuesFilePath(i))).delete();
        }
    }

    public B getNextFreeRow() {
        if (buffer.length > 0 && bufferBeanIndex != buffer.length) {
            B nextBean = (B) buffer[bufferBeanIndex];
            if (nextBean == null) {
                return this.rowCreator.createRowInstance();
            } else {
                return nextBean;
            }
        } else {
            return this.rowCreator.createRowInstance();
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
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

    private static class LongLengthOutputStream extends OutputStream {

        private long size = 0;

        private final OutputStream out;

        public LongLengthOutputStream(OutputStream out) {
            this.out = out;
        }

        @Override
        public void write(int b) throws IOException {
            out.write(b);
            incLength(1);
        }

        private void incLength(int length) {
            long tempLength = this.size + length;
            if (tempLength < 0) {
                tempLength = Long.MAX_VALUE;
            }
            this.size = tempLength;
        }

        public long size() {
            return size;
        }

        public void close() throws IOException {
            out.close();
            size = 0;
        }

    }

}
