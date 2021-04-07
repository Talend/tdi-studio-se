/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.talend.designer.dbmap.model.emf.dbmap.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.talend.designer.dbmap.model.emf.dbmap.DBMapperTableEntry;
import org.talend.designer.dbmap.model.emf.dbmap.DbmapPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>DB Mapper Table Entry</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.talend.designer.dbmap.model.emf.dbmap.impl.DBMapperTableEntryImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.talend.designer.dbmap.model.emf.dbmap.impl.DBMapperTableEntryImpl#getExpression <em>Expression</em>}</li>
 *   <li>{@link org.talend.designer.dbmap.model.emf.dbmap.impl.DBMapperTableEntryImpl#getType <em>Type</em>}</li>
 *   <li>{@link org.talend.designer.dbmap.model.emf.dbmap.impl.DBMapperTableEntryImpl#isNullable <em>Nullable</em>}</li>
 *   <li>{@link org.talend.designer.dbmap.model.emf.dbmap.impl.DBMapperTableEntryImpl#isJoin <em>Join</em>}</li>
 *   <li>{@link org.talend.designer.dbmap.model.emf.dbmap.impl.DBMapperTableEntryImpl#getOperator <em>Operator</em>}</li>
 * </ul>
 *
 * @generated
 */
public class DBMapperTableEntryImpl extends EObjectImpl implements DBMapperTableEntry {
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
     * The default value of the '{@link #getExpression() <em>Expression</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getExpression()
     * @generated
     * @ordered
     */
    protected static final String EXPRESSION_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getExpression() <em>Expression</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getExpression()
     * @generated
     * @ordered
     */
    protected String expression = EXPRESSION_EDEFAULT;

    /**
     * The default value of the '{@link #getType() <em>Type</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getType()
     * @generated
     * @ordered
     */
    protected static final String TYPE_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getType() <em>Type</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getType()
     * @generated
     * @ordered
     */
    protected String type = TYPE_EDEFAULT;

    /**
     * The default value of the '{@link #isNullable() <em>Nullable</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isNullable()
     * @generated
     * @ordered
     */
    protected static final boolean NULLABLE_EDEFAULT = false;

    /**
     * The cached value of the '{@link #isNullable() <em>Nullable</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isNullable()
     * @generated
     * @ordered
     */
    protected boolean nullable = NULLABLE_EDEFAULT;

    /**
     * The default value of the '{@link #isJoin() <em>Join</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isJoin()
     * @generated
     * @ordered
     */
    protected static final boolean JOIN_EDEFAULT = false;

    /**
     * The cached value of the '{@link #isJoin() <em>Join</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isJoin()
     * @generated
     * @ordered
     */
    protected boolean join = JOIN_EDEFAULT;

    /**
     * The default value of the '{@link #getOperator() <em>Operator</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getOperator()
     * @generated
     * @ordered
     */
    protected static final String OPERATOR_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getOperator() <em>Operator</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getOperator()
     * @generated
     * @ordered
     */
    protected String operator = OPERATOR_EDEFAULT;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected DBMapperTableEntryImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return DbmapPackage.Literals.DB_MAPPER_TABLE_ENTRY;
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
            eNotify(new ENotificationImpl(this, Notification.SET, DbmapPackage.DB_MAPPER_TABLE_ENTRY__NAME, oldName, name));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getExpression() {
        return expression;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setExpression(String newExpression) {
        String oldExpression = expression;
        expression = newExpression;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, DbmapPackage.DB_MAPPER_TABLE_ENTRY__EXPRESSION, oldExpression, expression));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getType() {
        return type;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setType(String newType) {
        String oldType = type;
        type = newType;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, DbmapPackage.DB_MAPPER_TABLE_ENTRY__TYPE, oldType, type));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean isNullable() {
        return nullable;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setNullable(boolean newNullable) {
        boolean oldNullable = nullable;
        nullable = newNullable;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, DbmapPackage.DB_MAPPER_TABLE_ENTRY__NULLABLE, oldNullable, nullable));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean isJoin() {
        return join;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setJoin(boolean newJoin) {
        boolean oldJoin = join;
        join = newJoin;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, DbmapPackage.DB_MAPPER_TABLE_ENTRY__JOIN, oldJoin, join));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getOperator() {
        return operator;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setOperator(String newOperator) {
        String oldOperator = operator;
        operator = newOperator;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, DbmapPackage.DB_MAPPER_TABLE_ENTRY__OPERATOR, oldOperator, operator));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public Object eGet(int featureID, boolean resolve, boolean coreType) {
        switch (featureID) {
            case DbmapPackage.DB_MAPPER_TABLE_ENTRY__NAME:
                return getName();
            case DbmapPackage.DB_MAPPER_TABLE_ENTRY__EXPRESSION:
                return getExpression();
            case DbmapPackage.DB_MAPPER_TABLE_ENTRY__TYPE:
                return getType();
            case DbmapPackage.DB_MAPPER_TABLE_ENTRY__NULLABLE:
                return isNullable();
            case DbmapPackage.DB_MAPPER_TABLE_ENTRY__JOIN:
                return isJoin();
            case DbmapPackage.DB_MAPPER_TABLE_ENTRY__OPERATOR:
                return getOperator();
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
            case DbmapPackage.DB_MAPPER_TABLE_ENTRY__NAME:
                setName((String)newValue);
                return;
            case DbmapPackage.DB_MAPPER_TABLE_ENTRY__EXPRESSION:
                setExpression((String)newValue);
                return;
            case DbmapPackage.DB_MAPPER_TABLE_ENTRY__TYPE:
                setType((String)newValue);
                return;
            case DbmapPackage.DB_MAPPER_TABLE_ENTRY__NULLABLE:
                setNullable((Boolean)newValue);
                return;
            case DbmapPackage.DB_MAPPER_TABLE_ENTRY__JOIN:
                setJoin((Boolean)newValue);
                return;
            case DbmapPackage.DB_MAPPER_TABLE_ENTRY__OPERATOR:
                setOperator((String)newValue);
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
            case DbmapPackage.DB_MAPPER_TABLE_ENTRY__NAME:
                setName(NAME_EDEFAULT);
                return;
            case DbmapPackage.DB_MAPPER_TABLE_ENTRY__EXPRESSION:
                setExpression(EXPRESSION_EDEFAULT);
                return;
            case DbmapPackage.DB_MAPPER_TABLE_ENTRY__TYPE:
                setType(TYPE_EDEFAULT);
                return;
            case DbmapPackage.DB_MAPPER_TABLE_ENTRY__NULLABLE:
                setNullable(NULLABLE_EDEFAULT);
                return;
            case DbmapPackage.DB_MAPPER_TABLE_ENTRY__JOIN:
                setJoin(JOIN_EDEFAULT);
                return;
            case DbmapPackage.DB_MAPPER_TABLE_ENTRY__OPERATOR:
                setOperator(OPERATOR_EDEFAULT);
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
            case DbmapPackage.DB_MAPPER_TABLE_ENTRY__NAME:
                return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
            case DbmapPackage.DB_MAPPER_TABLE_ENTRY__EXPRESSION:
                return EXPRESSION_EDEFAULT == null ? expression != null : !EXPRESSION_EDEFAULT.equals(expression);
            case DbmapPackage.DB_MAPPER_TABLE_ENTRY__TYPE:
                return TYPE_EDEFAULT == null ? type != null : !TYPE_EDEFAULT.equals(type);
            case DbmapPackage.DB_MAPPER_TABLE_ENTRY__NULLABLE:
                return nullable != NULLABLE_EDEFAULT;
            case DbmapPackage.DB_MAPPER_TABLE_ENTRY__JOIN:
                return join != JOIN_EDEFAULT;
            case DbmapPackage.DB_MAPPER_TABLE_ENTRY__OPERATOR:
                return OPERATOR_EDEFAULT == null ? operator != null : !OPERATOR_EDEFAULT.equals(operator);
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
        result.append(", expression: ");
        result.append(expression);
        result.append(", type: ");
        result.append(type);
        result.append(", nullable: ");
        result.append(nullable);
        result.append(", join: ");
        result.append(join);
        result.append(", operator: ");
        result.append(operator);
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
        DBMapperTableEntryImpl other = (DBMapperTableEntryImpl) obj;
        if(this.join != other.join){
            return false;   
        }
        if(this.nullable != other.nullable){
            return false;
        }
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
        
        if (this.operator == null) {
            if (other.operator != null) {
                return false;
            }
        } else if (!this.operator.equals(other.operator)) {
            return false;
        }
        
        if (this.type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!this.type.equals(other.type)) {
            return false;
        }
        return true;
    }

} //DBMapperTableEntryImpl
