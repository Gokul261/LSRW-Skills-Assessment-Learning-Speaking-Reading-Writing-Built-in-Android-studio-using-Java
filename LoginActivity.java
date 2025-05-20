package com.example.communication_app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText loginEmail, loginPassword;
    private Button loginButton, adminLoginButton, adminAccessButton;
    private TextView signUpRedirectText, forgotPassword;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Hooks
        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
        signUpRedirectText = findViewById(R.id.signUpRedirectText);
        forgotPassword = findViewById(R.id.forgot_password);
        adminLoginButton = findViewById(R.id.adminLoginButton);
        adminAccessButton = findViewById(R.id.adminAccessButton);

        // Normal Login Button
        loginButton.setOnClickListener(v -> loginUser());

        // Sign Up Redirect
        signUpRedirectText.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        // Forgot Password Redirect
        forgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        // Admin Login Logic
        adminLoginButton.setOnClickListener(v -> adminLogin());

        // Open Firebase Console if admin logs in successfully
        adminAccessButton.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://console.firebase.google.com/u/0/project/communication-app-4b841/overview"));
            startActivity(browserIntent);
        });
    }

    private void loginUser() {
        String email = loginEmail.getText().toString().trim();
        String password = loginPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            loginEmail.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            loginPassword.setError("Password is required");
            return;
        }

        if (password.length() < 6) {
            loginPassword.setError("Password must be at least 6 characters");
            return;
        }

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = auth.getCurrentUser();
                if (user != null) {
                    Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Please verify your email!", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(LoginActivity.this, "Login Failed! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void adminLogin() {
        String email = loginEmail.getText().toString().trim();
        String password = loginPassword.getText().toString().trim();

        if (email.equals("admin123@gmail.com") && password.equals("Admin123")) {
            Toast.makeText(this, "Admin login successful", Toast.LENGTH_SHORT).show();
            adminAccessButton.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, "Invalid admin credentials", Toast.LENGTH_SHORT).show();
        }
    }
}
