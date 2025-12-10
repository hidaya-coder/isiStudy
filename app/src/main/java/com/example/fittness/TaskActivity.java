package com.example.fittness;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class TaskActivity extends AppCompatActivity {

    private EditText titleEdit, dateEdit, timeEdit, estMinutesEdit;
    private Button saveButton, cancelButton, startPomodoroButton;
    private TaskManager taskManager;
    private Task currentTask;
    private long taskId;
    private boolean isEditMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Vérifier l'authentification
        AuthHelper.requireLogin(this);
        if (!AuthHelper.isLoggedIn(this)) {
            return;
        }

        // Utiliser un layout unique
        setContentView(R.layout.activity_task);

        // Récupérer les paramètres
        Intent intent = getIntent();
        isEditMode = intent.getBooleanExtra("isEditMode", false);
        taskId = intent.getLongExtra("taskId", -1);

        // Initialiser les vues
        initViews();

        // Initialiser TaskManager
        taskManager = new TaskManager(this);

        // Configurer selon le mode
        if (isEditMode && taskId != -1) {
            setupEditMode();
        } else {
            setupAddMode();
        }

        setupButtons();
    }

    private void initViews() {
        titleEdit = findViewById(R.id.titleEditText);
        dateEdit = findViewById(R.id.dateEditText);
        timeEdit = findViewById(R.id.timeEditText);
        estMinutesEdit = findViewById(R.id.estimatedMinutesEditText);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);
        startPomodoroButton = findViewById(R.id.startPomodoroButton);

        // Date Picker
        dateEdit.setOnClickListener(v -> showDatePicker());

        // Time Picker
        timeEdit.setOnClickListener(v -> showTimePicker());
    }

    private void showDatePicker() {
        final java.util.Calendar c = java.util.Calendar.getInstance();
        int year = c.get(java.util.Calendar.YEAR);
        int month = c.get(java.util.Calendar.MONTH);
        int day = c.get(java.util.Calendar.DAY_OF_MONTH);

        android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    // Format: yyyy-MM-dd
                    String date = String.format(java.util.Locale.getDefault(), "%04d-%02d-%02d", year1, monthOfYear + 1, dayOfMonth);
                    dateEdit.setText(date);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void showTimePicker() {
        final java.util.Calendar c = java.util.Calendar.getInstance();
        int hour = c.get(java.util.Calendar.HOUR_OF_DAY);
        int minute = c.get(java.util.Calendar.MINUTE);

        // Use THEME_HOLO_LIGHT for spinner style (scrolling)
        android.app.TimePickerDialog timePickerDialog = new android.app.TimePickerDialog(this,
                android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                (view, hourOfDay, minute1) -> {
                    String time = String.format(java.util.Locale.getDefault(), "%02d:%02d", hourOfDay, minute1);
                    timeEdit.setText(time);
                }, hour, minute, true);
        
        // Ensure the background is not transparent if using Holo theme on some devices
        timePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        timePickerDialog.show();
    }

    private void setupEditMode() {
        // Récupérer la tâche à éditer
        currentTask = taskManager.getTaskById(taskId);

        if (currentTask != null) {
            // Pré-remplir les champs
            titleEdit.setText(currentTask.getTitle());
            dateEdit.setText(currentTask.getDueDate());
            timeEdit.setText(currentTask.getDueTime());
            estMinutesEdit.setText(String.valueOf(currentTask.getEstimatedMinutes()));

            // Afficher le bouton Pomodoro
            startPomodoroButton.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, "Task not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupAddMode() {
        // Mode ajout - pas de tâche existante
        currentTask = null;

        // Masquer le bouton Pomodoro
        startPomodoroButton.setVisibility(View.GONE);
    }

    private void setupButtons() {
        // Définir le texte du bouton selon le mode
        saveButton.setText(isEditMode ? "Update" : "Save");

        // Bouton Sauvegarder
        saveButton.setOnClickListener(v -> saveTask());

        // Bouton Annuler
        cancelButton.setOnClickListener(v -> finish());

        // Bouton Pomodoro
        startPomodoroButton.setOnClickListener(v -> {
            if (currentTask != null) {
                startPomodoro();
            } else {
                Toast.makeText(this, "Please save the task first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveTask() {
        // Valider les données
        String title = titleEdit.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show();
            return;
        }

        String date = dateEdit.getText().toString().trim();
        String time = timeEdit.getText().toString().trim();

        int estMinutes = 0;
        try {
            String minutesStr = estMinutesEdit.getText().toString().trim();
            if (!minutesStr.isEmpty()) {
                estMinutes = Integer.parseInt(minutesStr);
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid estimated minutes", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isEditMode && currentTask != null) {
            // Mettre à jour la tâche existante
            currentTask.setTitle(title);
            currentTask.setDueDate(date);
            currentTask.setDueTime(time);
            currentTask.setEstimatedMinutes(estMinutes);
            taskManager.updateTask(currentTask);
            Toast.makeText(this, "Task updated", Toast.LENGTH_SHORT).show();
        } else {
            // Créer une nouvelle tâche
            taskManager.addTaskWithDetails(title, date, time, estMinutes);
            Toast.makeText(this, "Task created", Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    private void startPomodoro() {
        if (currentTask != null) {
            Intent intent = new Intent(this, PomodoroTimerActivity.class);
            intent.putExtra("taskId", currentTask.getId());
            intent.putExtra("taskTitle", currentTask.getTitle());
            startActivity(intent);
        }
    }
}