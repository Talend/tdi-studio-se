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
import org.eclipse.emf.ecore.util.EObjectResolvingEList;
import org.eclipse.emf.ecore.util.InternalEList;
import org.talend.designer.xmlmap.model.emf.xmlmap.Connection;
import org.talend.designer.xmlmap.model.emf.xmlmap.FilterConnection;
import org.talend.designer.xmlmap.model.emf.xmlmap.LookupConnection;
import org.talend.designer.xmlmap.model.emf.xmlmap.NodeType;
import org.talend.designer.xmlmap.model.emf.xmlmap.TreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlmapPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Tree Node</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.talend.designer.xmlmap.model.emf.xmlmap.impl.TreeNodeImpl#getChildren <em>Children</em>}</li>
 *   <li>{@link org.talend.designer.xmlmap.model.emf.xmlmap.impl.TreeNodeImpl#getXpath <em>Xpath</em>}</li>
 *   <li>{@link org.talend.designer.xmlmap.model.emf.xmlmap.impl.TreeNodeImpl#isLoop <em>Loop</em>}</li>
 *   <li>{@link org.talend.designer.xmlmap.model.emf.xmlmap.impl.TreeNodeImpl#getNodeType <em>Node Type</em>}</li>
 *   <li>{@link org.talend.designer.xmlmap.model.emf.xmlmap.impl.TreeNodeImpl#getPattern <em>Pattern</em>}</li>
 *   <li>{@link org.talend.designer.xmlmap.model.emf.xmlmap.impl.TreeNodeImpl#isKey <em>Key</em>}</li>
 *   <li>{@link org.talend.designer.xmlmap.model.emf.xmlmap.impl.TreeNodeImpl#isGroup <em>Group</em>}</li>
 *   <li>{@link org.talend.designer.xmlmap.model.emf.xmlmap.impl.TreeNodeImpl#isMain <em>Main</em>}</li>
 *   <li>{@link org.talend.designer.xmlmap.model.emf.xmlmap.impl.TreeNodeImpl#getLookupOutgoingConnections <em>Lookup Outgoing Connections</em>}</li>
 *   <li>{@link org.talend.designer.xmlmap.model.emf.xmlmap.impl.TreeNodeImpl#getLookupIncomingConnections <em>Lookup Incoming Connections</em>}</li>
 *   <li>{@link org.talend.designer.xmlmap.model.emf.xmlmap.impl.TreeNodeImpl#getDefaultValue <em>Default Value</em>}</li>
 *   <li>{@link org.talend.designer.xmlmap.model.emf.xmlmap.impl.TreeNodeImpl#isNullable <em>Nullable</em>}</li>
 *   <li>{@link org.talend.designer.xmlmap.model.emf.xmlmap.impl.TreeNodeImpl#isChoice <em>Choice</em>}</li>
 *   <li>{@link org.talend.designer.xmlmap.model.emf.xmlmap.impl.TreeNodeImpl#isSubstitution <em>Substitution</em>}</li>
 *   <li>{@link org.talend.designer.xmlmap.model.emf.xmlmap.impl.TreeNodeImpl#isOptional <em>Optional</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class TreeNodeImpl extends AbstractNodeImpl implements TreeNode {
    /**
     * The cached value of the '{@link #getChildren() <em>Children</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getChildren()
     * @generated
     * @ordered
     */
    protected EList<TreeNode> children;

    /**
     * The default value of the '{@link #getXpath() <em>Xpath</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getXpath()
     * @generated
     * @ordered
     */
    protected static final String XPATH_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getXpath() <em>Xpath</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getXpath()
     * @generated
     * @ordered
     */
    protected String xpath = XPATH_EDEFAULT;

    /**
     * The default value of the '{@link #isLoop() <em>Loop</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isLoop()
     * @generated
     * @ordered
     */
    protected static final boolean LOOP_EDEFAULT = false;

    /**
     * The cached value of the '{@link #isLoop() <em>Loop</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isLoop()
     * @generated
     * @ordered
     */
    protected boolean loop = LOOP_EDEFAULT;

    /**
     * The default value of the '{@link #getNodeType() <em>Node Type</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getNodeType()
     * @generated
     * @ordered
     */
    protected static final NodeType NODE_TYPE_EDEFAULT = NodeType.ELEMENT;

    /**
     * The cached value of the '{@link #getNodeType() <em>Node Type</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getNodeType()
     * @generated
     * @ordered
     */
    protected NodeType nodeType = NODE_TYPE_EDEFAULT;

    /**
     * The default value of the '{@link #getPattern() <em>Pattern</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getPattern()
     * @generated
     * @ordered
     */
    protected static final String PATTERN_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getPattern() <em>Pattern</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getPattern()
     * @generated
     * @ordered
     */
    protected String pattern = PATTERN_EDEFAULT;

    /**
     * The default value of the '{@link #isKey() <em>Key</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isKey()
     * @generated
     * @ordered
     */
    protected static final boolean KEY_EDEFAULT = false;

    /**
     * The cached value of the '{@link #isKey() <em>Key</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isKey()
     * @generated
     * @ordered
     */
    protected boolean key = KEY_EDEFAULT;

    /**
     * The default value of the '{@link #isGroup() <em>Group</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isGroup()
     * @generated
     * @ordered
     */
    protected static final boolean GROUP_EDEFAULT = false;

    /**
     * The cached value of the '{@link #isGroup() <em>Group</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isGroup()
     * @generated
     * @ordered
     */
    protected boolean group = GROUP_EDEFAULT;

    /**
     * The default value of the '{@link #isMain() <em>Main</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isMain()
     * @generated
     * @ordered
     */
    protected static final boolean MAIN_EDEFAULT = false;

    /**
     * The cached value of the '{@link #isMain() <em>Main</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isMain()
     * @generated
     * @ordered
     */
    protected boolean main = MAIN_EDEFAULT;

    /**
     * The cached value of the '{@link #getLookupOutgoingConnections() <em>Lookup Outgoing Connections</em>}' reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getLookupOutgoingConnections()
     * @generated
     * @ordered
     */
    protected EList<LookupConnection> lookupOutgoingConnections;

    /**
     * The cached value of the '{@link #getLookupIncomingConnections() <em>Lookup Incoming Connections</em>}' reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getLookupIncomingConnections()
     * @generated
     * @ordered
     */
    protected EList<LookupConnection> lookupIncomingConnections;

    /**
     * The default value of the '{@link #getDefaultValue() <em>Default Value</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getDefaultValue()
     * @generated
     * @ordered
     */
    protected static final String DEFAULT_VALUE_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getDefaultValue() <em>Default Value</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getDefaultValue()
     * @generated
     * @ordered
     */
    protected String defaultValue = DEFAULT_VALUE_EDEFAULT;

    /**
     * The default value of the '{@link #isNullable() <em>Nullable</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isNullable()
     * @generated
     * @ordered
     */
    protected static final boolean NULLABLE_EDEFAULT = true;

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
     * The default value of the '{@link #isChoice() <em>Choice</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isChoice()
     * @generated
     * @ordered
     */
    protected static final boolean CHOICE_EDEFAULT = false;

    /**
     * The cached value of the '{@link #isChoice() <em>Choice</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isChoice()
     * @generated
     * @ordered
     */
    protected boolean choice = CHOICE_EDEFAULT;

    /**
     * The default value of the '{@link #isSubstitution() <em>Substitution</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isSubstitution()
     * @generated
     * @ordered
     */
    protected static final boolean SUBSTITUTION_EDEFAULT = false;

    /**
     * The cached value of the '{@link #isSubstitution() <em>Substitution</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isSubstitution()
     * @generated
     * @ordered
     */
    protected boolean substitution = SUBSTITUTION_EDEFAULT;

    /**
     * The default value of the '{@link #isOptional() <em>Optional</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isOptional()
     * @generated
     * @ordered
     */
    protected static final boolean OPTIONAL_EDEFAULT = false;

    /**
     * The cached value of the '{@link #isOptional() <em>Optional</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isOptional()
     * @generated
     * @ordered
     */
    protected boolean optional = OPTIONAL_EDEFAULT;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected TreeNodeImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return XmlmapPackage.Literals.TREE_NODE;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList<TreeNode> getChildren() {
        if (children == null) {
            children = new EObjectContainmentEList<TreeNode>(TreeNode.class, this, XmlmapPackage.TREE_NODE__CHILDREN);
        }
        return children;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getXpath() {
        return xpath;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setXpath(String newXpath) {
        String oldXpath = xpath;
        xpath = newXpath;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, XmlmapPackage.TREE_NODE__XPATH, oldXpath, xpath));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean isLoop() {
        return loop;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setLoop(boolean newLoop) {
        boolean oldLoop = loop;
        loop = newLoop;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, XmlmapPackage.TREE_NODE__LOOP, oldLoop, loop));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NodeType getNodeType() {
        return nodeType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setNodeType(NodeType newNodeType) {
        NodeType oldNodeType = nodeType;
        nodeType = newNodeType == null ? NODE_TYPE_EDEFAULT : newNodeType;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, XmlmapPackage.TREE_NODE__NODE_TYPE, oldNodeType, nodeType));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setPattern(String newPattern) {
        String oldPattern = pattern;
        pattern = newPattern;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, XmlmapPackage.TREE_NODE__PATTERN, oldPattern, pattern));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean isKey() {
        return key;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setKey(boolean newKey) {
        boolean oldKey = key;
        key = newKey;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, XmlmapPackage.TREE_NODE__KEY, oldKey, key));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean isGroup() {
        return group;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setGroup(boolean newGroup) {
        boolean oldGroup = group;
        group = newGroup;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, XmlmapPackage.TREE_NODE__GROUP, oldGroup, group));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean isMain() {
        return main;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setMain(boolean newMain) {
        boolean oldMain = main;
        main = newMain;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, XmlmapPackage.TREE_NODE__MAIN, oldMain, main));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList<LookupConnection> getLookupOutgoingConnections() {
        if (lookupOutgoingConnections == null) {
            lookupOutgoingConnections = new EObjectResolvingEList<LookupConnection>(LookupConnection.class, this, XmlmapPackage.TREE_NODE__LOOKUP_OUTGOING_CONNECTIONS);
        }
        return lookupOutgoingConnections;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList<LookupConnection> getLookupIncomingConnections() {
        if (lookupIncomingConnections == null) {
            lookupIncomingConnections = new EObjectResolvingEList<LookupConnection>(LookupConnection.class, this, XmlmapPackage.TREE_NODE__LOOKUP_INCOMING_CONNECTIONS);
        }
        return lookupIncomingConnections;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setDefaultValue(String newDefaultValue) {
        String oldDefaultValue = defaultValue;
        defaultValue = newDefaultValue;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, XmlmapPackage.TREE_NODE__DEFAULT_VALUE, oldDefaultValue, defaultValue));
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
            eNotify(new ENotificationImpl(this, Notification.SET, XmlmapPackage.TREE_NODE__NULLABLE, oldNullable, nullable));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean isChoice() {
        return choice;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setChoice(boolean newChoice) {
        boolean oldChoice = choice;
        choice = newChoice;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, XmlmapPackage.TREE_NODE__CHOICE, oldChoice, choice));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean isSubstitution() {
        return substitution;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setSubstitution(boolean newSubstitution) {
        boolean oldSubstitution = substitution;
        substitution = newSubstitution;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, XmlmapPackage.TREE_NODE__SUBSTITUTION, oldSubstitution, substitution));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean isOptional() {
        return optional;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setOptional(boolean newOptional) {
        boolean oldOptional = optional;
        optional = newOptional;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, XmlmapPackage.TREE_NODE__OPTIONAL, oldOptional, optional));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
        switch (featureID) {
            case XmlmapPackage.TREE_NODE__CHILDREN:
                return ((InternalEList<?>)getChildren()).basicRemove(otherEnd, msgs);
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
            case XmlmapPackage.TREE_NODE__CHILDREN:
                return getChildren();
            case XmlmapPackage.TREE_NODE__XPATH:
                return getXpath();
            case XmlmapPackage.TREE_NODE__LOOP:
                return isLoop();
            case XmlmapPackage.TREE_NODE__NODE_TYPE:
                return getNodeType();
            case XmlmapPackage.TREE_NODE__PATTERN:
                return getPattern();
            case XmlmapPackage.TREE_NODE__KEY:
                return isKey();
            case XmlmapPackage.TREE_NODE__GROUP:
                return isGroup();
            case XmlmapPackage.TREE_NODE__MAIN:
                return isMain();
            case XmlmapPackage.TREE_NODE__LOOKUP_OUTGOING_CONNECTIONS:
                return getLookupOutgoingConnections();
            case XmlmapPackage.TREE_NODE__LOOKUP_INCOMING_CONNECTIONS:
                return getLookupIncomingConnections();
            case XmlmapPackage.TREE_NODE__DEFAULT_VALUE:
                return getDefaultValue();
            case XmlmapPackage.TREE_NODE__NULLABLE:
                return isNullable();
            case XmlmapPackage.TREE_NODE__CHOICE:
                return isChoice();
            case XmlmapPackage.TREE_NODE__SUBSTITUTION:
                return isSubstitution();
            case XmlmapPackage.TREE_NODE__OPTIONAL:
                return isOptional();
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
            case XmlmapPackage.TREE_NODE__CHILDREN:
                getChildren().clear();
                getChildren().addAll((Collection<? extends TreeNode>)newValue);
                return;
            case XmlmapPackage.TREE_NODE__XPATH:
                setXpath((String)newValue);
                return;
            case XmlmapPackage.TREE_NODE__LOOP:
                setLoop((Boolean)newValue);
                return;
            case XmlmapPackage.TREE_NODE__NODE_TYPE:
                setNodeType((NodeType)newValue);
                return;
            case XmlmapPackage.TREE_NODE__PATTERN:
                setPattern((String)newValue);
                return;
            case XmlmapPackage.TREE_NODE__KEY:
                setKey((Boolean)newValue);
                return;
            case XmlmapPackage.TREE_NODE__GROUP:
                setGroup((Boolean)newValue);
                return;
            case XmlmapPackage.TREE_NODE__MAIN:
                setMain((Boolean)newValue);
                return;
            case XmlmapPackage.TREE_NODE__LOOKUP_OUTGOING_CONNECTIONS:
                getLookupOutgoingConnections().clear();
                getLookupOutgoingConnections().addAll((Collection<? extends LookupConnection>)newValue);
                return;
            case XmlmapPackage.TREE_NODE__LOOKUP_INCOMING_CONNECTIONS:
                getLookupIncomingConnections().clear();
                getLookupIncomingConnections().addAll((Collection<? extends LookupConnection>)newValue);
                return;
            case XmlmapPackage.TREE_NODE__DEFAULT_VALUE:
                setDefaultValue((String)newValue);
                return;
            case XmlmapPackage.TREE_NODE__NULLABLE:
                setNullable((Boolean)newValue);
                return;
            case XmlmapPackage.TREE_NODE__CHOICE:
                setChoice((Boolean)newValue);
                return;
            case XmlmapPackage.TREE_NODE__SUBSTITUTION:
                setSubstitution((Boolean)newValue);
                return;
            case XmlmapPackage.TREE_NODE__OPTIONAL:
                setOptional((Boolean)newValue);
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
            case XmlmapPackage.TREE_NODE__CHILDREN:
                getChildren().clear();
                return;
            case XmlmapPackage.TREE_NODE__XPATH:
                setXpath(XPATH_EDEFAULT);
                return;
            case XmlmapPackage.TREE_NODE__LOOP:
                setLoop(LOOP_EDEFAULT);
                return;
            case XmlmapPackage.TREE_NODE__NODE_TYPE:
                setNodeType(NODE_TYPE_EDEFAULT);
                return;
            case XmlmapPackage.TREE_NODE__PATTERN:
                setPattern(PATTERN_EDEFAULT);
                return;
            case XmlmapPackage.TREE_NODE__KEY:
                setKey(KEY_EDEFAULT);
                return;
            case XmlmapPackage.TREE_NODE__GROUP:
                setGroup(GROUP_EDEFAULT);
                return;
            case XmlmapPackage.TREE_NODE__MAIN:
                setMain(MAIN_EDEFAULT);
                return;
            case XmlmapPackage.TREE_NODE__LOOKUP_OUTGOING_CONNECTIONS:
                getLookupOutgoingConnections().clear();
                return;
            case XmlmapPackage.TREE_NODE__LOOKUP_INCOMING_CONNECTIONS:
                getLookupIncomingConnections().clear();
                return;
            case XmlmapPackage.TREE_NODE__DEFAULT_VALUE:
                setDefaultValue(DEFAULT_VALUE_EDEFAULT);
                return;
            case XmlmapPackage.TREE_NODE__NULLABLE:
                setNullable(NULLABLE_EDEFAULT);
                return;
            case XmlmapPackage.TREE_NODE__CHOICE:
                setChoice(CHOICE_EDEFAULT);
                return;
            case XmlmapPackage.TREE_NODE__SUBSTITUTION:
                setSubstitution(SUBSTITUTION_EDEFAULT);
                return;
            case XmlmapPackage.TREE_NODE__OPTIONAL:
                setOptional(OPTIONAL_EDEFAULT);
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
            case XmlmapPackage.TREE_NODE__CHILDREN:
                return children != null && !children.isEmpty();
            case XmlmapPackage.TREE_NODE__XPATH:
                return XPATH_EDEFAULT == null ? xpath != null : !XPATH_EDEFAULT.equals(xpath);
            case XmlmapPackage.TREE_NODE__LOOP:
                return loop != LOOP_EDEFAULT;
            case XmlmapPackage.TREE_NODE__NODE_TYPE:
                return nodeType != NODE_TYPE_EDEFAULT;
            case XmlmapPackage.TREE_NODE__PATTERN:
                return PATTERN_EDEFAULT == null ? pattern != null : !PATTERN_EDEFAULT.equals(pattern);
            case XmlmapPackage.TREE_NODE__KEY:
                return key != KEY_EDEFAULT;
            case XmlmapPackage.TREE_NODE__GROUP:
                return group != GROUP_EDEFAULT;
            case XmlmapPackage.TREE_NODE__MAIN:
                return main != MAIN_EDEFAULT;
            case XmlmapPackage.TREE_NODE__LOOKUP_OUTGOING_CONNECTIONS:
                return lookupOutgoingConnections != null && !lookupOutgoingConnections.isEmpty();
            case XmlmapPackage.TREE_NODE__LOOKUP_INCOMING_CONNECTIONS:
                return lookupIncomingConnections != null && !lookupIncomingConnections.isEmpty();
            case XmlmapPackage.TREE_NODE__DEFAULT_VALUE:
                return DEFAULT_VALUE_EDEFAULT == null ? defaultValue != null : !DEFAULT_VALUE_EDEFAULT.equals(defaultValue);
            case XmlmapPackage.TREE_NODE__NULLABLE:
                return nullable != NULLABLE_EDEFAULT;
            case XmlmapPackage.TREE_NODE__CHOICE:
                return choice != CHOICE_EDEFAULT;
            case XmlmapPackage.TREE_NODE__SUBSTITUTION:
                return substitution != SUBSTITUTION_EDEFAULT;
            case XmlmapPackage.TREE_NODE__OPTIONAL:
                return optional != OPTIONAL_EDEFAULT;
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
        result.append(" (xpath: ");
        result.append(xpath);
        result.append(", loop: ");
        result.append(loop);
        result.append(", nodeType: ");
        result.append(nodeType);
        result.append(", pattern: ");
        result.append(pattern);
        result.append(", key: ");
        result.append(key);
        result.append(", group: ");
        result.append(group);
        result.append(", main: ");
        result.append(main);
        result.append(", defaultValue: ");
        result.append(defaultValue);
        result.append(", nullable: ");
        result.append(nullable);
        result.append(", choice: ");
        result.append(choice);
        result.append(", substitution: ");
        result.append(substitution);
        result.append(", optional: ");
        result.append(optional);
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
        TreeNodeImpl other = (TreeNodeImpl) obj;
        if (this.xpath == null) {
            if (other.xpath != null) {
                return false;
            }
        } else if (!this.xpath.equals(other.xpath)) {
            return false;
        }
        if (this.pattern == null) {
            if (other.pattern != null) {
                return false;
            }
        } else if (!this.pattern.equals(other.pattern)) {
            return false;
        }
        if (this.nodeType == null) {
            if (other.nodeType != null) {
                return false;
            }
        } else if (this.nodeType.getValue() != other.nodeType.getValue()) {
            return false;
        }else if (!this.nodeType.getLiteral().equals(other.nodeType.getLiteral())) {
            return false;
        }else if (!this.nodeType.getName().equals(other.nodeType.getName())) {
            return false;
        }
        if(this.group != other.group){
            return false;
        }
        if(this.nullable != other.nullable){
            return false;
        }
        if(this.key != other.key){
            return false;
        }
        if(this.choice != other.choice){
            return false;
        }
        if(this.optional != other.optional){
            return false;
        }
        if(this.loop != other.loop){
            return false;
        }
        if(this.main != other.main){
            return false;
        }
        if(this.substitution != other.substitution){
            return false;
        }
        if(this.getChildren().size() != this.getChildren().size()){
            return false;
        }
        if(this.getLookupIncomingConnections().size() != this.getLookupIncomingConnections().size()){
            return false;
        }
        if(this.getLookupOutgoingConnections().size() != this.getLookupOutgoingConnections().size()){
            return false;
        }
        
        for(TreeNode inputTable:getChildren()){
            boolean found = false;
            for(TreeNode input:other.getChildren()){
                if(inputTable.getName().equals(input.getName())){
                    found = true;
                    if(!inputTable.equals(input)){
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

} //TreeNodeImpl
