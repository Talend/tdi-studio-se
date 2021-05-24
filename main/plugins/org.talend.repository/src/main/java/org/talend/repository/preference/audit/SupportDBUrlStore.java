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
package org.talend.repository.preference.audit;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

/**
 * created by hcyi on May 29, 2018 Detailled comment
 *
 * This class store the all the audit support database connection url.
 *
 */
public final class SupportDBUrlStore {

    public static final String EMPTY_STRING = ""; //$NON-NLS-1$

    private static final Properties PROP = new Properties();

    private static Map<String, SupportDBUrlType> supportDBUrlMap = new HashMap<String, SupportDBUrlType>();

    private static Map<String, String> displayNameMap = new TreeMap<String, String>();

    private static SupportDBUrlStore dbUrlStore = new SupportDBUrlStore();

    public static SupportDBUrlStore getInstance() {
        return dbUrlStore;
    }

    private SupportDBUrlStore() {
        loadProperties();
        fillDbUrlMap();
    }

    private void loadProperties() {
        InputStream in = SupportDBUrlStore.class.getResourceAsStream("dburl.properties"); //$NON-NLS-1$
        try {
            PROP.load(in);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fillDbUrlMap() {
        supportDBUrlMap.put(SupportDBUrlType.H2LOCALDEFAULTURL.getDBKey(), SupportDBUrlType.H2LOCALDEFAULTURL);
        supportDBUrlMap.put(SupportDBUrlType.H2REMOTEDEFAULTURL.getDBKey(), SupportDBUrlType.H2REMOTEDEFAULTURL);
        supportDBUrlMap.put(SupportDBUrlType.MYSQLDEFAULTURL.getDBKey(), SupportDBUrlType.MYSQLDEFAULTURL);
        supportDBUrlMap.put(SupportDBUrlType.ORACLEDEFAULTURL.getDBKey(), SupportDBUrlType.ORACLEDEFAULTURL);
        supportDBUrlMap.put(SupportDBUrlType.MSSQLDEFAULTURL.getDBKey(), SupportDBUrlType.MSSQLDEFAULTURL);
        supportDBUrlMap.put(SupportDBUrlType.MARIADBDEFAULTURL.getDBKey(), SupportDBUrlType.MARIADBDEFAULTURL);
        supportDBUrlMap.put(SupportDBUrlType.POSTGRESQLEFAULTURL.getDBKey(), SupportDBUrlType.POSTGRESQLEFAULTURL);
        displayNameMap.put(SupportDBUrlType.H2LOCALDEFAULTURL.getDBKey(), SupportDBUrlType.H2LOCALDEFAULTURL.getDisplayName());
        displayNameMap.put(SupportDBUrlType.H2REMOTEDEFAULTURL.getDBKey(), SupportDBUrlType.H2REMOTEDEFAULTURL.getDisplayName());
        displayNameMap.put(SupportDBUrlType.MYSQLDEFAULTURL.getDBKey(), SupportDBUrlType.MYSQLDEFAULTURL.getDisplayName());
        displayNameMap.put(SupportDBUrlType.ORACLEDEFAULTURL.getDBKey(), SupportDBUrlType.ORACLEDEFAULTURL.getDisplayName());
        displayNameMap.put(SupportDBUrlType.MSSQLDEFAULTURL.getDBKey(), SupportDBUrlType.MSSQLDEFAULTURL.getDisplayName());
        displayNameMap.put(SupportDBUrlType.MARIADBDEFAULTURL.getDBKey(), SupportDBUrlType.MARIADBDEFAULTURL.getDisplayName());
        displayNameMap.put(SupportDBUrlType.POSTGRESQLEFAULTURL.getDBKey(),
                SupportDBUrlType.POSTGRESQLEFAULTURL.getDisplayName());
    }

    public String[] getDBDisplayNames() {
        String[] dbTypeItems = new String[displayNameMap.size()];
        displayNameMap.values().toArray(dbTypeItems);
        return dbTypeItems;
    }

    public String getDBType(String displayName) {
        if (displayNameMap.containsValue(displayName)) {
            for (String key : displayNameMap.keySet()) {
                String value = displayNameMap.get(key);
                if (displayName.equals(value)) {
                    return key;
                }
            }
        }
        return SupportDBUrlType.MYSQLDEFAULTURL.getDBKey();
    }

    private void testDbType(String dbType) {
        if (!PROP.containsKey(dbType) || !supportDBUrlMap.containsKey(dbType)) {
            StringBuffer sb = new StringBuffer();
            for (String current : supportDBUrlMap.keySet()) {
                sb.append("'" + current + "' ");//$NON-NLS-1$//$NON-NLS-2$
            }

            throw new IllegalArgumentException("Unknown dbtype '" + dbType + "'. Availables values are [" + sb + "]");//$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
        }
    }

    /**
     * Get the dburl via the dbType, and the dburl content come from the default value of SupportDBUrlType.
     *
     * @param dbType
     * @return
     */
    public String getDefaultDBUrl(String dbType) {
        testDbType(dbType);
        String propUrlValue = PROP.getProperty(dbType);
        SupportDBUrlType defaultUrlType = supportDBUrlMap.get(dbType);
        defaultUrlType = defaultUrlType == null ? SupportDBUrlType.MYSQLDEFAULTURL : defaultUrlType;
        if (propUrlValue == null) {
            return EMPTY_STRING;
        }
        Object[] args = { defaultUrlType.getHostName(), defaultUrlType.getPort(), defaultUrlType.getDBName(),
                defaultUrlType.getDataSource() };
        return MessageFormat.format(propUrlValue, args);
    }

    public SupportDBUrlType getDBUrlType(String dbType) {
        testDbType(dbType);
        SupportDBUrlType dbUrlDefaultType = supportDBUrlMap.get(dbType);
        return dbUrlDefaultType == null ? SupportDBUrlType.MYSQLDEFAULTURL : dbUrlDefaultType;
    }

    public static void main(String[] args) {
        try {
            InputStream in = SupportDBUrlStore.class.getResourceAsStream("dburl.properties"); //$NON-NLS-1$
            PROP.load(in);
            in.close();
            Set s = PROP.keySet();
            // System.out.println(s.toString());
            Iterator it = s.iterator();
            while (it.hasNext()) {
                String id = (String) it.next();
                String value = PROP.getProperty(id);
                System.out.println("\n" + id + "-------" + value); //$NON-NLS-1$ //$NON-NLS-2$

                // Object[] arguments = { "10.78.23.23", "33456", "testDB" };
                Object[] arguments = { "{hostname}", "{port}", "{dbname}", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                System.out.println(MessageFormat.format(value, arguments));
            }
            // System.out.println(p.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
