package com.example.assignmentmad2;

import android.app.AlertDialog;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private VideoView videoView;
    private TextView statusText;
    private MediaPlayer mediaPlayer;
    private boolean isAudio = false;
    private Uri currentUri;

    private final ActivityResultLauncher<String> filePicker = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    currentUri = uri;
                    String mimeType = getContentResolver().getType(uri);
                    if (mimeType != null && mimeType.startsWith("audio")) {
                        setupAudio(uri);
                    } else {
                        setupVideo(uri);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        videoView = findViewById(R.id.videoView);
        statusText = findViewById(R.id.statusText);

        findViewById(R.id.btnOpenFile).setOnClickListener(v -> filePicker.launch("*/*"));

        findViewById(R.id.btnOpenUrl).setOnClickListener(v -> showUrlDialog());

        findViewById(R.id.btnPlay).setOnClickListener(v -> {
            if (isAudio && mediaPlayer != null) {
                mediaPlayer.start();
            } else if (!isAudio) {
                videoView.start();
            }
        });

        findViewById(R.id.btnPause).setOnClickListener(v -> {
            if (isAudio && mediaPlayer != null) {
                mediaPlayer.pause();
            } else if (!isAudio) {
                videoView.pause();
            }
        });

        findViewById(R.id.btnStop).setOnClickListener(v -> {
            if (isAudio && mediaPlayer != null) {
                mediaPlayer.pause();
                mediaPlayer.seekTo(0);
            } else if (!isAudio) {
                videoView.pause();
                videoView.seekTo(0);
            }
        });

        findViewById(R.id.btnRestart).setOnClickListener(v -> {
            if (isAudio && mediaPlayer != null) {
                mediaPlayer.seekTo(0);
                mediaPlayer.start();
            } else if (!isAudio) {
                videoView.seekTo(0);
                videoView.start();
            }
        });
    }

    private void setupAudio(Uri uri) {
        stopAndRelease();
        isAudio = true;
        videoView.setVisibility(View.GONE);
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(getApplicationContext(), uri);
            mediaPlayer.prepare();
            statusText.setText("Audio Loaded: " + uri.getLastPathSegment());
        } catch (IOException e) {
            statusText.setText("Error loading audio");
            e.printStackTrace();
        }
    }

    private void setupVideo(Uri uri) {
        stopAndRelease();
        isAudio = false;
        videoView.setVisibility(View.VISIBLE);
        videoView.setVideoURI(uri);
        videoView.setOnPreparedListener(mp -> 
                statusText.setText("Video Ready: " + uri.getLastPathSegment())
        );
    }

    private void showUrlDialog() {
        EditText input = new EditText(this);
        input.setHint("Enter Video URL");
        new AlertDialog.Builder(this)
                .setTitle("Stream Video")
                .setView(input)
                .setPositiveButton("Stream", (dialog, which) -> {
                    String url = input.getText().toString();
                    if (!url.isEmpty()) {
                        Uri uri = Uri.parse(url);
                        currentUri = uri;
                        setupVideo(uri);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void stopAndRelease() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        videoView.stopPlayback();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAndRelease();
    }
}