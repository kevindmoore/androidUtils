/*
 * @author Kevin Moore
 *
 */
package com.mastertechsoftware.util.xml;


import com.mastertechsoftware.util.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Class used to write XML Files using XML Nodes
 * @author Kevin Moore
 */
public class XMLWriter
{
	protected Writer writer;
	protected ArrayList<XMLNode> nodes;
	protected int indentLevel = -1;
	protected boolean doIndents = true;
	protected boolean writeHeader = false;
	private String xmlIntro = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    private File xmlFile;

    /**
	 * Constructor
	 * @param xmlFile
	 */
	public XMLWriter(File xmlFile)
	{
        this.xmlFile = xmlFile;
	}
	
	/**
	 * Alternate constructor. Pass in a string representing an xml file
	 */
	public XMLWriter()
	{
	}

    protected boolean createWriter() {
        if (writer != null) {
            return true;
        }
        if (xmlFile != null) {
            try
            {
                writer = new FileWriter(xmlFile);
            }
            catch (IOException ex)
            {
                Logger.error(ex);
                return false;
            }
        } else {
            writer = new StringWriter();
        }
        return true;
    }

    /**
	 * Return the string representing the XML file
	 * @return String
	 */
	@Override
	public String toString()
	{
		return writer.toString();
	}
	
	/**
	 * Add Node to list
	 * @param node
	 */
	public void addXMLNode(XMLNode node)
	{
		if (nodes == null)
		{
			nodes = new ArrayList<XMLNode>();
		}
		nodes.add(node);
	}
	
	/**
	 * Write out all of our nodes
     * @return true if written
	 */
	public boolean writeNodes()
	{
        if (!createWriter()) {
            return false;
        }
        try
		{
			if (writeHeader)
				writer.write(xmlIntro);
			boolean noProblems = writeNodes(nodes);
			writer.close();
            if (!noProblems) {
                return false;
            }
        }
		catch (IOException ex)
		{
            Logger.error(ex);
            return false;
        }
        return true;
    }
	
	/**
	 * Recursively write out a node and it's children
	 * @param nodeList
     * @return true if written
	 */
	public boolean writeNodes(List<XMLNode> nodeList)
	{
		if (nodeList == null)
			return true;
		Iterator<XMLNode> iter = nodeList.iterator();
		while (iter.hasNext())
		{
			XMLNode node = iter.next();
			indentLevel++;
			if (doIndents)
				if (!writeIndents()) {
                    return false;
                }
			try
			{
				String nodeName = node.getNodeName();
				if ((nodeName == null) || (nodeName.length() == 0) && (node.getChildNodes() != null)) // Empty node
				{
//					indentLevel--;
					doIndents = false;
					writeNodes(node.getChildNodes());
					continue;
				}
				else if (nodeName.length() == 0)
				{
//					indentLevel--;
					doIndents = false;
					continue;
				}
				doIndents = true;
				writer.write("<" + nodeName);
				Map<String, String> attributes = node.getAttributes();
				if (attributes != null)
				{
					Iterator<String> keys = attributes.keySet().iterator();
					if (keys.hasNext())
					{
    					writer.write(" ");
    				}
					while (keys.hasNext())
					{
						String attribute = (String) keys.next();
						String value = (String) attributes.get(attribute);
						if (value != null && value.length() > 0)
							writer.write(attribute + "=\"" + value + "\"");
						if (keys.hasNext())
							writer.write(" ");
					}
				}
				if (node.getChildNodes() == null)
				{
					String value = node.getValue();
					if (value != null)
					{
						writer.write(">\n");
						indentLevel++;
						writeIndents();
						writer.write(value + "\n");
						indentLevel--;
						writeIndents();
						writer.write("</" + node.getNodeName() + ">\n");
					}
					else
					{
						writer.write("/>\n");
					}
				}
				else
				{
					String value = node.getValue();
					if (value != null)
					{
						writer.write(">\n");
						indentLevel++;
						writeIndents();
						indentLevel--;
						writer.write(value + "\n");
					}
					else
					{
						writer.write(">\n");
					}
					writeNodes(node.getChildNodes());
					writeIndents();
					writer.write("</" + node.getNodeName() + ">\n");
				}
				indentLevel--;
			}
			catch (IOException ex)
			{
                Logger.error(ex);
                return false;
            }
		}
        return true;
    }
	
	/**
	 * Write out indenting strings
     * @return true if written ok
	 */
	protected boolean writeIndents()
	{
		for (int i = 0; i < indentLevel	; i++)
		{
			try
			{
				writer.write("\t");
			}
			catch (IOException ex)
			{
                Logger.error(ex);
                return false;
            }
		}
        return true;
    }

    /**
	 * @return Returns the writeHeader.
	 */
	public boolean isWriteHeader()
	{
		return writeHeader;
	}

    /**
	 * @param writeHeader The writeHeader to set.
	 */
	public void setWriteHeader(boolean writeHeader)
	{
		this.writeHeader = writeHeader;
	}
}
