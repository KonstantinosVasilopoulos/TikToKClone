package gr.aueb.tiktokclone.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.os.StrictMode;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import gr.aueb.brokerlibrary.VideoInfo;
import gr.aueb.tiktokclone.R;
import gr.aueb.tiktokclone.domain.AppNode;
import gr.aueb.tiktokclone.domain.Consumer;
import gr.aueb.tiktokclone.domain.DownloadTask;

public class ChannelActivity extends AppCompatActivity {
    String mName;
    List<VideoInfo> mDisplayedVideos;
    Consumer consumer;

    TextView mChannelNameLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        mDisplayedVideos = new ArrayList<>();

        // Get the name from the intent and download the videos
        mName = getIntent().getStringExtra("CHANNEL_NAME");
        assert AppNode.getInstance() != null;
        consumer = AppNode.getInstance().getConsumer();
        new DownloadTask(this).execute(mName);

        // Use a popup to let the user know that videos are being fetched
        Snackbar.make(findViewById(R.id.channelView),
                R.string.video_fetching_popup,
                Snackbar.LENGTH_LONG).show();

        // Change channel's name label
        mChannelNameLabel = findViewById(R.id.channelNameLabel);
        mChannelNameLabel.setText(mName);
    }

    public void displayNewVideos() {
        // Create a video fragment for each new video
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment fragment;
        for (VideoInfo v : consumer.getDownloadedVideos().keySet()) {
            if (mName.equals(v.getChannelName()) && !mDisplayedVideos.contains(v)) {
                fragment = new VideoFragment();
                Bundle args = new Bundle();
                args.putString("VIDEO_FILENAME", v.getFilename());
                fragment.setArguments(args);
                ft.add(R.id.videosLayout, fragment);
                mDisplayedVideos.add(v);
            }
        }

        ft.commit();
    }
}