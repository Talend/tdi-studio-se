package org.talend.repository.model.migration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

public class UncheckMoveToRemoteDirSFTP extends AbstractJobMigrationTask {

    private static final String SFTP_PROPERTY_NAME = "SFTP"; //$NON-NLS-1$

    private static final String CONNECTION_PROPERTY_NAME = "CONNECTION"; //$NON-NLS-1$

    private static final String USE_CONNECTION_PROPERTY_NAME = "USE_EXISTING_CONNECTION"; //$NON-NLS-1$

    private static final String MOVE_TO_DIR_PROPERTY_NAME = "MOVE_TO_THE_CURRENT_DIRECTORY"; //$NON-NLS-1$

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2018, 9, 13, 15, 0, 0);
        return gc.getTime();
    }

    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        if (getProject().getLanguage() != ECodeLanguage.JAVA || processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }
            List<String> componentsNameToAffect = new ArrayList<>();
            componentsNameToAffect.add("tFTPDelete"); //$NON-NLS-1$
            componentsNameToAffect.add("tFTPFileExist"); //$NON-NLS-1$
            componentsNameToAffect.add("tFTPFileList"); //$NON-NLS-1$
            componentsNameToAffect.add("tFTPGet"); //$NON-NLS-1$
            componentsNameToAffect.add("tFTPPut"); //$NON-NLS-1$
            componentsNameToAffect.add("tFTPRename"); //$NON-NLS-1$
            componentsNameToAffect.add("tFTPTruncate"); //$NON-NLS-1$

            IComponentConversion uncheckMoveToCurrentDirConversion = new IComponentConversion() {

                @Override
                public void transform(NodeType node) {
                    if ((ComponentUtilities.getNodeProperty(node, USE_CONNECTION_PROPERTY_NAME) != null && "true"
                            .equals(ComponentUtilities.getNodePropertyValue(node, USE_CONNECTION_PROPERTY_NAME)))) {
                        String connectionComponentName = ComponentUtilities.getNodePropertyValue(node, CONNECTION_PROPERTY_NAME);
                        NodeType connectionNode = ComponentUtilities.getNodeTypeFromUniqueName(processType,
                                connectionComponentName);
                        if (isConnectionUseSFTP(connectionNode)) {
                            ComponentUtilities.setNodeValue(node, MOVE_TO_DIR_PROPERTY_NAME, "false");
                        }
                    }

                }

                private boolean isConnectionUseSFTP(NodeType connectionNode) {
                    return "true".equals(ComponentUtilities.getNodePropertyValue(connectionNode, SFTP_PROPERTY_NAME));
                }

            };

            for (String componentName : componentsNameToAffect) {
                IComponentFilter componentFilter = new NameComponentFilter(componentName);
                try {
                ModifyComponentsAction.searchAndModify(item, processType, componentFilter,
                        Arrays.<IComponentConversion> asList(uncheckMoveToCurrentDirConversion));
                } catch (PersistenceException e) {
                    ExceptionHandler.process(e);
                    return ExecutionResult.FAILURE;
                }
            }
            return ExecutionResult.SUCCESS_NO_ALERT;
    }

}
