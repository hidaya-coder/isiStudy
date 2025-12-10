package com.example.fittness;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NotesListActivity extends AppCompatActivity implements NoteAdapter.OnNoteClickListener {
    private RecyclerView recyclerView;
    private NoteAdapter adapter;
    private NoteManager noteManager;
    private Button addNoteButton;
    private View backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Check authentication
        AuthHelper.requireLogin(this);
        if (!AuthHelper.isLoggedIn(this)) {
            return;
        }
        
        setContentView(R.layout.activity_notes_list);

        noteManager = new NoteManager(this);
        recyclerView = findViewById(R.id.notesRecyclerView);
        addNoteButton = findViewById(R.id.addNoteButton);
        backButton = findViewById(R.id.backButton);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NoteAdapter(noteManager.getAllNotes(), this);
        recyclerView.setAdapter(adapter);

        addNoteButton.setOnClickListener(v -> {
            Intent intent = new Intent(NotesListActivity.this, AddNote.class);
            startActivity(intent);
        });

        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshNotes();
    }

    private void refreshNotes() {
        adapter.setNotes(noteManager.getAllNotes());
    }

    @Override
    public void onNoteClicked(Note note) {
        Intent intent = new Intent(this, AddNote.class);
        intent.putExtra("noteId", note.getId());
        startActivity(intent);
    }

    @Override
    public void onNoteDeleted(Note note) {
        noteManager.deleteNote(note);
        refreshNotes();
        Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show();
    }
}

