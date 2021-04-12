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
package org.talend.repository.generic.handler;

import java.util.List;
import java.util.Set;

import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.metadata.managment.ui.model.IConnParamName;

/**
 * created by ycbai on 2015年11月20日 Detailled comment
 *
 */
public interface IContextHandler {

    public Set<IConnParamName> getContextParams();

    public boolean exportContext(ConnectionItem connectionItem);

    public boolean revertContext(ConnectionItem connectionItem);

    public void setParameters(List<IElementParameter> parameters);

}
