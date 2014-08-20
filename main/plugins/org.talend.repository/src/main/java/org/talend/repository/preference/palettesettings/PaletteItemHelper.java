// ============================================================================
//
// Copyright (C) 2006-2014 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.preference.palettesettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.EComponentType;
import org.talend.core.model.components.IComponent;
import org.talend.designer.components.preference.provider.IPaletteItem;
import org.talend.repository.model.ComponentsFactoryProvider;

/**
 * created by nrousseau on Aug 11, 2014 Detailled comment
 *
 */
public class PaletteItemHelper {

    private static String FAMILY_FOLDER_SEPARATOR = "/"; //$NON-NLS-1$

    public static List<IPaletteItem> buildFullPaletteItemList() {
        List<IPaletteItem> paletteItems = new ArrayList<IPaletteItem>();
        Set<IComponent> components = ComponentsFactoryProvider.getInstance().getComponents();
        // for family folders
        for (IComponent component : components) {
            if (component.isTechnical() || component.getComponentType() == EComponentType.JOBLET) {
                continue;
            }
            createPaletteItems(component, paletteItems);
        }

        return paletteItems;
    }

    /**
     * DOC nrousseau Comment method "createPaletteItems".
     * 
     * @param component
     * @param paletteItems
     */
    private static void createPaletteItems(IComponent component, List<IPaletteItem> paletteItems) {
        String[] originalFamilies = component.getOriginalFamilyName().split(ComponentsFactoryProvider.FAMILY_SEPARATOR_REGEX);
        String[] translatedFamilies = component.getTranslatedFamilyName().split(ComponentsFactoryProvider.FAMILY_SEPARATOR_REGEX);
        if (originalFamilies.length != translatedFamilies.length) {
            // this case should not happen, but in this case we just don't care about the translated one.
            translatedFamilies = originalFamilies;
        }
        IPaletteItem rootPaletteItem = getRootPaletteItem(
                ComponentCategory.getComponentCategoryFromName(component.getPaletteType()), paletteItems);
        for (int i = 0; i < originalFamilies.length; i++) {
            String origFamily = originalFamilies[i];
            String translFamily = translatedFamilies[i];
            IPaletteItem parentPaletteItem = getFamilyItem(translFamily, rootPaletteItem);
            IPaletteItem componentItem = new ComponentPaletteItem(component, origFamily);
            parentPaletteItem.getChildren().add(componentItem);
            componentItem.setParent(parentPaletteItem);
        }
    }

    /**
     * DOC nrousseau Comment method "getRootPaletteItem".
     * 
     * @param valueOf
     * @param paletteItems
     * @return
     */
    private static IPaletteItem getRootPaletteItem(ComponentCategory category, List<IPaletteItem> paletteItems) {
        for (IPaletteItem paletteItem : paletteItems) {
            if (paletteItem.getPaletteType() == category) {
                return paletteItem;
            }
        }
        IPaletteItem rootPaletteItem = new RootPaletteItem(category);
        paletteItems.add(rootPaletteItem);
        return rootPaletteItem;
    }

    /**
     * DOC nrousseau Comment method "getFamilyItem".
     * 
     * @param family
     * @param rootPaletteItem
     * @return
     */
    private static IPaletteItem getFamilyItem(String family, IPaletteItem rootPaletteItem) {
        String curFolder = family.split(FAMILY_FOLDER_SEPARATOR)[0];
        IPaletteItem curFolderItem = null;
        for (IPaletteItem paletteItem : rootPaletteItem.getChildren()) {
            if (paletteItem instanceof FolderPaletteItem && paletteItem.getLabel().equals(curFolder)) {
                curFolderItem = paletteItem;
                break;
            }
        }
        if (curFolderItem == null) {
            curFolderItem = new FolderPaletteItem(curFolder, rootPaletteItem.getPaletteType());
            rootPaletteItem.getChildren().add(curFolderItem);
            curFolderItem.setParent(rootPaletteItem);
        }
        int index = family.indexOf(FAMILY_FOLDER_SEPARATOR);
        if (index < 0) {
            return curFolderItem;
        }
        return getFamilyItem(family.substring(index + 1, family.length()), curFolderItem);
    }
}
