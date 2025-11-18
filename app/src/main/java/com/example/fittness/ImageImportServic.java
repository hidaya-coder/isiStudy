package com.example.fittness;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ImageImportServic extends Service {

    private static final String TAG = "ImageImportService";
    public static final String ACTION_SAVE_IMAGE = "SAVE_IMAGE";
    public static final String ACTION_TAKE_PHOTO = "TAKE_PHOTO";
    public static final String ACTION_PICK_IMAGE = "PICK_IMAGE";

    public static final String BROADCAST_PHOTO_READY = "PHOTO_READY";
    public static final String BROADCAST_IMAGE_PICKED = "IMAGE_PICKED";
    public static final String BROADCAST_ERROR = "IMAGE_ERROR";
    public static final String EXTRA_IMAGE_URI = "IMAGE_URI";
    public static final String EXTRA_ERROR_MESSAGE = "ERROR_MESSAGE";

    private Uri currentPhotoUri;
    private String currentPhotoPath;

    public ImageImportServic() {}

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service d'import d'images démarré");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();

            switch (action) {
                case ACTION_SAVE_IMAGE:
                    Uri imageUri = intent.getParcelableExtra("IMAGE_URI");
                    if (imageUri != null) {
                        saveImagePermanently(imageUri);
                    }
                    break;

                case ACTION_TAKE_PHOTO:
                    takePhotoWithCamera();
                    break;

                case ACTION_PICK_IMAGE:
                    pickImageFromGallery();
                    break;
            }
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void takePhotoWithCamera() {
        try {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            // Vérifier explicitement qu'il y a une app caméra
            List<?> activities = getPackageManager().queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);
            if (activities.isEmpty()) {
                broadcastError("Aucune application caméra installée");
                return;
            }

            Log.d(TAG, "Applications caméra disponibles: " + activities.size());

            // Créer le fichier pour la photo
            File photoFile = createImageFile();
            if (photoFile == null) {
                broadcastError("Erreur création fichier photo");
                return;
            }

            // Utiliser FileProvider pour créer l'URI
            currentPhotoUri = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider",
                    photoFile);

            Log.d(TAG, "URI photo créée: " + currentPhotoUri);

            // Donner les permissions temporaires à toutes les apps
            takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri);
            takePictureIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Vérifier une dernière fois avant de lancer
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(takePictureIntent);
                Log.d(TAG, "Caméra lancée avec succès");

                // Informer que la caméra est ouverte
                broadcastPhotoReady(currentPhotoUri);
            } else {
                broadcastError("Impossible d'ouvrir l'application caméra");
            }

        } catch (Exception e) {
            Log.e(TAG, "Erreur caméra: " + e.getMessage());
            broadcastError("Erreur caméra: " + e.getMessage());
        }
    }

    private void pickImageFromGallery() {
        try {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryIntent.setType("image/*");
            galleryIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (galleryIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(galleryIntent);
                Log.d(TAG, "Galerie lancée avec succès");
            } else {
                broadcastError("Impossible d'ouvrir la galerie");
            }

        } catch (Exception e) {
            Log.e(TAG, "Erreur galerie: " + e.getMessage());
            broadcastError("Erreur galerie: " + e.getMessage());
        }
    }

    private File createImageFile() throws IOException {
        // Créer un nom de fichier unique
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "FITNESS_PHOTO_" + timeStamp;

        // Utiliser le stockage public pour la persistance
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        // Créer le dossier s'il n'existe pas
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

        // Sauvegarder le chemin pour usage futur
        currentPhotoPath = image.getAbsolutePath();
        Log.d(TAG, "Fichier photo créé: " + currentPhotoPath);

        return image;
    }

    private void saveImagePermanently(Uri imageUri) {
        new Thread(() -> {
            try {
                // Vérifier si l'image existe déjà dans un emplacement permanent
                if (isPermanentLocation(imageUri)) {
                    Log.d(TAG, "Image déjà en emplacement permanent: " + imageUri);
                    return;
                }

                // Copier l'image vers un emplacement permanent
                Uri permanentUri = copyImageToPermanentLocation(imageUri);
                if (permanentUri != null) {
                    Log.d(TAG, "Image sauvegardée avec succès: " + permanentUri);
                    showToast("Image sauvegardée");
                } else {
                    Log.e(TAG, "Échec sauvegarde image: " + imageUri);
                    showToast("Erreur sauvegarde image");
                }

            } catch (Exception e) {
                Log.e(TAG, "Erreur sauvegarde permanente: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private boolean isPermanentLocation(Uri uri) {
        try {
            String path = uri.getPath();
            return path != null && (path.contains("/Pictures/") || path.contains("/Android/data/"));
        } catch (Exception e) {
            return false;
        }
    }

    private Uri copyImageToPermanentLocation(Uri sourceUri) {
        FileOutputStream fos = null;
        InputStream inputStream = null;

        try {
            inputStream = getContentResolver().openInputStream(sourceUri);
            if (inputStream == null) {
                Log.e(TAG, "Impossible d'ouvrir l'image source: " + sourceUri);
                return null;
            }

            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (bitmap == null) {
                Log.e(TAG, "Impossible de décoder l'image: " + sourceUri);
                return null;
            }

            // Créer le fichier permanent
            File permanentFile = createPermanentImageFile();
            if (permanentFile == null) {
                Log.e(TAG, "Impossible de créer le fichier permanent");
                return null;
            }

            // Sauvegarder l'image
            fos = new FileOutputStream(permanentFile);
            boolean success = bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();

            if (success) {
                // Ajouter à la galerie
                addToGallery(permanentFile);

                // Libérer la mémoire du bitmap
                bitmap.recycle();

                return Uri.fromFile(permanentFile);
            } else {
                return null;
            }

        } catch (Exception e) {
            Log.e(TAG, "Erreur copie image: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (fos != null) fos.close();
                if (inputStream != null) inputStream.close();
            } catch (IOException e) {
                Log.e(TAG, "Erreur fermeture flux: " + e.getMessage());
            }
        }
    }

    private File createPermanentImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "FITNESS_NOTE_" + timeStamp + ".jpg";

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir == null) {
            throw new IOException("Storage directory non disponible");
        }

        if (!storageDir.exists()) {
            boolean created = storageDir.mkdirs();
            if (!created) {
                throw new IOException("Impossible de créer le dossier: " + storageDir.getAbsolutePath());
            }
        }

        File imageFile = new File(storageDir, imageFileName);
        Log.d(TAG, "Fichier permanent créé: " + imageFile.getAbsolutePath());
        return imageFile;
    }

    private void addToGallery(File imageFile) {
        try {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(imageFile);
            mediaScanIntent.setData(contentUri);
            sendBroadcast(mediaScanIntent);
            Log.d(TAG, "Image ajoutée à la galerie: " + contentUri);
        } catch (Exception e) {
            Log.e(TAG, "Erreur ajout galerie: " + e.getMessage());
        }
    }

    // Méthodes de broadcast pour communiquer avec l'activité
    private void broadcastPhotoReady(Uri photoUri) {
        Intent broadcastIntent = new Intent(BROADCAST_PHOTO_READY);
        broadcastIntent.putExtra(EXTRA_IMAGE_URI, photoUri.toString());
        sendBroadcast(broadcastIntent);
    }

    private void broadcastImagePicked(Uri imageUri) {
        Intent broadcastIntent = new Intent(BROADCAST_IMAGE_PICKED);
        broadcastIntent.putExtra(EXTRA_IMAGE_URI, imageUri.toString());
        sendBroadcast(broadcastIntent);
    }

    private void broadcastError(String errorMessage) {
        Intent broadcastIntent = new Intent(BROADCAST_ERROR);
        broadcastIntent.putExtra(EXTRA_ERROR_MESSAGE, errorMessage);
        sendBroadcast(broadcastIntent);
    }

    private void showToast(final String message) {
        new android.os.Handler(getMainLooper()).post(() -> {
            Toast.makeText(ImageImportServic.this, message, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Service d'import d'images arrêté");
        super.onDestroy();
    }
}