/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.talend.designer.mapper.model.emf.mapper.impl;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.talend.designer.mapper.model.emf.mapper.MapperPackage;
import org.talend.designer.mapper.model.emf.mapper.OutputTable;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Output Table</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.talend.designer.mapper.model.emf.mapper.impl.OutputTableImpl#isReject <em>Reject</em>}</li>
 *   <li>{@link org.talend.designer.mapper.model.emf.mapper.impl.OutputTableImpl#isRejectInnerJoin <em>Reject Inner Join</em>}</li>
 *   <li>{@link org.talend.designer.mapper.model.emf.mapper.impl.OutputTableImpl#isIsErrorRejectTable <em>Is Error Reject Table</em>}</li>
 *   <li>{@link org.talend.designer.mapper.model.emf.mapper.impl.OutputTableImpl#getIsJoinTableOf <em>Is Join Table Of</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class OutputTableImpl extends AbstractInOutTableImpl implements OutputTable {
    /**
     * The default value of the '{@link #isReject() <em>Reject</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isReject()
     * @generated
     * @ordered
     */
    protected static final boolean REJECT_EDEFAULT = false;

    /**
     * The cached value of the '{@link #isReject() <em>Reject</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isReject()
     * @generated
     * @ordered
     */
    protected boolean reject = REJECT_EDEFAULT;

    /**
     * The default value of the '{@link #isRejectInnerJoin() <em>Reject Inner Join</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isRejectInnerJoin()
     * @generated
     * @ordered
     */
    protected static final boolean REJECT_INNER_JOIN_EDEFAULT = false;

    /**
     * The cached value of the '{@link #isRejectInnerJoin() <em>Reject Inner Join</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isRejectInnerJoin()
     * @generated
     * @ordered
     */
    protected boolean rejectInnerJoin = REJECT_INNER_JOIN_EDEFAULT;

    /**
     * The default value of the '{@link #isIsErrorRejectTable() <em>Is Error Reject Table</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isIsErrorRejectTable()
     * @generated
     * @ordered
     */
    protected static final boolean IS_ERROR_REJECT_TABLE_EDEFAULT = false;

    /**
     * The cached value of the '{@link #isIsErrorRejectTable() <em>Is Error Reject Table</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isIsErrorRejectTable()
     * @generated
     * @ordered
     */
    protected boolean isErrorRejectTable = IS_ERROR_REJECT_TABLE_EDEFAULT;

    /**
     * The default value of the '{@link #getIsJoinTableOf() <em>Is Join Table Of</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getIsJoinTableOf()
     * @generated
     * @ordered
     */
    protected static final String IS_JOIN_TABLE_OF_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getIsJoinTableOf() <em>Is Join Table Of</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getIsJoinTableOf()
     * @generated
     * @ordered
     */
    protected String isJoinTableOf = IS_JOIN_TABLE_OF_EDEFAULT;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected OutputTableImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return MapperPackage.Literals.OUTPUT_TABLE;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean isReject() {
        return reject;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setReject(boolean newReject) {
        boolean oldReject = reject;
        reject = newReject;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, MapperPackage.OUTPUT_TABLE__REJECT, oldReject, reject));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean isRejectInnerJoin() {
        return rejectInnerJoin;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setRejectInnerJoin(boolean newRejectInnerJoin) {
        boolean oldRejectInnerJoin = rejectInnerJoin;
        rejectInnerJoin = newRejectInnerJoin;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, MapperPackage.OUTPUT_TABLE__REJECT_INNER_JOIN, oldRejectInnerJoin, rejectInnerJoin));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean isIsErrorRejectTable() {
        return isErrorRejectTable;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setIsErrorRejectTable(boolean newIsErrorRejectTable) {
        boolean oldIsErrorRejectTable = isErrorRejectTable;
        isErrorRejectTable = newIsErrorRejectTable;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, MapperPackage.OUTPUT_TABLE__IS_ERROR_REJECT_TABLE, oldIsErrorRejectTable, isErrorRejectTable));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getIsJoinTableOf() {
        return isJoinTableOf;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setIsJoinTableOf(String newIsJoinTableOf) {
        String oldIsJoinTableOf = isJoinTableOf;
        isJoinTableOf = newIsJoinTableOf;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, MapperPackage.OUTPUT_TABLE__IS_JOIN_TABLE_OF, oldIsJoinTableOf, isJoinTableOf));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public Object eGet(int featureID, boolean resolve, boolean coreType) {
        switch (featureID) {
            case MapperPackage.OUTPUT_TABLE__REJECT:
                return isReject();
            case MapperPackage.OUTPUT_TABLE__REJECT_INNER_JOIN:
                return isRejectInnerJoin();
            case MapperPackage.OUTPUT_TABLE__IS_ERROR_REJECT_TABLE:
                return isIsErrorRejectTable();
            case MapperPackage.OUTPUT_TABLE__IS_JOIN_TABLE_OF:
                return getIsJoinTableOf();
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
            case MapperPackage.OUTPUT_TABLE__REJECT:
                setReject((Boolean)newValue);
                return;
            case MapperPackage.OUTPUT_TABLE__REJECT_INNER_JOIN:
                setRejectInnerJoin((Boolean)newValue);
                return;
            case MapperPackage.OUTPUT_TABLE__IS_ERROR_REJECT_TABLE:
                setIsErrorRejectTable((Boolean)newValue);
                return;
            case MapperPackage.OUTPUT_TABLE__IS_JOIN_TABLE_OF:
                setIsJoinTableOf((String)newValue);
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
            case MapperPackage.OUTPUT_TABLE__REJECT:
                setReject(REJECT_EDEFAULT);
                return;
            case MapperPackage.OUTPUT_TABLE__REJECT_INNER_JOIN:
                setRejectInnerJoin(REJECT_INNER_JOIN_EDEFAULT);
                return;
            case MapperPackage.OUTPUT_TABLE__IS_ERROR_REJECT_TABLE:
                setIsErrorRejectTable(IS_ERROR_REJECT_TABLE_EDEFAULT);
                return;
            case MapperPackage.OUTPUT_TABLE__IS_JOIN_TABLE_OF:
                setIsJoinTableOf(IS_JOIN_TABLE_OF_EDEFAULT);
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
            case MapperPackage.OUTPUT_TABLE__REJECT:
                return reject != REJECT_EDEFAULT;
            case MapperPackage.OUTPUT_TABLE__REJECT_INNER_JOIN:
                return rejectInnerJoin != REJECT_INNER_JOIN_EDEFAULT;
            case MapperPackage.OUTPUT_TABLE__IS_ERROR_REJECT_TABLE:
                return isErrorRejectTable != IS_ERROR_REJECT_TABLE_EDEFAULT;
            case MapperPackage.OUTPUT_TABLE__IS_JOIN_TABLE_OF:
                return IS_JOIN_TABLE_OF_EDEFAULT == null ? isJoinTableOf != null : !IS_JOIN_TABLE_OF_EDEFAULT.equals(isJoinTableOf);
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
        result.append(" (reject: ");
        result.append(reject);
        result.append(", rejectInnerJoin: ");
        result.append(rejectInnerJoin);
        result.append(", isErrorRejectTable: ");
        result.append(isErrorRejectTable);
        result.append(", isJoinTableOf: ");
        result.append(isJoinTableOf);
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
        OutputTableImpl other = (OutputTableImpl) obj;
        if(this.isErrorRejectTable != other.isErrorRejectTable){
            return false;
        }
        if(this.reject != other.reject){
            return false;
        }
        if(this.rejectInnerJoin != other.rejectInnerJoin){
            return false;
        }
        if(this.rejectInnerJoin != other.rejectInnerJoin){
            return false;
        }
        if (this.isJoinTableOf == null) {
            if (other.isJoinTableOf != null) {
                return false;
            }
        } else if (!this.isJoinTableOf.equals(other.isJoinTableOf)) {
            return false;
        }
        if (this.expressionFilter == null) {
            if (other.expressionFilter != null) {
                return false;
            }
        } else if (!this.expressionFilter.equals(other.expressionFilter)) {
            return false;
        }
        return super.equals(obj);
    }

} //OutputTableImpl
