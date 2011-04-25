package com.mastertechsoftware.util.xml;

import com.mastertechsoftware.util.Logger;

import java.util.ArrayList;
import java.util.List;

public class XMLParsingEngine {
	private ArrayList<ParsingInterface> xmlEngines = new ArrayList<ParsingInterface>();
	private XMLParsingData data = new XMLParsingData();
	private List<XMLNodeFilter> xmlNodeFilters;
    private IgnoreParser ignoreParser;

	/**
	 * Setup the Parsing Engine
	 * @param xmlString
	 * @param xmlNodeFilters
	 */
	public XMLParsingEngine(StringBuilder xmlString, List<XMLNodeFilter> xmlNodeFilters) {
		this.xmlNodeFilters = xmlNodeFilters;
        if (xmlString != null) {
			try {
				data.pushParsingString(xmlString.toString());
			} catch (OutOfMemoryError error) {
				Logger.error("XMLParsingEngine", "OutOfMemoryError", error);
				return;
			}
        }
		data.setXmlNodeFilters(xmlNodeFilters);
        ignoreParser = new IgnoreParser();
//		xmlEngines.add(new IgnoreParser());
//		xmlEngines.add(new CDataParser(this));
		xmlEngines.add(new NoValueParser(this));
		xmlEngines.add(new ValueParser(this));
		xmlEngines.add(new EndingParser(this));
	}
	
	/**
	 * Start Parsing.
	 * @return whether it did any parsing
	 */
	public boolean parse() {
		long start = System.currentTimeMillis();
		boolean finished = false;
		boolean parsed= false;
		while (!finished) {
			if (data.getCurrentParsingString() == null) {
				return parsed;
			}
			boolean couldNotParse = true;
            if (ignoreParser.isParsable(data.getCurrentParsingString()) && ignoreParser.didParse(data)) {
                couldNotParse = false;
                parsed = true;
            } else {
                for (int i=0; i < xmlEngines.size(); i++) {
                     if (xmlEngines.get(i).didParse(data)) {
                         couldNotParse = false;
                         parsed = true;
                         break;
                     }
                }
            }
			if (couldNotParse) {
				finished = true;
			}
			if (finished) {
				String pushedString = data.popParsingString();
				XMLNode currentNode = data.getCurrentNode();
				if (pushedString != null) {
					if (currentNode != null) {
						currentNode.setValue(pushedString);
					}
//					data.popParsingString();
//					data.popCurrentNode();
					finished = false;
				}
			}
			if (data.getCurrentParsingString() == null) {
				finished = true;
			}
		}	
		long end = System.currentTimeMillis();
		Logger.debug("Parsing/Creating nodes took " + ((end - start)) + " milliseconds");
		return parsed;
	}

	public XMLParsingData getData() {
		return data;
	}

	public List<XMLNodeFilter> getXmlNodeFilters() {
		return xmlNodeFilters;
	}

	public XMLNode getRoot() {
		return data.getRoot();
	}	
}
