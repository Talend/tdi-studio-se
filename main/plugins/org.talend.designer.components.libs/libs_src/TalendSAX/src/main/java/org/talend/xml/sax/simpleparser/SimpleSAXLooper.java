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
package org.talend.xml.sax.simpleparser;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.talend.xml.sax.SAXLoopCompositeHandler;
import org.talend.xml.sax.commons.ISAXLooper;
import org.talend.xml.sax.io.UnicodeReader;
import org.talend.xml.sax.simpleparser.model.XMLNode;
import org.talend.xml.sax.simpleparser.model.XMLNodes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class SimpleSAXLooper implements ISAXLooper, Callable {

    private XMLNodes nodes = new XMLNodes();

    private DataBufferCache bcache;

    private DataBufferCache2 multiCache;

    private Thread task;

    private FutureTask futureTask;

    private boolean ignoreDTD = false;

    SimpleSAXLoopHandler hd = null;

    private String[] arrOrigLoopPath;

    private String rootPath;

    private String[] arrLoopPath;

    private final String LOAD_EXTERNAL_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";

    private final String EXTERNAL_GENERAL_ENTITIES = "http://xml.org/sax/features/external-general-entities";

    private final String EXTERNAL_PARAMETER_ENTITIES = "http://xml.org/sax/features/external-parameter-entities";

    private List<XMLNodes> nodesList = new ArrayList<XMLNodes>();

    public SimpleSAXLooper(String loopPath, String[] nodePaths, boolean[] asXMLs) {
        futureTask = new FutureTask(this);
        task = new Thread(futureTask);
        for (int i = 0; i < nodePaths.length; i++) {
            nodes.addNode(new XMLNode(loopPath, nodePaths[i], null, asXMLs[i]));
        }
        initLoopEntry();
    }

    public SimpleSAXLooper(String rootPath, String[] arrLoopPath, String[][] arrNodePaths) {
        futureTask = new FutureTask(this);
        task = new Thread(futureTask);

        this.arrOrigLoopPath = arrLoopPath;

        String tmpRootPath = rootPath;
        if (tmpRootPath.endsWith("/")) {
            tmpRootPath = tmpRootPath.substring(0, tmpRootPath.length() - 1);
        }

        this.rootPath = tmpRootPath;

        this.arrLoopPath = getLoopPaths(arrLoopPath);

        for (int j = 0; j < arrNodePaths.length; j++) {
            String[] nodePaths = arrNodePaths[j];
            XMLNodes ns = new XMLNodes();
            for (int i = 0; i < nodePaths.length; i++) {
                ns.addNode(new XMLNode(this.arrOrigLoopPath[j], this.arrLoopPath[j], nodePaths[i], null));
            }
            nodesList.add(ns);
        }

        initLoopEntries();
    }

    private String[] getLoopPaths(String[] arrLoops) {

        String[] loopPaths = new String[arrLoops.length];

        for (int i = 0; i < arrLoops.length; i++) {
            String column = arrLoops[i];
            String resultCol = this.rootPath;
            String[] splits = column.split("/");
            for (String tmp : splits) {
                if (tmp.equals("..")) {
                    resultCol = resultCol.substring(0, resultCol.lastIndexOf("/"));
                } else if (tmp.equals(".")) {
                } else if (!("").equals(tmp)) {
                    resultCol += "/" + tmp;
                }
            }

            loopPaths[i] = resultCol;
        }

        return loopPaths;
    }

    /**
     * handle the exception in task.
     * FutureTask.get() is a block method waiting for the Task over and it can throw the exception in
     * Task(Callable,Thread,Runnable)
     *
     * @throws Exception
     */
    public void handleException() throws Exception {
        futureTask.get();
    }

    private void initLoopEntry() {

        bcache = DataBufferCache.getInstance();

        // parse the node path to loopEntry
        for (XMLNode node : nodes.getNodes().values()) {
            String column = node.originPath;
            String resultCol = node.loopPath;
            String[] splits = column.split("/");
            for (String tmp : splits) {
                if (tmp.equals("..")) {
                    resultCol = resultCol.substring(0, resultCol.lastIndexOf("/"));
                    node.setAttrOutOfLoop(true);
                } else if (tmp.equals(".")) {
                    node.isDot = true;
                } else {
                    resultCol += "/" + tmp;
                }
            }
            node.nodePath = resultCol;
        }
    }

    private void initLoopEntries() {

        multiCache = DataBufferCache2.getInstance();

        for (XMLNodes ns : nodesList) {

            for (XMLNode node : ns.getNodes().values()) {
                String column = node.originPath;
                String resultCol = node.loopPath;
                String[] splits = column.split("/");
                for (String tmp : splits) {
                    if (tmp.equals("..")) {
                        resultCol = resultCol.substring(0, resultCol.lastIndexOf("/"));
                        node.setAttrOutOfLoop(true);
                    } else if (tmp.equals(".")) {
                        node.isDot = true;
                    } else {
                        resultCol += "/" + tmp;
                    }
                }

                node.nodePath = resultCol;
            }

        }
    }

    /**
     * Parse the XML file. Buffer the result in LoopEntry.
     *
     * @param fileURL file URL
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    private String fileURL = null;

    private String charset = "UTF-8";

    public void parse(String fileURL, String charset) {
        this.fileURL = fileURL;
        this.charset = charset;
        task.start();
    }

    private java.io.InputStream is;

    public void parse(java.io.InputStream is, String charset) {
        this.is = is;
        this.charset = charset;
        task.start();
    }

    public Object call() throws Exception {
        Reader reader = null;
        try {
            DefaultHandler handler = null;
            if (nodesList.size() > 0) {
                SAXLoopCompositeHandler chd = new SAXLoopCompositeHandler();
                for (int i = 0; i < nodesList.size(); i++) {
                    XMLNodes ns = nodesList.get(i);
                    chd.register(new SimpleSAXLoopHandler(ns, multiCache));
                }
                handler = chd;
            } else {
                hd = new SimpleSAXLoopHandler(nodes, bcache);
                handler = hd;
            }

            SAXParser saxParser = null;
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setFeature(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING, Boolean.TRUE);
            if (ignoreDTD) { // orginal code
                spf.setFeature(LOAD_EXTERNAL_DTD, false);
                spf.setFeature(EXTERNAL_GENERAL_ENTITIES, false);
                spf.setFeature(EXTERNAL_PARAMETER_ENTITIES, false);
            }
            saxParser = spf.newSAXParser();
            saxParser.setProperty("http://xml.org/sax/properties/lexical-handler", handler);
            if (fileURL != null) {
                // routines.system.UnicodeReader.java is used to ignore the BOM of the source file.
                reader = new UnicodeReader(new java.io.FileInputStream(fileURL), this.charset);
                org.xml.sax.InputSource inSource = new org.xml.sax.InputSource(reader);
                saxParser.parse(inSource, handler);
            } else {
                reader = new UnicodeReader(is, this.charset);
                org.xml.sax.InputSource inSource = new org.xml.sax.InputSource(reader);
                saxParser.parse(inSource, handler);
            }
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } finally {
                if (multiCache != null) {
                    multiCache.notifyErrorOccurred();
                }
                if (bcache != null) {
                    bcache.notifyErrorOccurred();
                }

            }
        }
        return null;
    }

    public static void main(String[] args) {
        try {

            Runtime.getRuntime().gc();
            long start = Runtime.getRuntime().freeMemory();
            long startall = Runtime.getRuntime().maxMemory();
            long timeStart = System.currentTimeMillis();

            String file = "C:/Documents and Settings/Administrator/æ¡Œé�¢/in.xml";
            // String file = "D:/test/outMain.xml";
            String[] query =
                    new String[] { "cust-vendor-num", "cust-vendor-num" + "/@xsi:nil", "cust", "cust" + "/@xsi:nil" };
            boolean[] asXMLs = new boolean[] { false, false, false, false };
            String loopPath = "/orderdata/order/header";

            SimpleSAXLooper looper = new SimpleSAXLooper(loopPath, query, asXMLs);
            looper.parse(file, "ISO-8859-15");
            Iterator<Map<String, String>> iter = looper.iterator();
            long num = 0;
            while (iter.hasNext()) {
                Map<String, String> map = iter.next();
                System.out.println(map.get("cust-vendor-num") + "\t" + map.get("cust"));
            }

            System.out.println("==Taltal==" + num);

            System.out.println("==Time=" + (System.currentTimeMillis() - timeStart));

            System.out.println("==Memory start all =" + startall);
            System.out.println("==Memory start free =" + start);
            System.out.println("==Memory end all =" + Runtime.getRuntime().maxMemory());
            System.out.println("==Memory end free =" + Runtime.getRuntime().freeMemory());
            System.out.println("==Memory=" + (start - Runtime.getRuntime().freeMemory()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.xml.sax.commons.ISAXLooper#iterator()
     */
    public Iterator<Map<String, String>> iterator() {
        // TODO Auto-generated method stub
        return new SimpleSAXIterator(this.bcache);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.xml.sax.commons.ISAXLooper#multiIterator()
     */
    public Iterator<Map<String, Map<String, String>>> multiIterator() {
        return new SimpleSAXMultiLoopIterator(multiCache);
    }

    public void setIgnoreDTD(boolean ignoreDTD) {

        this.ignoreDTD = ignoreDTD;

    }

    public void stopRead() {
        if (hd != null) {
            hd.stopRead();
        }
    }

}
