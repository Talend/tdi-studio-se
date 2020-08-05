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
import java.util.GregorianCalendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.resource.ImageDescriptor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.core.model.components.EComponentType;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IComponentsFactory;
import org.talend.core.model.components.IMultipleComponentManager;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.model.process.INodeReturn;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.temp.ECodePart;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.ui.editor.TalendEditorPaletteFactory.RecentlyUsedComponent;

/**
 * created by cmeng on Jul 21, 2016 Detailled comment
 *
 */
@SuppressWarnings("nls")
public class TalendEditorPaletteFactoryTest {

    private static final String TJAVA = "tJava";

    private static final String TJAVAROW = "tJavaRow";

    private static final String TROWGENERATOR = "tRowGenerator";

    private static final String TMYSQLSP = "tMysqlSP";

    /**
     * NOTE: the first ran junit test in this class may take longer time, since it will index help document first time.
     */

    @Before
    public void before() {
        removeRecentlyUsed();
    }

    @Test
    public void testAddComponentsByNameFilter() {
        IComponentsFactory componentsFactory = ComponentsFactoryProvider.getInstance();
        String keyword = "mysql";
        Set<IComponent> componentHits = new LinkedHashSet<IComponent>();
        List<IComponent> componentAll = new ArrayList<IComponent>(componentsFactory.readComponents());
        TalendEditorPaletteFactory.addComponentsByNameFilter(componentsFactory, componentHits, componentAll, keyword);

        /**
         * NOTE: the expect result may change if we add/change some new components in someday
         */
        assert (componentHits.iterator().next().getName().equals(TMYSQLSP));
    }

    @Test
    public void testFullMatch() {
        IComponentsFactory componentsFactory = ComponentsFactoryProvider.getInstance();
        String keyword = "tjava";
        List<IComponent> componentHits = TalendEditorPaletteFactory.getRelatedComponents(componentsFactory, keyword);

        /**
         * NOTE: the expect result may change if we add/change some new components in someday
         */
        assert (componentHits.get(0).getName().equals(TJAVA));
        assert (1 < componentHits.size());
    }

    @Test
    public void testSortResultBasedOnRecentlyUsed() {
        final String component1 = "component1";
        final String component2 = "component2";
        final String component3 = "component3";

        List<RecentlyUsedComponent> recentlyUsedList = new ArrayList<RecentlyUsedComponent>(3);
        RecentlyUsedComponent urc = new RecentlyUsedComponent();
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        urc.setName(component1);
        gregorianCalendar.set(2016, 7, 20);
        urc.setTimestamp(gregorianCalendar.getTime());
        recentlyUsedList.add(urc);

        urc = new RecentlyUsedComponent();
        urc.setName(component2);
        gregorianCalendar.set(2016, 7, 22);
        urc.setTimestamp(gregorianCalendar.getTime());
        recentlyUsedList.add(urc);

        urc = new RecentlyUsedComponent();
        urc.setName(component3);
        gregorianCalendar.set(2016, 7, 21);
        urc.setTimestamp(gregorianCalendar.getTime());
        recentlyUsedList.add(urc);

        TalendEditorPaletteFactory.storeRecentlyUsedList(recentlyUsedList);

        IComponent comp1 = new DummyComponent(component1, component1 + " long name");
        IComponent comp2 = new DummyComponent(component2, component2 + " long name");
        IComponent comp3 = new DummyComponent(component3, component3 + " long name");

        List<IComponent> componentHits = new ArrayList<IComponent>(3);
        componentHits.add(comp1);
        componentHits.add(comp2);
        componentHits.add(comp3);
        TalendEditorPaletteFactory.sortResultsBasedOnRecentlyUsed(componentHits);

        assert (componentHits.get(0) == comp2);
        assert (componentHits.get(1) == comp3);
        assert (componentHits.get(2) == comp1);
    }

    @Test
    public void testGetRelatedComponents_ComponentName() {
        storeRecentlyUsed();

        IComponentsFactory componentsFactory = ComponentsFactoryProvider.getInstance();

        String keyword = "tJava";
        List<IComponent> componentHits = TalendEditorPaletteFactory.getRelatedComponents(componentsFactory, keyword);
        Assert.assertFalse("Can't find any components", componentHits.isEmpty());
        assertExistedComponent(TJAVA, keyword, componentHits);
        assertExistedComponent(TJAVAROW, keyword, componentHits);

        keyword = "tjava";
        componentHits = TalendEditorPaletteFactory.getRelatedComponents(componentsFactory, keyword);
        Assert.assertFalse("Can't find any components", componentHits.isEmpty());
        assertExistedComponent(TJAVA, keyword, componentHits);
        assertExistedComponent(TJAVAROW, keyword, componentHits);
    }

    @Test
    public void testGetRelatedComponents_partOfComponentName() {
        storeRecentlyUsed();

        IComponentsFactory componentsFactory = ComponentsFactoryProvider.getInstance();

        String keyword = "row";
        List<IComponent> componentHits = TalendEditorPaletteFactory.getRelatedComponents(componentsFactory, keyword);
        Assert.assertFalse("Can't find any components", componentHits.isEmpty());
        assertExistedComponent(TJAVAROW, keyword, componentHits);
        assertExistedComponent(TROWGENERATOR, keyword, componentHits);

        keyword = "Row";
        componentHits = TalendEditorPaletteFactory.getRelatedComponents(componentsFactory, keyword);
        Assert.assertFalse("Can't find any components", componentHits.isEmpty());
        assertExistedComponent(TJAVAROW, keyword, componentHits);
        assertExistedComponent(TROWGENERATOR, keyword, componentHits);
    }

    @Test
    public void testGetRelatedComponents_help() {
        storeRecentlyUsed();

        IComponentsFactory componentsFactory = ComponentsFactoryProvider.getInstance();

        String keyword = "enter code";
        List<IComponent> componentHits = TalendEditorPaletteFactory.getRelatedComponents(componentsFactory, keyword);
        Assert.assertFalse("Can't find any components", componentHits.isEmpty());
        assertExistedComponent(TJAVA, keyword, componentHits);

        keyword = "enter";
        componentHits = TalendEditorPaletteFactory.getRelatedComponents(componentsFactory, keyword);
        Assert.assertFalse("Can't find any components", componentHits.isEmpty());
        assertExistedComponent(TJAVA, keyword, componentHits);
        assertExistedComponent(TJAVAROW, keyword, componentHits);
    }

    @Test
    public void testGetRelatedComponents_DelegateComponent() {
        storeRecentlyUsed();
        IComponentsFactory componentsFactory = ComponentsFactoryProvider.getInstance();
        ProcessComponentsHandler processComponentsHandler = new ProcessComponentsHandler();
        componentsFactory.setComponentsHandler(processComponentsHandler);
        String keyword = "tDBConnection";
        List<IComponent> componentHits = TalendEditorPaletteFactory.getRelatedComponents(componentsFactory, keyword);
        Assert.assertFalse("Can't find any components " + keyword, componentHits.isEmpty());
        assert (keyword.equals(componentHits.get(0).getName()));

        String keyword1 = "tDBInput";
        List<IComponent> componentHits1 = TalendEditorPaletteFactory.getRelatedComponents(componentsFactory,
                keyword1.toLowerCase());
        Assert.assertFalse("Can't find any components " + keyword, componentHits1.isEmpty());
        assert (keyword1.equals(componentHits1.get(0).getName()));

        String keyword2 = "tdb";
        List<IComponent> componentHits2 = TalendEditorPaletteFactory.getRelatedComponents(componentsFactory, keyword2);
        Assert.assertFalse("Can't find any components related" + keyword, componentHits2.isEmpty());
        assertExistedComponent(keyword, keyword2, componentHits2); // tdb includes tDBConnection
        assertExistedComponent(keyword1, keyword2, componentHits2); // tdb includes tDBInput

    }

    private void assertExistedComponent(String compName, String keywords, List<IComponent> componentHits) {
        boolean found = false;
        for (IComponent comp : componentHits) {
            if (comp.getName().equals(compName)) {
                found = true;
                break;
            }
        }
        Assert.assertTrue("Can't find the keywords \"" + keywords + "\" for component: " + compName, found);
    }

    private void storeRecentlyUsed() {
        List<RecentlyUsedComponent> recentlyUsedList = new ArrayList<RecentlyUsedComponent>(3);
        RecentlyUsedComponent urc = new RecentlyUsedComponent();
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        urc.setName(TROWGENERATOR);
        gregorianCalendar.set(2016, 7, 22);
        urc.setTimestamp(gregorianCalendar.getTime());
        recentlyUsedList.add(urc);

        urc = new RecentlyUsedComponent();
        urc.setName(TJAVAROW);
        gregorianCalendar.set(2016, 7, 21);
        urc.setTimestamp(gregorianCalendar.getTime());
        recentlyUsedList.add(urc);

        TalendEditorPaletteFactory.storeRecentlyUsedList(recentlyUsedList);
    }

    private void removeRecentlyUsed() {
        TalendEditorPaletteFactory.deleteRecentlyUsedComponentFromPreference(TROWGENERATOR);
        TalendEditorPaletteFactory.deleteRecentlyUsedComponentFromPreference(TJAVAROW);
    }

    private class DummyComponent implements IComponent {

        private String name;

        private String longName;

        public DummyComponent(String n, String ln) {
            this.name = n;
            this.longName = ln;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getOriginalName() {
            return getName();
        }

        @Override
        public String getLongName() {
            return longName;
        }

        @Override
        public String getOriginalFamilyName() {
            return null;
        }

        @Override
        public String getTranslatedFamilyName() {
            return null;
        }

        @Override
        public void setImageRegistry(Map<String, ImageDescriptor> imageRegistry) {
            // nothing to do
        }

        @Override
        public Map<String, ImageDescriptor> getImageRegistry() {
            return null;
        }

        @Override
        public ImageDescriptor getIcon32() {
            return null;
        }

        @Override
        public ImageDescriptor getIcon24() {
            return null;
        }

        @Override
        public ImageDescriptor getIcon16() {
            return null;
        }

        @Override
        public List<? extends IElementParameter> createElementParameters(INode node) {
            return null;
        }

        @Override
        public List<? extends INodeReturn> createReturns(INode node) {
            return null;
        }

        @Override
        public List<? extends INodeConnector> createConnectors(INode node) {
            return null;
        }

        @Override
        public boolean hasConditionalOutputs() {
            return false;
        }

        @Override
        public boolean isMultiplyingOutputs() {
            return false;
        }

        @Override
        public String getPluginExtension() {
            return null;
        }

        @Override
        public boolean isSchemaAutoPropagated() {
            return false;
        }

        @Override
        public boolean isDataAutoPropagated() {
            return false;
        }

        @Override
        public boolean isHashComponent() {
            return false;
        }

        @Override
        public boolean useMerge() {
            return false;
        }

        @Override
        public boolean useLookup() {
            return false;
        }

        @Override
        public String getVersion() {
            return null;
        }

        @Override
        public List<IMultipleComponentManager> getMultipleComponentManagers() {
            return null;
        }

        @Override
        public boolean isLoaded() {
            return false;
        }

        @Override
        public boolean isVisible() {
            return false;
        }

        @Override
        public boolean isVisible(String family) {
            return false;
        }

        @Override
        public List<ModuleNeeded> getModulesNeeded() {
            return null;
        }

        @Override
        public String getPathSource() {
            return null;
        }

        @Override
        public List<ECodePart> getAvailableCodeParts() {
            return null;
        }

        @Override
        public List<String> getPluginDependencies() {
            return null;
        }

        @Override
        public boolean isMultipleOutput() {
            return false;
        }

        @Override
        public boolean useImport() {
            return false;
        }

        @Override
        public EComponentType getComponentType() {
            return null;
        }

        @Override
        public boolean isTechnical() {
            return false;
        }

        @Override
        public boolean isVisibleInComponentDefinition() {
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
        public String getShortName() {
            return null;
        }

        @Override
        public String getCombine() {
            return null;
        }

        @Override
        public IProcess getProcess() {
            return null;
        }

        @Override
        public String getPaletteType() {
            return null;
        }

        @Override
        public void setPaletteType(String paletteType) {
            // nothing to do
        }

        @Override
        public String getRepositoryType() {
            return null;
        }

        @Override
        public boolean isLog4JEnabled() {
            return false;
        }

        @Override
        public EList getCONNECTORList() {
            return null;
        }

        @Override
        public String getType() {
            return null;
        }

        @Override
        public boolean isReduce() {
            return false;
        }

        @Override
        public String getInputType() {
            return null;
        }

        @Override
        public String getOutputType() {
            return null;
        }

        @Override
        public boolean isSparkAction() {
            return false;
        }

        @Override
        public String getPartitioning() {
            return null;
        }

        @Override
        public boolean isSupportDbType() {
            return false;
        }

        @Override
        public boolean isAllowedPropagated() {
            return false;
        }

        /*
         * (non-Javadoc)
         *
         * @see org.talend.core.model.components.IComponent#getModulesNeeded(org.talend.core.model.process.INode)
         */
        @Override
        public List<ModuleNeeded> getModulesNeeded(INode node) {
            return getModulesNeeded();
        }

        @Override
        public String getTemplateFolder() {
            return null;
        }

        @Override
        public String getTemplateNamePrefix() {
            return getName();
        }

        /*
         * (non-Javadoc)
         *
         * @see org.talend.core.model.components.IComponent#setOriginalFamilyName(java.lang.String)
         */
        @Override
        public void setOriginalFamilyName(String familyName) {
            // TODO Auto-generated method stub

        }

        /*
         * (non-Javadoc)
         *
         * @see org.talend.core.model.components.IComponent#getTranslatedFamilyName(java.lang.String)
         */
        @Override
        public void setTranslatedFamilyName(String translatedFamilyName) {
            // TODO Auto-generated method stub

        }

    }
}
