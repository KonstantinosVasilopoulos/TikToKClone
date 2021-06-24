package gr.aueb.tiktokclone.ui;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import gr.aueb.brokerlibrary.VideoInfo;
import gr.aueb.tiktokclone.R;
import gr.aueb.tiktokclone.domain.AppNode;
import gr.aueb.tiktokclone.domain.Consumer;

public class VideoFragment extends Fragment {

    private String filename;
    private VideoInfo videoInfo;
    private File video;

    private TextView videoNameLabel;
    private VideoView videoPlayer;
    private Button likeBtn;
    private Button dislikeBtn;

    private static final String ARG_PARAM1 = "VIDEO_FILENAME";

    public VideoFragment() {
        // Required empty public constructor
    }

    public static VideoFragment newInstance(String param1) {
        VideoFragment fragment = new VideoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // Find the video's info class
            Consumer consumer = AppNode.getInstance().getConsumer();
            filename = getArguments().getString(ARG_PARAM1);
            for (VideoInfo v : consumer.getDownloadedVideos().keySet()) {
                if (filename.equals(v.getFilename())) {
                    videoInfo = v;
                    break;
                }
            }

            // Find the video using the given filename
            video = new File(consumer.getDownloadsDir(), filename);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_video, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set the video's name as label
        videoNameLabel = view.findViewById(R.id.videoNameLabel);
        videoNameLabel.setText(videoInfo.getName());

        // Setup the video player
        videoPlayer = view.findViewById(R.id.videoPlayer);
        MediaController controller = new MediaController(view.getContext());
        controller.setAnchorView(videoPlayer);
        Uri uri = Uri.fromFile(video);
        videoPlayer.setVideoURI(uri);
        videoPlayer.requestFocus();
        videoPlayer.start();
    }
}