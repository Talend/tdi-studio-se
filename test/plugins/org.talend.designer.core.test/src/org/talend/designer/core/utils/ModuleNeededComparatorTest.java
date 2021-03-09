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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.talend.core.model.general.ModuleNeeded;

/**
 * Test class for the ModuleNeededComparator.
 *
 */
public class ModuleNeededComparatorTest {

    private final List<ModuleNeeded> neededModules = new ArrayList<>();

    private final List<ModuleNeeded> refNeededModules = new ArrayList<>();

    private final Map<Integer, String> refMap = new HashMap<>();

    private static final String EMPTY = ""; //$NON-NLS-1$

    private static final String BUNDLE_NAME = "bundle1"; //$NON-NLS-1$

    private static final String MODULE1 = "module1", MODULE2 = "module2"; //$NON-NLS-1$ //$NON-NLS-2$

    @Before
    public void setUp() {

        ModuleNeeded m1 = new ModuleNeeded("c1", MODULE1, EMPTY, false); //$NON-NLS-1$
        m1.setBundleName(EMPTY);
        ModuleNeeded m2 = new ModuleNeeded("c2", MODULE1, EMPTY, false); //$NON-NLS-1$
        m2.setBundleName(BUNDLE_NAME);
        ModuleNeeded m3 = new ModuleNeeded("c3", MODULE1, EMPTY, true); //$NON-NLS-1$
        ModuleNeeded m4 = new ModuleNeeded("c4", MODULE1, EMPTY, true); //$NON-NLS-1$
        m4.setBundleName(BUNDLE_NAME);
        ModuleNeeded m5 = new ModuleNeeded("c5", MODULE1, EMPTY, true); //$NON-NLS-1$
        ModuleNeeded m6 = new ModuleNeeded("c6", MODULE1, EMPTY, true); //$NON-NLS-1$
        m6.setMrRequired(true);
        ModuleNeeded m11 = new ModuleNeeded("c11", MODULE2, EMPTY, false); //$NON-NLS-1$
        m11.setBundleName(EMPTY);
        ModuleNeeded m12 = new ModuleNeeded("c12", MODULE2, EMPTY, false); //$NON-NLS-1$
        m12.setBundleName(BUNDLE_NAME);
        ModuleNeeded m13 = new ModuleNeeded("c13", MODULE2, EMPTY, true); //$NON-NLS-1$
        ModuleNeeded m14 = new ModuleNeeded("c14", MODULE2, EMPTY, true); //$NON-NLS-1$
        m14.setBundleName(BUNDLE_NAME);
        ModuleNeeded m15 = new ModuleNeeded("c15", MODULE2, EMPTY, true); //$NON-NLS-1$
        ModuleNeeded m16 = new ModuleNeeded("c16", MODULE2, EMPTY, true); //$NON-NLS-1$
        m16.setMrRequired(true);

        neededModules.add(m11);
        neededModules.add(m3);
        neededModules.add(m2);
        neededModules.add(m12);
        neededModules.add(m16);
        neededModules.add(m5);
        neededModules.add(m4);
        neededModules.add(m1);
        neededModules.add(m15);
        neededModules.add(m13);
        neededModules.add(m6);
        neededModules.add(m14);

        refNeededModules.add(m4);
        refNeededModules.add(m2);
        refNeededModules.add(m1);
        refNeededModules.add(m6);
        refNeededModules.add(m3);
        refNeededModules.add(m5);
        refNeededModules.add(m14);
        refNeededModules.add(m12);
        refNeededModules.add(m11);
        refNeededModules.add(m16);
        refNeededModules.add(m15);
        refNeededModules.add(m13);

        refMap.put(1, "c4"); //$NON-NLS-1$
        refMap.put(2, "c2"); //$NON-NLS-1$
        refMap.put(3, "c1"); //$NON-NLS-1$
        refMap.put(4, "c6"); //$NON-NLS-1$
        refMap.put(5, "c3"); //$NON-NLS-1$
        refMap.put(6, "c5"); //$NON-NLS-1$
        refMap.put(7, "c14"); //$NON-NLS-1$
        refMap.put(8, "c12"); //$NON-NLS-1$
        refMap.put(9, "c11"); //$NON-NLS-1$
        refMap.put(10, "c16"); //$NON-NLS-1$
        refMap.put(11, "c15"); //$NON-NLS-1$
        refMap.put(12, "c13"); //$NON-NLS-1$
    }

    @Test
    public void compareTest() {
        Collections.sort(neededModules, new ModuleNeededComparator());
        assertEquals(neededModules, refNeededModules);

        Iterator<ModuleNeeded> iter = neededModules.iterator();
        boolean ok = true;
        int index = 1;
        while (iter.hasNext()) {
            ModuleNeeded mn = iter.next();
            if (mn.getContext() == null || !mn.getContext().equals(refMap.get(index))) {
                ok = false;
                break;
            }
            index++;
        }

        assertTrue(ok);
    }
}
