// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.salesforce;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.axiom.om.OMElement;

import com.salesforce.soap.partner.QueryResult;
import com.salesforce.soap.partner.sobject.SObject;

public class TopRecord {

    // hold the rootType for coverting the column "Name" to "Account_Name" when
    // try to get the result in Map
    private String rootType = null;

    // Each TopQueryResult has itself columnName List, because sometimes they
    // are
    // differen't, it depend on the return XML, for the Node "xsi:nil=true",
    // there will
    // miss some columnName
    List columnNameList = new ArrayList();
    List columnLocalNameList = new ArrayList();

    Map valueMap = new HashMap();

    public TopRecord(String rootType) {
        this.rootType = rootType;
    }

    private void processQueryResult(QueryResult qr, String prefixName) throws Exception {
        for (int i = 0; i < qr.getRecords().length; i++) {
            SObject record = qr.getRecords()[i];
            processSObject(record, prefixName + TopConfig.COLUMNNAME_DELIMTER + record.getType());
        }
    }

    public void processSObject(SObject record, String prefixName) throws Exception {
        OMElement[] fields = record.getExtraElement();
        for (int j = 0; j < fields.length; j++) {
            processOMElement(fields[j], prefixName);
        }
    }

    private void processOMElement(OMElement ome, String prefixName) throws Exception {
        if (ome.getChildElements().hasNext()) {
            Iterator iter = ome.getChildElements();
            // delete the fixed id and type elements when find firstly
            int typeCount = 0;
            int idCount = 0;

            while (iter.hasNext()) {
                Object objectValue = iter.next();
                if (objectValue != null) {
                    if (objectValue instanceof OMElement) {
                        OMElement omeElem = (OMElement) objectValue;

                        if ("type".equals(omeElem.getLocalName()) && typeCount == 0) {
                            typeCount++;
                            continue;
                        }
                        if ("Id".equals(omeElem.getLocalName()) && idCount == 0) {
                            idCount++;
                            continue;
                        }

                        processOMElement(omeElem, prefixName + TopConfig.COLUMNNAME_DELIMTER + ome.getLocalName());
                    } else if (objectValue instanceof SObject) {
                        SObject sobject = (SObject) objectValue;
                        processSObject(sobject, prefixName + TopConfig.COLUMNNAME_DELIMTER + sobject.getType());
                    } else if (objectValue instanceof QueryResult) {
                        QueryResult queryResult = (QueryResult) objectValue;
                        processQueryResult(queryResult, prefixName);
                    } else {
                        throw new Exception("Unexcepted case happend...");
                    }
                }
            }
        } else {
            if (ome.getText() == null || "".equals(ome.getText())) {
                return;
            }
            String newPrefixName = prefixName + TopConfig.COLUMNNAME_DELIMTER + ome.getLocalName();
            // add the columnName to List one by one(order is important)
            if (!columnNameList.contains(newPrefixName)) {
                columnNameList.add(newPrefixName);
            }
            if(!columnLocalNameList.contains(ome.getLocalName())){
            	columnLocalNameList.add(ome.getLocalName());
            }
            Object value = valueMap.get(newPrefixName);
            if (value != null) {
                valueMap.put(newPrefixName, value + TopConfig.VALUE_DELIMITER + ome.getText());
            } else {
                valueMap.put(newPrefixName, ome.getText());
            }
        }
    }

    public Object getValue(String key) {
        Object value = null;

        // try directly: Name
        value = valueMap.get(key);

        if (value == null) {

            if (rootType != null) {
                // try again: Account_Name
                String fullKey = rootType + TopConfig.COLUMNNAME_DELIMTER + key;

                value = valueMap.get(fullKey);
            }
        }

        return value;
    }

    public List getColumnNameList() {
        return columnNameList;
    }
    
    public List getColumnLocalNameList() {
        return columnLocalNameList;
    }

    public Map getValueMap() {
        return valueMap;
    }

}
