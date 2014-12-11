package com.mastertechsoftware.util.xml;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IgnoreParser implements ParsingInterface {
	private final static Pattern ignorePattern = Pattern.compile("^\\s*<\\?xml.*?\\?>(.*)", Pattern.DOTALL);
	private final static Pattern commentPattern = Pattern.compile("^\\s*<!--.*?-->(.*)", Pattern.DOTALL);
	private Matcher ignoreMatcher, commentMatcher;
    private static final int SEARCH_COUNT = 10;

    public boolean didParse(XMLParsingData data) {
		String parseString = data.getCurrentParsingString();
		if (parseString == null ||  parseString.length() == 0 || parseString.trim().length() == 0) {
            data.popParsingString();
			return true;
		}
		if (ignoreMatcher == null) {
			ignoreMatcher = ignorePattern.matcher(data.getCurrentParsingString());
		} else {
			ignoreMatcher.reset(data.getCurrentParsingString());
		}
		if (ignoreMatcher.find()) {
			data.setCurrentParsingString(ignoreMatcher.group(1));
			return true;
		}		
		if (commentMatcher == null) {
			commentMatcher = commentPattern.matcher(data.getCurrentParsingString());
		} else {
			commentMatcher.reset(data.getCurrentParsingString());
		}
		if (commentMatcher.find()) {
			data.setCurrentParsingString(commentMatcher.group(1));
			return true;
		}		
		return false;
	}

    public boolean isParsable(String line) {
        line = line.trim();
        if (line.startsWith("<?xml") || line.startsWith("<!--")) {
            return true;
        }
        return false;
    }

}
