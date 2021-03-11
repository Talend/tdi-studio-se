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
package org.talend.designer.core.ui.editor.process;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.emf.common.util.EList;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.metadata.builder.connection.Concept;
import org.talend.core.model.metadata.builder.connection.HL7Connection;
import org.talend.core.model.metadata.builder.connection.MDMConnection;
import org.talend.core.model.metadata.builder.connection.MdmConceptType;
import org.talend.core.model.metadata.builder.connection.WSDLSchemaConnection;
import org.talend.core.model.metadata.builder.connection.XmlFileConnection;
import org.talend.core.model.properties.HL7ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.MDMConnectionItem;
import org.talend.core.model.properties.WSDLSchemaConnectionItem;
import org.talend.core.model.properties.XmlFileConnectionItem;
import org.talend.core.model.properties.impl.XmlFileConnectionItemImpl;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.repositoryObject.MetadataTableRepositoryObject;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.repository.model.RepositoryNode;

/**
 * ggu class global comment. Detailled comment
 *
 * @deprecated replaced by RepositoryComponentManager
 */

@Deprecated
final class TalendDndHelper {

    private static final String MAP = "MAP";//$NON-NLS-1$

    private static final String OUTPUT = "Output"; //$NON-NLS-1$

    private static final String INPUT = "Input"; //$NON-NLS-1$

    private static final String RECEIVE = "Receive"; //$NON-NLS-1$

    public static List<IComponent> filterNeededComponents(Item item, RepositoryNode seletetedNode, ERepositoryObjectType type) {
        EDatabaseComponentName name = EDatabaseComponentName.getCorrespondingComponentName(item, type);
        List<IComponent> neededComponents = new ArrayList<IComponent>();
        if (name == null) {
            return neededComponents;
        }
        String productNameWanted = filterProductNameWanted(name, item);
        boolean hl7Related = false;
        boolean hl7Output = false;
        if (item instanceof HL7ConnectionItem) {
            hl7Related = true;
            EList list = ((HL7Connection) ((HL7ConnectionItem) item).getConnection()).getRoot();
            if (list != null && list.size() > 0) {
                hl7Output = true;
            }
        }

        // for mdm
        MdmConceptType mdmType = null;
        if (item instanceof MDMConnectionItem) {
            MDMConnectionItem mdmItem = (MDMConnectionItem) item;
            if (seletetedNode != null && seletetedNode.getObject() instanceof MetadataTableRepositoryObject) {
                MetadataTableRepositoryObject object = (MetadataTableRepositoryObject) seletetedNode.getObject();
                if (mdmItem.getConnection() instanceof MDMConnection) {
                    MDMConnection connection = (MDMConnection) mdmItem.getConnection();
                    for (Object obj : connection.getSchemas()) {
                        if (obj instanceof Concept && object.getLabel().equals(((Concept) obj).getLabel())) {
                            mdmType = ((Concept) obj).getConceptType();
                        }

                    }
                }
            }
        }

        Collection<IComponent> components = ComponentsFactoryProvider.getInstance().readComponents();

        EmfComponent emfComponent = null;
        for (IComponent component : components) {
            if (component instanceof EmfComponent) {
                emfComponent = (EmfComponent) component;
                String componentProductname = emfComponent.getRepositoryType();

                boolean value = true;
                if (type == ERepositoryObjectType.METADATA_CON_TABLE) {
                    if (emfComponent.getName().toUpperCase().endsWith(MAP)) {
                        value = false;
                    }
                }
                if (hl7Output && !component.getName().equals("tHL7Output")) { //$NON-NLS-1$
                    value = false;
                } else if (hl7Related && !hl7Output && !component.getName().equals("tHL7Input")) {//$NON-NLS-N$ bug15632
                    value = false;
                }

                boolean flag = filterComponent(component, name, type);

                if (((componentProductname != null && productNameWanted.endsWith(componentProductname)) && value) || flag) {

                    Pattern pattern = Pattern.compile("^.*oracle.*$", Pattern.CASE_INSENSITIVE);

                    if (pattern.matcher(name.getDBType()).matches()
                            && (emfComponent.getName().equals("tAmazonOracleInput") || emfComponent.getName().equals(
                                    "tAmazonOracleOutput"))) {

                        continue;
                    }

                    if (item instanceof MDMConnectionItem) {
                        if (MdmConceptType.INPUT.equals(mdmType) && emfComponent.getName().endsWith(INPUT)) {
                            neededComponents.add(emfComponent);
                        } else if (MdmConceptType.OUTPUT.equals(mdmType) && emfComponent.getName().endsWith(OUTPUT)) {
                            neededComponents.add(emfComponent);
                        } else if (MdmConceptType.RECEIVE.equals(mdmType) && emfComponent.getName().endsWith(RECEIVE)) {
                            neededComponents.add(emfComponent);
                        }

                    } else {
                        neededComponents.add(emfComponent);
                    }
                }
            }
        }
        return sortFilteredComponnents(item, seletetedNode, type, neededComponents);
    }

    private static String filterProductNameWanted(EDatabaseComponentName name, Item item) {
        String productNameWanted = name.getProductName();

        if (item instanceof XmlFileConnectionItem) {
            XmlFileConnection connection = (XmlFileConnection) ((XmlFileConnectionItemImpl) item).getConnection();
            if (!connection.isInputModel()) {
                productNameWanted = "XMLOUTPUT"; //$NON-NLS-1$
            }
        }
        if (item instanceof WSDLSchemaConnectionItem) {
            WSDLSchemaConnection connection = (WSDLSchemaConnection) ((WSDLSchemaConnectionItem) item).getConnection();
            if (!connection.isIsInputModel()) {
                productNameWanted = "WEBSERVICE"; //$NON-NLS-1$
            }
        }
        return productNameWanted;
    }

    private static boolean filterComponent(IComponent component, EDatabaseComponentName name, ERepositoryObjectType type) {
        if (component != null && name != null && type != null) {
            String originalFamilyName = component.getOriginalFamilyName();
            if (originalFamilyName.startsWith("ELT")) { //$NON-NLS-1$
                String dbType = name.getDBType();
                switch (name) {
                case DBORACLEFORSID:
                case DBORACLESN:
                    dbType = EDatabaseTypeName.ORACLEFORSID.getProduct();
                }
                if (dbType != null && originalFamilyName.toUpperCase().endsWith(dbType.toUpperCase())) {
                    if (type == ERepositoryObjectType.METADATA_CONNECTIONS) {
                        if (component.getName().toUpperCase().endsWith(MAP)) {
                            return true;
                        }
                    }
                    if (type == ERepositoryObjectType.METADATA_CON_TABLE) {
                        if (!component.getName().toUpperCase().endsWith(MAP)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private static List<IComponent> sortFilteredComponnents(Item item, RepositoryNode seletetedNode, ERepositoryObjectType type,
            List<IComponent> neededComponents) {

        List<IComponent> normalTopComponents = new ArrayList<IComponent>();
        List<IComponent> specialTopComponents = new ArrayList<IComponent>();

        for (IComponent component : neededComponents) {
            String name = component.getName();
            if (name.contains(OUTPUT) || name.contains(INPUT)) {
                normalTopComponents.add(component);
            } else if (isSpecialTop(item, seletetedNode, type, component)) {
                specialTopComponents.add(component);
            }
        }

        List<IComponent> sortedComponents = new ArrayList<IComponent>();
        sortedComponents.addAll(specialTopComponents);
        sortedComponents.addAll(normalTopComponents);

        // add the left components
        neededComponents.removeAll(specialTopComponents);
        neededComponents.removeAll(normalTopComponents);
        sortedComponents.addAll(neededComponents);

        return sortedComponents;
    }

    private static boolean isSpecialTop(Item item, RepositoryNode seletetedNode, ERepositoryObjectType type, IComponent component) {
        // for MPx component
        if (component.getName().startsWith("tFS")) { //$NON-NLS-1$
            return true;
        }
        return false;
    }
}
