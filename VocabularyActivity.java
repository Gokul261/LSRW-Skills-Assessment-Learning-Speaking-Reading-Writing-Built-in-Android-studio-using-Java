package com.example.communication_app;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
public class VocabularyActivity extends AppCompatActivity {
    private TextView questionNumber, questionText;
    private RadioButton option1, option2, option3, option4;
    private Button submitButton, previousButton;
    private RadioGroup optionsGroup;
    private List<QuestionModel> questionList;
    private String[] selectedAnswers;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private FirebaseAuth auth;
    private DatabaseReference databaseRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocabulary);
        auth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference("Scores");
        // Initialize UI components
        questionNumber = findViewById(R.id.question_number);
        questionText = findViewById(R.id.question_text);
        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);
        option4 = findViewById(R.id.option4);
        optionsGroup = findViewById(R.id.options_group);
        submitButton = findViewById(R.id.submit_button);
        previousButton = findViewById(R.id.previous_button);
        // Load questions and remove duplicates
        questionList = loadUniqueQuestionsFromCSV();
        Collections.shuffle(questionList);
        selectedAnswers = new String[questionList.size()];
        if (!questionList.isEmpty()) {
            displayQuestion(currentQuestionIndex);
        }
        // Button listeners
        submitButton.setOnClickListener(v -> goToNextQuestion());
        previousButton.setOnClickListener(v -> goToPreviousQuestion());
    }
    private void displayQuestion(int index) {
        if (index < 0 || index >= questionList.size()) return;
        QuestionModel question = questionList.get(index);
        questionNumber.setText("Question " + (index + 1) + " of " + questionList.size());
        questionText.setText(question.getQuestion());
        option1.setText(question.getOption1());
        option2.setText(question.getOption2());
        option3.setText(question.getOption3());
        option4.setText(question.getOption4());
        optionsGroup.setOnCheckedChangeListener(null);
        optionsGroup.clearCheck();
        if (selectedAnswers[index] != null) {
            if (option1.getText().toString().equals(selectedAnswers[index])) option1.setChecked(true);
            else if (option2.getText().toString().equals(selectedAnswers[index])) option2.setChecked(true);
            else if (option3.getText().toString().equals(selectedAnswers[index])) option3.setChecked(true);
            else if (option4.getText().toString().equals(selectedAnswers[index])) option4.setChecked(true);
        }
        optionsGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton selectedRadioButton = findViewById(checkedId);
            if (selectedRadioButton != null) {
                selectedAnswers[currentQuestionIndex] = selectedRadioButton.getText().toString();
            }
        });
    }
    private void goToNextQuestion() {
        if (selectedAnswers[currentQuestionIndex] == null) {
            Toast.makeText(this, "Please select an answer before proceeding.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentQuestionIndex < questionList.size() - 1) {
            currentQuestionIndex++;
            displayQuestion(currentQuestionIndex);
        } else {
            if (allQuestionsAnswered()) {
                calculateAndStoreScore();
            } else {
                Toast.makeText(this, "Please answer all questions before submitting.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void goToPreviousQuestion() {
        if (currentQuestionIndex > 0) {
            currentQuestionIndex--;
            displayQuestion(currentQuestionIndex);
        } else {
            Toast.makeText(this, "This is the first question", Toast.LENGTH_SHORT).show();
        }
    }
    private boolean allQuestionsAnswered() {
        for (String answer : selectedAnswers) {
            if (answer == null) {
                return false;
            }
        }
        return true;
    }
    private void calculateAndStoreScore() {
        score = 0;
        for (int i = 0; i < questionList.size(); i++) {
            String correctAnswer = questionList.get(i).getCorrectAnswer();
            if (correctAnswer.equals(selectedAnswers[i])) {
                score++;
            }
        }
        navigateToResultPage();
    }
    private void navigateToResultPage() {
        Intent intent = new Intent(VocabularyActivity.this, ResultVocabularyActivity.class);
        intent.putExtra("score", score);
        intent.putExtra("questions", new ArrayList<>(questionList));
        intent.putExtra("selectedAnswers", selectedAnswers);
        startActivity(intent);
        finish();
    }
    private List<QuestionModel> loadUniqueQuestionsFromCSV() {
        List<QuestionModel> questions = new ArrayList<>();
        Set<String> uniqueQuestions = new HashSet<>();
        try {
            AssetManager assetManager = getAssets();
            InputStream is = assetManager.open("Vocabulary_Questions (1).csv");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 6) {
                    QuestionModel question = new QuestionModel(data[0], data[1], data[2], data[3], data[4], data[5]);
                    if (uniqueQuestions.add(question.getQuestion())) {  // Add only if unique
                        questions.add(question);
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (questions.size() > 10) {
            Collections.shuffle(questions);
            return questions.subList(0, 10); // Get first 10 unique
        } else {
            return questions;
        }
    }
    public static class QuestionModel implements Serializable {
        private String question, option1, option2, option3, option4, correctAnswer;
        public QuestionModel(String question, String option1, String option2, String option3, String option4, String correctAnswer) {
            this.question = question;
            this.option1 = option1;
            this.option2 = option2;
            this.option3 = option3;
            this.option4 = option4;
            this.correctAnswer = correctAnswer;
        }
        public String getQuestion() { return question; }
        public String getOption1() { return option1; }
        public String getOption2() { return option2; }
        public String getOption3() { return option3; }
        public String getOption4() { return option4; }
        public String getCorrectAnswer() { return correctAnswer; }
    }
}
