// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * created by hcyi on Nov 12, 2018
 * Detailled comment
 *
 */
public class ConnectionUtilTest {

    @Test
    public void testGetDriverJarNULL() {
        List list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();
        list.add(map);
        map.put("drivers", null);//$NON-NLS-1$
        ConnectionUtil.getDriverJar(list);
        Assert.assertEquals(1, map.size());
        Assert.assertNull(map.get("drivers")); //$NON-NLS-1$
    }

    @Test
    public void testGetDriverJarNullCharacter() {
        List list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();
        list.add(map);
        map.put("drivers", "");//$NON-NLS-1$//$NON-NLS-2$
        ConnectionUtil.getDriverJar(list);
        Assert.assertEquals(1, map.size());
        Assert.assertEquals("", map.get("drivers")); //$NON-NLS-1$//$NON-NLS-2$
    }

    @Test
    public void testGetDriverJarEmpty() {
        List list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();
        list.add(map);
        ConnectionUtil.getDriverJar(list);
        Assert.assertEquals(1, map.size());
        Assert.assertNull(map.get("drivers"));
    }

    @Test
    public void testGetDriverJarMysql8() {
        List list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();
        list.add(map);
        map.put("drivers", "mvn:mysql/mysql-connector-java/8.0.18/jar");//$NON-NLS-1$//$NON-NLS-2$
        ConnectionUtil.getDriverJar(list);
        Assert.assertEquals(1, map.size());
        Assert.assertEquals("mysql-connector-java-8.0.18.jar", map.get("drivers")); //$NON-NLS-1$//$NON-NLS-2$
    }

    @Test
    public void testGetDriverJarMysql5() {
        List list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();
        list.add(map);
        map.put("drivers", "mvn:org.talend.libraries/mysql-connector-java-5.1.30-bin/6.0.0");//$NON-NLS-1$//$NON-NLS-2$
        ConnectionUtil.getDriverJar(list);
        Assert.assertEquals(1, map.size());
        Assert.assertEquals("mysql-connector-java-5.1.30-bin.jar", map.get("drivers")); //$NON-NLS-1$//$NON-NLS-2$
    }

    @Test
    public void testGetDriverJarMysql8_JarName() {
        List list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();
        list.add(map);
        map.put("drivers", "mysql-connector-java-8.0.18.jar");//$NON-NLS-1$//$NON-NLS-2$
        ConnectionUtil.getDriverJar(list);
        Assert.assertEquals(1, map.size());
        Assert.assertEquals("mysql-connector-java-8.0.18.jar", map.get("drivers")); //$NON-NLS-1$//$NON-NLS-2$
    }

    @Test
    public void testResetDriverValueNULL() {
        List list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();
        list.add(map);
        map.put("drivers", null);//$NON-NLS-1$
        ConnectionUtil.getDriverJar(list);
        Assert.assertEquals(1, map.size());
        Assert.assertNull(map.get("drivers")); //$NON-NLS-1$
    }

    @Test
    public void testResetDriverValueNullCharacter() {
        List list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();
        list.add(map);
        map.put("drivers", "");//$NON-NLS-1$//$NON-NLS-2$
        ConnectionUtil.getDriverJar(list);
        Assert.assertEquals(1, map.size());
        Assert.assertEquals("", map.get("drivers")); //$NON-NLS-1$//$NON-NLS-2$
    }

    @Test
    public void testResetDriverValueEmpty() {
        List list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();
        list.add(map);
        ConnectionUtil.getDriverJar(list);
        Assert.assertEquals(1, map.size());
        Assert.assertNull(map.get("drivers"));
    }

    @Test
    public void testResetDriverValueMysql8() {
        List list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();
        list.add(map);
        map.put("drivers", "mvn:mysql/mysql-connector-java/8.0.18/jar");//$NON-NLS-1$//$NON-NLS-2$
        ConnectionUtil.getDriverJar(list);
        Assert.assertEquals(1, map.size());
        Assert.assertEquals("mysql-connector-java-8.0.18.jar", map.get("drivers")); //$NON-NLS-1$//$NON-NLS-2$
    }

    @Test
    public void testResetDriverValueMysql5() {
        List list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();
        list.add(map);
        map.put("drivers", "mvn:org.talend.libraries/mysql-connector-java-5.1.30-bin/6.0.0");//$NON-NLS-1$//$NON-NLS-2$
        ConnectionUtil.getDriverJar(list);
        Assert.assertEquals(1, map.size());
        Assert.assertEquals("mysql-connector-java-5.1.30-bin.jar", map.get("drivers")); //$NON-NLS-1$//$NON-NLS-2$
    }

    @Test
    public void testResetDriverValueMysql8_JarName() {
        List list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();
        list.add(map);
        map.put("drivers", "mysql-connector-java-8.0.18.jar");//$NON-NLS-1$//$NON-NLS-2$
        ConnectionUtil.getDriverJar(list);
        Assert.assertEquals(1, map.size());
        Assert.assertEquals("mysql-connector-java-8.0.18.jar", map.get("drivers")); //$NON-NLS-1$//$NON-NLS-2$
    }

    @Test
    public void testGetDriverJarFromMvnUrlNULL() {
        Assert.assertNull(ConnectionUtil.getDriverJarFromMvnUrl(null));
    }

    @Test
    public void testGetDriverJarFromMvnUrlNullCharacter() {
        Assert.assertEquals("", ConnectionUtil.getDriverJarFromMvnUrl("")); //$NON-NLS-1$//$NON-NLS-2$
    }

    @Test
    public void testGetDriverJarFromMvnUrlEmpty() {
        Assert.assertEquals(" ", ConnectionUtil.getDriverJarFromMvnUrl(" ")); //$NON-NLS-1$//$NON-NLS-2$
    }

    @Test
    public void testGetDriverJarFromMvnUrlMysql8() {
        Assert.assertEquals("mysql-connector-java-8.0.18.jar", //$NON-NLS-1$
                ConnectionUtil.getDriverJarFromMvnUrl("mvn:mysql/mysql-connector-java/8.0.18/jar")); //$NON-NLS-1$
    }

    @Test
    public void testGetDriverJarFromMvnUrlMysql5() {
        Assert.assertEquals("mysql-connector-java-5.1.30-bin.jar", //$NON-NLS-1$
                ConnectionUtil.getDriverJarFromMvnUrl("mvn:org.talend.libraries/mysql-connector-java-5.1.30-bin/6.0.0")); //$NON-NLS-1$
    }

    @Test
    public void testGetDriverJarFromMvnUrlMysql8_JarName() {
        Assert.assertEquals("mysql-connector-java-8.0.18.jar", //$NON-NLS-1$
                ConnectionUtil.getDriverJarFromMvnUrl("mysql-connector-java-8.0.18.jar")); //$NON-NLS-1$
    }
}
