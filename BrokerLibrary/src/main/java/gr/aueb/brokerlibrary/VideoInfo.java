package gr.aueb.brokerlibrary;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

public class VideoInfo implements Serializable {
    private final String name;
    private final String filename;
    private final String channelName;
    private String dateCreated;
    private String frameRate;
    private String frameWidth;
    private String frameHeight;
    private List<String> associatedHashtags;
    
    public VideoInfo(String name, String filename, String channelName) {
        this.name = name;
        this.filename = filename;
        this.channelName = channelName;
        this.associatedHashtags = new ArrayList<>();

        // The channel's name is a topic for the video
        this.associatedHashtags.add(channelName);
    }

    public String getName() {
        return name;
    }

    public String getFilename() {
        return filename;
    }

    public String getChannelName() {
        return channelName;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public String getFrameRate() {
        return frameRate;
    }

    public String getFrameWidth() {
        return frameWidth;
    }

    public String getFrameHeight() {
        return frameHeight;
    }

    public void setFrameRate(String frameRate) {
        this.frameRate = frameRate;
    }

    public void setFrameWidth(String frameWidth) {
        this.frameWidth = frameWidth;
    }

    public void setFrameHeight(String frameHeight) {
        this.frameHeight = frameHeight;
    }

    public List<String> getAssociatedHashtags() {
        return associatedHashtags;
    }

    public void setAssociatedHashtags(List<String> associatedHashtags) {
        this.associatedHashtags = associatedHashtags;
    }

    public void addAssociatedHashtag(String hashtag) {
        if (!associatedHashtags.contains(hashtag))
            associatedHashtags.add(hashtag);
    }

    public void removeAssociatedHashtag(String hashtag) {
        associatedHashtags.remove(hashtag);
    }
}