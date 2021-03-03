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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.hadoop.HadoopConstants;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.Problem.ProblemStatus;
import org.talend.core.model.process.ProcessUtils;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.ui.IJobletProviderService;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.views.problems.Problems;

/**
 * @author hwang
 *
 */
public class TRunjobUtil {
	
	public void checkTRunjobRecursiveLoop(Node node) {
    	List<String> idList = new ArrayList<>();
    	try {
    		if (node.getComponent() != null && "tRunJob".equals(node.getComponent().getName())) {  //$NON-NLS-1$
                IElementParameter thisElement = node.getElementParameter(EParameterName.PROCESS.getName());
                if(thisElement != null && !StringUtils.isBlank(thisElement.getValue().toString())) {
                	Object idObj = thisElement.getChildParameters().get(EParameterName.PROCESS_TYPE_PROCESS.getName()).getValue();
                	if(!StringUtils.isBlank(idObj.toString())) {
                		int loop = 0;
                		String id = idObj.toString().substring(idObj.toString().indexOf(":") + 1);
                		String proId = loop + "&" + id;  //$NON-NLS-1$
                		idList.add(proId);
                		IRepositoryViewObject obj = ProxyRepositoryFactory.getInstance().getLastVersion(id);
                		if(obj == null || obj.getProperty() == null || obj.getProperty().getItem() == null) {
                			return;
                		}
                		Item item = obj.getProperty().getItem();
                		if(item instanceof ProcessItem) {
                			ProcessType process = ((ProcessItem)item).getProcess();
                			boolean result = isInLoop(process, idList, loop + 1);
                			if(result) {
                				String message = Messages.getString("Node.inLoop", node.getUniqueName()); //$NON-NLS-1$
                                Problems.add(ProblemStatus.WARNING, node, message);
                			}
                		}
                	}
                }
                
            }else {
                if(GlobalServiceRegister.getDefault().isServiceRegistered(IJobletProviderService.class)) {
                    IJobletProviderService service = (IJobletProviderService) GlobalServiceRegister.getDefault().getService(
                            IJobletProviderService.class);
                    if (service != null && service.isJobletComponent(node)) {
                        ProcessType processType = service.getJobletProcess(node.getComponent());
                        boolean result = isInLoop(processType, idList, 1);
                       if(result) {
                           String message = Messages.getString("Node.inLoop", node.getUniqueName()); //$NON-NLS-1$
                            Problems.add(ProblemStatus.WARNING, node, message);
                       }
                    }
                }
            }
		} catch (PersistenceException e) {
			ExceptionHandler.process(e);
		}
    }
    
    private boolean isInLoop(ProcessType process, List<String> idList, int loop) throws PersistenceException{
    	if(process == null) {
    		return false;
    	}
    	List<NodeType> nodeList = process.getNode();
		for(NodeType nodeTye : nodeList) {
            String subid = null;
            String id = null;
			boolean isJoblet = false;
            boolean nodeActivate = true;
			List<ElementParameterType> typeList = nodeTye.getElementParameter();
	    	for(ElementParameterType eType : typeList) {
	    		if("PROCESS:PROCESS_TYPE_PROCESS".equals(eType.getName())) {//$NON-NLS-1$
                    id = ProcessUtils.getPureItemId(eType.getValue());
                    subid = loop + "&" + id;//$NON-NLS-1$
                } else if ("ACTIVATE".equals(eType.getName())) {
                    nodeActivate = Boolean.parseBoolean(eType.getValue());
	    		}else if("FAMILY".equals(eType.getName()) && "Joblets".equals(eType.getValue())) {
	    			isJoblet = true;
	    		}
	    	}
	    	
            if (nodeActivate && StringUtils.isNotBlank(id)) {
                for (String pid : idList) {
                    int parent = Integer.parseInt(pid.substring(0, pid.indexOf("&")));//$NON-NLS-1$
                    String child = pid.substring(pid.indexOf("&") + 1, pid.length());//$NON-NLS-1$
                    if (parent != loop && child.equals(id)) {
                        return true;
                    }
                }
                idList.add(subid);
                
                IRepositoryViewObject obj = ProxyRepositoryFactory.getInstance().getLastVersion(id);
                if (obj == null || obj.getProperty() == null || obj.getProperty().getItem() == null) {
                    return false;
                }
                Item item = obj.getProperty().getItem();
                if (item instanceof ProcessItem) {
                    ProcessType subprocess = ((ProcessItem) item).getProcess();
                    boolean result = isInLoop(subprocess, idList, loop + 1);
                    if (result) {
                        return result;
                    }
                }
            }

            if (isJoblet && nodeActivate) {
	    		String jobletPaletteType = null;
		        String frameWork = process.getFramework();
		        if (StringUtils.isBlank(frameWork)) {
		            jobletPaletteType = ComponentCategory.CATEGORY_4_DI.getName();
		        } else if (frameWork.equals(HadoopConstants.FRAMEWORK_SPARK)) {
		            jobletPaletteType = ComponentCategory.CATEGORY_4_SPARK.getName();
		        } else if (frameWork.equals(HadoopConstants.FRAMEWORK_SPARK_STREAMING)) {
		            jobletPaletteType = ComponentCategory.CATEGORY_4_SPARKSTREAMING.getName();
		        }
		        
		        if(GlobalServiceRegister.getDefault().isServiceRegistered(IJobletProviderService.class)) {
		            IJobletProviderService service =
	                        (IJobletProviderService) GlobalServiceRegister.getDefault().getService(
	                                IJobletProviderService.class);
	                if (service != null) {
	                    IComponent jobletComponent = service.getJobletComponent(nodeTye, jobletPaletteType);
	                    if(jobletComponent != null) {
	                    	ProcessType jobletProcess = service.getJobletProcess(jobletComponent);
		                    if(jobletProcess != null) {
		                    	boolean result = isInLoop(jobletProcess, idList, loop + 1);
			                    if(result) {
			                        return result;
			                    }
		                    }
	                    }
	                    
	                }
		        }
		    	
	    	}
	    	
		}
    	
    	return false;
    }

}
