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
package org.talend.designer.scd.util;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.talend.designer.scd.model.SurrogateCreationType;
import org.talend.designer.scd.model.SurrogateKey;

/**
 * DOC hcw class global comment. Detailled comment
 */
public class SurrogateKeyManager {

    private static final String PREFIX = "sk"; //$NON-NLS-1$

    private int index = 1;

    private Set<String> keyNames = new HashSet<String>();

    public void addSurrogateKey(SurrogateKey key) {
        keyNames.add(key.getColumn());
    }

    public SurrogateKey createSurrogateKey() {
        SurrogateKey key = new SurrogateKey();
        key.setColumn(generateKeyName());
        key.setCreation(SurrogateCreationType.AUTO_INCREMENT);
        key.setComplement(""); //$NON-NLS-1$
        return key;
    }

    public boolean hasErrors(SurrogateKey key) {
        if (key.getCreation() == SurrogateCreationType.INPUT_FIELD || key.getCreation() == SurrogateCreationType.ROUTINE) {
            // cannot be empty
            return StringUtils.isEmpty(key.getComplement());
        }
        return false;
    }

    public String generateKeyName() {
        while (true) {
            String name = PREFIX + index;
            index++;
            if (!keyNames.contains(name)) {
                keyNames.add(name);
                return name;
            }
        }
    }

}
