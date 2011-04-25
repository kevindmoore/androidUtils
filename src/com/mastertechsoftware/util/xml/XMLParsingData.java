package com.mastertechsoftware.util.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class XMLParsingData {
	public static final int READ_CURRENT_NODE = 1; 
	public static final int READ_NODE_AND_CHILDREN = 2; 
	public static final int IGNORE_NODE = 3;
	public static final int BYPASS = 4;

	private XMLNode root = new XMLNode();
	private XMLNode currentNode;
	private String currentParsingString;
	private boolean usingFilters = false;
	private List<XMLNodeFilter> xmlNodeFilters = new ArrayList<XMLNodeFilter>();
	private Stack<Integer> currentFilterActions = new Stack<Integer>();
	private Stack<String> currentParsingStrings = new Stack<String>();
	private Stack<XMLNode> currentNodes = new Stack<XMLNode>();
	private int currentFilterAction = -1;
	private String currentParentName;

	public XMLParsingData() {
		currentNode = root;
	}
	
	public XMLNode getRoot() {
		return root;
	}
	
	public void setRoot(XMLNode root) {
		this.root = root;
	}
	
	public XMLNode getCurrentNode() {
		return currentNode;
	}
	
	public void setCurrentNode(XMLNode currentNode) {
		this.currentNode = currentNode;
	}
	
	public boolean isUsingFilters() {
		return usingFilters;
	}
	
	public void setUsingFilters(boolean usingFilters) {
		this.usingFilters = usingFilters;
	}
	
	public List<XMLNodeFilter> getXmlNodeFilters() {
		return xmlNodeFilters;
	}

	public void setXmlNodeFilters(List<XMLNodeFilter> xmlNodeFilters) {
		this.xmlNodeFilters = xmlNodeFilters;
		if (xmlNodeFilters != null && xmlNodeFilters.size() > 0) {
			usingFilters = true;			
		}
	}
	
	/**
	 * Given a list of filters, pass the nodes to each filter to see if they want to handle it
	 * @param nodeName
	 */
	protected void handleFilter(String nodeName) {
		if (xmlNodeFilters == null) {
			return;
		}
		int size = xmlNodeFilters.size();
		if (size == 0) {
			return;
		}
		for (int i=0; i < size; i++) {
			int action = xmlNodeFilters.get(i).filterNode(currentParentName, nodeName);
			if (action != BYPASS) {
//				Log.d("handlerFilter", xmlNodeFilters.get(i).getClass().toString() + " returned action " + 
//						(action == 1 ? "Current Node" : (action == 2 ? "Node & Children" : "Ignore")) + " for node " + nodeName);
				currentFilterAction = action;
				break;
			}
		}
	}

	public Stack<Integer> getCurrentFilterActions() {
		return currentFilterActions;
	}

	public void setCurrentFilterActions(Stack<Integer> currentFilterActions) {
		this.currentFilterActions = currentFilterActions;
	}

	public String getCurrentParsingString() {
		return currentParsingString;
	}

	public void setCurrentParsingString(String currentParsingString) {
		this.currentParsingString = currentParsingString;
	}

	public void pushParsingString(String xmlString) {
		currentParsingStrings.add(xmlString);
		currentParsingString = xmlString;
	}

	public String popParsingString() {
		String popedString;
		if (currentParsingStrings.empty()) {
			currentParsingString = null;
			return null;
		} else {
			popedString = currentParsingStrings.pop();
		}
		if (!currentParsingStrings.empty()) {
			currentParsingString = currentParsingStrings.peek();
		}
		return popedString;
	}

	public void pushCurrentNode(XMLNode node) {
		currentNodes.add(node);
		currentNode = node;
	}
	
	public void addCurrentNode(XMLNode node) {
		if (currentNode == null) {
			currentNode = node;
			return;
		}
		currentNode.addChildNode(node);
	}
	
	public XMLNode popCurrentNode() {
		XMLNode popedNode = null;
		if (currentNodes.empty()) {
			currentNode = null;
			return null;
		} else {
			popedNode = currentNodes.pop();
		}
		if (!currentNodes.empty()) {
			currentNode = currentNodes.peek();
		}
		return popedNode;
	}
	
	public String getCurrentParentName() {
		return currentParentName;
	}
	
	public void setCurrentParentName(String currentParentName) {
		this.currentParentName = currentParentName;
	}
	
	public int getCurrentFilterAction() {
		return currentFilterAction;
	}
	
	public void setCurrentFilterAction(int currentFilterAction) {
		this.currentFilterAction = currentFilterAction;
	}
	
	public void pushCurrentFilterAction() {
		currentFilterActions.push(currentFilterAction);
	}
	
	public void pushCurrentAction(int action) {
		currentFilterActions.push(action);
	}
	
	public int popCurrentAction() {
		if (!currentFilterActions.isEmpty()) {
			currentFilterAction = currentFilterActions.pop();
			if (!currentFilterActions.isEmpty()) {
				currentFilterAction = currentFilterActions.peek();
			}
		}
		return currentFilterAction;
	}
	
}
