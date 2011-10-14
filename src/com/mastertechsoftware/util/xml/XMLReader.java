/*
 * 
 * @author Kevin Moore
 *
 */
package com.mastertechsoftware.util.xml;

import com.mastertechsoftware.util.log.Logger;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class reads in an xml file from either a file or a string
 *
 * @author Kevin Moore
 */
public class XMLReader {
    private File xmlFile;
    private String xmlString;
    private XMLNode rootXMLNode;
    private InputStream xmlStream;
    private XMLParser parser;

    public XMLReader() {
    	parser = new XMLParser();
	}

	/**
     * Constructor
     *
     * @param xmlStream
     */
    public XMLReader(InputStream xmlStream) {
    	this();
        this.xmlStream = xmlStream;
    }

    /**
     * Constructor
     *
     * @param xmlFile
     */
    public XMLReader(File xmlFile) {
    	this();
        this.xmlFile = xmlFile;
    }

    /**
     * Alternate constructor. Pass in a string representing an xml file
     *
     * @param xmlString
     */
    public XMLReader(String xmlString) {
    	this();
        this.xmlString = xmlString;
    }


    /**
     * Build the Node from the root
     *
     * @return XMLNode
     * @throws XMLException
     */
    public XMLNode buildXMLNode() throws XMLException {
        long start = System.currentTimeMillis();
        if (xmlFile != null && xmlFile.exists()) {
            try {
                rootXMLNode = parser.parse(new FileInputStream(xmlFile));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new XMLException("File Not Found", e);
            }
        } else if (xmlString != null && xmlString.length() > 0) {
            rootXMLNode = parser.parse(new ByteArrayInputStream(xmlString.getBytes()));
        } else if (xmlStream != null) {
            rootXMLNode = parser.parse(xmlStream);
       }
		long end = System.currentTimeMillis();
		Logger.debug("Total Build Time took " + ((end - start)) + " milliseconds");
		return rootXMLNode;
    }

    public XMLNode getRootXMLNode() {
		return rootXMLNode;
	}

	public XMLParser getParser() {
		return parser;
	}

	/**
     * Find the node with the given name
     *
     * @param node
     * @param nodeName
     * @return Node
     */
    public XMLNode findNodeName(XMLNode node, String nodeName) {
        if (node == null)
            node = rootXMLNode;
        if (node == null)
            return null;
        if (node.getNodeName() == null)
            return null;
        if (node.getNodeName().equalsIgnoreCase(nodeName)) {
            return node;
        }
        List<XMLNode> children = node.getChildNodes();
        if (children == null)
            return null;
        Iterator<XMLNode> iterator = children.iterator();
        while (iterator.hasNext()) {
            XMLNode childNode = iterator.next();
            if (childNode.getNodeName().equalsIgnoreCase(nodeName)) {
                return childNode;
            }
            XMLNode foundNode = findNodeName(childNode, nodeName);
            if (foundNode != null)
                return foundNode;
        }
        return null;
    }

    /**
     * Find all of the nodes with the given node name
     *
     * @param startingNode
     * @param nodeName
     * @return ArrayList
     */
    public ArrayList<XMLNode> findAllNodes(XMLNode startingNode, String nodeName) {
        ArrayList<XMLNode> nodeList = new ArrayList<XMLNode>();
        if (startingNode == null)
            startingNode = rootXMLNode;
        if (startingNode == null)
            return new ArrayList<XMLNode>();
        List<XMLNode> children = startingNode.getChildNodes();
        if (children == null)
            return new ArrayList<XMLNode>();
        Iterator<XMLNode> iterator = children.iterator();
        while (iterator.hasNext()) {
            XMLNode childNode = iterator.next();
            if (childNode.getNodeName().equalsIgnoreCase(nodeName)) {
                nodeList.add(childNode);
            }
            ArrayList<XMLNode> childNodes = findAllNodes(childNode, nodeName);
            if (childNodes != null && childNodes.size() > 0)
                nodeList.addAll(childNodes);
        }
        return nodeList;
    }

    /**
     * Find all of the nodes with the given node name
     *
     * @param startingNode
     * @param nodeName
     * @return ArrayList
     */
    public ArrayList<XMLNode> findAllNodesOneLevel(XMLNode startingNode, String nodeName) {
        ArrayList<XMLNode> nodeList = new ArrayList<XMLNode>();
        if (startingNode == null)
            startingNode = rootXMLNode;
        if (startingNode == null)
            return new ArrayList<XMLNode>();
        if (startingNode == rootXMLNode && nodeName.equalsIgnoreCase(startingNode.getNodeName())) {
            nodeList.add(startingNode);
        }
        List<XMLNode> children = startingNode.getChildNodes();
        if (children == null)
            return new ArrayList<XMLNode>();
        Iterator<XMLNode> iterator = children.iterator();
        boolean found = false;
        while (iterator.hasNext()) {
            XMLNode childNode = iterator.next();
            if (childNode.getNodeName().equalsIgnoreCase(nodeName)) {
                nodeList.add(childNode);
                found = true;
            }
            if (!found) {
                ArrayList<XMLNode> childNodes = findAllNodes(childNode, nodeName);
                if (childNodes != null && childNodes.size() > 0)
                    nodeList.addAll(childNodes);
            }
        }
        return nodeList;
    }

    /**
     * Find the node with the given name
     *
     * @param node
     * @param nodeName
     * @param attributeName
     * @param attributeValue
     * @return Node
     */
    public XMLNode findNodeAttributeValue(XMLNode node, String nodeName, String attributeName, String attributeValue) {
        if (node == null)
            node = rootXMLNode;
        if (node == null)
            return null;
        if (node.getNodeName().equalsIgnoreCase(nodeName)) {
            if (node.getAttributeValue(attributeName) != null)
                return node;
        }
        List<XMLNode> children = node.getChildNodes();
        if (children == null)
            return null;
        Iterator<XMLNode> iterator = children.iterator();
        while (iterator.hasNext()) {
            XMLNode childNode = iterator.next();
            if (childNode.getNodeName().equalsIgnoreCase(nodeName)) {
                if (childNode.getAttributeValue(attributeValue) != null)
                    return childNode;
            }
            XMLNode foundNode = findNodeName(childNode, nodeName);
            if (foundNode != null)
                return foundNode;
        }
        return null;
    }

    /**
     * @return Returns the rootElement.
     */
    public XMLNode getRootNode() {
        return rootXMLNode;
    }

    /**
	 * @param rootNode The rootElement to set.
	 */
	public void setRootNode(XMLNode rootNode)
	{
		this.rootXMLNode = rootNode;
	}
	
	/**
	 * Set all of the filters for the parser.
	 * @param xmlNodeFilters
	 */
	public void setXmlNodeFilters(List<XMLNodeFilter> xmlNodeFilters) {
		if (parser != null) {
			parser.setXmlNodeFilters(xmlNodeFilters);
		}
	}
}
