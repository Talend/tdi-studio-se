// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

    private static final String TMYSQLSP = "tMysqlSP";

    private static final String TMYSQLROW = "tMysqlRow";

    private static final String TJAVA = "tJava";

    private static final String TJAVAROW = "tJavaRow";

    private static final String TROWGENERATOR = "tRowGenerator";

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
        TalendEditorPaletteFactory.addComponentsByNameFilter(componentsFactory, componentHits, keyword);

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
    public void testGetRelatedComponents() {
        String keywords1 = "tjava";
        String keywords2 = "write java code";
        String keywords3 = "row";
        IComponentsFactory componentsFactory = ComponentsFactoryProvider.getInstance();

        /**
         * Result without RecentlyUsed
         */
        List<IComponent> componentHits = TalendEditorPaletteFactory.getRelatedComponents(componentsFactory, keywords1);
        assert (componentHits.get(0).getName().equals(TJAVA));

        componentHits = TalendEditorPaletteFactory.getRelatedComponents(componentsFactory, keywords2);
        assert (componentHits.get(0).getName().equals(TJAVA));

        componentHits = TalendEditorPaletteFactory.getRelatedComponents(componentsFactory, keywords3);
        assert (componentHits.get(0).getName().equals(TROWGENERATOR));


        /**
         * Result with RecentlyUsed
         */
        storeRecentlyUsed();

        componentHits = TalendEditorPaletteFactory.getRelatedComponents(componentsFactory, keywords1);
        assert (componentHits.get(0).getName().equals(TJAVAROW));

        // won't sort result from help
        componentHits = TalendEditorPaletteFactory.getRelatedComponents(componentsFactory, keywords2);
        assert (componentHits.get(0).getName().equals(TJAVA));

        componentHits = TalendEditorPaletteFactory.getRelatedComponents(componentsFactory, keywords3);
        assert (componentHits.get(0).getName().equals(TMYSQLROW));
        assert (componentHits.get(1).getName().equals(TJAVAROW));
    }

    private void storeRecentlyUsed() {
        List<RecentlyUsedComponent> recentlyUsedList = new ArrayList<RecentlyUsedComponent>(3);
        RecentlyUsedComponent urc = new RecentlyUsedComponent();
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        urc.setName(TMYSQLROW);
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
        TalendEditorPaletteFactory.deleteRecentlyUsedComponentFromPreference(TMYSQLROW);
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
        public List<? extends INodeReturn> createReturns() {
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

        /* (non-Javadoc)
         * @see org.talend.core.model.components.IComponent#getModulesNeeded(org.talend.core.model.process.INode)
         */
        @Override
        public List<ModuleNeeded> getModulesNeeded(INode node) {
            return getModulesNeeded();
        }

    }
}
