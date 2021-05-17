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
package org.talend.designer.xmlmap.generation;

import java.util.ArrayList;
import java.util.List;
import org.talend.core.model.process.BlockCode;



/**
 * DOC rdubois  class global comment. Detailled comment
 * <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40Z rdubois $
 *
 */
public class GenerationManagerFactory {

    private static GenerationManagerFactory instance;

    public static GenerationManagerFactory getInstance() {
        if (instance == null) {
            instance = new GenerationManagerFactory();
        }
        return instance;
    }

    public GenerationManager getGenerationManager() {
        GenerationManager gen = null;
        gen = new GenerationManager();

        return gen;
    }

}
