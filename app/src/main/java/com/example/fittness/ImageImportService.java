package com.example.fittness;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageImportService extends IntentService {

    private static final String TAG = "ImageImportService";
    public static final String ACTION_SAVE_IMAGE = "SAVE_IMAGE";

    public static final String BROADCAST_IMAGE_SAVED = "IMAGE_SAVED";
    public static final String EXTRA_IMAGE_URI = "IMAGE_URI";

    public ImageImportService() {
        super("ImageImportService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();

            Log.d(TAG, "Action reçue dans onHandleIntent: " + action);

            if (ACTION_SAVE_IMAGE.equals(action)) {
                Uri imageUri = intent.getParcelableExtra("IMAGE_URI");
                if (imageUri != null) {
                    saveImagePermanently(imageUri);
                }
            }
        }
    }

    private void saveImagePermanently(Uri imageUri) {
        try {
            Log.d(TAG, "Début sauvegarde image: " + imageUri);

            // Copier l'image vers un emplacement permanent
            Uri permanentUri = saveImagePermanentlySync(imageUri);
            if (permanentUri != null) {
                Log.d(TAG, "Image sauvegardée avec succès: " + permanentUri);
                showToast("Image sauvegardée");

                // Envoyer l'URI sauvegardé à l'activité
                broadcastImageSaved(permanentUri);
            } else {
                Log.e(TAG, "Échec sauvegarde image: " + imageUri);
                // Envoyer l'URI original si la sauvegarde échoue
                broadcastImageSaved(imageUri);
            }

        } catch (Exception e) {
            Log.e(TAG, "Erreur sauvegarde permanente: " + e.getMessage());
            e.printStackTrace();
            // Envoyer l'URI original en cas d'erreur
            broadcastImageSaved(imageUri);
        }
    }

    private Uri saveImagePermanentlySync(Uri sourceUri) {
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

                Log.d(TAG, "Image sauvegardée vers: " + permanentFile.getAbsolutePath());
                return Uri.fromFile(permanentFile);
            } else {
                return null;
            }

        } catch (Exception e) {
            Log.e(TAG, "Erreur sauvegarde image: " + e.getMessage());
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

    // Méthode de broadcast pour communiquer avec l'activité
    private void broadcastImageSaved(Uri imageUri) {
        Intent broadcastIntent = new Intent(BROADCAST_IMAGE_SAVED);
        broadcastIntent.putExtra(EXTRA_IMAGE_URI, imageUri.toString());
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
        Log.d(TAG, "Broadcast image sauvegardée: " + imageUri);
    }

    private void showToast(final String message) {
        new android.os.Handler(getMainLooper()).post(() -> {
            Toast.makeText(ImageImportService.this, message, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Service d'import d'images arrêté");
        super.onDestroy();
    }
}