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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

/**
 * DOC s class global comment. Detailled comment
 *
 * $Id: SAXLoopHandler.java,v 1.1 2008/03/21 07:20:39 xzhang Exp $
 */
public class SAXLoopHandler extends DefaultHandler2 {

    //
    private String loopPath = null;

    private String subLoopPath = null;

    private int subLoopCount = 0;

    private List<String> loopCols = new ArrayList<String>();

    private List<String> selectColumns = new ArrayList<String>();

    private List<Boolean> asXMLs = null;

    private List<Boolean> isDots = null;

    private String currentPath = "";

    // current element is include in looppath
    private boolean isLooping = false;

    // is read the text value
    private boolean[] outputTexts = null;

    private Row currentRow = null;

    // to mark the value is set or not
    private boolean[] currentRowHaveValue = null;

    private LoopEntry entry;

    List<Map<String, Object>> listArgs = new ArrayList<Map<String, Object>>();

    // ===============add for bug7632 begin======================================
    private ComplexSAXLooper saxLooper;

    // ========================end===============================================

    public SAXLoopHandler(LoopEntry entry) {
        this.entry = entry;
        this.loopPath = entry.getLoop();
        this.loopCols = entry.getPaths();
        this.asXMLs = entry.getAsXMLs();

        this.isDots = entry.getIsDots();

        outputTexts = new boolean[loopCols.size()];

        if (entry.getSubLoop() != null) {
            this.subLoopPath = entry.getSubLoop().getLoop();
        } else {
            this.subLoopPath = loopPath;
        }
    }

    public SAXLoopHandler(ComplexSAXLooper saxLooper, LoopEntry entry) {
        this(entry);
        this.saxLooper = saxLooper;
    }

    /**
     *
     *
     * @param loopPath
     * @param loopCols
     * @param subLoopPath
     */
    public SAXLoopHandler(String loopPath, List<String> loopCols, String subLoopPath) {
        this.loopPath = loopPath;
        this.subLoopPath = subLoopPath;
        this.loopCols = loopCols;
    }

    public void startDocument() throws SAXException {
    }

    public void endDocument() throws SAXException {
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        // System.out.println("startElement");

        String loopPath = this.loopPath;
        String subLoopPath = this.subLoopPath;
        selectColumns.clear();

        if (loopPath.indexOf("*") != -1) {
            loopPath = this.loopPath.replace("*", qName);
            subLoopPath = this.subLoopPath.replace("*", qName);
            for (String loopCol : this.loopCols) {
                if (loopCol.indexOf("@") != -1) {
                	selectColumns.add(loopCol.replace("*", qName));
                } else if (currentPath.length() > 0) {
                    if (this.loopPath.indexOf("*") < this.loopPath.length() - 1) {
                        String parent = currentPath.substring(currentPath.lastIndexOf("/") + 1);
                        selectColumns.add(loopCol.replace("*", parent));
                    } else if (this.loopPath.indexOf("*") == this.loopPath.length() - 1) {
                    	selectColumns.add(loopCol.replace("*", qName));
                    }
                }
            }
        } else {
        	selectColumns.addAll(this.loopCols);
        }

        currentPath += "/" + qName;
        if (currentPath.equals(loopPath)) {
            isLooping = true;
            subLoopCount = 0;
            currentRow = new Row(this.loopCols.size());
            currentRowHaveValue = new boolean[this.loopCols.size() + 1];
        }
        if (isLooping) {
            // count sub loop number
            if (currentPath.equals(subLoopPath)) {
                subLoopCount++;
            }
            // paser the attributes
            for (int i = 0; i < selectColumns.size(); i++) {
                String column = selectColumns.get(i);
                boolean asXML = this.asXMLs.get(i);
                boolean isDot = this.isDots.get(i);
                outputTexts[i] = false;

                if (asXML && (currentPath.equals(column) || currentPath.startsWith(column + "/"))) {
                    currentRow.addTextValue(i, "<");
                    currentRow.addTextValue(i, qName);
                    if (attributes.getLength() > 0) {
                        for (int m = 0; m < attributes.getLength(); m++) {
                            currentRow.addTextValue(i, " ");
                            currentRow.addTextValue(i, attributes.getQName(m));
                            currentRow.addTextValue(i, "=");
                            currentRow.addTextValue(i, "\"");
                            currentRow.addTextValue(i, escapeEntityHelper.escapeAttributeEntities(attributes.getValue(m)));
                            currentRow.addTextValue(i, "\"");
                        }
                    }
                    outputTexts[i] = true;
                    currentRowHaveValue[i] = false;
                    currentRow.addTextValue(i, ">");
                } else if (isDot && (currentPath.equals(column) || currentPath.startsWith(column + "/"))) {
                	outputTexts[i] = true;
                    currentRowHaveValue[i] = false;
                } else {
                    int index = column.lastIndexOf("@");
                    if (index > 0) {
                        if (currentPath.equals(column.substring(0, index - 1))) {
                            String attribute = attributes.getValue(column.substring(index + 1));
                            if (attribute != null && false == currentRowHaveValue[i]) {
                            	currentRow.addTextValue(i, attribute);
                                currentRowHaveValue[i] = true;
                            }
                        }
                    } else {

                        if (currentPath.equals(column)) {
                        	outputTexts[i] = true;
                        }
                    }
                }
            }
            // loop has function, then add the attribute to the args
            if (this.entry.hasFunctions()) {

                Map<String, Object> map = new HashMap<String, Object>();
                map.put("loopPath", loopPath);
                map.put("column", currentPath);
                map.put("value", attributes);
                listArgs.add(map);

            }

        }
    }

    /**
     * Deal with the bug 4402: the content <quotes_encoded>&quot;hello world&quot;</quotes_encoded>. In this case,
     * "characters" will be called three time.
     *
     * first: "
     *
     * second: hello world
     *
     * third: "
     */
    public void characters(char ch[], int start, int length) throws SAXException {
        // System.out.println(new String(ch, start, length) + " | " + isLooping);
        if (isLooping) {
            String text = new String(ch, start, length);
            // System.out.println(text);
            if (text.length() > 0) {
            	for (int i = 0; i < selectColumns.size(); i++) {
	                if (outputTexts[i] && !currentRowHaveValue[i]) {
	                	if(this.asXMLs.get(i) && !inCDATA) {
	                    	text = escapeEntityHelper.escapeElementEntities(text);
	                    }
	                	currentRow.addTextValue(i, text);
	                }
            	}

                /**
                 * when the loop has functions, add the value of the element to the args
                 */
                if (this.entry.hasFunctions()) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("loopPath", loopPath);
                    map.put("column", currentPath);
                    map.put("value", text);
                    listArgs.add(map);
                }

            }
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        // System.out.println("endElement");

        String loopPath = this.loopPath;
        if (loopPath.indexOf("*") != -1) {
            loopPath = this.loopPath.replace("*", qName);
        }

        if (isLooping) {
        	for (int i = 0; i < selectColumns.size(); i++) {
        		if(outputTexts[i]) {
        			if(!currentRowHaveValue[i]) {
        				currentRow.addTextValue(i, "");
        			}
        			currentRowHaveValue[i] = true;
        		}
        		String column = selectColumns.get(i);
        		boolean asXML = this.asXMLs.get(i);
        		boolean isDot = this.isDots.get(i);
        		if ((asXML || isDot) && (currentPath.equals(column) || currentPath.startsWith(column + "/"))) {
        			if(asXML) {
        				currentRow.addTextValue(i, "</");
        				currentRow.addTextValue(i, qName);
        				currentRow.addTextValue(i, ">");

        			}
        			if(this.currentPath.equals(column)) {
        				currentRowHaveValue[i] = true;
        			}
        		}

        		outputTexts[i] = false;
        	}
        }

        if (currentPath.equals(loopPath)) {
            isLooping = false;

            /**
             * put the the result of the fuctions to the column
             */
            if (this.entry.hasFunctions()) {
                entry.execFunctions(listArgs);

                List<Map<String, String>> results = entry.getFunctionResults();
                for (int i = 0; i < results.size(); i++) {
                    Map<String, String> map = results.get(i);
                    String strKey = map.get("name");
                    int index = this.loopCols.indexOf(strKey);
                    if (index >= 0 && currentRowHaveValue[index] == false) {
                        currentRow.addTextValue(index, map.get("value"));
                        currentRowHaveValue[index] = true;
                    }
                }

            }

            // for (String value : currentRow) {
            // System.out.print("|" + value);
            // }
            // System.out.println();
            String[] rowResult = currentRow.getStringResult();
            rowResult[rowResult.length - 1] = Integer.toString(subLoopCount);
            entry.getRows().add(rowResult);
            // ===========add for bug7632==========================
            if (this.entry.getOriginalLoopPath() != null) {
                this.saxLooper.addLoopOrder(this.entry.getOriginalLoopPath());
            }
            // =================end================================
            reset();
        }
        currentPath = currentPath.substring(0, currentPath.lastIndexOf("/"));
    }

    private void reset() {
    	for(int i=0;i<outputTexts.length;i++) {
    		outputTexts[i] = false;
    	}
    }

    /** Flag used to indicate that we are inside a CDATA section */
    private boolean inCDATA;

    private EscapeEntityHelper escapeEntityHelper = new EscapeEntityHelper();

	public void startCDATA() throws SAXException {
		inCDATA = true;
		if(isLooping) {
			for (int i = 0; i < selectColumns.size(); i++) {
	            if (this.asXMLs.get(i)) {
	                if (outputTexts[i] && !currentRowHaveValue[i]) {
	                	currentRow.addTextValue(i, "<![CDATA[");
	                }
	            }
	        }
		}
	}

	public void endCDATA() throws SAXException {
		inCDATA = false;
		if(isLooping) {
			for (int i = 0; i < selectColumns.size(); i++) {
	            if (this.asXMLs.get(i)) {
	                if (outputTexts[i] && !currentRowHaveValue[i]) {
	                	currentRow.addTextValue(i, "]]>");
	                }
	            }
	        }
		}
	}

	class Row {

		String[] values = null;
		StringBuffer[] stringBuffers = null;
		boolean[] isNullValue = null;

		Row(int columnCount) {
			values = new String[columnCount+1];

			stringBuffers = new StringBuffer[columnCount];
			for(int i=0;i<stringBuffers.length;i++) {
				stringBuffers[i] = new StringBuffer();
			}

			isNullValue = new boolean[columnCount];
			for(int i=0;i<isNullValue.length;i++) {
				isNullValue[i] = true;
			}

		}

		void addTextValue(int columnIndex,String appendValue) {
			isNullValue[columnIndex] = false;

			if(appendValue!=null) {
				stringBuffers[columnIndex].append(appendValue);
			}
		}

		String[] getStringResult() {
			for(int i=0;i<isNullValue.length;i++) {
				if(!isNullValue[i]) {
					values[i] = stringBuffers[i].toString();
				}
			}
			return values;
		}


	}
}
