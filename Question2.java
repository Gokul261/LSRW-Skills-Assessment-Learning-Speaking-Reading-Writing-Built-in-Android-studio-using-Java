package com.example.communication_app;

import java.util.Arrays;
import java.util.List;

public class Question2 {
    private String question, correctAnswer;
    private List<String> options;

    public Question2(String question, String option1, String option2, String option3, String option4, String correctAnswer) {
        this.question = question;
        this.options = Arrays.asList(option1, option2, option3, option4);
        this.correctAnswer = correctAnswer;
    }

    public String getQuestion() { return question; }
    public List<String> getOptions() { return options; }
    public String getCorrectAnswer() { return correctAnswer; }
}
