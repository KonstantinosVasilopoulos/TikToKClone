package gr.aueb.tiktokclone.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import gr.aueb.tiktokclone.R;

public class DiscoverActivity extends AppCompatActivity {
    EditText mSearchBar;
    ImageView mSearchBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Find the search bar and the button
        mSearchBar = findViewById(R.id.searchBar);
        mSearchBtn = findViewById(R.id.searchBtn);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Add a listener to the search button for displaying relevant results
        mSearchBtn.setOnClickListener(v -> {
            // Get the topic from the search bar
            String topic = mSearchBar.getText().toString();

            // Make sure the user gave a topic
            if (topic.length() == 0) {
                Snackbar.make(findViewById(R.id.discoverView),
                        R.string.search_error,
                        Snackbar.LENGTH_SHORT).show();
                return;
            }

            // Redirect to an activity displaying the topic's videos
            Intent intent = new Intent(v.getContext(), ChannelActivity.class);
            intent.putExtra("CHANNEL_NAME", topic);
            startActivity(intent);
        });
    }
}