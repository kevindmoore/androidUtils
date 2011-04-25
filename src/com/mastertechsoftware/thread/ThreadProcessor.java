package com.mastertechsoftware.thread;

/**
 * @author Kevin Moore
 */
public interface ThreadProcessor<Index, ProcessObject> {
	void process(Index index, ProcessObject processObject);
	void finished();
}
