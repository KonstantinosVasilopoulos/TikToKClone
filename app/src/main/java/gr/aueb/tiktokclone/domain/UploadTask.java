package gr.aueb.tiktokclone.domain;

import android.os.AsyncTask;

import gr.aueb.brokerlibrary.VideoInfo;

public class UploadTask extends AsyncTask<Void, Void, Void> {
    private final VideoInfo video;

    public UploadTask(VideoInfo video) {
        super();
        this.video = video;
    }

    @Override
    protected Void doInBackground(Void... params) {
        // Download the video using the consumer
        assert AppNode.getInstance() != null;
        AppNode.getInstance().getPublisher().upload(video);
        return null;
    }
}
