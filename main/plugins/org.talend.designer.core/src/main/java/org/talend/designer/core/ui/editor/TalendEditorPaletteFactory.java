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
package org.talend.designer.core.ui.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.CreationToolEntry;
import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteGroup;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.PaletteSeparator;
import org.eclipse.gef.ui.palette.FlyoutPaletteComposite.FlyoutPreferences;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IComponentsFactory;
import org.talend.core.model.components.TalendPaletteGroup;
import org.talend.core.ui.componentsettings.ComponentsSettingsHelper;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.IPaletteFilter;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.process.AbstractProcessProvider;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.notes.NoteCreationFactory;
import org.talend.designer.core.ui.editor.palette.TalendPaletteDrawer;
import org.talend.designer.core.ui.preferences.TalendDesignerPrefConstants;
import org.talend.repository.model.ComponentsFactoryProvider;

/**
 * This class creates the palette in the Gef Editor. <br/>
 * 
 * $Id$
 * 
 */
public final class TalendEditorPaletteFactory {

    private static final String FAMILY_HIER_SEPARATOR = "/"; //$NON-NLS-1$

    /** Preference ID used to persist the palette location. */
    public static final String PALETTE_DOCK_LOCATION = "TalendEditorPaletteFactory.Location"; //$NON-NLS-1$

    /** Preference ID used to persist the palette size. */
    public static final String PALETTE_SIZE = "TalendEditorPaletteFactory.Size"; //$NON-NLS-1$

    /** Preference ID used to persist the flyout palette's state. */
    public static final String PALETTE_STATE = "TalendEditorPaletteFactory.State"; //$NON-NLS-1$

    private static PaletteRoot palette;

    private static String filter;

    private static boolean paletteState = true;

    private static PaletteGroup paGroup = new PaletteGroup(""); //$NON-NLS-1$

    /** Create the "Shapes" drawer. */
    private static void createComponentsDrawer(final IComponentsFactory compFac, boolean needHiddenComponent, int a) {
        // clearGroup();
        PaletteDrawer componentsDrawer;
        String name, longName;
        String family;
        String oraFamily;
        LinkedList<CreationToolEntry> nodeList = new LinkedList<CreationToolEntry>();
        List<String> families = new ArrayList<String>();
        HashMap<String, String> familyMap = new HashMap<String, String>();
        CombinedTemplateCreationEntry component;
        Hashtable<String, PaletteDrawer> ht = new Hashtable<String, PaletteDrawer>();
        if (a == 0) {
            componentsDrawer = new PaletteDrawer(Messages.getString("TalendEditorPaletteFactory.Default")); //$NON-NLS-1$
        }
        List<IComponent> componentList = new ArrayList<IComponent>(compFac.getComponents());

        String paletteType = ComponentCategory.CATEGORY_4_DI.getName();
        // Added by Marvin Wang on Jan. 10, 2012
        if (compFac.getComponentsHandler() != null) {
            componentList = compFac.getComponentsHandler().filterComponents(componentList);
            compFac.getComponentsHandler().sortComponents(componentList);
            paletteType = compFac.getComponentsHandler().extractComponentsCategory().getName();
        }

        Collections.sort(componentList, new Comparator<IComponent>() {

            @Override
            public int compare(IComponent component1, IComponent component2) {
                return component1.getName().compareTo(component2.getName());
            }

        });

        Set<String> displayedFamilies = ComponentsSettingsHelper.getDisplayedFamilies(paletteType);

        for (int i = 0; i < componentList.size(); i++) {
            IComponent xmlComponent = componentList.get(i);

            if (xmlComponent.isTechnical()) {
                continue;
            }

            // if (xmlComponent.isTechnical() || !xmlComponent.isVisible()) {
            // continue;
            // }

            if (xmlComponent.isLoaded()) {
                family = xmlComponent.getTranslatedFamilyName();
                oraFamily = xmlComponent.getOriginalFamilyName();
                String[] strings = family.split(ComponentsFactoryProvider.FAMILY_SEPARATOR_REGEX);
                String[] oraStrings = oraFamily.split(ComponentsFactoryProvider.FAMILY_SEPARATOR_REGEX);
                for (int j = 0; j < strings.length; j++) {
                    if (displayedFamilies.contains(oraStrings[j])) {
                        families.add(strings[j]);
                        familyMap.put(strings[j], oraStrings[j]);
                    }
                }
            }
        }

        Collections.sort(families);
        if (a == 0) {
            for (Object element : families) {
                family = (String) element;
                String oraFam = familyMap.get(family);
                componentsDrawer = ht.get(family);
                if (componentsDrawer == null) {
                    componentsDrawer = createComponentDrawer(ht, family);
                    if (componentsDrawer instanceof IPaletteFilter) {
                        ((IPaletteFilter) componentsDrawer).setOriginalName(oraFam);
                    }
                }

            }
        }

        boolean noteAeeded = false;
        boolean needAddNote = true;
        boolean needToAdd = false;
        for (int i = 0; i < componentList.size(); i++) {
            IComponent xmlComponent = componentList.get(i);

            if (xmlComponent.isTechnical()) {
                continue;
            }
            family = xmlComponent.getTranslatedFamilyName();
            oraFamily = xmlComponent.getOriginalFamilyName();
            if (filter != null) {
                String regex = getFilterRegex();
                needAddNote = "Note".toLowerCase().matches(regex); //$NON-NLS-1$
            }
            if ((oraFamily.equals("Misc") || oraFamily.equals("Miscellaneous")) && !noteAeeded && needAddNote) { //$NON-NLS-1$
                CreationToolEntry noteCreationToolEntry = new CreationToolEntry(
                        Messages.getString("TalendEditorPaletteFactory.Note"), //$NON-NLS-1$
                        Messages.getString("TalendEditorPaletteFactory.CreateNote"), //$NON-NLS-1$
                        new NoteCreationFactory(), ImageProvider.getImageDesc(ECoreImage.CODE_ICON),
                        ImageProvider.getImageDesc(ECoreImage.CODE_ICON));
                if (a == 0) {
                    PaletteDrawer drawer = ht.get(family);
                    if (drawer != null) {
                        noteCreationToolEntry.setParent(drawer);
                        drawer.add(noteCreationToolEntry);
                    }
                } else if (a == 1) {
                    for (String s : families) {
                        if (s.equals(family)) {
                            needToAdd = true;
                        }
                    }
                    if (needToAdd == true) {
                        nodeList.add(0, noteCreationToolEntry);
                        // noteCreationToolEntry.setParent(paGroup);
                        // paGroup.add(noteCreationToolEntry);
                    }
                }

                noteAeeded = true;
            }

            if (filter != null) {

                String regex = getFilterRegex();
                if (!xmlComponent.getName().toLowerCase().matches(regex)
                        && !xmlComponent.getLongName().toLowerCase().matches(regex)) {
                    continue;
                }
            }

            if (xmlComponent.isLoaded()) {
                name = xmlComponent.getName();
                longName = xmlComponent.getLongName();

                ImageDescriptor imageSmall = xmlComponent.getIcon16();
                IPreferenceStore store = DesignerPlugin.getDefault().getPreferenceStore();
                ImageDescriptor imageLarge;
                final String string = store.getString(TalendDesignerPrefConstants.LARGE_ICONS_SIZE);
                if (string.equals("24")) { //$NON-NLS-1$
                    imageLarge = xmlComponent.getIcon24();
                } else {
                    imageLarge = xmlComponent.getIcon32();
                }

                String[] strings = family.split(ComponentsFactoryProvider.FAMILY_SEPARATOR_REGEX);
                String[] oraStrings = oraFamily.split(ComponentsFactoryProvider.FAMILY_SEPARATOR_REGEX);
                for (int j = 0; j < strings.length; j++) {
                    if (!needHiddenComponent && !xmlComponent.isVisible(oraStrings[j])) {
                        continue;
                    }

                    component = new CombinedTemplateCreationEntry(name, name, Node.class, new PaletteComponentFactory(
                            xmlComponent), imageSmall, imageLarge);

                    component.setDescription(longName);

                    if (a == 0) {
                        componentsDrawer = ht.get(strings[j]);
                        if (componentsDrawer == null) {
                            continue;
                        }
                        component.setParent(componentsDrawer);
                        componentsDrawer.add(component);
                    } else if (a == 1) {
                        boolean canAdd = true;
                        // listName = paGroup.getChildren();
                        // for (int z = 0; z < listName.size(); z++) {
                        // if ((((PaletteEntry) listName.get(z)).getLabel()).equals(component.getLabel())) {
                        // canAdd = false;
                        // }
                        // }
                        for (int z = 0; z < nodeList.size(); z++) {
                            if ((nodeList.get(z).getLabel()).equals(component.getLabel())) {
                                canAdd = false;
                            }
                        }
                        if (canAdd == true) {
                            nodeList.add(component);
                            // component.setParent(paGroup);
                            // paGroup.add(component);
                        }
                    }

                }
            }
        }
        if (a == 1) {
            for (CreationToolEntry entryCom : nodeList) {
                entryCom.setParent(paGroup);
                paGroup.add(entryCom);
            }
            palette.add(paGroup);
        }
    }

    /** Create the "Shapes" drawer. */
    private static void createComponentsDrawer(final IComponentsFactory compFac, boolean needHiddenComponent, boolean isFavorite,
            int a) {

        clearGroup();
        LinkedList<CreationToolEntry> nodeList = new LinkedList<CreationToolEntry>();
        // } else if (a == 0) {
        PaletteDrawer componentsDrawer;
        String name, longName;
        String family;
        String oraFamily;
        List<String> families = new ArrayList<String>();
        HashMap<String, String> familyMap = new HashMap<String, String>();
        boolean favoriteFlag;
        List listName = new ArrayList();
        CombinedTemplateCreationEntry component;
        Hashtable<String, PaletteDrawer> ht = new Hashtable<String, PaletteDrawer>();
        paletteState = isFavorite;
        if (a == 0) {
            componentsDrawer = new PaletteDrawer(Messages.getString("TalendEditorPaletteFactory.Default")); //$NON-NLS-1$
        }
        List<IComponent> componentList = new ArrayList<IComponent>(compFac.getComponents());

        // Added by Marvin Wang on Jan. 10, 2012
        if (compFac.getComponentsHandler() != null) {
            componentList = compFac.getComponentsHandler().filterComponents(componentList);
            componentList = compFac.getComponentsHandler().sortComponents(componentList);
        }

        Collections.sort(componentList, new Comparator<IComponent>() {

            @Override
            public int compare(IComponent component1, IComponent component2) {
                return component1.getName().compareTo(component2.getName());
            }

        });

        for (int i = 0; i < componentList.size(); i++) {
            IComponent xmlComponent = componentList.get(i);

            if (xmlComponent.isTechnical()) {
                continue;
            }

            if (xmlComponent.isLoaded()) {
                family = xmlComponent.getTranslatedFamilyName();
                oraFamily = xmlComponent.getOriginalFamilyName();

                String[] strings = family.split(ComponentsFactoryProvider.FAMILY_SEPARATOR_REGEX);
                String[] oraStrings = oraFamily.split(ComponentsFactoryProvider.FAMILY_SEPARATOR_REGEX);
                for (int j = 0; j < strings.length; j++) {
                    if (!needHiddenComponent && !xmlComponent.isVisible(oraStrings[j])) {
                        continue;
                    }
                    String key = null;
                    key = xmlComponent.getName() + "#" + oraStrings[j];//$NON-NLS-1$

                    if (a == 0) {
                        if (!oraStrings[j].equals("Misc")) {//$NON-NLS-1$
                            if (isFavorite && !DesignerPlugin.getDefault().getPreferenceStore().getBoolean(key)) {

                                continue;
                            }
                        }
                    }
                    families.add(strings[j]);
                    familyMap.put(strings[j], oraStrings[j]);

                }
            }
        }

        Collections.sort(families);
        if (a == 0) {
            for (Object element : families) {
                family = (String) element;
                String oraFam = familyMap.get(family);
                componentsDrawer = ht.get(family);
                if (componentsDrawer == null) {
                    componentsDrawer = createComponentDrawer(ht, family);
                    if (componentsDrawer instanceof IPaletteFilter) {
                        ((IPaletteFilter) componentsDrawer).setOriginalName(oraFam);
                    }
                }
            }
        }
        boolean noteAeeded = false;
        boolean needAddNote = true;
        boolean needToAdd = false;

        // For bug TDI-25745, to add "note" entry to Misc drawer for m/r job and common job editor. It should create
        // Misc drawer first if there is not the drawer in palette.
        PaletteDrawer drawer = ht.get("Miscellaneous"); //$NON-NLS-1$
        if (drawer == null) {
            drawer = ht.get("Misc"); //$NON-NLS-1$
            if (drawer == null) {
                drawer = createComponentDrawer(ht, "Misc"); //$NON-NLS-1$
                if (drawer instanceof IPaletteFilter) {
                    ((IPaletteFilter) drawer).setOriginalName("Misc"); //$NON-NLS-1$
                }
            }
        }

        CreationToolEntry noteCreationToolEntry = new CreationToolEntry(Messages.getString("TalendEditorPaletteFactory.Note"), //$NON-NLS-1$
                Messages.getString("TalendEditorPaletteFactory.CreateNote"), //$NON-NLS-1$
                new NoteCreationFactory(), ImageProvider.getImageDesc(ECoreImage.CODE_ICON),
                ImageProvider.getImageDesc(ECoreImage.CODE_ICON));
        noteCreationToolEntry.setParent(drawer);
        drawer.add(noteCreationToolEntry);

        for (int i = 0; i < componentList.size(); i++) {
            IComponent xmlComponent = componentList.get(i);

            if (xmlComponent.isTechnical()) {
                continue;
            }
            family = xmlComponent.getTranslatedFamilyName();
            oraFamily = xmlComponent.getOriginalFamilyName();
            if (filter != null) {
                Pattern pattern = Pattern.compile("^[A-Za-z0-9]+$");//$NON-NLS-1$
                Matcher matcher = pattern.matcher(filter);
                if (!matcher.matches() && filter.length() != 0) {
                    filter = "None";
                }
                String regex = getFilterRegex();
                needAddNote = "Note".toLowerCase().matches(regex); //$NON-NLS-1$
            }

            if (filter != null) {
                Pattern pattern = Pattern.compile("^[A-Za-z0-9]+$");//$NON-NLS-1$
                Matcher matcher = pattern.matcher(filter);
                if (matcher.matches()) {
                    String regex = getFilterRegex();
                    if (!xmlComponent.getName().toLowerCase().matches(regex)
                            && !xmlComponent.getLongName().toLowerCase().matches(regex)) {
                        continue;
                    }
                }
            }

            family = xmlComponent.getTranslatedFamilyName();
            oraFamily = xmlComponent.getOriginalFamilyName();

            String[] keys = family.split(ComponentsFactoryProvider.FAMILY_SEPARATOR_REGEX);
            String[] oraKeys = oraFamily.split(ComponentsFactoryProvider.FAMILY_SEPARATOR_REGEX);
            for (int j = 0; j < keys.length; j++) {
                String key = null;
                key = xmlComponent.getName() + "#" + oraKeys[j];//$NON-NLS-1$
                if (isFavorite && !DesignerPlugin.getDefault().getPreferenceStore().getBoolean(key)) {
                    continue;
                }

            }

            if (xmlComponent.isLoaded()) {
                name = xmlComponent.getName();
                longName = xmlComponent.getLongName();

                ImageDescriptor imageSmall = xmlComponent.getIcon16();
                IPreferenceStore store = DesignerPlugin.getDefault().getPreferenceStore();
                ImageDescriptor imageLarge;
                final String string = store.getString(TalendDesignerPrefConstants.LARGE_ICONS_SIZE);
                if (string.equals("24")) { //$NON-NLS-1$
                    imageLarge = xmlComponent.getIcon24();
                } else {
                    imageLarge = xmlComponent.getIcon32();
                }

                String[] strings = family.split(ComponentsFactoryProvider.FAMILY_SEPARATOR_REGEX);
                String[] oraStrings = oraFamily.split(ComponentsFactoryProvider.FAMILY_SEPARATOR_REGEX);
                for (int j = 0; j < strings.length; j++) {
                    if (!needHiddenComponent && !xmlComponent.isVisible(oraStrings[j])) {
                        continue;
                    }
                    String key = null;
                    key = xmlComponent.getName() + "#" + oraStrings[j];//$NON-NLS-1$

                    if (isFavorite && !DesignerPlugin.getDefault().getPreferenceStore().getBoolean(key)) {
                        continue;
                    }

                    component = new CombinedTemplateCreationEntry(name, name, Node.class, new PaletteComponentFactory(
                            xmlComponent), imageSmall, imageLarge);

                    component.setDescription(longName);
                    if (a == 0) {
                        componentsDrawer = ht.get(strings[j]);
                        component.setParent(componentsDrawer);
                        componentsDrawer.add(component);
                    } else if (a == 1) {
                        boolean canAdd = true;
                        // listName = paGroup.getChildren();
                        // for (int z = 0; z < listName.size(); z++) {
                        // if ((((PaletteEntry) listName.get(z)).getLabel()).equals(component.getLabel())) {
                        // canAdd = false;
                        // }
                        // }
                        for (int z = 0; z < nodeList.size(); z++) {

                            if ((nodeList.get(z).getLabel()).equals(component.getLabel())) {
                                canAdd = false;
                            }
                        }
                        if (canAdd == true) {
                            nodeList.add(component);
                            // component.setParent(paGroup);
                            // paGroup.add(component);
                        }
                    }

                }
            }
        }
        if (a == 1) {
            for (CreationToolEntry entryComponent : nodeList) {
                entryComponent.setParent(paGroup);
                paGroup.add(entryComponent);
            }
            palette.add(paGroup);
        }
        setFilter(""); //$NON-NLS-1$
    }

    /**
     * yzhang Comment method "getFilterRegex".
     * 
     * @return
     */
    private static String getFilterRegex() {
        String regex = "\\b.*" + filter.replaceAll("\\*", ".*") + ".*\\b"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        regex = regex.replaceAll("\\?", ".?"); //$NON-NLS-1$ //$NON-NLS-2$
        return regex;
    }

    private static PaletteDrawer createComponentDrawer(Hashtable<String, PaletteDrawer> ht, String familyToCreate) {

        int index = familyToCreate.lastIndexOf(FAMILY_HIER_SEPARATOR);
        String family;
        PaletteDrawer parentPaletteDrawer = null;

        if (index > -1) {
            family = familyToCreate.substring(index + 1);
            String parentFamily = familyToCreate.substring(0, index);
            parentPaletteDrawer = ht.get(parentFamily);
            if (parentPaletteDrawer == null) {
                parentPaletteDrawer = createComponentDrawer(ht, parentFamily);
            }
        } else {
            family = familyToCreate;
        }
        PaletteDrawer paletteDrawer = new TalendPaletteDrawer(family);
        paletteDrawer.setInitialState(loadFamilyState(familyToCreate));
        if (parentPaletteDrawer == null) {
            palette.add(paletteDrawer);
        } else {
            parentPaletteDrawer.add(paletteDrawer);
        }

        ht.put(familyToCreate, paletteDrawer);

        return paletteDrawer;
    }

    /**
     * DOC nrousseau Comment method "loadFamilyState".
     * 
     * @param family
     * @return
     */
    private static int loadFamilyState(String family) {
        IPreferenceStore preferenceStore = DesignerPlugin.getDefault().getPreferenceStore();
        preferenceStore.setDefault(PALETTE_STATE + family, PaletteDrawer.INITIAL_STATE_CLOSED);
        return preferenceStore.getInt(PALETTE_STATE + family);
    }

    public static void saveFamilyState(PaletteViewer viewer) {
        IPreferenceStore preferenceStore = DesignerPlugin.getDefault().getPreferenceStore();
        for (Object o : palette.getChildren()) {
            if (o instanceof PaletteDrawer) {
                PaletteDrawer paletteItem = (PaletteDrawer) o;
                saveFamilyState(viewer, preferenceStore, paletteItem);
            }
        }
    }

    private static void saveFamilyState(PaletteViewer viewer, IPreferenceStore preferenceStore, PaletteDrawer paletteItem) {
        String family = paletteItem.getLabel();
        int value;
        if (viewer.isExpanded(paletteItem)) {
            value = PaletteDrawer.INITIAL_STATE_OPEN;
        } else {
            value = PaletteDrawer.INITIAL_STATE_CLOSED;
        }
        preferenceStore.setValue(PALETTE_STATE + family, value);

        for (Iterator iter = paletteItem.getChildren().iterator(); iter.hasNext();) {
            Object object = iter.next();
            if (object instanceof PaletteDrawer) {
                PaletteDrawer paletteDrawer = (PaletteDrawer) object;
                saveFamilyState(viewer, preferenceStore, paletteDrawer);
            }
        }
    }

    /**
     * Creates the PaletteRoot and adds all palette elements. Use this factory method to create a new palette for your
     * graphical editor.
     * 
     * @return a new PaletteRoot
     */
    public static PaletteRoot createPalette(final IComponentsFactory compFac) {
        PaletteRoot pr = new PaletteRoot();
        pr.add(createToolsGroup());
        return createPalette(compFac, pr);
    }

    public static PaletteRoot createPalette(final IComponentsFactory compFac, PaletteRoot root) {// ing
        int histate = DesignerPlugin.getDefault().getPreferenceStore().getInt("HiddenState"); //$NON-NLS-1$
        palette = root;
        AbstractProcessProvider.loadComponentsFromProviders();
        createComponentsDrawer(compFac, false, histate);
        return palette;
    }

    public static PaletteRoot createPalette(final IComponentsFactory compFac, boolean isFavorite) {
        PaletteRoot pr = new PaletteRoot();
        pr.add(createToolsGroup());
        return createPalette(compFac, pr, isFavorite);
    }

    public static PaletteRoot createPalette(final IComponentsFactory compFac, PaletteRoot root, boolean isFavorite) {// after
        int histate = DesignerPlugin.getDefault().getPreferenceStore().getInt("HiddenState"); //$NON-NLS-1$
        palette = root;
        AbstractProcessProvider.loadComponentsFromProviders();
        createComponentsDrawer(compFac, false, isFavorite, histate);
        return palette;
    }

    public static PaletteRoot getAllNodeStructure(final IComponentsFactory compFac) {
        palette = new PaletteRoot();
        AbstractProcessProvider.loadComponentsFromProviders();
        createComponentsDrawer(compFac, true, 0);
        return palette;
    }

    public static PaletteRoot createPalletteForMapReduce(IComponentsFactory compFac, PaletteRoot root) {
        palette = root;
        int histate = DesignerPlugin.getDefault().getPreferenceStore().getInt("HiddenState"); //$NON-NLS-1$
        AbstractProcessProvider.loadComponentsFromProviders();
        createComponentsDrawer(compFac, false, histate);
        return palette;
    }

    /**
     * Return a FlyoutPreferences instance used to save/load the preferences of a flyout palette.
     */
    public static FlyoutPreferences createPalettePreferences() {
        return new FlyoutPreferences() {

            private IPreferenceStore getPreferenceStore() {
                return DesignerPlugin.getDefault().getPreferenceStore();
            }

            @Override
            public int getDockLocation() {
                return getPreferenceStore().getInt(PALETTE_DOCK_LOCATION);
            }

            @Override
            public int getPaletteState() {
                return getPreferenceStore().getInt(PALETTE_STATE);
            }

            @Override
            public int getPaletteWidth() {
                return getPreferenceStore().getInt(PALETTE_SIZE);
            }

            @Override
            public void setDockLocation(final int location) {
                getPreferenceStore().setValue(PALETTE_DOCK_LOCATION, location);
            }

            @Override
            public void setPaletteState(final int state) {
                getPreferenceStore().setValue(PALETTE_STATE, state);
            }

            @Override
            public void setPaletteWidth(final int width) {
                getPreferenceStore().setValue(PALETTE_SIZE, width);
            }
        };
    }

    /** Create the "Tools" group. */
    private static PaletteContainer createToolsGroup() {
        TalendPaletteGroup toolGroup = new TalendPaletteGroup(Messages.getString("TalendEditorPaletteFactory.Tools")); //$NON-NLS-1$
        // Add a selection tool to the group
        // ToolEntry tool = new PanningSelectionToolEntry();
        // toolGroup.add(tool);
        // palette.setDefaultEntry(tool);

        // Add a marquee tool to the group
        // toolGroup.add(new MarqueeToolEntry());

        //        CreationToolEntry noteCreationToolEntry = new CreationToolEntry(Messages.getString("TalendEditorPaletteFactory.Note"), //$NON-NLS-1$
        //                Messages.getString("TalendEditorPaletteFactory.CreateNote"), //$NON-NLS-1$
        // new NoteCreationFactory(), ImageProvider.getImageDesc(ECoreImage.CODE_ICON), ImageProvider
        // .getImageDesc(ECoreImage.CODE_ICON));
        // toolGroup.add(noteCreationToolEntry);

        // Add a (unnamed) separator to the group
        toolGroup.add(new PaletteSeparator());

        return toolGroup;
    }

    /** Utility class. */
    private TalendEditorPaletteFactory() {
        // Utility class
    }

    /**
     * yzhang Comment method "setFilter".
     * 
     * @param filter
     */
    public static void setFilter(String filter) {
        TalendEditorPaletteFactory.filter = filter.toLowerCase();
    }

    public static void clearGroup() {
        paGroup.getChildren().clear();
        List list = palette.getChildren();
        if (list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i) instanceof PaletteGroup) {
                    PaletteGroup entry = (PaletteGroup) list.get(i);
                    if (entry instanceof TalendPaletteGroup) {
                        continue;
                    }
                    palette.remove(entry);
                }

            }
        }

    }

    /**
     * DOC guanglong.du Comment method "createEmptyPalette".
     * 
     * @return
     */
    public static PaletteRoot createEmptyPalette() {
        palette = new PaletteRoot();
        palette.add(createToolsGroup());
        return palette;
    }
}
