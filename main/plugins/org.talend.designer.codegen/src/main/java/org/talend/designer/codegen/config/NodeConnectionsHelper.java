// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.codegen.config;

import java.util.ArrayList;
import java.util.List;

import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IConnectionCategory;
import org.talend.core.model.process.INode;

/**
 * Use for wrap a {@link INode} to provide faster and convenient methods to get
 * connections, such as {@link #inputConn}, {@link #connResponse},
 * {@link #connFault}, {@link #dataOutputs}.
 *
 * @author GaoZone
 * @update 2013-9-12 [TESB-11028]
 */
public class NodeConnectionsHelper {

	/** The node. */
	private final INode node;

	/** The input conn. */
	private IConnection inputConn;

	/** The conn response. */
	private IConnection connResponse;

	/** The conn fault. */
	private IConnection connFault;

	/** The data outputs. */
	private List<IConnection> dataOutputs;

	/**
	 * Gets the input conn.
	 *
	 * @return the input conn
	 */
	public IConnection getInputConn() {
		return inputConn;
	}

	/**
	 * Gets the output conn response.
	 *
	 * @return the output conn response
	 */
	public IConnection getOutputConnResponse() {
		return connResponse;
	}

	/**
	 * Gets the output conn fault.
	 *
	 * @return the output conn fault
	 */
	public IConnection getOutputConnFault() {
		return connFault;
	}

	/**
	 * Gets the data outputs.
	 *
	 * @return the data outputs
	 */
	public List<IConnection> getDataOutputs() {
		return dataOutputs;
	}

	/**
	 * Instantiates a new node connections helper.
	 *
	 * @param node
	 *            the node
	 */
	public NodeConnectionsHelper(INode node) {
		this(node, false);
	}

	/**
	 * Instantiates a new node connections helper.
	 *
	 * @param node
	 *            the node
	 * @param stopInitialWhenNoInput
	 *            the stop initial when no input
	 */
	public NodeConnectionsHelper(INode node, boolean stopInitialWhenNoInput) {
		this.node = node;
		initConnections(stopInitialWhenNoInput);
	}

	/**
	 * Inits the connections.
	 *
	 * @param stopInitialWhenNoInput
	 *            the stop initial when no input
	 */
	private void initConnections(boolean stopInitialWhenNoInput) {
		initInputConnection();
		if (!(stopInitialWhenNoInput && inputConn == null)) {
			initOutputConnections();
		}
	}

	/**
	 * Inits the {@link #inputConn} connection.
	 */
	private void initInputConnection() {
		List<? extends IConnection> incomingConnections = node.getIncomingConnections();
		if (incomingConnections != null) {
			for (IConnection conn : incomingConnections) {
				if (conn.getLineStyle().hasConnectionCategory(IConnectionCategory.DATA)) {
					inputConn = conn;
					break;
				}
			}
		}
	}

	/**
	 * Inits the {@link #dataOutputs}, {@link #connResponse}, {@link #connFault}
	 * connections.
	 */
	private void initOutputConnections() {
		List<? extends IConnection> conns = node.getOutgoingSortedConnections();
		dataOutputs = new ArrayList<IConnection>();
		if (conns != null) {
			for (IConnection conn : conns) {
				if (conn.getLineStyle().hasConnectionCategory(IConnectionCategory.DATA)) {
					dataOutputs.add(conn);
				}
			}
		}

		List<? extends IConnection> connsResponse = node.getOutgoingConnections("RESPONSE");
		List<? extends IConnection> connsFault = node.getOutgoingConnections("FAULT");
		if (connsResponse.size() == 1) {
			connResponse = connsResponse.get(0);
		}
		if (connsFault.size() == 1) {
			connFault = connsFault.get(0);
		}
	}

}
