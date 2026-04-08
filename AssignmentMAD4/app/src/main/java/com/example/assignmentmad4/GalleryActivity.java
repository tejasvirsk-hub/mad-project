package com.example.assignmentmad4;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    private GridView gvGallery;
    private TextView tvFolderPath;
    private ImageAdapter adapter;
    private List<DocumentFile> imageFiles = new ArrayList<>();
    private Uri folderUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        gvGallery = findViewById(R.id.gv_gallery);
        tvFolderPath = findViewById(R.id.tv_folder_path);

        String uriString = getIntent().getStringExtra("folderUri");
        if (uriString != null) {
            folderUri = Uri.parse(uriString);
            tvFolderPath.setText("Folder: " + folderUri.getPath());
            loadImages();
        }

        gvGallery.setOnItemClickListener((parent, view, position, id) -> {
            DocumentFile file = imageFiles.get(position);
            Intent intent = new Intent(this, ImageDetailsActivity.class);
            intent.putExtra("imageUri", file.getUri().toString());
            startActivityForResult(intent, 100);
        });
    }

    private void loadImages() {
        imageFiles.clear();
        DocumentFile rootFolder = DocumentFile.fromTreeUri(this, folderUri);
        if (rootFolder != null && rootFolder.isDirectory()) {
            for (DocumentFile file : rootFolder.listFiles()) {
                if (file.getType() != null && file.getType().startsWith("image/")) {
                    imageFiles.add(file);
                }
            }
        }
        adapter = new ImageAdapter(this, imageFiles);
        gvGallery.setAdapter(adapter);
        if (imageFiles.isEmpty()) {
            Toast.makeText(this, "No images found in folder", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            loadImages();
        }
    }

    private class ImageAdapter extends BaseAdapter {
        private Context context;
        private List<DocumentFile> files;

        public ImageAdapter(Context context, List<DocumentFile> files) {
            this.context = context;
            this.files = files;
        }

        @Override
        public int getCount() {
            return files.size();
        }

        @Override
        public Object getItem(int position) {
            return files.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.gallery_item, parent, false);
                imageView = convertView.findViewById(R.id.iv_gallery_item);
                convertView.setTag(imageView);
            } else {
                imageView = (ImageView) convertView.getTag();
            }

            imageView.setImageURI(files.get(position).getUri());
            return convertView;
        }
    }
}