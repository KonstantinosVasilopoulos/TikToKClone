package gr.aueb.brokerlibrary;

import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.Thread;

public class Broker implements Node, Runnable {
    // Keys are hashes and values are lists with IP address and ports
    private Map<String, List<String>> brokers;

    private ServerSocket serverSocket;

    // The hash is used for determining which hashtags and content creators
    // belong to this broker
    private String hash;  // Unique identifier for the Broker

    // Data structures for storing publishers and consumers
    private List<ChannelName> registeredPublishers;
    // Keys are usernames(aka channel names) and values are lists with topics
    private ConcurrentMap<String, List<String>> registeredUsers;

    // Data structure for storing saved videos
    private ConcurrentMap<VideoInfo, List<Chunk>> savedVideos;

    private final String IP_ADDRESS;
    private final int SERVER_PORT;

    public Broker(String ip, int port) {
        super();
        IP_ADDRESS = ip;
        SERVER_PORT = port;

        // Initiate data structures
        brokers = new HashMap<>();
        registeredUsers = new ConcurrentHashMap<>();
        registeredPublishers = new ArrayList<>();
        savedVideos = new ConcurrentHashMap<>();

        // Calculate the max hash using the SHA1 of the IP address plus the port number
        hash = getSHA1Hash(IP_ADDRESS + String.valueOf(SERVER_PORT));

        // Setup the brokers hashmap
        int brokersNum = 3;  // CHANGE THIS IF YOU WANT TO CHANGE THE NUMBER OF BROKERS!
        for (int i = 0; i < brokersNum; i++) {
            List<String> info = new ArrayList<>();
            info.add(ip);
            info.add(String.valueOf(55217 + i));
            brokers.put(getSHA1Hash(info.get(0) + info.get(1)), info);
        }
    }

    @Override
    public void run() {
        init();
    }

    @Override
    public void init() {
        try {
            serverSocket = new ServerSocket(SERVER_PORT);
            System.out.println("Broker " + hash + ": Server running at " + SERVER_PORT + ".");

            while (true) {
                // Await connection from a producer or consumer
                Socket socket = serverSocket.accept();
                System.out.println("Broker " + hash + ": Client accepted.");

                // Use a thread to handle each socket
                Thread handler = new Thread(new MessageHandler(socket, this));
                handler.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void disconnect() {

    }

    // Broker connects to a specific Consumer
    @Override
    public void connect(String address, int port) {
        
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

    public String getHash() {
        return hash;
    }

    public List<ChannelName> getRegisteredPublishers() {
        return new ArrayList<>(registeredPublishers);
    }

    public ConcurrentMap<VideoInfo, List<Chunk>> getSavedVideos() {
        return savedVideos;
    }

    public void saveVideo(VideoInfo video) {
        if (!savedVideos.containsKey(video))
            savedVideos.put(video, new ArrayList<Chunk>());
    }

    public void deleteVideo(VideoInfo video) {
        savedVideos.remove(video);
    }

    public void addHashtagToVideo(VideoInfo video, String hashtag) {
        if (savedVideos.containsKey(video)) {
            for (VideoInfo v : savedVideos.keySet()) {
                if (v.getFilename().equals(video.getFilename()))
                    v.addAssociatedHashtag(hashtag);
            }
        }
    }

    public void addChunkToVideo(VideoInfo video, Chunk chunk) {
        if (savedVideos.containsKey(video))
            savedVideos.get(video).add(chunk);
    }

    public ConcurrentMap<String, List<String>> getRegisteredUsers() {
        return registeredUsers;
    }

    public boolean registerUser(String channelName) {
        if (!registeredUsers.containsKey(channelName)) {
            registeredUsers.put(channelName, new ArrayList<>());
            return true;
        }

        return false;
    }

    public void unregisterUser(String channelName) {
        if (registeredUsers.containsKey(channelName))
            registeredUsers.remove(channelName);
    }

    public boolean addTopicToUser(String channelName, String topic) {
        if (registeredUsers.containsKey(channelName)) {
            if (!registeredUsers.get(channelName).contains(topic)) {
                registeredUsers.get(channelName).add(topic);
                return true;
            }
        }
        
        return false;
    }

    public boolean removeTopicFromUser(String channelName, String topic) {
        if (registeredUsers.containsKey(channelName)) {
            if (registeredUsers.get(channelName).contains(topic)) {
                registeredUsers.get(channelName).remove(topic);
                return true;
            }
        }

        return false;
    }

    public void addHashtagToPublisher(String channelName, String hashtag) {
        // Find the channelName and add the hashtag
        for (ChannelName channel : registeredPublishers) {
            if (channel.getChannelName().equals(channelName)) {
                channel.addHashtagPublished(hashtag);
                break;
            }
        }
    }

    public void removeHashtagFromPublisher(String channelName, String hashtag) {
        for (ChannelName channel : registeredPublishers) {
            if (channel.getChannelName().equals(channelName)) {
                channel.removeHashtagPublished(hashtag);
                break;
            }
        }
    }

    public void addRegisteredPublisher(ChannelName channelName) {
        for (ChannelName channel : registeredPublishers) {
            if (channel.getChannelName().equals(channelName.getChannelName()))
                return;
        }
        registeredPublishers.add(channelName);
    }

    @Override
    public Map<String, List<String>> getBrokers() {
        return brokers;
    }

    @Override
    public void setBrokers(Map<String, List<String>> brokers) {
        this.brokers = brokers;
    }

    public static void main(String[] args) {
        new Broker(args[0], Integer.parseInt(args[1])).init();
    }
}