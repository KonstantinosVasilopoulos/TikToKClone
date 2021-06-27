package gr.aueb.tiktokclone.ui;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.VideoView;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import gr.aueb.tiktokclone.R;

public class ChooseVideoFragment extends Fragment {
    private String videoPath;
    private VideoView videoView;
    private boolean chosen;

    private static final String ARG_PARAM1 = "VIDEO_PATH";

    public ChooseVideoFragment() {
        // Required empty public constructor
    }

    public static ChooseVideoFragment newInstance(String videoPath) {
        ChooseVideoFragment fragment = new ChooseVideoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, videoPath);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        chosen = false;
        if (getArguments() != null) {
            videoPath = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_choose_video, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set the video
        videoView = view.findViewById(R.id.videoView);
        File video = new File(videoPath);
        Uri uri = Uri.fromFile(video);
        videoView.setVideoURI(uri);
        videoView.requestFocus();
        videoView.start();
        videoView.setOnClickListener(v -> {
            // On click play the video
            videoView.stopPlayback();
            videoView.setVideoURI(uri);
            videoView.start();
        });

        // Add a listener to the whole fragment for choosing and un-choosing the video
        FrameLayout chooseVideoLayout = view.findViewById(R.id.ChooseVideoLayout);
        chooseVideoLayout.setOnClickListener(v -> {
            chosen = !chosen;

            if (chosen) {
                chooseVideoLayout.setBackgroundColor(getResources().getColor(R.color.purple));
            } else {
                chooseVideoLayout.setBackgroundColor(getResources().getColor(R.color.design_default_color_background));
            }
        });
    }

    public String getVideoPath() {
        return videoPath;
    }

    public boolean isChosen() {
        return chosen;
    }
}