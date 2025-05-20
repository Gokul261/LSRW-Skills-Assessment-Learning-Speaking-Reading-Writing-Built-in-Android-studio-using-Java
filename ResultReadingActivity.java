package com.example.communication_app;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultReadingActivity extends AppCompatActivity {

    private LinearLayout resultContainer;
    private TextView scoreText;

    private List<Question> questions;
    private List<String> userAnswers;
    private String email;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_reading);

        resultContainer = findViewById(R.id.result_container);
        scoreText = findViewById(R.id.score_text);

        // Receiving data from ReadingActivity
        questions = getIntent().getParcelableArrayListExtra("questions");
        userAnswers = getIntent().getStringArrayListExtra("userAnswers");
        email = getIntent().getStringExtra("email");

        int correctAnswers = getIntent().getIntExtra("correctAnswers", 0);
        int totalQuestions = getIntent().getIntExtra("totalQuestions", questions.size());

        // Set score text
        scoreText.setText("Your Score: " + correctAnswers + " / " + totalQuestions);

        // Display result in detail
        displayResultDetails();

        // Store result in Firebase
        saveResultToFirebase(correctAnswers, totalQuestions);
    }

    private void displayResultDetails() {
        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);
            String userAnswer = userAnswers.get(i);
            String correctAnswer = question.getCorrectOption();

            // Display Question Text
            TextView questionView = new TextView(this);
            questionView.setText("Q" + (i + 1) + ": " + question.getQuestion());
            questionView.setTextSize(16);
            questionView.setTextColor(Color.BLACK);
            resultContainer.addView(questionView);

            // Display User's Answer
            TextView userAnswerView = new TextView(this);
            userAnswerView.setText("Your Answer: " + userAnswer);
            userAnswerView.setTextSize(14);

            if (userAnswer.equals(correctAnswer)) {
                userAnswerView.setTextColor(Color.GREEN);  // Correct answer - green
            } else {
                userAnswerView.setTextColor(Color.RED);    // Wrong answer - red
            }
            resultContainer.addView(userAnswerView);

            // Display Correct Answer
            TextView correctAnswerView = new TextView(this);
            correctAnswerView.setText("Correct Answer: " + correctAnswer);
            correctAnswerView.setTextSize(14);
            correctAnswerView.setTextColor(Color.GREEN);  // Always green for correct answer
            resultContainer.addView(correctAnswerView);

            // Add spacing between questions
            Space space = new Space(this);
            space.setMinimumHeight(16);
            resultContainer.addView(space);
        }
    }

    private void saveResultToFirebase(int correctAnswers, int totalQuestions) {
        databaseReference = FirebaseDatabase.getInstance().getReference("ReadingResults");

        Map<String, Object> resultData = new HashMap<>();
        resultData.put("email", email);
        resultData.put("correctAnswers", correctAnswers);
        resultData.put("totalQuestions", totalQuestions);

        List<Map<String, String>> answersList = new ArrayList<>();
        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);
            Map<String, String> answerDetail = new HashMap<>();
            answerDetail.put("question", question.getQuestion());
            answerDetail.put("userAnswer", userAnswers.get(i));
            answerDetail.put("correctAnswer", question.getCorrectOption());
            answersList.add(answerDetail);
        }
        resultData.put("answers", answersList);

        databaseReference.child(email.replace(".", "_")).setValue(resultData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ResultReadingActivity.this, "Result saved successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ResultReadingActivity.this, "Failed to save result", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
