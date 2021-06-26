package gr.aueb.tiktokclone.domain;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import gr.aueb.tiktokclone.ui.ChannelActivity;

public class DownloadTask extends AsyncTask<String, Void, Void> {
    @SuppressLint("StaticFieldLeak")
    private ChannelActivity parent;

    public DownloadTask() {
        super();
    }

    public DownloadTask(ChannelActivity parent) {
        super();
        this.parent = parent;
    }

    @Override
    protected Void doInBackground(String... strings) {
        // Download the video using the consumer
        assert AppNode.getInstance() != null;
        AppNode.getInstance().getConsumer().query(strings[0]);
        parent.displayNewVideos();
        return null;
    }
}