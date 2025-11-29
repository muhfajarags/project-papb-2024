package com.example.music_player_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    // ================== KONFIGURASI SUPABASE ==================
    // contoh: https://abcde.supabase.co  (tanpa slash di belakang)
    private static final String SUPABASE_URL = "https://iyijalcpynhabovobdmh.supabase.co";
    // anon public key dari Supabase (Project Settings → API → anon public)
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Iml5aWphbGNweW5oYWJvdm9iZG1oIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjQzODg3ODYsImV4cCI6MjA3OTk2NDc4Nn0.V8c-Yi77TFWRclaQ16nEQpZsW9i71HyCzImaaKKALIA";

    // ================== KONFIGURASI SESSION ==================
    private static final String PREF_NAME = "celloo_prefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private ProgressBar progressBar;   // opsional, kalau kamu punya di XML
    private TextView tvRegister;

    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login); // pastikan sesuai dengan nama layout kamu

        emailEditText = findViewById(R.id.email_edit_text);       // sesuaikan dengan id di xml
        passwordEditText = findViewById(R.id.password_edit_text); // sesuaikan dengan id di xml
        loginButton = findViewById(R.id.btn_login);               // sesuaikan dengan id di xml
        tvRegister = findViewById(R.id.tv_register);              // "Don't have an account? Register"

        requestQueue = Volley.newRequestQueue(this);

        // Tombol login
        loginButton.setOnClickListener(v -> attemptLogin());

        // Teks "Register" → pindah ke RegisterActivity
        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // (Opsional) Auto-isi email kalau baru selesai register
        String prefillEmail = getIntent().getStringExtra("prefill_email");
        if (prefillEmail != null) {
            emailEditText.setText(prefillEmail);
        }
    }

    private void attemptLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Validasi sederhana
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email wajib diisi");
            emailEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password wajib diisi");
            passwordEditText.requestFocus();
            return;
        }

        // Panggil Supabase
        doSupabaseLogin(email, password);
    }

    private void setLoading(boolean loading) {
        loginButton.setEnabled(!loading);
        if (progressBar != null) {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }

    private void doSupabaseLogin(final String email, final String password) {
        setLoading(true);

        // Endpoint login email/password Supabase
        String url = SUPABASE_URL + "/auth/v1/token?grant_type=password";

        JSONObject body = new JSONObject();
        try {
            body.put("email", email);
            body.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                response -> {
                    setLoading(false);
                    try {
                        // Ambil token dari Supabase
                        String accessToken = response.getString("access_token");
                        String refreshToken = response.optString("refresh_token", null);

                        // Simpan session di SharedPreferences
                        saveLoginSession(email, accessToken, refreshToken);

                        // Pindah ke MainActivity (atau ProfileActivity kalau mau)
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        // Biar tombol back nggak balik ke login lagi
                        finish();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(LoginActivity.this,
                                "Gagal membaca respon server",
                                Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    setLoading(false);

                    String msg = "Login gagal, cek kembali email/password";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        // Kalau mau, bisa parsing pesan error dari Supabase
                        msg = new String(error.networkResponse.data);
                    }

                    Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", SUPABASE_ANON_KEY);
                headers.put("Content-Type", "application/json");
                headers.put("Accept", "application/json");
                return headers;
            }
        };

        requestQueue.add(request);
    }

    private void saveLoginSession(String email, String accessToken, String refreshToken) {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_ACCESS_TOKEN, accessToken);
        if (refreshToken != null) {
            editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        }
        editor.apply();
    }
}
