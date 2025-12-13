package com.example.fittness;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import android.widget.ImageButton;

public class PomodoroTimerActivity extends AppCompatActivity implements PomodoroTimer.TimerListener, android.speech.tts.TextToSpeech.OnInitListener {

    private CircularProgressView circularProgressView;
    private TextView timerText;
    private TextView modeText;
    private Button startPauseButton;
    private Button stopButton;
    private ImageButton settingsButton;
    private View backButton;
    private TextView taskTitleText;

    private PomodoroTimer pomodoroTimer;
    private SharedPreferences prefs;
    private TaskManager taskManager;
    private long linkedTaskId = -1;
    private long sessionStartTime = 0;
    private int sessionCompletedMinutes = 0;
    private android.speech.tts.TextToSpeech tts;

    private static final String PREFS_NAME = "PomodoroPrefs";
    private static final String KEY_FOCUS_DURATION = "focus_duration";
    private static final String KEY_SHORT_REST_DURATION = "short_rest_duration";
    private static final String KEY_LONG_REST_DURATION = "long_rest_duration";
    private static final String KEY_CYCLES_BEFORE_LONG_REST = "cycles_before_long_rest";
    private static final String KEY_TIMER_STATE = "timer_state";
    private static final String KEY_CURRENT_MODE = "current_mode";
    private static final String KEY_REMAINING_MILLIS = "remaining_millis";
    private static final String KEY_CYCLES_COMPLETED = "cycles_completed";
    private static final String KEY_LINKED_TASK_ID = "linked_task_id";
    private static final String KEY_SESSION_START_TIME = "session_start_time";
    private static final String KEY_SESSION_COMPLETED_MINUTES = "session_completed_minutes";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pomodoro_timer);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        taskManager = new TaskManager(this);

        // Get linked task ID if provided
        linkedTaskId = getIntent().getLongExtra("taskId", -1);
        String taskTitle = getIntent().getStringExtra("taskTitle");

        circularProgressView = findViewById(R.id.circularProgressView);
        timerText = findViewById(R.id.timerText);
        modeText = findViewById(R.id.modeText);
        startPauseButton = findViewById(R.id.startPauseButton);
        stopButton = findViewById(R.id.stopButton);
        settingsButton = findViewById(R.id.settingsButton);
        if (linkedTaskId != -1) {
             settingsButton.setVisibility(View.GONE);
        } else {
             settingsButton.setVisibility(View.VISIBLE);
        }
        backButton = findViewById(R.id.backButton);
        taskTitleText = findViewById(R.id.taskTitleText);

        if (taskTitle != null && !taskTitle.isEmpty()) {
            taskTitleText.setText(taskTitle);
            taskTitleText.setVisibility(View.VISIBLE);
        } else if (linkedTaskId != -1) {
            Task task = taskManager.getTaskById(linkedTaskId);
            if (task != null) {
                taskTitleText.setText(task.getTitle());
                taskTitleText.setVisibility(View.VISIBLE);
            }
        }

        // Initialize timer with saved or default values
        long focusMs = prefs.getLong(KEY_FOCUS_DURATION, 25 * 60 * 1000);
        long shortRestMs = prefs.getLong(KEY_SHORT_REST_DURATION, 5 * 60 * 1000);
        long longRestMs = prefs.getLong(KEY_LONG_REST_DURATION, 20 * 60 * 1000);
        int cycles = prefs.getInt(KEY_CYCLES_BEFORE_LONG_REST, 4);

        // Check for linked task estimated time
        if (linkedTaskId != -1) {
            Task task = taskManager.getTaskById(linkedTaskId);
            if (task != null && task.getEstimatedMinutes() > 0) {
                int remainingMinutes = task.getEstimatedMinutes() - task.getCompletedMinutes();
                if (remainingMinutes > 0) {
                    focusMs = remainingMinutes * 60 * 1000L;
                    Toast.makeText(this, "Using remaining time: " + remainingMinutes + " min", Toast.LENGTH_SHORT).show();
                } else {
                     Toast.makeText(this, "Task is already completed or time exceeded!", Toast.LENGTH_SHORT).show();
                     // Optional: Could fall back to default or stick to estimated. Using estimated (total) if restant is 0 seems wrong if completed.
                     // But let's assume if they start it, they want to work more.
                     // For now, if <= 0 calculate as if 0? No, let's just stick to default or estimated?
                     // Request: "time restant existe" -> implies positive.
                     // I will fallback to estimated if remaining is <= 0 to avoid 0 minute timer,
                     // OR I can set it to a standard pomodoro.
                     // I'll set it to remaining if > 0.
                }
            }
        }

        pomodoroTimer = new PomodoroTimer(this);
        pomodoroTimer.setDurations(focusMs, shortRestMs, longRestMs);
        pomodoroTimer.setCyclesBeforeLongRest(cycles);

        // Restore state if available
        if (savedInstanceState != null) {
            restoreTimerState(savedInstanceState);
        } else {
            restoreTimerStateFromPrefs();
        }

        setupListeners();
        updateUI();

        tts = new android.speech.tts.TextToSpeech(this, this);
    }

    @Override
    public void onInit(int status) {
        if (status == android.speech.tts.TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);
            if (result == android.speech.tts.TextToSpeech.LANG_MISSING_DATA ||
                    result == android.speech.tts.TextToSpeech.LANG_NOT_SUPPORTED) {
                // Handle error
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        
        // Strict Reset: Clear all session data if activity is finishing
        if (isFinishing()) {
            clearPomodoroSessionData();
        }
        super.onDestroy();
    }

    private void setupListeners() {
        startPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PomodoroTimer.TimerState state = pomodoroTimer.getState();
                if (state == PomodoroTimer.TimerState.RUNNING) {
                    pomodoroTimer.pause();
                    // Save session progress if linked to task
                    if (linkedTaskId != -1 && pomodoroTimer.getCurrentMode() == PomodoroTimer.TimerMode.FOCUS) {
                        updateTaskProgress();
                    }
                } else {
                    pomodoroTimer.start();
                    // Track session start time for linked task
                    if (linkedTaskId != -1 && pomodoroTimer.getCurrentMode() == PomodoroTimer.TimerMode.FOCUS) {
                        sessionStartTime = System.currentTimeMillis();
                    }
                }
                updateUI();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pomodoroTimer.stop();
                updateUI();
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSettingsDialog();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (linkedTaskId != -1 && pomodoroTimer.getCurrentMode() == PomodoroTimer.TimerMode.FOCUS) {
                    updateTaskProgress();
                }
                finish();
            }
        });
    }

    private void showSettingsDialog() {
        SettingsDialogFragment dialog = new SettingsDialogFragment();
        dialog.setSettingsListener(new SettingsDialogFragment.SettingsListener() {
            @Override
            public void onSettingsChanged(long focusMs, long shortRestMs, long longRestMs, int cycles) {
                pomodoroTimer.setDurations(focusMs, shortRestMs, longRestMs);
                pomodoroTimer.setCyclesBeforeLongRest(cycles);

                // Save to preferences
                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong(KEY_FOCUS_DURATION, focusMs);
                editor.putLong(KEY_SHORT_REST_DURATION, shortRestMs);
                editor.putLong(KEY_LONG_REST_DURATION, longRestMs);
                editor.putInt(KEY_CYCLES_BEFORE_LONG_REST, cycles);
                editor.apply();

                // If timer is stopped, update display
                if (pomodoroTimer.getState() == PomodoroTimer.TimerState.STOPPED) {
                    pomodoroTimer.setCurrentMode(pomodoroTimer.getCurrentMode());
                    updateUI();
                }
            }
        });

        // Load current values
        long focusMs = prefs.getLong(KEY_FOCUS_DURATION, 25 * 60 * 1000);
        long shortRestMs = prefs.getLong(KEY_SHORT_REST_DURATION, 5 * 60 * 1000);
        long longRestMs = prefs.getLong(KEY_LONG_REST_DURATION, 20 * 60 * 1000);
        int cycles = prefs.getInt(KEY_CYCLES_BEFORE_LONG_REST, 4);

        Bundle args = new Bundle();
        args.putLong("focus", focusMs);
        args.putLong("shortRest", shortRestMs);
        args.putLong("longRest", longRestMs);
        args.putInt("cycles", cycles);
        dialog.setArguments(args);

        dialog.show(getSupportFragmentManager(), "SettingsDialog");
    }

    private void updateUI() {
        PomodoroTimer.TimerState state = pomodoroTimer.getState();
        long remaining = pomodoroTimer.getRemainingMillis();

        // Update timer text
        int minutes = (int) (remaining / 1000) / 60;
        int seconds = (int) (remaining / 1000) % 60;
        timerText.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));

        // Update mode text
        PomodoroTimer.TimerMode mode = pomodoroTimer.getCurrentMode();
        String modeString = "";
        switch (mode) {
            case FOCUS:
                modeString = "Focus Time";
                break;
            case SHORT_REST:
                modeString = "Short Break";
                break;
            case LONG_REST:
                modeString = "Long Break";
                break;
        }
        modeText.setText(modeString);

        // Update buttons
        if (state == PomodoroTimer.TimerState.RUNNING) {
            startPauseButton.setText("Pause");
        } else {
            startPauseButton.setText("Start");
        }

        // Update progress
        float progress = 1.0f - ((float) remaining / (float) pomodoroTimer.getTotalMillis());
        circularProgressView.setProgress(progress);
    }

    @Override
    public void onTick(long remainingMillis, float progress) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int minutes = (int) (remainingMillis / 1000) / 60;
                int seconds = (int) (remainingMillis / 1000) % 60;
                timerText.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
                circularProgressView.setProgress(progress);
            }
        });
    }

    @Override
    public void onFinish() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // If timer finished and was linked to a task, update task progress
                // onFinish is called before mode changes, so currentMode is still the mode that just finished
                if (linkedTaskId != -1) {
                    Task task = taskManager.getTaskById(linkedTaskId);
                    if (task != null) {
                        PomodoroTimer.TimerMode finishedMode = pomodoroTimer.getCurrentMode();

                        // Only count FOCUS mode minutes
                        if (finishedMode == PomodoroTimer.TimerMode.FOCUS) {
                            // Get the focus duration that was just completed (in minutes)
                            long focusMs = prefs.getLong(KEY_FOCUS_DURATION, 25 * 60 * 1000);
                            int completedMinutes = (int) (focusMs / 60000);

                            // Add completed minutes to task
                            int currentCompleted = task.getCompletedMinutes();
                            task.setCompletedMinutes(currentCompleted + completedMinutes);
                            task.setLinkedPomodoroSessionId(-1); // Clear link
                            taskManager.updateTask(task);
                        }

                        sessionStartTime = 0; // Reset session
                        sessionCompletedMinutes = 0;
                    }
                }
                Toast.makeText(PomodoroTimerActivity.this, "Timer finished!", Toast.LENGTH_SHORT).show();
                if (tts != null && !tts.isSpeaking()) {
                    tts.speak("Pomodoro completed. Take a break.", android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null);
                }
                updateUI();
            }
        });
    }

    private void updateTaskProgress() {
        if (linkedTaskId != -1 && sessionStartTime > 0) {
            Task task = taskManager.getTaskById(linkedTaskId);
            if (task != null && pomodoroTimer.getCurrentMode() == PomodoroTimer.TimerMode.FOCUS) {
                // Calculate elapsed minutes in current session
                long elapsed = System.currentTimeMillis() - sessionStartTime;
                int elapsedMinutes = (int) (elapsed / 60000);

                // Get the base completed minutes (before this session)
                int baseCompleted = task.getCompletedMinutes() - sessionCompletedMinutes;

                // Update with new total
                task.setCompletedMinutes(baseCompleted + elapsedMinutes);
                task.setLinkedPomodoroSessionId(linkedTaskId);
                taskManager.updateTask(task);

                sessionCompletedMinutes = elapsedMinutes;
            }
        }
    }

    @Override
    public void onModeChanged(PomodoroTimer.TimerMode newMode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateUI();
                Toast.makeText(PomodoroTimerActivity.this, "Switched to " + modeText.getText(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveTimerState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            // User is exiting (Back button or finish called), clear data immediately
            clearPomodoroSessionData();
        } else {
            // App is backgrounding but not closing, save state
            saveTimerStateToPrefs();
        }
    }

    private void clearPomodoroSessionData() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_TIMER_STATE);
        editor.remove(KEY_CURRENT_MODE);
        editor.remove(KEY_REMAINING_MILLIS);
        editor.remove(KEY_CYCLES_COMPLETED);
        editor.remove(KEY_LINKED_TASK_ID);
        editor.remove(KEY_SESSION_START_TIME);
        editor.remove(KEY_SESSION_COMPLETED_MINUTES);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        restoreTimerStateFromPrefs();
        updateUI();
    }

    private void saveTimerState(Bundle outState) {
        outState.putString(KEY_TIMER_STATE, pomodoroTimer.getState().name());
        outState.putString(KEY_CURRENT_MODE, pomodoroTimer.getCurrentMode().name());
        outState.putLong(KEY_REMAINING_MILLIS, pomodoroTimer.getRemainingMillis());
        outState.putInt(KEY_CYCLES_COMPLETED, pomodoroTimer.getCyclesCompleted());
    }

    private void restoreTimerState(Bundle savedInstanceState) {
        String stateStr = savedInstanceState.getString(KEY_TIMER_STATE);
        String modeStr = savedInstanceState.getString(KEY_CURRENT_MODE);
        long remaining = savedInstanceState.getLong(KEY_REMAINING_MILLIS, pomodoroTimer.getTotalMillis());
        int cycles = savedInstanceState.getInt(KEY_CYCLES_COMPLETED, 0);

        if (stateStr != null) {
            pomodoroTimer.setState(PomodoroTimer.TimerState.valueOf(stateStr));
        }
        if (modeStr != null) {
            pomodoroTimer.setCurrentMode(PomodoroTimer.TimerMode.valueOf(modeStr));
        }
        pomodoroTimer.setRemainingMillis(remaining);
    }

    private void saveTimerStateToPrefs() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_TIMER_STATE, pomodoroTimer.getState().name());
        editor.putString(KEY_CURRENT_MODE, pomodoroTimer.getCurrentMode().name());
        editor.putLong(KEY_REMAINING_MILLIS, pomodoroTimer.getRemainingMillis());
        editor.putInt(KEY_CYCLES_COMPLETED, pomodoroTimer.getCyclesCompleted());
        editor.putLong(KEY_LINKED_TASK_ID, linkedTaskId);
        editor.putLong(KEY_SESSION_START_TIME, sessionStartTime);
        editor.putInt(KEY_SESSION_COMPLETED_MINUTES, sessionCompletedMinutes);
        editor.apply();
    }

    private void restoreTimerStateFromPrefs() {
        String stateStr = prefs.getString(KEY_TIMER_STATE, null);
        String modeStr = prefs.getString(KEY_CURRENT_MODE, null);
        long remaining = prefs.getLong(KEY_REMAINING_MILLIS, -1);

        // Restore linked task info
        if (linkedTaskId == -1) {
            linkedTaskId = prefs.getLong(KEY_LINKED_TASK_ID, -1);
        }
        sessionStartTime = prefs.getLong(KEY_SESSION_START_TIME, 0);
        sessionCompletedMinutes = prefs.getInt(KEY_SESSION_COMPLETED_MINUTES, 0);

        if (stateStr != null && modeStr != null && remaining > 0) {
            pomodoroTimer.setCurrentMode(PomodoroTimer.TimerMode.valueOf(modeStr));
            pomodoroTimer.setRemainingMillis(remaining);

            PomodoroTimer.TimerState state = PomodoroTimer.TimerState.valueOf(stateStr);
            if (state == PomodoroTimer.TimerState.RUNNING) {
                pomodoroTimer.start();
            } else {
                pomodoroTimer.setState(state);
            }
        }
    }
}

