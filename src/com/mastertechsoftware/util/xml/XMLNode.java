/*
 * @author Kevin Moore
 *
 */
package com.mastertechsoftware.util.xml;


import com.mastertechsoftware.list.MapList;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

/**
 * @author Kevin Moore
 *         Class for storing XML Node values
 */
public class XMLNode {
    protected String nodeName;
    protected HashMap<String, String> attributes;
//    protected ArrayList<XMLNode> childNodes;
    protected MapList<String, XMLNode> childNodes = new MapList<String, XMLNode>();
    private String value;

    /**
     * Constructor.
     */
    public XMLNode() {
    }

    /**
     * Constructor
     *
     * @param name
     */
    public XMLNode(String name) {
        nodeName = name;
    }

    /**
     * Constructor
     *
     * @param name
     * @param value
     */
    public XMLNode(String name, String value) {
        nodeName = name;
        this.value = value;
    }

    /**
     * Add a attribute to the node
     *
     * @param attribute
     * @param value
     */
    public void addAttribute(String attribute, String value) {
        if (attributes == null) {
            attributes = new HashMap<String, String>();
        }
        attributes.put(attribute, value);
    }

    /**
     * Test to see if this node has the given attribute
     *
     * @param attribute
     * @return true or false
     */
    public boolean hasAttribute(String attribute) {
        if (attributes == null || attributes.size() == 0 )
            return false;
        if (attributes.get(attribute) != null)
            return true;
        else
            return false;
    }

    /**
     * Test to see if this node has the given attribute value
     *
     * @param attribute
     * @param value
     */
    public boolean hasAttributeValue(String attribute, String value) {
        if (attributes == null || attributes.size() == 0 )
            return false;
        String attribValue = (String) attributes.get(attribute);
        if (attribValue == null)
            return false;
        else if (attribValue.equals(value))
            return true;
        else
            return false;
    }

    /**
     * Test to see if this node has any children with the given attribute value
     *
     * @param attribute
     * @param value
     */
    public boolean hasChildWithAttributeValue(String attribute, String value) {
        if (childNodes == null || childNodes.size() == 0 )
            return false;
//        int size = childNodes.size();
//        for (int i=0; i < size; i++) {
        Set<String> keys = childNodes.keySet();
        for (String key : keys) {
            List<XMLNode> xmlNodes = childNodes.getList(key);
            for (XMLNode node : xmlNodes) {

//            XMLNode node = childNodes.get(i);
                if (node.hasAttributeValue(attribute, value))
                    return true;
                if (node.hasChildWithAttributeValue(attribute, value))
                    return true;
            }
        }
        return false;
    }

    /**
     * Test to see if this node has the given child
     *
     * @param child XMLNode
     */
    public boolean hasChild(XMLNode child) {
        if (childNodes == null || childNodes.size() == 0 )
            return false;
//        int size = childNodes.size();
//        for (int i=0; i < size; i++) {
//            XMLNode node = (XMLNode) childNodes.get(i);
        Set<String> keys = childNodes.keySet();
        for (String key : keys) {
            List<XMLNode> xmlNodes = childNodes.getList(key);
            for (XMLNode node : xmlNodes) {
                if (node.equals(child))
                    return true;
                if (node.hasChild(child))
                    return true;
            }
        }
        return false;
    }

    /**
     * Test to see if this node has the given child
     *
     * @param name String
     */
    public boolean hasChild(String name) {
        if (childNodes == null || childNodes.size() == 0 )
            return false;
//        int size = childNodes.size();
//        for (int i=0; i < size; i++) {
//            XMLNode node = (XMLNode) childNodes.get(i);
        Set<String> keys = childNodes.keySet();
        for (String key : keys) {
            List<XMLNode> xmlNodes = childNodes.getList(key);
            for (XMLNode node : xmlNodes) {
                if (node.getNodeName().equals(name))
                    return true;
                if (node.hasChild(name))
                    return true;
            }
        }
        return false;
    }

    /**
     * Test for full equality
     */
    @Override
	public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof XMLNode))
            return false;
        XMLNode otherNode = (XMLNode) obj;
        if (!(otherNode.getNodeName().equals(nodeName)))
            return false;
        Iterator<String> iter = attributes.keySet().iterator();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            if (!otherNode.hasAttributeValue(key, (String) attributes.get(key)))
                return false;
        }
        return true;
    }

    /**
     * Get the attribute value
     *
     * @param attribute
     * @return value
     */
    public String getAttributeValue(String attribute) {
        if (attributes == null || attributes.size() == 0 )
            return "";
        return (String) attributes.get(attribute);
    }

    /**
     * Remove an attribute
     *
     * @param attribute
     */
    public void removeAttribute(String attribute) {
        if (attributes == null || attributes.size() == 0 )
            return;
        attributes.remove(attribute);
    }

    /**
     * Remove all Child elements
     */
    public void removeChildren() {
        if (childNodes == null || childNodes.size() == 0 )
            return;
        childNodes.clear();
    }

    /**
     * Remove child node
     *
     * @param child
     */
    public void removeChildNode(XMLNode child) {
        if (child == null)
            return;
        if (childNodes == null) {
            return;
        }
        childNodes.remove(child);
    }


    /**
     * Add a child node
     *
     * @param node
     */
    public void addChildNode(XMLNode node) {
        if (node == null)
            return;
//        if (childNodes == null) {
//            childNodes = new ArrayList<XMLNode>();
//        }
//        childNodes.add(node);
        childNodes.put(node.getNodeName(), node);
    }

    /**
     * Find a child node with the given name
     *
     * @param name
     * @return XMLNode
     */
    public XMLNode getChildNode(String name) {
        if ((childNodes == null) || childNodes.size() == 0 || (name == null || name.length() == 0))
            return null;
        // 1st look through the top level
        List<XMLNode> xmlNodes = childNodes.getList(name);
        if (xmlNodes != null) {
            for (XMLNode node : xmlNodes) {
                if (name.equals(node.getNodeName()))
                    return node;
            }
        }
        // Now look through the children
        Set<String> keys = childNodes.keySet();
        for (String key : keys) {
            xmlNodes = childNodes.getList(key);
            for (XMLNode node : xmlNodes) {
                XMLNode foundNode = node.getChildNode(name);
                if (foundNode != null)
                    return foundNode;
            }
        }
//        int size = childNodes.size();
//
//        // 1st look through the top level
//        for (int i=0; i < size; i++) {
//			XMLNode node = (XMLNode) childNodes.get(i);
//			if (name.equals(node.getNodeName()))
//				return node;
//        }
//
//        // Now look through the children
//        for (int i=0; i < size; i++) {
//			XMLNode node = (XMLNode) childNodes.get(i);
//        	XMLNode foundNode = node.getChildNode(name);
//        	if (foundNode != null)
//        		return foundNode;
//        }
        return null;
    }

    /**
     * Find a child node with the given name at the current level (don't recurse)
     *
     * @param name
     * @return XMLNode
     */
    public XMLNode getFirstChildNode(String name) {
    	if ((childNodes == null) || childNodes.size() == 0 || (name == null || name.length() == 0))
    		return null;
        Set<String> keys = childNodes.keySet();
        for (String key : keys) {
            XMLNode node  = childNodes.get(key);
            if (name.equals(node.getNodeName()))
                return node;
        }
//    	int size = childNodes.size();
//
//    	// 1st look through the top level
//    	for (int i=0; i < size; i++) {
//    		XMLNode node = (XMLNode) childNodes.get(i);
//    		if (name.equals(node.getNodeName()))
//    			return node;
//    	}
    	return null;
    }
    
   /**
     * Find a child node with the given attribute name
     *
     * @param childName
     * @param attribute
     * @return XMLNode
     */
    public XMLNode getChildNodeAttribute(String childName, String attribute) {
        if ((childNodes == null) || childNodes.size() == 0  || (childName == null || childName.length() == 0))
            return null;
//        int size = childNodes.size();
//        for (int i=0; i < size; i++) {
//            XMLNode node = (XMLNode) childNodes.get(i);
       Set<String> keys = childNodes.keySet();
       for (String key : keys) {
           List<XMLNode> xmlNodes = childNodes.getList(key);
           for (XMLNode node : xmlNodes) {
                if (!(childName.equals(node.getNodeName())))
                    continue;
                Map<String, String> attributes = node.getAttributes();
                if (attributes != null) {
                    Iterator<String> attribIter = attributes.keySet().iterator();
                    while (attribIter.hasNext()) {
                        String attributeName = (String) attribIter.next();
                        if (attribute.equals(attributeName))
                            return node;

                    }
                }
                XMLNode foundNode = node.getChildNodeAttribute(childName, attribute);
                if (foundNode != null)
                    return foundNode;
           }
        }
        return null;
    }

    /**
     * @return Returns the attributes.
     */
    public Map<String, String> getAttributes() {
        return attributes;
    }

    /**
     * @param attributes The attributes to set.
     */
    public void setAttributes(HashMap<String,String> attributes) {
        this.attributes = attributes;
    }

    /**
     * @return Returns the childNodes.
     */
    public List<XMLNode> getChildNodes() {
        List<XMLNode> childNodeList = new ArrayList<XMLNode>();
        Set<String> keys = childNodes.keySet();
        for (String key : keys) {
            List<XMLNode> xmlNodes = childNodes.getList(key);
            for (XMLNode node : xmlNodes) {
                childNodeList.add(node);
            }
        }
        return childNodeList;
    }

    public List<XMLNode> getChildNodes(String searchNodeName) {
    	ArrayList<XMLNode> foundNodes = new ArrayList<XMLNode>();
		searchChildNodes(this, searchNodeName, foundNodes);
    	return foundNodes;
    }

    private void searchChildNodes(XMLNode searchNode, String searchNodeName, ArrayList<XMLNode> foundNodes) {
    	if (searchNode == null || searchNode.childNodes == null) {
    		return;
    	}
//    	int size = searchNode.childNodes.size();
//    	for (int i=0; i < size; i++) {
//    		XMLNode childNode = searchNode.childNodes.get(i);
        Set<String> keys = searchNode.childNodes.keySet();
        for (String key : keys) {
            List<XMLNode> xmlNodes = searchNode.childNodes.getList(key);
            for (XMLNode childNode : xmlNodes) {
                if (childNode.getNodeName().equalsIgnoreCase(searchNodeName)) {
                    foundNodes.add(childNode);
                }
                if (childNode.getChildNodeCount() > 0) {
                    searchChildNodes(childNode, searchNodeName, foundNodes);
                }
            }
    	}    	
    }
    
    /**
     * Return the nth item
     *
     * @param index
     * @return XMLNode
     */
    public XMLNode getChild(int index) {
        if (childNodes == null)
            return null;
        return childNodes.get(index);
    }

    /**
     * @return Returns the number of children.
     */
    public int getChildNodeCount() {
        if (childNodes == null)
            return 0;
        return childNodes.size();
    }

    /**
     * @param childNodes The childNodes to set.
     */
    public void setChildeNodes(ArrayList<XMLNode> childNodes) {
        for (XMLNode childNode : childNodes) {
            addChildNode(childNode);
        }
//        this.childNodes = childNodes;
    }

    /**
     * @return Returns the nodeName.
     */
    public String getNodeName() {
        return nodeName;
    }

    /**
     * @param nodeName The nodeName to set.
     */
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    /**
     * Set the value for this node
     *
     * @param value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Helper method to encode a string so it can be stored properly.
     *
     * @param value string to encode
     * @return UTF-8 encoded string
     */
    public static String encode(String value) {
        if (value == null) {
            return "";
        }
        try {
            return URLEncoder.encode(value, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return value;
        }
    }

    /**
     * Helper method to decode a string so it can be stored properly.
     *
     * @param value string to encode
     * @return UTF-8 encoded string
     */
    public static String decode(String value) {
        if (value == null) {
            return "";
        }
        try {
            return URLDecoder.decode(value, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return value;
        }
    }

    /**
     * @return Returns the value.
     */
    public String getValue() {
        return value;
    }

    /**
     * Return the string representation of this node
     *
     * @return String
     */
	@Override
	public String toString()
	{
        if (nodeName == null) {
            return "";
        } else {
        	StringBuffer buffer = new StringBuffer();
        	buffer.append("<").append(nodeName);
        	if (attributes != null && attributes.size() > 0) {
            	buffer.append(" ");
        		for (String key : attributes.keySet()) {
        			buffer.append(key).append("=").append(attributes.get(key)).append(" ");
        		}
        	}
        	buffer.append(">");
        	if (value != null && value.length() > 0) {
        		buffer.append(value).append("</" + nodeName + ">");
        	}
        	return buffer.toString();
        }
//        XMLWriter writer = new XMLWriter();
//		writer.addXMLNode(this);
//		writer.writeNodes();
//		return writer.toString();
	}

	
	/**
	 * Find the child with the given name and return it's value
	 * @param childNodeName
	 * @return
	 */
	public String getChildText(String childNodeName) {
		XMLNode childNode = getChildNode(childNodeName);
		return childNode == null ? "" : childNode.getValue();
	}
}
