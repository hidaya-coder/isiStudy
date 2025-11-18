package com.example.fittness;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class TodoList extends AppCompatActivity {

    private ArrayList<NoteItem> notesList;
    private ArrayAdapter<NoteItem> adapter;
    private ListView notesListView;
    private Button addNoteButton;
    private Button startStudyingButton;

    private static final int ADD_NOTE_REQUEST = 1;
    private static final int EDIT_NOTE_REQUEST = 2;

    public class NoteItem {
        public String text;
        public String imageUri;

        public NoteItem(String text, String imageUri) {
            this.text = text;
            this.imageUri = imageUri;
        }

        public boolean hasImage() {
            return imageUri != null && !imageUri.isEmpty();
        }

        @Override
        public String toString() {
            return text + (hasImage() ? " 🖼️" : "");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_list);

        initializeViews();
        setupList();
        setupClickListeners();
        loadSampleData();
    }

    private void initializeViews() {
        notesListView = findViewById(R.id.notesListView);
        addNoteButton = findViewById(R.id.addNoteButton);
        startStudyingButton = findViewById(R.id.startpomodoro);
        notesList = new ArrayList<>();
    }

    private void setupList() {
        adapter = new ArrayAdapter<NoteItem>(this, R.layout.note_list_item, R.id.noteText, notesList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                NoteItem note = notesList.get(position);
                TextView noteText = view.findViewById(R.id.noteText);
                TextView imageIndicator = view.findViewById(R.id.imageIndicator);
                ImageView noteThumbnail = view.findViewById(R.id.noteThumbnail);
                ImageView noteIcon = view.findViewById(R.id.noteIcon);

                noteText.setText(note.text);

                if (note.hasImage()) {
                    imageIndicator.setVisibility(View.VISIBLE);
                    noteThumbnail.setVisibility(View.VISIBLE);
                    noteIcon.setImageResource(R.drawable.ic_note_text);

                    try {
                        // CORRECTION : Charger l'image depuis l'URI sauvegardé
                        Uri imageUri = Uri.parse(note.imageUri);
                        noteThumbnail.setImageURI(imageUri);
                    } catch (Exception e) {
                        noteThumbnail.setImageResource(android.R.drawable.ic_menu_gallery);
                    }
                } else {
                    imageIndicator.setVisibility(View.GONE);
                    noteThumbnail.setVisibility(View.GONE);
                    noteIcon.setImageResource(R.drawable.ic_note_text);
                }

                return view;
            }
        };

        notesListView.setAdapter(adapter);
    }

    private void loadSampleData() {
        if (notesList.isEmpty()) {
            notesList.add(new NoteItem("Révision mathématiques - Chapitre 4", null));
            notesList.add(new NoteItem("Projet physique - Rapport final", null));
            notesList.add(new NoteItem("Liste vocabulaire anglais", null));
            notesList.add(new NoteItem("Exercices programmation Java", null));
            adapter.notifyDataSetChanged();
        }
    }

    private void setupClickListeners() {
        addNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddNoteActivity();
            }
        });

        startStudyingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TodoList.this, PomodoroTimer.class);
                startActivity(intent);
            }
        });

        notesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NoteItem selectedNote = notesList.get(position);

                Intent intent = new Intent(TodoList.this, AddNote.class);
                Bundle bundle = new Bundle();
                bundle.putString("NOTE_TEXT", selectedNote.text);
                bundle.putInt("NOTE_POSITION", position);
                bundle.putString("MODE", "EDIT");

                // CORRECTION : Toujours envoyer l'URI de l'image
                if (selectedNote.hasImage()) {
                    bundle.putString("IMAGE_URI", selectedNote.imageUri);
                } else {
                    bundle.putString("IMAGE_URI", "");
                }

                intent.putExtras(bundle);
                startActivityForResult(intent, EDIT_NOTE_REQUEST);
            }
        });

        notesListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                notesList.remove(position);
                adapter.notifyDataSetChanged();
                Toast.makeText(TodoList.this, "Note supprimée", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    private void openAddNoteActivity() {
        Intent intent = new Intent(TodoList.this, AddNote.class);
        Bundle bundle = new Bundle();
        bundle.putString("MODE", "ADD");
        intent.putExtras(bundle);
        startActivityForResult(intent, ADD_NOTE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            Bundle bundle = data.getExtras();

            if (bundle != null) {
                String noteText = bundle.getString("NOTE_TEXT");
                String imageUriString = bundle.getString("IMAGE_URI"); // CORRECTION : Récupérer l'URI

                if (noteText != null && !noteText.trim().isEmpty()) {
                    // CORRECTION : Vérifier si l'URI est vide ou null
                    String imageUri = (imageUriString != null && !imageUriString.isEmpty()) ? imageUriString : null;

                    if (requestCode == ADD_NOTE_REQUEST) {
                        notesList.add(new NoteItem(noteText, imageUri));
                        adapter.notifyDataSetChanged();
                        showToast("Note ajoutée" + (imageUri != null ? " avec image" : ""));
                    } else if (requestCode == EDIT_NOTE_REQUEST) {
                        int position = bundle.getInt("NOTE_POSITION", -1);
                        if (position != -1 && position < notesList.size()) {
                            notesList.set(position, new NoteItem(noteText, imageUri));
                            adapter.notifyDataSetChanged();
                            showToast("Note modifiée" + (imageUri != null ? " avec image" : ""));
                        }
                    }
                }
            }
        } else if (resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "Action annulée", Toast.LENGTH_SHORT).show();
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}