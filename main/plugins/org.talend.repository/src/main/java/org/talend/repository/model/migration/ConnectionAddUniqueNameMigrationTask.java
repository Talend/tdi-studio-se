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

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IConnectionCategory;
import org.talend.core.model.properties.Item;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.utils.KeywordsValidator;
import org.talend.designer.core.model.utils.emf.talendfile.ConnectionType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;

/**
 * DOC talend class global comment. Detailled comment
 */
public class ConnectionAddUniqueNameMigrationTask extends AbstractJobMigrationTask {

    private static final String UNIQUE_NAME = "UNIQUE_NAME";

    private final List<String> uniqueConnectionNameList = new ArrayList<String>();

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.AbstractItemMigrationTask#execute(org.talend.core.model.properties.Item)
     */
    @Override
    public ExecutionResult execute(Item item) {
        try {
            uniqueConnectionNameList.clear();
            ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
            ProcessType processType = getProcessType(item);
            boolean modified = false;
            if (processType != null) {
                for (Object o : processType.getConnection()) {
                    ConnectionType currentConnection = (ConnectionType) o;
                    ElementParameterType uniqueNameParam = null;
                    for (Object paramObject : currentConnection.getElementParameter()) {
                        ElementParameterType paramType = (ElementParameterType) paramObject;
                        if (UNIQUE_NAME.equals(paramType.getName())) {
                            uniqueNameParam = paramType;
                        }
                    }
                    if (uniqueNameParam == null) {
                        uniqueNameParam = TalendFileFactory.eINSTANCE.createElementParameterType();
                        uniqueNameParam.setName("UNIQUE_NAME");
                        uniqueNameParam.setField(EParameterFieldType.TEXT.getName());
                        uniqueNameParam.setContextMode(false);
                        currentConnection.getElementParameter().add(uniqueNameParam);
                    }
                    String value = uniqueNameParam.getValue();
                    String baseName = "";
                    if (value == null || "".equals(value)) {
                        modified = true;
                        EConnectionType connectionType = EConnectionType.getTypeFromId(currentConnection.getLineStyle());
                        if (connectionType.hasConnectionCategory(IConnectionCategory.UNIQUE_NAME)
                                && connectionType.hasConnectionCategory(IConnectionCategory.FLOW)) {
                            baseName = null;
                        } else if (connectionType.equals(EConnectionType.ITERATE)) {
                            baseName = "iterate";
                        } else if (connectionType.equals(EConnectionType.TABLE)) {
                            baseName = null;
                        } else if (connectionType.equals(EConnectionType.SYNCHRONIZE)
                                || connectionType.equals(EConnectionType.PARALLELIZE)) {
                            baseName = null;
                        } else {
                            baseName = connectionType.getDefaultLinkName();
                        }
                        String uniqueName = "";
                        if (connectionType.equals(EConnectionType.TABLE)) {
                            uniqueName = currentConnection.getMetaname();
                        } else {
                            uniqueName = currentConnection.getLabel();
                        }
                        if (baseName != null && !"".equals(baseName)) {
                            uniqueName = generateUniqueConnectionName(baseName);
                        }
                        uniqueNameParam.setValue(uniqueName);
                        uniqueConnectionNameList.add(uniqueName);
                    }

                }

            }
            if (modified) {
                factory.save(item, true);
                return ExecutionResult.SUCCESS_WITH_ALERT;
            } else {
                return ExecutionResult.SUCCESS_NO_ALERT;
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.IProjectMigrationTask#getOrder()
     */
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2009, 12, 31, 12, 0, 0);
        return gc.getTime();
    }

    public String generateUniqueConnectionName(String baseName) {
        if (baseName == null) {
            throw new IllegalArgumentException("baseName can't be null"); //$NON-NLS-1$
        }
        String uniqueName = baseName + 1;

        int counter = 1;
        boolean exists = true;
        while (exists) {
            exists = !checkValidConnectionName(uniqueName);
            if (!exists) {
                break;
            }
            uniqueName = baseName + counter++;
        }
        return uniqueName;
    }

    public boolean checkValidConnectionName(String connectionName) {
        // test if name already exist but with ignore case (contains test only with same case)

        if (checkIgnoreCase(connectionName)) {
            return false;
        }
        Perl5Matcher matcher = new Perl5Matcher();
        Perl5Compiler compiler = new Perl5Compiler();
        Pattern pattern;

        try {
            pattern = compiler.compile("^[A-Za-z_][A-Za-z0-9_]*$"); //$NON-NLS-1$
            if (!matcher.matches(connectionName, pattern)) {
                return false;
            }
        } catch (MalformedPatternException e) {
            throw new RuntimeException(e);
        }

        return !KeywordsValidator.isKeyword(connectionName);
    }

    public boolean checkIgnoreCase(String connectionName) {
        if (connectionName.equals("")) {//$NON-NLS-1$
            return true;
        }
        if (uniqueConnectionNameList != null) {
            for (String value : uniqueConnectionNameList) {
                if (value.equals(connectionName)) {
                    return true;
                }
            }
        }

        return false;
    }

}
