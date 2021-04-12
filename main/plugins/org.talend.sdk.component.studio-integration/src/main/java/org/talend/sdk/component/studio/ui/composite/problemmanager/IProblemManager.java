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
package org.talend.sdk.component.studio.ui.composite.problemmanager;

import java.util.Set;

import org.talend.core.model.process.IElementParameter;
import org.talend.sdk.component.studio.ui.composite.TaCoKitComposite;

/**
 * DOC cmeng class global comment. Detailled comment
 */
public interface IProblemManager {

    Set<TaCoKitComposite> getTckComposites();

    void registTckComposite(TaCoKitComposite tckComposite);

    void unregistTckComposite(TaCoKitComposite tckComposite);

    void setError(IElementParameter ep, String errMsg);

    void setWarn(IElementParameter ep, String warnMsg);

    void setInfo(IElementParameter ep, String infoMsg);

    void cleanMsg(IElementParameter ep);

    void addUnresolvedRequriedElem(IElementParameter ep);

    void removeUnresolvedRequriedElem(IElementParameter ep);

    boolean hasUnresolvedRequiredElem();

    boolean hasInfo();

    boolean hasWarn();

    boolean hasError();

}
