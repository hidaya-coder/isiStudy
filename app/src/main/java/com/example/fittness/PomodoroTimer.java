package com.example.fittness;

import android.os.CountDownTimer;

public class PomodoroTimer {
    public enum TimerState {
        STOPPED, RUNNING, PAUSED
    }

    public enum TimerMode {
        FOCUS, SHORT_REST, LONG_REST
    }

    private TimerState state = TimerState.STOPPED;
    private TimerMode currentMode = TimerMode.FOCUS;
    private long remainingMillis = 0;
    private long totalMillis = 0;
    private CountDownTimer countDownTimer;
    private TimerListener listener;
    private int cyclesCompleted = 0;
    private int cyclesBeforeLongRest = 4;

    // Default durations in milliseconds
    private long focusDurationMs = 25 * 60 * 1000; // 25 minutes
    private long shortRestDurationMs = 5 * 60 * 1000; // 5 minutes
    private long longRestDurationMs = 20 * 60 * 1000; // 20 minutes

    public interface TimerListener {
        void onTick(long remainingMillis, float progress);
        void onFinish();
        void onModeChanged(TimerMode newMode);
    }

    public PomodoroTimer(TimerListener listener) {
        this.listener = listener;
        this.totalMillis = focusDurationMs;
        this.remainingMillis = focusDurationMs;
    }

    public void setDurations(long focusMs, long shortRestMs, long longRestMs) {
        this.focusDurationMs = focusMs;
        this.shortRestDurationMs = shortRestMs;
        this.longRestDurationMs = longRestMs;
        
        // If timer is stopped, update current mode duration
        if (state == TimerState.STOPPED) {
            updateDurationForCurrentMode();
            remainingMillis = totalMillis;
        }
    }

    public void setCyclesBeforeLongRest(int cycles) {
        this.cyclesBeforeLongRest = cycles;
    }

    public void start() {
        if (state == TimerState.RUNNING) {
            return;
        }

        if (state == TimerState.PAUSED) {
            resume();
            return;
        }

        // Starting fresh
        state = TimerState.RUNNING;
        updateDurationForCurrentMode();
        remainingMillis = totalMillis;

        startCountDown();
    }

    public void pause() {
        if (state != TimerState.RUNNING) {
            return;
        }

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        state = TimerState.PAUSED;
    }

    public void resume() {
        if (state != TimerState.PAUSED) {
            return;
        }

        state = TimerState.RUNNING;
        startCountDown();
    }

    public void stop() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        state = TimerState.STOPPED;
        updateDurationForCurrentMode();
        remainingMillis = totalMillis;
        
        if (listener != null) {
            float progress = 0f;
            listener.onTick(remainingMillis, progress);
        }
    }

    public void reset() {
        stop();
        cyclesCompleted = 0;
        currentMode = TimerMode.FOCUS;
        updateDurationForCurrentMode();
        remainingMillis = totalMillis;
        
        if (listener != null) {
            listener.onModeChanged(currentMode);
            listener.onTick(remainingMillis, 0f);
        }
    }

    private void startCountDown() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(remainingMillis, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingMillis = millisUntilFinished;
                float progress = 1.0f - ((float) millisUntilFinished / (float) totalMillis);
                
                if (listener != null) {
                    listener.onTick(millisUntilFinished, progress);
                }
            }

            @Override
            public void onFinish() {
                remainingMillis = 0;
                state = TimerState.STOPPED;
                
                if (listener != null) {
                    listener.onTick(0, 1.0f);
                    listener.onFinish();
                }

                // Auto-advance to next mode
                advanceToNextMode();
            }
        };

        countDownTimer.start();
    }

    private void advanceToNextMode() {
        if (currentMode == TimerMode.FOCUS) {
            cyclesCompleted++;
            if (cyclesCompleted >= cyclesBeforeLongRest) {
                currentMode = TimerMode.LONG_REST;
                cyclesCompleted = 0;
            } else {
                currentMode = TimerMode.SHORT_REST;
            }
        } else {
            currentMode = TimerMode.FOCUS;
        }

        updateDurationForCurrentMode();
        remainingMillis = totalMillis;

        if (listener != null) {
            listener.onModeChanged(currentMode);
        }
    }

    private void updateDurationForCurrentMode() {
        switch (currentMode) {
            case FOCUS:
                totalMillis = focusDurationMs;
                break;
            case SHORT_REST:
                totalMillis = shortRestDurationMs;
                break;
            case LONG_REST:
                totalMillis = longRestDurationMs;
                break;
        }
    }

    // Getters
    public TimerState getState() {
        return state;
    }

    public TimerMode getCurrentMode() {
        return currentMode;
    }

    public long getRemainingMillis() {
        return remainingMillis;
    }

    public long getTotalMillis() {
        return totalMillis;
    }

    public int getCyclesCompleted() {
        return cyclesCompleted;
    }

    public void setState(TimerState state) {
        this.state = state;
    }

    public void setCurrentMode(TimerMode mode) {
        this.currentMode = mode;
        updateDurationForCurrentMode();
        if (state == TimerState.STOPPED) {
            remainingMillis = totalMillis;
        }
    }

    public void setRemainingMillis(long millis) {
        this.remainingMillis = millis;
    }
}
