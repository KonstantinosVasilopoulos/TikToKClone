import java.io.Serializable;
import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class Video implements Serializable {
    private String name;
    private String filename;
    private String channelName;
    private String dateCreated;
    private String framerate;
    private String frameWidth;
    private String frameHeight;
    private List<String> associatedHashtags;
    
    public Video(String name, String videosDir, String filename, String channelName) {
        this.name = name;
        this.filename = filename;
        this.channelName = channelName;
        this.associatedHashtags = new ArrayList<>();

        // try {
        //     // Use TIKA to get the video's metadata
        //     BodyContentHandler handler = new BodyContentHandler();
        //     Metadata metadata = new Metadata();
        //     FileInputStream inputstream = new FileInputStream(new File(videosDir, filename));
        //     ParseContext pcontext = new ParseContext();
        //     MP4Parser MP4Parser = new MP4Parser();
        //     MP4Parser.parse(inputstream, handler, metadata, pcontext);

        //     dateCreated = metadata.get("meta:creation-date");
        //     // framerate = metadata.get("framerate");
        //     frameWidth = metadata.get("tiff:ImageWidth");
        //     frameHeight = metadata.get("tiff:ImageLength");
            
        // } catch (IOException | SAXException | TikaException e) {
        //     e.printStackTrace();
        // }

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

    public String getFramerate() {
        return framerate;
    }

    public String getFrameWidth() {
        return frameWidth;
    }

    public String getFrameHeight() {
        return frameHeight;
    }

    public void setFramerate(String framerate) {
        this.framerate = framerate;
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