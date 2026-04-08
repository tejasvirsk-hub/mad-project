package com.example.assignmentmad4;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 2;
    private static final int REQUEST_FOLDER_PICKER_FOR_SAVE = 3;
    private static final int REQUEST_FOLDER_PICKER_FOR_GALLERY = 4;

    private String currentPhotoPath;
    private Uri tempPhotoUri;
    private Uri selectedFolderUriForSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnTakePhoto = findViewById(R.id.btn_take_photo);
        Button btnViewGallery = findViewById(R.id.btn_view_gallery);

        btnTakePhoto.setOnClickListener(v -> checkCameraPermission());
        btnViewGallery.setOnClickListener(v -> pickFolderForGallery());
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            pickFolderForSaving();
        }
    }

    private void pickFolderForSaving() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, REQUEST_FOLDER_PICKER_FOR_SAVE);
    }

    private void pickFolderForGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, REQUEST_FOLDER_PICKER_FOR_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_FOLDER_PICKER_FOR_SAVE && data != null) {
                selectedFolderUriForSave = data.getData();
                takePhoto();
            } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
                savePhotoToChosenFolder();
            } else if (requestCode == REQUEST_FOLDER_PICKER_FOR_GALLERY && data != null) {
                Uri treeUri = data.getData();
                // Grant persistent access if needed, but for simplicity we just pass it
                getContentResolver().takePersistableUriPermission(treeUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                Intent intent = new Intent(this, GalleryActivity.class);
                intent.putExtra("folderUri", treeUri.toString());
                startActivity(intent);
            }
        }
    }

    private void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createTempImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Error creating temp file", Toast.LENGTH_SHORT).show();
            }
            if (photoFile != null) {
                tempPhotoUri = FileProvider.getUriForFile(this,
                        "com.example.assignmentmad4.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempPhotoUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createTempImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void savePhotoToChosenFolder() {
        if (selectedFolderUriForSave == null || currentPhotoPath == null) return;

        DocumentFile pickedDir = DocumentFile.fromTreeUri(this, selectedFolderUriForSave);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "IMG_" + timeStamp + ".jpg";
        
        DocumentFile newFile = pickedDir.createFile("image/jpeg", fileName);
        if (newFile != null) {
            try (InputStream is = getContentResolver().openInputStream(tempPhotoUri);
                 OutputStream os = getContentResolver().openOutputStream(newFile.getUri())) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
                Toast.makeText(this, "Photo saved to: " + newFile.getName(), Toast.LENGTH_LONG).show();
                // Delete temp file
                new File(currentPhotoPath).delete();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to save photo", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickFolderForSaving();
            } else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
            }
        }
    }
}