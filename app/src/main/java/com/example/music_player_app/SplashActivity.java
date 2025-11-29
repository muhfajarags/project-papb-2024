package com.example.music_player_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final String PREF_NAME = "celloo_prefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
            boolean isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false);

            Intent intent;
            if (isLoggedIn) {
                // Sudah login → langsung ke MainActivity
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                // Belum login → ke LoginActivity
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }

            startActivity(intent);
            finish();
        }, 1000); // 1 detik splash
    }
}
