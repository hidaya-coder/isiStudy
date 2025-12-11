package com.example.fittness;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.ImageButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class AddNote extends AppCompatActivity {
    private EditText titleEdit;
    private EditText contentEdit;
    private Button saveButton;
    private Button cancelButton;
    private Button attachImageButton;
    private ImageView noteImageView;
    private View backButton;
    private ImageButton deleteImageButton;
    private View imageContainer;
    private NoteManager noteManager;
    private Note currentNote;
    private ImageService imageService;
    private String currentImagePath;
    private File currentTempCameraFile;
    
    // Activity Result Launchers
    private ActivityResultLauncher<String[]> requestPermissionLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Check authentication
        AuthHelper.requireLogin(this);
        if (!AuthHelper.isLoggedIn(this)) {
            return;
        }
        
        if (savedInstanceState != null) {
            String tempPath = savedInstanceState.getString("temp_camera_path");
            if (tempPath != null) {
                currentTempCameraFile = new File(tempPath);
            }
            currentImagePath = savedInstanceState.getString("current_image_path");
        }
        
        setContentView(R.layout.activity_add_note);

        initializeViews();
        setupResultLaunchers();
        setupClickListeners();
        loadNoteIfEditing();
    }
    
    private void setupResultLaunchers() {
        imageService = new ImageService();

        // Permission Launcher
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    boolean allGranted = true;
                    for (Boolean granted : result.values()) {
                        if (!granted) {
                            allGranted = false;
                            break;
                        }
                    }
                    if (allGranted) {
                        Toast.makeText(this, "Permissions Granted", Toast.LENGTH_SHORT).show();
                        showImageSourceDialog();
                    } else {
                        Toast.makeText(this, "Permissions Denied", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Camera Launcher
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        if (currentTempCameraFile != null && currentTempCameraFile.exists()) {
                            String permPath = imageService.saveCameraImageToInternalStorage(this, currentTempCameraFile);
                            if (permPath != null) {
                                currentImagePath = permPath;
                                displayImage(currentImagePath);
                            } else {
                                Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
        );

        // Gallery Launcher
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            String permPath = imageService.saveImageToInternalStorage(this, selectedImageUri);
                            if (permPath != null) {
                                currentImagePath = permPath;
                                displayImage(currentImagePath);
                            } else {
                                Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
        );
    }

    private void initializeViews() {
        noteManager = new NoteManager(this);
        titleEdit = findViewById(R.id.noteTitleEdit);
        contentEdit = findViewById(R.id.noteContentEdit);
        attachImageButton = findViewById(R.id.attachImageButton);
        noteImageView = findViewById(R.id.noteImageView);
        deleteImageButton = findViewById(R.id.deleteImageButton);
        imageContainer = findViewById(R.id.imageContainer);
        saveButton = findViewById(R.id.saveNoteButton);
        cancelButton = findViewById(R.id.cancelNoteButton);
        backButton = findViewById(R.id.backButton);
    }

    private void setupClickListeners() {
        saveButton.setOnClickListener(v -> saveNote());
        cancelButton.setOnClickListener(v -> finish());
        attachImageButton.setOnClickListener(v -> checkPermissionsAndOpenDialog());
        deleteImageButton.setOnClickListener(v -> removeImage());

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
    
    private void checkPermissionsAndOpenDialog() {
        boolean cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean storagePermission;
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }

        if (cameraPermission && storagePermission) {
            showImageSourceDialog();
        } else {
            String[] permissions;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES};
            } else {
                permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
            }
            requestPermissionLauncher.launch(permissions);
        }
    }

    private void showImageSourceDialog() {
        CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Photo");
        builder.setItems(options, (dialog, item) -> {
            if (options[item].equals("Take Photo")) {
                 try {
                     currentTempCameraFile = imageService.createImageFile(this);
                     Intent intent = imageService.getCaptureImageIntent(this, currentTempCameraFile);
                     cameraLauncher.launch(intent);
                 } catch (IOException e) {
                     e.printStackTrace();
                     Toast.makeText(this, "Error creating file", Toast.LENGTH_SHORT).show();
                 }
            } else if (options[item].equals("Choose from Gallery")) {
                Intent intent = imageService.getPickImageIntent();
                galleryLauncher.launch(intent);
            } else if (options[item].equals("Cancel")) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void displayImage(String path) {
        if (path != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            noteImageView.setImageBitmap(bitmap);
            imageContainer.setVisibility(View.VISIBLE);
        } else {
            imageContainer.setVisibility(View.GONE);
        }
    }
    
    private void removeImage() {
        currentImagePath = null;
        imageContainer.setVisibility(View.GONE);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (currentTempCameraFile != null) {
            outState.putString("temp_camera_path", currentTempCameraFile.getAbsolutePath());
        }
        if (currentImagePath != null) {
            outState.putString("current_image_path", currentImagePath);
        }
    }

    private void loadNote(long noteId) {
        for (Note note : noteManager.getAllNotes()) {
            if (note.getId() == noteId) {
                currentNote = note;
                titleEdit.setText(note.getTitle());
                contentEdit.setText(note.getContent());
                currentImagePath = note.getImagePath();
                if (currentImagePath != null) {
                    displayImage(currentImagePath);
                }
                break;
            }
        }
    }

    private void saveNote() {
        String title = titleEdit.getText().toString().trim();
        String content = contentEdit.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Note title cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentNote != null) {
            currentNote.setTitle(title);
            currentNote.setContent(content);
            currentNote.setImagePath(currentImagePath);
            noteManager.updateNote(currentNote);
        } else {
            noteManager.addNote(title, content, currentImagePath);
        }

        Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show();
        finish();
    }
}