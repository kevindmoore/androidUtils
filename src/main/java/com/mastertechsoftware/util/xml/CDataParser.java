package com.mastertechsoftware.util.xml;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Date: Oct 8, 2010
 */
public class CDataParser extends AbstractParser {
    private static int VALUE_POSITION = 1;
    private static int REMAINDER_POSITION = 2;
    private final static Pattern CDataPattern = Pattern
        .compile("^<!\\[CDATA\\[(.*?)(?:\\]\\]>)(.*)", Pattern.DOTALL);
    private Matcher matcher;

    public CDataParser(XMLParsingEngine engine) {
        super(engine);
    }

    @Override
    public boolean didParse(XMLParsingData data) {
        if (matcher == null) {
            matcher = CDataPattern.matcher(data.getCurrentParsingString());
        } else {
            matcher.reset(data.getCurrentParsingString());
        }
        if (matcher.find()) {
            data.popParsingString();
            String value = matcher.group(REMAINDER_POSITION);
            if (value != null && value.length() > 0) {
                data.pushParsingString(value);
            }
            value = matcher.group(VALUE_POSITION);
            if (value != null && value.length() > 0) {
                XMLNode currentNode = data.getCurrentNode();
                String currentValue = currentNode.getValue();
                if (currentValue == null || currentValue.length() == 0) {
                    currentNode.setValue(value);
                } else {
                    currentNode.setValue(currentValue + value);
                }
                data.popParsingString();
                data.popCurrentNode();
            }
            return true;
        }
        return false;
    }
}
