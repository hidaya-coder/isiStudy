package com.example.fittness;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
    private String currentPhotoPath = null;

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int TAKE_PHOTO_REQUEST = 2;
    private static final int CAMERA_PERMISSION_REQUEST = 100;
    private static final int STORAGE_PERMISSION_REQUEST = 101;

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
            Log.d("AddNote", "Image chargée avec succès: " + imageUri);
            Toast.makeText(this, "Image chargée", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("AddNote", "Erreur chargement image: " + e.getMessage());
            Toast.makeText(this, "Erreur chargement image", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupClickListeners() {
        saveButton.setOnClickListener(v -> saveNote());
        cancelButton.setOnClickListener(v -> cancelAndReturn());
        addImageButton.setOnClickListener(v -> pickImageFromGallery());
        takePhotoButton.setOnClickListener(v -> takePhotoWithCamera());
        noteImageView.setOnClickListener(v -> removeImage());
    }

    private void setupBroadcastReceiver() {
        imageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.d("AddNote", "Broadcast reçu: " + action);

                if (ImageImportService.BROADCAST_IMAGE_SAVED.equals(action)) {
                    String imageUriString = intent.getStringExtra(ImageImportService.EXTRA_IMAGE_URI);

                    if (imageUriString != null) {
                        currentImageUri = Uri.parse(imageUriString);
                        Log.d("AddNote", "Image sauvegardée reçue du service: " + currentImageUri);
                        loadImageIntoView(currentImageUri);
                        Toast.makeText(AddNote.this, "Image sauvegardée et chargée", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter(ImageImportService.BROADCAST_IMAGE_SAVED);
        LocalBroadcastManager.getInstance(this).registerReceiver(imageReceiver, filter);

        Log.d("AddNote", "Broadcast receiver enregistré");
    }

    private void pickImageFromGallery() {
        if (!checkStoragePermission()) {
            return;
        }

        try {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryIntent.setType("image/*");
            startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
            Log.d("AddNote", "Galerie lancée");
        } catch (Exception e) {
            Log.e("AddNote", "Erreur galerie: " + e.getMessage());
            Toast.makeText(this, "Erreur ouverture galerie", Toast.LENGTH_SHORT).show();
        }
    }

    private void takePhotoWithCamera() {
        if (!checkCameraPermission()) {
            return;
        }

        if (!checkStoragePermission()) {
            return;
        }

        launchCamera();
    }

    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST);
            return false;
        }
        return true;
    }

    private boolean checkStoragePermission() {
        List<String> permissionsNeeded = new ArrayList<>();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(android.Manifest.permission.READ_MEDIA_IMAGES);
            }
        }

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsNeeded.toArray(new String[0]),
                    STORAGE_PERMISSION_REQUEST);
            return false;
        }

        return true;
    }

    private void launchCamera() {
        try {
            File photoFile = createImageFile();
            if (photoFile == null) {
                Toast.makeText(this, "Erreur création fichier photo", Toast.LENGTH_LONG).show();
                return;
            }

            currentImageUri = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider",
                    photoFile);

            Log.d("AddNote", "URI photo créée: " + currentImageUri);

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            List<?> activities = getPackageManager().queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);
            if (activities.isEmpty()) {
                Toast.makeText(this, "Aucune application caméra installée", Toast.LENGTH_LONG).show();
                return;
            }

            takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentImageUri);

            startActivityForResult(takePictureIntent, TAKE_PHOTO_REQUEST);

        } catch (Exception e) {
            Log.e("AddNote", "Erreur caméra: " + e.getMessage());
            Toast.makeText(this, "Erreur caméra: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "FITNESS_PHOTO_" + timeStamp;

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        if (storageDir != null && !storageDir.exists()) {
            boolean created = storageDir.mkdirs();
            if (!created) {
                throw new IOException("Impossible de créer le dossier: " + storageDir.getAbsolutePath());
            }
        }

        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        currentPhotoPath = image.getAbsolutePath();
        Log.d("AddNote", "Fichier photo créé: " + currentPhotoPath);
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("AddNote", "onActivityResult - requestCode: " + requestCode + ", resultCode: " + resultCode);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PICK_IMAGE_REQUEST:
                    if (data != null && data.getData() != null) {
                        Uri selectedImageUri = data.getData();
                        Log.d("AddNote", "Image sélectionnée depuis galerie: " + selectedImageUri);

                        // Afficher l'image immédiatement
                        currentImageUri = selectedImageUri;
                        loadImageIntoView(currentImageUri);

                        // Envoyer au service pour sauvegarde
                        sendImageToService(selectedImageUri);
                    }
                    break;

                case TAKE_PHOTO_REQUEST:
                    if (currentImageUri != null) {
                        Log.d("AddNote", "Photo prise avec caméra: " + currentImageUri);

                        // Afficher l'image immédiatement
                        loadImageIntoView(currentImageUri);

                        // Envoyer au service pour sauvegarde
                        sendImageToService(currentImageUri);
                    }
                    break;
            }
        }
    }

    private void sendImageToService(Uri imageUri) {
        try {
            Intent serviceIntent = new Intent(this, ImageImportService.class);
            serviceIntent.setAction(ImageImportService.ACTION_SAVE_IMAGE);
            serviceIntent.putExtra("IMAGE_URI", imageUri);
            startService(serviceIntent);
            Log.d("AddNote", "Image envoyée au service: " + imageUri);
        } catch (Exception e) {
            Log.e("AddNote", "Erreur envoi image au service: " + e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d("AddNote", "onRequestPermissionsResult - requestCode: " + requestCode);

        boolean allGranted = true;
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (allGranted) {
            switch (requestCode) {
                case CAMERA_PERMISSION_REQUEST:
                    takePhotoWithCamera();
                    break;
                case STORAGE_PERMISSION_REQUEST:
                    pickImageFromGallery();
                    break;
            }
        }
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

    private void cancelAndReturn() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (imageReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(imageReceiver);
        }
    }
}