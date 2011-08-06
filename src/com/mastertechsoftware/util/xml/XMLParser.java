package com.mastertechsoftware.util.xml;

import com.mastertechsoftware.io.BufferedReader;
import com.mastertechsoftware.util.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Substitute for SAX & DOM Parser User: Kevin
 */
public class XMLParser {
	public static final int READ_CURRENT_NODE = 1; 
	public static final int READ_NODE_AND_CHILDREN = 2; 
	public static final int IGNORE_NODE = 3;
	public static final int BYPASS = 4;
	
	
	private XMLNode root = new XMLNode();
	private String inputString;
	private List<XMLNodeFilter> xmlNodeFilters = new ArrayList<XMLNodeFilter>();

	/**
	 * Add a filter for filtering Nodes (exclude certain nodes)
	 * @param filter
	 */
	public void addFilter(XMLNodeFilter filter) {
		xmlNodeFilters.add(filter);
	}
	
	/**
	 * Parse the input stream
	 * @param input
	 * @return root XMLNode
	 */
	public XMLNode parse(InputStream input) throws XMLException {
//		Debug.startMethodTracing("XMLParser");

		long start = System.currentTimeMillis();
//		BufferedInputStream stream = new BufferedInputStream(input);
//   		BufferedReader reader = new BufferedReader(new InputStreamReader(input), 16384);
   		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		StringBuilder inputString = null;//new StringBuilder();
		try {
//			int size = 8192;
//			char[] buffer = new char[size];
//			while (reader.read(buffer, 0, size) != -1) {
//				inputString.append(buffer);
//			}
            inputString = reader.readAll();
//			String line;
//			while ((line = reader.readLine()) != null) {
//				line = line.trim();
//				if (line.startsWith("<!--") || line.length() == 0) {
//					continue;
//				}
//				inputString.append(line);
//			}
		} catch (OutOfMemoryError e) {
			Logger.error(e);
            throw new XMLException("XMLParser: Problems reading stream", e);
		} catch (Exception e) {
			Logger.error(e);
            throw new XMLException("XMLParser: Problems reading stream", e);
		}
		long end = System.currentTimeMillis();
		Logger.debug("Reading took " + ((end - start)) + " milliseconds");

        if (inputString.length() == 0 ) {
            return null;
        }
        // This should start with <?xml
        while (inputString.charAt(0) != '<') {
            inputString.deleteCharAt(0);
        }
        XMLParsingEngine engine = new XMLParsingEngine(inputString, xmlNodeFilters);
		engine.parse();
		root = engine.getRoot();
//		Debug.stopMethodTracing();

		// Debugging - TODO turn off when done
//		XMLWriter writer = new XMLWriter();
//		writer.addXMLNode(root);
//		writer.writeNodes();
//		String output = writer.toString();
//		Logger.d("XMLParser",output);
		return root;
	}


	public XMLNode getRoot() {
		return root;
	}

	public String getInputString() {
		return inputString;
	}


	public List<XMLNodeFilter> getXmlNodeFilters() {
		return xmlNodeFilters;
	}

	
	public void setXmlNodeFilters(List<XMLNodeFilter> xmlNodeFilters) {
		this.xmlNodeFilters = xmlNodeFilters;
	}
}
