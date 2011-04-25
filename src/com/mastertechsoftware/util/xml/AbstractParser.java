package com.mastertechsoftware.util.xml;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractParser implements ParsingInterface {
	private final static Pattern attributeSetPattern = Pattern
		.compile("\\s*([^=]+?)=\"([^\"]*)\"\\s*?");

	protected XMLParsingEngine engine;
	
	public AbstractParser(XMLParsingEngine engine) {
		this.engine = engine;
	}
	
	public abstract boolean didParse(XMLParsingData data);

	/**
	 * Add attributes to the current Node
	 * @param data
	 * @param attributes
	 */
	protected void addAttributes(XMLParsingData data, String attributes) {
		if (attributes != null && attributes.length() > 0) {
			Matcher attributeMatcher = attributeSetPattern.matcher(attributes);
			while (attributeMatcher.find()) {
				String attribute = attributeMatcher.group(1).trim();
				String value = attributeMatcher.group(2).trim();
				data.getCurrentNode().addAttribute(attribute, value);
			}
		}
	}
	
	protected void addAttributes(XMLNode node, String attributes) {
		if (attributes != null && attributes.length() > 0) {
			Matcher attributeMatcher = attributeSetPattern.matcher(attributes);
			while (attributeMatcher.find()) {
				String attribute = attributeMatcher.group(1).trim();
				String value = attributeMatcher.group(2).trim();
				node.addAttribute(attribute, value);
			}
		}
	}
	
	protected XMLNode addNode(XMLParsingData data, String nodeName) {
		XMLNode root = data.getRoot();
		if (root.getNodeName() == null) {
			root.setNodeName(nodeName);
			return root;
		} else {
			XMLNode childNode = new XMLNode(nodeName);
			data.getCurrentNode().addChildNode(childNode);
			data.setCurrentNode(childNode);
			return childNode;
		}		
	}

	protected XMLNode createNode(XMLParsingData data, String nodeName) {
		XMLNode childNode = new XMLNode(nodeName);
		return childNode;
	}
}
