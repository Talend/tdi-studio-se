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
package shadow;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * DOC chuger class global comment. Detailled comment <br/>
 * 
 * $Id: AllTests.java 38013 2010-03-05 14:21:59Z mhirt $
 * 
 */
public final class AllTests {

    /**
     * Default Constructor. Must not be used.
     */
    private AllTests() {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for org.talend.designer.runprocess.shadow"); //$NON-NLS-1$
        // $JUnit-BEGIN$
        suite.addTestSuite(ShadowProcessTest.class);
        // $JUnit-END$
        return suite;
    }

}
