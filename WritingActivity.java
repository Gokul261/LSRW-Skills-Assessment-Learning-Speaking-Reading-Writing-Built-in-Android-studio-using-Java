package com.example.communication_app;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputType;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class WritingActivity extends AppCompatActivity {

    private TextView paragraphText, timerText;
    private EditText userInput;
    private Button skipButton, submitButton;
    private List<String> paragraphs = new ArrayList<>();
    private Set<Integer> usedIndices = new HashSet<>();
    private int currentIndex = -1;  // Will be set dynamically

    private CountDownTimer timer;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_writing);

        email = getIntent().getStringExtra("email");

        initViews();
        loadParagraphsFromCsv();
        selectNewParagraph();
        showParagraphWithTimer();
    }

    private void initViews() {
        paragraphText = findViewById(R.id.paragraph_text);
        timerText = findViewById(R.id.timer_text);
        userInput = findViewById(R.id.user_input);
        skipButton = findViewById(R.id.skip_button);
        submitButton = findViewById(R.id.submit_button);

        // Disable auto-correction and suggestions in EditText
        userInput.setInputType(userInput.getInputType() | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        skipButton.setOnClickListener(v -> showWritingSection());
        submitButton.setOnClickListener(v -> evaluateAndNavigate());
    }

    private void loadParagraphsFromCsv() {
        try (InputStream is = getAssets().open("English_Paragraphs_3Lines.csv");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            String line;
            while ((line = reader.readLine()) != null) {
                paragraphs.add(line.trim());
            }

            if (paragraphs.isEmpty()) {
                Toast.makeText(this, "No paragraphs found in file!", Toast.LENGTH_SHORT).show();
                finish();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Failed to load paragraphs", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void selectNewParagraph() {
        if (usedIndices.size() == paragraphs.size()) {
            usedIndices.clear(); // Reset if all questions are used
        }

        Random random = new Random();
        do {
            currentIndex = random.nextInt(paragraphs.size());
        } while (usedIndices.contains(currentIndex));

        usedIndices.add(currentIndex);
    }

    private void showParagraphWithTimer() {
        if (currentIndex == -1 || currentIndex >= paragraphs.size()) {
            finish();
            return;
        }

        paragraphText.setText(paragraphs.get(currentIndex));

        // Show reading mode UI
        paragraphText.setVisibility(View.VISIBLE);
        userInput.setVisibility(View.GONE);
        skipButton.setVisibility(View.VISIBLE);
        submitButton.setVisibility(View.GONE);
        timerText.setVisibility(View.VISIBLE);

        timer = new CountDownTimer(2 * 60 * 1000, 1000) {
            public void onTick(long millisUntilFinished) {
                timerText.setText("Time Left: " + millisUntilFinished / 1000 + " sec");
            }

            public void onFinish() {
                showWritingSection();
            }
        }.start();
    }

    private void showWritingSection() {
        if (timer != null) timer.cancel();

        paragraphText.setVisibility(View.GONE);
        skipButton.setVisibility(View.GONE);
        timerText.setVisibility(View.GONE);

        userInput.setVisibility(View.VISIBLE);
        submitButton.setVisibility(View.VISIBLE);
    }

    private void evaluateAndNavigate() {
        String userText = userInput.getText().toString().trim();
        String originalText = paragraphs.get(currentIndex);

        List<String> mistakesList = calculateMistakes(originalText, userText);

        Intent intent = new Intent(this, ResultWritingActivity.class);
        intent.putStringArrayListExtra("mistakesList", new ArrayList<>(mistakesList));
        intent.putExtra("paragraph", originalText);
        intent.putExtra("email", email);

        startActivity(intent);
        finish();
    }

    private List<String> calculateMistakes(String original, String userText) {
        String[] originalWords = original.split("\\s+");
        String[] userWords = userText.split("\\s+");

        List<String> mistakes = new ArrayList<>();

        int len = Math.min(originalWords.length, userWords.length);

        for (int i = 0; i < len; i++) {
            if (!originalWords[i].equalsIgnoreCase(userWords[i])) {
                mistakes.add("Expected: " + originalWords[i] + ", Got: " + userWords[i]);
            }
        }

        // Handle extra/missing words
        if (originalWords.length > userWords.length) {
            for (int i = userWords.length; i < originalWords.length; i++) {
                mistakes.add("Missing: " + originalWords[i]);
            }
        } else if (userWords.length > originalWords.length) {
            for (int i = originalWords.length; i < userWords.length; i++) {
                mistakes.add("Extra: " + userWords[i]);
            }
        }

        return mistakes;
    }
}
