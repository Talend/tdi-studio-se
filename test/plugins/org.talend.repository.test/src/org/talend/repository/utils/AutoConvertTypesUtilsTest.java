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
package org.talend.repository.utils;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.talend.core.model.metadata.types.AutoConversionType;
import org.talend.core.model.metadata.types.JavaTypesManager;

/**
 * created by hcyi on Aug 25, 2016 Detailled comment
 *
 */
public class AutoConvertTypesUtilsTest {

    private File testFile = null;

    private List<AutoConversionType> testBeanList = new ArrayList<>();

    @Before
    public void init() throws Exception {
        Bundle b = Platform.getBundle("org.talend.repository.test");//$NON-NLS-1$
        URL url = FileLocator.toFileURL(FileLocator.find(b, new Path("AutoConversionTypesTest.xml"), null)); //$NON-NLS-1$
        Assert.assertNotNull(url);
        testFile = new File(url.getPath());
        Assert.assertTrue(testFile.exists());
        //
        AutoConversionType bean = new AutoConversionType();
        bean.setSourceDataType(JavaTypesManager.getDefaultJavaType().getId());
        bean.setTargetDataType(JavaTypesManager.INTEGER.getId());
        bean.setConversionFunction("Integer.parseInt(${0})"); //$NON-NLS-1$
        testBeanList.add(bean);
    }

    @Test
    public void testLoadFile() throws Exception {
        List<AutoConversionType> beanList = AutoConvertTypesUtils.load(testFile);
        Assert.assertFalse(beanList.isEmpty());
        Assert.assertTrue(beanList.size() == 1);
    }

    @Test
    public void testSaveFile() throws Exception {
        Assert.assertFalse(testBeanList.isEmpty());
        Assert.assertTrue(testBeanList.size() == 1);
        boolean saved = AutoConvertTypesUtils.save(testBeanList, testFile);
        Assert.assertTrue(saved);
    }
}
