package com.mastertechsoftware.util.xml;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValueParser extends AbstractParser {

	private static int NODE_NAME_POSITION = 1;
	private static int ATTRIBUTE_POSITION = 2;
	private static int VALUE_POSITION = 3;
	private static int END_POSITION = 4;
	private static int REMAINDER_POSITION = 5;
    private static int CD_VALUE_POSITION = 1;
	private final static Pattern nodeAttributeAndValuePattern = Pattern
		.compile("^\\s*?<([^/>\\s]+?)(?:\\s+([^>]*))??>(.*?)(</\\1>)(.*)", Pattern.DOTALL);

	private Matcher matcher;
    private final static Pattern CDataPattern = Pattern
        .compile("^<!\\[CDATA\\[(.*?)(?:\\]\\]>)(.*)", Pattern.DOTALL);
    private Matcher CDMatcher;

	/**
	 * Constructor.
	 * @param engine
	 */
	public ValueParser(XMLParsingEngine engine) {
		super(engine);
	}

	/**
	 * Parse the current string. Push new strings on the stack
	 */
	public boolean didParse(XMLParsingData data) {
		if (matcher == null) {
			matcher = nodeAttributeAndValuePattern.matcher(data.getCurrentParsingString());
		} else {
			matcher.reset(data.getCurrentParsingString());
		}
		if (matcher.find()) {
			data.popParsingString();
			String nodeName = matcher.group(NODE_NAME_POSITION).trim();
			if (data.isUsingFilters()) {
				data.handleFilter(nodeName);
				if (data.getCurrentFilterAction() == XMLParsingData.IGNORE_NODE) {
                    pushRemainder(data, matcher);
//					pushEnding(data, matcher);
					return true;
				}
			}
            data.setCurrentParentName(nodeName);
			XMLNode node = createNode(data, nodeName);
			if (data.isUsingFilters()) {
				data.pushCurrentFilterAction();
			}
			data.addCurrentNode(node);
			data.pushCurrentNode(node);
			addAttributes(node, matcher.group(ATTRIBUTE_POSITION));

            pushRemainder(data, matcher);
            pushEnding(data, matcher);
            pushValue(data, matcher);
			return true;
		}		
		return false;
	}

	private void pushEnding(XMLParsingData data, Matcher matcher) {
		String value = matcher.group(END_POSITION);
		if (value != null && value.length() > 0) {
			data.pushParsingString(value);
		}
	}
    
	private void pushRemainder(XMLParsingData data, Matcher matcher) {
		String value = matcher.group(REMAINDER_POSITION);
		if (value != null && value.length() > 0) {
			data.pushParsingString(value);
		}
	}

	private void pushValue(XMLParsingData data, Matcher matcher) {
		String value = matcher.group(VALUE_POSITION);
		if (value != null && value.length() > 0) {
            if (CDMatcher == null) {
                CDMatcher = CDataPattern.matcher(value);
            } else {
                CDMatcher.reset(value);
            }
            if (CDMatcher.find()) {
                value = CDMatcher.group(CD_VALUE_POSITION);
                data.getCurrentNode().setValue(value);
            } else {
			    data.pushParsingString(value);
            }
		}
	}
}
