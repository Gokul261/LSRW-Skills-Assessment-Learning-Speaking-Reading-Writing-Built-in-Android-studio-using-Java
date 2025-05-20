package com.example.communication_app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.RecognitionListener;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

public class SpeakingActivity extends AppCompatActivity {

    private TextView questionTextView, resultTextView, scoreTextView;
    private Button startSpeakingButton;
    private SpeechRecognizer speechRecognizer;
    private String currentQuestion;
    private long startTime, endTime;

    private FirebaseAuth auth;
    private DatabaseReference databaseReference;

    private String[] questions = {
            "Describe your favorite hobby.",
            "Talk about a memorable trip you have taken.",
            "What are your goals for the future?",
            "Describe your best friend.",
            "Talk about a movie you recently watched."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speaking);

        questionTextView = findViewById(R.id.questionTextView);
        resultTextView = findViewById(R.id.resultTextView);
        scoreTextView = findViewById(R.id.scoreTextView);
        startSpeakingButton = findViewById(R.id.startSpeakingButton);

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("FluencyScores");

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        // Request Microphone Permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }

        // Check if Speech Recognition is available
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "Speech Recognition not available on this device", Toast.LENGTH_LONG).show();
        }

        // Set a random question
        setRandomQuestion();

        startSpeakingButton.setOnClickListener(view -> startSpeechRecognition());

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                startTime = System.currentTimeMillis();  // Start timing when speech begins
            }

            @Override
            public void onBeginningOfSpeech() {}

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {
                endTime = System.currentTimeMillis();  // End timing when speech stops
            }

            @Override
            public void onError(int error) {
                String errorMessage;
                switch (error) {
                    case SpeechRecognizer.ERROR_NETWORK:
                        errorMessage = "Network error. Check your internet.";
                        break;
                    case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                        errorMessage = "Network timeout. Try again.";
                        break;
                    case SpeechRecognizer.ERROR_AUDIO:
                        errorMessage = "Audio error. Check your microphone.";
                        break;
                    case SpeechRecognizer.ERROR_NO_MATCH:
                        errorMessage = "No speech detected. Try again.";
                        break;
                    case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                        errorMessage = "You didn't speak. Try again.";
                        break;
                    default:
                        errorMessage = "Unknown error: " + error;
                }
                Toast.makeText(SpeakingActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String spokenText = matches.get(0);
                    resultTextView.setText("You said: " + spokenText);
                    calculateFluencyScore(spokenText);
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });
    }

    private void setRandomQuestion() {
        Random random = new Random();
        currentQuestion = questions[random.nextInt(questions.length)];
        questionTextView.setText("Question: " + currentQuestion);
    }

    private void startSpeechRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...");
        speechRecognizer.startListening(intent);
    }

    private void calculateFluencyScore(String spokenText) {
        String[] words = spokenText.split("\\s+");
        int wordCount = words.length;
        double durationInSeconds = (endTime - startTime) / 1000.0;

        int fluencyScore = (durationInSeconds == 0) ? 0 : (int) Math.min(wordCount / durationInSeconds * 2, 10);
        scoreTextView.setText("Fluency Score: " + fluencyScore + "/10");

        storeFluencyScore(fluencyScore);
    }

    private void storeFluencyScore(int score) {
        String sanitizedEmail = auth.getCurrentUser().getEmail().replace(".", "_").replace("@", "_");
        databaseReference.child(sanitizedEmail).child("lastScore").setValue(score);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }
}
