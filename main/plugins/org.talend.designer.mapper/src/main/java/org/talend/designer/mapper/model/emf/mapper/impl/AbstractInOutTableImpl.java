/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.talend.designer.mapper.model.emf.mapper.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.talend.designer.mapper.model.emf.mapper.AbstractInOutTable;
import org.talend.designer.mapper.model.emf.mapper.MapperPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Abstract In Out Table</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.talend.designer.mapper.model.emf.mapper.impl.AbstractInOutTableImpl#getExpressionFilter <em>Expression Filter</em>}</li>
 *   <li>{@link org.talend.designer.mapper.model.emf.mapper.impl.AbstractInOutTableImpl#isActivateExpressionFilter <em>Activate Expression Filter</em>}</li>
 *   <li>{@link org.talend.designer.mapper.model.emf.mapper.impl.AbstractInOutTableImpl#isActivateCondensedTool <em>Activate Condensed Tool</em>}</li>
 *   <li>{@link org.talend.designer.mapper.model.emf.mapper.impl.AbstractInOutTableImpl#isActivateColumnNameFilter <em>Activate Column Name Filter</em>}</li>
 *   <li>{@link org.talend.designer.mapper.model.emf.mapper.impl.AbstractInOutTableImpl#getId <em>Id</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public abstract class AbstractInOutTableImpl extends AbstractDataMapTableImpl implements AbstractInOutTable {
    /**
     * The default value of the '{@link #getExpressionFilter() <em>Expression Filter</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getExpressionFilter()
     * @generated
     * @ordered
     */
    protected static final String EXPRESSION_FILTER_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getExpressionFilter() <em>Expression Filter</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getExpressionFilter()
     * @generated
     * @ordered
     */
    protected String expressionFilter = EXPRESSION_FILTER_EDEFAULT;

    /**
     * The default value of the '{@link #isActivateExpressionFilter() <em>Activate Expression Filter</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isActivateExpressionFilter()
     * @generated
     * @ordered
     */
    protected static final boolean ACTIVATE_EXPRESSION_FILTER_EDEFAULT = false;

    /**
     * The cached value of the '{@link #isActivateExpressionFilter() <em>Activate Expression Filter</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isActivateExpressionFilter()
     * @generated
     * @ordered
     */
    protected boolean activateExpressionFilter = ACTIVATE_EXPRESSION_FILTER_EDEFAULT;

    /**
     * The default value of the '{@link #isActivateCondensedTool() <em>Activate Condensed Tool</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isActivateCondensedTool()
     * @generated
     * @ordered
     */
    protected static final boolean ACTIVATE_CONDENSED_TOOL_EDEFAULT = false;

    /**
     * The cached value of the '{@link #isActivateCondensedTool() <em>Activate Condensed Tool</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isActivateCondensedTool()
     * @generated
     * @ordered
     */
    protected boolean activateCondensedTool = ACTIVATE_CONDENSED_TOOL_EDEFAULT;

    /**
     * The default value of the '{@link #isActivateColumnNameFilter() <em>Activate Column Name Filter</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isActivateColumnNameFilter()
     * @generated
     * @ordered
     */
    protected static final boolean ACTIVATE_COLUMN_NAME_FILTER_EDEFAULT = false;

    /**
     * The cached value of the '{@link #isActivateColumnNameFilter() <em>Activate Column Name Filter</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isActivateColumnNameFilter()
     * @generated
     * @ordered
     */
    protected boolean activateColumnNameFilter = ACTIVATE_COLUMN_NAME_FILTER_EDEFAULT;

    /**
     * The default value of the '{@link #getId() <em>Id</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getId()
     * @generated
     * @ordered
     */
    protected static final String ID_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getId() <em>Id</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getId()
     * @generated
     * @ordered
     */
    protected String id = ID_EDEFAULT;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected AbstractInOutTableImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return MapperPackage.Literals.ABSTRACT_IN_OUT_TABLE;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getExpressionFilter() {
        return expressionFilter;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setExpressionFilter(String newExpressionFilter) {
        String oldExpressionFilter = expressionFilter;
        expressionFilter = newExpressionFilter;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, MapperPackage.ABSTRACT_IN_OUT_TABLE__EXPRESSION_FILTER, oldExpressionFilter, expressionFilter));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean isActivateExpressionFilter() {
        return activateExpressionFilter;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setActivateExpressionFilter(boolean newActivateExpressionFilter) {
        boolean oldActivateExpressionFilter = activateExpressionFilter;
        activateExpressionFilter = newActivateExpressionFilter;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, MapperPackage.ABSTRACT_IN_OUT_TABLE__ACTIVATE_EXPRESSION_FILTER, oldActivateExpressionFilter, activateExpressionFilter));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean isActivateCondensedTool() {
        return activateCondensedTool;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setActivateCondensedTool(boolean newActivateCondensedTool) {
        boolean oldActivateCondensedTool = activateCondensedTool;
        activateCondensedTool = newActivateCondensedTool;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, MapperPackage.ABSTRACT_IN_OUT_TABLE__ACTIVATE_CONDENSED_TOOL, oldActivateCondensedTool, activateCondensedTool));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean isActivateColumnNameFilter() {
        return activateColumnNameFilter;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setActivateColumnNameFilter(boolean newActivateColumnNameFilter) {
        boolean oldActivateColumnNameFilter = activateColumnNameFilter;
        activateColumnNameFilter = newActivateColumnNameFilter;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, MapperPackage.ABSTRACT_IN_OUT_TABLE__ACTIVATE_COLUMN_NAME_FILTER, oldActivateColumnNameFilter, activateColumnNameFilter));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getId() {
        return id;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setId(String newId) {
        String oldId = id;
        id = newId;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, MapperPackage.ABSTRACT_IN_OUT_TABLE__ID, oldId, id));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public Object eGet(int featureID, boolean resolve, boolean coreType) {
        switch (featureID) {
            case MapperPackage.ABSTRACT_IN_OUT_TABLE__EXPRESSION_FILTER:
                return getExpressionFilter();
            case MapperPackage.ABSTRACT_IN_OUT_TABLE__ACTIVATE_EXPRESSION_FILTER:
                return isActivateExpressionFilter();
            case MapperPackage.ABSTRACT_IN_OUT_TABLE__ACTIVATE_CONDENSED_TOOL:
                return isActivateCondensedTool();
            case MapperPackage.ABSTRACT_IN_OUT_TABLE__ACTIVATE_COLUMN_NAME_FILTER:
                return isActivateColumnNameFilter();
            case MapperPackage.ABSTRACT_IN_OUT_TABLE__ID:
                return getId();
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
            case MapperPackage.ABSTRACT_IN_OUT_TABLE__EXPRESSION_FILTER:
                setExpressionFilter((String)newValue);
                return;
            case MapperPackage.ABSTRACT_IN_OUT_TABLE__ACTIVATE_EXPRESSION_FILTER:
                setActivateExpressionFilter((Boolean)newValue);
                return;
            case MapperPackage.ABSTRACT_IN_OUT_TABLE__ACTIVATE_CONDENSED_TOOL:
                setActivateCondensedTool((Boolean)newValue);
                return;
            case MapperPackage.ABSTRACT_IN_OUT_TABLE__ACTIVATE_COLUMN_NAME_FILTER:
                setActivateColumnNameFilter((Boolean)newValue);
                return;
            case MapperPackage.ABSTRACT_IN_OUT_TABLE__ID:
                setId((String)newValue);
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
            case MapperPackage.ABSTRACT_IN_OUT_TABLE__EXPRESSION_FILTER:
                setExpressionFilter(EXPRESSION_FILTER_EDEFAULT);
                return;
            case MapperPackage.ABSTRACT_IN_OUT_TABLE__ACTIVATE_EXPRESSION_FILTER:
                setActivateExpressionFilter(ACTIVATE_EXPRESSION_FILTER_EDEFAULT);
                return;
            case MapperPackage.ABSTRACT_IN_OUT_TABLE__ACTIVATE_CONDENSED_TOOL:
                setActivateCondensedTool(ACTIVATE_CONDENSED_TOOL_EDEFAULT);
                return;
            case MapperPackage.ABSTRACT_IN_OUT_TABLE__ACTIVATE_COLUMN_NAME_FILTER:
                setActivateColumnNameFilter(ACTIVATE_COLUMN_NAME_FILTER_EDEFAULT);
                return;
            case MapperPackage.ABSTRACT_IN_OUT_TABLE__ID:
                setId(ID_EDEFAULT);
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
            case MapperPackage.ABSTRACT_IN_OUT_TABLE__EXPRESSION_FILTER:
                return EXPRESSION_FILTER_EDEFAULT == null ? expressionFilter != null : !EXPRESSION_FILTER_EDEFAULT.equals(expressionFilter);
            case MapperPackage.ABSTRACT_IN_OUT_TABLE__ACTIVATE_EXPRESSION_FILTER:
                return activateExpressionFilter != ACTIVATE_EXPRESSION_FILTER_EDEFAULT;
            case MapperPackage.ABSTRACT_IN_OUT_TABLE__ACTIVATE_CONDENSED_TOOL:
                return activateCondensedTool != ACTIVATE_CONDENSED_TOOL_EDEFAULT;
            case MapperPackage.ABSTRACT_IN_OUT_TABLE__ACTIVATE_COLUMN_NAME_FILTER:
                return activateColumnNameFilter != ACTIVATE_COLUMN_NAME_FILTER_EDEFAULT;
            case MapperPackage.ABSTRACT_IN_OUT_TABLE__ID:
                return ID_EDEFAULT == null ? id != null : !ID_EDEFAULT.equals(id);
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
        result.append(" (expressionFilter: ");
        result.append(expressionFilter);
        result.append(", activateExpressionFilter: ");
        result.append(activateExpressionFilter);
        result.append(", activateCondensedTool: ");
        result.append(activateCondensedTool);
        result.append(", activateColumnNameFilter: ");
        result.append(activateColumnNameFilter);
        result.append(", id: ");
        result.append(id);
        result.append(')');
        return result.toString();
    }

} //AbstractInOutTableImpl
