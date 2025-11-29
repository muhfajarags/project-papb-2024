package com.example.music_player_app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class RegisterActivity extends AppCompatActivity {

    // ================== KONFIGURASI SUPABASE ==================
    private static final String SUPABASE_URL = "https://iyijalcpynhabovobdmh.supabase.co";
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Iml5aWphbGNweW5oYWJvdm9iZG1oIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjQzODg3ODYsImV4cCI6MjA3OTk2NDc4Nn0.V8c-Yi77TFWRclaQ16nEQpZsW9i71HyCzImaaKKALIA";

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button btnRegister;
    private TextView tvLogin;

    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register); // pastikan nama layout = register.xml

        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        btnRegister = findViewById(R.id.btn_register);
        tvLogin = findViewById(R.id.tv_login);

        requestQueue = Volley.newRequestQueue(this);

        btnRegister.setOnClickListener(v -> attemptRegister());

        // pindah ke Login
        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void attemptRegister() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

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

        doSupabaseRegister(email, password);
    }

    private void doSupabaseRegister(final String email, final String password) {
        String url = SUPABASE_URL + "/auth/v1/signup";

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
                    // NOTE:
                    // kalau email confirmation ON, biasanya session null,
                    // tapi kalau OFF kadang langsung dapat session.
                    Toast.makeText(
                            RegisterActivity.this,
                            "Registrasi berhasil. Silakan cek email / login.",
                            Toast.LENGTH_LONG
                    ).show();

                    // kembali ke LoginActivity
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    // optionally: kirim email biar otomatis terisi
                    intent.putExtra("prefill_email", email);
                    startActivity(intent);
                    finish();
                },
                error -> {
                    String msg = "Registrasi gagal, coba lagi.";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        msg = new String(error.networkResponse.data);
                    }

                    Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_LONG).show();
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
}
