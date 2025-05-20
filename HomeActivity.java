package com.example.communication_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeActivity extends AppCompatActivity {

    private TextView tvUsername;
    private DatabaseReference userRef;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        auth = FirebaseAuth.getInstance();
        tvUsername = findViewById(R.id.tvUsername);

        fetchAndDisplayUsername();

        ImageButton btnDailyTask = findViewById(R.id.btnDailyTask);
        btnDailyTask.setOnClickListener(v -> navigateTo(DailyTaskActivity.class));

        ImageButton btnListening = findViewById(R.id.btnListening);
        btnListening.setOnClickListener(v -> navigateTo(ListeningActivity.class));

        ImageButton btnSpeaking = findViewById(R.id.btnSpeaking);
        btnSpeaking.setOnClickListener(v -> navigateTo(SpeakingActivity.class));

        ImageButton btnReading = findViewById(R.id.btnReading);
        btnReading.setOnClickListener(v -> navigateTo(ReadingActivity.class));

        ImageButton btnWriting = findViewById(R.id.btnWriting);
        btnWriting.setOnClickListener(v -> navigateTo(WritingActivity.class));

        ImageButton btnVocabulary = findViewById(R.id.btnVocabulary);
        btnVocabulary.setOnClickListener(v -> navigateTo(VocabularyActivity.class));

        ImageButton btnGrammar = findViewById(R.id.btnGrammar);
        btnGrammar.setOnClickListener(v -> navigateTo(GrammarActivity.class));

        ImageButton btnVideoLink = findViewById(R.id.btnVideoLink);
        btnVideoLink.setOnClickListener(v -> navigateTo(VideoLinkActivity.class));

        ImageButton btnFeedBack = findViewById(R.id.btnFeedBack);
        btnFeedBack.setOnClickListener(v -> navigateTo(FeedBackActivity.class));

        ImageButton btnAboutUs = findViewById(R.id.btnAboutUs);
        btnAboutUs.setOnClickListener(v -> navigateTo(AboutUsActivity.class));

        ImageButton btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> logout());
    }

    private void fetchAndDisplayUsername() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String username = snapshot.child("username").getValue(String.class);
                        tvUsername.setText("Welcome, " + username + "\uD83E\uDD29✌\uFE0F");
                    } else {
                        tvUsername.setText("Welcome!");
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(HomeActivity.this, "Failed to load username", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void navigateTo(Class<?> targetActivity) {
        Intent intent = new Intent(HomeActivity.this, targetActivity);
        startActivity(intent);
    }

    private void logout() {
        auth.signOut();  // Clear current user
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
