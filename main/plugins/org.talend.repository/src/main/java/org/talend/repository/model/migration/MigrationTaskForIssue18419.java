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
package org.talend.repository.model.migration;

import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;

import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Project;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * DOC hcyi class global comment. Detailled comment
 */
public class MigrationTaskForIssue18419 extends AbstractJobMigrationTask {

    private static final String DEMOS_PROJECT_NAME = "TALENDDEMOSJAVA"; //$NON-NLS-1$

    @Override
    public ExecutionResult execute(final Item item) {
        ProcessType processType = getProcessType(item);
        if (getProject().getLanguage() != ECodeLanguage.JAVA || processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }

        IComponentFilter filter = new NameComponentFilter("tFileUnarchive"); //$NON-NLS-1$
        try {
            ModifyComponentsAction.searchAndModify(item, processType, filter,
                    Arrays.<IComponentConversion> asList(new IComponentConversion() {

                        @Override
                        public void transform(NodeType node) {
                            Project pro = ProxyRepositoryFactory.getInstance().getProject(item);
                            String proName = pro.getLabel();
                            ElementParameterType zipFile = ComponentUtilities.getNodeProperty(node, "ZIPFILE"); //$NON-NLS-1$
                            String zipFileName = zipFile.getValue();
                            final String oldValues = getOldValues();
                            if (proName != null && !proName.equals(getDemoProjectName()) && zipFileName != null
                                    && zipFileName.equals(oldValues)) {
                                zipFile.setValue(oldValues.replaceFirst(getDemoProjectName(), proName));
                            }
                        }

                    }));
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }

        return ExecutionResult.SUCCESS_NO_ALERT;

    }

    protected String getDemoProjectName() {
        return DEMOS_PROJECT_NAME;
    }

    private String getOldValues() {
        return "System.getProperty(\"user.dir\").concat(\"/workspace/" //$NON-NLS-1$
                + getDemoProjectName() + "/documentations/talend_files_0.1.item\")"; //$NON-NLS-1$
    }

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2011, 11, 10, 13, 0, 0);
        return gc.getTime();
    }
}
