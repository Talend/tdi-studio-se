/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.talend.designer.dbmap.model.emf.dbmap.impl;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.talend.designer.dbmap.model.emf.dbmap.DBMapperTableEntry;
import org.talend.designer.dbmap.model.emf.dbmap.DbmapPackage;
import org.talend.designer.dbmap.model.emf.dbmap.InputTable;
import org.talend.designer.dbmap.model.emf.dbmap.VarTable;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Input Table</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.talend.designer.dbmap.model.emf.dbmap.impl.InputTableImpl#getJoinType <em>Join Type</em>}</li>
 *   <li>{@link org.talend.designer.dbmap.model.emf.dbmap.impl.InputTableImpl#getAlias <em>Alias</em>}</li>
 * </ul>
 *
 * @generated
 */
public class InputTableImpl extends AbstaceDBInOutTableImpl implements InputTable {
    /**
     * The default value of the '{@link #getJoinType() <em>Join Type</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getJoinType()
     * @generated
     * @ordered
     */
    protected static final String JOIN_TYPE_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getJoinType() <em>Join Type</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getJoinType()
     * @generated
     * @ordered
     */
    protected String joinType = JOIN_TYPE_EDEFAULT;

    /**
     * The default value of the '{@link #getAlias() <em>Alias</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getAlias()
     * @generated
     * @ordered
     */
    protected static final String ALIAS_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getAlias() <em>Alias</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getAlias()
     * @generated
     * @ordered
     */
    protected String alias = ALIAS_EDEFAULT;

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
        return DbmapPackage.Literals.INPUT_TABLE;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getJoinType() {
        return joinType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setJoinType(String newJoinType) {
        String oldJoinType = joinType;
        joinType = newJoinType;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, DbmapPackage.INPUT_TABLE__JOIN_TYPE, oldJoinType, joinType));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getAlias() {
        return alias;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setAlias(String newAlias) {
        String oldAlias = alias;
        alias = newAlias;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, DbmapPackage.INPUT_TABLE__ALIAS, oldAlias, alias));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public Object eGet(int featureID, boolean resolve, boolean coreType) {
        switch (featureID) {
            case DbmapPackage.INPUT_TABLE__JOIN_TYPE:
                return getJoinType();
            case DbmapPackage.INPUT_TABLE__ALIAS:
                return getAlias();
        }
        return super.eGet(featureID, resolve, coreType);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public void eSet(int featureID, Object newValue) {
        switch (featureID) {
            case DbmapPackage.INPUT_TABLE__JOIN_TYPE:
                setJoinType((String)newValue);
                return;
            case DbmapPackage.INPUT_TABLE__ALIAS:
                setAlias((String)newValue);
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
            case DbmapPackage.INPUT_TABLE__JOIN_TYPE:
                setJoinType(JOIN_TYPE_EDEFAULT);
                return;
            case DbmapPackage.INPUT_TABLE__ALIAS:
                setAlias(ALIAS_EDEFAULT);
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
            case DbmapPackage.INPUT_TABLE__JOIN_TYPE:
                return JOIN_TYPE_EDEFAULT == null ? joinType != null : !JOIN_TYPE_EDEFAULT.equals(joinType);
            case DbmapPackage.INPUT_TABLE__ALIAS:
                return ALIAS_EDEFAULT == null ? alias != null : !ALIAS_EDEFAULT.equals(alias);
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
        result.append(" (joinType: ");
        result.append(joinType);
        result.append(", alias: ");
        result.append(alias);
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
        if (this.alias == null) {
            if (other.alias != null) {
                return false;
            }
        } else if (!this.alias.equals(other.alias)) {
            return false;
        }
        
        if (this.joinType == null) {
            if (other.joinType != null) {
                return false;
            }
        } else if (!this.joinType.equals(other.joinType)) {
            return false;
        }

        return super.equals(obj);
    }
    
} //InputTableImpl
