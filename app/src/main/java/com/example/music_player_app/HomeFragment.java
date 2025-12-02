package com.example.music_player_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment implements OnSongClickListener {

    private static final String TAG = "HomeFragment";

    // GANTI dengan URL & anon key Supabase kamu
    private static final String SUPABASE_URL = "https://iyijalcpynhabovobdmh.supabase.co";
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Iml5aWphbGNweW5oYWJvdm9iZG1oIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjQzODg3ODYsImV4cCI6MjA3OTk2NDc4Nn0.V8c-Yi77TFWRclaQ16nEQpZsW9i71HyCzImaaKKALIA";

    // endpoint REST untuk tabel "songs"
    // asumsi kolom: title, artist, coverUrl, songUrl
    private static final String SONGS_ENDPOINT = "/rest/v1/today_hits?select=*";

    private RecyclerView recyclerView;
    private SongAdapter adapter;
    private RequestQueue requestQueue;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
        );

        adapter = new SongAdapter(new ArrayList<>(), requireContext(), this);
        recyclerView.setAdapter(adapter);

        // inisialisasi Volley
        requestQueue = Volley.newRequestQueue(requireContext());

        // load data dari Supabase
        loadSongsFromSupabase();

        // use case profile (biarkan seperti semula)
        ImageView profileButton = view.findViewById(R.id.ic_profile);
        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ProfileActivity.class);
            startActivity(intent);
        });

        return view;
    }

    /**
     * Mengambil list lagu dari Supabase REST API (tabel songs)
     */
    private void loadSongsFromSupabase() {
        String url = SUPABASE_URL + SONGS_ENDPOINT;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    List<Song> songs = new ArrayList<>();

                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);

                            String title = obj.optString("title", "");
                            String artist = obj.optString("artist", "");
                            String coverUrl = obj.optString("coverUrl", "");
                            String songUrl = obj.optString("songUrl", "");

                            Song song = new Song(title, artist, coverUrl, songUrl);
                            songs.add(song);
                            Log.d(TAG, "Song loaded from Supabase: " + title);
                        }

                        adapter.updateSongs(songs);

                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parse error: " + e.getMessage(), e);
                        Toast.makeText(getContext(),
                                "Gagal parsing data lagu", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e(TAG, "Supabase request error: " + error.toString());
                    Toast.makeText(getContext(),
                            "Gagal memuat lagu dari Supabase", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", SUPABASE_ANON_KEY);
                headers.put("Authorization", "Bearer " + SUPABASE_ANON_KEY);
                headers.put("Accept", "application/json");
                return headers;
            }
        };

        requestQueue.add(request);
    }

    /**
     * Dipanggil ketika item lagu di-klik di RecyclerView
     */
    @Override
    public void onSongClick(Song song) {
        Intent intent = new Intent(getActivity(), MusicPlayerActivity.class);
        intent.putExtra("songTitle", song.getTitle());
        intent.putExtra("artistName", song.getArtist());
        intent.putExtra("songUrl", song.getSongUrl());
        intent.putExtra("coverUrl", song.getCoverUrl());
        startActivity(intent);
    }

    @Override
    public void onDestroyView() { // untuk menghindari memory leak
        super.onDestroyView();
        recyclerView.setAdapter(null);
        adapter = null;
        requestQueue = null;
    }
}
