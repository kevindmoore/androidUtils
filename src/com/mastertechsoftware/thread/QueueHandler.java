package com.mastertechsoftware.thread;


import com.mastertechsoftware.util.log.Logger;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * User: kevin.moore
 * This class wraps an executor, processor & signal class to implement a queue that runs in the background.
 */
public class QueueHandler<QueueItem> {

    private static final int EXECUTOR_TIMEOUT = 15000; // Milliseconds
    private static final int EXECUTOR_SHUTDOWN_TIMEOUT = 30 * 1000; // 30 Milliseconds
    protected ExecutorService executor;
    protected QueueProcessor<QueueItem> queueProcessor;
    protected CountDownLatch commandSignal;

    public void start(QueueProcessor<QueueItem> queueProcessor) {
        this.queueProcessor = queueProcessor;
        executor = Executors.newSingleThreadExecutor();
        executor.execute(queueProcessor);
    }

    /**
     * Final shutdown steps
     */
    public void stop() {
        if (queueProcessor != null) {
            queueProcessor.setShutdown(true);
			queueProcessor.setRunning(false);
        }
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(EXECUTOR_TIMEOUT, TimeUnit.MILLISECONDS)) {
                    Logger.error(this, "QueueHandler:shutdown Timed out, didn't finish all tasks");
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                Logger.error(this, "QueueHandler:shutdown " + e.getMessage(), e);
                executor.shutdownNow();
            }
            executor = null;
        }
    }

    public void runToFinish() {
        if (queueProcessor != null) {
            queueProcessor.setShutdown(true);
        }
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(EXECUTOR_SHUTDOWN_TIMEOUT, TimeUnit.MILLISECONDS)) {
                    Logger.error(this, "QueueHandler:shutdown Timed out, didn't finish all tasks");
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                Logger.error(this, "QueueHandler:shutdown " + e.getMessage(), e);
                executor.shutdownNow();
            }
            executor = null;
        }
        if (queueProcessor != null) {
            queueProcessor.setRunning(false);
        }

    }


    /**
     * Default add - It should always go to the end unless we need to fix something
     * @param commandQueueItem
     */
    public void addToQueue(QueueItem commandQueueItem) {
        addToQueue(commandQueueItem, true);
    }

    /**
     * Add the given command queue item to our queue processor
     * @param commandQueueItem
     * @param addLast
     */
    public void addToQueue(QueueItem commandQueueItem, boolean addLast) {
        if (addLast) {
            queueProcessor.addQueue(commandQueueItem);
        } else {
            queueProcessor.addFirst(commandQueueItem);
        }
    }

    /**
     * Get the current items on the queue
     * @return List<QueueItem>
     */
    public List<QueueItem> getCurrentQueueList() {
        return queueProcessor.getCurrentQueueList();
    }


    /**
     * Send a signal to the countdown latch
     */
    public void sendSignal() {
        if (commandSignal != null) {
            Logger.debug(this, "commandSignal: countdown");
            commandSignal.countDown();
        } else {
            Logger.error(this, "commandSignal is null");
        }
    }

    /**
     * In case we get an exception we want to reset the state of the signal.
     */
    public void clearSignal() {
        if (commandSignal != null) {
            Logger.debug(this, "clearSignal");
            long count = commandSignal.getCount();
            for (int i = 0; i < count; i++) {
                commandSignal.countDown();
            }
            commandSignal = null;
        } else {
            Logger.error(this, "clearSignal:commandSignal is null");
        }

    }

    /**
     * Create a new signal with the given count
     * @param signalCount
     */
    public void createSignal(int signalCount) {
        if (commandSignal != null && commandSignal.getCount() > 0) {
            Logger.error(this, "createSignal: Current Signal still has " + commandSignal.getCount() + " count");
            clearSignal();
        }
        Logger.debug(this, "createSignal: creating signal with count " + signalCount);
        commandSignal = new CountDownLatch(signalCount);
        try {
            commandSignal.await();
        } catch (InterruptedException e) {
            Logger.error(this , "createSignal:InterruptedException " + e.getMessage());
        }

    }

}
