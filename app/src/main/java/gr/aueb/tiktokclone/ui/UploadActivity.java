package gr.aueb.tiktokclone.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;

import gr.aueb.brokerlibrary.VideoInfo;
import gr.aueb.tiktokclone.R;
import gr.aueb.tiktokclone.domain.AppNode;
import gr.aueb.tiktokclone.domain.Publisher;
import gr.aueb.tiktokclone.domain.UploadTask;

public class UploadActivity extends AppCompatActivity {
    ImageView mBackBtn;
    EditText mVideoNameField;
    EditText mHashtagsField;
    Button mUploadBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        mBackBtn = findViewById(R.id.backBtn);
        mVideoNameField = findViewById(R.id.videoNameField);
        mHashtagsField = findViewById(R.id.hashtagsField);
        mUploadBtn = findViewById(R.id.videoUploadBtn);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Create a fragment for each available file
        assert AppNode.getInstance() != null;
        Publisher publisher = AppNode.getInstance().getPublisher();
        File folder = new File(publisher.getVideosDir());
        File[] listOfFiles = folder.listFiles();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment fragment;
        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (file.getName().endsWith(".mp4")) {
                    fragment = new ChooseVideoFragment();
                    Bundle args = new Bundle();
                    args.putString("VIDEO_PATH", file.getAbsolutePath());
                    fragment.setArguments(args);
                    ft.add(R.id.availableVideosLayout, fragment);
                }
            }

            ft.commit();
        }

        // Add a listener to the button redirecting to the main page
        mBackBtn.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), MainActivity.class);
            startActivity(intent);
        });

        // Add a listener to button responsible for uploading
        mUploadBtn.setOnClickListener(v -> {
            // Get the name of the video and check that a value was given
            String name = mVideoNameField.getText().toString();
            if (name.equals("")) {
                Snackbar.make(findViewById(R.id.UploadView),
                        R.string.name_error,
                        Snackbar.LENGTH_SHORT).show();
                return;
            }

            // Get the hashtags for the video
            String[] hashtags = splitGivenHashtags();

            // Get the chosen video
            ChooseVideoFragment chosenVideo = null;
            for (Fragment videoFragment : getSupportFragmentManager().getFragments()) {
                if (videoFragment.isVisible() && videoFragment instanceof ChooseVideoFragment) {
                    if (((ChooseVideoFragment) videoFragment).isChosen()) {
                        chosenVideo = (ChooseVideoFragment) videoFragment;
                        break;
                    }
                }
            }

            // Ensure that a video was chosen
            if (chosenVideo == null) {
                Snackbar.make(findViewById(R.id.UploadView),
                        R.string.no_video_chosen_error,
                        Snackbar.LENGTH_SHORT).show();
                return;
            }

            // Create a new video and upload it
            VideoInfo video = new VideoInfo(name, chosenVideo.getVideoPath(), publisher.getChannelName().getChannelName());
            for (String topic : hashtags)
                video.addAssociatedHashtag(topic);
            new UploadTask(video).execute();

            // Go back to the main activity
            Intent intent = new Intent(v.getContext(), MainActivity.class);
            startActivity(intent);
        });
    }

    private String[] splitGivenHashtags() {
        String hashtags = mHashtagsField.getText().toString();
        return hashtags.split("(\\b[^\\s]+\\b)");
    }
}