// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
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
import java.util.List;
import java.util.Map;

import com.salesforce.soap.partner.QueryResult;
import com.salesforce.soap.partner.sobject.SObject;

public class TopQueryResult {

    // Level1 means "table1 join table2 join table3", only focus to iterate the
    // first table
    private List<TopRecord> allTopRecords = new ArrayList<TopRecord>();

    // return the final result, it normalize the "Multi-Rows" as one column.
    
    private List returnColumns = new ArrayList();

	private List returnLocalColumns = new ArrayList();
    
    public List<TopRecord> getAllTopRecords() {
        return allTopRecords;
    }

    public void processTopQueryResult(QueryResult qr) throws Exception {
        if (qr == null || qr.getRecords() == null) {
            return;
        }

        for (int i = 0; i < qr.getRecords().length; i++) {

            SObject record = qr.getRecords()[i];

            // transmit the rootType to each TopRecord
            TopRecord topRecord = new TopRecord(record.getType());

            topRecord.processSObject(record, record.getType());
            addNewColumns(this.returnColumns,topRecord.getColumnNameList());
            addNewColumns(this.returnLocalColumns,topRecord.getColumnLocalNameList());
            // buffer all the TopRecord in a List
            allTopRecords.add(topRecord);
        }

    }
    
    private void addNewColumns(List returnColumns,List recordColumns){
    	
    	for(Object columnName:recordColumns){
    		if(!returnColumns.contains(columnName)){
    			returnColumns.add(columnName);
    		}
    	}
    	
    }
    
    public List getReturnColumns() {
		return returnColumns;
	}

	public List getReturnLocalColumns() {
		return returnLocalColumns;
	}

    // print for debug
    public void printResult() {

        int counter = 0;
        for (TopRecord topRecord : allTopRecords) {
            counter++;

            // //support these 2 types ColumnName
            // System.out.println(topRecord.getValue("Account_Contact_LastName"));
            // System.out.println(topRecord.getValue("Contact_LastName"));

            List columnNameList = topRecord.getColumnNameList();
            Map valueMap = topRecord.getValueMap();

            // iterate the columnNameList, it "Keep the Order".
            for (Object columnName : columnNameList) {
                Object value = valueMap.get(columnName);
                System.out.println(counter + " : " + columnName + "---" + value);
            }

            System.out.println();
        }
    }
}
