package gr.aueb.tiktokclone.domain;

import android.content.Context;
import android.os.AsyncTask;

import java.math.BigInteger;
import java.net.Socket;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import gr.aueb.brokerlibrary.ChannelName;
import gr.aueb.brokerlibrary.Chunk;
import gr.aueb.brokerlibrary.VideoInfo;
import gr.aueb.brokerlibrary.Node;

public class Consumer implements Node {
    // Keys are hashes and values are lists with IP address and ports
    private Map<String, List<String>> brokers;
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private final ConsumerSubscriptionsTimer timer;

    // Data
    private String channelName;
    private List<String> subscribedTopics;
    private Map<VideoInfo, List<Chunk>> downloadedVideos;

    private final String DOWNLOADS_DIR;

    public Consumer(String channelName, Context context) {
        super();
        this.brokers = new HashMap<>();
        this.channelName = channelName;
        this.subscribedTopics = new ArrayList<>();
        this.downloadedVideos = new HashMap<>();

        // Setup directory for downloaded videos
        DOWNLOADS_DIR = context.getExternalFilesDir("downloads").getAbsolutePath();

        // Register the new user
        register();

        // Start the timer which monitors the consumer's subscriptions
        timer = new ConsumerSubscriptionsTimer(this);
        Thread timerThread = new Thread(timer);
        timerThread.start();
    }

    public String getChannelName() {
        return channelName;
    }

    public List<String> getSubscribedTopics() {
        return subscribedTopics;
    }

    public Map<VideoInfo, List<Chunk>> getDownloadedVideos() {
        return downloadedVideos;
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
            final String BROKER_IP = "192.168.1.4";
            connect(BROKER_IP, 55217);

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

        } catch (IOException | ClassNotFoundException ioe) {
            ioe.printStackTrace();
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
            VideoInfo requestedVideo;
            for (int i = 0; i < videosNumber; i++) {
                // Get the video class instance and save the video
                response = input.readObject();
                requestedVideo = (VideoInfo) response;
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

        } catch (IOException | ClassNotFoundException ioe) {
            ioe.printStackTrace();
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

    public void close() {
        timer.setTicking(false);
    }

    @Override
    public String getSHA1Hash(String text) {
        String hashedText = "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] textDigest = md.digest(text.getBytes());
            BigInteger no = new BigInteger(1, textDigest);
            hashedText = no.toString(16);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        return hashedText;
    }

    // Returns the broker to whom the hash belongs to
    // The broker is a list containing the broker's IP
    // address and port
    @Override
    public List<String> getBrokerForHash(String hash) {
        List<String> hashes = new ArrayList<>(brokers.keySet());
        Collections.sort(hashes);
        for (String h : hashes) {
            if (hash.compareTo(h) <= 0) {
                return brokers.get(h);
            }
        }

        // Return the last broker
        String lastHash = hashes.get(hashes.size() - 1);
        return brokers.get(lastHash);
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

    @Override
    public Map<String, List<String>> getBrokers() {
        return brokers;
    }

    @Override
    public void setBrokers(Map<String, List<String>> brokers) {
        this.brokers = brokers;
    }
}