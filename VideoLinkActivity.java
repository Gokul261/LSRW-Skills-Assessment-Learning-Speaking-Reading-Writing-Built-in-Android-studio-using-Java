package com.example.communication_app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class VideoLinkActivity extends AppCompatActivity {

    private WebView videoWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_link);

        videoWebView = findViewById(R.id.videoWebView);
        setupWebView();

        // Back to Home Button
        findViewById(R.id.btnBackToHome).setOnClickListener(v -> {
            startActivity(new Intent(VideoLinkActivity.this, HomeActivity.class));
            finish();
        });

        // Set up all video and tutorial buttons
        setupVideoButton(R.id.btnListeningVideo, "https://www.youtube.com/embed/HQJ0HoflcUA");
        setupTutorialButton(R.id.btnListeningTutorial, "https://www.esl-lounge.com/student/listening.php");

        setupVideoButton(R.id.btnSpeakingVideo, "https://www.youtube.com/embed/X60DPGODWC4");
        setupTutorialButton(R.id.btnSpeakingTutorial, "https://speechify.in/study-materials");

        setupVideoButton(R.id.btnReadingVideo, "https://www.youtube.com/embed/7A4L1Ul7q2M");
        setupTutorialButton(R.id.btnReadingTutorial, "https://www.tutorialsduniya.com/");

        setupVideoButton(R.id.btnWritingVideo, "https://www.youtube.com/embed/EWcsv-nPKBg");
        setupTutorialButton(R.id.btnWritingTutorial, "https://www.uagc.edu/blog/how-improve-college-writing-skills-6-resources-students");

        setupVideoButton(R.id.btnVocabularyVideo, "https://www.youtube.com/embed/RjTY5VMcs4s");
        setupTutorialButton(R.id.btnVocabularyTutorial, "https://takeyoursuccess.com/college-vocabulary-words/");

        setupVideoButton(R.id.btnGrammarVideo, "https://www.youtube.com/embed/-qpm7RtJLbQ");
        setupTutorialButton(R.id.btnGrammarTutorial, "https://edu.gcfglobal.org/en/grammar/");
    }

    private void setupWebView() {
        WebSettings webSettings = videoWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        videoWebView.setWebViewClient(new WebViewClient());
    }

    private void setupVideoButton(int buttonId, String videoUrl) {
        Button button = findViewById(buttonId);
        button.setOnClickListener(v -> {
            videoWebView.setVisibility(View.VISIBLE);
            loadYouTubeVideo(videoUrl);
        });
    }

    private void setupTutorialButton(int buttonId, String tutorialUrl) {
        Button button = findViewById(buttonId);
        button.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(tutorialUrl));
            startActivity(intent);
        });
    }

    private void loadYouTubeVideo(String embedUrl) {
        String html = "<html><body style=\"margin:0;padding:0;\">" +
                "<iframe width=\"100%\" height=\"100%\" " +
                "src=\"" + embedUrl + "\" " +
                "frameborder=\"0\" allowfullscreen></iframe></body></html>";
        videoWebView.loadData(html, "text/html", "utf-8");
    }
}
