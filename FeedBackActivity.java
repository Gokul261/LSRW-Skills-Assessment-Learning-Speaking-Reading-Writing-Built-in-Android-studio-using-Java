package com.example.communication_app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class FeedBackActivity extends AppCompatActivity {

    EditText editTextFeedback;
    Button btnSubmitFeedback;
    DatabaseReference feedbackRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_back);

        editTextFeedback = findViewById(R.id.editTextFeedback);
        btnSubmitFeedback = findViewById(R.id.btnSubmitFeedback);

        feedbackRef = FirebaseDatabase.getInstance().getReference("feedback");

        btnSubmitFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitFeedback();
            }
        });
    }

    private void submitFeedback() {
        String feedback = editTextFeedback.getText().toString().trim();

        if (TextUtils.isEmpty(feedback)) {
            Toast.makeText(this, "Please enter your feedback", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = user.getEmail();
        if (email == null) {
            Toast.makeText(this, "No email found for user", Toast.LENGTH_SHORT).show();
            return;
        }

        // Replace dots in email to make it safe for Firebase keys
        String safeEmailKey = email.replace(".", "_");

        // Get a unique key (e.g., timestamp) to add each feedback as a new entry
        String feedbackId = feedbackRef.child(safeEmailKey).push().getKey();

        if (feedbackId == null) {
            Toast.makeText(this, "Failed to generate feedback ID", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> feedbackData = new HashMap<>();
        feedbackData.put("feedback", feedback);

        feedbackRef.child(safeEmailKey).child(feedbackId).setValue(feedbackData)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Feedback submitted successfully!", Toast.LENGTH_SHORT).show();
                    editTextFeedback.setText("");

                    // Navigate to HomeActivity after 3 seconds
                    new Handler().postDelayed(() -> {
                        startActivity(new Intent(FeedBackActivity.this, HomeActivity.class));
                        finish();  // Optional: closes the feedback page so it doesn’t come back on back press
                    }, 3000); // 3 seconds delay
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to submit feedback", Toast.LENGTH_SHORT).show();
                });
    }
}
