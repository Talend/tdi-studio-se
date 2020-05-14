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
package org.talend.designer.core.ui.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Priority;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.CreationToolEntry;
import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteGroup;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.PaletteSeparator;
import org.eclipse.gef.tools.CreationTool;
import org.eclipse.gef.ui.palette.FlyoutPaletteComposite.FlyoutPreferences;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.internal.search.ISearchHitCollector;
import org.eclipse.help.internal.search.ISearchQuery;
import org.eclipse.help.internal.search.LocalSearchManager;
import org.eclipse.help.internal.search.QueryTooComplexException;
import org.eclipse.help.internal.search.SearchHit;
import org.eclipse.help.internal.search.SearchQuery;
import org.eclipse.help.internal.search.federated.FederatedSearchJob;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.talend.commons.exception.CommonExceptionHandler;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.EComponentType;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IComponentsFactory;
import org.talend.core.model.components.IComponentsHandler;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.core.ui.component.TalendPaletteGroup;
import org.talend.core.ui.component.settings.ComponentsSettingsHelper;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.IPaletteFilter;
import org.talend.designer.core.IUnifiedComponentService;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.ComponentHit;
import org.talend.designer.core.model.components.StitchPseudoComponent;
import org.talend.designer.core.model.process.AbstractProcessProvider;
import org.talend.designer.core.model.process.GenericProcessProvider;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.notes.NoteCreationFactory;
import org.talend.designer.core.ui.editor.palette.TalendCombinedTemplateCreationEntry;
import org.talend.designer.core.ui.editor.palette.TalendPaletteDrawer;
import org.talend.designer.core.ui.preferences.PaletteSettingsPreferencePage;
import org.talend.designer.core.ui.preferences.TalendDesignerPrefConstants;
import org.talend.designer.core.utils.UnifiedComponentUtil;
import org.talend.designer.unifiedcomponent.delegate.service.IComponentDelegate;

/**
 * This class creates the palette in the Gef Editor. <br/>
 *
 * $Id$
 *
 */
public final class TalendEditorPaletteFactory {

    private static final String FAMILY_HIER_SEPARATOR = "/"; //$NON-NLS-1$

    private static final String FAMILY_DATABASES = "Databases"; //$NON-NLS-1$

    private static final String FAMILY_DATABASES_SPECIFIED = "Databases/DB Specifics"; //$NON-NLS-1$

    /** Preference ID used to persist the palette location. */
    public static final String PALETTE_DOCK_LOCATION = "TalendEditorPaletteFactory.Location"; //$NON-NLS-1$

    /** Preference ID used to persist the palette size. */
    public static final String PALETTE_SIZE = "TalendEditorPaletteFactory.Size"; //$NON-NLS-1$

    /** Preference ID used to persist the flyout palette's state. */
    public static final String PALETTE_STATE = "TalendEditorPaletteFactory.State"; //$NON-NLS-1$

    public static final String FAVORITES = Messages.getString("TalendEditorPaletteFactory.palette.favorites"); //$NON-NLS-1$

    public static final String RECENTLY_USED = Messages.getString("TalendEditorPaletteFactory.palette.recentlyUsed"); //$NON-NLS-1$

    protected static final String FAVORITES_KEY = "Palette.Favorite.ComponentsList"; //$NON-NLS-1$

    protected static final String FAVORITES_KEY_LIST_SEPERATOR = ","; //$NON-NLS-1$

    protected static final String RECENTLY_USED_KEY = "Palette.RecentlyUsed.ComponentsList"; //$NON-NLS-1$

    protected static final String RECENTLY_USED_LIST_SEPERATOR = ","; //$NON-NLS-1$

    protected static final String RECENTLY_USED_COMPONENT_NAME_TIMESTEMP_SEPERATOR = ":"; //$NON-NLS-1$

    private static PaletteRoot palette;

    private static String filter;

    private static boolean paletteState = true;

    private static PaletteGroup paGroup = new PaletteGroup(""); //$NON-NLS-1$

    protected static final String SEARCHING_FROM_HELP = Messages.getString("TalendEditorPaletteFactory.searchingFromHelp"); //$NON-NLS-1$

    protected static void createComponentsDrawer(final IComponentsFactory compFac, final boolean needHiddenComponent, final int a) {
        List<IComponent> componentList = null;
        // needn't to check visible, since this visble check will be done in method "createComponentsDrawer(...)"
        componentList = getRelatedComponents(compFac, TalendEditorPaletteFactory.filter, false);

        String paletteType = ComponentCategory.CATEGORY_4_DI.getName();
        // Added by Marvin Wang on Jan. 10, 2012
        if (compFac.getComponentsHandler() != null) {
            // the filter already done in method "getRelatedComponents()"
            // componentList = compFac.getComponentsHandler().filterComponents(componentList);
            compFac.getComponentsHandler().sortComponents(componentList);
            paletteType = compFac.getComponentsHandler().extractComponentsCategory().getName();
        }

        Collections.sort(componentList, new Comparator<IComponent>() {

            @Override
            public int compare(IComponent component1, IComponent component2) {
                return component1.getName().toLowerCase().compareTo(component2.getName().toLowerCase());
            }

        });

        final List<IComponent> finalComponentList = componentList;
        final String finalPaletteType = paletteType;
        DisplayUtils.getDisplay().syncExec(new Runnable() {

            @Override
            public void run() {
                createComponentsDrawer(finalComponentList, needHiddenComponent, a, finalPaletteType);
            }
        });
    }

    /** Create the "Shapes" drawer. */
    private static void createComponentsDrawer(final List<IComponent> componentList, final boolean needHiddenComponent,
            final int a, final String paletteType) {
        // clearGroup();
        PaletteDrawer componentsDrawer;
        String name, longName;
        String family;
        String oraFamily;
        List<CreationToolEntry> nodeList = new LinkedList<CreationToolEntry>();
        List<String> families = new ArrayList<String>();
        HashMap<String, String> familyMap = new HashMap<String, String>();
        CombinedTemplateCreationEntry component;
        Hashtable<String, PaletteDrawer> ht = new Hashtable<String, PaletteDrawer>();
        List<String> favoriteComponentNames = null;
        if (a == 0) {
            componentsDrawer = new PaletteDrawer(Messages.getString("TalendEditorPaletteFactory.Default")); //$NON-NLS-1$
            favoriteComponentNames = getFavoritesList();
        }

        Set<String> displayedFamilies = ComponentsSettingsHelper.getDisplayedFamilies(paletteType);

        Iterator<IComponent> componentIter = componentList.iterator();
        while (componentIter.hasNext()) {
            IComponent xmlComponent = componentIter.next();

            if (xmlComponent.isTechnical()) {
                continue;
            }

            // if (xmlComponent.isTechnical() || !xmlComponent.isVisible()) {
            // continue;
            // }
            oraFamily = xmlComponent.getOriginalFamilyName();
            family = xmlComponent.getTranslatedFamilyName();
            if (xmlComponent.isLoaded()) {
                String[] strings = family.split(ComponentsFactoryProvider.FAMILY_SEPARATOR_REGEX);
                String[] oraStrings = oraFamily.split(ComponentsFactoryProvider.FAMILY_SEPARATOR_REGEX);
                for (int j = 0; j < strings.length; j++) {
                    if (displayedFamilies.contains(oraStrings[j]) || xmlComponent.getComponentType() == EComponentType.JOBLET) {
                        families.add(strings[j]);
                        familyMap.put(strings[j], oraStrings[j]);
                    }
                }
            }
        }

        Collections.sort(families);

        List<String> recentlyUsedComponentNames = null;
        List<RecentlyUsedComponent> recentlyUsedComponents = null;
        if (a == 0) {
            // if a==1, then means hide folder mode
            recentlyUsedComponents = new LinkedList<TalendEditorPaletteFactory.RecentlyUsedComponent>();
            recentlyUsedComponentNames = getRecentlyUsedList(recentlyUsedComponents);
            recentlyUsedComponents = sortRecentlyUsed(recentlyUsedComponents);

            families.add(0, FAVORITES);
            familyMap.put(FAVORITES, FAVORITES);

            families.add(1, RECENTLY_USED);
            familyMap.put(RECENTLY_USED, RECENTLY_USED);

            for (Object element : families) {
                family = (String) element;
                String oraFam = familyMap.get(family);
                componentsDrawer = ht.get(family);
                if (componentsDrawer == null) {
                    componentsDrawer = createComponentDrawer(ht, family);
                    // if (TalendEditorPaletteFactory.FAVORITES.equals(family)) {
                    // ((TalendPaletteViewer) componentsDrawer .getViewer()).setFavoritesEditPart(drawerEditPart);
                    // } else if (TalendEditorPaletteFactory.RECENTLY_USED.equals(family)) {
                    // ((TalendPaletteViewer) componentsDrawer.getViewer()).setRecentlyUsedEditPart(drawerEditPart);
                    // }
                    if (componentsDrawer instanceof IPaletteFilter) {
                        ((IPaletteFilter) componentsDrawer).setOriginalName(oraFam);
                    }
                }

            }
        }

        boolean noteAeeded = false;
        boolean needAddNote = true;
        boolean needToAdd = false;
        Map<String, IComponent> recentlyUsedMap = new HashMap<String, IComponent>();
        IPreferenceStore store = DesignerPlugin.getDefault().getPreferenceStore();
        String largeIconsSize = store.getString(TalendDesignerPrefConstants.LARGE_ICONS_SIZE);
        componentIter = componentList.iterator();
        while (componentIter.hasNext()) {
            IComponent xmlComponent = componentIter.next();
            if (xmlComponent.isTechnical()) {
                continue;
            }
            family = xmlComponent.getTranslatedFamilyName();
            oraFamily = xmlComponent.getOriginalFamilyName();
            if (filter != null) {
                String regex = getFilterRegex(filter);
                needAddNote = "Note".toLowerCase().matches(regex); //$NON-NLS-1$
            }
            if ((oraFamily.equals("Misc") || oraFamily.equals("Miscellaneous")) && !noteAeeded && needAddNote) { //$NON-NLS-1$
                CreationToolEntry noteCreationToolEntry = createNoteEntry(largeIconsSize);
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

            if (xmlComponent.isLoaded()) {
                name = UnifiedComponentUtil.getComponentDisplayNameForPalette(xmlComponent, filter);
                longName = xmlComponent.getLongName();

                ImageDescriptor imageSmall = xmlComponent.getIcon16();
                ImageDescriptor imageLarge;
                if (largeIconsSize.equals("24")) { //$NON-NLS-1$
                    imageLarge = xmlComponent.getIcon24();
                } else {
                    imageLarge = xmlComponent.getIcon32();
                }

                if (favoriteComponentNames != null && favoriteComponentNames.contains(xmlComponent.getName())) {
                    componentsDrawer = ht.get(FAVORITES);
                    if (componentsDrawer != null) {
                        component = new TalendCombinedTemplateCreationEntry(name, name, Node.class, xmlComponent, imageSmall,
                                imageLarge, filter);

                        component.setDescription(longName);
                        component.setParent(componentsDrawer);
                        componentsDrawer.add(component);
                    }
                }

                if (recentlyUsedComponentNames != null && recentlyUsedComponentNames.contains(xmlComponent.getName())) {
                    recentlyUsedMap.put(xmlComponent.getName(), xmlComponent);
                }

                String[] strings = family.split(ComponentsFactoryProvider.FAMILY_SEPARATOR_REGEX);
                String[] oraStrings = oraFamily.split(ComponentsFactoryProvider.FAMILY_SEPARATOR_REGEX);
                for (int j = 0; j < strings.length; j++) {
                    if (!needHiddenComponent && !xmlComponent.isVisible(oraStrings[j])) {
                        continue;
                    }

                    component = new TalendCombinedTemplateCreationEntry(name, name, Node.class, xmlComponent, imageSmall,
                            imageLarge, filter);

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
                        Iterator<CreationToolEntry> iter = nodeList.iterator();
                        while (iter.hasNext()) {
                            if ((iter.next().getLabel()).equals(component.getLabel())) {
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

        if (a == 0) {
            createRecentlyUsedEntryList(ht, recentlyUsedComponents, recentlyUsedMap);
        }

        if (a == 1) {
            Iterator<CreationToolEntry> iter = nodeList.iterator();
            while (iter.hasNext()) {
                CreationToolEntry entryCom = iter.next();
                entryCom.setParent(paGroup);
                paGroup.add(entryCom);
            }
            palette.add(paGroup);
        }
    }

    public static List<IComponent> getRelatedComponents(final IComponentsFactory compFac, String keyword) {
        return getRelatedComponents(compFac, keyword, true);
    }

    protected static List<IComponent> getRelatedComponents(final IComponentsFactory compFac, String keyword,
            boolean needCheckVisible) {
        Collection<IComponent> componentSet = null;
        IComponentsHandler componentsHandler = compFac.getComponentsHandler();
        String lowerCasedKeyword = null;
        if (keyword != null) {
            lowerCasedKeyword = keyword.toLowerCase().trim();
        }

        List<IComponent> componentAll = new ArrayList<IComponent>(compFac.readComponents());
        // filter usefull components

        if (compFac != null && componentsHandler != null) {
            componentAll = componentsHandler.filterComponents(componentAll);
        }

        if (needCheckVisible) {
            Iterator<IComponent> iter = componentAll.iterator();
            while (iter.hasNext()) {
                IComponent component = iter.next();
                if (component == null || !ComponentUtilities.isComponentVisible(component) || component.isTechnical()) {
                    iter.remove();
                }
            }
        }

        /**
         * No need to sort result from help, since it may confuse user
         */
        boolean needSort = true;

        if (compFac != null && lowerCasedKeyword != null && 0 < lowerCasedKeyword.length()) {
            componentSet = new LinkedHashSet<IComponent>();

            // 1. match the full name component
            Map<String, Map<String, Set<IComponent>>> componentNameMap = compFac.getComponentNameMap();
            if (componentNameMap != null) {
                Map<String, Set<IComponent>> map = componentNameMap.get(lowerCasedKeyword);
                if (map != null) {
                    Collection<Set<IComponent>> componentSets = map.values();
                    Iterator<Set<IComponent>> componentSetIter = componentSets.iterator();
                    List<IComponent> filteredComponent = new ArrayList<IComponent>();
                    while (componentSetIter.hasNext()) {
                        filteredComponent.addAll(componentSetIter.next());
                    }

                    if (componentsHandler != null) {
                        filteredComponent = componentsHandler.filterComponents(filteredComponent);
                    }

                    Iterator<IComponent> componentIter = filteredComponent.iterator();
                    while (componentIter.hasNext()) {
                        IComponent iComponent = componentIter.next();
                        if (iComponent == null || !ComponentUtilities.isComponentVisible(iComponent) || iComponent.isTechnical()) {
                            componentIter.remove();
                        }
                    }
                    if (!filteredComponent.isEmpty()) {
                        componentSet.addAll(filteredComponent);
                    }
                }
                for (IComponent component : componentSet) {
                    IComponent delegateComponent = UnifiedComponentUtil.getDelegateComponent(component);
                    if (delegateComponent != null) {
                        componentSet.add(delegateComponent);
                    }
                }
            }
            // 2. do usual search
            if (componentNameMap != null) {

                // 2.1 search from local palette
                addComponentsByNameFilter(compFac, componentSet, componentAll, lowerCasedKeyword);
                if (!componentSet.isEmpty()) {
                    componentSet = new LinkedHashSet<IComponent>(sortResultsBasedOnRecentlyUsed(new ArrayList<IComponent>(
                            componentSet)));
                    needSort = false;
                }

                // 2.2 search from help document
                boolean shouldSearchFromHelpAPI = PaletteSettingsPreferencePage.isPaletteSearchFromHelp();
                if (shouldSearchFromHelpAPI) {
                    String helpKeyword = keyword;
                    if (helpKeyword != null) {
                        helpKeyword = helpKeyword.trim();
                    }
                    Set<String> componentNames = getRelatedComponentNamesFromHelp(helpKeyword);
                    if (componentNames != null && 0 < componentNames.size()) {
                        int limit = PaletteSettingsPreferencePage.getPaletteSearchResultLimitFromHelp();
                        int i = 0;
                        Iterator<String> nameIter = componentNames.iterator();
                        while (nameIter.hasNext()) {
                            if (limit <= i) {
                                break;
                            }
                            String componentName = nameIter.next();
                            Map<String, Set<IComponent>> map = componentNameMap.get(componentName.toLowerCase());
                            if (map == null) {
                                continue;
                            }
                            Set<IComponent> findedComponents = map.get(componentName);
                            if (findedComponents != null && !findedComponents.isEmpty()) {
                                for (IComponent iComponent : findedComponents) {
                                    if (limit <= i) {
                                        break;
                                    }
                                    if (ComponentUtilities.isComponentVisible(iComponent) && !iComponent.isTechnical()
                                            && filterComponent(iComponent, componentsHandler)) {
                                        componentSet.add(iComponent);
                                        i++;
                                    }
                                }
                                // componentSet.addAll(findedComponents);
                            }
                        }

                        if (0 < i) {
                            needSort = false;
                        }
                    }
                }
            }
        } else if (compFac != null) {
            componentSet = new LinkedHashSet<IComponent>();
            componentSet.addAll(componentAll);
        }

        componentSet.addAll(getStitchPseudoComponents(lowerCasedKeyword));

        addDelegateComponents(compFac, componentSet, lowerCasedKeyword);

        List<IComponent> relatedComponents = null;
        if (componentSet == null || componentSet.isEmpty()) {
            relatedComponents = new LinkedList<IComponent>();
        } else {
            relatedComponents = new LinkedList<IComponent>(componentSet);
        }

        if (needSort) {
            relatedComponents = sortResultsBasedOnRecentlyUsed(relatedComponents);
        }

        return relatedComponents;
    }

    private static List<IComponent> getStitchPseudoComponents(String lowerCasedKeyword) {
        List<IComponent> componentList = new ArrayList<>();
        if (lowerCasedKeyword != null) {
            for (StitchPseudoComponent compo : StitchDataLoaderConstants.INTEGRATION_SOURCE_LIST) {
                if (compo.getName().toLowerCase().contains(lowerCasedKeyword)) {
                    componentList.add(compo);
                }
            }
            for (StitchPseudoComponent compo : StitchDataLoaderConstants.DATA_WAREHOUSE_LIST) {
                if (compo.getName().toLowerCase().contains(lowerCasedKeyword)) {
                    componentList.add(compo);
                }
            }
        }
        return componentList;
    }

	protected static List<IComponent> sortResultsBasedOnRecentlyUsed(List<IComponent> relatedComponents) {
        if (relatedComponents == null || relatedComponents.isEmpty()) {
            return relatedComponents;
        }
        List<RecentlyUsedComponent> recentlyUsedComponents = new LinkedList<TalendEditorPaletteFactory.RecentlyUsedComponent>();
        getRecentlyUsedList(recentlyUsedComponents);
        recentlyUsedComponents = sortRecentlyUsed(recentlyUsedComponents);

        if (recentlyUsedComponents != null && !recentlyUsedComponents.isEmpty()) {
            final List<String> recentlyUsedComponentNames = new ArrayList<String>(recentlyUsedComponents.size());
            for (RecentlyUsedComponent ruc : recentlyUsedComponents) {
                recentlyUsedComponentNames.add(ruc.getName());
            }

            Collections.sort(relatedComponents, new Comparator<IComponent>() {

                @Override
                public int compare(IComponent arg0, IComponent arg1) {
                    int result = 0;
                    int arg0InRecentlyUsed = recentlyUsedComponentNames.indexOf(arg0.getName());
                    int arg1InRecentlyUsed = recentlyUsedComponentNames.indexOf(arg1.getName());

                    if (0 <= arg0InRecentlyUsed && 0 <= arg1InRecentlyUsed) {
                        result = (arg0InRecentlyUsed - arg1InRecentlyUsed);
                    } else if (0 <= arg0InRecentlyUsed && arg1InRecentlyUsed < 0) {
                        result = -1;
                    } else if (arg0InRecentlyUsed < 0 && 0 <= arg1InRecentlyUsed) {
                        result = 1;
                    }

                    return result;
                }
            });
        }

        return relatedComponents;
    }

    private static List<RecentlyUsedComponent> sortRecentlyUsed(List<RecentlyUsedComponent> recentlyUsedComponents) {
        if (recentlyUsedComponents == null || recentlyUsedComponents.isEmpty()) {
            return recentlyUsedComponents;
        }
        Collections.sort(recentlyUsedComponents, new Comparator<TalendEditorPaletteFactory.RecentlyUsedComponent>() {

            @Override
            public int compare(RecentlyUsedComponent arg0, RecentlyUsedComponent arg1) {
                return -1 * arg0.getTimestamp().compareTo(arg1.getTimestamp());
            }
        });
        return recentlyUsedComponents;
    }

    protected static boolean filterComponent(IComponent component, IComponentsHandler componentsHandler) {
        boolean canUse = true;

        if (componentsHandler == null || component == null) {
            return canUse;
        }
        List<IComponent> filteredComponents = new ArrayList<IComponent>();
        filteredComponents.add(component);
        filteredComponents = componentsHandler.filterComponents(filteredComponents);
        if (filteredComponents == null || filteredComponents.isEmpty()) {
            canUse = false;
        }

        return canUse;
    }

    protected static void addComponentsByNameFilter(final IComponentsFactory compFac, Collection<IComponent> componentSet,
            List<IComponent> componentAll, String nameFilter) {
        if (compFac == null || componentSet == null) {
            return;
        }

        if (nameFilter != null && !nameFilter.trim().isEmpty()) {
            Iterator<IComponent> iter = componentAll.iterator();
            String regex = getFilterRegex(nameFilter);
            Pattern pattern = Pattern.compile(regex);
            Set<ComponentHit> resultByName = new LinkedHashSet<ComponentHit>();
            Set<ComponentHit> resultByLongName = new LinkedHashSet<ComponentHit>();
            while (iter.hasNext()) {
                IComponent xmlComponent = iter.next();

                Matcher matcher = pattern.matcher(xmlComponent.getDisplayName().toLowerCase());
                if (matcher.find()) {
                    resultByName.add(new ComponentHit(xmlComponent, matcher.start()));
                    continue;
                }

                matcher = pattern.matcher(xmlComponent.getLongName().toLowerCase());
                if (matcher.find()) {
                    resultByLongName.add(new ComponentHit(xmlComponent, matcher.start()));
                    continue;
                }
            }
            if (!resultByName.isEmpty()) {
                ComponentHit[] hitArray = resultByName.toArray(new ComponentHit[resultByName.size()]);
                Arrays.sort(hitArray);
                addComponents(componentSet, hitArray);
            }
            if (!resultByLongName.isEmpty()) {
                ComponentHit[] hitArray = resultByLongName.toArray(new ComponentHit[resultByLongName.size()]);
                Arrays.sort(hitArray);
                addComponents(componentSet, hitArray);
            }
        }

    }

    private static void addComponents(Collection<IComponent> componentSet, ComponentHit[] hitArray) {
        for (ComponentHit ch : hitArray) {
            IComponent component = ch.getComponent();
            componentSet.add(component);
        }
    }

    private static void addDelegateComponents(IComponentsFactory compFac, Collection<IComponent> componentSet,
            String lowerCasedKeyword) {
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IUnifiedComponentService.class)) {
            IUnifiedComponentService service = GlobalServiceRegister.getDefault().getService(
                    IUnifiedComponentService.class);
            service.filterUnifiedComponentForPalette(compFac, componentSet, lowerCasedKeyword);

            if (compFac != null && compFac.getComponentsHandler() != null) {
                String paletteType = compFac.getComponentsHandler().extractComponentsCategory().getName();
                LinkedHashSet<IComponent> delegateCompSet = new LinkedHashSet<IComponent>();
                List<IComponent> delegateComponents = new ArrayList<IComponent>(service.getDelegateComponents(paletteType));
                addComponentsByNameFilter(compFac, delegateCompSet, delegateComponents, lowerCasedKeyword);
                componentSet.addAll(delegateComponents);
            }

        }
    }

    /**
     * DOC cmeng Comment method "createRecentlyUsedEntry".
     *
     * @param componentsDrawer
     * @param ht
     * @param recentlyUsedList
     * @param recentlyUsedMap
     * @return
     */
    protected static void createRecentlyUsedEntryList(Hashtable<String, PaletteDrawer> ht,
            List<RecentlyUsedComponent> recentlyUsedList, Map<String, IComponent> recentlyUsedMap) {
        String name;
        String longName;
        TalendCombinedTemplateCreationEntry component;
        final int recentlyUsedSize = PaletteSettingsPreferencePage.getPaletteRencentlyUsedListSize();
        int i = 1;
        for (RecentlyUsedComponent recentlyUsed : recentlyUsedList) {
            if (recentlyUsedSize < i) {
                break;
            }
            IComponent recentlyUsedComponent = recentlyUsedMap.get(recentlyUsed.getName());
            if (recentlyUsedComponent == null) {
                continue;
            }
            if (!ComponentUtilities.isComponentVisible(recentlyUsedComponent)) {
                continue;
            }

            recentlyUsedComponent = UnifiedComponentUtil.getDelegateComponent(recentlyUsedComponent);

            ++i;
            PaletteDrawer componentsDrawer = ht.get(RECENTLY_USED);
            if (componentsDrawer != null) {
                name = UnifiedComponentUtil.getComponentDisplayNameForPalette(recentlyUsedComponent, filter);
                longName = recentlyUsedComponent.getLongName();

                ImageDescriptor imageSmall = recentlyUsedComponent.getIcon16();
                IPreferenceStore store = DesignerPlugin.getDefault().getPreferenceStore();
                ImageDescriptor imageLarge;
                final String string = store.getString(TalendDesignerPrefConstants.LARGE_ICONS_SIZE);
                if (string.equals("24")) { //$NON-NLS-1$
                    imageLarge = recentlyUsedComponent.getIcon24();
                } else {
                    imageLarge = recentlyUsedComponent.getIcon32();
                }
                component = new TalendCombinedTemplateCreationEntry(name, name, Node.class, recentlyUsedComponent, imageSmall,
                        imageLarge, filter);

                component.setDescription(longName);
                component.setParent(componentsDrawer);
                component.setTimestemp(recentlyUsed.getTimestamp());
                componentsDrawer.add(component);
            }
        }
    }

    public static TalendCombinedTemplateCreationEntry createEntryFrom(CombinedTemplateCreationEntry entry) {
        String name = entry.getLabel();
        IComponent component = ((PaletteComponentFactory) entry.getToolProperty(CreationTool.PROPERTY_CREATION_FACTORY))
                .getComponent();
        TalendCombinedTemplateCreationEntry newEntry = new TalendCombinedTemplateCreationEntry(name, name, Node.class, component,
                entry.getSmallIcon(), entry.getLargeIcon());

        newEntry.setDescription(entry.getDescription());
        return newEntry;
    }

    public static TalendCombinedTemplateCreationEntry createEntryFrom(IComponent component) {
        if (component == null) {
            return null;
        }
        IComponent delegateComponent = UnifiedComponentUtil.getDelegateComponent(component);
        String filter = null;
        if (delegateComponent != component) {
            // emf component name
            filter = component.getName();
        }
        String name = UnifiedComponentUtil.getComponentDisplayNameForPalette(delegateComponent, filter);
        ImageDescriptor imageSmall = delegateComponent.getIcon16();
        IPreferenceStore store = DesignerPlugin.getDefault().getPreferenceStore();
        ImageDescriptor imageLarge;
        final String string = store.getString(TalendDesignerPrefConstants.LARGE_ICONS_SIZE);
        if (string.equals("24")) { //$NON-NLS-1$
            imageLarge = delegateComponent.getIcon24();
        } else {
            imageLarge = delegateComponent.getIcon32();
        }
        TalendCombinedTemplateCreationEntry newEntry = new TalendCombinedTemplateCreationEntry(name, name, Node.class,
                delegateComponent, imageSmall, imageLarge, filter);

        newEntry.setDescription(component.getLongName());
        return newEntry;
    }

    protected static void createComponentsDrawer(final IComponentsFactory compFac, final boolean needHiddenComponent,
            final boolean isFavorite, final int a) {
        List<IComponent> componentList = null;
        // needn't to check visible, since this visble check will be done in method "createComponentsDrawer(...)"
        componentList = getRelatedComponents(compFac, TalendEditorPaletteFactory.filter, false);

        // Added by Marvin Wang on Jan. 10, 2012
        if (compFac.getComponentsHandler() != null) {
            // the filter already done in the method "getRelatedComponents()"
            // componentList = compFac.getComponentsHandler().filterComponents(componentList);
            componentList = compFac.getComponentsHandler().sortComponents(componentList);
        }

        Collections.sort(componentList, new Comparator<IComponent>() {

            @Override
            public int compare(IComponent component1, IComponent component2) {
                return component1.getName().toLowerCase().compareTo(component2.getName().toLowerCase());
            }

        });

        final List<IComponent> finalComponentList = componentList;

        DisplayUtils.getDisplay().syncExec(new Runnable() {

            @Override
            public void run() {
                createComponentsDrawer(finalComponentList, needHiddenComponent, isFavorite, a);
            }
        });
    }

    /** Create the "Shapes" drawer. */
    private static void createComponentsDrawer(List<IComponent> componentList, boolean needHiddenComponent, boolean isFavorite,
            int a) {
        clearGroup();
        List<CreationToolEntry> nodeList = new LinkedList<CreationToolEntry>();
        // } else if (a == 0) {
        PaletteDrawer componentsDrawer;
        String name, longName;
        String family;
        String oraFamily;
        List<String> families = new ArrayList<String>();
        HashMap<String, String> familyMap = new HashMap<String, String>();
        // boolean favoriteFlag;
        // List listName = new ArrayList();
        CombinedTemplateCreationEntry component;
        Hashtable<String, PaletteDrawer> ht = new Hashtable<String, PaletteDrawer>();
        paletteState = isFavorite;
        List<String> favoriteComponentNames = null;
        if (a == 0) {
            //            componentsDrawer = new PaletteDrawer(Messages.getString("TalendEditorPaletteFactory.Default")); //$NON-NLS-1$
            favoriteComponentNames = getFavoritesList();
        }

        IComponent matchComponent = null;
        Iterator<IComponent> componentIter = componentList.iterator();
        while (componentIter.hasNext()) {
            IComponent xmlComponent = componentIter.next();
            if (xmlComponent.isTechnical()) {
                continue;
            }
            oraFamily = xmlComponent.getOriginalFamilyName();
            family = xmlComponent.getTranslatedFamilyName();
            if (xmlComponent.isLoaded()) {
                if (StringUtils.isNotBlank(filter)) {
                    if (xmlComponent.getName().toLowerCase().trim().equals(filter.toLowerCase().trim())) {
                        matchComponent = xmlComponent;
                    }
                }
                String[] strings = family.split(ComponentsFactoryProvider.FAMILY_SEPARATOR_REGEX);
                String[] oraStrings = oraFamily.split(ComponentsFactoryProvider.FAMILY_SEPARATOR_REGEX);
                for (int j = 0; j < strings.length; j++) {
                    try {
                        if (!needHiddenComponent && !xmlComponent.isVisible(oraStrings[j])) {
                            continue;
                        }
                    } catch (Exception e) {
                        System.out.println();
                    }
                    // String key = null;
                    // key = xmlComponent.getName() + "#" + oraStrings[j];//$NON-NLS-1$

                    if (a == 0) {
                        if (!oraStrings[j].equals("Misc")) {//$NON-NLS-1$
                            if (isFavorite
                                    && !(favoriteComponentNames != null && favoriteComponentNames
                                            .contains(xmlComponent.getName()))) {

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

        List<String> recentlyUsedComponentNames = null;
        List<RecentlyUsedComponent> recentlyUsedComponents = null;
        if (a == 0) {
            // if a==1, then means hide folder mode
            recentlyUsedComponents = new LinkedList<TalendEditorPaletteFactory.RecentlyUsedComponent>();
            recentlyUsedComponentNames = getRecentlyUsedList(recentlyUsedComponents);
            Collections.sort(recentlyUsedComponents, new Comparator<TalendEditorPaletteFactory.RecentlyUsedComponent>() {

                @Override
                public int compare(RecentlyUsedComponent arg0, RecentlyUsedComponent arg1) {
                    return -1 * arg0.getTimestamp().compareTo(arg1.getTimestamp());
                }
            });

            families.add(0, FAVORITES);
            familyMap.put(FAVORITES, FAVORITES);

            families.add(1, RECENTLY_USED);
            familyMap.put(RECENTLY_USED, RECENTLY_USED);

            //for TUP-19604 upper the priority of full matched component family
            if (matchComponent != null) {
                String[] matchFamily = matchComponent.getTranslatedFamilyName()
                        .split(ComponentsFactoryProvider.FAMILY_SEPARATOR_REGEX);
                for (String familyObj : matchFamily) {
                    if (families.contains(familyObj)) {
                        families.remove(familyObj);
                        families.add(2, familyObj);
                    }
                }
            }

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
        Map<String, IComponent> recentlyUsedMap = new HashMap<String, IComponent>();

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
        IPreferenceStore store = DesignerPlugin.getDefault().getPreferenceStore();
        String largeIconsSize = store.getString(TalendDesignerPrefConstants.LARGE_ICONS_SIZE);
        CreationToolEntry noteCreationToolEntry = createNoteEntry(largeIconsSize);
        noteCreationToolEntry.setParent(drawer);
        drawer.add(noteCreationToolEntry);

        componentIter = componentList.iterator();
        while (componentIter.hasNext()) {
            IComponent xmlComponent = componentIter.next();

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
                String regex = getFilterRegex(filter);
                needAddNote = "Note".toLowerCase().matches(regex); //$NON-NLS-1$
            }

            family = xmlComponent.getTranslatedFamilyName();
            oraFamily = xmlComponent.getOriginalFamilyName();

            // String[] keys = family.split(ComponentsFactoryProvider.FAMILY_SEPARATOR_REGEX);
            // String[] oraKeys = oraFamily.split(ComponentsFactoryProvider.FAMILY_SEPARATOR_REGEX);
            // for (String key2 : keys) {
            // String key = null;
            //                key = xmlComponent.getName() + "#" + oraKeys[j];//$NON-NLS-1$
            if (isFavorite && !(favoriteComponentNames != null && favoriteComponentNames.contains(xmlComponent.getName()))) {
                continue;
            }

            // }

            if (xmlComponent.isLoaded()) {
                name = UnifiedComponentUtil.getComponentDisplayNameForPalette(xmlComponent, filter);
                longName = xmlComponent.getLongName();

                if(!isDBCommonVisible(xmlComponent, name)){
                    continue;
                }

                ImageDescriptor imageSmall = xmlComponent.getIcon16();
                ImageDescriptor imageLarge;
                if (largeIconsSize.equals("24")) { //$NON-NLS-1$
                    imageLarge = xmlComponent.getIcon24();
                } else {
                    imageLarge = xmlComponent.getIcon32();
                }

                if (favoriteComponentNames != null && favoriteComponentNames.contains(xmlComponent.getName())) {
                    componentsDrawer = ht.get(FAVORITES);
                    if (componentsDrawer != null) {
                        component = new TalendCombinedTemplateCreationEntry(name, name, Node.class, xmlComponent, imageSmall,
                                imageLarge, filter);

                        component.setDescription(longName);
                        component.setParent(componentsDrawer);
                        componentsDrawer.add(component);
                    }
                }

                if (recentlyUsedComponentNames != null && recentlyUsedComponentNames.contains(xmlComponent.getName())) {
                    recentlyUsedMap.put(xmlComponent.getName(), xmlComponent);
                }

                if (isFavorite && !(favoriteComponentNames != null && favoriteComponentNames.contains(xmlComponent.getName()))) {
                    continue;
                }

                String[] strings = family.split(ComponentsFactoryProvider.FAMILY_SEPARATOR_REGEX);
                String[] oraStrings = oraFamily.split(ComponentsFactoryProvider.FAMILY_SEPARATOR_REGEX);
                for (int j = 0; j < strings.length; j++) {
                    if (!needHiddenComponent && !xmlComponent.isVisible(oraStrings[j])) {
                        continue;
                    }
                    // String key = null;
                    // key = xmlComponent.getName() + "#" + oraStrings[j];//$NON-NLS-1$

                    component = new TalendCombinedTemplateCreationEntry(name, name, Node.class, xmlComponent, imageSmall,
                            imageLarge, filter);

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
                        Iterator<CreationToolEntry> iter = nodeList.iterator();
                        while (iter.hasNext()) {

                            if ((iter.next().getLabel()).equals(component.getLabel())) {
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

        if (a == 0) {
            createRecentlyUsedEntryList(ht, recentlyUsedComponents, recentlyUsedMap);
        }

        if (a == 1) {
            Iterator<CreationToolEntry> iter = nodeList.iterator();
            while (iter.hasNext()) {
                CreationToolEntry entryComponent = iter.next();
                entryComponent.setParent(paGroup);
                paGroup.add(entryComponent);
            }
            palette.add(paGroup);
        }
        setFilter(""); //$NON-NLS-1$
    }

    private static boolean isDBCommonVisible(IComponent xmlComponent, String name){
        if(xmlComponent == null || name == null){
            return true;
        }
        if(!xmlComponent.getOriginalFamilyName().equals(IComponentDelegate.FAMILY)){
            return true;
        }
        if(!name.contains("(") || !name.contains(")")){
            return true;
        }
        String dbType = name.substring(name.indexOf("(")+1, name.indexOf(")"));
        String comName = xmlComponent.getName().replaceFirst("DB", dbType);
        IComponent component  = ComponentsFactoryProvider.getInstance().get(comName, xmlComponent.getPaletteType());
        if(component == null){
            return true;
        }
        return ComponentUtilities.isComponentVisible(component);
    }

    public static void addNewFavoriteIntoPreference(String componentName) {
        List<String> favoritesList = getFavoritesList();
        if (favoritesList.contains(componentName)) {
            return;
        }

        favoritesList.add(componentName);
        storeFavoritesList(favoritesList);
    }

    public static void deleteFavoriteFromPreference(String componentName) {
        List<String> favoritesList = getFavoritesList();
        if (!favoritesList.contains(componentName)) {
            return;
        }

        favoritesList.remove(componentName);
        storeFavoritesList(favoritesList);
    }

    protected static void storeFavoritesList(List<String> favoritesList) {
        StringBuffer favoritesBuffer = new StringBuffer();
        // List<String> alreadyExistsFavorList = getFavoritesList();
        // Set<String> allFavoritesList = new HashSet<String>();
        // allFavoritesList.addAll(favoritesList);
        // allFavoritesList.addAll(alreadyExistsFavorList);
        boolean needAddSeperator = false;
        for (String favorite : favoritesList) {
            if (needAddSeperator) {
                favoritesBuffer.append(FAVORITES_KEY_LIST_SEPERATOR);
            } else {
                needAddSeperator = true;
            }
            favoritesBuffer.append(favorite);
        }
        DesignerPlugin.getDefault().getPreferenceStore().putValue(FAVORITES_KEY, favoritesBuffer.toString());
    }

    public static List<String> getFavoritesList() {
        List<String> favoritesList = null;
        String favoritesString = DesignerPlugin.getDefault().getPreferenceStore().getString(FAVORITES_KEY);
        if (StringUtils.isNotEmpty(favoritesString)) {
            String[] favoritesArray = favoritesString.split(FAVORITES_KEY_LIST_SEPERATOR);
            if (favoritesArray != null && 0 < favoritesArray.length) {
                favoritesList = new ArrayList<String>(Arrays.asList(favoritesArray));
            }
        }
        if (favoritesList == null) {
            favoritesList = new ArrayList<String>();
        }
        return favoritesList;
    }

    public static void storeRecentlyUsedList(List<RecentlyUsedComponent> recentlyUsedList) {
        StringBuffer recentlyUsedBuffer = new StringBuffer();
        boolean needAddSeperator = false;
        Set<RecentlyUsedComponent> allRecentlyUsedList = new LinkedHashSet<TalendEditorPaletteFactory.RecentlyUsedComponent>();
        // **MUST** add recently used list first
        allRecentlyUsedList.addAll(recentlyUsedList);

        List<RecentlyUsedComponent> alreadyExistRecentlyUsedList = new ArrayList<TalendEditorPaletteFactory.RecentlyUsedComponent>();
        getRecentlyUsedList(alreadyExistRecentlyUsedList);
        allRecentlyUsedList.addAll(alreadyExistRecentlyUsedList);
        for (RecentlyUsedComponent recentlyUsedEntry : allRecentlyUsedList) {
            if (needAddSeperator) {
                recentlyUsedBuffer.append(RECENTLY_USED_LIST_SEPERATOR);
            } else {
                needAddSeperator = true;
            }
            recentlyUsedBuffer.append(recentlyUsedEntry.getName() + RECENTLY_USED_COMPONENT_NAME_TIMESTEMP_SEPERATOR
                    + recentlyUsedEntry.getTimestamp().getTime());
        }
        DesignerPlugin.getDefault().getPreferenceStore().putValue(RECENTLY_USED_KEY, recentlyUsedBuffer.toString());
    }

    public static List<String> getRecentlyUsedList(List<RecentlyUsedComponent> recentlyUsedList) {
        List<String> recentlyUsedNameList = new ArrayList<String>();
        String recentlyUsedString = DesignerPlugin.getDefault().getPreferenceStore().getString(RECENTLY_USED_KEY);
        if (StringUtils.isNotEmpty(recentlyUsedString)) {
            List<String> nameWithTimestampList = Arrays.asList(recentlyUsedString.split(RECENTLY_USED_LIST_SEPERATOR));
            for (String nameWithTimestamp : nameWithTimestampList) {
                String[] nameWithTimestampArray = nameWithTimestamp.split(RECENTLY_USED_COMPONENT_NAME_TIMESTEMP_SEPERATOR);
                if (nameWithTimestampArray == null || nameWithTimestampArray.length < 2) {
                    continue;
                }
                RecentlyUsedComponent recentlyUsedComponent = new RecentlyUsedComponent();
                try {
                    recentlyUsedComponent.setTimestamp(new Date(Long.valueOf(nameWithTimestampArray[1])));
                } catch (Exception e) {
                    continue;
                }
                recentlyUsedComponent.setName(nameWithTimestampArray[0]);
                recentlyUsedNameList.add(nameWithTimestampArray[0]);
                recentlyUsedList.add(recentlyUsedComponent);
            }
        }
        return recentlyUsedNameList;
    }

    public static void deleteJobletConfigurationsFromPalette(String jobletName) {
        deleteFavoriteFromPreference(jobletName);
        deleteRecentlyUsedComponentFromPreference(jobletName);
    }

    /**
     * DOC cmeng Comment method "deleteRecentlyUsedComponentFromPreference".
     *
     * @param jobletName
     */
    protected static void deleteRecentlyUsedComponentFromPreference(String jobletName) {
        List<RecentlyUsedComponent> recentlyUsedList = new LinkedList<TalendEditorPaletteFactory.RecentlyUsedComponent>();
        getRecentlyUsedList(recentlyUsedList);

        StringBuffer recentlyUsedBuffer = new StringBuffer();
        boolean needAddSeperator = false;
        boolean finded = false;
        for (TalendEditorPaletteFactory.RecentlyUsedComponent recentlyUsedEntry : recentlyUsedList) {
            if (recentlyUsedEntry.getName().equals(jobletName)) {
                finded = true;
                continue;
            }
            if (needAddSeperator) {
                recentlyUsedBuffer.append(RECENTLY_USED_LIST_SEPERATOR);
            } else {
                needAddSeperator = true;
            }
            recentlyUsedBuffer.append(recentlyUsedEntry.getName() + RECENTLY_USED_COMPONENT_NAME_TIMESTEMP_SEPERATOR
                    + recentlyUsedEntry.getTimestamp().getTime());
        }
        if (finded) {
            DesignerPlugin.getDefault().getPreferenceStore().putValue(RECENTLY_USED_KEY, recentlyUsedBuffer.toString());
        }
    }

    /**
     * yzhang Comment method "getFilterRegex".
     *
     * @return
     */
    private static String getFilterRegex(String nameFilter) {
        String regex = nameFilter.replaceAll("\\*", ".*"); //$NON-NLS-1$ //$NON-NLS-2$
        regex = regex.replaceAll(" ", ".*"); //$NON-NLS-1$ //$NON-NLS-2$
        regex = regex.replaceAll("\\?", ".?"); //$NON-NLS-1$ //$NON-NLS-2$
        regex = regex.replaceAll("[\\{\\}\\[\\]]", "\\."); //$NON-NLS-1$ //$NON-NLS-2$

        try {
            Pattern.compile(regex);
        } catch (Throwable e) {
            regex = ".*"; //$NON-NLS-1$
            CommonExceptionHandler.process(e, Priority.WARN);
        }
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
        final PaletteDrawer paletteDrawer = new TalendPaletteDrawer(family);
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
        AbstractProcessProvider.loadComponentsFromProviders(getJobletObjectType(compFac.getComponentsHandler()));
        GenericProcessProvider.getInstance().loadComponentsFromProviders();
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
        AbstractProcessProvider.loadComponentsFromProviders(getJobletObjectType(compFac.getComponentsHandler()));
        GenericProcessProvider.getInstance().loadComponentsFromProviders();
        createComponentsDrawer(compFac, false, isFavorite, histate);
        return palette;
    }

    public static PaletteRoot getAllNodeStructure(final IComponentsFactory compFac) {
        palette = new PaletteRoot();
        AbstractProcessProvider.loadComponentsFromProviders(getJobletObjectType(compFac.getComponentsHandler()));
        GenericProcessProvider.getInstance().loadComponentsFromProviders();
        createComponentsDrawer(compFac, true, 0);
        return palette;
    }

    public static PaletteRoot createPalletteForMapReduce(IComponentsFactory compFac, PaletteRoot root) {
        palette = root;
        int histate = DesignerPlugin.getDefault().getPreferenceStore().getInt("HiddenState"); //$NON-NLS-1$
        AbstractProcessProvider.loadComponentsFromProviders(getJobletObjectType(compFac.getComponentsHandler()));
        GenericProcessProvider.getInstance().loadComponentsFromProviders();
        createComponentsDrawer(compFac, false, histate);
        return palette;
    }

    protected static CreationToolEntry createNoteEntry(String largeIconsSize) {
        ImageDescriptor imageLargeIcon = null;
        if (largeIconsSize.equals("24")) { //$NON-NLS-1$
            imageLargeIcon = ImageProvider.getImageDesc(ECoreImage.NOTE_MIDDLE_ICON);
        } else {
            imageLargeIcon = ImageProvider.getImageDesc(ECoreImage.NOTE_BIG_ICON);
        }
        return new CreationToolEntry(Messages.getString("TalendEditorPaletteFactory.Note"), //$NON-NLS-1$
                Messages.getString("TalendEditorPaletteFactory.CreateNote"), //$NON-NLS-1$
                new NoteCreationFactory(), ImageProvider.getImageDesc(ECoreImage.NOTE_SMALL_ICON), imageLargeIcon);
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

    private static ERepositoryObjectType getJobletObjectType(IComponentsHandler handler) {
        if (handler == null) {
            return null;
        }
        ComponentCategory category = handler.extractComponentsCategory();
        ERepositoryObjectType type = ERepositoryObjectType.JOBLET;
        if (category == null) {
            return type;
        }
        switch (category) {
        case CATEGORY_4_SPARK:
            type = ERepositoryObjectType.SPARK_JOBLET;
            break;
        case CATEGORY_4_SPARKSTREAMING:
            type = ERepositoryObjectType.SPARK_STREAMING_JOBLET;
            break;
        default:
            break;
        }
        return type;
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

    static Job searchInHelpJob = null;

    protected static Set<String> getRelatedComponentNamesFromHelp(final String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return null;
        }
        // This method will cost lots of time to complete when it is called the first time
        final List<SearchHit> querySearchResult = new ArrayList<SearchHit>();
        if (searchInHelpJob != null && searchInHelpJob.getState() != Job.NONE) {
            searchInHelpJob.cancel();
        }
        searchInHelpJob = new Job(SEARCHING_FROM_HELP) {

            @SuppressWarnings("restriction")
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                String helpKeyword = keyword;
                try {
                    TalendPaletteSearchIndex searchIndex = TalendPaletteSearchIndex.getInstance();
                    LocalSearchManager localSearchManager = BaseHelpSystem.getLocalSearchManager();
                    // if not work or null, maybe should throw a warn to inform user and help us trace
                    // if (searchIndex != null && localSearchManager != null) {
                    localSearchManager.ensureIndexUpdated(monitor, searchIndex);

                    final String WHITESPACE = " "; //$NON-NLS-1$
                    helpKeyword = helpKeyword + WHITESPACE + "OR" + WHITESPACE + helpKeyword + "*"; //$NON-NLS-1$//$NON-NLS-2$

                    ISearchQuery searchQuery = new SearchQuery(helpKeyword, false, new ArrayList<String>(), Platform.getNL());
                    searchIndex.search(searchQuery, new ISearchHitCollector() {

                        @Override
                        public void addQTCException(QueryTooComplexException exception) throws QueryTooComplexException {
                            // nothing need to do
                        }

                        @Override
                        public void addHits(List<SearchHit> hits, String wordsSearched) {
                            querySearchResult.addAll(hits);
                        }
                    });
                    // }
                } catch (Throwable e) {
                    CommonExceptionHandler.process(e, Priority.WARN);
                }
                return Status.OK_STATUS;
            }

            @Override
            public boolean belongsTo(Object family) {
                return family.equals(FederatedSearchJob.FAMILY);
            }
        };

        try {
            searchInHelpJob.setPriority(Job.INTERACTIVE);
            searchInHelpJob.schedule();
            searchInHelpJob.join();
        } catch (Throwable e) {
            CommonExceptionHandler.process(e, Priority.WARN);
        }

        Set<String> componentNames = new LinkedHashSet<String>();
        // the limitation has been moved to it's caller
        // int limit = PaletteSettingsPreferencePage.getPaletteSearchResultLimitFromHelp();
        // int i = 1;
        Iterator<SearchHit> iter = querySearchResult.iterator();
        while (iter.hasNext()) {
            // if (limit < i) {
            // break;
            // }
            SearchHit result = iter.next();
            String label = result.getLabel();
            if (label == null || label.trim().length() == 0) {
                continue;
            }
            componentNames.add(label);
            // i++;
        }

        return componentNames;
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

    public static class RecentlyUsedComponent {

        protected String displayName;

        protected String name;

        protected Date timestamp;

        /**
         * Getter for name.
         *
         * @return the name
         */
        public String getName() {
            return this.name;
        }

        /**
         * Sets the name.
         *
         * @param name the name to set
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * Sets the displayName.
         *
         * @param displayName the displayName to set
         */
        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        /**
         * Getter for displayName.
         *
         * @return the displayName
         */
        public String getDisplayName() {
            return this.displayName;
        }

        /**
         * Getter for timestamp.
         *
         * @return the timestamp
         */
        public Date getTimestamp() {
            return this.timestamp;
        }

        /**
         * Sets the timestamp.
         *
         * @param timestamp the timestamp to set
         */
        public void setTimestamp(Date timestamp) {
            this.timestamp = timestamp;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof RecentlyUsedComponent)) {
                return false;
            }
            RecentlyUsedComponent recentlyUsed = (RecentlyUsedComponent) obj;
            return this.getName().equals(recentlyUsed.getName());
        }

        @Override
        public int hashCode() {
            String hashName = name;
            if (hashName == null) {
                hashName = ""; //$NON-NLS-1$
            }
            return hashName.hashCode();
        }

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return name;
        }
    }
}
