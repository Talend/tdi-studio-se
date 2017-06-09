package org.talend.designer.core.assist;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IComponentsFactory;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.IConnectionCategory;
import org.talend.core.model.process.IProcess2;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.model.components.DummyComponent;
import org.talend.designer.core.model.process.ConnectionManager;
import org.talend.designer.core.model.utils.emf.component.CONNECTORType;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.editor.AbstractTalendEditor;
import org.talend.designer.core.ui.preferences.TalendDesignerPrefConstants;

public class TalendEditorComponentCreationUtil {

    private static final String COMPONENT_CREATION_ASSIST_LISTENER = "COMPONENT_CREATION_ASSIST_LISTENER";

    public static void addComponentCreationAssist(AbstractTalendEditor talendEditor) {

        final GraphicalViewer graphicalViewer = (GraphicalViewer) talendEditor.getAdapter(GraphicalViewer.class);
        final CommandStack commandStack = (CommandStack) talendEditor.getAdapter(CommandStack.class);
        final String categoryName = talendEditor.getComponenentsHandler().extractComponentsCategory().getName();
        final IProcess2 process = talendEditor.getProcess();

        KeyListener listener = new KeyListener() {

            @Override
            public void keyReleased(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (Character.isISOControl(e.character) || Character.isSpaceChar(e.character)) {
                    return;
                }
                TalendEditorComponentCreationAssist assist = new TalendEditorComponentCreationAssist(categoryName,
                        graphicalViewer, commandStack, process);
                assist.showComponentCreationAssist(e.character);
            }
        };
        graphicalViewer.getControl().addKeyListener(listener);
        graphicalViewer.getControl().setData(COMPONENT_CREATION_ASSIST_LISTENER, listener);

    }

    public static void removeComponentCreationAssist(AbstractTalendEditor talendEditor) {
        GraphicalViewer graphicalViewer = (GraphicalViewer) talendEditor.getAdapter(GraphicalViewer.class);
        Object data = graphicalViewer.getControl().getData(COMPONENT_CREATION_ASSIST_LISTENER);
        if (data != null && data instanceof KeyListener) {
            graphicalViewer.getControl().removeKeyListener((KeyListener) data);
            graphicalViewer.getControl().update();
        }

    }

    public static void removeAssistListenerOnOpenedEditors() {
        IEditorReference[] editorReferences = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                .getEditorReferences();
        for (IEditorReference er : editorReferences) {
            if (er == null) {
                continue;
            }
            IEditorPart editor = er.getEditor(true);
            if (editor == null || !(editor instanceof AbstractMultiPageTalendEditor)) {
                continue;
            }
            AbstractTalendEditor talendEditor = ((AbstractMultiPageTalendEditor) editor).getTalendEditor();
            removeComponentCreationAssist(talendEditor);
        }
    }

    public static void registerAssistListenerFromOpenedEditors() {
        IEditorReference[] editorReferences = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                .getEditorReferences();
        for (IEditorReference er : editorReferences) {
            if (er == null) {
                continue;
            }
            IEditorPart editor = er.getEditor(true);
            if (editor == null || !(editor instanceof AbstractMultiPageTalendEditor)) {
                continue;
            }
            AbstractTalendEditor talendEditor = ((AbstractMultiPageTalendEditor) editor).getTalendEditor();
            addComponentCreationAssist(talendEditor);
        }
    }

    public static void updateAssistListener() {
        boolean isEnable = DesignerPlugin.getDefault().getPreferenceStore()
                .getBoolean(TalendDesignerPrefConstants.COMPONENT_ASSIST);
        if (isEnable) {
            registerAssistListenerFromOpenedEditors();
        } else {
            removeAssistListenerOnOpenedEditors();
        }
    }

    public static void updateAssistListener(AbstractTalendEditor talendEditor) {
        boolean isEnable = DesignerPlugin.getDefault().getPreferenceStore()
                .getBoolean(TalendDesignerPrefConstants.COMPONENT_ASSIST);
        if (isEnable) {
            addComponentCreationAssist(talendEditor);
        } else {
            removeComponentCreationAssist(talendEditor);
        }
    }

    /**
     * read all components belongs to some category (DI, CAMEL etc.) then store it into a map which can be reused
     * 
     * @param categoryName
     * @param entries
     */
    /*
     * TODO this can be improved after refactoring org.talend.core.model.components.IComponentsHandler implementation in
     * each editor
     */
    private static void readComponentsInCategory(String categoryName, Map<String, IComponent> entries) {
        IComponentsFactory componentsFactory = ComponentsFactoryProvider.getInstance();
        Collection<IComponent> allComponents = componentsFactory.readComponents();
        for (IComponent component : allComponents) {
            String compType = component.getPaletteType();
            if (!component.isTechnical() && compType != null && categoryName.equals(compType)) {
                entries.put(component.getName(), component);
            }
        }
        DummyComponent noteComponent = new DummyComponent("Note");
        noteComponent.setIcon16(ImageProvider.getImageDesc(ECoreImage.CODE_ICON));
        noteComponent.setOriginalFamilyName("Misc");
        entries.put("Note", noteComponent);
    }

    private static Map<String, Map<String, IComponent>> entries = new HashMap<String, Map<String, IComponent>>();

    /**
     * get all components belongs to some category
     * 
     * @param categoryName
     * @return
     */
    public static Map<String, IComponent> getComponentsInCategory(String categoryName) {
        Map<String, IComponent> map = entries.get(categoryName);
        if (map == null) {
            map = new HashMap<String, IComponent>();
            entries.put(categoryName, map);
            readComponentsInCategory(categoryName, map);
        }
        return map;
    }

    private static Map<EConnectionType, Map<String, IComponent>> lineTypeEntries = new HashMap<EConnectionType, Map<String, IComponent>>();

    public static Map<String, IComponent> getComponentsInType(String categoryName, EConnectionType type) {
        if (type == null) {
            type = ConnectionManager.getNewConnectionType();
        }
        Map<String, IComponent> lineTypeMap = lineTypeEntries.get(type);
        if (lineTypeMap != null && !lineTypeMap.isEmpty()) {
            return lineTypeMap;
        }
        Map<String, IComponent> map = entries.get(categoryName);
        if (map == null) {
            map = new HashMap<String, IComponent>();
            entries.put(categoryName, map);
            readComponentsInCategory(categoryName, map);
        }

        lineTypeMap = new HashMap<String, IComponent>();
        for (String key : map.keySet()) {
            IComponent component = map.get(key);
            if (isComponentAllowedWithConnectionType(component, type)) {
                lineTypeMap.put(component.getName(), component);
            }
        }
        lineTypeEntries.put(type, lineTypeMap);
        return lineTypeMap;
    }

    public static boolean isComponentAllowedWithConnectionType(IComponent component, EConnectionType lineStyle) {
        String connectorName = lineStyle.getName();

        if (component.getOriginalFamilyName().equals("FileScale")) {
            if (lineStyle.hasConnectionCategory(IConnectionCategory.FLOW) && !connectorName.equals("FSCOMBINE")) { //$NON-NLS-1$
                return false;
            }
        }

        // TDI-25765 : avoid any connection for components not accepting PIG
        if (lineStyle.hasConnectionCategory(IConnectionCategory.FLOW) && "PIGCOMBINE".equals(connectorName)) { //$NON-NLS-1$
            if (!component.getName().startsWith("tPig")) { //$NON-NLS-1$
                return false;
            }
        }
        if (component != null && component.getName().startsWith("tPig")) { //$NON-NLS-1$
            if (lineStyle.hasConnectionCategory(IConnectionCategory.FLOW) && !"PIGCOMBINE".equals(connectorName)) { //$NON-NLS-1$
                return false;
            }
        }

        // TDI-29775 : avoid any connection for components not accepting SPARK
        if (lineStyle.hasConnectionCategory(IConnectionCategory.FLOW) && "SPARKCOMBINE".equals(connectorName)) { //$NON-NLS-1$
            if (!component.getName().startsWith("tSpark")) { //$NON-NLS-1$
                return false;
            }
        }
        if (component != null && component.getName().startsWith("tSpark")) { //$NON-NLS-1$
            if (lineStyle.hasConnectionCategory(IConnectionCategory.FLOW) && !"SPARKCOMBINE".equals(connectorName)) { //$NON-NLS-1$
                return false;
            }
        }

        // if (PluginChecker.isJobLetPluginLoaded()) {
        // IJobletProviderService service = (IJobletProviderService) GlobalServiceRegister.getDefault().getService(
        // IJobletProviderService.class);
        // if (service != null && service.isJobletComponent(target)
        // && !lineStyle.hasConnectionCategory(IConnectionCategory.FLOW)) {
        // List<INodeConnector> inputConnector = service.getFreeTriggerBuiltConnectors(target, lineStyle, true);
        // if (inputConnector.isEmpty()) {
        // return false;
        // }
        // isJoblet = true;
        // }
        // }

        return isTypeAllowed(lineStyle, component.getCONNECTORList());
    }

    public static boolean isTypeAllowed(final EConnectionType connType, EList listConnType) {
        if (listConnType == null) {
            return false;
        }
        EConnectionType testedType;
        if (connType.hasConnectionCategory(IConnectionCategory.FLOW)) {
            testedType = EConnectionType.FLOW_MAIN;
        } else {
            testedType = connType;
        }

        CONNECTORType currentType;
        for (int i = 0; i < listConnType.size(); i++) {
            currentType = (CONNECTORType) listConnType.get(i);
            EConnectionType tempType = EConnectionType.getTypeFromName(currentType.getCTYPE());
            if (tempType == testedType) {
                int maxInput = currentType.getMAXINPUT();
                int minInput = currentType.getMININPUT();
                if (maxInput > 0 || minInput > 0) {
                    return true;
                }
            }
        }
        return false;
    }
}
