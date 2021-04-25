/**
 * Copyright (C) 2006-2021 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.talend.servicenow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

public class Util {

    private HttpClient client;

    private String baseurl;

    public Util(HttpClient client, String baseurl) {
        this.client = client;
        this.baseurl = baseurl;
    }

    private Map<String, String> typeMapping = new HashMap<String, String>();

    {
        typeMapping.put("string", "id_String");
        typeMapping.put("boolean", "id_Boolean");
        typeMapping.put("integer", "id_Integer");
        typeMapping.put("decimal", "id_BigDecimal");
        typeMapping.put("float", "id_Float");
        typeMapping.put("glide_date_time", "id_Date");
        typeMapping.put("glide_date", "id_Date");
        typeMapping.put("glide_time", "id_Date");
    }

    public static class ColumnMetadata {

        private String name;

        private String type;

        private int maxlength;

        private String pattern;

        public ColumnMetadata(String name, String type, int maxlength, String pattern) {
            this.name = name;
            this.type = type;
            this.maxlength = maxlength;
            this.pattern = pattern;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public int getMaxLength() {
            return maxlength;
        }

        public String getPattern() {
            return pattern;
        }

        public String toString() {
            return name + ":" + type + ":" + maxlength;
        }
    }

    public Map<String, ColumnMetadata> getMetadata(String tablename) throws ClientProtocolException, IOException {
        Map<String, ColumnMetadata> result = new HashMap<String, ColumnMetadata>();
        List<String> relation = getRelationship(tablename);

        StringBuilder sb = new StringBuilder();
        for (String tname : relation) {
            sb.setLength(0);
            sb.append(this.baseurl);
            sb.append("/api/now/table/");
            sb.append("sys_dictionary");
            sb.append("?sysparm_exclude_reference_link=true");
            sb.append("&sysparm_query=name=");
            sb.append(tname);
            sb.append("&sysparm_fields=element,internal_type,max_length,active");

            HttpGet httpget = new HttpGet(sb.toString());
            httpget.setHeader("Accept", "application/json");

            HttpResponse response = this.client.execute(httpget);
            List<Map<String, String>> info = extractResponse4MultiRowFromArray(response);

            for (Map<String, String> row : info) {
                String element = row.get("element");
                boolean active = Boolean.parseBoolean(row.get("active"));
                if (element != null && !element.isEmpty() && active) {
                    String talend_type = null;
                    String pattern = null;

                    String servicenow_type = (String) row.get("internal_type");

                    if (servicenow_type != null && !servicenow_type.isEmpty()) {
                        talend_type = typeMapping.get(servicenow_type);

                        //need date pattern
                        if ("glide_date_time".equals(servicenow_type)) {
                            pattern = "yyyy-MM-dd HH:mm:ss";
                        } else if ("glide_date".equals(servicenow_type)) {
                            pattern = "yyyy-MM-dd";
                        } else if ("glide_time".equals(servicenow_type)) {
                            pattern = "HH:mm:ss";
                        }
                    }

                    if (talend_type == null) {
                        talend_type = "id_String";
                    }

                    int mlength = 64;
                    String maxlength = (String) row.get("max_length");
                    if (maxlength != null && !maxlength.isEmpty()) {
                        mlength = Integer.parseInt(maxlength);
                    }

                    ColumnMetadata column = new ColumnMetadata(element, talend_type, mlength, pattern);
                    result.put(element, column);
                }
            }
        }

        return result;
    }

    private List<String> getRelationship(String tablename) throws ClientProtocolException, IOException {
        LinkedList<String> result = new LinkedList<String>();

        StringBuilder sb = new StringBuilder();
        sb.append(this.baseurl);
        sb.append("/api/now/table/");
        sb.append("sys_db_object");
        sb.append("?sysparm_exclude_reference_link=true");
        sb.append("&sysparm_query=name=");
        sb.append(tablename);
        sb.append("&sysparm_fields=name,super_class");

        HttpGet httpget = new HttpGet(sb.toString());
        httpget.setHeader("Accept", "application/json");

        HttpResponse response = this.client.execute(httpget);
        Map<String, String> info = extractResponse4OneRowFromArray(response);

        result.add(info.get("name"));

        String superclass = info.get("super_class");
        while (superclass != null && !superclass.isEmpty()) {
            sb.setLength(0);
            sb.append(this.baseurl);
            sb.append("/api/now/table/");
            sb.append("sys_db_object/");
            sb.append(superclass);
            sb.append("?sysparm_exclude_reference_link=true");
            sb.append("&sysparm_fields=name,super_class");

            httpget = new HttpGet(sb.toString());
            httpget.setHeader("Accept", "application/json");

            response = this.client.execute(httpget);
            info = extractResponse4OneRowFromObject(response);

            result.add(info.get("name"));

            superclass = info.get("super_class");
        }

        Collections.reverse(result);

        return result;
    }

    private Map<String, String> extractResponse4OneRowFromArray(HttpResponse response)
            throws ParseException, IOException {
        validateResponse(response);

        Map<String, String> result = new HashMap<String, String>();

        org.json.JSONArray array = (org.json.JSONArray) getResult(response);

        for (int i = 0; i < array.length(); i++) {
            org.json.JSONObject row = (org.json.JSONObject) array.get(i);
            for (String key : row.keySet()) {
                result.put(key, (String) row.get(key));
            }
        }

        return result;
    }

    private Map<String, String> extractResponse4OneRowFromObject(HttpResponse response)
            throws ParseException, IOException {
        validateResponse(response);

        Map<String, String> result = new HashMap<String, String>();

        org.json.JSONObject object = (org.json.JSONObject) getResult(response);

        for (String key : object.keySet()) {
            result.put(key, (String) object.get(key));
        }

        return result;
    }

    private List<Map<String, String>> extractResponse4MultiRowFromArray(HttpResponse response)
            throws ParseException, IOException {
        validateResponse(response);

        List<Map<String, String>> result = new ArrayList<Map<String, String>>();

        org.json.JSONArray array = (org.json.JSONArray) getResult(response);

        for (int i = 0; i < array.length(); i++) {
            Map<String, String> element = new HashMap<String, String>();
            org.json.JSONObject row = (org.json.JSONObject) array.get(i);
            for (String key : row.keySet()) {
                element.put(key, (String) row.get(key));
            }
            result.add(element);
        }

        return result;
    }

    private void validateResponse(HttpResponse response) {
        if (response.getStatusLine().getStatusCode() != 200) {
            throw new RuntimeException(
                    "Fail to get the table metadata. The operation has returned the code : " + response.getStatusLine()
                            + ".");
        }
    }

    private Object getResult(HttpResponse response) throws ParseException, IOException {
        String responseBody = org.apache.http.util.EntityUtils.toString(response.getEntity());
        org.json.JSONObject json = new org.json.JSONObject(responseBody);
        return json.get("result");
    }
}