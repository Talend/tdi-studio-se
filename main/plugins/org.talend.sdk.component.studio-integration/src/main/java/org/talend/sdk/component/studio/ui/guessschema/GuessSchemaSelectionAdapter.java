/**
 * Copyright (C) 2006-2021 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.talend.sdk.component.studio.ui.guessschema;

import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.singletonList;
import static org.talend.sdk.component.studio.model.parameter.OutputSchemaParameter.ADDITIONAL_PARAM_METADATA_ELEMENT;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.exception.ExceptionMessageDialog;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ITDQRuleService;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataColumn;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.metadata.MetadataToolHelper;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.runtime.IAdditionalInfo;
import org.talend.core.ui.metadata.dialog.MetadataDialog;
import org.talend.core.utils.KeywordsValidator;
import org.talend.designer.core.ui.editor.cmd.ChangeMetadataCommand;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.properties.controllers.uidialog.OpenContextChooseComboDialog;
import org.talend.sdk.component.studio.i18n.Messages;
import org.talend.sdk.component.studio.ui.guessschema.TaCoKitGuessSchemaProcess.GuessSchemaResult;
import org.talend.sdk.component.studio.util.TaCoKitUtil;

public class GuessSchemaSelectionAdapter extends SelectionAdapter {

    private static final String CONTEXT_CHOOSE_DIALOG_TITLE = "Choose a context for query :";//$NON-NLS-1$

    private IElement elem;

    private IElementParameter elementParameter;

    private CommandStack commandStack;

    private Composite composite;

    public GuessSchemaSelectionAdapter(final IElement elem, final IElementParameter elementParameter,
            final Composite composite,
            final CommandStack commandStack) {
        this.elem = elem;
        this.elementParameter = elementParameter;
        this.composite = composite;
        this.commandStack = commandStack;

    }

    private IContext selectContext() {
        Shell parentShell = composite.getShell();
        List<IContext> allContexts = Node.class.cast(elementParameter.getElement())
                .getProcess().getContextManager().getListContext();
        OpenContextChooseComboDialog dialog = new OpenContextChooseComboDialog(parentShell, allContexts);
        dialog.create();
        dialog.getShell().setText(CONTEXT_CHOOSE_DIALOG_TITLE);
        IContext selectContext = null;
        // job only have defoult context,or the query isn't context mode
        if (allContexts.size() == 1) {
            selectContext = Node.class.cast(elementParameter.getElement())
                    .getProcess().getContextManager().getDefaultContext();
        } else if (Window.OK == dialog.open()) {
            selectContext = dialog.getSelectedContext();
        }
        return selectContext;
    }

    @Override
    public void widgetSelected(final SelectionEvent event) {
        super.widgetSelected(event);
        final IContext context = selectContext();
        if (context == null) {
            return;
        }

        final GuessSchemaRunnable guessSchema = new GuessSchemaRunnable(context, elementParameter);
        try {
            new ProgressMonitorDialog(this.composite.getShell())
                    .run(true, true, guessSchema); //this block until the guessSchema is done
        } catch (InvocationTargetException | InterruptedException e) {
            Display.getDefault().asyncExec(() -> {
                ExceptionMessageDialog.openError(composite.getShell(),
                        Messages.getString("guessSchema.dialog.error.title"), //$NON-NLS-1$
                        Messages.getString("guessSchema.dialog.error.msg.default"), e); //$NON-NLS-1$
            });

            if (InterruptedException.class.isInstance(e)) {
                Thread.currentThread().interrupt();
            }

            return; // Guess schema failed
        }

        if (guessSchema.isCanceled()) {
            return; // task was canceled
        }

        final GuessSchemaResult schema = guessSchema.getSchema();
        if (schema == null || TaCoKitUtil.isBlank(schema.getResult())) {
            String errorMessage = schema.getError();
            if (!TaCoKitUtil.isBlank(errorMessage)) {
                ExceptionHandler.process(new Exception(errorMessage));
            }
            ExceptionMessageDialog.openInformation(composite.getShell(),
                    Messages.getString("guessSchema.dialog.info.NoSchema.title"), //$NON-NLS-1$
                    Messages.getString("guessSchema.dialog.info.NoSchema.msg")); //$NON-NLS-1$
            return;
        }

        final Node node = Node.class.cast(elementParameter.getElement());
        IMetadataTable newMeta = buildMetadata(schema.getResult());
        if (newMeta == null) {
            Exception causedBy = null;
            String errMessage = schema.getError();
            if (errMessage != null && !errMessage.trim().isEmpty()) {
                causedBy = new Exception(causedBy);
            }
            Exception ex = null;
            if (causedBy != null) {
                ex = new Exception(schema.getResult(), causedBy);
            } else {
                ex = new Exception(schema.getResult());
            }
            ExceptionHandler.process(ex);
            ExceptionMessageDialog.openError(composite.getShell(), Messages.getString("guessSchema.dialog.error.title"), //$NON-NLS-1$
                    Messages.getString("guessSchema.dialog.error.msg.default"), //$NON-NLS-1$
                    ex);
            return;
        }
        MetadataDialog metaDialog = new MetadataDialog(composite.getShell(), newMeta, node, commandStack);
        metaDialog.setText(Messages.getString("guessSchema.dialog.title", node.getLabel())); //$NON-NLS-1$
        if (metaDialog.open() == MetadataDialog.OK) {
            final IMetadataTable outputMetaCopy = metaDialog.getOutputMetaData();
            final IMetadataTable old = node.getMetadataTable(outputMetaCopy.getTableName());
            IElementParameter param = node.getElementParameter(elementParameter.getName());
            if (param instanceof IAdditionalInfo) {
                IElementParameter schemaParam = (IElementParameter) ((IAdditionalInfo) param)
                        .getInfo(ADDITIONAL_PARAM_METADATA_ELEMENT);
                if (schemaParam != null) {
                    param = node.getElementParameter(schemaParam.getName());
                }
            }
            final List<? extends IConnection> incomingConnections = new ArrayList<>(node.getIncomingConnections());
            final List<? extends IConnection> outgoingConnections = new ArrayList<>(node.getOutgoingConnections());
            final List<? extends INodeConnector> connectorsList = new ArrayList<>(node.getListConnector());

            try {
                node.setIncomingConnections(EMPTY_LIST);
                node.setListConnector(singletonList(node.getConnectorFromName(outputMetaCopy.getTableName())));
                node.setOutgoingConnections(node.getOutgoingConnections(outputMetaCopy.getTableName()));
                final ChangeMetadataCommand cmd = new ChangeMetadataCommand(node, param, old, outputMetaCopy);
                if (commandStack != null) {
                    commandStack.execute(cmd);
                } else {
                    cmd.execute();
                }
            } finally {
                node.setIncomingConnections(incomingConnections);
                node.setOutgoingConnections(outgoingConnections);
                node.setListConnector(connectorsList);
            }
        }
    }

    private IMetadataTable buildMetadata(final String schema) {
        if (schema == null || schema.trim().isEmpty()) {
            return null;
        }
        Collection<MetadataColumn> jsonColumns = new ArrayList<>();
        final String[] lines = schema.split("\n"); //$NON-NLS-1$
        for (String line : lines) {
            try (final Jsonb jsonb = JsonbBuilder.create()) {
                Collection<MetadataColumn> columns = jsonb.fromJson(line, new ParameterizedType() {

                    @Override
                    public Type[] getActualTypeArguments() {
                        return new Type[] { MetadataColumn.class };
                    }

                    @Override
                    public Type getRawType() {
                        return Collection.class;
                    }

                    @Override
                    public Type getOwnerType() {
                        return null;
                    }
                });
                if (columns != null && !columns.isEmpty()) {
                    jsonColumns.addAll(columns);
                }
            } catch (final Exception e) {
                ExceptionHandler.process(e);
            }
        }
        if (jsonColumns.isEmpty()) {
            return null;
        }

        final List<String> columnLabels = new ArrayList<>();
        final List<IMetadataColumn> columns = new ArrayList<>();
        int i = -1;
        for (final IMetadataColumn oneColumn : jsonColumns) {
            i++;
            oneColumn.setLabel(getLabel(columnLabels, i, oneColumn));
            oneColumn.setOriginalDbColumnName(oneColumn.getLabel());
            oneColumn.setPrecision(0);
            oneColumn.setLength(0);
            columns.add(oneColumn);
            columnLabels.add(oneColumn.getLabel());
        }

        if (!columns.isEmpty()) {
            IElementParameter dqRule = elem.getElementParameter("DQRULES_LIST");
            if (dqRule != null) {
                ITDQRuleService ruleService = null;
                try {
                    ruleService =
                            (ITDQRuleService) GlobalServiceRegister.getDefault().getService(ITDQRuleService.class);
                } catch (RuntimeException e) {
                    // nothing to do
                }
                IElementParameter queryParam = elem.getElementParameter("QUERY");
                if (ruleService != null && queryParam != null) {
                    ruleService.updateOriginalColumnNames(columns, queryParam);
                }
            }
        }

        IMetadataTable metadataTable = new MetadataTable();
        metadataTable.setTableName(elementParameter.getContext());
        metadataTable.setListColumns(columns);
        return metadataTable;
    }

    private String getLabel(final List<String> columnLabels, final int i, final IMetadataColumn oneColumn) {
        String labelName = oneColumn.getLabel();
        String sub = ""; //$NON-NLS-1$
        String sub2 = ""; //$NON-NLS-1$
        if (labelName != null && labelName.length() > 0 && labelName.startsWith("_")) { //$NON-NLS-1$
            sub = labelName.substring(1);
            if (sub.length() > 0) {
                sub2 = sub.substring(1);
            }
        }
        if (KeywordsValidator.isKeyword(labelName) || KeywordsValidator.isKeyword(sub)
                || KeywordsValidator.isKeyword(sub2)) {
            labelName = "_" + labelName; //$NON-NLS-1$
        }
        return MetadataToolHelper.validateColumnName(labelName, i, columnLabels);
    }

}
