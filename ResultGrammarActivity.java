package com.example.communication_app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ResultGrammarActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private DatabaseReference databaseRef;
    private TextView resultText;
    private LinearLayout questionsContainer;
    private Button finishButton;

    private ArrayList<GrammarActivity.QuestionModel> questionList;
    private String[] selectedAnswers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_grammar);

        resultText = findViewById(R.id.result_text);
        questionsContainer = findViewById(R.id.questions_container);
        finishButton = findViewById(R.id.finish_button);

        auth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference("Scores");

        // Get score and questions/answers from intent
        int score = getIntent().getIntExtra("score", 0);
        questionList = (ArrayList<GrammarActivity.QuestionModel>) getIntent().getSerializableExtra("questions");
        selectedAnswers = getIntent().getStringArrayExtra("selectedAnswers");

        resultText.setText("You scored: " + score + "/" + questionList.size());

        displayQuestionsWithAnswers();

        storeResultInFirebase(score);

        finishButton.setOnClickListener(v -> {
            startActivity(new Intent(ResultGrammarActivity.this, HomeActivity.class));
            finish();
        });
    }

    private void displayQuestionsWithAnswers() {
        for (int i = 0; i < questionList.size(); i++) {
            GrammarActivity.QuestionModel question = questionList.get(i);

            // Create question TextView
            TextView questionView = new TextView(this);
            questionView.setText((i + 1) + ". " + question.getQuestion());
            questionView.setTextSize(18);
            questionView.setPadding(0, 10, 0, 5);

            questionsContainer.addView(questionView);

            // Create options views with color logic
            addOptionView(question.getOption1(), question.getCorrectAnswer(), selectedAnswers[i]);
            addOptionView(question.getOption2(), question.getCorrectAnswer(), selectedAnswers[i]);
            addOptionView(question.getOption3(), question.getCorrectAnswer(), selectedAnswers[i]);
            addOptionView(question.getOption4(), question.getCorrectAnswer(), selectedAnswers[i]);

            View divider = new View(this);
            divider.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    2
            ));
            divider.setBackgroundColor(Color.LTGRAY);
            questionsContainer.addView(divider);
        }
    }

    private void addOptionView(String optionText, String correctAnswer, String selectedAnswer) {
        TextView optionView = new TextView(this);
        optionView.setText("• " + optionText);
        optionView.setTextSize(16);
        optionView.setPadding(20, 5, 0, 5);

        if (optionText.equals(correctAnswer)) {
            optionView.setTextColor(Color.GREEN);
        } else if (optionText.equals(selectedAnswer)) {
            optionView.setTextColor(Color.RED);
        } else {
            optionView.setTextColor(Color.BLACK);
        }

        questionsContainer.addView(optionView);
    }

    private void storeResultInFirebase(int score) {
        if (auth.getCurrentUser() != null) {
            String userEmail = auth.getCurrentUser().getEmail().replace(".", "_"); // Firebase-safe key
            databaseRef.child(userEmail).setValue(score)
                    .addOnSuccessListener(aVoid -> Toast.makeText(ResultGrammarActivity.this, "Score saved successfully!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(ResultGrammarActivity.this, "Failed to save score.", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "User not logged in. Score not saved.", Toast.LENGTH_SHORT).show();
        }
    }
}
