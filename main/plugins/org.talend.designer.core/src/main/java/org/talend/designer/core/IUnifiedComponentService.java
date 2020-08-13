package org.talend.designer.core;

import java.util.Collection;
import java.util.List;

import org.talend.core.IService;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IComponentsFactory;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.model.utils.IComponentName;

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

/**
 * created by wchen on Dec 6, 2017 Detailled comment
 *
 */
public interface IUnifiedComponentService extends IService {

    public boolean isDelegateComponent(IComponent component);

    public boolean isUnifiedComponent(IComponent component);

    /**
     *
     * This function is used for drag&drop items from repository
     *
     * @param setting
     * @param selectedComponent
     * @return
     */
    public String getUnifiedComponetName4DndFromRepository(IComponentName setting, IComponent selectedComponent);

    /**
     *
     * Get the delegate component
     *
     * @param component
     * @return
     */
    public IComponent getDelegateComponent(IComponent component);

    /**
     *
     * Get the delegate component from name
     *
     * @param delegateCompName
     * @param paletteType
     * @return
     */
    public IComponent getDelegateComponent(String delegateCompName, String paletteType);

    public Collection<IComponent> getDelegateComponents(String paletteType);

    /**
     *
     * Create parameters for delegate component
     *
     * @param node
     * @param listParams
     * @param delegateComp
     * @param emfComp
     */
    public void createParameters(INode node, List<IElementParameter> listParams, IComponent delegateComp, IComponent emfComp);

    public void switchComponent(INode node, IComponent delegateComponent, String oldEmfComponent,
            List<? extends IElementParameter> oldParms, List<IMetadataTable> oldMetadataTables,
            List<INodeConnector> oldConnectors);

    public String getUnifiedCompDisplayName(IComponent delegateComponent, String emfComponent);

    public String getUnifiedCompRealComponentName(IComponent delegateComponent, String emfComponent);

    public void filterUnifiedComponentForPalette(IComponentsFactory compFac, Collection<IComponent> componentSet,
            String lowerCasedKeyword);

    public String getComponentDisplayNameForPalette(IComponent delegateComponent, String keyWord);

    public IComponent getUnifiedComponentByFilter(IComponent delegateComponent, String filter);

    public void initComponentIfJDBC(INode node, IComponent delegateComponent);
}
