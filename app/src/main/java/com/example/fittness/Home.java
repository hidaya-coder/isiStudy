package com.example.fittness;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class Home extends AppCompatActivity implements TaskAdapter.OnTaskClickListener, NoteAdapter.OnNoteClickListener {

    private static final String TAG = "HomeActivity";

    private Button startStudyButton;
    private Button addNoteButton;
    private Button addTaskButton;
    private RecyclerView tasksRecyclerView;
    private RecyclerView notesRecyclerView;
    private TaskAdapter taskAdapter;
    private NoteAdapter noteAdapter;
    private TaskManager taskManager;
    private NoteManager noteManager;
    private TextView userNameText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Log.d(TAG, "Home onCreate started");
        // Récupérer l'email connecté
        String loggedInEmail = AuthHelper.getLoggedInEmail(this);
        Log.d(TAG, "✅ Utilisateur connecté: " + loggedInEmail);

        // Maintenant on peut charger le layout
        setContentView(R.layout.activity_home);

        try {
            // Initialiser les vues
            startStudyButton = findViewById(R.id.startStudyButton);
            addNoteButton = findViewById(R.id.addNoteButton);
            addTaskButton = findViewById(R.id.addTaskButton);
            tasksRecyclerView = findViewById(R.id.tasksRecyclerView);
            notesRecyclerView = findViewById(R.id.notesRecyclerView);
            userNameText = findViewById(R.id.userNameText);

            // Définir le nom d'utilisateur - PRIORITÉ À L'EMAIL SAUVEGARDÉ
            String displayEmail = loggedInEmail;

            // Vérifier si un email est passé en extra (pour compatibilité)
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra("email")) {
                String extraEmail = intent.getStringExtra("email");
                if (extraEmail != null && !extraEmail.isEmpty()) {
                    displayEmail = extraEmail;
                    Log.d(TAG, "Email depuis extra: " + extraEmail);
                }
            }

            // Afficher le message de bienvenue
            if (displayEmail != null && !displayEmail.isEmpty()) {
                String name = displayEmail.split("@")[0];
                userNameText.setText("Bonjour, " + name + "!");
                Log.d(TAG, "Nom affiché: " + name);
            } else {
                userNameText.setText("Bonjour, Utilisateur!");
                Log.w(TAG, "⚠️ Aucun email trouvé");
            }

            // Initialiser les managers
            taskManager = new TaskManager(this);
            noteManager = new NoteManager(this);

            // Initialiser le RecyclerView des tâches
            tasksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            List<Task> tasks = taskManager.getAllTasks();
            if (tasks == null) tasks = new ArrayList<>();
            Log.d(TAG, "📋 Tâches chargées: " + tasks.size());
            taskAdapter = new TaskAdapter(tasks, this);
            tasksRecyclerView.setAdapter(taskAdapter);

            // Initialiser le RecyclerView des notes
            if (notesRecyclerView != null) {
                notesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                List<Note> notes = noteManager.getAllNotes();
                if (notes == null) notes = new ArrayList<>();
                Log.d(TAG, "📝 Notes chargées: " + notes.size());
                noteAdapter = new NoteAdapter(notes, this);
                notesRecyclerView.setAdapter(noteAdapter);
            } else {
                Log.w(TAG, "⚠️ notesRecyclerView non trouvé");
            }

        } catch (Exception e) {
            Log.e(TAG, "💥 Erreur d'initialisation", e);
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return;
        }

        // Bouton Démarrer session
        startStudyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "▶️ Démarrage session d'étude");
                Intent intent = new Intent(Home.this, PomodoroTimerActivity.class);
                startActivity(intent);
            }
        });

        // Bouton Ajouter note
        addNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "➕ Ajout note");
                Intent intent = new Intent(Home.this, AddNote.class);
                startActivity(intent);
            }
        });

        // Bouton Ajouter tâche
        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "➕ Ajout tâche");
                Intent intent = new Intent(Home.this, TaskActivity.class);
                intent.putExtra("isEditMode", false);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Checking login status in onResume");
        Log.d(TAG, "🔄 Home onResume");

        // Vérifier à nouveau l'authentification
        if (!AuthHelper.isLoggedIn(this)) {
            Log.d(TAG, "❌ Utilisateur déconnecté");
            Toast.makeText(this, "Session expirée", Toast.LENGTH_SHORT).show();
            AuthHelper.requireLogin(this);
            return;
        }

        refreshData();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "🚀 Home onStart");
    }

    private void refreshData() {
        Log.d(TAG, "🔄 Rafraîchissement des données");
        refreshTasks();
        refreshNotes();
    }

    private void refreshTasks() {
        if (taskAdapter != null && taskManager != null) {
            List<Task> tasks = taskManager.getAllTasks();
            Log.d(TAG, "📋 Rafraîchissement tâches: " + (tasks != null ? tasks.size() : 0));
            if (tasks != null) {
                taskAdapter.setTasks(tasks);
            }
        }
    }

    private void refreshNotes() {
        if (noteAdapter != null && noteManager != null && notesRecyclerView != null) {
            List<Note> notes = noteManager.getAllNotes();
            Log.d(TAG, "📝 Rafraîchissement notes: " + (notes != null ? notes.size() : 0));
            if (notes != null) {
                noteAdapter.setNotes(notes);
            }
        }
    }

    // Implémentation TaskAdapter.OnTaskClickListener
    @Override
    public void onTaskChecked(Task task, boolean isChecked) {
        Log.d(TAG, "✓ Tâche " + (isChecked ? "cochée" : "décochée") + ": " + task.getTitle());
        task.setCompleted(isChecked);
        taskManager.updateTask(task);
        refreshTasks();
    }

    @Override
    public void onTaskDeleted(Task task) {
        Log.d(TAG, "🗑️ Tâche supprimée: " + task.getTitle());
        taskManager.deleteTask(task);
        refreshTasks();
        Toast.makeText(this, "Tâche supprimée", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStartPomodoro(Task task) {
        Log.d(TAG, "⏱️ Pomodoro pour: " + task.getTitle());
        Intent intent = new Intent(Home.this, PomodoroTimerActivity.class);
        intent.putExtra("taskId", task.getId());
        startActivity(intent);
    }

    @Override
    public void onTaskClicked(Task task) {
        Log.d(TAG, "✏️ Édition tâche: " + task.getTitle());
        Intent intent = new Intent(Home.this, TaskActivity.class);
        intent.putExtra("isEditMode", true);
        intent.putExtra("taskId", task.getId());
        startActivity(intent);
    }

    // Implémentation NoteAdapter.OnNoteClickListener
    @Override
    public void onNoteClicked(Note note) {
        Log.d(TAG, "✏️ Édition note");
        Intent intent = new Intent(Home.this, AddNote.class);
        intent.putExtra("noteId", note.getId());
        startActivity(intent);
    }

    @Override
    public void onNoteDeleted(Note note) {
        Log.d(TAG, "🗑️ Note supprimée");
        noteManager.deleteNote(note);
        refreshNotes();
        Toast.makeText(this, "Note supprimée", Toast.LENGTH_SHORT).show();
    }
}