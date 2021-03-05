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
package org.talend.designer.components.tsort.io.sortimpl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.talend.designer.components.tsort.io.IMapHashFile;
import org.talend.designer.components.tsort.io.beans.ILightSerializable;
import org.talend.designer.components.tsort.io.util.DeleteFileOnExitUtil;

/**
 *
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * @param <V> object value to sort
 */
public class FlowSorterIterator<V extends ILightSerializable> implements IMapHashFile<V>, Iterator<V> {

    /**
     * DOC amaumont SortedMultipleHashFile constructor comment.
     */
    public FlowSorterIterator() {
        super();
    }

    int[] bwPositionArray = null;

    boolean readonly;

    RandomAccessFile[] raArray = null;

    Object[] lastRetrievedObjectArray = null;

    long[] lastRetrievedCursorPositionArray = null;

    int countUniqueGet;

    // ////////////////////////
    // private int bufferSize = 5000000;
    // private int bufferSize = 8000000;
    // private int bufferSize = 9000000;
    // private int bufferSize = 9200000;
    private int bufferSize = 10000000;

    private int itemCountInBuffer = 0;

    private ILightSerializable[] buffer;

    private String container = null;

    ILightSerializable iLightSerializable = null;// Change this based on the

    // Bean class;

    private int beansCount;

    private ArrayList<Long> fileLengths;

    private ArrayList<DataInputStream> diss;

    private ArrayList<File> files = new ArrayList<File>();

    public String workDirectory = "/home/amaumont/hash_benchs/external_sort/";

    // public String workDirectory = "/home/amaumont/abc/c/";

    public int count = 0;

    private boolean someFileStillHasRows = true;

    private boolean haveDataWrote = false;

    private ArrayList<DataContainer> datas;

    private V currentObject;

    private boolean isFirstNext = true;

    // ///////////////////////

    public void initPut(String container) throws IOException {
        // System.out.println("bufferSize=" + bufferSize + " objects");
        this.container = container;
        buffer = new ILightSerializable[bufferSize];
    }

    public long put(String container, V bean) throws IOException {
        ILightSerializable item = (ILightSerializable) bean;

        if (!haveDataWrote) {
            haveDataWrote = true;
        }

        if (itemCountInBuffer >= bufferSize) {// buffer is full do sort and
            // write.
            // sort

            writeBuffer(buffer, itemCountInBuffer);

            // System.gc();

            itemCountInBuffer = 0;
        }
        buffer[itemCountInBuffer++] = item;

        beansCount++;

        return -1;

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.components.thash.io.IMapHashFile#endPut()
     */
    public void endPut() throws IOException {
        writeRemainingData();

        // System.gc();

        // for (; iFinalHashFile < numberFiles; iFinalHashFile++) {
        //
        // beforeLoopFind();
        //
        // // System.out.println("datasSameHashcodeValue=" +
        // datasSameHashcodeValue.toString());
        //
        // while (fileCount > 0) {
        //
        // findNextData();
        //
        // }
        //
        // afterLoopFind();
        //
        // }

    }

    /**
     * DOC amaumont Comment method "writeRemainingData".
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void writeRemainingData() throws FileNotFoundException, IOException {
        if (itemCountInBuffer > 0) {
            writeBuffer(buffer, itemCountInBuffer);
        }
        buffer = null;
    }

    public void initGet(String container) throws FileNotFoundException {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.components.thash.io.MapHashFile#getTotalSize()
     */
    public long getTotalSize() {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public void setILightSerializable(ILightSerializable ils) {
        this.iLightSerializable = ils;
    }

    /**
     * get number of objects already put.
     *
     * @return
     */
    public int getObjectsCount() {
        return beansCount;
    }

    // /////////////
    // tips for use//
    // /////////////

    // ///before put//////
    // SortedMultipleHashFile smh = SortedMultipleHashFile.getInstance();
    // smh.setBufferSize(10000000);//setBufferSize
    // smh.setILightSerializable(new Bean());//set an Instance of proccessed
    // Bean;
    // String container = "D:/temp/test";
    // smh.initPut(container);

    // ////do put//////
    // smh.put(container, data);

    // ////endput to merge files and gain the map//////
    // smh.endPut();

    // ////get the result map, it is a THashMap instance.//////
    // Map map = smh.getMap();

    // ////the get proccess will be the same as other IMapHashFile.//////
    // ......

    /**
     * sort list and then use light serialization to store Data
     *
     * @param list
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void writeBuffer(ILightSerializable[] list, int length) throws FileNotFoundException, IOException {
        // long time1 = System.currentTimeMillis();
        // System.out.println("Sorting buffer...");

        Arrays.sort(list, 0, length);

        // long time2 = System.currentTimeMillis();
        // long deltaTimeSort = (time2 - time1);
        // int itemsPerSecSort = (int) ((float) length / (float) deltaTimeSort *
        // 1000f);
        // System.out.println(deltaTimeSort + " milliseconds for " + length
        // + " objects to sort in memory. " + itemsPerSecSort
        // + "  items/s ");

        // time1 = System.currentTimeMillis();
        // System.out.println("Writing ordered buffer in file...");

        File file = new File(workDirectory + "TEMP_" + count);
        file.deleteOnExit();
        count++;
        DataOutputStream rw = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
        byte[] bytes = null;
        for (int i = 0; i < length; i++) {
            bytes = list[i].toByteArray();
            rw.writeInt(bytes.length);
            rw.write(bytes);
        }
        rw.close();
        files.add(file);

        // time2 = System.currentTimeMillis();
        // long deltaTimeWrite = (time2 - time1);
        // int itemsPerSecWrite = (int) ((float) length / (float) deltaTimeWrite
        // * 1000f);
        // System.out.println(deltaTimeWrite + " milliseconds for " + length
        // + " objects to write in file. " + itemsPerSecWrite
        // + "  items/s ");

    }

    /**
     * @param args
     * @throws IOException
     * @throws ClassNotFoundException
     */
    // public static void main(String[] args) throws IOException,
    // ClassNotFoundException {
    // ExternalSortIterator esort = new ExternalSortIterator();
    //
    // NumberFormat numberFormat = NumberFormat.getInstance();
    //
    // // int nbItems = 60000000;
    // // int bufferSize = 2000000;
    // // int nbItems = 60000000;
    // // int bufferSize = 4000000;
    // // int nbItems = 60000000;
    // // int bufferSize = 10000000;
    // // int nbItems = 10000000;
    // // int bufferSize = 1000000;
    // // int nbItems = 1000000;
    // // int bufferSize = 100000;
    // // int nbItems = 20;
    // // int bufferSize = 2;
    // int nbItems = 100;
    // int bufferSize = 20;
    //
    // Random rand = new Random(System.currentTimeMillis());
    //
    // long start = System.currentTimeMillis();
    //
    // Data[] arrayData = new Data[bufferSize];
    //
    // int nbItemsProcessed = 0;
    //
    // for (int i = 0; nbItemsProcessed < nbItems; i++) {
    // int v = rand.nextInt(nbItems);
    //
    // arrayData[i] = new Data("test" + v, v, 0);
    //
    // if (i == bufferSize - 1) {
    //
    // // esort.writeBuffer(arrayData);
    // esort.writeBuffer(arrayData);
    //
    // long time1 = System.currentTimeMillis();
    //
    // Arrays.fill(arrayData, null);
    //
    // long time2 = System.currentTimeMillis();
    // long deltaTimeNull = (time2 - time1);
    // System.out.println(deltaTimeNull + " milliseconds for "
    // + bufferSize + " objects to set buffer as null. ");
    //
    // nbItemsProcessed += i + 1;
    // System.out.println(numberFormat.format(nbItemsProcessed)
    // + " / " + numberFormat.format(nbItems) + " processed.");
    // i = -1;
    // }
    //
    // }
    // System.out.println("Final process : merging file...");
    // long time1 = System.currentTimeMillis();
    //
    // // esort.sort();
    // // esort.mergeFiles();
    // // esort.mergeFiles2();
    // // esort.eMergeFiles2();
    //
    // for (; esort.hasNext();) {
    // System.out.println(esort.next());
    // ;
    // }
    //
    // long time2 = System.currentTimeMillis();
    // long deltaTimeMerge = (time2 - time1);
    // int itemsPerSecMerge = (int) ((float) nbItems / (float) deltaTimeMerge *
    // 1000f);
    // System.out.println(deltaTimeMerge + " milliseconds for " + nbItems
    // + " ordered objects to merge. " + itemsPerSecMerge
    // + "  items/s ");
    //
    // long end = System.currentTimeMillis();
    //
    // long deltaTime = (end - start);
    //
    // int itemsPerSec = (int) ((float) nbItems / (float) deltaTime * 1000f);
    //
    // System.out.println(deltaTime + " milliseconds for " + nbItems
    // + " objects all sort process. " + itemsPerSec + "  items/s ");
    //
    // }
    public boolean hasNext() {
        return someFileStillHasRows && haveDataWrote;
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
     */
    private void beforeLoopFind() throws IOException {
        // File file = new File(workDirectory + "TEMP_" + count);

        // DataOutputStream dos = new DataOutputStream(new
        // BufferedOutputStream(new FileOutputStream(file)));
        int numFiles = files.size();
        diss = new ArrayList<DataInputStream>();
        datas = new ArrayList<DataContainer>();
        fileLengths = new ArrayList<Long>();

        boolean someFileStillHasRows = false;

        for (int i = 0; i < numFiles; i++) {
            diss.add(new DataInputStream(new BufferedInputStream(new FileInputStream(files.get(i)))));
            fileLengths.add(files.get(i).length());
            DataContainer dc = new DataContainer();
            DataInputStream dis = diss.get(i);
            byte[] bytes = new byte[dis.readInt()];
            dis.read(bytes);
            dc.object = iLightSerializable.createInstance(bytes);
            if (!someFileStillHasRows) {
                someFileStillHasRows = true;
            }
            datas.add(dc);
            DeleteFileOnExitUtil.add(files.get(i).getAbsolutePath(), dis);
        }

    }

    /**
     * DOC amaumont Comment method "findNextData".
     *
     * @throws IOException
     */
    private void findNextData() throws IOException {
        DataContainer min = null;
        int minIndex = 0;
        DataContainer dataContainer = datas.get(0);

        if (dataContainer.object != null) {
            min = dataContainer;
            minIndex = 0;
        } else {
            min = null;
            minIndex = -1;
        }

        // check which one is min
        for (int i = 1; i < datas.size(); i++) {
            dataContainer = datas.get(i);

            if (min != null) {
                if (dataContainer.object != null && ((Comparable) (dataContainer.object)).compareTo(min.object) < 0) {
                    minIndex = i;
                    min = dataContainer;
                }
            } else {
                if (dataContainer.object != null) {
                    min = dataContainer;
                    minIndex = i;
                }
            }
        }

        if (minIndex < 0) {
            someFileStillHasRows = false;
        } else {
            // write to the sorted file
            // write(min.data, dos);

            currentObject = (V) min.object;

            min.reset();

            // get another data from the file
            DataInputStream dis = diss.get(minIndex);
            if (dis.available() > 0) {
                byte[] bytes = new byte[dis.readInt()];
                dis.read(bytes);
                min.object = iLightSerializable.createInstance(bytes);
                min.cursorPosition += 4 + bytes.length;
            }
            // check if one still has data
            someFileStillHasRows = false;
            for (int i = 0; i < datas.size(); i++) {
                if (datas.get(i).object != null) {
                    someFileStillHasRows = true;
                    break;
                }
            }
        }

        // delete temp files
        if (!someFileStillHasRows) {
            afterLoopFind();
        }
    }

    /**
     * DOC amaumont Comment method "afterLoopFind".
     *
     * @throws IOException
     */
    private void afterLoopFind() throws IOException {
        // close all the streams
        // dos.close();
        for (int i = 0; i < diss.size(); i++) {
            diss.get(i).close();
        }
        // delete files
        for (int i = 0; i < files.size(); i++) {
            files.get(i).delete();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.components.thash.io.IMapHashFile#endGet(java.lang .String)
     */
    public void endGet(String container) throws IOException {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.components.thash.io.IMapHashFile#get(java.lang.String , long, int)
     */
    public V get(String container, long cursorPosition, int hashcode) throws Exception {
        throw new UnsupportedOperationException();
    }

}
