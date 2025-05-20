package com.example.communication_app;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultWritingActivity extends AppCompatActivity {

    private TextView resultText;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_writing);

        resultText = findViewById(R.id.result_text);

        // Fetch Data from Intent
        List<String> mistakesList = getIntent().getStringArrayListExtra("mistakesList");
        String paragraph = getIntent().getStringExtra("paragraph");
        String email = getIntent().getStringExtra("email");

        if (mistakesList == null) {
            mistakesList = new ArrayList<>();
        }

        // Display the result
        displayResults(mistakesList, paragraph);

        // Save to Firebase
        saveToFirebase(email, mistakesList.size(), mistakesList);
    }

    private void displayResults(List<String> mistakesList, String paragraph) {
        StringBuilder result = new StringBuilder();
        result.append("Total Mistakes: ").append(mistakesList.size()).append("\n\n");

        if (mistakesList.isEmpty()) {
            result.append("Perfect! No mistakes found.\n");
        } else {
            for (String mistake : mistakesList) {
                result.append(mistake).append("\n");
            }
        }

        result.append("\nOriginal Paragraph:\n").append(paragraph);
        resultText.setText(result.toString());
    }

    private void saveToFirebase(String email, int mistakeCount, List<String> mistakesList) {
        databaseRef = FirebaseDatabase.getInstance().getReference("ReadingResults");

        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "Saved to Firebase.", Toast.LENGTH_LONG).show();
            return;
        }

        // Email cleanup for Firebase key compatibility
        String formattedEmail = email.replace(".", "_").replace("@", "_");

        Map<String, Object> data = new HashMap<>();
        data.put("MistakeCount", mistakeCount);
        data.put("Mistakes", mistakesList);

        databaseRef.child(formattedEmail).setValue(data)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Saved to Firebase!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to save: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}
