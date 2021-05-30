package gr.aueb.tiktokclone.domain;

import java.io.Serializable;

public class Chunk implements Serializable {
    private String videoName;
    private String length;
    private byte[] videoFileChuck;
    private int chunkNumber;
    private boolean lastChunk;

    public Chunk(String videoName){
        this.videoName=videoName;
    }

    public String getVideoName(){
        return videoName;
    }

    public String getLength(){
        return length;
    }

    public byte[] getVideoFileChuck(){
        return videoFileChuck;
    }

    public int getChunkNumber() {
        return chunkNumber;
    }

    public boolean getLastChunk() {
        return lastChunk;
    }

    public void setVideoName(String videoName){
        this.videoName = videoName;
    }

    public void setLength(String length){
        this.length = length;
    }
    
    public void setVideoFileChuck(byte[] videoFileChuck){
        this.videoFileChuck = videoFileChuck;
    }

    public void setChunkNumber(int chunkNumber) {
        this.chunkNumber = chunkNumber;
    }

    public void setLastChunk(boolean lastChunk) {
        this.lastChunk = lastChunk;
    }

}
