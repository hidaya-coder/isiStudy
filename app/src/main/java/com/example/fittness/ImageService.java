package com.example.fittness;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Service;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class ImageService extends Service {

    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        public ImageService getService() {
            return ImageService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v("ImageService", "the service is alive!!!");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v("ImageService", "the service in this place is dead man!");
    }

    public ImageService() {
    }

    public Intent getPickImageIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        return intent;
    }

    public Intent getCaptureImageIntent(Context context, File photoFile) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(context,
                        context.getPackageName() + ".fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            }
        }
        return takePictureIntent;
    }

    public File createImageFile(Context context) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }

    public String saveImageToInternalStorage(Context context, Uri sourceUri) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + ".jpg";
        
        File internalStorageDir = context.getFilesDir(); // Private internal storage
        File destFile = new File(internalStorageDir, imageFileName);

        try (InputStream inputStream = context.getContentResolver().openInputStream(sourceUri);
             OutputStream outputStream = new FileOutputStream(destFile)) {

            if (inputStream == null) return null;

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            return destFile.getAbsolutePath();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String saveCameraImageToInternalStorage(Context context, File sourceFile) {
         if (sourceFile == null || !sourceFile.exists()) return null;

         String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
         String imageFileName = "IMG_" + timeStamp + ".jpg";
         File destFile = new File(context.getFilesDir(), imageFileName);
         
         try (InputStream in = new java.io.FileInputStream(sourceFile);
              OutputStream out = new FileOutputStream(destFile)) {
             byte[] buffer = new byte[1024];
             int len;
             while ((len = in.read(buffer)) > 0) {
                 out.write(buffer, 0, len);
             }
             sourceFile.delete();
             return destFile.getAbsolutePath();
         } catch (IOException e) {
             e.printStackTrace();
             return null;
         }
    }
}