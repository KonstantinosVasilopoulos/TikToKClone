package gr.aueb.tiktokclone.domain;

import java.net.Socket;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.lang.Thread;

import gr.aueb.tiktokclone.domain.PublisherRequestHandler;
import gr.aueb.tiktokclone.domain.Video;

public class Publisher extends Node {
    private ChannelName channelName;
    private Socket brokerSocket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private PublisherRequestHandler handler;

    // Data structures
    private List<Video> savedVideos;

    private final String VIDEOS_DIR;

    public Publisher(String channelName, String ip, int port) {
        super();
        this.channelName = new ChannelName(channelName, ip, port);
        this.savedVideos = new ArrayList<>();

        // Setup video storage
        File videoFolder = new File("videos/" + channelName + "/");
        VIDEOS_DIR = videoFolder.getAbsolutePath();
        videoFolder.mkdirs();

        // Get the list containg all brokers
        getBrokersHashMap();

        // Start the request handler
        handler = new PublisherRequestHandler(port, this);
        Thread requestHandler = new Thread(handler);
        requestHandler.start();
    }

    public ChannelName getChannelName() {
        return channelName;
    }

    public List<Video> getSavedVideos() {
        return savedVideos;
    }

    public String getVideosDir() {
        return VIDEOS_DIR;
    }

    @Override
    public void init() {
        // ADD STUFF YOU WANT THE PUBLISHER TO DO HERE
    }

    @Override
    public void connect(String address, int port){
        try{
            brokerSocket = new Socket(address, port);
            output = new ObjectOutputStream(brokerSocket.getOutputStream());
            input = new ObjectInputStream(brokerSocket.getInputStream());

            //send connect
            output.writeUTF("connectP");
            output.flush();

            // Wait for true
            boolean response = input.readBoolean();

            // Send the channel name
            if (response) {
                output.writeObject(channelName);
                output.flush();
                System.out.println("Publisher: Connected to broker.");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void disconnect() {
        try {
            output.close();
            input.close();
            brokerSocket.close();
            System.out.println("Publisher: Disconnected.");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    // Get the hashmap containing all brokers along with their info
    // We assume 127.0.0.1:55217 is always active
    @SuppressWarnings("unchecked")
    public void getBrokersHashMap() {
        try {
            // Connect to 127.0.0.1:55217
            connect("127.0.0.1", 55217);

            // Send "getBrokers"
            output.writeUTF("getBrokers");
            output.flush();

            // Wait for the brokers hashmap
            Object response = input.readObject();
            setBrokers((HashMap<String, List<String>>) response);
            System.out.println("Publisher: Received hashmap with all brokers.");

            // Close the connection
            disconnect();

        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
    }

    public void addHashtag(String hashtag) {
        channelName.addHashtagPublished(hashtag);
    }

    public void removeHashtag(String hashtag) {
        channelName.removeHashtagPublished(hashtag);
    }

    public void upload(Video video) {
        try {
            for (String topic : video.getAssociatedHashtags()) {
                // Update channel's published hashtags and save the video
                addHashtag(topic);
                if (!savedVideos.contains(video))
                    savedVideos.add(video);

                // Connect to the broker responsible for the hashtag
                List<String> broker = super.getBrokerForHash(super.getSHA1Hash(topic));
                connect(broker.get(0), Integer.parseInt(broker.get(1)));

                // Send "upload"
                output.writeUTF("upload");
                output.flush();

                // Send the channel name and the video's class instance
                output.writeUTF(channelName.getChannelName());
                output.writeObject(video);
                output.flush();

                // Wait for true and disconnect
                input.readBoolean();
                disconnect();
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public List<Chunk> generateChunks(String filename) {
        // Make sure the file given exists
        File file = new File(VIDEOS_DIR, filename);
        if (!file.exists()) return null;

        List<Chunk> chunks = new ArrayList<>();
        Chunk chunk;
        int chunkNumber = 0;
        int chunkSize = 1024;
        int bytesRead;
        byte[] buffer = new byte[chunkSize];

        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            
            // Read chunk sized parts of the file and create a 
            // new chunk for each one
           while ((bytesRead = bis.read(buffer)) != -1) {
                chunk = new Chunk(file.getName());
                chunk.setLength(String.valueOf(bytesRead));
                chunk.setVideoFileChuck(buffer);
                chunk.setChunkNumber(chunkNumber);
                chunk.setLastChunk(false);
                chunks.add(chunk);

                chunkNumber++;
                buffer = new byte[chunkSize];
            }

            // Mark the last chunk
            chunks.get(chunks.size() - 1).setLastChunk(true);

            // Close the input stream and return the chunks
            bis.close();
            return chunks;

        } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public List<Video> requestVideoList(String topic) {
        try {
            // Connect to the broker responsible for the hashtag
            List<String> broker = super.getBrokerForHash(super.getSHA1Hash(topic));
            connect(broker.get(0), Integer.parseInt(broker.get(1)));

            // Send "requestVideoList"
            output.writeUTF("requestVideoList");
            output.flush();

            // Send the topic and the channel's name
            output.writeUTF(topic);
            output.writeUTF(channelName.getChannelName());
            output.flush();

            // Wait for the list and disconnect
            Object response = input.readObject();
            List<Video> videos = (ArrayList<Video>) response;
            disconnect();
            return videos;

        } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
            return null;
        }
    }

    public void close() {
        handler.close();
    }

    public static void main(String[] args) {
        new Publisher(args[0], args[1], Integer.parseInt(args[2])).init();
    }
}