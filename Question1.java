package com.example.communication_app;

import android.os.Parcel;
import android.os.Parcelable;

public class Question1 implements Parcelable {
    private String paragraph, question, option1, option2, option3, option4, correctOption;

    public Question1(String paragraph, String question, String option1, String option2, String option3, String option4, String correctOption) {
        this.paragraph = paragraph;
        this.question = question;
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
        this.option4 = option4;
        this.correctOption = correctOption;
    }

    public String getParagraph() { return paragraph; }
    public String getQuestion() { return question; }
    public String getOption1() { return option1; }
    public String getOption2() { return option2; }
    public String getOption3() { return option3; }
    public String getOption4() { return option4; }
    public String getCorrectOption() { return correctOption; }

    // Parcelable part to pass Question list if needed
    protected Question1(Parcel in) {
        paragraph = in.readString();
        question = in.readString();
        option1 = in.readString();
        option2 = in.readString();
        option3 = in.readString();
        option4 = in.readString();
        correctOption = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(paragraph);
        dest.writeString(question);
        dest.writeString(option1);
        dest.writeString(option2);
        dest.writeString(option3);
        dest.writeString(option4);
        dest.writeString(correctOption);
    }

    public static final Creator<Question> CREATOR = new Creator<Question>() {
        @Override
        public Question createFromParcel(Parcel in) {
            return new Question(in);
        }

        @Override
        public Question[] newArray(int size) {
            return new Question[size];
        }
    };

    @Override
    public int describeContents() { return 0; }
}
