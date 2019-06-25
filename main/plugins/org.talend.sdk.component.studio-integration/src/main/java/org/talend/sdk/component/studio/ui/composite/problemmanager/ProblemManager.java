// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.talend.core.model.process.IElementParameter;
import org.talend.sdk.component.studio.ui.composite.TaCoKitComposite;
import org.talend.sdk.component.studio.util.TaCoKitUtil;

/**
 * DOC cmeng  class global comment. Detailled comment
 */
public class ProblemManager implements IProblemManager {

    private Set<TaCoKitComposite> tckComposites = new LinkedHashSet<>();

    private Map<IElementParameter, String> errMap = new LinkedHashMap<>();

    private Map<IElementParameter, String> warnMap = new LinkedHashMap<>();

    private Map<IElementParameter, String> infoMap = new LinkedHashMap<>();

    private Set<IElementParameter> unresolvedRequriedElems = new LinkedHashSet<>();

    @Override
    public Set<TaCoKitComposite> getTckComposites() {
        return tckComposites;
    }

    @Override
    public void registTckComposite(TaCoKitComposite tckComposite) {
        this.tckComposites.add(tckComposite);
    }

    @Override
    public void unregistTckComposite(TaCoKitComposite tckComposite) {
        this.tckComposites.remove(tckComposite);
    }

    public Map<IElementParameter, String> getErrMap() {
        return errMap;
    }

    @Override
    public void setError(IElementParameter ep, String errMsg) {
        if (TaCoKitUtil.isBlank(errMsg)) {
            errMap.remove(ep);
        } else {
            errMap.put(ep, errMsg);
        }
    }

    @Override
    public boolean hasError() {
        return !errMap.isEmpty();
    }

    public Map<IElementParameter, String> getWarnMap() {
        return warnMap;
    }

    @Override
    public void setWarn(IElementParameter ep, String warnMsg) {
        if (TaCoKitUtil.isBlank(warnMsg)) {
            warnMap.remove(ep);
        } else {
            warnMap.put(ep, warnMsg);
        }
    }

    @Override
    public boolean hasWarn() {
        return !warnMap.isEmpty();
    }

    public Map<IElementParameter, String> getInfoMap() {
        return infoMap;
    }

    @Override
    public void setInfo(IElementParameter ep, String infoMsg) {
        if (TaCoKitUtil.isBlank(infoMsg)) {
            infoMap.remove(ep);
        } else {
            infoMap.put(ep, infoMsg);
        }
    }

    @Override
    public boolean hasInfo() {
        return !infoMap.isEmpty();
    }

    @Override
    public void cleanMsg(IElementParameter ep) {
        errMap.remove(ep);
        warnMap.remove(ep);
        infoMap.remove(ep);
    }

    @Override
    public void addUnresolvedRequriedElem(IElementParameter ep) {
        unresolvedRequriedElems.add(ep);
    }

    @Override
    public void removeUnresolvedRequriedElem(IElementParameter ep) {
        unresolvedRequriedElems.remove(ep);
    }

    @Override
    public boolean hasUnresolvedRequiredElem() {
        return !unresolvedRequriedElems.isEmpty();
    }
}
