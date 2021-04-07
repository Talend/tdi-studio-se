/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.talend.designer.dbmap.model.emf.dbmap.impl;

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

import org.talend.designer.dbmap.model.emf.dbmap.AbstractDBDataMapTable;
import org.talend.designer.dbmap.model.emf.dbmap.DBMapperTableEntry;
import org.talend.designer.dbmap.model.emf.dbmap.DbmapPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Abstract DB Data Map Table</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.talend.designer.dbmap.model.emf.dbmap.impl.AbstractDBDataMapTableImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.talend.designer.dbmap.model.emf.dbmap.impl.AbstractDBDataMapTableImpl#isMinimized <em>Minimized</em>}</li>
 *   <li>{@link org.talend.designer.dbmap.model.emf.dbmap.impl.AbstractDBDataMapTableImpl#isReadonly <em>Readonly</em>}</li>
 *   <li>{@link org.talend.designer.dbmap.model.emf.dbmap.impl.AbstractDBDataMapTableImpl#getDBMapperTableEntries <em>DB Mapper Table Entries</em>}</li>
 *   <li>{@link org.talend.designer.dbmap.model.emf.dbmap.impl.AbstractDBDataMapTableImpl#getTableName <em>Table Name</em>}</li>
 * </ul>
 *
 * @generated
 */
public class AbstractDBDataMapTableImpl extends EObjectImpl implements AbstractDBDataMapTable {
    /**
     * The default value of the '{@link #getName() <em>Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getName()
     * @generated
     * @ordered
     */
    protected static final String NAME_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getName() <em>Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getName()
     * @generated
     * @ordered
     */
    protected String name = NAME_EDEFAULT;

    /**
     * The default value of the '{@link #isMinimized() <em>Minimized</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isMinimized()
     * @generated
     * @ordered
     */
    protected static final boolean MINIMIZED_EDEFAULT = false;

    /**
     * The cached value of the '{@link #isMinimized() <em>Minimized</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isMinimized()
     * @generated
     * @ordered
     */
    protected boolean minimized = MINIMIZED_EDEFAULT;

    /**
     * The default value of the '{@link #isReadonly() <em>Readonly</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isReadonly()
     * @generated
     * @ordered
     */
    protected static final boolean READONLY_EDEFAULT = false;

    /**
     * The cached value of the '{@link #isReadonly() <em>Readonly</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isReadonly()
     * @generated
     * @ordered
     */
    protected boolean readonly = READONLY_EDEFAULT;

    /**
     * The cached value of the '{@link #getDBMapperTableEntries() <em>DB Mapper Table Entries</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getDBMapperTableEntries()
     * @generated
     * @ordered
     */
    protected EList<DBMapperTableEntry> dbMapperTableEntries;

    /**
     * The default value of the '{@link #getTableName() <em>Table Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getTableName()
     * @generated
     * @ordered
     */
    protected static final String TABLE_NAME_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getTableName() <em>Table Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getTableName()
     * @generated
     * @ordered
     */
    protected String tableName = TABLE_NAME_EDEFAULT;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected AbstractDBDataMapTableImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return DbmapPackage.Literals.ABSTRACT_DB_DATA_MAP_TABLE;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getName() {
        return name;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setName(String newName) {
        String oldName = name;
        name = newName;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, DbmapPackage.ABSTRACT_DB_DATA_MAP_TABLE__NAME, oldName, name));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean isMinimized() {
        return minimized;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setMinimized(boolean newMinimized) {
        boolean oldMinimized = minimized;
        minimized = newMinimized;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, DbmapPackage.ABSTRACT_DB_DATA_MAP_TABLE__MINIMIZED, oldMinimized, minimized));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean isReadonly() {
        return readonly;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setReadonly(boolean newReadonly) {
        boolean oldReadonly = readonly;
        readonly = newReadonly;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, DbmapPackage.ABSTRACT_DB_DATA_MAP_TABLE__READONLY, oldReadonly, readonly));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList<DBMapperTableEntry> getDBMapperTableEntries() {
        if (dbMapperTableEntries == null) {
            dbMapperTableEntries = new EObjectContainmentEList<DBMapperTableEntry>(DBMapperTableEntry.class, this, DbmapPackage.ABSTRACT_DB_DATA_MAP_TABLE__DB_MAPPER_TABLE_ENTRIES);
        }
        return dbMapperTableEntries;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setTableName(String newTableName) {
        String oldTableName = tableName;
        tableName = newTableName;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, DbmapPackage.ABSTRACT_DB_DATA_MAP_TABLE__TABLE_NAME, oldTableName, tableName));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
        switch (featureID) {
            case DbmapPackage.ABSTRACT_DB_DATA_MAP_TABLE__DB_MAPPER_TABLE_ENTRIES:
                return ((InternalEList<?>)getDBMapperTableEntries()).basicRemove(otherEnd, msgs);
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
            case DbmapPackage.ABSTRACT_DB_DATA_MAP_TABLE__NAME:
                return getName();
            case DbmapPackage.ABSTRACT_DB_DATA_MAP_TABLE__MINIMIZED:
                return isMinimized();
            case DbmapPackage.ABSTRACT_DB_DATA_MAP_TABLE__READONLY:
                return isReadonly();
            case DbmapPackage.ABSTRACT_DB_DATA_MAP_TABLE__DB_MAPPER_TABLE_ENTRIES:
                return getDBMapperTableEntries();
            case DbmapPackage.ABSTRACT_DB_DATA_MAP_TABLE__TABLE_NAME:
                return getTableName();
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
            case DbmapPackage.ABSTRACT_DB_DATA_MAP_TABLE__NAME:
                setName((String)newValue);
                return;
            case DbmapPackage.ABSTRACT_DB_DATA_MAP_TABLE__MINIMIZED:
                setMinimized((Boolean)newValue);
                return;
            case DbmapPackage.ABSTRACT_DB_DATA_MAP_TABLE__READONLY:
                setReadonly((Boolean)newValue);
                return;
            case DbmapPackage.ABSTRACT_DB_DATA_MAP_TABLE__DB_MAPPER_TABLE_ENTRIES:
                getDBMapperTableEntries().clear();
                getDBMapperTableEntries().addAll((Collection<? extends DBMapperTableEntry>)newValue);
                return;
            case DbmapPackage.ABSTRACT_DB_DATA_MAP_TABLE__TABLE_NAME:
                setTableName((String)newValue);
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
            case DbmapPackage.ABSTRACT_DB_DATA_MAP_TABLE__NAME:
                setName(NAME_EDEFAULT);
                return;
            case DbmapPackage.ABSTRACT_DB_DATA_MAP_TABLE__MINIMIZED:
                setMinimized(MINIMIZED_EDEFAULT);
                return;
            case DbmapPackage.ABSTRACT_DB_DATA_MAP_TABLE__READONLY:
                setReadonly(READONLY_EDEFAULT);
                return;
            case DbmapPackage.ABSTRACT_DB_DATA_MAP_TABLE__DB_MAPPER_TABLE_ENTRIES:
                getDBMapperTableEntries().clear();
                return;
            case DbmapPackage.ABSTRACT_DB_DATA_MAP_TABLE__TABLE_NAME:
                setTableName(TABLE_NAME_EDEFAULT);
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
            case DbmapPackage.ABSTRACT_DB_DATA_MAP_TABLE__NAME:
                return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
            case DbmapPackage.ABSTRACT_DB_DATA_MAP_TABLE__MINIMIZED:
                return minimized != MINIMIZED_EDEFAULT;
            case DbmapPackage.ABSTRACT_DB_DATA_MAP_TABLE__READONLY:
                return readonly != READONLY_EDEFAULT;
            case DbmapPackage.ABSTRACT_DB_DATA_MAP_TABLE__DB_MAPPER_TABLE_ENTRIES:
                return dbMapperTableEntries != null && !dbMapperTableEntries.isEmpty();
            case DbmapPackage.ABSTRACT_DB_DATA_MAP_TABLE__TABLE_NAME:
                return TABLE_NAME_EDEFAULT == null ? tableName != null : !TABLE_NAME_EDEFAULT.equals(tableName);
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

        StringBuilder result = new StringBuilder(super.toString());
        result.append(" (name: ");
        result.append(name);
        result.append(", minimized: ");
        result.append(minimized);
        result.append(", readonly: ");
        result.append(readonly);
        result.append(", tableName: ");
        result.append(tableName);
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
        AbstractDBDataMapTableImpl other = (AbstractDBDataMapTableImpl) obj;
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
        
        if (this.tableName == null) {
            if (other.tableName != null) {
                return false;
            }
        } else if (!this.tableName.equals(other.tableName)) {
            return false;
        }
        
        EList<DBMapperTableEntry> otherEntriss = other.getDBMapperTableEntries();
        if(getDBMapperTableEntries().size() != otherEntriss.size()){
            return false;
        }
        for(DBMapperTableEntry entry:dbMapperTableEntries){
            boolean found = false;
            for(DBMapperTableEntry otherEntry:otherEntriss){
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

} //AbstractDBDataMapTableImpl
