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
package org.talend.designer.core.ui.views.properties;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.runtime.image.ImageUtils.ICON_SIZE;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.EComponentType;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IConnectionCategory;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.services.IGenericWizardService;
import org.talend.core.ui.images.CoreImageProvider;
import org.talend.core.ui.properties.tab.HorizontalTabFactory;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.core.ui.properties.tab.TalendPropertyTabDescriptor;
import org.talend.core.views.IComponentSettingsView;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.AbstractBasicComponent;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.connections.ConnectionLabel;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.notes.Note;
import org.talend.designer.core.ui.editor.properties.connections.MainConnectionComposite;
import org.talend.designer.core.ui.editor.properties.notes.AbstractNotePropertyComposite;
import org.talend.designer.core.ui.editor.properties.notes.BasicNotePropertyComposite;
import org.talend.designer.core.ui.editor.subjobcontainer.SubjobContainer;
import org.talend.designer.core.ui.preferences.TalendDesignerPrefConstants;
import org.talend.designer.core.ui.views.properties.composites.MissingSettingsMultiThreadDynamicComposite;
import org.talend.designer.core.ui.views.subjob.SubjobBasicComposite;
import org.talend.designer.core.utils.UnifiedComponentUtil;
import org.talend.repository.RepositoryPlugin;

/**
 * nrousseau class global comment. Detailled comment <br/>
 *
 */
public class ComponentSettingsView extends ViewPart implements IComponentSettingsView, PropertyChangeListener {

    private static final String PARENT = "parent"; //$NON-NLS-1$

    private static final String CATEGORY = "category"; //$NON-NLS-1$

    private static final String DEFAULT = "default"; //$NON-NLS-1$

    private static final String TABLEVIEW = "table view"; //$NON-NLS-1$

    public static final String ID = "org.talend.designer.core.ui.views.properties.ComponentSettingsView"; //$NON-NLS-1$

    private HorizontalTabFactory tabFactory = null;

    private TalendPropertyTabDescriptor currentSelectedTab;

    private Element element;

    private IDynamicProperty dc = null;

    private boolean cleaned;

    private boolean selectedPrimary;

    private Map<String, Composite> parentMap = null;

    private Map<String, EComponentCategory> categoryMap = null;

    private Composite parent;

    private SelectionListener compactListener;

    private SelectionListener tableButtonListener;

    /**
     * Getter for parentMap.
     *
     * @return the parentMap
     */
    public Map<String, Composite> getParentMap() {
        return this.parentMap;
    }

    /**
     * Getter for categoryMap.
     *
     * @return the categoryMap
     */
    public Map<String, EComponentCategory> getCategoryMap() {
        return this.categoryMap;
    }

    /**
     * nrousseau ComponentSettings constructor comment.
     */
    public ComponentSettingsView() {
        tabFactory = new HorizontalTabFactory();
        parentMap = new HashMap<String, Composite>();
        categoryMap = new HashMap<String, EComponentCategory>();
        ProxyRepositoryFactory.getInstance().addPropertyChangeListener(this);
    }

    /**
     * DOC zwang Comment method "getPreference".
     *
     * @return
     */
    private IPreferenceStore getPreference() {
        // TODO Auto-generated method stub
        return DesignerPlugin.getDefault().getPreferenceStore();
    }

    public Composite getParent() {
        return parent;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        this.parent = parent;
        tabFactory.initComposite(parent, true);
        tabFactory.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                TalendPropertyTabDescriptor descriptor = (TalendPropertyTabDescriptor) selection.getFirstElement();

                if (descriptor == null) {
                    return;
                }

                if (currentSelectedTab != null
                        && (!currentSelectedTab.getData().equals(descriptor.getData()) || currentSelectedTab.getCategory() != descriptor
                                .getCategory())) {
                    for (Control curControl : tabFactory.getTabComposite().getChildren()) {
                        curControl.dispose();
                    }
                }

                if (element == null || !element.equals(descriptor.getData()) || currentSelectedTab == null
                        || currentSelectedTab.getCategory() != descriptor.getCategory() || selectedPrimary) {
                    element = (Element) descriptor.getData();
                    currentSelectedTab = descriptor;

                    if (descriptor.getData() instanceof ConnectionLabel) {
                        createDynamicComposite(tabFactory.getTabComposite(),
                                ((ConnectionLabel) descriptor.getData()).getConnection(), descriptor.getCategory());
                    } else {
                        createDynamicComposite(tabFactory.getTabComposite(), (Element) descriptor.getData(),
                                descriptor.getCategory());
                    }

                    selectedPrimary = false;
                }
            }
        });
    }

    /**
     * yzhang Comment method "createDynamicComposite".
     *
     * @param parent
     * @param element
     * @param category
     */
    private void createDynamicComposite(final Composite parent, Element element, EComponentCategory category) {
        // DynamicComposite dc = null;

        getParentMap().put(ComponentSettingsView.PARENT, parent);
        getCategoryMap().put(ComponentSettingsView.CATEGORY, category);
        if (element instanceof Node) {
            IComponent component = ((Node) element).getComponent();
            IGenericWizardService wizardService = null;
            boolean generic = false;
            if (EComponentType.GENERIC.equals(component.getComponentType())) {
                generic = true;
                if (GlobalServiceRegister.getDefault().isServiceRegistered(IGenericWizardService.class)) {
                    wizardService = (IGenericWizardService) GlobalServiceRegister.getDefault().getService(
                            IGenericWizardService.class);
                }
            }
            tabFactory.getTabbedPropertyComposite().setCompactViewVisible(false);
            if (category == EComponentCategory.BASIC) {
                // getElementMap().put(ComponentSettingsView.ELEMENT, element);
                createButtonListener();
                boolean isCompactView = true;
                if (ComponentSettingsView.TABLEVIEW.equals(getPreference().getString(TalendDesignerPrefConstants.VIEW_OPTIONS))) {
                    isCompactView = false;
                }
                tabFactory.getTabbedPropertyComposite().setCompactViewVisible(true);
                tabFactory.getTabbedPropertyComposite().setCompactView(isCompactView);
                // Generic
                if (generic && wizardService != null) {
                    Composite composite = wizardService.creatDynamicComposite(parent, element, EComponentCategory.BASIC, true);
                    if (composite instanceof MultipleThreadDynamicComposite) {
                        dc = (MultipleThreadDynamicComposite) composite;
                    }
                } else {
                    dc = new MissingSettingsMultiThreadDynamicComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.NO_FOCUS,
                            category, element, isCompactView);
                }
            } else if (category == EComponentCategory.DYNAMICS_SETTINGS) {
                dc = new AdvancedContextComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.NO_FOCUS, element);
            } else if (category == EComponentCategory.SQL_PATTERN) {
                dc = new SQLPatternComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.NO_FOCUS, element);
            } else if (category == EComponentCategory.ADVANCED) {
                // Generic
                if (generic && wizardService != null) {
                    Composite composite = wizardService.creatDynamicComposite(parent, element, EComponentCategory.ADVANCED, true);
                    if (composite instanceof MultipleThreadDynamicComposite) {
                        dc = (MultipleThreadDynamicComposite) composite;
                    }
                } else {
                    dc = new MissingSettingsMultiThreadDynamicComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.NO_FOCUS, category,
                            element, true);
                }
            } else {
                dc = new MultipleThreadDynamicComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.NO_FOCUS, category, element,
                        true);
            }
        } else if (element instanceof Connection) {
            dc = new MainConnectionComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.NO_FOCUS, category, element);
        } else if (element instanceof Note) {

            if (category == EComponentCategory.BASIC) {

                if (parent.getLayout() instanceof FillLayout) {
                    FillLayout layout = (FillLayout) parent.getLayout();
                    layout.type = SWT.VERTICAL;
                    layout.marginHeight = 0;
                    layout.marginWidth = 0;
                    layout.spacing = 0;
                }
                ScrolledComposite scrolled = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
                scrolled.setExpandHorizontal(true);
                scrolled.setExpandVertical(true);

                scrolled.setMinWidth(600);
                scrolled.setMinHeight(400);

                Composite composite = tabFactory.getWidgetFactory().createComposite(scrolled);
                scrolled.setContent(composite);
                composite.setLayout(new FormLayout());
                FormData d = new FormData();
                d.left = new FormAttachment(0, 0);
                d.right = new FormAttachment(100, 0);
                d.top = new FormAttachment(0, 0);
                d.bottom = new FormAttachment(100, 0);
                composite.setLayoutData(d);

                AbstractNotePropertyComposite c1 = new BasicNotePropertyComposite(composite, (Note) element, tabFactory);
                // AbstractNotePropertyComposite c2 = new TextNotePropertyComposite(composite, (Note) element,
                // tabFactory);
                // FormData data = new FormData();
                // data.top = new FormAttachment(c1.getComposite(), 20, SWT.DOWN);
                // data.left = new FormAttachment(0, 0);
                // data.right = new FormAttachment(100, 0);
                // c2.getComposite().setLayoutData(data);
                parent.layout();
            }
        } else if (element instanceof SubjobContainer) {
            if (category == EComponentCategory.BASIC) {
                dc = new SubjobBasicComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.NO_FOCUS, element);
            }
        } else {
            tabFactory.getTabbedPropertyComposite().setCompactViewVisible(false);
            dc = new MultipleThreadDynamicComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.NO_FOCUS, category, element, true);
        }

        if (parent.getChildren().length == 0) {
            if (parent.getLayout() instanceof FillLayout) {
                FillLayout layout = (FillLayout) parent.getLayout();
                layout.type = SWT.VERTICAL;
                layout.marginHeight = 0;
                layout.marginWidth = 0;
                layout.spacing = 0;
            }

            Composite composite = tabFactory.getWidgetFactory().createComposite(parent);

            composite.setLayout(new FormLayout());
            FormData d = new FormData();
            d.left = new FormAttachment(2, 0);
            d.right = new FormAttachment(100, 0);
            d.top = new FormAttachment(5, 0);
            d.bottom = new FormAttachment(100, 0);
            composite.setLayoutData(d);

            Label alertText = new Label(composite, SWT.NONE);
            alertText.setText(Messages.getString("ComponentSettingsView.noAdvancedSetting")); //$NON-NLS-1$
            alertText.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
            parent.layout();
        }
        if (dc != null) {
            dc.refresh();
        }
    }

    /**
     * DOC zwang Comment method "createButtons".
     */
    private void createButtonListener() {
        // TODO Auto-generated method stub
        // tabFactory.getTabbedPropertyComposite().getComposite().setBackground(
        // ImageProvider.getImage(EImage.COMPOSITE_BACKGROUND).getBackground());
        Button compactButton = tabFactory.getTabbedPropertyComposite().getCompactButton();
        if (compactButton != null && !compactButton.isDisposed()) {
            compactButton.setVisible(true);
            if (compactListener == null) {
                compactListener = new SelectionAdapter() {

                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        getPreference().setValue(TalendDesignerPrefConstants.VIEW_OPTIONS, ComponentSettingsView.DEFAULT);
                        if (getDc() != null) {
                            getDc().dispose();
                            Composite composite = getParentMap().get(ComponentSettingsView.PARENT);
                            EComponentCategory category2 = getCategoryMap().get(ComponentSettingsView.CATEGORY);
                            if (composite != null && category2 != null) {
                                createDynamicComposite(composite, element, category2);
                            }
                        }
                    }
                };
                compactButton.addSelectionListener(compactListener);
            }
        }
        Button tableButton = tabFactory.getTabbedPropertyComposite().getTableButton();
        if (tableButton != null && !tableButton.isDisposed()) {
            tableButton.setVisible(true);
            if (tableButtonListener == null) {
                tableButtonListener = new SelectionAdapter() {

                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        getPreference().setValue(TalendDesignerPrefConstants.VIEW_OPTIONS, ComponentSettingsView.TABLEVIEW);
                        if (getDc() != null) {
                            getDc().dispose();
                            Composite composite = getParentMap().get(ComponentSettingsView.PARENT);
                            EComponentCategory category2 = getCategoryMap().get(ComponentSettingsView.CATEGORY);
                            if (composite != null && category2 != null) {
                                createDynamicComposite(composite, element, category2);
                            }
                        }
                    }
                };
                tableButton.addSelectionListener(tableButtonListener);
            }
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
        this.parent.setFocus();
    }

    public boolean isCleaned() {
        return this.cleaned;
    }

    @Override
    public void cleanDisplay() {
        tabFactory.setInput(null);
        tabFactory.setTitle(null, null);
        tabFactory.getTabbedPropertyComposite().setCompactViewVisible(false);
        if (tabFactory.getTabComposite() != null) {
            for (Control curControl : tabFactory.getTabComposite().getChildren()) {
                curControl.dispose();
            }
        }
        this.element = null;
        cleaned = true;
        selectedPrimary = true;
        if (currentSelectedTab != null) {
            currentSelectedTab.setData(null);
            currentSelectedTab = null;
        }
    }

    @Override
    public void setElement(Element elem) {
        if (currentSelectedTab != null && currentSelectedTab.getData().equals(elem) && !cleaned) {
            updatePropertiesViewerTitle();

            return;
        }

        EComponentCategory[] categories = getCategories(elem);
        final List<TalendPropertyTabDescriptor> descriptors = new ArrayList<TalendPropertyTabDescriptor>();
        for (EComponentCategory category : categories) {
            TalendPropertyTabDescriptor d = new TalendPropertyTabDescriptor(category);
            d.setData(elem);
            descriptors.add(d);
            // if (category.hadSubCategories()) {
            // for (EComponentCategory subCategory : category.getSubCategories()) {
            // TalendPropertyTabDescriptor subc = new TalendPropertyTabDescriptor(subCategory);
            // subc.setElement(elem);
            // d.addSubItem(subc);
            // }
            // }
        }

        tabFactory.setInput(descriptors);
        setPropertiesViewerTitle(elem);
        cleaned = false;
        tabFactory.setSelection(new IStructuredSelection() {

            @Override
            public Object getFirstElement() {
                return null;
            }

            @Override
            public Iterator iterator() {
                return null;
            }

            @Override
            public int size() {
                return 0;
            }

            @Override
            public Object[] toArray() {
                return null;
            }

            @Override
            public List toList() {
                List<TalendPropertyTabDescriptor> d = new ArrayList<TalendPropertyTabDescriptor>();

                if (descriptors.size() > 0) {
                    if (currentSelectedTab != null) {
                        for (TalendPropertyTabDescriptor ds : descriptors) {
                            if (ds.getCategory() == currentSelectedTab.getCategory()) {
                                d.add(ds);
                                return d;
                            }
                        }
                    }
                    d.add(descriptors.get(0));
                }
                return d;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

        });
    }

    public void selectTab(final EComponentCategory category) {
        if (tabFactory.getSelection().getCategory().equals(category)) {
            return;
        }

        List<TalendPropertyTabDescriptor> allTabs = tabFactory.getInput();
        final List<TalendPropertyTabDescriptor> selection = new ArrayList<TalendPropertyTabDescriptor>();
        for (TalendPropertyTabDescriptor talendPropertyTabDescriptor : allTabs) {
            if (talendPropertyTabDescriptor.getCategory().equals(category)) {
                selection.add(talendPropertyTabDescriptor);
            }
        }
        tabFactory.setSelection(new StructuredSelection() {

            @Override
            public List toList() {
                return selection;
            }
        });
    }

    public void updatePropertiesViewerTitle() {
        if (this.element != null) {
            setPropertiesViewerTitle(this.element);
        }
    }

    /**
     * yzhang Comment method "setPropertiesViewerTitle".
     *
     * @param elem
     */
    private void setPropertiesViewerTitle(Element elem) {
        String label = null;
        Image image = null;
        if (elem instanceof Node) {
            Node node = (Node) elem;
            label = node.getLabel();

            String uniqueName = node.getUniqueName();
            if (!label.equals(uniqueName)) {
                label = label + "(" + uniqueName + ")"; //$NON-NLS-1$ //$NON-NLS-2$
            }
            if (node.getComponent() != node.getDelegateComponent()) {
                String componentName = node.getComponent().getName();
                if (StringUtils.isNoneBlank(node.getUnifiedComponentDisplayName())) {
                    componentName = node.getUnifiedComponentDisplayName();
                }
                String dispalyName = UnifiedComponentUtil.getUnifiedComponentDisplayName(node.getDelegateComponent(),
                        componentName);
                if (dispalyName != null) {
                    label = label + "(" + dispalyName + ")";
                }
            }
            image = CoreImageProvider.getComponentIcon(node.getComponent(), ICON_SIZE.ICON_24);
        } else if (elem instanceof Connection) {
            label = ((Connection) elem).getElementName();
            image = ImageProvider.getImage(EImage.RIGHT_ICON);
        } else if (elem instanceof Note) {
            label = Messages.getString("ComponentSettingsView.note"); //$NON-NLS-1$
            image = ImageProvider.getImage(EImage.PASTE_ICON);
        } else if (elem instanceof SubjobContainer) {
            label = Messages.getString("ComponentSettingsView.subjob2"); //$NON-NLS-1$
            image = ImageProvider.getImage(EImage.PASTE_ICON);
        } else if (elem instanceof ConnectionLabel) {
            label = ((ConnectionLabel) elem).getConnection().getElementName();
            image = ImageProvider.getImage(EImage.RIGHT_ICON);
        }
        tabFactory.setTitle(label, image);
        super.setTitleToolTip(label);
    }

    private boolean isMrGroupLine(Element elem) {
        if (elem instanceof Connection) {
            Connection con = (Connection) elem;
            if (con.getSource() instanceof Node && con.getTarget() instanceof Node) {
                Node nodeSource = (Node) con.getSource();
                Node nodeTarget = (Node) con.getTarget();
                if (nodeSource.getMrGroupId() != null && !nodeSource.getMrGroupId().equals(nodeTarget.getMrGroupId())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * yzhang Comment method "getCategories".
     *
     * @param elem
     * @return
     */
    private EComponentCategory[] getCategories(Element elem) {
        if (elem instanceof Connection) {

            EComponentCategory[] categories = EElementType.CONNECTION.getCategories();
            if (PluginChecker.isTeamEdition()) {

                Object propertyValue = elem.getPropertyValue(Connection.LINESTYLE_PROP);
                if (propertyValue instanceof EConnectionType
                        && ((EConnectionType) propertyValue).hasConnectionCategory(IConnectionCategory.FLOW)) {
                    // if (((Connection) elem).checkTraceShowEnable()) {
                    final List<EComponentCategory> list = new ArrayList<EComponentCategory>(Arrays.asList(categories));
                    boolean isMRProcess = false;
                    IProcess process = ((Connection) elem).getSource().getProcess();
                    if (process instanceof IProcess2) {
                        IProcess2 process2 = (IProcess2) process;
                        if (ComponentCategory.CATEGORY_4_MAPREDUCE.getName().equals(process2.getComponentsType())) {
                            isMRProcess = true;
                        }
                    }
                    boolean isStormProcess = false;
                    process = ((Connection) elem).getSource().getProcess();
                    if (process instanceof IProcess2) {
                        IProcess2 process2 = (IProcess2) process;
                        if (ComponentCategory.CATEGORY_4_STORM.getName().equals(process2.getComponentsType())) {
                            isStormProcess = true;
                        }
                    }
                    // mrjob and stormjob not add breakpoint
                    if (!isStormProcess && !isMRProcess) {
                        list.add(EComponentCategory.BREAKPOINT);
                    }

                    if (elem.getElementParameter(EParameterName.DEPARTITIONER.getName()) != null
                            || elem.getElementParameter(EParameterName.PARTITIONER.getName()) != null
                            || elem.getElementParameter(EParameterName.REPARTITIONER.getName()) != null) {
                        list.add(EComponentCategory.PARALLELIZATION);
                    }
                    // if it mr group line then add errorRecovery
                    if (isMRProcess && isMrGroupLine(elem)) {
                        list.add(EComponentCategory.RESUMING);
                    }
                    return list.toArray(new EComponentCategory[0]);
                    // }
                } else if (propertyValue.equals(EConnectionType.ON_COMPONENT_OK)
                        || propertyValue.equals(EConnectionType.ON_COMPONENT_ERROR)
                        || propertyValue.equals(EConnectionType.RUN_IF) || propertyValue.equals(EConnectionType.ON_SUBJOB_OK)
                        || propertyValue.equals(EConnectionType.ON_SUBJOB_ERROR)
                        || propertyValue.equals(EConnectionType.ROUTE_WHEN) || propertyValue.equals(EConnectionType.ROUTE_CATCH)
                        || propertyValue.equals(EConnectionType.STARTS)) {

                    boolean isMRProcess = false;
                    IProcess process = ((Connection) elem).getSource().getProcess();
                    if (process instanceof IProcess2) {
                        IProcess2 process2 = (IProcess2) process;
                        if (ComponentCategory.CATEGORY_4_MAPREDUCE.getName().equals(process2.getComponentsType())) {
                            isMRProcess = true;
                        }
                    }
                    boolean isStormProcess = false;
                    process = ((Connection) elem).getSource().getProcess();
                    if (process instanceof IProcess2) {
                        IProcess2 process2 = (IProcess2) process;
                        if (ComponentCategory.CATEGORY_4_STORM.getName().equals(process2.getComponentsType())) {
                            isStormProcess = true;
                        }
                    }
                    int length = categories.length;
                    EComponentCategory[] newCategories;
                    boolean isNormalJobNeedRecovery = (!isMRProcess && !isStormProcess && !isAvoidRecoveryByConditions(elem));
                    boolean isMrStormJobNeedRecovery = isMRProcess || isStormProcess;
                    if (isNeedRecoveryCategory(propertyValue, isNormalJobNeedRecovery, isMrStormJobNeedRecovery)) {
                        newCategories = new EComponentCategory[length + 1];
                        for (int i = 0; i < length; i++) {
                            newCategories[i] = categories[i];
                        }
                        EComponentCategory resuming = EComponentCategory.RESUMING;
                        newCategories[length] = resuming;
                    } else {
                        newCategories = new EComponentCategory[length];
                        for (int i = 0; i < length; i++) {
                            newCategories[i] = categories[i];
                        }
                    }
                    return newCategories;
                }

            }
            return categories;
        } else if (elem instanceof Node) {
            // if (isAdvancedType(elem)) {
            if (((Node) elem).isELTComponent()) {
                if (!((Node) elem).getComponent().getName().endsWith("Output") //$NON-NLS-1$
                        && !((Node) elem).getComponent().getName().endsWith("Input") //$NON-NLS-1$
                        && !((Node) elem).getComponent().getName().endsWith("Map") //$NON-NLS-1$
                        && !((Node) elem).getComponent().getName().endsWith("TableList") //$NON-NLS-1$
                        && !((Node) elem).getComponent().getName().endsWith("ColumnList")) { //$NON-NLS-1$
                    return EElementType.ELT_NODE.getCategories();
                }
            }
            EComponentCategory[] categories = EElementType.ADVANCED_NODE.getCategories();
            // add for bug TDI-8476
            if (((Node) elem).getComponent() != null) {
                String paletteType = ((Node) elem).getComponent().getPaletteType();
                if (ComponentCategory.CATEGORY_4_CAMEL.getName().equals(paletteType)) {
                    categories = EElementType.NODE.getCategories();
                }
            }

            if (PluginChecker.isValidationrulesPluginLoaded() && isSupportValidationRuleNode((Node) elem)) { // show
                EComponentCategory[] newCategories = new EComponentCategory[categories.length + 1];
                System.arraycopy(categories, 0, newCategories, 0, categories.length);
                newCategories[categories.length] = EComponentCategory.VALIDATION_RULES;
                return newCategories;
            }
            return categories;
        } else if (elem instanceof Note) {
            return EElementType.NOTE.getCategories();
        } else if (elem instanceof SubjobContainer) {
            return EElementType.SUBJOB.getCategories();
        } else if (elem instanceof ConnectionLabel) {
            return getCategories(((ConnectionLabel) elem).getConnection());
        }
        return null;
    }

    /**
     * Need to add recovery category or not by some conditions for current connection
     *
     * @param elem
     */
    private boolean isAvoidRecoveryByConditions(IElement elem) {
        if (elem instanceof IConnection) {
            INode source = ((IConnection) elem).getSource();
            List<? extends IConnection> conns = source.getIncomingConnections();
            for (IConnection conn : conns) {
                Object propertyValue = conn.getPropertyValue(Connection.LINESTYLE_PROP);
                if (propertyValue.equals(EConnectionType.ON_COMPONENT_OK)
                        || propertyValue.equals(EConnectionType.ON_COMPONENT_ERROR)
                        || propertyValue.equals(EConnectionType.RUN_IF) || propertyValue.equals(EConnectionType.ON_SUBJOB_ERROR)
                        || propertyValue.equals(EConnectionType.PARALLELIZE)) {
                    return true;
                } else {
                    if (isAvoidRecoveryByConditions(conn)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isNeedRecoveryCategory(Object currentConnPropertyValue, boolean isNormalJob, boolean isMrStormJob) {
        // subjob_ok and subjob_error both need the recovery category
        boolean isSubJobConn = currentConnPropertyValue.equals(EConnectionType.ON_SUBJOB_OK);
        return (isSubJobConn && isNormalJob) || isMrStormJob;
    }

    /**
     * DOC ycbai Comment method "isSupportValidationRuleNode".
     *
     * @param node
     * @return
     */
    private boolean isSupportValidationRuleNode(Node node) {
        boolean hasFlow = false;
        if (!ComponentCategory.CATEGORY_4_DI.getName().equals(node.getProcess().getComponentsType())) {
            return false;
        }
        if (node.getComponent() != null && node.getComponent() instanceof AbstractBasicComponent) {
            AbstractBasicComponent component = (AbstractBasicComponent) node.getComponent();
            if (component.useLookup() || component.useMerge() || !component.useSchema(node)) {
                return false;
            }
            if (component.useFlow()) {
                hasFlow = true;
            }
        }

        return hasFlow;
    }

    @Override
    public Element getElement() {
        return element;
    }

    /**
     * yzhang Comment method "isAdvancedType".
     *
     * @param elem
     * @return
     */
    private boolean isAdvancedType(Element elem) {
        for (IElementParameter param : elem.getElementParameters()) {
            if (param.getCategory().equals(EComponentCategory.ADVANCED)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Getter for dc.
     *
     * @return the dc
     */
    public Composite getDc() {
        return (Composite) this.dc;
    }

    // /**
    // * Getter for elementMap.
    // *
    // * @return the elementMap
    // */
    // public Map<String, Element> getElementMap() {
    // return this.elementMap;
    // }
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("view_refresh")) { //$NON-NLS-1$
            RepositoryPlugin.getDefault().getDesignerCoreService().switchToCurComponentSettingsView();
        }
    }

    @Override
    public void dispose() {
        ProxyRepositoryFactory.getInstance().removePropertyChangeListener(this);
        super.dispose();
    }
}
