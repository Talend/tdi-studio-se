/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.talend.designer.xmlmap.model.emf.xmlmap.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;
import org.talend.designer.xmlmap.model.emf.xmlmap.InputLoopNodesTable;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputTreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.TreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlmapPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Output Xml Tree</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.talend.designer.xmlmap.model.emf.xmlmap.impl.OutputXmlTreeImpl#getNodes <em>Nodes</em>}</li>
 *   <li>{@link org.talend.designer.xmlmap.model.emf.xmlmap.impl.OutputXmlTreeImpl#isReject <em>Reject</em>}</li>
 *   <li>{@link org.talend.designer.xmlmap.model.emf.xmlmap.impl.OutputXmlTreeImpl#isRejectInnerJoin <em>Reject Inner Join</em>}</li>
 *   <li>{@link org.talend.designer.xmlmap.model.emf.xmlmap.impl.OutputXmlTreeImpl#isErrorReject <em>Error Reject</em>}</li>
 *   <li>{@link org.talend.designer.xmlmap.model.emf.xmlmap.impl.OutputXmlTreeImpl#isAllInOne <em>All In One</em>}</li>
 *   <li>{@link org.talend.designer.xmlmap.model.emf.xmlmap.impl.OutputXmlTreeImpl#isEnableEmptyElement <em>Enable Empty Element</em>}</li>
 *   <li>{@link org.talend.designer.xmlmap.model.emf.xmlmap.impl.OutputXmlTreeImpl#getInputLoopNodesTables <em>Input Loop Nodes Tables</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class OutputXmlTreeImpl extends AbstractInOutTreeImpl implements OutputXmlTree {
    /**
     * The cached value of the '{@link #getNodes() <em>Nodes</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getNodes()
     * @generated
     * @ordered
     */
    protected EList<OutputTreeNode> nodes;

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
     * The default value of the '{@link #isErrorReject() <em>Error Reject</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isErrorReject()
     * @generated
     * @ordered
     */
    protected static final boolean ERROR_REJECT_EDEFAULT = false;

    /**
     * The cached value of the '{@link #isErrorReject() <em>Error Reject</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isErrorReject()
     * @generated
     * @ordered
     */
    protected boolean errorReject = ERROR_REJECT_EDEFAULT;

    /**
     * The default value of the '{@link #isAllInOne() <em>All In One</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isAllInOne()
     * @generated
     * @ordered
     */
    protected static final boolean ALL_IN_ONE_EDEFAULT = false;

    /**
     * The cached value of the '{@link #isAllInOne() <em>All In One</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isAllInOne()
     * @generated
     * @ordered
     */
    protected boolean allInOne = ALL_IN_ONE_EDEFAULT;

    /**
     * The default value of the '{@link #isEnableEmptyElement() <em>Enable Empty Element</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isEnableEmptyElement()
     * @generated
     * @ordered
     */
    protected static final boolean ENABLE_EMPTY_ELEMENT_EDEFAULT = true;

    /**
     * The cached value of the '{@link #isEnableEmptyElement() <em>Enable Empty Element</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isEnableEmptyElement()
     * @generated
     * @ordered
     */
    protected boolean enableEmptyElement = ENABLE_EMPTY_ELEMENT_EDEFAULT;

    /**
     * The cached value of the '{@link #getInputLoopNodesTables() <em>Input Loop Nodes Tables</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getInputLoopNodesTables()
     * @generated
     * @ordered
     */
    protected EList<InputLoopNodesTable> inputLoopNodesTables;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected OutputXmlTreeImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return XmlmapPackage.Literals.OUTPUT_XML_TREE;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList<OutputTreeNode> getNodes() {
        if (nodes == null) {
            nodes = new EObjectContainmentEList<OutputTreeNode>(OutputTreeNode.class, this, XmlmapPackage.OUTPUT_XML_TREE__NODES);
        }
        return nodes;
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
            eNotify(new ENotificationImpl(this, Notification.SET, XmlmapPackage.OUTPUT_XML_TREE__REJECT, oldReject, reject));
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
            eNotify(new ENotificationImpl(this, Notification.SET, XmlmapPackage.OUTPUT_XML_TREE__REJECT_INNER_JOIN, oldRejectInnerJoin, rejectInnerJoin));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean isErrorReject() {
        return errorReject;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setErrorReject(boolean newErrorReject) {
        boolean oldErrorReject = errorReject;
        errorReject = newErrorReject;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, XmlmapPackage.OUTPUT_XML_TREE__ERROR_REJECT, oldErrorReject, errorReject));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean isAllInOne() {
        return allInOne;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setAllInOne(boolean newAllInOne) {
        boolean oldAllInOne = allInOne;
        allInOne = newAllInOne;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, XmlmapPackage.OUTPUT_XML_TREE__ALL_IN_ONE, oldAllInOne, allInOne));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean isEnableEmptyElement() {
        return enableEmptyElement;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setEnableEmptyElement(boolean newEnableEmptyElement) {
        boolean oldEnableEmptyElement = enableEmptyElement;
        enableEmptyElement = newEnableEmptyElement;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, XmlmapPackage.OUTPUT_XML_TREE__ENABLE_EMPTY_ELEMENT, oldEnableEmptyElement, enableEmptyElement));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList<InputLoopNodesTable> getInputLoopNodesTables() {
        if (inputLoopNodesTables == null) {
            inputLoopNodesTables = new EObjectContainmentEList<InputLoopNodesTable>(InputLoopNodesTable.class, this, XmlmapPackage.OUTPUT_XML_TREE__INPUT_LOOP_NODES_TABLES);
        }
        return inputLoopNodesTables;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
        switch (featureID) {
            case XmlmapPackage.OUTPUT_XML_TREE__NODES:
                return ((InternalEList<?>)getNodes()).basicRemove(otherEnd, msgs);
            case XmlmapPackage.OUTPUT_XML_TREE__INPUT_LOOP_NODES_TABLES:
                return ((InternalEList<?>)getInputLoopNodesTables()).basicRemove(otherEnd, msgs);
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
            case XmlmapPackage.OUTPUT_XML_TREE__NODES:
                return getNodes();
            case XmlmapPackage.OUTPUT_XML_TREE__REJECT:
                return isReject();
            case XmlmapPackage.OUTPUT_XML_TREE__REJECT_INNER_JOIN:
                return isRejectInnerJoin();
            case XmlmapPackage.OUTPUT_XML_TREE__ERROR_REJECT:
                return isErrorReject();
            case XmlmapPackage.OUTPUT_XML_TREE__ALL_IN_ONE:
                return isAllInOne();
            case XmlmapPackage.OUTPUT_XML_TREE__ENABLE_EMPTY_ELEMENT:
                return isEnableEmptyElement();
            case XmlmapPackage.OUTPUT_XML_TREE__INPUT_LOOP_NODES_TABLES:
                return getInputLoopNodesTables();
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
            case XmlmapPackage.OUTPUT_XML_TREE__NODES:
                getNodes().clear();
                getNodes().addAll((Collection<? extends OutputTreeNode>)newValue);
                return;
            case XmlmapPackage.OUTPUT_XML_TREE__REJECT:
                setReject((Boolean)newValue);
                return;
            case XmlmapPackage.OUTPUT_XML_TREE__REJECT_INNER_JOIN:
                setRejectInnerJoin((Boolean)newValue);
                return;
            case XmlmapPackage.OUTPUT_XML_TREE__ERROR_REJECT:
                setErrorReject((Boolean)newValue);
                return;
            case XmlmapPackage.OUTPUT_XML_TREE__ALL_IN_ONE:
                setAllInOne((Boolean)newValue);
                return;
            case XmlmapPackage.OUTPUT_XML_TREE__ENABLE_EMPTY_ELEMENT:
                setEnableEmptyElement((Boolean)newValue);
                return;
            case XmlmapPackage.OUTPUT_XML_TREE__INPUT_LOOP_NODES_TABLES:
                getInputLoopNodesTables().clear();
                getInputLoopNodesTables().addAll((Collection<? extends InputLoopNodesTable>)newValue);
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
            case XmlmapPackage.OUTPUT_XML_TREE__NODES:
                getNodes().clear();
                return;
            case XmlmapPackage.OUTPUT_XML_TREE__REJECT:
                setReject(REJECT_EDEFAULT);
                return;
            case XmlmapPackage.OUTPUT_XML_TREE__REJECT_INNER_JOIN:
                setRejectInnerJoin(REJECT_INNER_JOIN_EDEFAULT);
                return;
            case XmlmapPackage.OUTPUT_XML_TREE__ERROR_REJECT:
                setErrorReject(ERROR_REJECT_EDEFAULT);
                return;
            case XmlmapPackage.OUTPUT_XML_TREE__ALL_IN_ONE:
                setAllInOne(ALL_IN_ONE_EDEFAULT);
                return;
            case XmlmapPackage.OUTPUT_XML_TREE__ENABLE_EMPTY_ELEMENT:
                setEnableEmptyElement(ENABLE_EMPTY_ELEMENT_EDEFAULT);
                return;
            case XmlmapPackage.OUTPUT_XML_TREE__INPUT_LOOP_NODES_TABLES:
                getInputLoopNodesTables().clear();
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
            case XmlmapPackage.OUTPUT_XML_TREE__NODES:
                return nodes != null && !nodes.isEmpty();
            case XmlmapPackage.OUTPUT_XML_TREE__REJECT:
                return reject != REJECT_EDEFAULT;
            case XmlmapPackage.OUTPUT_XML_TREE__REJECT_INNER_JOIN:
                return rejectInnerJoin != REJECT_INNER_JOIN_EDEFAULT;
            case XmlmapPackage.OUTPUT_XML_TREE__ERROR_REJECT:
                return errorReject != ERROR_REJECT_EDEFAULT;
            case XmlmapPackage.OUTPUT_XML_TREE__ALL_IN_ONE:
                return allInOne != ALL_IN_ONE_EDEFAULT;
            case XmlmapPackage.OUTPUT_XML_TREE__ENABLE_EMPTY_ELEMENT:
                return enableEmptyElement != ENABLE_EMPTY_ELEMENT_EDEFAULT;
            case XmlmapPackage.OUTPUT_XML_TREE__INPUT_LOOP_NODES_TABLES:
                return inputLoopNodesTables != null && !inputLoopNodesTables.isEmpty();
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
        result.append(", errorReject: ");
        result.append(errorReject);
        result.append(", allInOne: ");
        result.append(allInOne);
        result.append(", enableEmptyElement: ");
        result.append(enableEmptyElement);
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
        OutputXmlTreeImpl other = (OutputXmlTreeImpl) obj;
        if(this.reject != other.reject){
            return false;
        }
        if(this.rejectInnerJoin != other.rejectInnerJoin){
            return false;
        }
        if(this.errorReject != other.errorReject){
            return false;
        }
        if(this.allInOne != other.allInOne){
            return false;
        }
        if(this.enableEmptyElement != other.enableEmptyElement){
            return false;
        }
        if(this.getNodes().size() != other.getNodes().size()){
            return false;
        }
        if(this.getInputLoopNodesTables().size() != other.getInputLoopNodesTables().size()){
            return false;
        }
        for(TreeNode tree:nodes){
            boolean found = false;
            for(TreeNode otherTree:other.nodes){
                if(tree.getName().equals(otherTree.getName())){
                    found = true;
                    if(!tree.equals(otherTree)){
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

} //OutputXmlTreeImpl
