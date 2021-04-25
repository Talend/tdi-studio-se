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
package org.talend.designer.core.ui.editor.connections;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.ActionEvent;
import org.eclipse.draw2d.ActionListener;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.runtime.image.OverlayImage.EPosition;
import org.talend.commons.ui.runtime.image.OverlayImageProvider;
import org.talend.commons.ui.utils.workbench.gef.SimpleHtmlFigure;
import org.talend.commons.utils.workbench.preferences.GlobalConstant;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.TraceData;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.designer.core.model.components.EParameterName;

/**
 * DOC nrousseau class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class ConnectionTraceFigure extends Figure {

    private static final String FIELD_SEP = "|"; //$NON-NLS-1$

    private static final String FIELD_EQUAL = "="; //$NON-NLS-1$

    private static final Color BACKGROUND = new Color(null, 220, 220, 220);

    private static final int MAX_VARIABLE_WIDTH = 70;

    private static final int MAX_VALUE_WIDTH = 100;

    public static final String BREAKPOINT_IMAGE = "icons/breakpoint.png";

    private Connection connection;

    private boolean maximized;

    private ConnectionTraceFigure tooltip = null;

    private CollapseFigure collapseButton;

    private int contentWidth = 0;

    public ConnectionTraceFigure(Connection connection, boolean maximized) {
        ToolbarLayout layout = new ToolbarLayout();
        setLayoutManager(layout);
        // setBorder(new SimpleRaisedBorder());
        this.connection = connection;
        this.maximized = maximized;
        if (maximized) {
            tooltip = new ConnectionTraceFigure(connection, false);
            this.setToolTip(tooltip);
            tooltip.setVisible(false);
        }
    }

    @Override
    public void paint(Graphics graphics) {
        // see bug 2074
        if (GlobalConstant.generatingScreenShoot) {
            return;
        }
        super.paint(graphics);
    }

    Figure variableFigure = null;

    private void setVariableFigureBorder() {
        if (variableFigure != null && variableFigure.getBorder() == null) {
            variableFigure.setBorder(new LineBorder(ColorConstants.darkGray, SWT.LEFT | SWT.BOTTOM | SWT.RIGHT));
        }
    }

    public void setTraceData(TraceData data, boolean flag, boolean traceFlag) {
        if (data != null) {
            List childrens = this.getChildren();
            childrens.clear();
            boolean noVarNameDefined = false;

            Figure outlineFigure = new Figure();
            outlineFigure.setLayoutManager(new ToolbarLayout(true));

            int title1With = 0;
            int title2With = 0;

            if (tooltip != null) {
                collapseButton = new CollapseFigure();
                collapseButton.setCollapsed(connection.getConnectionTrace().isCollapse());
                collapseButton.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent event) {
                        connection.getConnectionTrace().setCollapse(!connection.getConnectionTrace().isCollapse());
                        collapseButton.setCollapsed(connection.getConnectionTrace().isCollapse());
                        refreshCollapseStatus();
                    }
                });
                if (flag == true) {
                    outlineFigure.add(collapseButton);
                }
            }
            // int sepIndex = data.indexOf(FIELD_SEP); // index separator for row name
            //
            // String dataWithoutRowName = data.substring(sepIndex + 1);
            // sepIndex = dataWithoutRowName.indexOf(FIELD_SEP);
            int lineNumber = data.getNbLine();
            SimpleHtmlFigure titleFigure = new SimpleHtmlFigure();

            titleFigure.setText(""); //$NON-NLS-1$
            titleFigure.setText("<font color='#0000FF'> <b> " + connection.getConnectionLabel().getLabelText() //$NON-NLS-1$
                    + "</b></font>"); //$NON-NLS-1$
            if (tooltip != null) {
                titleFigure.setBackgroundColor(ColorConstants.white);
                titleFigure.setOpaque(false);
            }
            titleFigure.getPreferredSize().expand(20, 2);
            title1With = titleFigure.getPreferredSize().width;

            SimpleHtmlFigure titleFigureSe = new SimpleHtmlFigure();

            titleFigureSe.setText(" <font color='#808080'>Current row : " + lineNumber + "</font>"); //$NON-NLS-1$ //$NON-NLS-2$
            if (tooltip != null) {
                titleFigureSe.setBackgroundColor(ColorConstants.white);
                titleFigureSe.setOpaque(false);
            }

            titleFigureSe.getPreferredSize().expand(20, 2);
            title2With = titleFigureSe.getPreferredSize().width;
            if (flag == true) {
                outlineFigure.add(titleFigure);
            }

            ImageFigure figure = new ImageFigure(getTraceConnectionImage(flag));
            outlineFigure.add(figure);
            if (flag == true) {
                outlineFigure.add(titleFigureSe);
            }

            outlineFigure.setBorder(new LineBorder(ColorConstants.darkGray, SWT.LEFT | SWT.RIGHT | SWT.TOP | SWT.BOTTOM));
            outlineFigure.setOpaque(true);
            add(outlineFigure);

            Dimension size = titleFigure.getPreferredSize().getCopy();

            int variableWidth = 0;
            int valueWidth = 0;
            // String lineInfo = dataWithoutRowName.substring(sepIndex + 1);
            // ArrayList columnValueList = new ArrayList();
            // int lastLocation = 0;
            // int endLocation = lineInfo.indexOf(FIELD_SEP, lastLocation);
            // while (endLocation != -1) {
            // columnValueList.add(lineInfo.substring(lastLocation, endLocation + 1));
            // lastLocation = endLocation + 2;
            // endLocation = lineInfo.indexOf(FIELD_SEP, lastLocation);
            // }
            // if (columnValueList.size() > 0 && connection.traceColumn.size() == 0
            // && connection.getPropertyValue(EParameterName.TRACES_CONNECTION_FILTER.getName()) != null) {
            // Object value = connection.getPropertyValue(EParameterName.TRACES_CONNECTION_FILTER.getName());
            // lineInfo = "";
            // for (Object o : columnValueList) {
            // for (Object o1 : (ArrayList) value) {
            // if (o1 instanceof HashMap) {
            // String columnValue = o.toString();
            // if (columnValue.indexOf("=") > 0) {
            // if (((HashMap) o1).get(IConnection.TRACE_SCHEMA_COLUMN).toString()
            // .equals(columnValue.substring(0, columnValue.indexOf("=")).trim())
            // && Boolean.TRUE.toString().equals(
            // ((HashMap) o1).get(IConnection.TRACE_SCHEMA_COLUMN_CHECKED).toString())) {
            // lineInfo += columnValue;
            // break;
            // }
            // }
            // }
            // }
            // }
            // }
            // if (columnValueList.size() > 0 && (connection.traceColumn.size() > 0 || connection.setNullColumn ==
            // true)) {
            // lineInfo = "";
            // Object value = connection.getPropertyValue(EParameterName.TRACES_CONNECTION_FILTER.getName());
            // int columnNum = 0;
            // for (Object o : columnValueList) {
            // if (connection.traceColumn != null && connection.traceColumn.contains(columnNum)) {
            // lineInfo += o.toString();
            // } else {
            // for (Object o1 : (ArrayList) value) {
            // if (((HashMap) o1).get(IConnection.TRACE_SCHEMA_COLUMN).toString()
            // .equals(o.toString().substring(0, o.toString().indexOf("=")).trim())
            // && Boolean.TRUE.toString().equals(
            // ((HashMap) o1).get(IConnection.TRACE_SCHEMA_COLUMN_CHECKED).toString())) {
            // lineInfo += o.toString();
            // break;
            // }
            // }
            // }
            // columnNum++;
            // }
            // }
            // StringTokenizer st = new StringTokenizer(lineInfo, FIELD_SEP);
            List<Map<String, Object>> columnsCheckInfo = null;
            if (data.getData() != null) {
                columnsCheckInfo = TracesConnectionUtils.getTraceConnectionFilterValues(connection);
                Iterator<String> iterator = data.getData().keySet().iterator();
                while (iterator.hasNext()) {
                    String columnLabel = iterator.next();
                    if (columnsCheckInfo != null && !isColumnChecked(columnsCheckInfo, columnLabel)) {
                        continue;
                    }
                    // String str = st.nextToken();
                    // int valueStart = str.indexOf(FIELD_EQUAL);
                    // if (valueStart != -1) {
                    String formatedVariable = "<font color='#000000'>  <b>" + columnLabel //$NON-NLS-1$
                            + "</b></font>"; //$NON-NLS-1$
                    String formatedValue = "<font color='#FF8040'> <b>" + data.getData().get(columnLabel) + "</b></font>"; //$NON-NLS-1$ //$NON-NLS-2$
                    SimpleHtmlFigure var = new SimpleHtmlFigure();
                    var.setText(formatedVariable);
                    SimpleHtmlFigure value = new SimpleHtmlFigure();
                    value.setText(formatedValue);
                    Dimension varSize = var.getPreferredSize();
                    varSize.expand(0, 3);
                    var.setPreferredSize(varSize);
                    if (varSize.width > variableWidth) {
                        variableWidth = varSize.width;
                    }
                    Dimension valueSize = value.getPreferredSize();
                    valueSize.expand(0, 3);
                    value.setPreferredSize(valueSize);
                    if (valueSize.width > valueWidth) {
                        valueWidth = valueSize.width;
                    }
                    size.expand(0, varSize.height);
                    // } else {
                    // noVarNameDefined = true;
                    //                    String formatedValue = "<font color='#FF8040'> <b>" + str + "</b></font>"; //$NON-NLS-1$ //$NON-NLS-2$
                    // SimpleHtmlFigure value = new SimpleHtmlFigure();
                    // value.setText(formatedValue);
                    // Dimension valueSize = value.getPreferredSize();
                    // if (valueSize.width > valueWidth) {
                    // valueWidth = valueSize.width;
                    // }
                    // size.expand(0, valueSize.height);
                    // }
                }
            }
            variableWidth += 10;
            valueWidth += 10;

            if (variableWidth < title1With) {
                variableWidth = title1With;
            }

            if (valueWidth < title2With) {
                valueWidth = title2With;
            }

            // if (maximized) {
            if (variableWidth < MAX_VARIABLE_WIDTH) {
                variableWidth = MAX_VARIABLE_WIDTH;
            }

            if (valueWidth < MAX_VALUE_WIDTH) {
                valueWidth = MAX_VALUE_WIDTH;
            }
            // }

            if ((variableWidth + valueWidth) < titleFigure.getPreferredSize().width) {
                valueWidth = titleFigure.getPreferredSize().width - variableWidth;
            }
            if (noVarNameDefined) {
                if (titleFigure.getPreferredSize().width > valueWidth) {
                    valueWidth = titleFigure.getPreferredSize().width;
                }
            }

            // st = new StringTokenizer(lineInfo, FIELD_SEP);
            int nbVar = 0;
            if (data.getData() != null) {
                Iterator<String> iterator = data.getData().keySet().iterator();
                while (iterator.hasNext()) {
                    String columnLabel = iterator.next();
                    if (columnsCheckInfo != null && !isColumnChecked(columnsCheckInfo, columnLabel)) {
                        continue;
                    }
                    // int valueStart = str.indexOf(FIELD_EQUAL);
                    // if (valueStart != -1) {
                    String formatedVariable = "<font color='#000000'>  <b>" + columnLabel //$NON-NLS-1$
                            + "</b></font>"; //$NON-NLS-1$
                    String formatedValue = "<font color='#FF8040'> <b>" + data.getData().get(columnLabel) + "</b></font>"; //$NON-NLS-1$ //$NON-NLS-2$
                    SimpleHtmlFigure var = new SimpleHtmlFigure();
                    var.setText(formatedVariable);
                    SimpleHtmlFigure value = new SimpleHtmlFigure();
                    value.setText(formatedValue);
                    Dimension valueSize = value.getPreferredSize();
                    valueSize.expand(0, 3);
                    value.setPreferredSize(valueSize);
                    value.setPreferredSize(valueWidth, valueSize.height);
                    var.setBorder(new LineBorder(ColorConstants.darkGray, SWT.RIGHT));
                    Dimension varSize = var.getPreferredSize();
                    varSize.expand(0, 3);
                    var.setPreferredSize(varSize);
                    var.setPreferredSize(variableWidth, varSize.height);

                    ToolbarLayout variableLayout = new ToolbarLayout(true);
                    variableFigure = new Figure();
                    variableFigure.setLayoutManager(variableLayout);
                    variableFigure.add(var);
                    variableFigure.add(value);
                    if (flag == true) {
                        add(variableFigure);
                    }
                    // } else {
                    //                    String formatedValue = "<font color='#FF8040'> <b> " + str + "</b></font>"; //$NON-NLS-1$ //$NON-NLS-2$
                    // SimpleHtmlFigure value = new SimpleHtmlFigure();
                    // value.setText(formatedValue);
                    //
                    // Dimension valueSize = value.getPreferredSize();
                    // valueSize.expand(0, 3);
                    // value.setPreferredSize(valueSize);
                    // value.setPreferredSize(valueWidth, valueSize.height);
                    //
                    // ToolbarLayout variableLayout = new ToolbarLayout(true);
                    // variableFigure = new Figure();
                    // variableFigure.setLayoutManager(variableLayout);
                    // variableFigure.add(value);
                    // if (flag == true) {
                    // add(variableFigure);
                    // }
                    // }
                }
                if (tooltip != null) {
                    if (variableFigure != null) {
                        variableFigure.setBorder(new LineBorder(ColorConstants.darkGray, SWT.LEFT | SWT.RIGHT));
                    }
                }
                if (variableFigure != null) {
                    variableFigure.setOpaque(true);
                }
                if ((nbVar % 2) != 0) {
                    if (tooltip != null) {
                        if (variableFigure != null) {
                            variableFigure.setBackgroundColor(ColorConstants.white);
                        }
                    }
                } else {
                    if (variableFigure != null) {
                        variableFigure.setBackgroundColor(BACKGROUND);
                    }
                }

                nbVar++;
            }
            if (tooltip != null) {
                if (variableFigure != null) {
                    variableFigure.setBorder(new LineBorder(ColorConstants.darkGray, SWT.LEFT | SWT.BOTTOM | SWT.RIGHT));
                }
            }
            if (maximized) {
                this.setVariableFigureBorder();
            }
            if (noVarNameDefined) {
                size.width = valueWidth;
            } else {
                size.width = variableWidth + valueWidth;
            }

            if (size.width < titleFigure.getPreferredSize().width) {
                size.width = titleFigure.getPreferredSize().width;
            }
            // size.width = size.width * 2;
            size.expand(5, 3);
            setPreferredSize(size);
            setVisible(true);
            if (!flag) {
                this.remove(outlineFigure);
                add(figure);
            }

        } else {
            if (traceFlag) {
                Image enableImage = getTraceConnectionImage(flag);

                setPreferredSize(enableImage.getImageData().width, enableImage.getImageData().height);

                this.getChildren().clear();
                ImageFigure figure = new ImageFigure(enableImage);
                add(figure);

                setVisible(true);
            } else {
                setPreferredSize(0, 0);
                setVisible(false);
            }
        }
        if (tooltip != null) {
            if (flag) {
                tooltip.setTraceData(data, flag, traceFlag);
            } else {
                tooltip.setTraceData(data, flag, false);
            }
        }
        contents = new ArrayList(getChildren());
        refreshCollapseStatus();
    }

    /**
     * judge the column checked or not DOC cmeng Comment method "isColumnChecked".
     *
     * @param columnsCheckInfo
     * @param columnLabel
     * @return 1. columnLabel exists in columnsCheckInfo <br>
     * <li>true : columnLabel is checked<br> <li>false : columnLabel is not checked<br>
     * 2. columnLabel not exists in columnsCheckInfo <br> <li>true
     */
    private boolean isColumnChecked(List<Map<String, Object>> columnsCheckInfo, String columnLabel) {
        Map<String, Object> foundLine = null;
        for (Map<String, Object> line : columnsCheckInfo) {
            Object column = line.get(IConnection.TRACE_SCHEMA_COLUMN);
            if (columnLabel.equals(column)) {// found
                foundLine = line;
                break;
            }
        }
        if (foundLine != null) {
            return Boolean.valueOf(foundLine.get(IConnection.TRACE_SCHEMA_COLUMN_CHECKED).toString());
        }
        return columnsCheckInfo == null || columnsCheckInfo.isEmpty();
    }

    /**
     *
     * cLi Comment method "getTraceConnectionImage".
     *
     * feature 6355.
     */
    private Image getTraceConnectionImage(boolean enable) {
        Image image = null;
        if (enable) {
            image = ImageProvider.getImage(ECoreImage.TRACE_ON);
        } else {
            image = ImageProvider.getImage(ECoreImage.TRACE_OFF);
        }
        if (connection.getElementParameter(EParameterName.ACTIVEBREAKPOINT.getName()) != null) {
            if ((Boolean) connection.getElementParameter(EParameterName.ACTIVEBREAKPOINT.getName()).getValue()
                    && (Boolean) connection.getElementParameter(EParameterName.TRACES_CONNECTION_ENABLE.getName()).getValue()) {
                image = ImageProvider.getImage(CoreUIPlugin.getImageDescriptor(BREAKPOINT_IMAGE));
            } else {

            }

        }
        if (image != null && connection.getCondition() != null) {
            image = OverlayImageProvider.getImageForOverlay(image, EImage.INFORMATION_SMALL, EPosition.BOTTOM_LEFT);
        }
        return image;
    }

    private List contents = null;

    /**
     * Refresh the collapse status of the content table according to the selection of collapse button.
     */
    private void refreshCollapseStatus() {
        if (collapseButton == null) {
            return;
        }

        boolean collapse = connection.getConnectionTrace().isCollapse();
        if (collapse) {
            List list = new ArrayList(getChildren());
            for (int i = 1; i < list.size(); i++) {
                remove((IFigure) list.get(i));
            }
            IFigure figure = (IFigure) contents.get(0);
            this.getPreferredSize().height = figure.getPreferredSize().height;
            if (contentWidth <= this.getPreferredSize().width) {
                contentWidth = this.getPreferredSize().width;
            } else {
                this.getPreferredSize().width = contentWidth;
            }
            setPreferredSize(getPreferredSize());
        } else {
            removeAll();
            for (int i = 0; i < contents.size(); i++) {
                IFigure figure = (IFigure) contents.get(i);
                add(figure);
            }
            IFigure figure = (IFigure) contents.get(0);
            this.getPreferredSize().height = figure.getPreferredSize().height * contents.size();
            if (contentWidth <= this.getPreferredSize().width) {
                contentWidth = this.getPreferredSize().width;
            } else {
                this.getPreferredSize().width = contentWidth;
            }
            setPreferredSize(getPreferredSize());

        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.draw2d.Figure#getPreferredSize(int, int)
     */
    @Override
    public Dimension getPreferredSize(int hint, int hint2) {
        // TODO Auto-generated method stub
        return super.getPreferredSize(hint, hint2);
    }
}
