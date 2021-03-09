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
package org.talend.repository.generic.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.repository.ERepositoryObjectType;

/**
 * created by ycbai on 2016年10月8日 Detailled comment
 *
 */
public class RepTypeMappingManager {

    private static RepTypeMappingManager instance = null;

    private static final String TYPE_MAPPING_FILE = "rep_type_mapping.properties"; //$NON-NLS-1$

    private Properties properties;

    private RepTypeMappingManager() {
    }

    public synchronized static RepTypeMappingManager getInstance() {
        if (instance == null) {
            instance = new RepTypeMappingManager();
        }
        return instance;
    }

    private void loadTypeMapping() {
        properties = new Properties();
        InputStream input = null;
        try {
            input = getClass().getClassLoader().getResourceAsStream(TYPE_MAPPING_FILE);
            properties.load(input);
        } catch (IOException e) {
            ExceptionHandler.process(e);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace(); // Only for debug.
                }
            }
        }
    }

    public ERepositoryObjectType getNewRepType(String oldType) {
        if (properties == null || properties.isEmpty()) {
            loadTypeMapping();
        }
        ERepositoryObjectType newType = null;
        String property = properties.getProperty(oldType);
        if (property != null) {
            newType = ERepositoryObjectType.valueOf(property);
        }
        return newType;
    }

}
