package com.mastertechsoftware.util;

/**
 * Class Used to parse text and keep track of positions.
 */
public class StringParser {
    private int position;
    private String stringToParse;

    public StringParser(String stringToParse) {
        this.stringToParse = stringToParse;
    }

    public boolean parse(String stringToFind) {
        return parse(position, stringToFind);
    }

    public boolean parse(int startPostion, String stringToFind) {
        int index = stringToParse.indexOf(stringToFind, startPostion);
        if (index == -1) {
            return false;
        }
        position = index;
        return true;
    }

    public String getString(int length) {
        return stringToParse.substring(position, position+length);
    }

    public String getString(int start, int end) {
        return stringToParse.substring(start, end);
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
