/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.talend.designer.mapper.model.emf.mapper.impl;

import java.util.Collection;
import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;
import org.talend.designer.mapper.model.emf.mapper.InputTable;
import org.talend.designer.mapper.model.emf.mapper.MapperPackage;
import org.talend.designer.mapper.model.emf.mapper.MapperTableEntry;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Input Table</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.talend.designer.mapper.model.emf.mapper.impl.InputTableImpl#getMatchingMode <em>Matching Mode</em>}</li>
 *   <li>{@link org.talend.designer.mapper.model.emf.mapper.impl.InputTableImpl#getLookupMode <em>Lookup Mode</em>}</li>
 *   <li>{@link org.talend.designer.mapper.model.emf.mapper.impl.InputTableImpl#getGlobalMapKeysValues <em>Global Map Keys Values</em>}</li>
 *   <li>{@link org.talend.designer.mapper.model.emf.mapper.impl.InputTableImpl#isInnerJoin <em>Inner Join</em>}</li>
 *   <li>{@link org.talend.designer.mapper.model.emf.mapper.impl.InputTableImpl#isPersistent <em>Persistent</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class InputTableImpl extends AbstractInOutTableImpl implements InputTable {
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
     * The cached value of the '{@link #getGlobalMapKeysValues() <em>Global Map Keys Values</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getGlobalMapKeysValues()
     * @generated
     * @ordered
     */
    protected EList<MapperTableEntry> globalMapKeysValues;

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
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected InputTableImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return MapperPackage.Literals.INPUT_TABLE;
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
            eNotify(new ENotificationImpl(this, Notification.SET, MapperPackage.INPUT_TABLE__MATCHING_MODE, oldMatchingMode, matchingMode));
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
            eNotify(new ENotificationImpl(this, Notification.SET, MapperPackage.INPUT_TABLE__LOOKUP_MODE, oldLookupMode, lookupMode));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList<MapperTableEntry> getGlobalMapKeysValues() {
        if (globalMapKeysValues == null) {
            globalMapKeysValues = new EObjectContainmentEList<MapperTableEntry>(MapperTableEntry.class, this, MapperPackage.INPUT_TABLE__GLOBAL_MAP_KEYS_VALUES);
        }
        return globalMapKeysValues;
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
            eNotify(new ENotificationImpl(this, Notification.SET, MapperPackage.INPUT_TABLE__INNER_JOIN, oldInnerJoin, innerJoin));
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
            eNotify(new ENotificationImpl(this, Notification.SET, MapperPackage.INPUT_TABLE__PERSISTENT, oldPersistent, persistent));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
        switch (featureID) {
            case MapperPackage.INPUT_TABLE__GLOBAL_MAP_KEYS_VALUES:
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
            case MapperPackage.INPUT_TABLE__MATCHING_MODE:
                return getMatchingMode();
            case MapperPackage.INPUT_TABLE__LOOKUP_MODE:
                return getLookupMode();
            case MapperPackage.INPUT_TABLE__GLOBAL_MAP_KEYS_VALUES:
                return getGlobalMapKeysValues();
            case MapperPackage.INPUT_TABLE__INNER_JOIN:
                return isInnerJoin();
            case MapperPackage.INPUT_TABLE__PERSISTENT:
                return isPersistent();
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
            case MapperPackage.INPUT_TABLE__MATCHING_MODE:
                setMatchingMode((String)newValue);
                return;
            case MapperPackage.INPUT_TABLE__LOOKUP_MODE:
                setLookupMode((String)newValue);
                return;
            case MapperPackage.INPUT_TABLE__GLOBAL_MAP_KEYS_VALUES:
                getGlobalMapKeysValues().clear();
                getGlobalMapKeysValues().addAll((Collection<? extends MapperTableEntry>)newValue);
                return;
            case MapperPackage.INPUT_TABLE__INNER_JOIN:
                setInnerJoin((Boolean)newValue);
                return;
            case MapperPackage.INPUT_TABLE__PERSISTENT:
                setPersistent((Boolean)newValue);
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
            case MapperPackage.INPUT_TABLE__MATCHING_MODE:
                setMatchingMode(MATCHING_MODE_EDEFAULT);
                return;
            case MapperPackage.INPUT_TABLE__LOOKUP_MODE:
                setLookupMode(LOOKUP_MODE_EDEFAULT);
                return;
            case MapperPackage.INPUT_TABLE__GLOBAL_MAP_KEYS_VALUES:
                getGlobalMapKeysValues().clear();
                return;
            case MapperPackage.INPUT_TABLE__INNER_JOIN:
                setInnerJoin(INNER_JOIN_EDEFAULT);
                return;
            case MapperPackage.INPUT_TABLE__PERSISTENT:
                setPersistent(PERSISTENT_EDEFAULT);
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
            case MapperPackage.INPUT_TABLE__MATCHING_MODE:
                return MATCHING_MODE_EDEFAULT == null ? matchingMode != null : !MATCHING_MODE_EDEFAULT.equals(matchingMode);
            case MapperPackage.INPUT_TABLE__LOOKUP_MODE:
                return LOOKUP_MODE_EDEFAULT == null ? lookupMode != null : !LOOKUP_MODE_EDEFAULT.equals(lookupMode);
            case MapperPackage.INPUT_TABLE__GLOBAL_MAP_KEYS_VALUES:
                return globalMapKeysValues != null && !globalMapKeysValues.isEmpty();
            case MapperPackage.INPUT_TABLE__INNER_JOIN:
                return innerJoin != INNER_JOIN_EDEFAULT;
            case MapperPackage.INPUT_TABLE__PERSISTENT:
                return persistent != PERSISTENT_EDEFAULT;
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
        result.append(" (matchingMode: ");
        result.append(matchingMode);
        result.append(", lookupMode: ");
        result.append(lookupMode);
        result.append(", innerJoin: ");
        result.append(innerJoin);
        result.append(", persistent: ");
        result.append(persistent);
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
        InputTableImpl other = (InputTableImpl) obj;
        if (this.innerJoin != other.innerJoin) {
            return false;
        }
        
        if (this.persistent != other.persistent) {
            return false;
        }
        
        if (this.lookupMode == null) {
            if (other.lookupMode != null) {
                return false;
            }
        } else if (!this.lookupMode.equals(other.lookupMode)) {
            return false;
        }
        
        if (this.matchingMode == null) {
            if (other.matchingMode != null) {
                return false;
            }
        } else if (!this.matchingMode.equals(other.matchingMode)) {
            return false;
        }
        
        EList<MapperTableEntry> otherEntries = other.getGlobalMapKeysValues();
        if(getGlobalMapKeysValues().size() != otherEntries.size()){
            return false;
        }
        for(MapperTableEntry entry:getGlobalMapKeysValues()){
            boolean found = false;
            for(MapperTableEntry otherEntry:otherEntries){
                if(entry.getName().equals(otherEntry.getName())){
                    found = true;
                    if(!entry.equals(otherEntry)){
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

} //InputTableImpl
