/**
 * <copyright> </copyright>
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
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;
import org.talend.designer.mapper.model.emf.mapper.AbstractDataMapTable;
import org.talend.designer.mapper.model.emf.mapper.MapperPackage;
import org.talend.designer.mapper.model.emf.mapper.MapperTableEntry;
import org.talend.designer.mapper.model.emf.mapper.SizeState;

/**
 * <!-- begin-user-doc --> An implementation of the model object '<em><b>Abstract Data Map Table</b></em>'. <!--
 * end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.talend.designer.mapper.model.emf.mapper.impl.AbstractDataMapTableImpl#getSizeState <em>Size State</em>}</li>
 *   <li>{@link org.talend.designer.mapper.model.emf.mapper.impl.AbstractDataMapTableImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.talend.designer.mapper.model.emf.mapper.impl.AbstractDataMapTableImpl#isMinimized <em>Minimized</em>}</li>
 *   <li>{@link org.talend.designer.mapper.model.emf.mapper.impl.AbstractDataMapTableImpl#getMapperTableEntries <em>Mapper Table Entries</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public abstract class AbstractDataMapTableImpl extends EObjectImpl implements AbstractDataMapTable {

    /**
     * The default value of the '{@link #getSizeState() <em>Size State</em>}' attribute.
     * <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * @see #getSizeState()
     * @generated
     * @ordered
     */
    protected static final SizeState SIZE_STATE_EDEFAULT = SizeState.MINIMIZED;

    /**
     * The cached value of the '{@link #getSizeState() <em>Size State</em>}' attribute.
     * <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * @see #getSizeState()
     * @generated
     * @ordered
     */
    protected SizeState sizeState = SIZE_STATE_EDEFAULT;

    /**
     * The default value of the '{@link #getName() <em>Name</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     * 
     * @see #getName()
     * @generated
     * @ordered
     */
    protected static final String NAME_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getName() <em>Name</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     * 
     * @see #getName()
     * @generated
     * @ordered
     */
    protected String name = NAME_EDEFAULT;

    /**
     * The default value of the '{@link #isMinimized() <em>Minimized</em>}' attribute.
     * <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * @see #isMinimized()
     * @generated
     * @ordered
     */
    protected static final boolean MINIMIZED_EDEFAULT = false;

    /**
     * The cached value of the '{@link #isMinimized() <em>Minimized</em>}' attribute.
     * <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * @see #isMinimized()
     * @generated
     * @ordered
     */
    protected boolean minimized = MINIMIZED_EDEFAULT;

    /**
     * The cached value of the '{@link #getMapperTableEntries() <em>Mapper Table Entries</em>}' containment reference list.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @see #getMapperTableEntries()
     * @generated
     * @ordered
     */
    protected EList<MapperTableEntry> mapperTableEntries;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    protected AbstractDataMapTableImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return MapperPackage.Literals.ABSTRACT_DATA_MAP_TABLE;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    public SizeState getSizeState() {
        return sizeState;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    public void setSizeState(SizeState newSizeState) {
        SizeState oldSizeState = sizeState;
        sizeState = newSizeState == null ? SIZE_STATE_EDEFAULT : newSizeState;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, MapperPackage.ABSTRACT_DATA_MAP_TABLE__SIZE_STATE, oldSizeState, sizeState));
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    public String getName() {
        return name;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    public void setName(String newName) {
        String oldName = name;
        name = newName;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, MapperPackage.ABSTRACT_DATA_MAP_TABLE__NAME, oldName, name));
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    public boolean isMinimized() {
        return minimized;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    public void setMinimized(boolean newMinimized) {
        boolean oldMinimized = minimized;
        minimized = newMinimized;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, MapperPackage.ABSTRACT_DATA_MAP_TABLE__MINIMIZED, oldMinimized, minimized));
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    public EList<MapperTableEntry> getMapperTableEntries() {
        if (mapperTableEntries == null) {
            mapperTableEntries = new EObjectContainmentEList<MapperTableEntry>(MapperTableEntry.class, this, MapperPackage.ABSTRACT_DATA_MAP_TABLE__MAPPER_TABLE_ENTRIES);
        }
        return mapperTableEntries;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
        switch (featureID) {
            case MapperPackage.ABSTRACT_DATA_MAP_TABLE__MAPPER_TABLE_ENTRIES:
                return ((InternalEList<?>)getMapperTableEntries()).basicRemove(otherEnd, msgs);
        }
        return super.eInverseRemove(otherEnd, featureID, msgs);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public Object eGet(int featureID, boolean resolve, boolean coreType) {
        switch (featureID) {
            case MapperPackage.ABSTRACT_DATA_MAP_TABLE__SIZE_STATE:
                return getSizeState();
            case MapperPackage.ABSTRACT_DATA_MAP_TABLE__NAME:
                return getName();
            case MapperPackage.ABSTRACT_DATA_MAP_TABLE__MINIMIZED:
                return isMinimized();
            case MapperPackage.ABSTRACT_DATA_MAP_TABLE__MAPPER_TABLE_ENTRIES:
                return getMapperTableEntries();
        }
        return super.eGet(featureID, resolve, coreType);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @SuppressWarnings("unchecked")
    @Override
    public void eSet(int featureID, Object newValue) {
        switch (featureID) {
            case MapperPackage.ABSTRACT_DATA_MAP_TABLE__SIZE_STATE:
                setSizeState((SizeState)newValue);
                return;
            case MapperPackage.ABSTRACT_DATA_MAP_TABLE__NAME:
                setName((String)newValue);
                return;
            case MapperPackage.ABSTRACT_DATA_MAP_TABLE__MINIMIZED:
                setMinimized((Boolean)newValue);
                return;
            case MapperPackage.ABSTRACT_DATA_MAP_TABLE__MAPPER_TABLE_ENTRIES:
                getMapperTableEntries().clear();
                getMapperTableEntries().addAll((Collection<? extends MapperTableEntry>)newValue);
                return;
        }
        super.eSet(featureID, newValue);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public void eUnset(int featureID) {
        switch (featureID) {
            case MapperPackage.ABSTRACT_DATA_MAP_TABLE__SIZE_STATE:
                setSizeState(SIZE_STATE_EDEFAULT);
                return;
            case MapperPackage.ABSTRACT_DATA_MAP_TABLE__NAME:
                setName(NAME_EDEFAULT);
                return;
            case MapperPackage.ABSTRACT_DATA_MAP_TABLE__MINIMIZED:
                setMinimized(MINIMIZED_EDEFAULT);
                return;
            case MapperPackage.ABSTRACT_DATA_MAP_TABLE__MAPPER_TABLE_ENTRIES:
                getMapperTableEntries().clear();
                return;
        }
        super.eUnset(featureID);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public boolean eIsSet(int featureID) {
        switch (featureID) {
            case MapperPackage.ABSTRACT_DATA_MAP_TABLE__SIZE_STATE:
                return sizeState != SIZE_STATE_EDEFAULT;
            case MapperPackage.ABSTRACT_DATA_MAP_TABLE__NAME:
                return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
            case MapperPackage.ABSTRACT_DATA_MAP_TABLE__MINIMIZED:
                return minimized != MINIMIZED_EDEFAULT;
            case MapperPackage.ABSTRACT_DATA_MAP_TABLE__MAPPER_TABLE_ENTRIES:
                return mapperTableEntries != null && !mapperTableEntries.isEmpty();
        }
        return super.eIsSet(featureID);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public String toString() {
        if (eIsProxy()) return super.toString();

        StringBuffer result = new StringBuffer(super.toString());
        result.append(" (sizeState: ");
        result.append(sizeState);
        result.append(", name: ");
        result.append(name);
        result.append(", minimized: ");
        result.append(minimized);
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
        AbstractDataMapTableImpl other = (AbstractDataMapTableImpl) obj;
        if(this.minimized != other.minimized){
            return false;
        }
        if (this.name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!this.name.equals(other.name)) {
            return false;
        }
        if (this.sizeState == null) {
            if (other.sizeState != null) {
                return false;
            }
        } else if (!this.sizeState.getName().equals(other.sizeState.getName())) {
            return false;
        }else if (this.sizeState.getValue() != other.sizeState.getValue()) {
            return false;
        }else if (!this.sizeState.getLiteral().equals(other.sizeState.getLiteral())) {
            return false;
        }
        
        EList<MapperTableEntry> otherEntries = other.getMapperTableEntries();
        if(getMapperTableEntries().size() != otherEntries.size()){
            return false;
        }
        for(MapperTableEntry entry:mapperTableEntries){
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
        return true;
    }
} // AbstractDataMapTableImpl
