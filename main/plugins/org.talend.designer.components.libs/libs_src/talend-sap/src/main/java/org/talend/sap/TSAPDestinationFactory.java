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
package org.talend.sap;

import org.talend.sap.impl.SAPConnectionFactory;

import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;

/**
 * created by bchen on Sep 11, 2014 Detailled comment
 *
 */
public class TSAPDestinationFactory extends SAPConnectionFactory {

    /** The connection factory instance. */
    private static final TSAPDestinationFactory INSTANCE = new TSAPDestinationFactory();

    /**
     * Returns the connection factory instance.
     */
    public static TSAPDestinationFactory getInstance() {
        return INSTANCE;
    }

    protected TSAPDestinationFactory() {
    }

    public JCoDestination getDestination(TSAPDestinationData destData) throws JCoException {
        return JCoDestinationManager.getDestination(addDestinationProperties(destData.toProperties()));
    }
}
