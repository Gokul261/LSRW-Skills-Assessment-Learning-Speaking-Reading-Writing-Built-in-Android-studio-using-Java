
  package com.example.communication_app;

    public class User {
        public String email, username, regno, dob;

        // Default constructor required for calls to DataSnapshot.getValue(User.class)
        public User() {}

        public User(String email, String username, String regno, String dob) {
            this.email = email;
            this.username = username;
            this.regno = regno;
            this.dob = dob;
        }
    }

