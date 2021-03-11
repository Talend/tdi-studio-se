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
package org.talend.sbi.engines.client.ui.wizards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.SpagoBiServer;

/**
 * DOC cantoine class global comment. Detailled comment <br/>
 *
 * $Id: SpagoBiServerHelper.java 2738 2007-04-27 13:12:27Z cantoine $
 *
 */
public final class SpagoBiServerHelper {

    private static final String SPAGOBISERVER_DELIMITER = ";"; //$NON-NLS-1$

    private static final String ENGINE_DESCRIPTION_DELIMITER = "#o#"; //$NON-NLS-1$

    public static String writeString(List<String> items) {
        int size = items.size();
        StringBuffer buf = new StringBuffer(size * 50);
        for (int i = 0; i < size; i++) {
            buf.append(items.get(i));
            if (i != size - 1)
                buf.append(SPAGOBISERVER_DELIMITER);
        }
        return buf.toString();
    }

    public static List<String> readString(String stringList) {
        if (stringList == null || "".equals(stringList)) //$NON-NLS-1$
            return EMPTY_STRING_LIST;
        check(stringList);
        ArrayList<String> result = new ArrayList<String>(50);
        for (String tmp : stringList.split(SPAGOBISERVER_DELIMITER)) {
            result.add(tmp);
        }
        return result;
    }

    public static List<SpagoBiServer> parse(String stringList) {
        if (stringList == null || "".equals(stringList)) //$NON-NLS-1$
            return EMPTY_SPAGOBISERVER_LIST;
        final List<String> strings = readString(stringList);
        List<SpagoBiServer> result = new ArrayList<SpagoBiServer>();
        for (String spagoBiServerStr : strings) {
            final SpagoBiServer spagoBiServer = PropertiesFactory.eINSTANCE.createSpagoBiServer();
            spagoBiServer.setEngineName(getEngineName(spagoBiServerStr));
            spagoBiServer.setShortDescription(getShortDescription(spagoBiServerStr));
            spagoBiServer.setHost(getHost(spagoBiServerStr));
            spagoBiServer.setPort(getPort(spagoBiServerStr));
            spagoBiServer.setLogin(getLogin(spagoBiServerStr));
            spagoBiServer.setPassword(getPassword(spagoBiServerStr));
            // spagoBiServer.setApplicationContext(getApplicationContext(spagoBiServerStr));
            result.add(spagoBiServer);
        }
        return Collections.unmodifiableList(result);
    }

    public static String flat(List<SpagoBiServer> list) {
        final List<String> strings = new ArrayList<String>(list.size());
        for (SpagoBiServer spagoBiServer : list) {
            strings.add(getString(spagoBiServer.getEngineName(), spagoBiServer.getShortDescription(), spagoBiServer
                    .getHost(), spagoBiServer.getPort(), spagoBiServer.getLogin(), spagoBiServer.getPassword()));
        }
        return writeString(strings);
    }

    public static final String getEngineName(String value) {
        check(value);
        String[] index = value.split(ENGINE_DESCRIPTION_DELIMITER);
        return index[0];
    }

    public static final String getShortDescription(String value) {
        check(value);
        String[] index = value.split(ENGINE_DESCRIPTION_DELIMITER);
        return index[1];
    }

    public static final String getHost(String value) {
        check(value);
        String[] index = value.split(ENGINE_DESCRIPTION_DELIMITER);
        return index[2];
    }

    public static final String getPort(String value) {
        check(value);
        String[] index = value.split(ENGINE_DESCRIPTION_DELIMITER);
        return index[3];
    }

    public static final String getLogin(String value) {
        check(value);
        String[] index = value.split(ENGINE_DESCRIPTION_DELIMITER);
        return index[4];
    }

    public static final String getPassword(String value) {
        check(value);
        String[] index = value.split(ENGINE_DESCRIPTION_DELIMITER);
        return index[5];
    }

    // public static final String getApplicationContext(String value) {
    // check(value);
    // String[] index = value.split(ENGINE_DESCRIPTION_DELIMITER);
    // return index[6];
    // }

    public static final String getString(String engineName, String shortDescription, String host, String port,
            String login, String password) {
        check(engineName);
        check(shortDescription);
        check(host);
        check(port);
        check(login);
        check(password);
        // check(applicationContext);
        return engineName + ENGINE_DESCRIPTION_DELIMITER + shortDescription + ENGINE_DESCRIPTION_DELIMITER + host
                + ENGINE_DESCRIPTION_DELIMITER + port + ENGINE_DESCRIPTION_DELIMITER + login
                + ENGINE_DESCRIPTION_DELIMITER + password;
        // + ENGINE_DESCRIPTION_DELIMITER + applicationContext;
    }

    private static void check(String str) {
        if (str == null || str.equals("")) //$NON-NLS-1$
            throw new IllegalArgumentException();
    }

    private SpagoBiServerHelper() {
    }

    private static final List<SpagoBiServer> EMPTY_SPAGOBISERVER_LIST = Collections
            .unmodifiableList(new ArrayList<SpagoBiServer>());

    private static final List<String> EMPTY_STRING_LIST = Collections.unmodifiableList(new ArrayList<String>());
}
