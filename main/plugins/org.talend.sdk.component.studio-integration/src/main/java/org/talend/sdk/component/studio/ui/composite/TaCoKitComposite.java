/**
 * Copyright (C) 2006-2021 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.talend.sdk.component.studio.ui.composite;

import static java.util.stream.Stream.of;
import static org.talend.sdk.component.studio.model.parameter.SchemaElementParameter.guessButtonName;

import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.bidimap.DualHashBidiMap;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IElementParameter;
import org.talend.designer.core.model.FakeElement;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController;
import org.talend.designer.core.ui.views.properties.composites.MissingSettingsMultiThreadDynamicComposite;
import org.talend.sdk.component.studio.model.parameter.Layout;
import org.talend.sdk.component.studio.model.parameter.LayoutParameter;
import org.talend.sdk.component.studio.model.parameter.Level;
import org.talend.sdk.component.studio.model.parameter.TaCoKitElementParameter;
import org.talend.sdk.component.studio.ui.composite.problemmanager.IProblemManager;

/**
 * Registers PropertyChangeListener for each IElementParameter during instantiation
 * PropertyChangeListener refreshes layout after each IElementParameter value update
 */
public class TaCoKitComposite extends MissingSettingsMultiThreadDynamicComposite {

    /**
     * Indent between component options on properties form
     */
    private static final int OPTIONS_INDENT = Platform.getOS().equals(Platform.OS_LINUX) ? 0 : 2;

    private List<? extends IElementParameter> parameters;

    private final IProblemManager problemManager;

    private PropertyChangeListener redrawListener = evt -> {
        if (!"show".equals(evt.getPropertyName())) {
            return;
        }
        refresh();
    };

    public TaCoKitComposite(final Composite parentComposite, final int styles, final EComponentCategory section,
            final Element element, final boolean isCompactView, final IProblemManager problemManager) {
        super(parentComposite, styles, section, element, isCompactView);
        this.problemManager = problemManager;
        registProblemManager();
        postInit();
    }

    TaCoKitComposite(final Composite parentComposite, final int styles, final EComponentCategory section, final Element element,
            final boolean isCompactView, final Color backgroundColor, final IProblemManager problemManager) {
        super(parentComposite, styles, section, element, isCompactView, backgroundColor);
        this.problemManager = problemManager;
        registProblemManager();
        postInit();
    }

    private void registProblemManager() {
        if (this.problemManager != null) {
            this.problemManager.registTckComposite(this);
        }
        elem.getElementParameters().stream().filter(Objects::nonNull).filter(TaCoKitElementParameter.class::isInstance)
                .map(TaCoKitElementParameter.class::cast).forEach(p -> {
                    /**
                     * Regist validators, so that problems from validators can be shown using problem manager
                     */
                    p.getRegistValidatorCallback().ifPresent(r -> {
                        try {
                            r.call();
                        } catch (Exception e) {
                            ExceptionHandler.process(e);
                        }
                    });
                    p.setProblemManager(problemManager);
                });
    }

    private void unregistProblemManager() {
        if (this.problemManager != null) {
            this.problemManager.unregistTckComposite(this);
        }
        elem.getElementParameters().stream().filter(Objects::nonNull).filter(TaCoKitElementParameter.class::isInstance)
                .map(TaCoKitElementParameter.class::cast).forEach(p -> p.setProblemManager(null));
    }

    protected void postInit() {
        elem.getElementParameters().stream()
                .filter(Objects::nonNull)
                .filter(TaCoKitElementParameter.class::isInstance)
                .map(TaCoKitElementParameter.class::cast)
                .forEach(p -> p.registerRedrawListener("show", redrawListener));
    }

    protected void preDispose() {
        elem.getElementParameters().stream()
                .filter(Objects::nonNull)
                .filter(TaCoKitElementParameter.class::isInstance)
                .map(TaCoKitElementParameter.class::cast)
                .forEach(p -> p.unregisterRedrawListener("show", redrawListener));
    }

    @Override
    public synchronized void dispose() {
        unregistProblemManager();
        preDispose();
        super.dispose();
    }

    @Override
    public void refresh() {
        if (elem instanceof FakeElement) { // sync exec
            DisplayUtils.getDisplay().syncExec(this::operationInThread);
        } else { // async exec
            super.refresh();
        }
    }

    public PropertyChangeListener getRedrawListener() {
        return redrawListener;
    }

    /**
     * Specifies minimal height of current UI element
     *
     * @return minimal height
     */
    @Override
    public int getMinHeight() {
        if (minHeight < 200) {
            return 200;
        } else if (minHeight > 700) {
            return 700;
        }
        return minHeight;
    }

    /**
     * Initialize all components for the defined section for this node.
     * Note, the method was copied from MultipleThreadDynamicComposite
     *
     * @param forceRedraw  defines whether to force redraw or not
     * @param reInitialize defines whether Composite is re-initialized. If yes, then children are disposed
     * @param height       not used, but it is here, because the method is overridden
     */
    @Override
    protected synchronized void placeComponents(final boolean forceRedraw, final boolean reInitialize,
            final int height) {
        // achen modifed to fix feature 0005991 if composite.isDisposed return
       if (elem == null || composite.isDisposed()) {
            return;
        }
        if (!forceRedraw) {
            final boolean needRedraw = isNeedRedraw();
            if (!needRedraw) {
                return;
            }
        }
        if (reInitialize) {
            if (currentComponent != null) {
                disposeChildren();
            }
        }
        hashCurControls = new DualHashBidiMap();
        parameters = elem.getElementParametersWithChildrens();
        generator.initController(this);
        final Composite previousComposite = addCommonWidgets();
        final Optional<Layout> layout = getFormLayout();
        layout.ifPresent(l -> fillComposite(composite, l, previousComposite));
        resizeScrolledComposite();
    }

    /**
     * Adds common widgets on specified {@code parent} Composite.
     * These widgets will shown in the top of parent Composite.
     * The method may be overridden.
     *
     * @return last Composite added
     */
    protected Composite addCommonWidgets() {
        final Composite propertyComposite = addPropertyType(composite);
        final Composite schemaComposite = addSchemas(composite, propertyComposite);
        final Composite lastComposite = addStatCatcher(schemaComposite);
        return lastComposite;
    }

    protected Composite addPropertyType(final Composite parent) {
        final Composite propertyComposite = new Composite(parent, SWT.NONE);
        propertyComposite.setBackground(parent.getBackground());
        propertyComposite.setLayout(new FormLayout());
        propertyComposite.setLayoutData(levelLayoutData(getMessagesComp()));
        final IElementParameter propertyType = elem.getElementParameter("PROPERTY");
        addWidgetIfActive(propertyComposite, propertyType);
        return propertyComposite;
    }

    /**
     * Adds activated schemas (show = true), which are not present on layout
     *
     * @param parent   Composite on which schema will be located
     * @param previous Composite which is located above this schema. Schema will be attached to the bottom of prev
     *                 Composite
     * @return Schema Composite
     */
    protected Composite addSchemas(final Composite parent, final Composite previous) {
        Composite previousComposite = previous;
        final List<IElementParameter> activeSchemas = parameters
                .stream()
                .filter(p -> p.getFieldType() == EParameterFieldType.SCHEMA_TYPE)
                .filter(this::doShow)
                .filter(this::isNotPresentOnLayout)
                .collect(Collectors.toList());
        for (final IElementParameter schema : activeSchemas) {
            final Composite schemaComposite = new Composite(parent, SWT.NONE);
            schemaComposite.setBackground(parent.getBackground());
            schemaComposite.setLayout(new FormLayout());
            schemaComposite.setLayoutData(levelLayoutData(previousComposite));
            previousComposite = schemaComposite;
            addSchemaWidget(schemaComposite, schema);
        }
        return previousComposite;
    }

    protected Composite addStatCatcher(final Composite parent) {
        final IElementParameter parameter = elem.getElementParameter(EParameterName.TSTATCATCHER_STATS.getName());
        if (doShow(parameter)) {
            addWidget(parent, parameter, null);
        }
        return parent;
    }

    private boolean isNotPresentOnLayout(final IElementParameter schema) {
        final Optional<Layout> rootLayout = getFormLayout();
        if (rootLayout.isPresent()) {
            final String path = schema.getName();
            return toStream(rootLayout.get()).noneMatch(l -> path.equals(l.getPath()));
        } else {
            return true;
        }
    }

    private Stream<Layout> toStream(final Layout layout) {
        return Stream.concat(of(layout),
                layout.getLevels().stream().flatMap(l -> l.getColumns().stream()).flatMap(this::toStream));
    }

    private Optional<Layout> getFormLayout() {
        final LayoutParameter layoutParameter =
                (LayoutParameter) elem.getElementParameter(LayoutParameter.name(section));
        if (layoutParameter == null) {
            return Optional.empty();
        } else {
            return Optional.of(layoutParameter.getLayout());
        }
    }

    /**
     * Fills composite according specified layout
     *
     * @param composite composite to fill
     * @param layout    composite layout
     */
    private void fillComposite(final Composite composite, final Layout layout, final Composite previous) {
        if (layout.isLeaf()) {
            final String path = layout.getPath();
            final IElementParameter current = elem.getElementParameter(path);
            addWidgetIfActive(composite, current);
        } else {
            Composite previousLevel = previous;
            for (final Level level : layout.getLevels()) {
                final Composite levelComposite = new Composite(composite, SWT.NONE);
                levelComposite.setBackground(composite.getBackground());
                levelComposite.setLayout(new FormLayout());
                levelComposite.setLayoutData(levelLayoutData(previousLevel));
                previousLevel = levelComposite;

                final int columnSize = level.getColumns().size();
                for (int i = 0; i < columnSize; i++) {
                    final Layout column = level.getColumns().get(i);
                    final Composite columnComposite = new Composite(levelComposite, SWT.NONE);
                    columnComposite.setLayout(new FormLayout());
                    columnComposite.setBackground(levelComposite.getBackground());
                    final FormData columnLayoutData = new FormData();
                    columnLayoutData.top = new FormAttachment(0, 0);
                    columnLayoutData.left = new FormAttachment((100 / columnSize) * i, 0);
                    columnLayoutData.right = new FormAttachment((100 / columnSize) * (i + 1), 0);
                    columnLayoutData.bottom = new FormAttachment(100, 0);
                    columnComposite.setLayoutData(columnLayoutData);
                    fillComposite(columnComposite, column, null);
                }
            }
        }
    }

    /**
     * Checks whether IElementParameter is active and creates Control for it, if it is.
     * Parameter is active when:
     * <ol>
     * <li>it is not null</li>
     * <li>its category is the same for which Composite is building</li>
     * <li>it is not TECHNICAL parameter</li>
     * <li>its field show=true</li>
     * </ol>
     *
     * @param parent    Composite on which widget will be added
     * @param parameter ElementParameter(Model) associated with widget
     */
    protected void addWidgetIfActive(final Composite parent, final IElementParameter parameter) {
        if (doShow(parameter)) {
            if (EParameterFieldType.SCHEMA_TYPE.equals(parameter.getFieldType())) {
                addSchemaWidget(parent, parameter);
            } else {
                addWidget(parent, parameter, null);
            }
        }
    }

    protected Control addWidget(final Composite parent, final IElementParameter parameter, final Control previous) {
        final AbstractElementPropertySectionController controller =
                generator.getController(parameter.getFieldType(), this);
        return controller.createControl(parent, parameter, 1, 1, OPTIONS_INDENT, previous);
    }

    /**
     * Creates schema and guess schema button widgets
     *
     * @param schemaComposite parent Composite
     * @param schema Schema ElementParameter
     */
    private void addSchemaWidget(final Composite schemaComposite, final IElementParameter schema) {
        final Control schemaControl = addWidget(schemaComposite, schema, null);
        final String schemaName = schema.getName();
        final IElementParameter guessSchema = elem.getElementParameter(guessButtonName(schemaName));
        if (guessSchema != null) {
            addWidget(schemaComposite, guessSchema, schemaControl);
        }
    }

    private FormData levelLayoutData(final Composite previousLevel) {
        final FormData layoutData = new FormData();
        if (previousLevel == null) {
            layoutData.top = new FormAttachment(0, 0);
        } else {
            layoutData.top = new FormAttachment(previousLevel, 0);
        }
        layoutData.left = new FormAttachment(0, 0);
        layoutData.right = new FormAttachment(100, 0);
        return layoutData;
    }

    protected boolean doShow(final IElementParameter parameter) {
        return parameter != null && parameter.getCategory() == section
                && parameter.getFieldType() != EParameterFieldType.TECHNICAL && parameter.isShow(parameters);
    }
}
