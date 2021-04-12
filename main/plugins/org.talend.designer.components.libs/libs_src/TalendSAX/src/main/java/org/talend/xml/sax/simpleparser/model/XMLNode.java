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
package org.talend.xml.sax.simpleparser.model;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class XMLNode {

    public boolean isLooping = false;

    public boolean hasValue = false;

    public boolean outputText = false;

    public String originLoopPath;

    public String loopPath;

    public String nodePath;

    public String originPath;

    public boolean isAsXML = false;

    public boolean isDot = false;

    private boolean isAttrOutOfLoop = false;

    private String attrValueOutOfLoop = "";

    public void setAttrOutOfLoop(boolean isAttrOutOfLoop) {
        this.isAttrOutOfLoop = isAttrOutOfLoop;
    }

    private StringBuffer value = new StringBuffer();
    private boolean isNullValue = true;

    public XMLNode(String loopPath, String originPath, String nodePath, boolean isAsXML, boolean isDot) {
        this.loopPath = loopPath;
        this.nodePath = nodePath;
        this.originPath = originPath;
        this.isAsXML = isAsXML;
        this.isDot = isDot;
    }

    public XMLNode(String loopPath, String originPath, String nodePath, boolean isAsXML) {
        this(loopPath, originPath, nodePath, isAsXML, false);
    }

    public XMLNode(String loopPath, String originPath, String nodePath) {
        this(loopPath, originPath, nodePath, false, false);
    }

    public XMLNode(String originLoopPath,String loopPath, String originPath, String nodePath) {
        this(loopPath, originPath, nodePath, false, false);
        this.originLoopPath = originLoopPath;
    }

    public void addTextValue(String appendValue) {
        if(isAttrOutOfLoop) {
            attrValueOutOfLoop = appendValue;
            return;
        }

    	isNullValue = false;

        if (appendValue != null) {
        	value.append(appendValue);
        }
    }

    public String getTextValue() {
        if(isAttrOutOfLoop) {
            return attrValueOutOfLoop;
        }

    	if(isNullValue) return null;
    	return value.toString();
    }

    public void resetValue() {
        if(isAttrOutOfLoop) {
            return;
        }

    	isNullValue = true;
    	value.setLength(0);
    }

}
