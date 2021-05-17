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
package org.talend.designer.codegen.junit;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;
import org.talend.designer.codegen.model.CodeGeneratorEmittersPoolFactory;

/**
 * created by nrousseau on Jul 25, 2012 Detailled comment
 *
 */
public class JUnitStudioStartup {

    private static final long ONE_MINUTE_IN_MILLISEC = 60000;

    private static final long INIT_TIMEOUT = ONE_MINUTE_IN_MILLISEC * 10;

    private static final long INIT_PAUSE = 100;

    private final SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss");

    @Test
    public void test() throws InterruptedException {
        System.out.println("|" + format.format(new Date()) + "| Wait for full generation of jet emitters");
        long startTimer = System.currentTimeMillis();
        long endTimer = startTimer;
        while ((!CodeGeneratorEmittersPoolFactory.isInitialized()) && ((endTimer - startTimer) < INIT_TIMEOUT)) {
            Thread.sleep(INIT_PAUSE);
            endTimer = System.currentTimeMillis();
        }
        if ((endTimer - startTimer) > INIT_TIMEOUT) {
            System.out.println("|" + format.format(new Date())
                    + "| Timeout when generate jet emitters (10 minutes), will just continue with standards junits");
        } else {
            System.out.println("|" + format.format(new Date())
                    + "| Jet emitters initialized successfully, will proceed to standards junits");
        }
    }

}
