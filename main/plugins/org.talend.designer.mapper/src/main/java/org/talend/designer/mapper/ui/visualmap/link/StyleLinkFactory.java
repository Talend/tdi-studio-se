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
package org.talend.designer.mapper.ui.visualmap.link;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.talend.commons.ui.swt.drawing.link.BezierHorizontalLink;
import org.talend.commons.ui.swt.drawing.link.ExtremityEastArrow;
import org.talend.commons.ui.swt.drawing.link.ExtremityWestArrow;
import org.talend.commons.ui.swt.drawing.link.IDrawableLink;
import org.talend.commons.ui.swt.drawing.link.IStyleLink;
import org.talend.commons.ui.swt.drawing.link.LineLinkWithHorizontalConnectors;
import org.talend.commons.ui.swt.drawing.link.StyleLink;
import org.talend.commons.ui.swt.drawing.link.VerticalRoundedCornerLink;
import org.talend.designer.abstractmap.model.tableentry.ITableEntry;
import org.talend.designer.abstractmap.ui.properties.LINK_STYLE;
import org.talend.designer.abstractmap.ui.visualmap.link.ILinkState;
import org.talend.designer.abstractmap.ui.visualmap.link.IMapperLink;
import org.talend.designer.abstractmap.ui.visualmap.link.LinkState;
import org.talend.designer.abstractmap.ui.visualmap.link.PointLinkDescriptor;
import org.talend.designer.mapper.model.tableentry.ExpressionFilterEntry;
import org.talend.designer.mapper.model.tableentry.FilterTableEntry;
import org.talend.designer.mapper.model.tableentry.GlobalMapEntry;
import org.talend.designer.mapper.ui.color.ColorInfo;
import org.talend.designer.mapper.ui.color.ColorProviderMapper;
import org.talend.designer.mapper.ui.visualmap.zone.Zone;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class StyleLinkFactory {

    // private static DrawableLinkFactory instance;

    private IStyleLink selectedZoneToZoneStyle;

    private IStyleLink selectedSameZoneFilterStyle;

    private IStyleLink unselectedSameZoneFilterStyle;

    private IStyleLink selectedZoneToZoneFilterStyle;

    private IStyleLink unselectedZoneToZoneFilterStyle;

    private IStyleLink selectedSameInputZoneStyle;

    private IStyleLink unselectedSameInputZoneStyle;

    private IStyleLink unselectedZoneToZoneStyle;

    private IStyleLink selectedSameVarsZoneStyle;

    private IStyleLink unselectedSameVarsZoneStyle;

    private LINK_STYLE linkStyle;

    private IStyleLink selectedSameZoneGlobalMapStyle;

    private IStyleLink unselectedSameZoneGlobalMapStyle;

    /**
     * DOC amaumont LinkFactory constructor comment.
     */
    public StyleLinkFactory(LINK_STYLE linkStyle) {
        super();
        this.linkStyle = linkStyle;
        init();
    }

    /**
     * DOC amaumont Comment method "init".
     */
    private void init() {

        selectedZoneToZoneStyle = getSelectedZoneToOtherZoneStyle(ColorProviderMapper
                .getColor(ColorInfo.COLOR_SELECTED_ZONE_TO_ZONE_LINK));
        unselectedZoneToZoneStyle = getUnselectedZoneToZoneStyle();

        selectedZoneToZoneFilterStyle = getSelectedZoneToZoneFilterStyle();
        unselectedZoneToZoneFilterStyle = getUnselectedZoneToZoneFilterStyle();

        selectedSameZoneGlobalMapStyle = getSelectedSameZoneGlobalMapStyle();
        unselectedSameZoneGlobalMapStyle = getUnselectedSameZoneGlobalMapStyle();

        selectedSameZoneFilterStyle = getSelectedSameZoneFilterStyle();
        unselectedSameZoneFilterStyle = getUnselectedSameZoneFilterStyle();

        selectedSameInputZoneStyle = getSelectedSameInputZoneStyle();
        unselectedSameInputZoneStyle = getUnselectedSameInputZoneStyle();

        selectedSameVarsZoneStyle = getSelectedSameVarsZoneStyle();
        unselectedSameVarsZoneStyle = getUnselectedSameVarsZoneStyle();

    }

    // public static DrawableLinkFactory getInstance() {
    // if (instance == null) {
    // instance = new DrawableLinkFactory();
    // }
    // return instance;
    // }
    //
    // public IDrawableLink getDrawableLink(IMapperLink link) {
    // IDrawableLink drawableLink = null;
    // PointLinkDescriptor pointLinkDescriptorSource = link.getPointLinkDescriptorSource();
    // PointLinkDescriptor pointLinkDescriptorTarget = link.getPointLinkDescriptorTarget();
    // if (pointLinkDescriptorSource.getZone() != pointLinkDescriptorTarget.getZone()) {
    // drawableLink = new HorizontalBezierLink();
    // } else if (pointLinkDescriptorSource.getZone() == pointLinkDescriptorTarget.getZone()) {
    // drawableLink = new VerticalRoundedCornerLink();
    // }
    // return drawableLink;
    // }
    //
    public IStyleLink getStyleLink(IMapperLink link) {
        ILinkState linkState = link.getState();
        PointLinkDescriptor pointLinkDescriptorSource = link.getPointLinkDescriptor1();
        PointLinkDescriptor pointLinkDescriptorTarget = link.getPointLinkDescriptor2();
        ITableEntry targetTableEntry = pointLinkDescriptorTarget.getTableEntry();

        boolean targetIsConstraint = false;
        if (targetTableEntry instanceof FilterTableEntry || targetTableEntry instanceof ExpressionFilterEntry) {
            targetIsConstraint = true;
        }

        boolean targetIsGlobalMap = false;
        if (targetTableEntry instanceof GlobalMapEntry) {
            targetIsGlobalMap = true;
        }

        IStyleLink style = null;
        if (pointLinkDescriptorSource.getZone() != pointLinkDescriptorTarget.getZone()) {
            if (linkState == LinkState.SELECTED) {
                if (targetIsConstraint) {
                    style = selectedZoneToZoneFilterStyle;
                } else {
                    style = selectedZoneToZoneStyle;
                }
            } else if (linkState == LinkState.UNSELECTED) {
                if (targetIsConstraint) {
                    style = unselectedZoneToZoneFilterStyle;
                } else {
                    style = unselectedZoneToZoneStyle;
                }
            }
        } else if (pointLinkDescriptorSource.getZone() == Zone.INPUTS
                && pointLinkDescriptorSource.getZone() == pointLinkDescriptorTarget.getZone()) {
            if (linkState == LinkState.SELECTED) {
                if (pointLinkDescriptorTarget.getTableEntry() instanceof ExpressionFilterEntry) {
                    style = selectedSameZoneFilterStyle;
                } else if(pointLinkDescriptorTarget.getTableEntry() instanceof GlobalMapEntry) {
                    style = selectedSameZoneGlobalMapStyle;
                } else {
                    style = selectedSameInputZoneStyle;
                }
            } else if (linkState == LinkState.UNSELECTED) {
                if (pointLinkDescriptorTarget.getTableEntry() instanceof ExpressionFilterEntry) {
                    style = unselectedSameZoneFilterStyle;
                } else if(pointLinkDescriptorTarget.getTableEntry() instanceof GlobalMapEntry) {
                    style = unselectedSameZoneGlobalMapStyle;
                } else {
                    style = unselectedSameInputZoneStyle;
                }
            }
        } else if (pointLinkDescriptorSource.getZone() == Zone.VARS
                && pointLinkDescriptorSource.getZone() == pointLinkDescriptorTarget.getZone()) {
            if (linkState == LinkState.SELECTED) {
                style = selectedSameVarsZoneStyle;
            } else if (linkState == LinkState.UNSELECTED) {
                style = unselectedSameVarsZoneStyle;
            }
        }
        return style;
    }

    /**
     * DOC amaumont Comment method "getSelectedFilterStyle".
     *
     * @return
     */
    private IStyleLink getSelectedZoneToZoneFilterStyle() {
        StyleLink style = new StyleLink();
        setCommonsStyleProperties(style);
        style.setDrawableLink(getZoneToZoneLink(style));
        ExtremityEastArrow eastArrowSource = new ExtremityEastArrow(style);
        style.setExtremity1(eastArrowSource);
        ExtremityEastArrow eastArrowTarget = new ExtremityEastArrow(style);
        eastArrowTarget.setXOffset(-eastArrowTarget.getSize().x);
        style.setExtremity2(eastArrowTarget);
        style.setForegroundColor(ColorProviderMapper.getColor(ColorInfo.COLOR_SELECTED_FILTER_LINK));
        return style;
    }

    /**
     * DOC amaumont Comment method "getSelectedFilterStyle".
     *
     * @return
     */
    private IStyleLink getUnselectedZoneToZoneFilterStyle() {
        StyleLink style = new StyleLink();
        setCommonsStyleProperties(style);
        style.setDrawableLink(getZoneToZoneLink(style));
        // ExtremityEastArrow eastArrowSource = new ExtremityEastArrow(style);
        // style.setExtremity1(eastArrowSource);
        ExtremityEastArrow eastArrowTarget = new ExtremityEastArrow(style);
        eastArrowTarget.setXOffset(-eastArrowTarget.getSize().x);
        style.setExtremity2(eastArrowTarget);
        style.setForegroundColor(ColorProviderMapper.getColor(ColorInfo.COLOR_UNSELECTED_FILTER_LINK));
        return style;
    }

    private IStyleLink getSelectedZoneToOtherZoneStyle(Color foregroundColor) {
        StyleLink style = new StyleLink();
        setCommonsStyleProperties(style);
        style.setDrawableLink(getZoneToZoneLink(style));
        ExtremityEastArrow eastArrowSource = new ExtremityEastArrow(style);
        style.setExtremity1(eastArrowSource);
        ExtremityEastArrow eastArrowTarget = new ExtremityEastArrow(style);
        eastArrowTarget.setXOffset(-eastArrowTarget.getSize().x);
        style.setExtremity2(eastArrowTarget);
        style.setForegroundColor(foregroundColor);
        return style;
    }

    /**
     * DOC amaumont Comment method "getNotSelectedSameZoneStyle".
     *
     * @return
     */
    private IStyleLink getUnselectedZoneToZoneStyle() {
        StyleLink style = new StyleLink();
        setCommonsStyleProperties(style);
        style.setDrawableLink(getZoneToZoneLink(style));
        ExtremityEastArrow eastArrowTarget = new ExtremityEastArrow(style);
        eastArrowTarget.setXOffset(-eastArrowTarget.getSize().x);
        style.setExtremity2(eastArrowTarget);
        style.setForegroundColor(ColorProviderMapper.getColor(ColorInfo.COLOR_UNSELECTED_ZONE_TO_ZONE_LINK));
        return style;
    }

    /**
     * DOC amaumont Comment method "getNotSelectedSameZoneStyle".
     *
     * @return
     */
    private IStyleLink getUnselectedSameInputZoneStyle() {
        StyleLink style = new StyleLink();
        setCommonsStyleProperties(style);
        style.setDrawableLink(getSameZoneLink(style));
        ExtremityEastArrow eastArrowTarget = new ExtremityEastArrow(style);
        eastArrowTarget.setXOffset(-eastArrowTarget.getSize().x);
        style.setExtremity2(eastArrowTarget);
        style.setForegroundColor(ColorProviderMapper.getColor(ColorInfo.COLOR_UNSELECTED_LOOKUP_LINKS));
        return style;
    }

    /**
     * DOC amaumont Comment method "getSelectedSameZoneStyle".
     *
     * @param foregroundColor
     * @return
     */
    public IStyleLink getSelectedSameInputZoneStyle() {
        StyleLink style = new StyleLink();
        setCommonsStyleProperties(style);
        IDrawableLink sameZoneLink = getSameZoneLink(style);
        style.setDrawableLink(sameZoneLink);
        ExtremityWestArrow westArrow = new ExtremityWestArrow(style);
        style.setExtremity1(westArrow);
        ExtremityEastArrow eastArrowTarget = new ExtremityEastArrow(style);
        eastArrowTarget.setXOffset(-eastArrowTarget.getSize().x);
        style.setExtremity2(eastArrowTarget);
        style.setForegroundColor(ColorProviderMapper.getColor(ColorInfo.COLOR_SELECTED_LOOKUP_LINKS));
        return style;
    }

    /**
     * DOC amaumont Comment method "getSelectedSameZoneStyle".
     *
     * @param foregroundColor
     * @return
     */
    public IStyleLink getSelectedSameZoneFilterStyle() {
        IStyleLink style = getSelectedSameInputZoneStyle();
        style.setForegroundColor(ColorProviderMapper.getColor(ColorInfo.COLOR_SELECTED_FILTER_LINK));
        return style;
    }

    /**
     * DOC amaumont Comment method "getSelectedSameZoneStyle".
     *
     * @param foregroundColor
     * @return
     */
    public IStyleLink getUnselectedSameZoneFilterStyle() {
        IStyleLink style = getSelectedSameInputZoneStyle();
        style.setForegroundColor(ColorProviderMapper.getColor(ColorInfo.COLOR_UNSELECTED_FILTER_LINK));
        return style;
    }


    /**
     * DOC amaumont Comment method "getSelectedFilterStyle".
     *
     * @return
     */
    private IStyleLink getSelectedSameZoneGlobalMapStyle() {
        IStyleLink style = getSelectedSameInputZoneStyle();
        style.setForegroundColor(ColorProviderMapper.getColor(ColorInfo.COLOR_SELECTED_GLOBALMAP_LINK));
        return style;
    }

    /**
     * DOC amaumont Comment method "getSelectedFilterStyle".
     *
     * @return
     */
    private IStyleLink getUnselectedSameZoneGlobalMapStyle() {
        IStyleLink style = getSelectedSameInputZoneStyle();
        style.setForegroundColor(ColorProviderMapper.getColor(ColorInfo.COLOR_UNSELECTED_GLOBALMAP_LINK));
        return style;
    }


    /**
     * DOC amaumont Comment method "getNotSelectedSameZoneStyle".
     *
     * @return
     */
    private IStyleLink getUnselectedSameVarsZoneStyle() {
        StyleLink style = new StyleLink();
        setCommonsStyleProperties(style);
        style.setDrawableLink(getSameZoneLink(style));
        ExtremityWestArrow westArrow = new ExtremityWestArrow(style);
        style.setExtremity1(westArrow);
        ExtremityEastArrow eastArrowTarget = new ExtremityEastArrow(style);
        eastArrowTarget.setXOffset(-eastArrowTarget.getSize().x);
        style.setExtremity2(eastArrowTarget);
        style.setForegroundColor(ColorProviderMapper.getColor(ColorInfo.COLOR_UNSELECTED_LOOKUP_LINKS));
        return style;
    }

    /**
     * DOC amaumont Comment method "getSelectedSameZoneStyle".
     *
     * @param foregroundColor
     * @return
     */
    public IStyleLink getSelectedSameVarsZoneStyle() {
        StyleLink style = new StyleLink();
        setCommonsStyleProperties(style);
        IDrawableLink sameZoneLink = getSameZoneLink(style);
        style.setDrawableLink(sameZoneLink);
        ExtremityWestArrow westArrow = new ExtremityWestArrow(style);
        style.setExtremity1(westArrow);
        ExtremityEastArrow eastArrowTarget = new ExtremityEastArrow(style);
        eastArrowTarget.setXOffset(-eastArrowTarget.getSize().x);
        style.setExtremity2(eastArrowTarget);
        style.setForegroundColor(ColorProviderMapper.getColor(ColorInfo.COLOR_SELECTED_LOOKUP_LINKS));
        return style;
    }

    /**
     * DOC amaumont Comment method "setCommonsStyleProperties".
     *
     * @param style
     */
    private void setCommonsStyleProperties(StyleLink style) {
        style.setLineWidth(2);
        style.setLineCap(SWT.CAP_SQUARE);
        style.setLineJoin(SWT.JOIN_ROUND);
        // style.setLineStyle(SWT.LINE_DASH);
        // style.setLineDash(SWT.JOIN_BEVEL);
    }

    /**
     * DOC amaumont Comment method "getSameZoneLink".
     *
     * @param style
     * @return
     */
    private IDrawableLink getSameZoneLink(StyleLink style) {
        return new VerticalRoundedCornerLink(style);
    }

    /**
     * DOC amaumont Comment method "getZoneToZoneLink".
     *
     * @return
     */
    private IDrawableLink getZoneToZoneLink(StyleLink style) {
        if (linkStyle == LINK_STYLE.BEZIER_CURVE) {
            return new BezierHorizontalLink(style);
        } else {
            return new LineLinkWithHorizontalConnectors(style);
        }
    }

}
