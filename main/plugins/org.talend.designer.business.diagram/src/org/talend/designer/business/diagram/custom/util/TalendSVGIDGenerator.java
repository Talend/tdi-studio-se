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
package org.talend.designer.business.diagram.custom.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.batik.svggen.SVGIDGenerator;
import org.apache.batik.svggen.SVGSyntax;
import org.talend.designer.business.model.business.BusinessAssignment;
import org.talend.designer.business.model.business.BusinessItemShape;

/**
 * wchen class global comment. Detailled comment
 */
public class TalendSVGIDGenerator extends SVGIDGenerator {

    private List svgObject;

    private List businessModels;

    private Map prefixMap = new HashMap();

    public TalendSVGIDGenerator(List businessModels) {
        this.businessModels = businessModels;
        svgObject = init(businessModels);
    }

    @Override
    public String generateID(String prefix) {
        Integer maxId = (Integer) prefixMap.get(prefix);
        if (maxId == null) {
            maxId = new Integer(0);
            prefixMap.put(prefix, maxId);
        }

        maxId = new Integer(maxId.intValue() + 1);
        prefixMap.put(prefix, maxId);
        if (svgObject != null && maxId - 1 < svgObject.size() && prefix.equals(SVGSyntax.ID_PREFIX_CLIP_PATH)) {
            if (svgObject.get(maxId - 1) != null) {
                Object obj = svgObject.get(maxId - 1);
                if (obj instanceof BusinessItemShape) {
                    BusinessItemShape item = (BusinessItemShape) obj;
                    String id = "";
                    for (Object assign : item.getAssignments()) {
                        if (assign instanceof BusinessAssignment) {
                            BusinessAssignment assignment = (BusinessAssignment) assign;
                            id = id + assignment.getTalendItem().getLabel() + ";";
                        }
                    }
                    if (id != "") {
                        id = id.substring(0, id.length() - 1);
                        return "businessItem." + businessModels.indexOf(obj);
                    }
                }
            }
        }

        return prefix + maxId;
    }

    private List init(List businessModels) {
        List svgObject = new ArrayList();
        if (businessModels != null) {
            // add shap items
            for (Object obj : businessModels) {
                if (obj instanceof BusinessItemShape) {
                    BusinessItemShape editPart = (BusinessItemShape) obj;
                    svgObject.add(obj);
                    if (editPart.getName() != null) {
                        svgObject.add(null);
                    }

                }
            }
            return svgObject;
        }

        return null;

    }

}
