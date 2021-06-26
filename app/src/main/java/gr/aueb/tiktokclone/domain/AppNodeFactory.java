package gr.aueb.tiktokclone.domain;

import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;

import gr.aueb.brokerlibrary.VideoInfo;

public class AppNodeFactory extends AsyncTask<Integer, Void, Void> {

    private final String IP_ADDRESS = "192.168.1.2";
    private final String DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();

    public AppNodeFactory() {
        super();
    }

    @Override
    protected Void doInBackground(Integer... integers) {
        AppNode node;
        int port = 55221;
        String username;
        for (int i = 0; i < integers[0]; i++) {
            if (i == 0) {
                username = "syntakas.alex";
                node = new AppNode(username, IP_ADDRESS, port + i, DIR);
                VideoInfo video = new VideoInfo("Acropolis", node.getPublisher().getVideosDir(),
                        "20210626_135646.mp4", username);
                node.getPublisher().upload(video);

//                video = new VideoInfo("Classes Example", node.getPublisher().getVideosDir(),
//                        "video_2.mp4", username);
//                node.getPublisher().upload(video);

            } else  if (i == 1) {
                username = "makis01";
                new AppNode(username, IP_ADDRESS, port + i, DIR);

            } else {
                username = "User_" + i;
                new AppNode(username, IP_ADDRESS, port + i, DIR);
            }
        }

        return null;
    }
}