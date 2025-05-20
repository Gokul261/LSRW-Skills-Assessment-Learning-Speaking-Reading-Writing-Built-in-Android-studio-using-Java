package com.example.communication_app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultListeningActivity extends AppCompatActivity {

    private LinearLayout resultContainer;
    private Button btnFinish;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private int correctCount = 0;
    private int totalQuestions = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_listening);

        resultContainer = findViewById(R.id.resultContainer);
        btnFinish = findViewById(R.id.btnFinish);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Check if user is logged in
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "No user logged in!", Toast.LENGTH_LONG).show();
            finish();
            return; // Stop further execution if no user is logged in
        } else {
            Log.d("FirebaseUser", "Logged in as: " + auth.getCurrentUser().getEmail());
        }

        // Fetch data from intent
        List<QuizActivity.Question> questions = (List<QuizActivity.Question>) getIntent().getSerializableExtra("questions");
        Map<Integer, String> userAnswers = (Map<Integer, String>) getIntent().getSerializableExtra("userAnswers");

        if (questions == null || userAnswers == null) {
            Toast.makeText(this, "Error loading quiz data!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        totalQuestions = questions.size();

        // Display results
        displayResults(questions, userAnswers);
        showScoreSummary();

        btnFinish.setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });
    }

    private void displayResults(List<QuizActivity.Question> questions, Map<Integer, String> userAnswers) {
        for (int i = 0; i < questions.size(); i++) {
            QuizActivity.Question q = questions.get(i);
            String userAnswer = userAnswers.get(i);

            TextView questionView = new TextView(this);
            questionView.setText((i + 1) + ". " + q.question);
            questionView.setTextSize(18f);
            questionView.setPadding(0, 16, 0, 8);

            TextView answerView = new TextView(this);
            answerView.setText("Your Answer: " + (userAnswer != null ? userAnswer : "No Answer") +
                    "\nCorrect Answer: " + q.correctAnswer);
            answerView.setTextSize(16f);

            if (userAnswer != null && userAnswer.equalsIgnoreCase(q.correctAnswer)) {
                answerView.setTextColor(Color.GREEN);
                correctCount++;
            } else {
                answerView.setTextColor(Color.RED);
            }

            resultContainer.addView(questionView);
            resultContainer.addView(answerView);
        }

        // After displaying, store result
        storeResultInFirebase();
    }

    private void showScoreSummary() {
        TextView tvSummary = new TextView(this);
        tvSummary.setTextSize(22f);
        tvSummary.setTextColor(Color.BLACK);
        tvSummary.setText("You scored: " + correctCount + "/" + totalQuestions);
        tvSummary.setPadding(0, 0, 0, 24);

        resultContainer.addView(tvSummary, 0); // Add at the top
    }

    private void storeResultInFirebase() {
        String email = auth.getCurrentUser().getEmail();
        String documentName = email.replace(".", "_") + "_" + System.currentTimeMillis();

        Map<String, Object> resultData = new HashMap<>();
        resultData.put("email", email);
        resultData.put("correctCount", correctCount);
        resultData.put("totalQuestions", totalQuestions);
        resultData.put("timestamp", System.currentTimeMillis());

        Toast.makeText(this, "Result saved successfully!", Toast.LENGTH_SHORT).show();

        db.collection("ListeningResults").document(documentName)
                .set(resultData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Result saved successfully!", Toast.LENGTH_SHORT).show();
                    Log.d("FirestoreSave", "Result saved to document: " + documentName);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save result: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("FirestoreError", "Error saving result", e);
                });
    }
}
