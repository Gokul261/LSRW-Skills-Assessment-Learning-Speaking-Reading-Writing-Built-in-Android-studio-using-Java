package com.example.communication_app;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import androidx.appcompat.app.AppCompatActivity;

public class AboutUsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WebView webView = new WebView(this);  // Create WebView
        setContentView(webView);  // Use WebView directly as content view

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);  // Enable JS if needed

        webView.loadUrl("file:///android_asset/about_us_summary.html");  // Load your HTML file
    }
}
