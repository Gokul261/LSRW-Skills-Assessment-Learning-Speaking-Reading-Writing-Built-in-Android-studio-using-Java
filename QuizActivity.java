package com.example.communication_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuizActivity extends AppCompatActivity {

    private LinearLayout quizContainer;
    private Button btnSubmitQuiz;

    private List<Question> questions = new ArrayList<>();
    private Map<Integer, String> userAnswers = new HashMap<>();  // To store user-selected answers

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        quizContainer = findViewById(R.id.quizContainer);
        btnSubmitQuiz = findViewById(R.id.btnSubmitQuiz);

        loadQuestionsFromCSV();

        btnSubmitQuiz.setOnClickListener(v -> goToResultPage());
    }

    private void loadQuestionsFromCSV() {
        try {
            InputStream is = getAssets().open("listening_quiz_questions.csv");  // File must be placed inside src/main/assets
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            String line;
            reader.readLine(); // Skip the first header line (Question,Option1,Option2...)

            int index = 0;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 6) {
                    continue; // Skip if format is wrong
                }

                Question q = new Question(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]);
                questions.add(q);
                addQuestionToLayout(index++, q);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to load questions.", Toast.LENGTH_SHORT).show();
        }
    }

    private void addQuestionToLayout(int index, Question q) {
        // Display the question
        TextView tv = new TextView(this);
        tv.setText((index + 1) + ". " + q.question);
        tv.setPadding(0, 16, 0, 8);
        quizContainer.addView(tv);

        // Create a RadioGroup for options
        RadioGroup group = new RadioGroup(this);

        String[] options = {q.option1, q.option2, q.option3, q.option4};
        for (String option : options) {
            RadioButton rb = new RadioButton(this);
            rb.setText(option);

            int finalIndex = index;
            rb.setOnClickListener(v -> userAnswers.put(finalIndex, option));  // Capture selected option

            group.addView(rb);
        }

        quizContainer.addView(group);
    }

    private void goToResultPage() {
        if (userAnswers.size() < questions.size()) {
            Toast.makeText(this, "Please answer all questions!", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, ResultListeningActivity.class);
        intent.putExtra("questions", (Serializable) questions);
        intent.putExtra("userAnswers", (Serializable) userAnswers);
        startActivity(intent);
        finish();
    }

    public static class Question implements Serializable {
        String question, option1, option2, option3, option4, correctAnswer;

        public Question(String question, String option1, String option2, String option3, String option4, String correctAnswer) {
            this.question = question;
            this.option1 = option1;
            this.option2 = option2;
            this.option3 = option3;
            this.option4 = option4;
            this.correctAnswer = correctAnswer;
        }
    }
}
