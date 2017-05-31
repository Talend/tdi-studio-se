/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.talend.designer.xmlmap.model.emf.xmlmap.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.eclipse.emf.ecore.util.EObjectResolvingEList;
import org.talend.designer.xmlmap.model.emf.xmlmap.AbstractInOutTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.FilterConnection;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlmapPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Abstract In Out Tree</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.talend.designer.xmlmap.model.emf.xmlmap.impl.AbstractInOutTreeImpl#getExpressionFilter <em>Expression Filter</em>}</li>
 *   <li>{@link org.talend.designer.xmlmap.model.emf.xmlmap.impl.AbstractInOutTreeImpl#isActivateExpressionFilter <em>Activate Expression Filter</em>}</li>
 *   <li>{@link org.talend.designer.xmlmap.model.emf.xmlmap.impl.AbstractInOutTreeImpl#isActivateCondensedTool <em>Activate Condensed Tool</em>}</li>
 *   <li>{@link org.talend.designer.xmlmap.model.emf.xmlmap.impl.AbstractInOutTreeImpl#isMinimized <em>Minimized</em>}</li>
 *   <li>{@link org.talend.designer.xmlmap.model.emf.xmlmap.impl.AbstractInOutTreeImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.talend.designer.xmlmap.model.emf.xmlmap.impl.AbstractInOutTreeImpl#getFilterIncomingConnections <em>Filter Incoming Connections</em>}</li>
 *   <li>{@link org.talend.designer.xmlmap.model.emf.xmlmap.impl.AbstractInOutTreeImpl#isMultiLoops <em>Multi Loops</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public abstract class AbstractInOutTreeImpl extends EObjectImpl implements AbstractInOutTree {
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
     * The cached value of the '{@link #getFilterIncomingConnections() <em>Filter Incoming Connections</em>}' reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getFilterIncomingConnections()
     * @generated
     * @ordered
     */
    protected EList<FilterConnection> filterIncomingConnections;

    /**
     * The default value of the '{@link #isMultiLoops() <em>Multi Loops</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isMultiLoops()
     * @generated
     * @ordered
     */
    protected static final boolean MULTI_LOOPS_EDEFAULT = false;

    /**
     * The cached value of the '{@link #isMultiLoops() <em>Multi Loops</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isMultiLoops()
     * @generated
     * @ordered
     */
    protected boolean multiLoops = MULTI_LOOPS_EDEFAULT;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected AbstractInOutTreeImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return XmlmapPackage.Literals.ABSTRACT_IN_OUT_TREE;
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
            eNotify(new ENotificationImpl(this, Notification.SET, XmlmapPackage.ABSTRACT_IN_OUT_TREE__EXPRESSION_FILTER, oldExpressionFilter, expressionFilter));
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
            eNotify(new ENotificationImpl(this, Notification.SET, XmlmapPackage.ABSTRACT_IN_OUT_TREE__ACTIVATE_EXPRESSION_FILTER, oldActivateExpressionFilter, activateExpressionFilter));
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
            eNotify(new ENotificationImpl(this, Notification.SET, XmlmapPackage.ABSTRACT_IN_OUT_TREE__ACTIVATE_CONDENSED_TOOL, oldActivateCondensedTool, activateCondensedTool));
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
            eNotify(new ENotificationImpl(this, Notification.SET, XmlmapPackage.ABSTRACT_IN_OUT_TREE__MINIMIZED, oldMinimized, minimized));
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
            eNotify(new ENotificationImpl(this, Notification.SET, XmlmapPackage.ABSTRACT_IN_OUT_TREE__NAME, oldName, name));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList<FilterConnection> getFilterIncomingConnections() {
        if (filterIncomingConnections == null) {
            filterIncomingConnections = new EObjectResolvingEList<FilterConnection>(FilterConnection.class, this, XmlmapPackage.ABSTRACT_IN_OUT_TREE__FILTER_INCOMING_CONNECTIONS);
        }
        return filterIncomingConnections;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean isMultiLoops() {
        return multiLoops;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setMultiLoops(boolean newMultiLoops) {
        boolean oldMultiLoops = multiLoops;
        multiLoops = newMultiLoops;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, XmlmapPackage.ABSTRACT_IN_OUT_TREE__MULTI_LOOPS, oldMultiLoops, multiLoops));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public Object eGet(int featureID, boolean resolve, boolean coreType) {
        switch (featureID) {
            case XmlmapPackage.ABSTRACT_IN_OUT_TREE__EXPRESSION_FILTER:
                return getExpressionFilter();
            case XmlmapPackage.ABSTRACT_IN_OUT_TREE__ACTIVATE_EXPRESSION_FILTER:
                return isActivateExpressionFilter();
            case XmlmapPackage.ABSTRACT_IN_OUT_TREE__ACTIVATE_CONDENSED_TOOL:
                return isActivateCondensedTool();
            case XmlmapPackage.ABSTRACT_IN_OUT_TREE__MINIMIZED:
                return isMinimized();
            case XmlmapPackage.ABSTRACT_IN_OUT_TREE__NAME:
                return getName();
            case XmlmapPackage.ABSTRACT_IN_OUT_TREE__FILTER_INCOMING_CONNECTIONS:
                return getFilterIncomingConnections();
            case XmlmapPackage.ABSTRACT_IN_OUT_TREE__MULTI_LOOPS:
                return isMultiLoops();
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
            case XmlmapPackage.ABSTRACT_IN_OUT_TREE__EXPRESSION_FILTER:
                setExpressionFilter((String)newValue);
                return;
            case XmlmapPackage.ABSTRACT_IN_OUT_TREE__ACTIVATE_EXPRESSION_FILTER:
                setActivateExpressionFilter((Boolean)newValue);
                return;
            case XmlmapPackage.ABSTRACT_IN_OUT_TREE__ACTIVATE_CONDENSED_TOOL:
                setActivateCondensedTool((Boolean)newValue);
                return;
            case XmlmapPackage.ABSTRACT_IN_OUT_TREE__MINIMIZED:
                setMinimized((Boolean)newValue);
                return;
            case XmlmapPackage.ABSTRACT_IN_OUT_TREE__NAME:
                setName((String)newValue);
                return;
            case XmlmapPackage.ABSTRACT_IN_OUT_TREE__FILTER_INCOMING_CONNECTIONS:
                getFilterIncomingConnections().clear();
                getFilterIncomingConnections().addAll((Collection<? extends FilterConnection>)newValue);
                return;
            case XmlmapPackage.ABSTRACT_IN_OUT_TREE__MULTI_LOOPS:
                setMultiLoops((Boolean)newValue);
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
            case XmlmapPackage.ABSTRACT_IN_OUT_TREE__EXPRESSION_FILTER:
                setExpressionFilter(EXPRESSION_FILTER_EDEFAULT);
                return;
            case XmlmapPackage.ABSTRACT_IN_OUT_TREE__ACTIVATE_EXPRESSION_FILTER:
                setActivateExpressionFilter(ACTIVATE_EXPRESSION_FILTER_EDEFAULT);
                return;
            case XmlmapPackage.ABSTRACT_IN_OUT_TREE__ACTIVATE_CONDENSED_TOOL:
                setActivateCondensedTool(ACTIVATE_CONDENSED_TOOL_EDEFAULT);
                return;
            case XmlmapPackage.ABSTRACT_IN_OUT_TREE__MINIMIZED:
                setMinimized(MINIMIZED_EDEFAULT);
                return;
            case XmlmapPackage.ABSTRACT_IN_OUT_TREE__NAME:
                setName(NAME_EDEFAULT);
                return;
            case XmlmapPackage.ABSTRACT_IN_OUT_TREE__FILTER_INCOMING_CONNECTIONS:
                getFilterIncomingConnections().clear();
                return;
            case XmlmapPackage.ABSTRACT_IN_OUT_TREE__MULTI_LOOPS:
                setMultiLoops(MULTI_LOOPS_EDEFAULT);
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
            case XmlmapPackage.ABSTRACT_IN_OUT_TREE__EXPRESSION_FILTER:
                return EXPRESSION_FILTER_EDEFAULT == null ? expressionFilter != null : !EXPRESSION_FILTER_EDEFAULT.equals(expressionFilter);
            case XmlmapPackage.ABSTRACT_IN_OUT_TREE__ACTIVATE_EXPRESSION_FILTER:
                return activateExpressionFilter != ACTIVATE_EXPRESSION_FILTER_EDEFAULT;
            case XmlmapPackage.ABSTRACT_IN_OUT_TREE__ACTIVATE_CONDENSED_TOOL:
                return activateCondensedTool != ACTIVATE_CONDENSED_TOOL_EDEFAULT;
            case XmlmapPackage.ABSTRACT_IN_OUT_TREE__MINIMIZED:
                return minimized != MINIMIZED_EDEFAULT;
            case XmlmapPackage.ABSTRACT_IN_OUT_TREE__NAME:
                return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
            case XmlmapPackage.ABSTRACT_IN_OUT_TREE__FILTER_INCOMING_CONNECTIONS:
                return filterIncomingConnections != null && !filterIncomingConnections.isEmpty();
            case XmlmapPackage.ABSTRACT_IN_OUT_TREE__MULTI_LOOPS:
                return multiLoops != MULTI_LOOPS_EDEFAULT;
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
        result.append(", minimized: ");
        result.append(minimized);
        result.append(", name: ");
        result.append(name);
        result.append(", multiLoops: ");
        result.append(multiLoops);
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
        AbstractInOutTreeImpl other = (AbstractInOutTreeImpl) obj;
        if(this.getFilterIncomingConnections().size() != other.getFilterIncomingConnections().size()){
            return false;
        }
        if (this.name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!this.name.equals(other.name)) {
            return false;
        }
        if (this.expressionFilter == null) {
            if (other.expressionFilter != null) {
                return false;
            }
        } else if (!this.expressionFilter.equals(other.expressionFilter)) {
            return false;
        }
        
        if(this.activateExpressionFilter != other.activateExpressionFilter){
            return false;
        }
        if(this.activateCondensedTool != other.activateCondensedTool){
            return false;
        }
        if(this.minimized != other.minimized){
            return false;
        }
        if(this.multiLoops != other.multiLoops){
            return false;
        }
        
        return true;
    }

} //AbstractInOutTreeImpl
