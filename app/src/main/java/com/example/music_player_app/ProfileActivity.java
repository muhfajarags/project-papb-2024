package com.example.music_player_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

public class ProfileActivity extends AppCompatActivity {

    // Harus sama dengan yang dipakai di LoginActivity & SplashActivity
    private static final String PREF_NAME = "my_app_prefs";
    private static final String KEY_EMAIL = "email";

    private ImageButton arrowBack;
    private TextView profileName;
    private Button editProfileBtn;
    private RecyclerView myRecyclerView;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Kalau nama layout-mu "profile.xml", maka:
        setContentView(R.layout.profile);
        // Kalau ternyata namanya "activity_profile.xml", ganti jadi:
        // setContentView(R.layout.activity_profile);

        initViews();
        bindUserData();
        setupActions();
    }

    private void initViews() {
        arrowBack = findViewById(R.id.arrow_back);
        profileName = findViewById(R.id.profileName);
        editProfileBtn = findViewById(R.id.editProfileBtn);
        myRecyclerView = findViewById(R.id.myRecyclerView);
        btnLogout = findViewById(R.id.btn_logout);
    }

    private void bindUserData() {
        // Ambil email yang disimpan saat login
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String email = prefs.getString(KEY_EMAIL, null);

        if (email != null) {
            // Contoh: pakai bagian sebelum '@' sebagai nama tampilan
            String displayName = email;
            int atIndex = email.indexOf("@");
            if (atIndex > 0) {
                displayName = email.substring(0, atIndex);
            }
            profileName.setText(displayName);
        } else {
            // fallback kalau belum ada di prefs
            profileName.setText("User");
        }

        // myRecyclerView:
        // Di sini kamu isi adapter + layoutManager kalau sudah punya data playlist
        // Contoh:
        // myRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // myRecyclerView.setAdapter(myPlaylistAdapter);
    }

    private void setupActions() {
        // Tombol back di kiri atas
        arrowBack.setOnClickListener(v -> onBackPressed());

        // Edit profile (sementara kasih Toast / nanti arahkan ke EditProfileActivity)
        editProfileBtn.setOnClickListener(v ->
                Toast.makeText(ProfileActivity.this,
                        "Edit Profile coming soon",
                        Toast.LENGTH_SHORT).show()
        );

        // Logout
        btnLogout.setOnClickListener(v -> logout());
    }

    private void logout() {
        // Hapus semua data session
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        // Arahkan ke LoginActivity dan clear back stack
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
