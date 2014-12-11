package com.mastertechsoftware.util.xml;

import com.mastertechsoftware.util.log.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NoValueParser extends AbstractParser {
	private static int NODE_NAME_POSITION = 1;
	private static int ATTRIBUTE_POSITION = 2;
	private static int END_POSITION = 3;
	private static int REMAINDER_POSITION = 3;
	private final static Pattern nodeAndAttributePattern = Pattern
		.compile("^\\s*<([^>\\s]+)(?:\\s*([^>]*)\\s*)??/>(.*)", Pattern.DOTALL);
	private Matcher matcher;

	/**
	 * Constructor.
	 * @param engine
	 */
	public NoValueParser(XMLParsingEngine engine) {
		super(engine);
	}

	/**
	 * Parse the current string. Push new strings on the stack
	 */
	public boolean didParse(XMLParsingData data) {
		if (matcher == null) {
			matcher = nodeAndAttributePattern.matcher(data.getCurrentParsingString());
		} else {
			matcher.reset(data.getCurrentParsingString());
		}
		if (matcher.find()) {
			data.popParsingString();
			String nodeName = matcher.group(NODE_NAME_POSITION).trim();
			if (data.isUsingFilters() && data.getCurrentFilterAction() != XMLParsingData.READ_NODE_AND_CHILDREN) {
				data.handleFilter(nodeName);
				if (data.getCurrentFilterAction() == XMLParsingData.IGNORE_NODE) {
                    pushRemainder(data, matcher);
					return true;
				}
			}
			XMLNode node = createNode(data, nodeName);
			if (data.getCurrentNode() != null) {
				data.getCurrentNode().addChildNode(node);
			} else {
				Logger.debug("Current Node is null");
			}
			addAttributes(node, matcher.group(ATTRIBUTE_POSITION));
            pushRemainder(data, matcher);
			return true;
		}		
		return false;
	}


    private void pushRemainder(XMLParsingData data, Matcher matcher) {
        String value = matcher.group(REMAINDER_POSITION);
        if (value != null && value.length() > 0) {
            data.pushParsingString(value);
        }
    }

}
