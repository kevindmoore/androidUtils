package com.mastertechsoftware.util.xml;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EndingParser extends AbstractParser {
	private static int NODE_NAME_POSITION = 1;
	private static int REMAINDER_POSITION = 2;
	private final static Pattern endingNodePattern = Pattern
		.compile("^</([^>\\s]+?)>(.*)", Pattern.DOTALL);
	private Matcher matcher;

	public EndingParser(XMLParsingEngine engine) {
		super(engine);
	}

	@Override
	public boolean didParse(XMLParsingData data) {
		if (matcher == null) {
			matcher = endingNodePattern.matcher(data.getCurrentParsingString());
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
			data.popCurrentNode();
			if (data.isUsingFilters()) {
				data.popCurrentAction();
			}
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
