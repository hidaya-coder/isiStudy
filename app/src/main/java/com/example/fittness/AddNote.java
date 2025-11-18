package com.example.fittness;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class AddNote extends AppCompatActivity {

    private EditText noteEditText;
    private Button saveButton;
    private Button cancelButton;
    private Button addImageButton;
    private Button takePhotoButton;
    private ImageView noteImageView;
    private TextView titleText;

    private int notePosition = -1;
    private String currentMode = "ADD";
    private Uri currentImageUri = null;

    private BroadcastReceiver imageReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        initializeViews();
        getIntentData();
        setupClickListeners();
        setupBroadcastReceiver();
    }

    private void initializeViews() {
        noteEditText = findViewById(R.id.noteEditText);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);
        addImageButton = findViewById(R.id.addImageButton);
        takePhotoButton = findViewById(R.id.takePhotoButton);
        noteImageView = findViewById(R.id.noteImageView);
        titleText = findViewById(R.id.titleText);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            currentMode = intent.getStringExtra("MODE");

            if ("EDIT".equals(currentMode)) {
                titleText.setText("Modifier la Note");
                String existingNote = intent.getStringExtra("NOTE_TEXT");
                notePosition = intent.getIntExtra("NOTE_POSITION", -1);
                String imageUriString = intent.getStringExtra("IMAGE_URI");

                if (existingNote != null) {
                    noteEditText.setText(existingNote);
                    noteEditText.setSelection(existingNote.length());
                }

                if (imageUriString != null && !imageUriString.isEmpty()) {
                    currentImageUri = Uri.parse(imageUriString);
                    loadImageIntoView(currentImageUri);
                }
            } else {
                titleText.setText("Nouvelle Note");
            }
        }
    }

    private void loadImageIntoView(Uri imageUri) {
        try {
            noteImageView.setImageURI(imageUri);
            noteImageView.setVisibility(View.VISIBLE);
            Log.d("AddNote", "Image chargée: " + imageUri);
        } catch (Exception e) {
            Log.e("AddNote", "Erreur chargement image: " + e.getMessage());
            Toast.makeText(this, "Erreur chargement image", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupClickListeners() {
        saveButton.setOnClickListener(v -> saveNote());
        cancelButton.setOnClickListener(v -> cancelAndReturn());

        // Appeler le service pour la galerie
        addImageButton.setOnClickListener(v -> {
            Log.d("AddNote", "Démarrage service pour galerie");
            Intent serviceIntent = new Intent(AddNote.this, ImageImportServic.class);
            serviceIntent.setAction(ImageImportServic.ACTION_PICK_IMAGE);
            startService(serviceIntent);
        });

        // Appeler le service pour la caméra
        takePhotoButton.setOnClickListener(v -> {
            Log.d("AddNote", "Démarrage service pour caméra");
            Intent serviceIntent = new Intent(AddNote.this, ImageImportServic.class);
            serviceIntent.setAction(ImageImportServic.ACTION_TAKE_PHOTO);
            startService(serviceIntent);
        });

        noteImageView.setOnClickListener(v -> removeImage());
    }

    private void setupBroadcastReceiver() {
        imageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.d("AddNote", "Broadcast reçu: " + action);

                if (ImageImportServic.BROADCAST_PHOTO_READY.equals(action)) {
                    String imageUriString = intent.getStringExtra(ImageImportServic.EXTRA_IMAGE_URI);
                    if (imageUriString != null) {
                        currentImageUri = Uri.parse(imageUriString);
                        loadImageIntoView(currentImageUri);
                        Toast.makeText(AddNote.this, "Photo prête", Toast.LENGTH_SHORT).show();
                        Log.d("AddNote", "Photo URI reçue: " + currentImageUri);
                    }

                } else if (ImageImportServic.BROADCAST_IMAGE_PICKED.equals(action)) {
                    String imageUriString = intent.getStringExtra(ImageImportServic.EXTRA_IMAGE_URI);
                    if (imageUriString != null) {
                        currentImageUri = Uri.parse(imageUriString);
                        loadImageIntoView(currentImageUri);
                        Toast.makeText(AddNote.this, "Image sélectionnée", Toast.LENGTH_SHORT).show();
                        Log.d("AddNote", "Image URI reçue: " + currentImageUri);
                    }

                } else if (ImageImportServic.BROADCAST_ERROR.equals(action)) {
                    String errorMessage = intent.getStringExtra(ImageImportServic.EXTRA_ERROR_MESSAGE);
                    Toast.makeText(AddNote.this, "Erreur: " + errorMessage, Toast.LENGTH_LONG).show();
                    Log.e("AddNote", "Erreur du service: " + errorMessage);
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(ImageImportServic.BROADCAST_PHOTO_READY);
        filter.addAction(ImageImportServic.BROADCAST_IMAGE_PICKED);
        filter.addAction(ImageImportServic.BROADCAST_ERROR);
        LocalBroadcastManager.getInstance(this).registerReceiver(imageReceiver, filter);

        Log.d("AddNote", "Broadcast receiver enregistré");
    }

    private void removeImage() {
        currentImageUri = null;
        noteImageView.setImageResource(android.R.color.transparent);
        noteImageView.setVisibility(View.GONE);
        Toast.makeText(this, "Image supprimée", Toast.LENGTH_SHORT).show();
    }

    private void saveNote() {
        String noteText = noteEditText.getText().toString().trim();

        if (noteText.isEmpty()) {
            noteEditText.setError("Veuillez écrire une note");
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putString("NOTE_TEXT", noteText);

        if (currentImageUri != null) {
            bundle.putString("IMAGE_URI", currentImageUri.toString());
            Log.d("AddNote", "Sauvegarde note avec image: " + currentImageUri.toString());

            // Sauvegarder l'image via le service
            saveImageViaService(currentImageUri);
        } else {
            bundle.putString("IMAGE_URI", "");
        }

        if ("EDIT".equals(currentMode)) {
            bundle.putInt("NOTE_POSITION", notePosition);
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtras(bundle);
        setResult(RESULT_OK, resultIntent);

        Toast.makeText(this, "Note enregistrée", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void saveImageViaService(Uri imageUri) {
        try {
            Intent serviceIntent = new Intent(this, ImageImportServic.class);
            serviceIntent.setAction(ImageImportServic.ACTION_SAVE_IMAGE);

            serviceIntent.putExtra("IMAGE_URI", imageUri);
            startService(serviceIntent);
            Log.d("AddNote", "Sauvegarde image via service: " + imageUri);
        } catch (Exception e) {
            Log.e("AddNote", "Erreur sauvegarde service: " + e.getMessage());
        }
    }

    private void cancelAndReturn() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (imageReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(imageReceiver);
            Log.d("AddNote", "Broadcast receiver désenregistré");
        }
    }
}