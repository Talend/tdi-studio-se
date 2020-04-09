/**
 * <copyright> </copyright>
 *
 * $Id$
 */
package org.talend.designer.dbmap.model.emf.dbmap.impl;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.talend.designer.dbmap.model.emf.dbmap.DbmapPackage;
import org.talend.designer.dbmap.model.emf.dbmap.FilterEntry;

/**
 * <!-- begin-user-doc --> An implementation of the model object '<em><b>Filter Entry</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.talend.designer.dbmap.model.emf.dbmap.impl.FilterEntryImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.talend.designer.dbmap.model.emf.dbmap.impl.FilterEntryImpl#getExpression <em>Expression</em>}</li>
 *   <li>{@link org.talend.designer.dbmap.model.emf.dbmap.impl.FilterEntryImpl#getFilterKind <em>Filter Kind</em>}</li>
 * </ul>
 *
 * @generated
 */
public class FilterEntryImpl extends EObjectImpl implements FilterEntry {

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
     * The default value of the '{@link #getExpression() <em>Expression</em>}' attribute.
     * <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * @see #getExpression()
     * @generated
     * @ordered
     */
    protected static final String EXPRESSION_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getExpression() <em>Expression</em>}' attribute.
     * <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * @see #getExpression()
     * @generated
     * @ordered
     */
    protected String expression = EXPRESSION_EDEFAULT;

    protected static final String FILTER_KIND_EDEFAULT = null;

    protected String filterKind = FILTER_KIND_EDEFAULT;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    protected FilterEntryImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return DbmapPackage.Literals.FILTER_ENTRY;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public void setName(String newName) {
        String oldName = name;
        name = newName;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, DbmapPackage.FILTER_ENTRY__NAME, oldName, name));
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public String getExpression() {
        return expression;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public void setExpression(String newExpression) {
        String oldExpression = expression;
        expression = newExpression;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, DbmapPackage.FILTER_ENTRY__EXPRESSION, oldExpression, expression));
    }

    @Override
    public String getFilterKind() {
        return filterKind;
    };

    @Override
    public void setFilterKind(String newFilterKind) {
        String oldFilterKind = filterKind;
        filterKind = newFilterKind;
        if (eNotificationRequired()) {
            eNotify(new ENotificationImpl(this, Notification.SET, DbmapPackage.FILTER_ENTRY__FILTER_KIND, oldFilterKind,
                    filterKind));
        }
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public Object eGet(int featureID, boolean resolve, boolean coreType) {
        switch (featureID) {
            case DbmapPackage.FILTER_ENTRY__NAME:
                return getName();
            case DbmapPackage.FILTER_ENTRY__EXPRESSION:
                return getExpression();
            case DbmapPackage.FILTER_ENTRY__FILTER_KIND:
                return getFilterKind();
        }
        return super.eGet(featureID, resolve, coreType);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public void eSet(int featureID, Object newValue) {
        switch (featureID) {
            case DbmapPackage.FILTER_ENTRY__NAME:
                setName((String)newValue);
                return;
            case DbmapPackage.FILTER_ENTRY__EXPRESSION:
                setExpression((String)newValue);
                return;
            case DbmapPackage.FILTER_ENTRY__FILTER_KIND:
                setFilterKind((String)newValue);
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
            case DbmapPackage.FILTER_ENTRY__NAME:
                setName(NAME_EDEFAULT);
                return;
            case DbmapPackage.FILTER_ENTRY__EXPRESSION:
                setExpression(EXPRESSION_EDEFAULT);
                return;
            case DbmapPackage.FILTER_ENTRY__FILTER_KIND:
                setFilterKind(FILTER_KIND_EDEFAULT);
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
            case DbmapPackage.FILTER_ENTRY__NAME:
                return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
            case DbmapPackage.FILTER_ENTRY__EXPRESSION:
                return EXPRESSION_EDEFAULT == null ? expression != null : !EXPRESSION_EDEFAULT.equals(expression);
            case DbmapPackage.FILTER_ENTRY__FILTER_KIND:
                return FILTER_KIND_EDEFAULT == null ? filterKind != null : !FILTER_KIND_EDEFAULT.equals(filterKind);
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

        StringBuilder result = new StringBuilder(super.toString());
        result.append(" (name: ");
        result.append(name);
        result.append(", expression: ");
        result.append(expression);
        result.append(", filterKind: ");
        result.append(filterKind);
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
        FilterEntryImpl other = (FilterEntryImpl) obj;
        if (this.name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!this.name.equals(other.name)) {
            return false;
        }
        
        if (this.expression == null) {
            if (other.expression != null) {
                return false;
            }
        } else if (!this.expression.equals(other.expression)) {
            return false;
        }
        
        if (this.filterKind == null) {
            if (other.filterKind != null) {
                return false;
            }
        } else if (!this.filterKind.equals(other.filterKind)) {
            return false;
        }
        return true;
    }

} // FilterEntryImpl
