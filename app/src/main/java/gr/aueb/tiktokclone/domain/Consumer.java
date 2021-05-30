package gr.aueb.tiktokclone.domain;

import java.net.Socket;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import gr.aueb.tiktokclone.domain.Node;
import gr.aueb.tiktokclone.domain.Video;

public class Consumer extends Node {
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;

    // Data
    private String channelName;
    private List<String> subscribedTopics;
    private Map<Video, List<Chunk>> downloadedVideos;

    private final String DOWNLOADS_DIR;

    public Consumer(String channelName) {
        super();
        this.channelName = channelName;
        this.subscribedTopics = new ArrayList<>();
        this.downloadedVideos = new HashMap<>();

        // Setup directory for downloaded videos
        File videosFolder = new File("downloads/" + channelName + "/");
        DOWNLOADS_DIR = videosFolder.getAbsolutePath();
        videosFolder.mkdirs();

        // Register the new user
        register();
    }

    public String getChannelName() {
        return channelName;
    }

    public List<String> getSubscribedTopics() {
        return subscribedTopics;
    }

    public void subscribe(String topic) {
        if (!subscribedTopics.contains(topic)) {
            subscribedTopics.add(topic);
            sendTopic(topic, true);
        }
    }

    public void unsubscribe(String topic) {
        if (subscribedTopics.contains(topic)) {
            subscribedTopics.remove(topic);
            sendTopic(topic, false);
        }
    }

    public String getDownloadsDir() {
        return DOWNLOADS_DIR;
    }

    @Override
    public void init() {
        // ADD STUFF YOU WANT THE CONSUMER TO DO HERE
    }

    @Override
    public void connect(String address, int port) {
        try {
            // Connect and open IO streams
            socket = new Socket(address, port);
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
            System.out.println("Consumer " + channelName + ": Connected to " + address + ":" + port + ".");

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    @Override
    public void disconnect() {
        try {
            output.close();
            input.close();
            socket.close();
            System.out.println("Consumer " + channelName + ": Disconnected.");

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    // Get the hashmap containing all brokers along with their info
    // We assume 127.0.0.1:55217 is always active
    @SuppressWarnings("unchecked")
    public void register() {
        try {
            // Connect to 127.0.0.1:55217
            connect("127.0.0.1", 55217);

            // Send "register"
            output.writeUTF("register");
            output.flush();

            // Wait for the brokers hashmap
            Object response = input.readObject();
            setBrokers((HashMap<String, List<String>>) response);

            // Send the channel's name and the subscribedTopics
            output.writeUTF(channelName);
            output.writeObject(subscribedTopics);

            // Wait for true
            boolean success = input.readBoolean();
            if (success)
                System.out.println("Consumer " + channelName + ": Registered successfully.");
            else
                System.out.println("Consumer " + channelName + ": Failed to register.");

            // Close the connection
            disconnect();

        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
    }

    public void query(String topic) {
        try {
            // Send "query" to the correct broker
            List<String> broker = getBrokerForHash(getSHA1Hash(topic));
            connect(broker.get(0), Integer.parseInt(broker.get(1)));
            output.writeUTF("query");
            output.flush();

            // Send the channel's name and the topic
            output.writeUTF(channelName);
            output.writeUTF(topic);
            output.flush();

            // Wait for an answer
            boolean exists = input.readBoolean();
            if (!exists) {
                System.out.println("Consumer " + channelName + ": Topic " + topic + " doesn't have any videos.");
                return;
            }

            // Receive the number of videos to be expected
            int videosNumber = input.readInt();

            // Receive the videos
            BufferedOutputStream bos;
            Object response;
            Chunk chunk;
            Video requestedVideo;
            for (int i = 0; i < videosNumber; i++) {
                // Get the video class instance and save the video
                response = input.readObject();
                requestedVideo = (Video) response;
                downloadedVideos.put(requestedVideo, new ArrayList<>());
                bos = new BufferedOutputStream(new FileOutputStream(
                    new File(DOWNLOADS_DIR, requestedVideo.getFilename())
                ));

                // Receive the chunks
                while (true) {
                    response = input.readObject();
                    chunk = (Chunk) response;

                    // Save the chunk and send true
                    downloadedVideos.get(requestedVideo).add(chunk);
                    bos.write(chunk.getVideoFileChuck());
                    output.writeBoolean(true);
                    output.flush();

                    // Break if this is the last chunk
                    if (chunk.getLastChunk()) break;
                }

                bos.close();
                System.out.println("Consumer " + channelName + ": Received video " + requestedVideo.getFilename() + ".");
            }

            disconnect();

        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public List<ChannelName> requestRecommendedChannels() {
        List<ChannelName> recommendedChannels = new ArrayList<>();
        for (String broker : brokers.keySet()) {
            // Connect to the broker
            connect(brokers.get(broker).get(0), Integer.parseInt(brokers.get(broker).get(1)));

            try {
                // Send "requestRecommendedChannels"
                output.writeUTF("requestRecommendedChannels");
                output.flush();

                // Send the channel's name
                output.writeUTF(channelName);
                output.flush();

                // Wait for list containing the recommended channels
                Object response = input.readObject();
                List<ChannelName> channels = (ArrayList<ChannelName>) response;

                // Merge lists and disconnect
                recommendedChannels.addAll(channels);
                disconnect();

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        // Notify user and return channels list
        System.out.println("Consumer " + channelName + ": Received recommended channels.");
        return recommendedChannels;
    }

    private void sendTopic(String topic, boolean action) {
        try {
            // Find and connect to the broker responsible for the topic
            List<String> broker = getBrokerForHash(getSHA1Hash(topic));
            connect(broker.get(0), Integer.parseInt(broker.get(1)));

            // Send "sendTopic"
            output.writeUTF("sendTopic");
            output.flush();

            // Send the channel's name, the topic, the action
            output.writeUTF(channelName);
            output.writeUTF(topic);
            output.writeBoolean(action);
            output.flush();

            // Wait for true and disconnect
            input.readBoolean();
            System.out.println("Consumer " + channelName + ": Notified brokers for topic " + topic + ".");
            disconnect();

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Consumer(args[0]).init();
    }
}