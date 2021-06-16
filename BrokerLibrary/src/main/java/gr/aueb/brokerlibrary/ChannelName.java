package gr.aueb.brokerlibrary;

import java.util.*;
import java.io.Serializable;

/**
 * ChannelName
 */
public class ChannelName implements Serializable {
    private String channelName;
    private ArrayList<String> hashtagsPublished;
    private String ipAddress;
    private int port;

    public ChannelName(String channelName, String ipAddress, int port) {
        this.channelName = channelName;
        this.ipAddress = ipAddress;
        this.port = port;
        this.hashtagsPublished = new ArrayList<>();
    }

    public String getChannelName(){
        return channelName;
    }

    public void setChannelName(String channelName){
        this.channelName= channelName;
    }

    public ArrayList<String> getHashtagsPublished(){
        return new ArrayList<String>(hashtagsPublished);
    }

    public void addHashtagPublished(String hashtag){
        if (!hashtagsPublished.contains(hashtag))
            hashtagsPublished.add(hashtag);
    }

    public void removeHashtagPublished(String hashtag) {
        hashtagsPublished.remove(hashtag);
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
