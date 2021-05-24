package org.talend.repository.ui.exception;
//============================================================================
//
//Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
//This source code is available under agreement available at
//%InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
//You should have received a copy of the agreement
//along with this program; if not, write to Talend SA
//9 rue Pages 92150 Suresnes, France
//
//============================================================================
import java.util.ArrayList;
import java.util.List;

import org.talend.commons.exception.WarningException;
import org.talend.core.model.repository.IRepositoryViewObject;


public class ImportInvalidObjectException extends WarningException {
    private static final long serialVersionUID = -1049573770315084145L;

    List<IRepositoryViewObject> invalidViewObjectList = new ArrayList<IRepositoryViewObject>();
    public ImportInvalidObjectException(String message) {
        super(message);
    }

    public List<IRepositoryViewObject> getInvalidViewObjectList() {
        return invalidViewObjectList;
    }

    public void setInvalidViewObjectList(List<IRepositoryViewObject> invalidViewObjectList) {
        this.invalidViewObjectList = invalidViewObjectList;
    }
}
