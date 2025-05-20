package com.example.communication_app;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class ListeningActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private Button btnPlayPause;
    private boolean isPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listening);

        btnPlayPause = findViewById(R.id.btnPlayPause);

        // Initialize MediaPlayer
        setupMediaPlayer();

        // Play/Pause button listener
        btnPlayPause.setOnClickListener(v -> handlePlayPause());
    }

    private void setupMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        try {
            AssetFileDescriptor afd = getAssets().openFd("listening_audio.mp3");  // Make sure the file is inside /src/main/assets/
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to load audio file", Toast.LENGTH_SHORT).show();
        }

        // When audio finishes, navigate to Quiz screen
        mediaPlayer.setOnCompletionListener(mp -> {
            Toast.makeText(this, "Audio Finished! Starting Quiz...", Toast.LENGTH_SHORT).show();
            startQuiz();
        });
    }

    private void handlePlayPause() {
        if (isPlaying) {
            mediaPlayer.pause();
            btnPlayPause.setText("Resume Listening");
        } else {
            mediaPlayer.start();
            btnPlayPause.setText("Pause Listening");
        }
        isPlaying = !isPlaying;
    }

    private void startQuiz() {
        Intent intent = new Intent(ListeningActivity.this, QuizActivity.class);
        startActivity(intent);
        finish();  // Close ListeningActivity to avoid back navigation to it.
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
