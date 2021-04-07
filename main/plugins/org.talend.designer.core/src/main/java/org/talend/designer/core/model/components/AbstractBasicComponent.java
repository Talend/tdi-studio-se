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
package org.talend.designer.core.model.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.talend.core.model.components.IMultipleComponentManager;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.temp.ECodePart;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.editor.nodes.Node;

/**
 * created by hcyi on Mar 22, 2016 Detailled comment
 *
 */
public abstract class AbstractBasicComponent extends AbstractComponent {

    public static final String BUILTIN = "BUILT_IN"; //$NON-NLS-1$

    public static final String REPOSITORY = "REPOSITORY"; //$NON-NLS-1$

    public static final String TEXT_BUILTIN = Messages.getString("AbstractBasicComponent.builtIn"); //$NON-NLS-1$

    public static final String TEXT_REPOSITORY = Messages.getString("AbstractBasicComponent.repository"); //$NON-NLS-1$

    protected List<ECodePart> codePartListX;

    protected Map<String, ImageDescriptor> imageRegistry = new HashMap<>();

    private List<IMultipleComponentManager> multipleComponentManagers;

    private boolean initializing;

    protected Boolean visible = null;

    protected Boolean technical = null;

    protected Boolean visibleFromComponentDefinition = null;

    private boolean isNeedMigration;

    @Override
    public void setImageRegistry(Map<String, ImageDescriptor> imageRegistry) {
        this.imageRegistry = imageRegistry;
    }

    @Override
    public Map<String, ImageDescriptor> getImageRegistry() {
        return imageRegistry;
    }

    @Override
    public List<IMultipleComponentManager> getMultipleComponentManagers() {
        if (multipleComponentManagers == null) {
            multipleComponentManagers = createMultipleComponentManagers();
        }// else already exist so return it
        return multipleComponentManagers;
    }

    protected List<IMultipleComponentManager> createMultipleComponentManagers() {
        return new ArrayList<>();
    }

    @Override
    public boolean isLoaded() {
        return true;
    }

    @Override
    public String getPathSource() {
        return null;
    }

    @Override
    public List<String> getPluginDependencies() {
        return new ArrayList<>();
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    @Override
    public boolean isVisible(String family) {
        return true;
    }

    @Override
    public boolean isVisibleInComponentDefinition() {
        return false;
    }

    @Override
    public boolean useMerge() {
        // TUP-4149
        return false;
    }

    public boolean useFlow() {
        // TUP-4149
        return false;
    }

    public boolean useSchema(Node node) {
        // TUP-4149
        return false;
    }

    @Override
    public boolean isMultiplyingOutputs() {
        // TUP-4150
        return false;
    }

    @Override
    public boolean isMultipleOutput() {
        // TUP-4150
        return false;
    }

    public boolean isMultiSchemaOutput() {
        // TUP-4150
        return false;
    }

    @Override
    public boolean hasConditionalOutputs() {
        return false;
    }

    @Override
    public boolean useLookup() {
        return false;
    }

    @Override
    public boolean useImport() {
        return false;
    }

    @Override
    public boolean isHashComponent() {
        return false;
    }

    @Override
    public boolean isTechnical() {
        return false;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    @Override
    public boolean isMainCodeCalled() {
        return false;
    }

    @Override
    public boolean canParallelize() {
        return false;
    }

    @Override
    public String getPartitioning() {
        return "";//$NON-NLS-1$
    }

    @Override
    public String getOutputType() {
        return "";//$NON-NLS-1$
    }

    @Override
    public String getInputType() {
        return "";//$NON-NLS-1$
    }

    @Override
    public String getCombine() {
        return "";//$NON-NLS-1$
    }

    @Override
    public String getVersion() {
        return "";//$NON-NLS-1$
    }

    @Override
    public IProcess getProcess() {
        return null;
    }

    @Override
    public String getPluginExtension() {
        return null;
    }

    public void initNodeProperties(INode newNode, INode oldNode) {
        // do nothing
    }

    public void initNodePropertiesFromSerialized(INode node, String serialized) {
        // do nothing
    }

    public String genericToSerialized(IElementParameter param) {
        return null;
    }

    public Object getElementParameterValueFromComponentProperties(INode iNode, IElementParameter param) {
        return null;
    }

    public boolean setGenericPropertyValue(IElementParameter param) {
        return false;
    }

    public boolean isInitializing() {
        return this.initializing;
    }

    public void setInitializing(boolean initializing) {
        this.initializing = initializing;
    }

    public boolean isNeedMigration() {
        return this.isNeedMigration;
    }

    public void setNeedMigration(boolean isNeedMigration) {
        this.isNeedMigration = isNeedMigration;
    }
}
