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
package org.talend.designer.core.ui.editor.properties.controllers.uidialog.tns;

import org.eclipse.gef.commands.Command;
import org.talend.core.model.process.IElement;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.views.properties.ComponentSettings;

/**
 *
 * rshi class global comment. Detailled comment
 */
public class TnsPropertyCommand extends Command {

    private TnsInfo tnsInfo;

    private TnsInfo oldTnsInfo = new TnsInfo();

    private final IElement elem;

    public TnsPropertyCommand(TnsInfo tnsInfo, IElement elem) {
        super();
        this.tnsInfo = tnsInfo;
        this.elem = elem;
    }

    @Override
    public void execute() {
        elem.getElementParameter(EParameterName.HOST.getName()).setValue(TalendTextUtils.addQuotes(tnsInfo.getHost()));
        elem.getElementParameter(EParameterName.PORT.getName()).setValue(TalendTextUtils.addQuotes(tnsInfo.getPort()));
        elem.getElementParameter(EParameterName.DBNAME.getName()).setValue(TalendTextUtils.addQuotes(tnsInfo.getDbName()));
        elem.getElementParameter(EParameterName.CONNECTION_TYPE.getName()).setValue(tnsInfo.getConnectionType());

        oldTnsInfo.setConnectionType((String) elem.getElementParameter(EParameterName.CONNECTION_TYPE.getName()).getValue());
        oldTnsInfo.setDbName((String) elem.getElementParameter(EParameterName.DBNAME.getName()).getValue());
        oldTnsInfo.setPort((String) elem.getElementParameter(EParameterName.PORT.getName()).getValue());
        oldTnsInfo.setHost((String) elem.getElementParameter(EParameterName.HOST.getName()).getValue());

        ComponentSettings.switchToCurComponentSettingsView();
    }

    @Override
    public void undo() {
        elem.getElementParameter(EParameterName.HOST.getName()).setValue(oldTnsInfo.getHost());
        elem.getElementParameter(EParameterName.PORT.getName()).setValue(oldTnsInfo.getPort());
        elem.getElementParameter(EParameterName.DBNAME.getName()).setValue(oldTnsInfo.getDbName());
        elem.getElementParameter(EParameterName.CONNECTION_TYPE.getName()).setValue(oldTnsInfo.getConnectionType());

        ComponentSettings.switchToCurComponentSettingsView();

    }

}
