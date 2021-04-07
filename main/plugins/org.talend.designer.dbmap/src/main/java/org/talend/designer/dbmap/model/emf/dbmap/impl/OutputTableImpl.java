/**
 * <copyright> </copyright>
 *
 * $Id$
 */
package org.talend.designer.dbmap.model.emf.dbmap.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;
import org.talend.designer.dbmap.model.emf.dbmap.DBMapperTableEntry;
import org.talend.designer.dbmap.model.emf.dbmap.DbmapPackage;
import org.talend.designer.dbmap.model.emf.dbmap.FilterEntry;
import org.talend.designer.dbmap.model.emf.dbmap.OutputTable;

/**
 * <!-- begin-user-doc --> An implementation of the model object '<em><b>Output Table</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.talend.designer.dbmap.model.emf.dbmap.impl.OutputTableImpl#getFilterEntries <em>Filter Entries</em>}</li>
 * </ul>
 *
 * @generated
 */
public class OutputTableImpl extends AbstaceDBInOutTableImpl implements OutputTable {

    /**
     * The cached value of the '{@link #getFilterEntries() <em>Filter Entries</em>}' containment reference list. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #getFilterEntries()
     * @generated
     * @ordered
     */
    protected EList<FilterEntry> filterEntries;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    protected OutputTableImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return DbmapPackage.Literals.OUTPUT_TABLE;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EList<FilterEntry> getFilterEntries() {
        if (filterEntries == null) {
            filterEntries = new EObjectContainmentEList<FilterEntry>(FilterEntry.class, this, DbmapPackage.OUTPUT_TABLE__FILTER_ENTRIES);
        }
        return filterEntries;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
        switch (featureID) {
            case DbmapPackage.OUTPUT_TABLE__FILTER_ENTRIES:
                return ((InternalEList<?>)getFilterEntries()).basicRemove(otherEnd, msgs);
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
            case DbmapPackage.OUTPUT_TABLE__FILTER_ENTRIES:
                return getFilterEntries();
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
            case DbmapPackage.OUTPUT_TABLE__FILTER_ENTRIES:
                getFilterEntries().clear();
                getFilterEntries().addAll((Collection<? extends FilterEntry>)newValue);
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
            case DbmapPackage.OUTPUT_TABLE__FILTER_ENTRIES:
                getFilterEntries().clear();
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
            case DbmapPackage.OUTPUT_TABLE__FILTER_ENTRIES:
                return filterEntries != null && !filterEntries.isEmpty();
        }
        return super.eIsSet(featureID);
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
        OutputTableImpl other = (OutputTableImpl) obj;
        
        EList<FilterEntry> otherFilters = other.getFilterEntries();
        
        if (getFilterEntries().size() != otherFilters.size()) {
            return false;
        }
        
        for(FilterEntry filter:getFilterEntries()){
            boolean found = false;
            for(FilterEntry otherFilter:otherFilters){
                if(filter.getName().equals(otherFilter.getName())){
                    found = true;
                    if(!filter.equals(otherFilter)){
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

} // OutputTableImpl
