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

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.emf.common.util.EList;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.utils.emf.talendfile.ConnectionType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementValueType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * wliu class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2009-06-05 17:06:40Z wliu $
 *
 */
public class ChangeReturnTypeOfServerAliveResult extends AbstractJobMigrationTask {

    private static final String MATCH_REGEX_GET = "\\(\\(\\bString\\b\\)\\bglobalMap\\b\\.get\\("
            + "\"tServerAlive_[1-9]\\d*_SERVER_ALIVE_RESULT\"\\)\\)\\.equals\\(\"(OK|KO)\"\\)";//$NON-NLS-1$

    private static final Pattern PATTERN_GET = Pattern.compile(MATCH_REGEX_GET); //$NON-NLS-1$

    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        if (processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }
        try {
            convertItem(item, processType);
            return ExecutionResult.SUCCESS_WITH_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    private String getReplaceValue(Matcher match) {
        match.reset();
        StringBuffer sb = new StringBuffer();
        // boolean bl = match.find();
        // System.out.println("group:" + match.group());
        while (match.find()) {
            String group = match.group();
            String tmp = ""; //$NON-NLS-1$
            if (group.contains("equals(\"OK\")")) { //$NON-NLS-1$
                tmp = group.replace("String", "Boolean").replace(".equals(\"OK\")", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

            } else if (group.contains("equals(\"KO\")")) { //$NON-NLS-1$
                tmp = group.replace("((String)", "!((Boolean)").replace(".equals(\"KO\")", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            }
            match.appendReplacement(sb, tmp);
        }
        match.appendTail(sb);
        return sb.toString();

    }

    private boolean convertItem(Item item, ProcessType processType) throws PersistenceException {
        ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        boolean modified = false;
        ECodeLanguage language = getProject().getLanguage();
        EList node = processType.getNode();
        EList connections = processType.getConnection();

        for (Object n : node) {
            NodeType type = (NodeType) n;
            EList elementParameterList = type.getElementParameter();
            for (Object elem : elementParameterList) {
                ElementParameterType elemType = (ElementParameterType) elem;
                if (language.equals(ECodeLanguage.JAVA)) {
                    if (elemType.getValue() != null && elemType.getValue().contains("tServerAlive")) { //$NON-NLS-1$
                        Matcher match = PATTERN_GET.matcher(elemType.getValue());
                        String replace = getReplaceValue(match);
                        if (replace != null && replace.length() > 0) {
                            elemType.setValue(replace);
                            modified = true;
                        }
                    }
                }

                // for table

                EList elemValue = elemType.getElementValue();
                for (Object elemV : elemValue) {
                    ElementValueType elemVal = (ElementValueType) elemV;
                    if (language.equals(ECodeLanguage.JAVA)) {
                        if (elemVal.getValue() != null && elemVal.getValue().contains("tServerAlive")) { //$NON-NLS-1$
                            Matcher match = PATTERN_GET.matcher(elemVal.getValue());
                            String replace = getReplaceValue(match);
                            if (replace != null && replace.length() > 0) {
                                elemVal.setValue(replace);
                                modified = true;
                            }
                        }
                    }
                }

            }

        }
        // the links
        for (Object n : connections) {
            ConnectionType type = (ConnectionType) n;
            EList elementParameterList = type.getElementParameter();
            for (Object elem : elementParameterList) {
                ElementParameterType elemType = (ElementParameterType) elem;
                if (language.equals(ECodeLanguage.JAVA)) {
                    if (elemType.getValue() != null && elemType.getValue().contains("tServerAlive")) { //$NON-NLS-1$
                        Matcher match = PATTERN_GET.matcher(elemType.getValue());
                        String replace = getReplaceValue(match);
                        if (replace != null && replace.length() > 0) {
                            elemType.setValue(replace);
                            modified = true;
                        }
                    }
                }
            }
        }

        if (modified) {
            factory.save(item, true);
        }

        return modified;

    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2009, 6, 1, 12, 0, 0);
        return gc.getTime();
    }

}
