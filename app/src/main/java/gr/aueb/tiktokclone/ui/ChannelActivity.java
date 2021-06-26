package gr.aueb.tiktokclone.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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

    TextView mChannelNameLabel;
    ImageView mBackBtn;
    Button mSubscribeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        mDisplayedVideos = new ArrayList<>();

        // Get the name from the intent and download the videos
        mName = getIntent().getStringExtra("CHANNEL_NAME");
        displayNewVideos();
        assert AppNode.getInstance() != null;
        Consumer consumer = AppNode.getInstance().getConsumer();
        new DownloadTask(this).execute(mName);

        // Use a popup to let the user know that videos are being fetched
        Snackbar.make(findViewById(R.id.channelView),
                R.string.video_fetching_popup,
                Snackbar.LENGTH_LONG).show();

        // Change channel's name label
        mChannelNameLabel = findViewById(R.id.channelNameLabel);
        mChannelNameLabel.setText(mName);

        // Find the buttons
        mBackBtn = findViewById(R.id.backBtn);
        mSubscribeBtn = findViewById(R.id.subscribeBtn);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Set the subscribe button's text accordingly
        assert AppNode.getInstance() != null;
        Consumer consumer = AppNode.getInstance().getConsumer();
        if (consumer.getSubscribedTopics().contains(mName))
            mSubscribeBtn.setText(R.string.unsubscribe_btn);

        // Add a listener to the button redirecting to the previous view
        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        // Add a listener to the button handling subscription
        mSubscribeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                assert AppNode.getInstance() != null;
                Consumer consumer = AppNode.getInstance().getConsumer();
                if (consumer.getSubscribedTopics().contains(mName)) {
                    // Unsubscribe and change the button's text
                    consumer.unsubscribe(mName);
                    mSubscribeBtn.setText(R.string.subscribe_btn);
                } else {
                    // Subscribe and change the button's text
                    consumer.subscribe(mName);
                    mSubscribeBtn.setText(R.string.unsubscribe_btn);
                }
            }
        });
    }

    public void displayNewVideos() {
        // Create a video fragment for each new video
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment fragment;
        assert AppNode.getInstance() != null;
        Consumer consumer = AppNode.getInstance().getConsumer();
        for (VideoInfo v : consumer.getDownloadedVideos().keySet()) {
            if (mName.equals(v.getChannelName()) && !checkVideoDisplayed(v)) {
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

    private boolean checkVideoDisplayed(VideoInfo video) {
        for (VideoInfo v : mDisplayedVideos) {
            if (video.getFilename().equals(v.getFilename()))
                return true;
        }

        return false;
    }
}