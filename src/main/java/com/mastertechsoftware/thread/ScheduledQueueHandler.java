package com.mastertechsoftware.thread;

import android.util.Log;

import com.mastertechsoftware.util.log.Logger;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * User: kevin.moore
 */
public class ScheduledQueueHandler<Item> {
    private String TAG = "ScheduledQueueHandler";
    protected static final int EXECUTOR_TIMEOUT = 15000; // Milliseconds
    protected ScheduledExecutorService executor;

    public ScheduledQueueHandler() {
        executor = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * Final shutdown steps
     */
    public void stop() {
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(EXECUTOR_TIMEOUT, TimeUnit.MILLISECONDS)) {
                    Logger.error(this,
                          "ScheduledQueueHandler:shutdown Timed out, didn't finish all tasks");
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                Logger.error(this, "ScheduledQueueHandler:shutdown " + e.getMessage(), e);
                executor.shutdownNow();
            }
            executor = null;
        }
    }

    /**
     * Restart executor
     */
    public void restart() {
        if (executor != null) {
            return;
        }
        executor = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * Are we currently running?
     * @return true if yes
     */
    public boolean isRunning() {
        return executor != null;
    }

    public ScheduledFuture<?> schedule(Runnable command,
                                       long delay,
                                       TimeUnit unit) {
        return executor.schedule(command, delay, unit);
    }

    public <V> ScheduledFuture<V> schedule(Callable<V> callable,
                                           long delay,
                                           TimeUnit unit) {
        return executor.schedule(callable, delay, unit);
    }

    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
                                                  long initialDelay,
                                                  long period,
                                                  TimeUnit unit) {
        return executor.scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,
                                                     long initialDelay,
                                                     long delay,
                                                     TimeUnit unit) {
        return executor.scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }
}
