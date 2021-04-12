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
package org.talend.designer.core.model.components;

import org.eclipse.jface.resource.ImageDescriptor;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.ui.editor.StitchDataLoaderConstants;

public class StitchPseudoComponent extends DummyComponent {

    private String connectorURL;

    private String description;

    private String familyName;

    private static final ImageDescriptor STITCH_ICON_DESCRIPTOR_32 =
            DesignerPlugin.getImageDescriptor("icons/stitch_data_loader_32.png");

    private static final ImageDescriptor STITCH_ICON_DESCRIPTOR_24 =
            DesignerPlugin.getImageDescriptor("icons/stitch_data_loader_24.png");

    private static final ImageDescriptor STITCH_ICON_DESCRIPTOR_16 =
            DesignerPlugin.getImageDescriptor("icons/stitch_data_loader_16.png");

    public StitchPseudoComponent(String componentName, String category, String connectorURL, String description) {
        super(componentName);
        this.connectorURL = connectorURL;
        this.description = description;
        this.familyName = StitchDataLoaderConstants.CONNECTOR_FAMILY_NAME + "/" + category;
    }

    public String getConnectorURL() {
        return connectorURL; // $NON-NLS-1$
    }

    @Override
    public String getLongName() {
        return description;
    }

    @Override
    public boolean isLoaded() {
        return true;
    }

    @Override
    public ImageDescriptor getIcon32() {
        return STITCH_ICON_DESCRIPTOR_32;
    }

    @Override
    public ImageDescriptor getIcon24() {
        return STITCH_ICON_DESCRIPTOR_24;
    }

    @Override
    public ImageDescriptor getIcon16() {
        return STITCH_ICON_DESCRIPTOR_16;
    }

    @Override
    public String getOriginalFamilyName() {
        return familyName;
    }

    @Override
    public String getTranslatedFamilyName() {
        return familyName;
    }

    @Override
    public String getPaletteType() {
        return StitchDataLoaderConstants.CONNECTOR_PALETTE_TYPE;
    }

    @Override
    public boolean isVisible(String family) {
        return true;
    }

    @Override
    public String toString() {
        return String.format("StitchPseudoComponent[%s]", getName());
    }

}
