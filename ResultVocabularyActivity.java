package com.example.communication_app;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
public class ResultVocabularyActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private DatabaseReference databaseRef;
    private TextView resultText;
    private LinearLayout questionsContainer;
    private Button finishButton;
    private ArrayList<VocabularyActivity.QuestionModel> questionList;
    private String[] selectedAnswers;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_vocabulary);
        // Initialize UI components
        resultText = findViewById(R.id.result_text);
        questionsContainer = findViewById(R.id.questions_container);
        finishButton = findViewById(R.id.finish_button);
        // Firebase setup
        auth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference("vocabulary");
        // Receiving data from VocabularyActivity
        int score = getIntent().getIntExtra("score", 0);
        questionList = (ArrayList<VocabularyActivity.QuestionModel>) getIntent().getSerializableExtra("questions");
        selectedAnswers = getIntent().getStringArrayExtra("selectedAnswers");
        // Display score
        resultText.setText("You scored: " + score + "/" + questionList.size());
        // Display all questions with correct and selected answers
        displayQuestionsWithAnswers();
        // Save result to Firebase
        storeResultInFirebase(score);
        // Handle Finish button click
        finishButton.setOnClickListener(v -> {
            startActivity(new Intent(ResultVocabularyActivity.this, HomeActivity.class));
            finish();
        });
    }
    private void displayQuestionsWithAnswers() {
        for (int i = 0; i < questionList.size(); i++) {
            VocabularyActivity.QuestionModel question = questionList.get(i);
            // Show question
            TextView questionView = new TextView(this);
            questionView.setText((i + 1) + ". " + question.getQuestion());
            questionView.setTextSize(18);
            questionView.setPadding(0, 10, 0, 5);
            questionsContainer.addView(questionView);
            // Show options - color coded
            addOptionView(question.getOption1(), question.getCorrectAnswer(), selectedAnswers[i]);
            addOptionView(question.getOption2(), question.getCorrectAnswer(), selectedAnswers[i]);
            addOptionView(question.getOption3(), question.getCorrectAnswer(), selectedAnswers[i]);
            addOptionView(question.getOption4(), question.getCorrectAnswer(), selectedAnswers[i]);
        }
    }
    private void addOptionView(String optionText, String correctAnswer, String selectedAnswer) {
        TextView optionView = new TextView(this);
        optionView.setText("• " + optionText);
        optionView.setTextSize(16);
        optionView.setPadding(20, 5, 0, 5);
        if (optionText.equals(correctAnswer)) {
            optionView.setTextColor(Color.GREEN);  // Correct answer
        } else if (optionText.equals(selectedAnswer)) {
            optionView.setTextColor(Color.RED);    // Wrong selected answer
        } else {
            optionView.setTextColor(Color.BLACK);  // Other options
        }
        questionsContainer.addView(optionView);
    }
    private void storeResultInFirebase(int score) {
        if (auth.getCurrentUser() != null) {
            String userEmail = auth.getCurrentUser().getEmail().replace(".", "_");  // Firebase-safe key
            DatabaseReference userRef = databaseRef.child(userEmail);
            // Create data map with score and optional timestamp
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("vocabulary_score", score);
            // Save data under the user's node
            userRef.setValue(resultData)
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(ResultVocabularyActivity.this, "Score saved successfully!", Toast.LENGTH_SHORT).show()
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(ResultVocabularyActivity.this, "Failed to save score: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        } else {
            Toast.makeText(this, "User not logged in. Score not saved.", Toast.LENGTH_SHORT).show();
        }
    }
}
