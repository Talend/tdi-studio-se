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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.talend.designer.core.model.utils.emf.component.IMPORTSType;
import org.talend.designer.core.model.utils.emf.component.IMPORTType;

/**
 *
 * created by ycbai on 2014-5-7 Detailled comment
 *
 */
public class ImportModuleManager {

    private static final String SPACE = " "; //$NON-NLS-1$

    private static final String AND = "AND"; //$NON-NLS-1$

    private static ImportModuleManager instance;

    private ImportModuleManager() {
    }

    public static synchronized final ImportModuleManager getInstance() {
        if (instance == null) {
            instance = new ImportModuleManager();
        }

        return instance;
    }

    public List<IMPORTType> getImportTypes(IMPORTSType importsType) {
        List<IMPORTType> types = new ArrayList<IMPORTType>();
        List<IMPORTType> importTypes = importsType.getIMPORT();
        for (IMPORTType importType : importTypes) {
            inheritParentProperties(importsType, importType);
            types.add(importType);
        }
        List<IMPORTSType> importsTypes = importsType.getIMPORTS();
        for (IMPORTSType imports : importsTypes) {
            inheritParentProperties(importsType, imports);
            types.addAll(getImportTypes(imports));
        }

        return types;
    }

    private void inheritParentProperties(IMPORTSType parentImportsType, Object importType) {
        ImportTypeModel importTypeModel = new ImportTypeModel(importType);
        importTypeModel.setRequiredIf(countCondition(parentImportsType, importTypeModel));
    }

    private String countCondition(IMPORTSType parentImportsType, ImportTypeModel importTypeModel) {
        String requiredif = importTypeModel.getRequiredIf();
        String parentRequiredif = parentImportsType.getREQUIREDIF();
        if (StringUtils.isNotBlank(parentRequiredif)) {
            if (StringUtils.isNotBlank(requiredif)) {
                requiredif = parentRequiredif.concat(SPACE).concat(AND).concat(SPACE).concat(requiredif);
            } else {
                requiredif = parentRequiredif;
            }
        }

        return requiredif;
    }

    class ImportTypeModel {

        private IMPORTSType importsType;

        private IMPORTType importType;

        public ImportTypeModel(IMPORTType importType) {
            this.importType = importType;
        }

        public ImportTypeModel(IMPORTSType importsType) {
            this.importsType = importsType;
        }

        public ImportTypeModel(Object importType) {
            if (importType instanceof IMPORTType) {
                this.importType = (IMPORTType) importType;
            } else if (importType instanceof IMPORTSType) {
                this.importsType = (IMPORTSType) importType;
            }
        }

        public IMPORTSType getImportsType() {
            return this.importsType;
        }

        public IMPORTType getImportType() {
            return this.importType;
        }

        public String getRequiredIf() {
            String requiredIf = null;
            if (importType != null) {
                requiredIf = StringUtils.trimToEmpty(importType.getREQUIREDIF());
            } else if (importsType != null) {
                requiredIf = StringUtils.trimToEmpty(importsType.getREQUIREDIF());
            }

            return requiredIf;
        }

        public void setRequiredIf(String requiredIf) {
            if (importType != null) {
                importType.setREQUIREDIF(requiredIf);
            } else if (importsType != null) {
                importsType.setREQUIREDIF(requiredIf);
            }
        }

    }
}
