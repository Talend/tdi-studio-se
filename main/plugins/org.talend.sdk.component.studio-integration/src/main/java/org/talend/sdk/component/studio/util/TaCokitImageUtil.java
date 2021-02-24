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
package org.talend.sdk.component.studio.util;

import java.io.ByteArrayInputStream;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.talend.commons.CommonsPlugin;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.ImageUtils;
import org.talend.commons.ui.runtime.image.ImageUtils.ICON_SIZE;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.ui.images.CoreImageProvider;
import org.talend.sdk.component.server.front.model.ConfigTypeNode;
import org.talend.sdk.component.studio.GAV;
import org.talend.sdk.component.studio.Lookups;

/**
 * DOC jding  class global comment. Detailled comment
 */
public class TaCokitImageUtil {

    public static Image getTaCoKitImageByRepositoryType(ERepositoryObjectType repObjType) {
        if (!TaCoKitUtil.isTaCoKitType(repObjType)) {
            return null;
        }
        if (TaCoKitConst.METADATA_TACOKIT.equals(repObjType)) {
            return CoreImageProvider.getImage(repObjType);
        }
        Image imageToDispose = null;
        try {
            ImageRegistry imageRegistry = JFaceResources.getImageRegistry();
            ConfigTypeNode configTypeNode = Lookups.taCoKitCache().getRepositoryObjectType2ConfigTypeNodeMap().get(repObjType);
            if (configTypeNode == null) {
                return null;
            }
            String id = configTypeNode.getId();
            String imageKey = GAV.INSTANCE.getArtifactId() + "/TaCoKit/Family/Metadata/" + id; //$NON-NLS-1$
            Image image = imageRegistry.get(imageKey);
            if (image == null) {
                try {
                    byte[] icon = null;
                    if (configTypeNode.getParentId() == null) {
                        icon = requestFamilyIcon(id);
                    } else {
                        icon = requestIcon(id);
                    }
                    image = buildTaCoKitImage(icon);
                    imageToDispose = image;
                    image = ImageUtils.scale(image, ICON_SIZE.ICON_16);
                } catch (Exception e) {
                    if (CommonsPlugin.isDebugMode()) {
                        ExceptionHandler.process(e);
                    }
                    byte[] bytes = requestFamilyIcon(Lookups.taCoKitCache().getFamilyNode(configTypeNode).getId());
                    image = buildTaCoKitImage(bytes);
                    imageToDispose = image;
                    image = ImageUtils.scale(image, ICON_SIZE.ICON_16);
                }
                if (image != null) {
                    imageRegistry.put(imageKey, image);
                }
            }
            if (image != null) {
                return image;
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        } finally {
            if (imageToDispose != null) {
                imageToDispose.dispose();
            }
        }
        return null;
    }

    public static byte[] requestFamilyIcon(String id) throws Exception {
        return Lookups.client().v1().component().familyIcon(id);
    }

    public static byte[] requestIcon(String id) throws Exception {
        return Lookups.client().v1().component().icon(id);
    }

    private static Image buildTaCoKitImage(byte[] icon) {
        if (icon != null && 0 < icon.length) {
            ByteArrayInputStream inputStream = null;
            try {
                inputStream = new ByteArrayInputStream(icon);
                return new Image(Display.getDefault(), inputStream);
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }
            }
        }
        return null;
    }

}
