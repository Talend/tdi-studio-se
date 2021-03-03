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
package org.talend.designer.webservice.ui.dnd;

import java.util.List;

import org.eclipse.jface.util.TransferDropTargetListener;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.talend.commons.ui.runtime.utils.TableUtils;
import org.talend.commons.ui.runtime.ws.WindowSystem;
import org.talend.commons.ui.swt.advanced.dataeditor.AbstractDataTableEditorView;
import org.talend.commons.ui.swt.extended.table.ExtendedTableModel;
import org.talend.commons.ui.swt.linking.TableToTablesLinker;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.designer.webservice.WebServiceComponent;
import org.talend.designer.webservice.data.InputMappingData;
import org.talend.designer.webservice.data.OutPutMappingData;
import org.talend.designer.webservice.ui.ParameterInfoUtil;
import org.talend.designer.webservice.ui.link.WebServiceTableLiner;
import org.talend.designer.webservice.ws.wsdlinfo.ParameterInfo;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class DropTargetListenerForWebService implements TransferDropTargetListener {

    private Control draggableControl;

    private AbstractDataTableEditorView draggableControlView;

    private WebServiceTableLiner tabTotabLink;

    private WebServiceComponent connector;

    public DropTargetListenerForWebService(AbstractDataTableEditorView draggableControlView, TableToTablesLinker tabTotabLink,
            WebServiceComponent connector) {
        this.draggableControlView = draggableControlView;
        this.draggableControl = draggableControlView.getTable();
        this.tabTotabLink = (WebServiceTableLiner) tabTotabLink;
        this.connector = connector;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.util.TransferDropTargetListener#getTransfer()
     */

    public Transfer getTransfer() {
        return TableEntriesTransfer.getInstance();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.util.TransferDropTargetListener#isEnabled(org.eclipse.swt.dnd.DropTargetEvent)
     */

    public boolean isEnabled(DropTargetEvent event) {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.swt.dnd.DropTargetListener#dragEnter(org.eclipse.swt.dnd.DropTargetEvent)
     */

    public void dragEnter(DropTargetEvent event) {
        if (event.detail == DND.DROP_DEFAULT) {
            if ((event.operations & DND.DROP_COPY) != 0) {
                event.detail = DND.DROP_COPY;
            } else {
                event.detail = DND.DROP_NONE;
            }
        }

        for (int i = 0; i < event.dataTypes.length; i++) {
            if (getTransfer().isSupportedType(event.dataTypes[i])) {
                event.currentDataType = event.dataTypes[i];
                if (event.detail != DND.DROP_COPY) {
                    event.detail = DND.DROP_NONE;
                }
                break;
            }
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.swt.dnd.DropTargetListener#dragLeave(org.eclipse.swt.dnd.DropTargetEvent)
     */

    public void dragLeave(DropTargetEvent event) {

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.swt.dnd.DropTargetListener#dragOperationChanged(org.eclipse.swt.dnd.DropTargetEvent)
     */

    public void dragOperationChanged(DropTargetEvent event) {

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.swt.dnd.DropTargetListener#dragOver(org.eclipse.swt.dnd.DropTargetEvent)
     */

    public void dragOver(DropTargetEvent event) {

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.swt.dnd.DropTargetListener#drop(org.eclipse.swt.dnd.DropTargetEvent)
     */

    public void drop(DropTargetEvent event) {
        if (getTransfer().isSupportedType(event.currentDataType)) {
            // boolean canRemove = true;
            // Parameter pa = TableEntriesTransfer.getInstance().getParameter();
            Table draggableTable = (Table) draggableControl;
            Point point = new Point(event.x, event.y);
            int selevIndex = TableUtils.getItemIndexWhereInsertFromPosition(draggableTable, point);
            if (selevIndex < 0) {
                return;
            }
            TableItem tabitem = TableEntriesTransfer.getInstance().getTableItem();
            if (tabitem == null) {
                return;
            }
            ExtendedTableModel tabelModel = draggableControlView.getExtendedTableModel();
            if (tabelModel.getBeansList().size() == 0 || tabelModel.getBeansList().size() == selevIndex) {
                // if (tabelModel.getName().equals("INPUTMAPPING")) {
                // InputMappingData inData = new InputMappingData();
                // IMetadataColumn inputColumn = null;
                // if (tabitem.getData() instanceof IMetadataColumn) {
                // inputColumn = (IMetadataColumn) tabitem.getData();
                // List<IMetadataColumn> columnList = inData.getMetadataColumnList();
                // columnList.add(inputColumn);
                // inData.setInputColumnValue(initInRoWName() + "." + inputColumn.getLabel());
                // inData.setParameterName("");
                // if (tabelModel.getBeansList().size() > 0) {
                // tabelModel.remove(tabelModel.getBeansList().size());
                // tabelModel.add(inData, tabelModel.getBeansList().size());
                // } else {
                // tabelModel.add(inData);
                // }
                // }
                // // inData.setInputColumnValue(tabitem.getData())
                // } else if (tabelModel.getName().equals("OUTPUTMAPPING")) {
                // OutPutMappingData outData = new OutPutMappingData();
                // if (tabitem.getData() instanceof ParameterInfo) {
                // ParameterInfo para = (ParameterInfo) tabitem.getData();
                //
                // outData.setParameterName(para.getName());
                // outData.getParameterList().add(para);
                // // outData.setParameter(para);
                // outData.setOutputColumnValue("");
                // if (tabelModel.getBeansList().size() > 0) {
                // tabelModel.remove(tabelModel.getBeansList().size());
                // tabelModel.add(outData, tabelModel.getBeansList().size());
                // } else {
                // tabelModel.add(outData);
                // }
                // }
                //
                // }
                return;
            } else if (tabelModel.getBeansList().size() > 0) {
                if (tabelModel.getBeansList().size() < selevIndex) {
                    return;
                }
                Object objData = tabelModel.getBeansList().get(selevIndex);

                if (objData instanceof InputMappingData) {
                    InputMappingData inData = (InputMappingData) objData;
                    IMetadataColumn inputColumn = null;
                    if (tabitem.getData() instanceof IMetadataColumn) {
                        inputColumn = (IMetadataColumn) tabitem.getData();
                        List<IMetadataColumn> columnList = inData.getMetadataColumnList();
                        if (inData.getInputColumnValue() == null || "".equals(inData.getInputColumnValue())) {
                            inData.setInputColumnValue(connector.initInRoWName() + "." + inputColumn.getLabel());
                            columnList.add(inputColumn);
                        } else {
                            inData.setInputColumnValue(inData.getInputColumnValue() + " " + connector.initInRoWName() + "."
                                    + inputColumn.getLabel());
                            columnList.add(inputColumn);
                        }

                    }

                    tabelModel.remove(selevIndex);

                    tabelModel.add(inData, selevIndex);
                } else if (objData instanceof OutPutMappingData) {
                    OutPutMappingData outData = (OutPutMappingData) objData;
                    if (tabitem.getData() instanceof ParameterInfo) {
                        ParameterInfo para = (ParameterInfo) tabitem.getData();
                        if (outData.getParameterName() == null || "".equals(outData.getParameterName())) {
                            if (para.getParent() != null) {
                                String name = new ParameterInfoUtil().getParentName(para);
                                outData.setParameterName(name);
                            } else {
                                outData.setParameterName(para.getName());
                            }
                        } else {
                            outData.setParameterName(outData.getParameterName() + " " + para.getName());
                        }
                        outData.getParameterList().add(para);
                        // outData.setParameter(para);
                    } else if (tabitem.getData() instanceof OutPutMappingData) {
                        if (((OutPutMappingData) tabitem.getData()).getParameter() instanceof ParameterInfo) {
                            ParameterInfo para = ((OutPutMappingData) tabitem.getData()).getParameter();
                            if (outData.getParameterName() == null || "".equals(outData.getParameterName())) {
                                if (para.getParent() != null) {
                                    String name = "";
                                    if (((OutPutMappingData) tabitem.getData()).getParameterName() != null) {
                                        name = ((OutPutMappingData) tabitem.getData()).getParameterName();
                                    } else {
                                        name = new ParameterInfoUtil().getParentName(para);
                                    }
                                    outData.setParameterName(name);
                                } else {
                                    outData.setParameterName(para.getName());
                                }
                            } else {
                                outData.setParameterName(outData.getParameterName() + " " + para.getName());
                            }
                            outData.getParameterList().add(para);
                            // outData.setParameter(para);
                        }
                    }

                    tabelModel.remove(selevIndex);

                    tabelModel.add(outData, selevIndex);
                }
            }

            TableItem[] items = draggableTable.getSelection();
            if (items.length <= 0) {
                return;
            }
            TableItem itemTarget = items[0];
            // Returned wrong TableItem without this.
            if (WindowSystem.isBigSurOrLater()) {
                itemTarget.getText();
            }
            itemTarget.setChecked(true);
            createLinks(itemTarget, tabitem, tabelModel.getName());
            if (WindowSystem.isBigSurOrLater()) {
                tabTotabLink.getBackgroundRefresher().refreshBackground();
            }
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.swt.dnd.DropTargetListener#dropAccept(org.eclipse.swt.dnd.DropTargetEvent)
     */

    public void dropAccept(DropTargetEvent event) {

    }

    public void createLinks(TableItem itemTarget, TableItem itemSource, String mark) {

        tabTotabLink.addLinks(itemSource, itemSource.getData(), itemTarget.getParent(), itemTarget.getData(), mark);
    }
}
