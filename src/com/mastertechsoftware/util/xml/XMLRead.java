package com.mastertechsoftware.util.xml;

import java.io.InputStream;

/**
 * Interface for classes that read in an XMLNode
 * User: Kevin
 */
public interface XMLRead {
    void readInputStream(InputStream is);
    void readNode(XMLNode node);
}
