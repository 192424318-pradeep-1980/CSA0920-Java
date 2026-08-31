package com.candymatch.game;

import javax.swing.SwingUtilities;

/**
 * GameTimerManager.java
 * Multithreaded timer engine implementing Runnable.
 * Runs an independent countdown thread once per second during level gameplay without freezing the UI or main thread.
 */
public class GameTimerManager implements Runnable {

    public interface TimerTickListener {
        void onTimerTick(int remainingSeconds);
        void onTimerExpired();
    }

    private volatile boolean running = false;
    private volatile boolean paused = false;
    private int remainingSeconds;
    private final TimerTickListener listener;
    private Thread timerThread;

    public GameTimerManager(int initialSeconds, TimerTickListener listener) {
        this.remainingSeconds = initialSeconds;
        this.listener = listener;
    }

    public synchronized void startTimer(int seconds) {
        stopTimer();
        this.remainingSeconds = seconds;
        this.running = true;
        this.paused = false;

        this.timerThread = new Thread(this, "GameTimerThread");
        this.timerThread.setDaemon(true);
        this.timerThread.start();
    }

    public synchronized void stopTimer() {
        this.running = false;
        if (timerThread != null && timerThread.isAlive()) {
            timerThread.interrupt();
        }
        timerThread = null;
    }

    public synchronized void pauseTimer() {
        this.paused = true;
    }

    public synchronized void resumeTimer() {
        this.paused = false;
    }

    @Override
    public void run() {
        while (running && remainingSeconds > 0) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // Thread interrupted on stop
                break;
            }

            if (!running) break;

            if (!paused) {
                remainingSeconds--;
                final int current = remainingSeconds;

                // Safely update Swing UI on Event Dispatch Thread
                SwingUtilities.invokeLater(() -> {
                    if (listener != null) {
                        listener.onTimerTick(current);
                    }
                });

                if (remainingSeconds <= 0) {
                    running = false;
                    SwingUtilities.invokeLater(() -> {
                        if (listener != null) {
                            listener.onTimerExpired();
                        }
                    });
                    break;
                }
            }
        }
    }

    public int getRemainingSeconds() {
        return remainingSeconds;
    }

    public boolean isRunning() {
        return running && remainingSeconds > 0;
    }
}
