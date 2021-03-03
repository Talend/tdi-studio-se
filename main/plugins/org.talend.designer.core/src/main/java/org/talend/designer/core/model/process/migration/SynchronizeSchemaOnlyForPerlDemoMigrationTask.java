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
package org.talend.designer.core.model.process.migration;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Project;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.ui.editor.cmd.ChangeMetadataCommand;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.joblet.model.JobletProcess;
import org.talend.repository.ProjectManager;
import org.talend.repository.RepositoryPlugin;

/**
 * ggu class global comment. Detailled comment.
 *
 * @deprecated work for such as tFileCompare, tLogCatcher, tStatCatcher.
 */
// Bug : 5640 have deleted this class's extensions
public class SynchronizeSchemaOnlyForPerlDemoMigrationTask extends AbstractJobMigrationTask {

    private static List<String> syncComponents = new ArrayList<String>();
    static {
        // have be added in demo
        syncComponents.add("tFileCompare"); //$NON-NLS-1$
        syncComponents.add("tLogCatcher"); //$NON-NLS-1$
        syncComponents.add("tStatCatcher"); //$NON-NLS-1$

        // not be added in demo. maybe, not required

        //        syncComponents.add("tAssertCatcher"); //$NON-NLS-1$
        //        syncComponents.add("tFlowMeterCatcher"); //$NON-NLS-1$
        //        syncComponents.add("tDTDValidator"); //$NON-NLS-1$
        //        syncComponents.add("tXSDValidator"); //$NON-NLS-1$
    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2008, 11, 24, 12, 0, 0);
        return gc.getTime();
    }

    /*
     * (non-Javadoc)
     *
     * @seeorg.talend.core.model.migration.AbstractJobMigrationTask#executeOnProcess(org.talend.core.model.properties.
     * ProcessItem)
     */
    @Override
    public ExecutionResult execute(Item item) {
        ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        try {
            boolean modified = false;
            ECodeLanguage language = LanguageManager.getCurrentLanguage();
            if (language == ECodeLanguage.PERL) {
                Project project = ProjectManager.getInstance().getProject(item);
                if (project != null && project.getTechnicalLabel().equalsIgnoreCase("TALENDDEMOSPERL")) { //$NON-NLS-1$
                    IProcess2 process = (IProcess2) RepositoryPlugin.getDefault().getDesignerCoreService().getProcessFromItem(
                            item);
                    modified = synchronizeSchema(process);

                    if (modified) {
                        ProcessType processType = process.saveXmlFile();
                        if (item instanceof ProcessItem) {
                            ((ProcessItem) item).setProcess(processType);
                        } else if (item instanceof JobletProcessItem) {
                            ((JobletProcessItem) item).setJobletProcess((JobletProcess) processType);
                        }
                        factory.save(item, true);
                        return ExecutionResult.SUCCESS_NO_ALERT;
                    }
                }
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
        return ExecutionResult.NOTHING_TO_DO;
    }

    private boolean synchronizeSchema(IProcess2 process) {
        List<? extends INode> graphicalNodes = process.getGraphicalNodes();
        boolean modified = false;
        for (INode node : graphicalNodes) {
            if (syncComponents.contains(node.getComponent().getName())) {
                IElementParameter param = node.getElementParameter(EParameterName.SCHEMA_TYPE.getName());
                if (param != null) {
                    IMetadataTable meta = node.getMetadataFromConnector(param.getContext());
                    if (meta != null) {
                        IMetadataTable metaCopy = meta.clone(true);
                        ChangeMetadataCommand cmd = new ChangeMetadataCommand((Node) node, param, meta, metaCopy);
                        cmd.execute(true);
                        modified = true;
                    }
                }
            }
        }
        return modified;
    }
}
