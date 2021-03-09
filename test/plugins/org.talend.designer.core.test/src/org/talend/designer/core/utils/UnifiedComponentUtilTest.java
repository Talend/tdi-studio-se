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
package org.talend.designer.core.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Test;
import org.talend.designer.core.model.components.UnifiedJDBCBean;

/**
 * DOC jding  class global comment. Detailled comment
 */
public class UnifiedComponentUtilTest {

    private static List<String> dbDisplayName = Arrays.asList(new String[] { "Test DB", "Test DB R" });

    @Test
    public void testLoadAdditionalJDBC() {
        String jsonContent = getMokeAdditionalJDBCJson();
        InputStream in = new ByteArrayInputStream(jsonContent.getBytes());
        Map<String, UnifiedJDBCBean> loadAdditionalJDBC = UnifiedComponentUtil.loadAdditionalJDBC(in);
        for (String key : dbDisplayName) {
            assertTrue(key + " didn't load properly", loadAdditionalJDBC.containsKey(key));
            UnifiedJDBCBean bean = loadAdditionalJDBC.get(key);
            assertNotNull(bean.getDatabaseId());
            assertNotNull(bean.getDisplayName());
            assertNotNull(bean.getDriverClass());
            assertNotNull(bean.getPaths());
            assertTrue(bean.getPaths().size()>0);

            assertNotNull(bean.getExcludeDefinitions());
            assertNotNull(bean.getParameterConfigurations());
        }
        UnifiedJDBCBean testdbBean = loadAdditionalJDBC.get("Test DB");
        assertTrue(testdbBean.getExcludeDefinitions().size() > 0);
        assertTrue(testdbBean.getParameterConfigurations().size() > 0);
        testdbBean = loadAdditionalJDBC.get("Test DB R");
        assertTrue(testdbBean.getExcludeDefinitions().isEmpty());
        assertTrue(testdbBean.getParameterConfigurations().isEmpty());

        String excludeCompName = "tJDBCSP";
        assertTrue(UnifiedComponentUtil.isUnsupportedComponent(excludeCompName, loadAdditionalJDBC.get("Test DB")));
        assertFalse(UnifiedComponentUtil.isUnsupportedComponent(excludeCompName, loadAdditionalJDBC.get("Test DB R")));
    }


    @AfterClass
    public static void afterClass() {
        Map<String, UnifiedJDBCBean> additionalJDBCMap = UnifiedComponentUtil.getAdditionalJDBC();
        for (String key : dbDisplayName) {
            additionalJDBCMap.remove(key);
        }
    }

    private String getMokeAdditionalJDBCJson() {
        /**
         * [
         * { "id" : "TEST_DB", "displayName" : "Test DB", "class" : "com.simba.spark.jdbc.Driver", "url" :
         * "jdbc:spark://", "paths" : [ {"path" : "mvn:Spark/SparkJDBC42/2.6.14.1018/jar"} ], "excludes": [{
         * "component": "tJDBCSP" }, { "component": "tJDBCCommit" }, { "component": "tJDBCRollback" } ],
         * "configuration": [{ "component": "tTestDBConnection", "parameters": [{ "name": "use_autocommit", "value":
         * "true" }, { "name": "autocommit", "value": "true" } ] } ]
         * }, { "id" : "TEST_DB_R", "displayName" : "Test DB R", "class" : "com.simba.spark.jdbc.Driver", "url" :
         * "jdbc:spark://", "paths" : [ {"path" : "mvn:Spark/SparkJDBC42/2.6.14.1018/jar"} ]
         * }
         * ]
         */
        String jsonContent = "[\r\n" + "\r\n" + "    {\r\n" + "        \"id\" : \"TEST_DB\",\r\n"
                + "        \"displayName\" : \"Test DB\",\r\n" + "        \"class\" : \"com.simba.spark.jdbc.Driver\",\r\n"
                + "        \"url\" : \"jdbc:spark://\",\r\n" + "        \"paths\" : \r\n" + "        [\r\n"
                + "            {\"path\" : \"mvn:Spark/SparkJDBC42/2.6.14.1018/jar\"}\r\n" + "        ],\r\n"
                + "        \"excludes\": [{\r\n" + "                \"component\": \"tJDBCSP\"\r\n" + "            },\r\n"
                + "            {\r\n" + "                \"component\": \"tJDBCCommit\"\r\n" + "            },\r\n"
                + "            {\r\n" + "                \"component\": \"tJDBCRollback\"\r\n" + "            }\r\n"
                + "        ],\r\n" + "        \"configuration\": [{\r\n"
                + "                \"component\": \"tTestDBConnection\",\r\n" + "                \"parameters\": [{\r\n"
                + "                        \"name\": \"use_autocommit\",\r\n" + "                        \"value\": \"true\"\r\n"
                + "                    },\r\n" + "                    {\r\n"
                + "                        \"name\": \"autocommit\",\r\n" + "                        \"value\": \"true\"\r\n"
                + "                    }\r\n" + "                ]\r\n" + "            }\r\n" + "        ]\r\n" + "    \r\n"
                + "    },  {\r\n" + "        \"id\" : \"TEST_DB_R\",\r\n" + "        \"displayName\" : \"Test DB R\",\r\n"
                + "        \"class\" : \"com.simba.spark.jdbc.Driver\",\r\n" + "        \"url\" : \"jdbc:spark://\",\r\n"
                + "        \"paths\" : \r\n" + "        [\r\n"
                + "            {\"path\" : \"mvn:Spark/SparkJDBC42/2.6.14.1018/jar\"}\r\n" + "        ]\r\n" + "    \r\n"
                + "    }\r\n" + "\r\n" + "]";
        return jsonContent;
    }

}
