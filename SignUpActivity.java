package com.example.communication_app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText email, username, regno, dob, password, repass;
    private Button signup, adminLoginButton, adminAccessButton;
    private DatabaseReference databaseReference;
    private TextView signin;

    private final String ADMIN_EMAIL = "admin123@gmail.com";
    private final String ADMIN_PASSWORD = "Admin123";

    private boolean isAdminLoggedIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // Initialize views
        email = findViewById(R.id.emailEditText);
        username = findViewById(R.id.usernameEditText);
        regno = findViewById(R.id.regNumberEditText);
        dob = findViewById(R.id.dobEditText);
        password = findViewById(R.id.passwordEditText);
        repass = findViewById(R.id.retypePasswordEditText);
        signup = findViewById(R.id.signupButton);
        signin = findViewById(R.id.signinButton);
        adminLoginButton = findViewById(R.id.adminLoginButton);
        adminAccessButton = findViewById(R.id.adminAccessButton);

        adminAccessButton.setVisibility(View.GONE);

        signin.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
        });

        signup.setOnClickListener(v -> performUserSignUp());

        adminLoginButton.setOnClickListener(v -> checkAdminCredentials());

        adminAccessButton.setOnClickListener(v -> openFirebaseConsole());
    }

    private void performUserSignUp() {
        if (isAdminLoggedIn) {
            Toast.makeText(this, "Please logout from Admin mode to sign up as a user", Toast.LENGTH_SHORT).show();
            return;
        }

        String useremail = email.getText().toString().trim();
        String uname = username.getText().toString().trim();
        String reg = regno.getText().toString().trim();
        String dobirth = dob.getText().toString().trim();
        String pass = password.getText().toString().trim();
        String rePass = repass.getText().toString().trim();

        if (!validateInputs(useremail, uname, reg, dobirth, pass, rePass)) return;

        auth.createUserWithEmailAndPassword(useremail, pass).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String userId = auth.getCurrentUser().getUid();
                User user = new User(useremail, uname, reg, dobirth);

                databaseReference.child(userId).setValue(user).addOnCompleteListener(storeTask -> {
                    if (storeTask.isSuccessful()) {
                        Toast.makeText(SignUpActivity.this, "SignUp Successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                    } else {
                        Toast.makeText(SignUpActivity.this, "Failed to store user details", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(SignUpActivity.this, "SignUp Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateInputs(String email, String username, String regno, String dob, String pass, String rePass) {
        if (email.isEmpty() || username.isEmpty() || regno.isEmpty() || dob.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (pass.isEmpty() || pass.length() < 8) {
            Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!pass.equals(rePass)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void checkAdminCredentials() {
        String enteredEmail = email.getText().toString().trim();
        String enteredPassword = password.getText().toString().trim();

        if (enteredEmail.equals(ADMIN_EMAIL) && enteredPassword.equals(ADMIN_PASSWORD)) {
            Toast.makeText(this, "Admin Login Successful!", Toast.LENGTH_SHORT).show();
            isAdminLoggedIn = true;
            adminAccessButton.setVisibility(View.VISIBLE);
            clearUserFields();
        } else {
            Toast.makeText(this, "Invalid Admin Credentials", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearUserFields() {
        email.setText("");
        username.setText("");
        regno.setText("");
        dob.setText("");
        password.setText("");
        repass.setText("");
    }

    private void openFirebaseConsole() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://console.firebase.google.com/u/0/project/communication-app-4b841/overview")));
    }
}
