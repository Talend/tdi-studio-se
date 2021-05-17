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
package org.talend.xml.sax;

import java.util.Iterator;
import java.util.Map;

import org.talend.xml.sax.commons.ISAXLooper;
import org.talend.xml.sax.simpleparser.SimpleSAXLooper;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class SAXLooper {

    // loop path in the tFileInputXML component.
    private String loopPath;

    // node paths special which tab will be read as the row value
    private String[] nodePaths;

    // add to support node.asXML()
    private boolean[] asXMLs;

    private String rootPath = null;

    // private String[] arrLoopPath = null;

    private String[][] arrNodePaths = null;

    private String[] arrOrigLoopPath = null;

    private boolean isSimpleParse = true;

    private ISAXLooper looper = null;

    public SAXLooper(String loopPath, String[] nodePaths) {
        this.loopPath = loopPath;
        this.nodePaths = nodePaths;
        this.asXMLs = new boolean[this.nodePaths.length];
        judgeIsSimple();
    }

    public SAXLooper(String loopPath, String[] nodePaths, boolean[] asXMLs) {
        this.loopPath = loopPath;
        this.nodePaths = nodePaths;
        this.asXMLs = asXMLs;
        judgeIsSimple();
    }

    public SAXLooper(String rootPath, String[] arrLoopPath, String[][] arrNodePaths) {
        this.rootPath = rootPath;

        // this.arrLoopPath = new String[arrLoopPath.length];

        this.arrOrigLoopPath = arrLoopPath;

        this.arrNodePaths = arrNodePaths;

        judegeMultiIsSimple();
    }

    private void judgeIsSimple() {
        isSimpleParse = isSimplePath(loopPath);

        for(String path : nodePaths) {
            if(!isSimpleParse) {
                break;
            }
            isSimpleParse = isSimplePath(path);
        }

        if (this.isSimpleParse) {
            looper = new SimpleSAXLooper(loopPath, nodePaths, asXMLs);
        } else {
            looper = new ComplexSAXLooper(loopPath, nodePaths, asXMLs);
        }
    }

    private void judegeMultiIsSimple() {
        isSimpleParse = isSimplePath(rootPath);

        for(String path : arrOrigLoopPath) {
            if(!isSimpleParse) {
                break;
            }
            isSimpleParse = isSimplePath(path);
        }

        for(String[] arrNodePath : arrNodePaths) {
            for(String path : arrNodePath) {
                if(!isSimpleParse) {
                    break;
                }
                isSimpleParse = isSimplePath(path);
            }
            if(!isSimpleParse) {
                break;
            }
        }

        if (this.isSimpleParse) {
            looper = new SimpleSAXLooper(rootPath, arrOrigLoopPath, arrNodePaths);
        } else {
            looper = new ComplexSAXLooper(rootPath, arrOrigLoopPath, arrNodePaths);
        }
    }

    /**
     * ../../../@attr also can read like a stream(now only consider the case)
     * @param path
     * @return
     */
    private boolean isSimplePath(String path) {
        if(path == null || !path.contains("..") && !path.contains("*")) {
            return true;
        }

        boolean isSimplePath = true;

        String[] nodes = path.split("/");

        for(int i = 0;i<nodes.length;i++) {
            String node = nodes[i];

            if(i < (nodes.length - 1)) {
                isSimplePath = isSimplePath && "..".equals(node);
            } else {
                isSimplePath = isSimplePath && node.startsWith("@");
            }
        }

        return isSimplePath;

    }

    private String charset = "UTF-8";

    /**
     *
     * DOC WLIU set the encoding of the parser, it doesn't work if the source is a character stream
     *
     * @param charset
     */
    public void setEncoding(String charset) {
        this.charset = charset;
    }

    public void parse(String fileName) {
        looper.parse(fileName, charset);
    }

    public void parse(java.io.InputStream is) {
        looper.parse(is, charset);
    }

    public Iterator<Map<String, String>> iterator() {
        return looper.iterator();
    }

    public java.util.Iterator<java.util.Map<String, java.util.Map<String, String>>> multiIterator() {
        return looper.multiIterator();
    }

	public void setIgnoreDTD(boolean ignoreDTD) {
		this.looper.setIgnoreDTD(ignoreDTD);
	}

    /**
     * handle the exception in task.
     * @throws Exception
     */
    public void handleTaskResponse() throws Exception {
    	if(this.isSimpleParse) {
    		SimpleSAXLooper ssl = (SimpleSAXLooper)this.looper;
    		ssl.handleException();
    	}
    }

    /**
     * stop the read action asap
     *
     */
    public void stopRead() {
        if(this.isSimpleParse) {
            SimpleSAXLooper ssl = (SimpleSAXLooper)this.looper;
            ssl.stopRead();
        }
    }

    public static void main(String args[]) {

        try {

            Runtime.getRuntime().gc();
            long start = Runtime.getRuntime().freeMemory();
            long startall = Runtime.getRuntime().maxMemory();
            long timeStart = System.currentTimeMillis();

            String file = "./libs_src/TalendSAX/org/talend/xml/sax/in_2.xml";
            // String file = "D:/test/outMain.xml";
            String[] query = new String[] { "." };
            boolean[] asXMLs = new boolean[] { false };
            String loopPath = "/root/*";

            SAXLooper looper = new SAXLooper(loopPath, query, asXMLs);
            looper.parse(file);
            Iterator<Map<String, String>> iter = looper.iterator();
            long num = 0;
            while (iter.hasNext()) {
                num++;
                Map<String, String> map = iter.next();
                for (int i = 0; i < query.length; i++) {
                    System.out.print(map.get(query[i]) + "#");
                }
                System.out.println();
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
}
