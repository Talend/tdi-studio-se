package org.talend.designer.runprocess.maven.listener;

import java.util.ArrayList;
import java.util.List;

import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.components.IComponentsFactory;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProjectRepositoryNode;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.editor.update.UpdateManagerUtils;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.RepositoryNode;

public class PropertyProcessUpdater {
	
    private static List<ProcessItem> getProcesses(List<? extends IRepositoryNode> nodes) {
        List<ProcessItem> value = new ArrayList<ProcessItem>();
        for (IRepositoryNode node : nodes) {
            if (node.getType() == ENodeType.SYSTEM_FOLDER || node.getType() == ENodeType.SIMPLE_FOLDER) {
                value.addAll(getProcesses(node.getChildren()));
            }
            if (node.getType() == ENodeType.REPOSITORY_ELEMENT) {
                IRepositoryViewObject repositoryObject = node.getObject();
                if (repositoryObject.getProperty().getItem() instanceof ProcessItem) {
                    ProcessItem processItem = (ProcessItem) repositoryObject.getProperty().getItem();
                    value.add(processItem);
                }
            }
        }
        return value;
    }

    public static void updateRouteCode(String originalObjectName) {
        RepositoryNode repNode =
                ProjectRepositoryNode.getInstance().getRootRepositoryNode(ERepositoryObjectType.PROCESS_ROUTE);

        //update component properties
		IComponentsFactory componentsFactory = ComponentsFactoryProvider.getInstance();
        componentsFactory.resetSpecificComponents();

        List<IRepositoryNode> repNodeList = new ArrayList<IRepositoryNode>();
        repNodeList.add(repNode);

        List<IProcess2> processes = UpdateManagerUtils.getOpenedProcess();

        List<ProcessItem> prItems = getProcesses(repNodeList);
        for (ProcessItem refreshedProcessItem : prItems) {
            IProcess2 processItem = null;

            //selects processes that have current routelet in one of the nodes
            if(ProcessorUtilities.hasRoutelet(refreshedProcessItem, originalObjectName)) {

                for (int j = 0; j < processes.size(); j++) {
                    if (refreshedProcessItem.getProperty().getLabel().equals(processes.get(j).getProperty().getLabel())) {
                        processItem = processes.get(j);
                        break;
                    }
                }

                try {
                    if (processItem != null) { //refresh properties of process items of opened editors
                        refreshProcess(processItem, refreshedProcessItem, true);
                    }

                    //refresh process code
                    ProcessorUtilities.generateCode(refreshedProcessItem, refreshedProcessItem.getProcess().getDefaultContext(), false, false, true, ProcessorUtilities.GENERATE_MAIN_ONLY);
                } catch (ProcessorException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void refreshProcess(IProcess2 process, final Item refreshedItem, boolean force) {
     	process.setProperty(refreshedItem.getProperty());
     	process.updateProperties();
     	ProcessType processType = null;
     	if (refreshedItem instanceof ProcessItem) {
     		processType = ((ProcessItem) refreshedItem).getProcess();
     	} else if (refreshedItem instanceof JobletProcessItem) {
     		processType = ((JobletProcessItem) refreshedItem).getJobletProcess();
     	} else {
     		ExceptionHandler.process(new Exception("Mismatched case")); //$NON-NLS-1$
     	}
     	if (processType != null) {
     		((Process) process).updateProcess(processType);
     	}
     	process.refreshProcess();
     }

}
