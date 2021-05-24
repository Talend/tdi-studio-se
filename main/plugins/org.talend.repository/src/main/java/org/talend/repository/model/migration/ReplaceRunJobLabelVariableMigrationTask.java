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
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.Perl5Substitution;
import org.apache.oro.text.regex.Util;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * ggu class global comment. Detailled comment
 *
 * only in the tRunJob node and only for the parameter label.
 */
public class ReplaceRunJobLabelVariableMigrationTask extends AbstractItemMigrationTask {

    IComponentFilter filter = new IComponentFilter() {

        public boolean accept(NodeType node) {
            if (node.getComponentName().equals("tRunJob")) { //$NON-NLS-1$
                return replaceValue(node, false);
            }
            return false;
        }

    };

    IComponentConversion replaceConversion = new IComponentConversion() {

        public void transform(NodeType node) {
            replaceValue(node, true);
        }
    };

    private boolean replaceValue(NodeType node, boolean replace) {
        if (node == null) {
            return false;
        }
        ElementParameterType param = ComponentUtilities.getNodeProperty(node, "LABEL"); //$NON-NLS-1$
        if (param != null && param.getField().equals(EParameterFieldType.TEXT.getName())) {
            String value = param.getValue();
            if (value != null) {
                PatternCompiler compiler = new Perl5Compiler();
                Perl5Matcher matcher = new Perl5Matcher();

                try {
                    Pattern pattern = compiler.compile("\\b(" //$NON-NLS-1$
                            + "__PROCESS_TYPE_PROCESS__" + ")(\\b)"); //$NON-NLS-1$ //$NON-NLS-2$
                    if (matcher.contains(value, pattern)) {
                        if (replace) { // replace
                            Perl5Substitution substitution = new Perl5Substitution("__PROCESS__" + "$2", //$NON-NLS-1$ //$NON-NLS-2$
                                    Perl5Substitution.INTERPOLATE_ALL);

                            String newValue = Util.substitute(matcher, pattern, substitution, value, Util.SUBSTITUTE_ALL);

                            param.setValue(newValue);
                        }
                        return true;
                    }
                } catch (MalformedPatternException e) {
                    //
                }

            }
        }
        return false;
    }

    @Override
    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> types = new ArrayList<ERepositoryObjectType>();
        types.add(ERepositoryObjectType.PROCESS);
        types.add(ERepositoryObjectType.JOBLET);
        return types;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.AbstractItemMigrationTask#execute(org.talend.core.model.properties.Item)
     */
    @SuppressWarnings("unchecked")
    @Override
    public ExecutionResult execute(Item item) {

        try {
            List<NodeType> nodes = null;
            if (item instanceof ProcessItem) {
                nodes = ((ProcessItem) item).getProcess().getNode();
            } else if (item instanceof JobletProcessItem) {
                nodes = ((JobletProcessItem) item).getJobletProcess().getNode();
            }

            if (nodes != null) {
                boolean modified = false;
                for (NodeType node : nodes) {
                    if (ModifyComponentsAction.searchAndModify(node, filter, Arrays
                            .<IComponentConversion> asList(replaceConversion))) {
                        modified = true;
                    }
                }
                if (modified) {
                    IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                    factory.save(item, true);
                }
            }
            return ExecutionResult.SUCCESS_WITH_ALERT;
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
        GregorianCalendar gc = new GregorianCalendar(2008, 6, 25, 12, 0, 0);
        return gc.getTime();
    }

}
