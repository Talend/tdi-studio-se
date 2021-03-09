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

import java.util.Comparator;

import org.talend.core.model.general.ModuleNeeded;

/**
 * Comparator that will allow sorting a list of modules on their priority.
 */
public class ModuleNeededComparator implements Comparator<ModuleNeeded> {

    /**
     * 1) The modules are going to be sorted by their module name.<br>
     * 2) The modules with a bundle name are going to be prioritize compared to the modules without a bundle name.<br>
     * 3) The modules with a non-empty bundle name are going to be prioritize compared to me moudles with an empty
     * bundle name.<br>
     * 4) The modules which are required are going to be prioritize compared to non-required modules.<br>
     * 5) The modules which are mrrequired are going to be prioritize compared to non-mrrequired modules.
     */
    @Override
    public int compare(ModuleNeeded o1, ModuleNeeded o2) {
        if (o1.getModuleName() != null && o1.getModuleName().equals(o2.getModuleName())) {
            if (o1.getBundleName() == null && o2.getBundleName() != null) {
                return 1;
            }

            if (o2.getBundleName() == null && o1.getBundleName() != null) {
                return -1;
            }

            if (o1.getBundleName() != null && o2.getBundleName() != null && "".equals(o1.getBundleName()) //$NON-NLS-1$
                    && !"".equals(o2.getBundleName())) { //$NON-NLS-1$
                return 1;
            }

            if (o2.getBundleName() != null && o1.getBundleName() != null && "".equals(o2.getBundleName()) //$NON-NLS-1$
                    && !"".equals(o1.getBundleName())) { //$NON-NLS-1$
                return -1;
            }

            if (!o1.isRequired() && o2.isRequired()) {
                return 1;
            }

            if (!o2.isRequired() && o1.isRequired()) {
                return -1;
            }

            if (!o1.isMrRequired() && o2.isMrRequired()) {
                return 1;
            }

            if (!o2.isMrRequired() && o1.isMrRequired()) {
                return -1;
            }

            return 0;
        }

        if (o1.getModuleName() != null && o2.getModuleName() != null) {
            return o1.getModuleName().compareTo(o2.getModuleName());
        }
        return 0;
    }
}
