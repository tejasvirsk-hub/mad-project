package com.example.assignmentmad4;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageDetailsActivity extends AppCompatActivity {

    private ImageView ivDetailImage;
    private TextView tvImageName, tvImagePath, tvImageSize, tvImageDate;
    private Button btnDeleteImage;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_details);

        ivDetailImage = findViewById(R.id.iv_detail_image);
        tvImageName = findViewById(R.id.tv_image_name);
        tvImagePath = findViewById(R.id.tv_image_path);
        tvImageSize = findViewById(R.id.tv_image_size);
        tvImageDate = findViewById(R.id.tv_image_date);
        btnDeleteImage = findViewById(R.id.btn_delete_image);

        String uriString = getIntent().getStringExtra("imageUri");
        if (uriString != null) {
            imageUri = Uri.parse(uriString);
            displayImageDetails();
        }

        btnDeleteImage.setOnClickListener(v -> showDeleteConfirmation());
    }

    private void displayImageDetails() {
        DocumentFile file = DocumentFile.fromSingleUri(this, imageUri);
        if (file != null && file.exists()) {
            ivDetailImage.setImageURI(imageUri);
            tvImageName.setText("Name: " + file.getName());
            tvImagePath.setText("Path: " + imageUri.getPath());
            tvImageSize.setText("Size: " + (file.length() / 1024) + " KB");

            long lastModified = file.lastModified();
            if (lastModified > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                tvImageDate.setText("Date: " + sdf.format(new Date(lastModified)));
            }
        }
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Image")
                .setMessage("Are you sure you want to delete this image?")
                .setPositiveButton("Delete", (dialog, which) -> deleteImage())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteImage() {
        DocumentFile file = DocumentFile.fromSingleUri(this, imageUri);
        if (file != null && file.delete()) {
            Toast.makeText(this, "Image deleted", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Failed to delete image", Toast.LENGTH_SHORT).show();
        }
    }
}