import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.Thread;

public class Broker extends Node {
    private ServerSocket serverSocket;

    // The hash is used for determining which hashtags and content creators
    // belong to this broker
    private String hash;  // Unique identifier for the Broker

    // Data structures for storing publishers and consumers
    private List<ChannelName> registeredPublishers;
    // Keys are usernames(aka channel names) and values are lists with topics
    private ConcurrentMap<String, List<String>> registeredUsers;

    // Data structure for storing saved videos
    private ConcurrentMap<Video, List<Chunk>> savedVideos;

    private final String IP_ADDRESS;
    private final int SERVER_PORT;

    public Broker(String ip, int port) {
        super();
        IP_ADDRESS = ip;
        SERVER_PORT = port;

        // Initiate data structures
        registeredUsers = new ConcurrentHashMap<>();
        registeredPublishers = new ArrayList<>();
        savedVideos = new ConcurrentHashMap<>();

        // Calculate the max hash using the SHA1 of the IP address plus the port number
        hash = getSHA1Hash(IP_ADDRESS + String.valueOf(SERVER_PORT));

        // Setup the brokers hashmap
        int brokersNum = 3;  // CHANGE THIS IF YOU WANT TO CHANGE THE NUMBER OF BROKERS!
        for (int i = 0; i < brokersNum; i++) {
            List<String> info = new ArrayList<>();
            info.add("127.0.0.1");
            info.add(String.valueOf(55217 + i));
            brokers.put(getSHA1Hash(info.get(0) + info.get(1)), info);
        }
    }

    @Override
    public void init() {
        try {
            serverSocket = new ServerSocket(SERVER_PORT);
            System.out.println("Broker " + hash + ": Server running.");

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

    public String getHash() {
        return hash;
    }

    public List<ChannelName> getRegisteredPublishers() {
        return new ArrayList<>(registeredPublishers);
    }

    public ConcurrentMap<Video, List<Chunk>> getSavedVideos() {
        return savedVideos;
    }

    public void saveVideo(Video video) {
        if (!savedVideos.containsKey(video))
            savedVideos.put(video, new ArrayList<Chunk>());
    }

    public void deleteVideo(Video video) {
        savedVideos.remove(video);
    }

    public void addHashtagToVideo(Video video, String hashtag) {
        if (savedVideos.containsKey(video)) {
            for (Video v : savedVideos.keySet()) {
                if (v.getFilename().equals(video.getFilename()))
                    v.addAssociatedHashtag(hashtag);
            }
        }
    }

    public void addChunkToVideo(Video video, Chunk chunk) {
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

    public static void main(String[] args) {
        new Broker(args[0], Integer.parseInt(args[1])).init();
    }
}