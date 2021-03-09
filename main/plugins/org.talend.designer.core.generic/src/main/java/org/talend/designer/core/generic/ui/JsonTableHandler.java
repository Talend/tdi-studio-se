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
package org.talend.designer.core.generic.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.talend.designer.core.generic.i18n.Messages;
import org.talend.utils.json.JSONArray;
import org.talend.utils.json.JSONException;
import org.talend.utils.json.JSONObject;

/**
 * created by ycbai on 2016年9月5日 Detailled comment
 *
 */
public class JsonTableHandler {

    private static Logger log = Logger.getLogger(JsonTableHandler.class);

    private static JsonTableHandler handler = null;

    private static final String COLUMN_NAMES_KEY = "columnNames"; //$NON-NLS-1$

    private static final String DATA_KEY = "data"; //$NON-NLS-1$

    private JsonTableHandler() {
    }

    public synchronized static JsonTableHandler getInstance() {
        if (handler == null) {
            handler = new JsonTableHandler();
        }
        return handler;
    }

    public JsonTableVO parse(String jsonStr) {
        JsonTableVO vo = null;
        try {
            JSONObject rootObj = new JSONObject(jsonStr);
            List<String> columnTitles = getColumnNames(rootObj);
            AtomicInteger maxColumnsCount = new AtomicInteger();
            List<List<Object>> dataRowList = getDataRowList(rootObj, maxColumnsCount);
            if (columnTitles.size() == 0) {
                columnTitles = getColumnTitles(maxColumnsCount.get());
            }
            vo = buildVO(columnTitles, dataRowList);
        } catch (Exception e) {
            log.warn(Messages.getString("JsonTableHandler.parse.warnMsg", jsonStr), e); //$NON-NLS-1$
        }
        return vo;
    }

    private JsonTableVO buildVO(List<String> columnTitles, List<List<Object>> dataRowList) {
        JsonTableVO vo = new JsonTableVO();
        vo.setTitles(columnTitles);
        List<Map<String, Object>> data = new ArrayList<>();
        for (List<Object> rowList : dataRowList) {
            Map<String, Object> cellMap = new HashMap<>();
            for (int i = 0; i < rowList.size(); i++) {
                if (i > columnTitles.size() - 1) {
                    continue;
                }
                cellMap.put(columnTitles.get(i), rowList.get(i));
            }
            data.add(cellMap);
        }
        vo.setData(data);
        return vo;
    }

    private List<String> getColumnNames(JSONObject rootObj) {
        List<String> titles = new ArrayList<>();
        try {
            JSONArray jsonArr = rootObj.getJSONArray(COLUMN_NAMES_KEY);
            for (int i = 0; i < jsonArr.length(); i++) {
                titles.add(jsonArr.getString(i));
            }
        } catch (JSONException e) {
            // Ignore it.
        }
        return titles;
    }

    private List<String> getColumnTitles(int maxColumnsNum) {
        String columnNamePrefix = "Column "; //$NON-NLS-1$
        List<String> titles = new ArrayList<>();
        for (int i = 0; i < maxColumnsNum; i++) {
            titles.add(columnNamePrefix.concat(i + 1 + "")); //$NON-NLS-1$
        }
        return titles;
    }

    private List<List<Object>> getDataRowList(JSONObject rootObj, AtomicInteger columnsCount) throws JSONException {
        List<List<Object>> rows = new ArrayList<>();
        AtomicInteger count = columnsCount;
        if (count == null) {
            count = new AtomicInteger();
        }
        JSONArray jsonArr = rootObj.getJSONArray(DATA_KEY);
        for (int i = 0; i < jsonArr.length(); i++) {
            List<Object> row = new ArrayList<>();
            JSONArray rowArray = jsonArr.getJSONArray(i);
            for (int j = 0; j < rowArray.length(); j++) {
                row.add(rowArray.get(j));
            }
            if (row.size() > count.get()) {
                count.set(row.size());
            }
            rows.add(row);
        }
        return rows;
    }

}
