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
package org.talend.designer.core.ui.editor.cmd;

import java.util.List;

import org.eclipse.gef.commands.Command;
import org.talend.core.model.metadata.QueryUtil;
import org.talend.core.model.metadata.builder.connection.Query;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.EmfComponent;

/**
 * DOC admin class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 *
 */
public class RepositoryChangeQueryCommand extends Command {

    private IElement elem;

    private Query query;

    private String value;

    private String propertyName;

    private Object oldMetadata;

    private Object oldValue;

    /**
     * DOC admin ChangeQueryCommand constructor comment.
     */
    public RepositoryChangeQueryCommand(IElement elem, Query query, String propertyName, String value) {
        this.elem = elem;
        this.query = query;
        this.value = value;
        this.propertyName = propertyName;

        setLabel(Messages.getString("PropertyChangeCommand.Label")); //$NON-NLS-1$
    }

    @SuppressWarnings("unchecked")//$NON-NLS-1$
    @Override
    public void execute() {
        // Force redraw of Commponents propoerties
        elem.setPropertyValue(EParameterName.UPDATE_COMPONENTS.getName(), new Boolean(true));
        if (propertyName.equals(EParameterName.QUERYSTORE_TYPE.getName()) && (EmfComponent.BUILTIN.equals(value))) {
            for (IElementParameter param : elem.getElementParameters()) {
                if (param.getFieldType() == EParameterFieldType.MEMO_SQL) {
                    param.setRepositoryValueUsed(false);
                    param.setReadOnly(false);
                }
            }
        } else {
            for (IElementParameter param : (List<IElementParameter>) elem.getElementParameters()) {
                if (param.getFieldType() == EParameterFieldType.MEMO_SQL) {
                    // modified by hyWang
                    String queryStr = query.getValue();
                    if (!query.isContextMode()) {
                        queryStr = QueryUtil.checkAndAddQuotes(query.getValue());
                    }
                    elem.setPropertyValue(param.getName(), queryStr);
                    oldValue = elem.getPropertyValue(param.getName());
                    param.setRepositoryValueUsed(true);
                    param.setReadOnly(true);
                }
            }
        }
        if (propertyName.equals(EParameterName.QUERYSTORE_TYPE.getName())) {
            elem.setPropertyValue(EParameterName.QUERYSTORE_TYPE.getName(), value);
        } else {
            oldMetadata = elem.getPropertyValue(EParameterName.REPOSITORY_QUERYSTORE_TYPE.getName());
            elem.setPropertyValue(EParameterName.REPOSITORY_QUERYSTORE_TYPE.getName(), value);
        }
    }

    @SuppressWarnings("unchecked")//$NON-NLS-1$
    @Override
    public void undo() {
        // Force redraw of Commponents propoerties
        elem.setPropertyValue(EParameterName.UPDATE_COMPONENTS.getName(), new Boolean(true));

        if (propertyName.equals(EParameterName.QUERYSTORE_TYPE.getName()) && (EmfComponent.BUILTIN.equals(value))) {
            for (IElementParameter param : elem.getElementParameters()) {
                String repositoryValue = param.getRepositoryValue();
                if (param.isShow(elem.getElementParameters()) && (repositoryValue != null)
                        && (!param.getName().equals(EParameterName.QUERYSTORE_TYPE.getName()))) {
                    param.setRepositoryValueUsed(true);
                }
            }
        } else {
            for (IElementParameter param : (List<IElementParameter>) elem.getElementParameters()) {
                if (param.getFieldType() == EParameterFieldType.MEMO_SQL) {
                    elem.setPropertyValue(param.getName(), oldValue);
                    param.setRepositoryValueUsed(false);
                }
            }
        }
        if (propertyName.equals(EParameterName.QUERYSTORE_TYPE.getName())) {
            if (value.equals(EmfComponent.BUILTIN)) {
                elem.setPropertyValue(EParameterName.QUERYSTORE_TYPE.getName(), EmfComponent.REPOSITORY);
            } else {
                elem.setPropertyValue(EParameterName.QUERYSTORE_TYPE.getName(), EmfComponent.BUILTIN);
            }
        } else {
            elem.setPropertyValue(EParameterName.REPOSITORY_QUERYSTORE_TYPE.getName(), oldMetadata);
        }
    }
}
