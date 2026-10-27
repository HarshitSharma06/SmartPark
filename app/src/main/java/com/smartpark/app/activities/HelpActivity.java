package com.smartpark.app.activities;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.smartpark.app.R;

public class HelpActivity extends AppCompatActivity {

    private WebView webView;
    private ProgressBar progressBar;
    private MaterialButton btnFaqStatic, btnRemoteSupport;

    private static final String STATIC_HTML_URL = "file:///android_asset/help.html";
    private static final String REMOTE_SUPPORT_URL = "https://support.google.com/maps";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        MaterialToolbar toolbar = findViewById(R.id.toolbar_help);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        webView = findViewById(R.id.web_view_help);
        progressBar = findViewById(R.id.progress_bar_web);
        btnFaqStatic = findViewById(R.id.btn_faq_static);
        btnRemoteSupport = findViewById(R.id.btn_remote_support);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(HelpActivity.this, "Failed to load page", Toast.LENGTH_SHORT).show();
            }
        });

        btnFaqStatic.setOnClickListener(v -> loadStaticFaq());
        btnRemoteSupport.setOnClickListener(v -> loadRemoteSupport());

        loadStaticFaq();
    }

    private void loadStaticFaq() {
        webView.loadUrl(STATIC_HTML_URL);
    }

    private void loadRemoteSupport() {
        webView.loadUrl(REMOTE_SUPPORT_URL);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
