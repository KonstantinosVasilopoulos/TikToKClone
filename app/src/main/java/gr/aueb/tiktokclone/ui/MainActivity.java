package gr.aueb.tiktokclone.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.os.StrictMode;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import gr.aueb.brokerlibrary.ChannelName;
import gr.aueb.tiktokclone.R;
import gr.aueb.tiktokclone.domain.AppNode;
import gr.aueb.tiktokclone.domain.Consumer;

public class MainActivity extends AppCompatActivity {

    LinearLayout mVideosLayout;
    FragmentManager fm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        mVideosLayout = findViewById(R.id.channelsLayout);
        fm = getSupportFragmentManager();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Get the recommended channels
        List<ChannelName> recommendedChannels;
        assert AppNode.getInstance() != null;
        Consumer consumer = AppNode.getInstance().getConsumer();
        recommendedChannels = consumer.requestRecommendedChannels();

        // Create a channel fragment for each recommended channel
        List<String> displayedChannels = new ArrayList<>();
        for (ChannelName channel : recommendedChannels) {
            // Do not display duplicates
            if (displayedChannels.contains(channel.getChannelName()))
                continue;

            displayedChannels.add(channel.getChannelName());

            // Create the new fragment
            FragmentTransaction ft = fm.beginTransaction();
            Fragment fragment = new ChannelFragment();
            Bundle args = new Bundle();
            args.putString("CHANNEL_NAME", channel.getChannelName());
            fragment.setArguments(args);
            ft.add(R.id.channelsLayout, fragment);
            ft.commit();
        }
    }
}