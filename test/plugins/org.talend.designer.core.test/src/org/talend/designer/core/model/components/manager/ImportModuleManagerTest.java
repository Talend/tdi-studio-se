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
package org.talend.designer.core.model.components.manager;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.talend.designer.core.model.utils.emf.component.ComponentFactory;
import org.talend.designer.core.model.utils.emf.component.IMPORTSType;
import org.talend.designer.core.model.utils.emf.component.IMPORTType;

/**
 * created by ycbai on 2014-5-9 Detailled comment
 *
 */
public class ImportModuleManagerTest {

    private ImportModuleManager manager;

    private IMPORTSType testImportsType;

    private void initTestImportsType() {
        testImportsType = ComponentFactory.eINSTANCE.createIMPORTSType();

        IMPORTType importType1 = ComponentFactory.eINSTANCE.createIMPORTType();
        importType1.setMODULE("test-jar1.jar"); //$NON-NLS-1$
        importType1.setREQUIREDIF("x > 0"); //$NON-NLS-1$
        testImportsType.getIMPORT().add(importType1);
        IMPORTType importType2 = ComponentFactory.eINSTANCE.createIMPORTType();
        importType2.setMODULE("test-jar2.jar"); //$NON-NLS-1$
        importType2.setREQUIREDIF("y > 0"); //$NON-NLS-1$
        testImportsType.getIMPORT().add(importType2);

        IMPORTSType importsType = ComponentFactory.eINSTANCE.createIMPORTSType();
        importsType.setREQUIREDIF("z > 0"); //$NON-NLS-1$
        IMPORTType importType3 = ComponentFactory.eINSTANCE.createIMPORTType();
        importType3.setMODULE("test-jar3.jar"); //$NON-NLS-1$
        importType3.setREQUIREDIF("(x > y)"); //$NON-NLS-1$
        importsType.getIMPORT().add(importType3);
        testImportsType.getIMPORTS().add(importsType);
    }

    @Test
    public void testGetImportTypes() {
        manager = ImportModuleManager.getInstance();
        initTestImportsType();

        List<IMPORTType> importTypes = manager.getImportTypes(testImportsType);
        assertNotNull(importTypes);
        assertTrue(importTypes.size() == 3);

        IMPORTType importType1 = importTypes.get(0);
        assertEquals("test-jar1.jar", importType1.getMODULE()); //$NON-NLS-1$
        assertEquals("x > 0", importType1.getREQUIREDIF()); //$NON-NLS-1$

        IMPORTType importType2 = importTypes.get(1);
        assertEquals("test-jar2.jar", importType2.getMODULE()); //$NON-NLS-1$
        assertEquals("y > 0", importType2.getREQUIREDIF()); //$NON-NLS-1$

        IMPORTType importType3 = importTypes.get(2);
        assertEquals("test-jar3.jar", importType3.getMODULE()); //$NON-NLS-1$
        assertEquals("z > 0 AND (x > y)", importType3.getREQUIREDIF()); //$NON-NLS-1$
    }

}
