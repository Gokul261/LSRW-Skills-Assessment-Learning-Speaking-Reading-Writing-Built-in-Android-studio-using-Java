package com.example.communication_app;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ReadingActivity extends AppCompatActivity {
    private TextView sentenceTextView, resultTextView;
    private Button btnListen, btnSpeak;
    private TextToSpeech textToSpeech;
    private SpeechRecognizer speechRecognizer;
    private List<String> sentenceList;
    private String currentSentence;
    private FirebaseDatabase database;
    private DatabaseReference scoresRef;
    private FirebaseUser currentUser;
    private String sanitizedEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading);

        sentenceTextView = findViewById(R.id.wordTextView);
        resultTextView = findViewById(R.id.resultTextView);
        btnListen = findViewById(R.id.btnListen);
        btnSpeak = findViewById(R.id.btnSpeak);

        database = FirebaseDatabase.getInstance();
        scoresRef = database.getReference("PronunciationScores");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            sanitizedEmail = currentUser.getEmail().replace(".", "_dot_").replace("@", "_at_");
        }

        sentenceList = new ArrayList<>();
        Collections.addAll(sentenceList,
                "The quick brown fox jumps over the lazy dog happily.",
                "She enjoys reading books while drinking a cup of coffee.",
                "Learning a new language requires patience, practice, and dedication daily.",
                "The teacher explained the grammar rules in a very simple way.",
                "Listening to English podcasts can improve both pronunciation and vocabulary.",
                "He always tries to communicate clearly and effectively with his colleagues.",
                "The weather today is perfect for a long walk in nature.",
                "Writing essays helps students to express their thoughts more clearly.",
                "Good communication skills are essential for success in any profession today.",
                "Pronouncing words correctly makes conversations easier and more understandable for everyone."
        );

        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.ENGLISH);
            }
        });

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    evaluatePronunciation(matches.get(0));
                }
            }
            public void onError(int error) { Toast.makeText(ReadingActivity.this, "Speech Recognition Failed!", Toast.LENGTH_SHORT).show(); }
            public void onReadyForSpeech(Bundle params) {}
            public void onBeginningOfSpeech() {}
            public void onRmsChanged(float rmsdB) {}
            public void onBufferReceived(byte[] buffer) {}
            public void onEndOfSpeech() {}
            public void onPartialResults(Bundle partialResults) {}
            public void onEvent(int eventType, Bundle params) {}
        });

        btnListen.setOnClickListener(v -> textToSpeech.speak(currentSentence, TextToSpeech.QUEUE_FLUSH, null, null));
        btnSpeak.setOnClickListener(v -> startSpeechRecognition());

        loadNextSentence();
    }

    private void loadNextSentence() {
        if (sentenceList.isEmpty()) {
            Toast.makeText(this, "No sentences available", Toast.LENGTH_SHORT).show();
            return;
        }
        currentSentence = sentenceList.get((int) (Math.random() * sentenceList.size()));
        sentenceTextView.setText(currentSentence);
    }

    private void startSpeechRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say the sentence: " + currentSentence);
        speechRecognizer.startListening(intent);
    }

    private void evaluatePronunciation(String spokenSentence) {
        int score = calculatePronunciationScore(spokenSentence, currentSentence);
        if (score > 9) score = 9;  // Ensure the score does not exceed 9
        resultTextView.setText("Score: " + score + "/10");
        resultTextView.setVisibility(View.VISIBLE);

        if (currentUser != null) {
            saveScoreToFirebase(score);
        }

        loadNextSentence();
    }

    private int calculatePronunciationScore(String spoken, String expected) {
        String[] spokenWords = spoken.toLowerCase().split(" ");
        String[] expectedWords = expected.toLowerCase().split(" ");

        int correctWords = 0;
        int minLength = Math.min(spokenWords.length, expectedWords.length);

        for (int i = 0; i < minLength; i++) {
            if (spokenWords[i].equals(expectedWords[i])) {
                correctWords++;
            }
        }

        int score = (int) (((double) correctWords / expectedWords.length) * 10);
        return Math.min(score, 9); // Ensure the score does not exceed 9
    }

    private void saveScoreToFirebase(int score) {
        scoresRef.child(sanitizedEmail).child("scores").push().setValue(score);
        scoresRef.child(sanitizedEmail).child("completedSentences").push().setValue(currentSentence);
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        super.onDestroy();
    }
}
