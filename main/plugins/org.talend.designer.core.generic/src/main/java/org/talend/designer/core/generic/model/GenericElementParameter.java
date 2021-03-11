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
package org.talend.designer.core.generic.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.avro.Schema;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.commons.ui.swt.dialogs.ErrorDialogWidthDetailArea;
import org.talend.components.api.component.Connector;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.service.ComponentService;
import org.talend.core.model.components.EComponentType;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataToolAvroHelper;
import org.talend.core.model.metadata.MetadataToolHelper;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IGenericElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.model.process.IProcess;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.PresentationItem;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.SchemaProperty;
import org.talend.daikon.properties.runtime.RuntimeContext;
import org.talend.designer.core.generic.constants.IElementParameterEventProperties;
import org.talend.designer.core.generic.constants.IGenericConstants;
import org.talend.designer.core.generic.utils.ComponentsUtils;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.ui.editor.cmd.ChangeMetadataCommand;
import org.talend.designer.core.ui.editor.nodes.Node;

/**
 * created by ycbai on 2015/9/24 Detailled comment
 *
 */
public class GenericElementParameter extends ElementParameter implements IGenericElementParameter {

    private ComponentProperties rootProperties;

    private Form form;

    private Widget widget;

    private ComponentService componentService;

    private List<?> possibleValues;

    private boolean supportContext;

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private boolean isFirstCall;

    private boolean drivedByForm;

    private Boolean askPropagate;

    private InvocationTargetException exceptionResult;

    public GenericElementParameter(IElement element, ComponentProperties rootProperties, Form form, Widget widget,
            ComponentService componentService) {
        super(element);
        this.rootProperties = rootProperties;
        this.form = form;
        this.widget = widget;
        this.componentService = componentService;
        isFirstCall = true;

        NamedThing widgetProperty = widget.getContent();
        setName(widgetProperty.getName());
        setDisplayName(widgetProperty.getDisplayName());
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.removePropertyChangeListener(listener);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.components.ElementParameter#getValue()
     */
    @Override
    public Object getValue() {
        if (getFieldType() == EParameterFieldType.CLOSED_LIST) {
            if (super.getValue() != null) {
                return super.getValue().toString();
            }
        }
        return super.getValue();
    }

    @Override
    public void setValue(Object o) {
        super.setValue(o);
        if (!isFirstCall
                || (widget.getContent() instanceof PropertiesImpl && !Widget.TABLE_WIDGET_TYPE.equals(widget.getWidgetType()))) {
            updateProperty(o);
            /*
             * Prevent Salesforce and Snowflake from performing API calls to retrieve table metadata.
             * Possible values is going to be a marker that shows that method was called from RepositoryChangeMetadataCommand.
             * Otherwise method was called to perform "validate" and "after" operations.
             */
            if ((!"table.tableName".equals(getName()) && !"module.moduleName".equals(getName())) || !possibleValues.isEmpty()) {
	            boolean calledValidate = callValidate();
	            if (calledValidate) {
	                fireValidateStatusEvent();
	            }
	            boolean calledAfter = callAfter();
	            if (calledAfter) {
	                fireValueChangedEvent();
                } else if (exceptionResult != null) {
                    String mainMsg = Messages.getString("DatabaseForm.checkFailure") + " "
                            + Messages.getString("DatabaseForm.checkFailureTip");
                    new ErrorDialogWidthDetailArea(PlatformUI.getWorkbench().getDisplay().getActiveShell(),
                            "org.talend.metadata.management.ui", mainMsg, ExceptionUtils.getFullStackTrace(exceptionResult));
	            }
            }
        }
        isFirstCall = false;
    }

    private void updateProperty(Object newValue) {
        if (getSubProperties() == null) {
            return;
        }

        NamedThing widgetProperty = widget.getContent();
        if (widgetProperty instanceof SchemaProperty) {
            if (newValue instanceof String) {
                ((SchemaProperty) widgetProperty).setValue(new Schema.Parser().parse((String) newValue));
            } else if (newValue instanceof Schema) {
                ((SchemaProperty) widgetProperty).setValue(((Schema) newValue));
            }
        } else if (widgetProperty instanceof Property) {
            Property se = (Property<?>) widgetProperty;
            Object oldValue = se.getStoredValue();
            Object value = newValue;
            List<?> propertyPossibleValues = ((Property<?>) widgetProperty).getPossibleValues();
            if (propertyPossibleValues != null) {
                for (Object possibleValue : propertyPossibleValues) {
                    if (possibleValue != null && possibleValue.toString().equals(newValue)) {
                        value = possibleValue;
                        break;
                    }
                }
            }
            se.setStoredValue(value);
            if (value != null && !value.equals(oldValue)) {
                se = (Property<?>) getSubProperties().getProperty(se.getName());
                if (isDrivedByForm()) {
                    form.setValue(se.getName(), value);
                } else {
                    se.setStoredValue(value);
                }
                fireConnectionPropertyChangedEvent(newValue);
            }
        } else if (widgetProperty instanceof PresentationItem) {
            PresentationItem pi = (PresentationItem) widgetProperty;
            Form formtoShow = pi.getFormtoShow();
            if (formtoShow != null) {
                fireShowDialogEvent(getSubProperties().getForm(formtoShow.getName()));
            }
        } else if (widgetProperty instanceof ComponentProperties && Widget.TABLE_WIDGET_TYPE.equals(widget.getWidgetType())) {
            GenericTableUtils.setTableValues((ComponentProperties) widgetProperty, (List<Map<String, Object>>) newValue, this);
        }
    }

    private boolean hasPropertyChangeListener() {
        return pcs.getPropertyChangeListeners().length != 0;
    }

    private void fireValidateStatusEvent() {
        if (hasPropertyChangeListener() && this.getFieldType() != EParameterFieldType.TEXT
                && this.getFieldType() != EParameterFieldType.TABLE) {
            this.pcs.firePropertyChange(IElementParameterEventProperties.EVENT_VALIDATE_RESULT_UPDATE, null,
                    getSubProperties().getValidationResult());
        }
    }

    private void fireValueChangedEvent() {
        if (hasPropertyChangeListener()) {
            List<Form> forms = getSubProperties().getForms();
            for (Form f : forms) {
                if (f.isRefreshUI()) {
                    this.pcs.firePropertyChange(IElementParameterEventProperties.EVENT_PROPERTY_VALUE_CHANGED, null, null);
                    return;
                }
            }
        }
    }

    private void fireConnectionPropertyChangedEvent(Object newPropertyName) {
        if (IGenericConstants.NAME_PROPERTY.equalsIgnoreCase(getName()) && hasPropertyChangeListener()) {
            this.pcs.firePropertyChange(IElementParameterEventProperties.EVENT_PROPERTY_NAME_CHANGED, null, newPropertyName);
        }
    }

    private void fireShowDialogEvent(Form formToDisplay) {
        if (hasPropertyChangeListener()) {
            this.pcs.firePropertyChange(IElementParameterEventProperties.EVENT_SHOW_DIALOG, null, formToDisplay);
        }
    }

    public boolean callBeforePresent() {
        if (widget.isCallBeforePresent() && hasPropertyChangeListener()) {
            return new ComponentServiceCaller(widget.getContent().getDisplayName(), widget.isLongRunning()) {

                @Override
                protected void doWork() throws Throwable {
                    componentService.beforePropertyPresent(getParameterName(), getSubProperties(), prepareRuntimeContextForTriggerMethod());
                }
            }.call();
        }
        return false;
    }

    public boolean callBeforeActivate() {
        if (widget.isCallBeforeActivate() && hasPropertyChangeListener()) {
            return new ComponentServiceCaller(widget.getContent().getDisplayName(), widget.isLongRunning()) {

                @Override
                protected void doWork() throws Throwable {
                    componentService.beforePropertyActivate(getParameterName(), getSubProperties(), prepareRuntimeContextForTriggerMethod());
                    fireValidateStatusEvent();
                    update();
                }
            }.call();
        }
        return false;
    }

    private void update() {
        NamedThing property = getSubProperties().getProperty(getParameterName());
        if (property != null && property instanceof Property) {
            List<?> values = ((Property) property).getPossibleValues();
            if (values != null) {
                this.setPossibleValues(values);
            }
        }
    }

    private boolean callValidate() {
        if (widget.isCallValidate() && hasPropertyChangeListener()) {
            return new ComponentServiceCaller(widget.getContent().getDisplayName(), widget.isLongRunning()) {

                @Override
                protected void doWork() throws Throwable {
                    componentService.validateProperty(getParameterName(), getSubProperties(), prepareRuntimeContextForTriggerMethod());
                }
            }.call();
        }
        return false;
    }

    private RuntimeContext prepareRuntimeContextForTriggerMethod() {
        //in future, we can pass all studio vars here to the trigger method in tcomp
        RuntimeContext context = new RuntimeContext() {

            @Override
            public Object getData(String key) {
                if("MAPPING_LOCATION".equals(key)) {
                    return System.getProperty("talend.mappings.url");
                }

                return null;
            }

        };

        return context;
    }

    private boolean callAfter() {
        if (widget.isCallAfter()
                && (hasPropertyChangeListener() || Widget.SCHEMA_REFERENCE_WIDGET_TYPE.equals(widget.getWidgetType()))) {
            // schema update must also
            return new ComponentServiceCaller(widget.getContent().getDisplayName(), widget.isLongRunning()) {

                @Override
                protected void doWork() throws Throwable {
                    componentService.afterProperty(getParameterName(), getSubProperties(), prepareRuntimeContextForTriggerMethod());
                    ValidationResult validationResult = getSubProperties().getValidationResult();
                    if (validationResult != null && Result.ERROR == validationResult.getStatus()) {
                        DisplayUtils.getDisplay().asyncExec(new Runnable() {

                            @Override
                            public void run() {
                                fireValidateStatusEvent();
                            }
                        });
                    }
                    updateSchema();
                }
            }.call();
        }
        return false;
    }

    private void updateSchema() {
        IElement element = this.getElement();
        if (element instanceof Node) {
            Node node = (Node) element;
            List<INodeConnector> connectors = node.getConnectorsFromType(EConnectionType.FLOW_MAIN);
            for (INodeConnector connector : connectors) {
                if (connector instanceof GenericNodeConnector) {
                    Connector componentConnector = ((GenericNodeConnector) connector).getComponentConnector();
                    Schema schema = null;
                    schema = getRootProperties().getSchema(componentConnector, ((GenericNodeConnector) connector).isOutput());
                    IMetadataTable mainTable = node.getMetadataFromConnector(connector.getName());
                    if (schema != null && mainTable != null) {
                        MetadataTable metadataTable = MetadataToolAvroHelper.convertFromAvro(schema);
                        IMetadataTable newTable = MetadataToolHelper.convert(metadataTable);
                        if (!mainTable.sameMetadataAs(newTable) || !newTable.sameMetadataAs(mainTable)) {
                            mainTable.setListColumns(newTable.getListColumns());
                            List<IElementParameter> schemaParameters = node
                                    .getElementParametersFromField(EParameterFieldType.SCHEMA_REFERENCE);
                            updateSchemaParameters(schemaParameters, connector.getName(), schema);
                            if (this.askPropagate == null && node.getOutgoingConnections().size() != 0) {
                                boolean hasPropagation = false;
                                for (IConnection connection : node.getOutgoingConnections()) {
                                    if (connector.getName().equals(connection.getConnectorName())) {
                                        if (isSchemaPropagated(connection.getTarget())) {
                                            hasPropagation = true;
                                            break;
                                        }
                                    }
                                }
                                if (hasPropagation) {
                                    Display.getDefault().syncExec(new Runnable() {

                                        @Override
                                        public void run() {
                                            askPropagate = ChangeMetadataCommand.askPropagate();
                                        }
                                    });
                                }
                            }
                            if (this.askPropagate != null && this.askPropagate) {
                                for (IConnection connection : node.getOutgoingConnections()) {
                                    if (connector.getName().equals(connection.getConnectorName())) {
                                        INode target = connection.getTarget();
                                        if (!isSchemaPropagated(target)) {
                                            continue;
                                        }
                                        ChangeMetadataCommand cmd = new ChangeMetadataCommand(target, null, null, newTable, null);
                                        cmd.setPropagate(true);
                                        IProcess process = node.getProcess();
                                        if (process instanceof org.talend.designer.core.ui.editor.process.Process) {
                                            CommandStack commandStack = ((org.talend.designer.core.ui.editor.process.Process) process)
                                                    .getCommandStack();
                                            commandStack.execute(cmd);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            this.askPropagate = null;
        }
    }

    /**
     * Update schema parameters according the <code>currentContext</code>. Here will update UI at the same time.
     *
     * @param schemaParameters
     * @param currentContext
     * @param newSchema
     */
    private void updateSchemaParameters(List<IElementParameter> schemaParameters, String currentContext, Schema newSchema) {
        if (schemaParameters == null || currentContext == null) {
            return;
        }
        for (IElementParameter parameter : schemaParameters) {
            if (parameter != this && currentContext.equals(parameter.getContext())) {
                parameter.setValue(newSchema);
            }
        }
    }

    private boolean isSchemaPropagated(INode node) {
        if (node == null) {
            return false;
        }
        IComponent component = node.getComponent();
        if (component == null) {
            return false;
        }
        // Always consider it is true for the new component.
        if (EComponentType.GENERIC.equals(component.getComponentType())) {
            return true;
        }
        return component.isSchemaAutoPropagated();
    }

    public String getParameterName() {
        String paramName = getName();
        if (paramName.indexOf(IGenericConstants.EXP_SEPARATOR) != -1) {
            paramName = paramName.substring(paramName.lastIndexOf(IGenericConstants.EXP_SEPARATOR) + 1);
        }
        return paramName;
    }

    abstract class ComponentServiceCaller {

        private String title;

        private boolean isLongRuning;

        public ComponentServiceCaller(String title, boolean isLongRuning) {
            this.title = title;
            this.isLongRuning = isLongRuning;
        }

        public boolean call() {
            if (isLongRuning) {
                return new RunWithProgress(title) {

                    @Override
                    protected void toDo() throws Throwable {
                        doWork();
                    };

                }.run();
            } else {
                try {
                    doWork();
                } catch (Throwable e) {
                    ExceptionHandler.process(e);
                    return false;
                }
            }
            return true;
        }

        protected abstract void doWork() throws Throwable;

    }

    abstract class RunWithProgress {

        private String taskName;

        public RunWithProgress(String taskName) {
            this.taskName = taskName;
        }

        public boolean run() {
            final AtomicBoolean result = new AtomicBoolean();
            IRunnableWithProgress runnableWithProgress = new IRunnableWithProgress() {

                @Override
                public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    monitor.beginTask(taskName, IProgressMonitor.UNKNOWN);
                    try {
                        exceptionResult = null;
                        toDo();
                    } catch (Throwable e) {
                        result.set(false);
                        exceptionResult = new InvocationTargetException(e);
                        throw exceptionResult;
                    }
                    result.set(true);
                }
            };
            ProgressMonitorDialog dialog = new ProgressMonitorDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell());
            try {
                dialog.run(true, true, runnableWithProgress);
            } catch (Exception e) {
                result.set(false);
                ExceptionHandler.process(e);
            }
            return result.get();
        }

        protected abstract void toDo() throws Throwable;

    }

    public List<?> getPossibleValues() {
        return this.possibleValues;
    }

    public void setPossibleValues(List<?> possibleValues) {
        this.possibleValues = possibleValues;
    }

    public Widget getWidget() {
        return this.widget;
    }

    private Properties getSubProperties() {
        return ComponentsUtils.getCurrentProperties(getRootProperties(), getName());
    }

    public ComponentProperties getRootProperties() {
        if (this.getElement() instanceof INode) {
            return ((INode) this.getElement()).getComponentProperties();
        }
        return this.rootProperties;
    }

    public boolean isSupportContext() {
        return this.supportContext;
    }

    public void setSupportContext(boolean supportContext) {
        this.supportContext = supportContext;
    }

    public ComponentService getComponentService() {
        return this.componentService;
    }

    public void setComponentService(ComponentService componentService) {
        this.componentService = componentService;
    }

    public boolean isDrivedByForm() {
        return this.drivedByForm;
    }

    public void setDrivedByForm(boolean drivedByForm) {
        this.drivedByForm = drivedByForm;
    }

    public Property getProperty() {
        NamedThing content = widget.getContent();
        if (content instanceof Property) {
            return (Property) content;
        }
        return null;
    }

    public Properties getProperties() {
        NamedThing content = widget.getContent();
        if (content instanceof Properties) {
            return (Properties) content;
        }
        return null;
    }

    @Override
    public boolean isRepositoryValueUsed() {
        Property property = getProperty();
        if (property != null) {
            return property.getTaggedValue(IGenericConstants.REPOSITORY_VALUE) != null;
        }
        Properties properties = getProperties();
        if(properties != null){
            for(NamedThing thing : properties.getProperties()){
                if(thing instanceof Property){
                    return ((Property)thing).getTaggedValue(IGenericConstants.REPOSITORY_VALUE) != null;
                }
            }
        }
        return false;
    }

    @Override
    public void setRepositoryValueUsed(boolean repositoryUsed) {
        Property property = getProperty();
        if (property != null) {
            property.setTaggedValue(IGenericConstants.REPOSITORY_VALUE, repositoryUsed ? property.getName() : null);
        }
        Properties properties = getProperties();
        if(properties != null){
            for(NamedThing thing : properties.getProperties()){
                if(thing instanceof Property){
                    ((Property)thing).setTaggedValue(IGenericConstants.REPOSITORY_VALUE, repositoryUsed ? thing.getName() : null);
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.model.components.ElementParameter#getRepositoryValue()
     */
    @Override
    public String getRepositoryValue() {
        if (isRepositoryValueUsed() && super.getRepositoryValue() == null) {
            super.setRepositoryValue(this.getName());
//            Property property = getProperty();
//            if (property != null) {
//                super.setRepositoryValue((String) property.getTaggedValue(IGenericConstants.REPOSITORY_VALUE));
//            }
//            Properties properties = getProperties();
//            if(properties != null){
//                super.setRepositoryValue(properties.getName());
//            }
        }
        return super.getRepositoryValue();
    }

    @Override
    public void setContextMode(boolean mode) {
        super.setContextMode(mode);
        Property property = getProperty();
        if (property != null) {
            property.setTaggedValue(IGenericConstants.IS_DYNAMIC, mode);
        }
        Properties properties = getProperties();
        if(properties != null){
            for(NamedThing thing : properties.getProperties()){
                if(thing instanceof Property){
                    ((Property)thing).setTaggedValue(IGenericConstants.IS_DYNAMIC, mode);
                }
            }
        }
    }

    @Override
    public void setAskPropagate(Boolean askPropagate) {
        this.askPropagate = askPropagate;
    }

    @Override
    public void setTaggedValue(String key, Object value) {
        super.setTaggedValue(key, value);
        setPropertyTaggedValue(key, value);
    }

    private void setPropertyTaggedValue(String key, Object value) {
        Property property = getProperty();
        if (property != null) {
            property.setTaggedValue(key, value);
        }
    }

    @Override
    public Object getTaggedValue(String key) {
        Object taggedValue = super.getTaggedValue(key);
        if (taggedValue == null) {
            taggedValue = getPropertyTaggedValue(key);
        }
        return taggedValue;
    }

    private Object getPropertyTaggedValue(String key) {
        Property property = getProperty();
        if (property != null) {
            return property.getTaggedValue(key);
        }
        return null;
    }

}
