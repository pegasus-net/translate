package com.icarus.words.view;

public class RepeatThread extends Thread {
    private volatile boolean isRunning;

    @Override
    public void run() {
        isRunning = true;
        while (isRunning) {
            task();
        }
    }

    protected void task() {

    }

    public boolean isRunning() {
        return isRunning;
    }

    synchronized public void cancel() {
        isRunning = false;
    }
}
