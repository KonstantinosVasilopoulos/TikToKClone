package gr.aueb.tiktokclone.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import gr.aueb.tiktokclone.R;
import gr.aueb.tiktokclone.domain.AppNode;
import gr.aueb.tiktokclone.domain.Consumer;

public class FollowingActivity extends AppCompatActivity {
    TextView mFollowingBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_following);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        mFollowingBtn = findViewById(R.id.followingBtn);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Populate the channels' layout with the subscribed topics
        assert AppNode.getInstance() != null;
        Consumer consumer = AppNode.getInstance().getConsumer();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment fragment;
        for (String topic : consumer.getSubscribedTopics()) {
            fragment = new ChannelFragment();
            Bundle args = new Bundle();
            args.putString("CHANNEL_NAME", topic);
            fragment.setArguments(args);
            ft.add(R.id.channelsLayout, fragment);
        }

        ft.commit();

        // Add a listener for redirecting to the view containing the subscribed topics
        mFollowingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), FollowingActivity.class);
                startActivity(intent);
            }
        });
    }
}