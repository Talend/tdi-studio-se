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
package org.talend.designer.webservice.ui;

import java.util.ArrayList;
import java.util.List;

import org.talend.designer.webservice.data.ArrayIndexList;
import org.talend.designer.webservice.data.InputMappingData;
import org.talend.designer.webservice.ws.wsdlinfo.ParameterInfo;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class ParameterInfoUtil {

    private int currentindex;

    private List<ArrayIndexList> currenIndexList;

    public List<ParameterInfo> getAllMostParameterInfo(ParameterInfo para, String mark, Object obj) {
        ParameterInfo parentPara = para.getParent();
        ParameterInfo paraToCom = null;
        if (mark.equals("IN")) {
            paraToCom = ((InputMappingData) obj).getParameter();
        } else if (mark.equals("OUT")) {
            paraToCom = (ParameterInfo) obj;
        }
        List<ParameterInfo> list = new ArrayList<ParameterInfo>();
        if (parentPara != paraToCom) {
            list.add(para);
            List<ParameterInfo> pali = getAllMostParameterInfo(parentPara, mark, obj);
            list.addAll(pali);
        } else {
            list.add(para);
        }
        return list;
    }

    public int getCurrentindex() {
        return currentindex;
    }

    public void setCurrentindex(int currentindex) {
        this.currentindex = currentindex;
    }

    public List<ArrayIndexList> getCurrenIndexList() {
        return this.currenIndexList;
    }

    public void setCurrenIndexList(List<ArrayIndexList> currenIndexList) {
        this.currenIndexList = currenIndexList;
    }

    public List<ParameterInfo> getAllChildren(ParameterInfo para) {
        List<ParameterInfo> list = new ArrayList<ParameterInfo>();
        List<ParameterInfo> childList = para.getParameterInfos();
        list.addAll(childList);
        for (ParameterInfo paraC : childList) {
            if (paraC.getParameterInfos().size() > 0) {
                list.addAll(getAllChildren(paraC));
            }
        }
        return list;
    }

    public List<ParameterInfo> getAllParameterInfo(ParameterInfo para) {
        ParameterInfo parentPara = para.getParent();
        List<ParameterInfo> list = new ArrayList<ParameterInfo>();
        if (parentPara != null) {
            list.add(para);
            List<ParameterInfo> pali = getAllParameterInfo(parentPara);
            list.addAll(pali);
        } else {
            list.add(para);
        }
        return list;
    }

    public String getParentName(ParameterInfo para) {
        if (currenIndexList != null) {
            if (!currenIndexList.isEmpty()) {
                return getParentName(para, 2);
            }
        }
        List<ParameterInfo> paraList = getAllParameterInfo(para);
        StringBuffer buffer = new StringBuffer();
        ParameterInfo nearArrayParent = null;
        goout: for (ParameterInfo pa : paraList) {
            if (pa.getArraySize() != 0) {
                nearArrayParent = pa;
                break goout;
            }
        }
        for (int i = paraList.size() - 1; i >= 0; i--) {
            ParameterInfo parentPara = paraList.get(i);
            if (parentPara == null) {
                continue;
            }
            buffer.append(parentPara.getName());

            if (nearArrayParent != null && parentPara == nearArrayParent) {
                if (para.getArraySize() == 0 && para.getParameterInfos().size() == 0 && getCurrentindex() != -1 && i != 0) {
                    buffer.append("[" + getCurrentindex() + "]");
                } else if (para.getArraySize() != 0 && para.getParameterInfos().size() == 0 && getCurrentindex() != -1) {
                    buffer.append("[" + getCurrentindex() + "]");
                } else if (para.getArraySize() != 0 && para.getParameterInfos().size() == 0 && getCurrentindex() == -1) {
                    buffer.append("[*]");
                }
            }
            if (i != 0) {
                buffer.append(".");
            }
            // if (para.getArraySize() != 0) {
            // if (i == 0) {
            // buffer.append("[]");
            // }
            // }
        }
        return buffer.toString();
    }

    public String getParentName(ParameterInfo para, int multi) {
        List<ParameterInfo> paraList = getAllParameterInfo(para);
        StringBuffer buffer = new StringBuffer();

        for (int i = paraList.size() - 1; i >= 0; i--) {
            ParameterInfo parentPara = paraList.get(i);
            if (parentPara == null) {
                continue;
            }
            buffer.append(parentPara.getName());

            for (int m = 0; m < currenIndexList.size(); m++) {
                if (currenIndexList.get(m).getParameterName().equals(parentPara.getName())) {
                    buffer.append("[" + currenIndexList.get(m).getIndexNum() + "]");
                }
            }
            if (i != 0) {
                buffer.append(".");
            }
        }

        return buffer.toString();
    }

    public String getArrayFullName(ParameterInfo paraElement) {
        String arrayFullName = paraElement.getName();
        if (paraElement.getIndex() != null) {
            arrayFullName = paraElement.getName() + "[" + paraElement.getIndex() + "]";
        } else if (paraElement.getIndex() == null) {
            arrayFullName = paraElement.getName();
        }
        if (paraElement.getParent() != null) {
            arrayFullName = getAllParentsName(paraElement.getParent(), arrayFullName);
        }
        return arrayFullName;

    }

    public String getAllParentsName(ParameterInfo para, String arrayFullName) {
        if (para.getIndex() != null) {
            arrayFullName = para.getName() + "[" + para.getIndex() + "]" + "." + arrayFullName;
        } else if (para.getIndex() == null) {
            arrayFullName = para.getName() + "." + arrayFullName;
        }
        if (para.getParent() != null) {
            arrayFullName = getAllParentsName(para.getParent(), arrayFullName);
        }

        return arrayFullName;

    }

    public static List<ParameterInfo> getAllChildren(ParameterInfo para, String arrayFullName) {
        List<ParameterInfo> list = new ArrayList<ParameterInfo>();
        List<ParameterInfo> childList = para.getParameterInfos();
        for (ParameterInfo paraC : childList) {
            if (arrayFullName != null) {
                arrayFullName = arrayFullName + "." + paraC.getName() + paraC.getIndex() == null ? ""
                        : ("[" + paraC.getIndex() + "]");
            }
        }
        list.addAll(childList);
        for (ParameterInfo paraC : childList) {
            if (paraC.getParameterInfos().size() > 0) {
                list.addAll(getAllChildren(paraC, arrayFullName));
            }
        }
        return list;
    }
}
