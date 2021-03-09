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

import org.eclipse.emf.common.util.EList;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class AddScreenshotFileMigrationTask extends AbstractJobMigrationTask {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.migration.IProjectMigrationTask#getOrder()
     */
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2011, 7, 27, 18, 0, 0);
        return gc.getTime();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.AbstractItemMigrationTask#execute(org.talend.core.model.properties.Item)
     */
    @Override
    public ExecutionResult execute(Item item) {
        byte[] screenshotStream = null;
        EList nodeList = null;
        try {
            if (item instanceof ProcessItem) {
                ProcessItem process = (ProcessItem) item;
                screenshotStream = process.getProcess().getScreenshot();
                process.getProcess().getScreenshots().put("process", screenshotStream);
                nodeList = process.getProcess().getNode();
                for (int i = 0; i < nodeList.size(); i++) {
                    String nodeName;
                    NodeType node = (NodeType) nodeList.get(i);
                    if (node.getScreenshot() != null && node.getScreenshot().length > 0) {
                        EList params = node.getElementParameter();
                        for (int j = 0; j < params.size(); j++) {
                            ElementParameterType type = (ElementParameterType) params.get(j);
                            if ("UNIQUE_NAME".equals(type.getName())) {
                                nodeName = type.getValue();
                                process.getProcess().getScreenshots().put(nodeName, node.getScreenshot());
                                break;
                            }
                        }
                        node.setScreenshot(null);
                    }
                }
                process.getProcess().setScreenshot(null);
            } else if (item instanceof JobletProcessItem) {
                JobletProcessItem joblet = (JobletProcessItem) item;
                screenshotStream = ((JobletProcessItem) item).getJobletProcess().getScreenshot();
                joblet.getJobletProcess().getScreenshots().put("process", screenshotStream);
                nodeList = joblet.getJobletProcess().getNode();
                for (int i = 0; i < nodeList.size(); i++) {
                    String nodeName;
                    NodeType node = (NodeType) nodeList.get(i);
                    if (node.getScreenshot() != null && node.getScreenshot().length > 0) {
                        EList params = node.getElementParameter();
                        for (int j = 0; j < params.size(); j++) {
                            ElementParameterType type = (ElementParameterType) params.get(j);
                            if ("UNIQUE_NAME".equals(type.getName())) {
                                nodeName = type.getValue();
                                joblet.getJobletProcess().getScreenshots().put(nodeName, node.getScreenshot());
                                break;
                            }
                        }
                        node.setScreenshot(null);
                    }
                }
                joblet.getJobletProcess().setScreenshot(null);
            }
            ProxyRepositoryFactory.getInstance().save(item, true);
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
        return ExecutionResult.SUCCESS_NO_ALERT;
    }

}
