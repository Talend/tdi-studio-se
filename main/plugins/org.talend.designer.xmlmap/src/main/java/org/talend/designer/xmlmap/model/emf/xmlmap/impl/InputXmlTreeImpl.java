/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.talend.designer.xmlmap.model.emf.xmlmap.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.talend.designer.xmlmap.model.emf.xmlmap.GlobalMapNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.AbstractNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.InputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.TreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlmapPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Input Xml Tree</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.talend.designer.xmlmap.model.emf.xmlmap.impl.InputXmlTreeImpl#getNodes <em>Nodes</em>}</li>
 *   <li>{@link org.talend.designer.xmlmap.model.emf.xmlmap.impl.InputXmlTreeImpl#isLookup <em>Lookup</em>}</li>
 *   <li>{@link org.talend.designer.xmlmap.model.emf.xmlmap.impl.InputXmlTreeImpl#getMatchingMode <em>Matching Mode</em>}</li>
 *   <li>{@link org.talend.designer.xmlmap.model.emf.xmlmap.impl.InputXmlTreeImpl#getLookupMode <em>Lookup Mode</em>}</li>
 *   <li>{@link org.talend.designer.xmlmap.model.emf.xmlmap.impl.InputXmlTreeImpl#isInnerJoin <em>Inner Join</em>}</li>
 *   <li>{@link org.talend.designer.xmlmap.model.emf.xmlmap.impl.InputXmlTreeImpl#isPersistent <em>Persistent</em>}</li>
 *   <li>{@link org.talend.designer.xmlmap.model.emf.xmlmap.impl.InputXmlTreeImpl#isActivateGlobalMap <em>Activate Global Map</em>}</li>
 *   <li>{@link org.talend.designer.xmlmap.model.emf.xmlmap.impl.InputXmlTreeImpl#getGlobalMapKeysValues <em>Global Map Keys Values</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class InputXmlTreeImpl extends AbstractInOutTreeImpl implements InputXmlTree {
    /**
     * The cached value of the '{@link #getNodes() <em>Nodes</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getNodes()
     * @generated
     * @ordered
     */
    protected EList<TreeNode> nodes;

    /**
     * The default value of the '{@link #isLookup() <em>Lookup</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isLookup()
     * @generated
     * @ordered
     */
    protected static final boolean LOOKUP_EDEFAULT = false;

    /**
     * The cached value of the '{@link #isLookup() <em>Lookup</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isLookup()
     * @generated
     * @ordered
     */
    protected boolean lookup = LOOKUP_EDEFAULT;

    /**
     * The default value of the '{@link #getMatchingMode() <em>Matching Mode</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getMatchingMode()
     * @generated
     * @ordered
     */
    protected static final String MATCHING_MODE_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getMatchingMode() <em>Matching Mode</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getMatchingMode()
     * @generated
     * @ordered
     */
    protected String matchingMode = MATCHING_MODE_EDEFAULT;

    /**
     * The default value of the '{@link #getLookupMode() <em>Lookup Mode</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getLookupMode()
     * @generated
     * @ordered
     */
    protected static final String LOOKUP_MODE_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getLookupMode() <em>Lookup Mode</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getLookupMode()
     * @generated
     * @ordered
     */
    protected String lookupMode = LOOKUP_MODE_EDEFAULT;

    /**
     * The default value of the '{@link #isInnerJoin() <em>Inner Join</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isInnerJoin()
     * @generated
     * @ordered
     */
    protected static final boolean INNER_JOIN_EDEFAULT = false;

    /**
     * The cached value of the '{@link #isInnerJoin() <em>Inner Join</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isInnerJoin()
     * @generated
     * @ordered
     */
    protected boolean innerJoin = INNER_JOIN_EDEFAULT;

    /**
     * The default value of the '{@link #isPersistent() <em>Persistent</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isPersistent()
     * @generated
     * @ordered
     */
    protected static final boolean PERSISTENT_EDEFAULT = false;

    /**
     * The cached value of the '{@link #isPersistent() <em>Persistent</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isPersistent()
     * @generated
     * @ordered
     */
    protected boolean persistent = PERSISTENT_EDEFAULT;

    /**
     * The default value of the '{@link #isActivateGlobalMap() <em>Activate Global Map</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isActivateGlobalMap()
     * @generated
     * @ordered
     */
    protected static final boolean ACTIVATE_GLOBAL_MAP_EDEFAULT = false;

    /**
     * The cached value of the '{@link #isActivateGlobalMap() <em>Activate Global Map</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isActivateGlobalMap()
     * @generated
     * @ordered
     */
    protected boolean activateGlobalMap = ACTIVATE_GLOBAL_MAP_EDEFAULT;

    /**
     * The cached value of the '{@link #getGlobalMapKeysValues() <em>Global Map Keys Values</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getGlobalMapKeysValues()
     * @generated
     * @ordered
     */
    protected EList<GlobalMapNode> globalMapKeysValues;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected InputXmlTreeImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return XmlmapPackage.Literals.INPUT_XML_TREE;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList<TreeNode> getNodes() {
        if (nodes == null) {
            nodes = new EObjectContainmentEList<TreeNode>(TreeNode.class, this, XmlmapPackage.INPUT_XML_TREE__NODES);
        }
        return nodes;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean isLookup() {
        return lookup;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setLookup(boolean newLookup) {
        boolean oldLookup = lookup;
        lookup = newLookup;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, XmlmapPackage.INPUT_XML_TREE__LOOKUP, oldLookup, lookup));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getMatchingMode() {
        return matchingMode;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setMatchingMode(String newMatchingMode) {
        String oldMatchingMode = matchingMode;
        matchingMode = newMatchingMode;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, XmlmapPackage.INPUT_XML_TREE__MATCHING_MODE, oldMatchingMode, matchingMode));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getLookupMode() {
        return lookupMode;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setLookupMode(String newLookupMode) {
        String oldLookupMode = lookupMode;
        lookupMode = newLookupMode;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, XmlmapPackage.INPUT_XML_TREE__LOOKUP_MODE, oldLookupMode, lookupMode));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean isInnerJoin() {
        return innerJoin;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setInnerJoin(boolean newInnerJoin) {
        boolean oldInnerJoin = innerJoin;
        innerJoin = newInnerJoin;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, XmlmapPackage.INPUT_XML_TREE__INNER_JOIN, oldInnerJoin, innerJoin));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean isPersistent() {
        return persistent;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setPersistent(boolean newPersistent) {
        boolean oldPersistent = persistent;
        persistent = newPersistent;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, XmlmapPackage.INPUT_XML_TREE__PERSISTENT, oldPersistent, persistent));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean isActivateGlobalMap() {
        return activateGlobalMap;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setActivateGlobalMap(boolean newActivateGlobalMap) {
        boolean oldActivateGlobalMap = activateGlobalMap;
        activateGlobalMap = newActivateGlobalMap;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, XmlmapPackage.INPUT_XML_TREE__ACTIVATE_GLOBAL_MAP, oldActivateGlobalMap, activateGlobalMap));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList<GlobalMapNode> getGlobalMapKeysValues() {
        if (globalMapKeysValues == null) {
            globalMapKeysValues = new EObjectContainmentEList<GlobalMapNode>(GlobalMapNode.class, this, XmlmapPackage.INPUT_XML_TREE__GLOBAL_MAP_KEYS_VALUES);
        }
        return globalMapKeysValues;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
        switch (featureID) {
            case XmlmapPackage.INPUT_XML_TREE__NODES:
                return ((InternalEList<?>)getNodes()).basicRemove(otherEnd, msgs);
            case XmlmapPackage.INPUT_XML_TREE__GLOBAL_MAP_KEYS_VALUES:
                return ((InternalEList<?>)getGlobalMapKeysValues()).basicRemove(otherEnd, msgs);
        }
        return super.eInverseRemove(otherEnd, featureID, msgs);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public Object eGet(int featureID, boolean resolve, boolean coreType) {
        switch (featureID) {
            case XmlmapPackage.INPUT_XML_TREE__NODES:
                return getNodes();
            case XmlmapPackage.INPUT_XML_TREE__LOOKUP:
                return isLookup();
            case XmlmapPackage.INPUT_XML_TREE__MATCHING_MODE:
                return getMatchingMode();
            case XmlmapPackage.INPUT_XML_TREE__LOOKUP_MODE:
                return getLookupMode();
            case XmlmapPackage.INPUT_XML_TREE__INNER_JOIN:
                return isInnerJoin();
            case XmlmapPackage.INPUT_XML_TREE__PERSISTENT:
                return isPersistent();
            case XmlmapPackage.INPUT_XML_TREE__ACTIVATE_GLOBAL_MAP:
                return isActivateGlobalMap();
            case XmlmapPackage.INPUT_XML_TREE__GLOBAL_MAP_KEYS_VALUES:
                return getGlobalMapKeysValues();
        }
        return super.eGet(featureID, resolve, coreType);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @SuppressWarnings("unchecked")
    @Override
    public void eSet(int featureID, Object newValue) {
        switch (featureID) {
            case XmlmapPackage.INPUT_XML_TREE__NODES:
                getNodes().clear();
                getNodes().addAll((Collection<? extends TreeNode>)newValue);
                return;
            case XmlmapPackage.INPUT_XML_TREE__LOOKUP:
                setLookup((Boolean)newValue);
                return;
            case XmlmapPackage.INPUT_XML_TREE__MATCHING_MODE:
                setMatchingMode((String)newValue);
                return;
            case XmlmapPackage.INPUT_XML_TREE__LOOKUP_MODE:
                setLookupMode((String)newValue);
                return;
            case XmlmapPackage.INPUT_XML_TREE__INNER_JOIN:
                setInnerJoin((Boolean)newValue);
                return;
            case XmlmapPackage.INPUT_XML_TREE__PERSISTENT:
                setPersistent((Boolean)newValue);
                return;
            case XmlmapPackage.INPUT_XML_TREE__ACTIVATE_GLOBAL_MAP:
                setActivateGlobalMap((Boolean)newValue);
                return;
            case XmlmapPackage.INPUT_XML_TREE__GLOBAL_MAP_KEYS_VALUES:
                getGlobalMapKeysValues().clear();
                getGlobalMapKeysValues().addAll((Collection<? extends GlobalMapNode>)newValue);
                return;
        }
        super.eSet(featureID, newValue);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public void eUnset(int featureID) {
        switch (featureID) {
            case XmlmapPackage.INPUT_XML_TREE__NODES:
                getNodes().clear();
                return;
            case XmlmapPackage.INPUT_XML_TREE__LOOKUP:
                setLookup(LOOKUP_EDEFAULT);
                return;
            case XmlmapPackage.INPUT_XML_TREE__MATCHING_MODE:
                setMatchingMode(MATCHING_MODE_EDEFAULT);
                return;
            case XmlmapPackage.INPUT_XML_TREE__LOOKUP_MODE:
                setLookupMode(LOOKUP_MODE_EDEFAULT);
                return;
            case XmlmapPackage.INPUT_XML_TREE__INNER_JOIN:
                setInnerJoin(INNER_JOIN_EDEFAULT);
                return;
            case XmlmapPackage.INPUT_XML_TREE__PERSISTENT:
                setPersistent(PERSISTENT_EDEFAULT);
                return;
            case XmlmapPackage.INPUT_XML_TREE__ACTIVATE_GLOBAL_MAP:
                setActivateGlobalMap(ACTIVATE_GLOBAL_MAP_EDEFAULT);
                return;
            case XmlmapPackage.INPUT_XML_TREE__GLOBAL_MAP_KEYS_VALUES:
                getGlobalMapKeysValues().clear();
                return;
        }
        super.eUnset(featureID);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public boolean eIsSet(int featureID) {
        switch (featureID) {
            case XmlmapPackage.INPUT_XML_TREE__NODES:
                return nodes != null && !nodes.isEmpty();
            case XmlmapPackage.INPUT_XML_TREE__LOOKUP:
                return lookup != LOOKUP_EDEFAULT;
            case XmlmapPackage.INPUT_XML_TREE__MATCHING_MODE:
                return MATCHING_MODE_EDEFAULT == null ? matchingMode != null : !MATCHING_MODE_EDEFAULT.equals(matchingMode);
            case XmlmapPackage.INPUT_XML_TREE__LOOKUP_MODE:
                return LOOKUP_MODE_EDEFAULT == null ? lookupMode != null : !LOOKUP_MODE_EDEFAULT.equals(lookupMode);
            case XmlmapPackage.INPUT_XML_TREE__INNER_JOIN:
                return innerJoin != INNER_JOIN_EDEFAULT;
            case XmlmapPackage.INPUT_XML_TREE__PERSISTENT:
                return persistent != PERSISTENT_EDEFAULT;
            case XmlmapPackage.INPUT_XML_TREE__ACTIVATE_GLOBAL_MAP:
                return activateGlobalMap != ACTIVATE_GLOBAL_MAP_EDEFAULT;
            case XmlmapPackage.INPUT_XML_TREE__GLOBAL_MAP_KEYS_VALUES:
                return globalMapKeysValues != null && !globalMapKeysValues.isEmpty();
        }
        return super.eIsSet(featureID);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public String toString() {
        if (eIsProxy()) return super.toString();

        StringBuffer result = new StringBuffer(super.toString());
        result.append(" (lookup: ");
        result.append(lookup);
        result.append(", matchingMode: ");
        result.append(matchingMode);
        result.append(", lookupMode: ");
        result.append(lookupMode);
        result.append(", innerJoin: ");
        result.append(innerJoin);
        result.append(", persistent: ");
        result.append(persistent);
        result.append(", activateGlobalMap: ");
        result.append(activateGlobalMap);
        result.append(')');
        return result.toString();
    }
    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated not
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        InputXmlTreeImpl other = (InputXmlTreeImpl) obj;
        if (this.matchingMode == null) {
            if (other.matchingMode != null) {
                return false;
            }
        } else if (!this.matchingMode.equals(other.matchingMode)) {
            return false;
        }
        if (this.lookupMode == null) {
            if (other.lookupMode != null) {
                return false;
            }
        } else if (!this.lookupMode.equals(other.lookupMode)) {
            return false;
        }
        if (this.lookup != other.lookup) {
            return false;
        }
        if (this.innerJoin != other.innerJoin) {
            return false;
        }
        if (this.persistent != other.persistent) {
            return false;
        }
        if (this.activateGlobalMap != other.activateGlobalMap) {
            return false;
        }
        
        if(this.getNodes().size() != other.getNodes().size()){
            return false;
        }
        for(TreeNode inputTable:nodes){
            boolean found = false;
            for(TreeNode input:other.nodes){
                if(inputTable.getName().equals(input.getName())){
                    found = true;
                    if(!inputTable.equals(input)){
                        return false;
                    }
                    break;
                }
            }
            if(found == false){
                return false;
            }
        }
        
        if(this.getGlobalMapKeysValues().size() != other.getGlobalMapKeysValues().size()){
            return false;
        }
        for(GlobalMapNode inputTable:globalMapKeysValues){
            boolean found = false;
            for(GlobalMapNode input:other.globalMapKeysValues){
                if(inputTable.getName().equals(input.getName())){
                    found = true;
                    if(!inputTable.equals(input)){
                        return false;
                    }
                    break;
                }
            }
            if(found == false){
                return false;
            }
        }
        return super.equals(obj);
    }

} //InputXmlTreeImpl
