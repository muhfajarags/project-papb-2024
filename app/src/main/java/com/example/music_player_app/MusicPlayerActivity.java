package com.example.music_player_app;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;

import java.io.IOException;

public class MusicPlayerActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private ImageButton playPauseButton;
    private boolean isPlaying = false;
    private static final String TAG = "MusicPlayerApp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_player);

        // Retrieve data from Intent
        Intent intent = getIntent();
        String songTitle = intent.getStringExtra("songTitle");
        String artistName = intent.getStringExtra("artistName");
        String songUrl = intent.getStringExtra("songUrl");
        String coverImageUrl = intent.getStringExtra("coverImageUrl");
        int songId = intent.getIntExtra("songId", 1); // Replace with the actual song ID

        // Additional data for song information
        String writtenBy = intent.getStringExtra("writtenBy");  // Added line to retrieve the "written by" info
        String producedBy = intent.getStringExtra("producedBy"); // Added line to retrieve the "produced by" info

        // Initialize UI elements
        TextView songTitleView = findViewById(R.id.song_title);
        TextView artistNameView = findViewById(R.id.song_artist);
        ImageView coverImageView = findViewById(R.id.song_cover);
        playPauseButton = findViewById(R.id.pause_music_player);
        ImageButton infoButton = findViewById(R.id.info);

        // Set data to UI elements
        songTitleView.setText(songTitle);
        artistNameView.setText(artistName);
        Glide.with(this).load(coverImageUrl).into(coverImageView);

        mediaPlayer = new MediaPlayer();

        // Play/Pause button
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying) {
                    pauseMusic();
                } else {
                    playMusic(songUrl);
                }
                Log.d(TAG, "Play/Pause button clicked. isPlaying: " + isPlaying);
            }
        });

        // Back button
        ImageButton backButton = findViewById(R.id.arrow_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Info button - Transition to SongInfoFragment
        infoButton.setOnClickListener(v -> {
            // Create instance of SongInfoFragment and pass songId
            SongInfoFragment songInfoFragment = SongInfoFragment.newInstance(songId);

            // Replace current view with the fragment
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, songInfoFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();

            Log.d(TAG, "Navigating to SongInfoFragment with songId: " + songId);
        });
    }

    private void playMusic(String audioUrl) {
        try {
            if (!isPlaying) {
                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(audioUrl);
                    mediaPlayer.prepare();
                }
                mediaPlayer.start();
                playPauseButton.setImageResource(R.drawable.img_pause);
                isPlaying = true;
                Log.d(TAG, "Music started playing");

                mediaPlayer.setOnCompletionListener(mp -> {
                    isPlaying = false;
                    playPauseButton.setImageResource(R.drawable.img_play);
                });
            }
        } catch (IOException e) {
            Log.e(TAG, "Error playing music", e);
            Toast.makeText(this, "Error playing music: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Invalid audio URL", e);
            Toast.makeText(this, "Invalid audio URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error", e);
            Toast.makeText(this, "Unexpected error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void pauseMusic() {
        if (isPlaying && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            playPauseButton.setImageResource(R.drawable.img_play);
            isPlaying = false;
            Log.d(TAG, "Music paused");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            Log.d(TAG, "MediaPlayer released");
        }
    }
}
