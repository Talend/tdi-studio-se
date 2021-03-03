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
package org.talend.designer.core.ui.editor.properties.controllers;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ITDQRepositoryService;
import org.talend.core.model.process.IElementParameter;
import org.talend.designer.core.ui.editor.nodes.Node;

/**
 * DOC klliu class global comment. Detailled comment
 */
public class ExportRulesToRepository implements SelectionListener {

    private Node node;

    /**
     * DOC klliu ExportRulesToRepository constructor comment.
     *
     * @param generateGrammarController
     */
    public ExportRulesToRepository(GenerateGrammarController generateGrammarController) {
        node = (Node) generateGrammarController.elem;
    }

    public void widgetSelected(SelectionEvent e) {
        IElementParameter elementParameter = node.getElementParameter("RULE_TABLE");
        ArrayList<HashMap<String, Object>> value = (ArrayList<HashMap<String, Object>>) elementParameter.getValue();
        ;
        ITDQRepositoryService tdqRepService = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(ITDQRepositoryService.class)) {
            tdqRepService = (ITDQRepositoryService) GlobalServiceRegister.getDefault().getService(ITDQRepositoryService.class);
        }
        String elementName = node.getProcess().getName() + "_" + node.getNodeLabel().getElementName();
        tdqRepService.createParserRuleItem(value, elementName);
    }

    public void widgetDefaultSelected(SelectionEvent e) {
    }
}
