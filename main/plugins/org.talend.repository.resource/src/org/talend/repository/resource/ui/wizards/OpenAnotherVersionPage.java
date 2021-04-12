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
package org.talend.repository.resource.ui.wizards;

import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.designer.core.ui.wizards.OpenExistVersionProcessPage;

public class OpenAnotherVersionPage extends OpenExistVersionProcessPage {

    protected OpenAnotherVersionPage(boolean alreadyEditedByUser, IRepositoryViewObject processObject) {
        super(alreadyEditedByUser, processObject);
    }

}
