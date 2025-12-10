package com.example.fittness;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddNote extends AppCompatActivity {
    private EditText titleEdit;
    private EditText contentEdit;
    private Button saveButton;
    private Button cancelButton;
    private View backButton;
    private NoteManager noteManager;
    private Note currentNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Check authentication
        AuthHelper.requireLogin(this);
        if (!AuthHelper.isLoggedIn(this)) {
            return;
        }
        
        setContentView(R.layout.activity_add_note);

        initializeViews();
        setupClickListeners();
        loadNoteIfEditing();
    }

    private void initializeViews() {
        noteManager = new NoteManager(this);
        titleEdit = findViewById(R.id.noteTitleEdit);
        contentEdit = findViewById(R.id.noteContentEdit);
        saveButton = findViewById(R.id.saveNoteButton);
        cancelButton = findViewById(R.id.cancelNoteButton);
        backButton = findViewById(R.id.backButton);
    }

    private void setupClickListeners() {
        saveButton.setOnClickListener(v -> saveNote());
        cancelButton.setOnClickListener(v -> finish());
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
    }

    private void loadNoteIfEditing() {
        long noteId = getIntent().getLongExtra("noteId", -1);
        if (noteId != -1) {
            loadNote(noteId);
        }
    }

    private void loadNote(long noteId) {
        for (Note note : noteManager.getAllNotes()) {
            if (note.getId() == noteId) {
                currentNote = note;
                titleEdit.setText(note.getTitle());
                contentEdit.setText(note.getContent());
                break;
            }
        }
    }

    private void saveNote() {
        String title = titleEdit.getText().toString().trim();
        String content = contentEdit.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Le titre de la note ne peut pas être vide", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentNote != null) {
            currentNote.setTitle(title);
            currentNote.setContent(content);
            noteManager.updateNote(currentNote);
        } else {
            noteManager.addNote(title, content);
        }

        Toast.makeText(this, "Note sauvegardée", Toast.LENGTH_SHORT).show();
        finish();
    }
}