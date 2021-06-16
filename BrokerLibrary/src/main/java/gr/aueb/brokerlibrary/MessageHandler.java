package gr.aueb.brokerlibrary;

import java.net.Socket;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;

public class MessageHandler implements Runnable {
    private Socket socket;
    private Broker broker;
    private ObjectOutputStream output;
    private ObjectInputStream input;

    public MessageHandler(Socket socket, Broker broker) {
        this.socket = socket;
        this.broker = broker;

        try {
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void run() {
        try {
            // Receive message and respond accordingly
            String receivedMessage;
            while (!socket.isClosed()) {
                if (input.available() == 0) continue;

                receivedMessage = input.readUTF();
                switch (receivedMessage) {
                    case "register":
                        replyBrokers();
                        acceptRegister();
                        break;

                    case "connectP":
                        acceptConnectionFromPublisher();
                        break;

                    case "upload":
                        acceptUpload();
                        break;

                    case "getBrokers":
                        replyBrokers();
                        break;                        

                    case "notifyChanges":
                        acceptNotifyChanges();
                        break;

                    case "sendTopic":
                        receiveTopic();
                        break;

                    case "query":
                        sendChunks();
                        break;

                    case "requestVideoList":
                        sendVideoList();
                        break;

                    case "requestRecommendedChannels":
                        replyRecommendedChannels();
                        break;

                    default:
                        System.out.println("Broker " + broker.getHash() + " Unknown operation: " + receivedMessage + ".");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void replyBrokers() {
        try {
            // Send the brokers hashmap
            output.writeObject(broker.getBrokers());
            output.flush();

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void acceptRegister() {
        try {
            // Receive the channel's name
            String channelName = input.readUTF();
            boolean success = broker.registerUser(channelName);

            // Receive the topics
            Object response = input.readObject();
            List<String> subscribedTopics = (ArrayList<String>) response;
            for (String topic : subscribedTopics)
                broker.addTopicToUser(channelName, topic);

            // Notify other brokers
            notifyBrokersOnChanges();

            // Send answer
            if (success) {
                System.out.println("Broker " + broker.getHash() + ": Registered user " + channelName + ".");
                output.writeBoolean(true);
            } else {
                System.out.println("Broker " + broker.getHash() + ": Failed to register user " + channelName + ".");
                output.writeBoolean(false);
            }
            output.flush();

        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
    }

    // Sends other brokers the registredUsers hash map
    private void notifyBrokersOnChanges() {
        try {
            List<String> connectionInfo;
            Socket otherSocket;
            ObjectOutputStream otherOutput;
            ObjectInputStream otherInput;
            for (String h : broker.getBrokers().keySet()) {
                // Skip yourself
                if (h.equals(broker.getHash())) continue;

                // Connect to other broker and open IO streams
                connectionInfo = broker.getBrokers().get(h);
                otherSocket = new Socket(connectionInfo.get(0), Integer.parseInt(connectionInfo.get(1)));
                otherOutput = new ObjectOutputStream(otherSocket.getOutputStream());
                otherInput = new ObjectInputStream(otherSocket.getInputStream());

                // Send "notifyChanges"
                otherOutput.writeUTF("notifyChanges");
                otherOutput.flush();

                // Send registeredUSers hashmap
                otherOutput.writeObject(broker.getRegisteredUsers());
                otherOutput.flush();

                // Close the connection
                otherOutput.close();
                otherInput.close();
                otherSocket.close();
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void replyRecommendedChannels() {
        try {
            // Wait for the channel's name
            String channelName = input.readUTF();

            // Filter the list
            List<ChannelName> toRemove = new ArrayList<>();
            List<ChannelName> channels = broker.getRegisteredPublishers();
            for (ChannelName channel : channels) {
                if (channelName.equals(channel.getChannelName()))
                    toRemove.add(channel);
            }
            channels.removeAll(toRemove);

            // Send list containing channels
            output.writeObject(channels);
            output.flush();
            System.out.println("Broker " + broker.getHash() + "Sent recommended channels to comsumer.");

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void receiveTopic() {
        try {
            // Read the channels' name, the topic 
            // and the action(true=add, false=remove)
            String channelName = input.readUTF();
            String topic = input.readUTF();
            boolean action = input.readBoolean();

            // Perform action
            boolean success;
            if (action) {
                success = broker.addTopicToUser(channelName, topic);
                if (success)
                    System.out.println("Broker " + broker.getHash() + ": Added topic " + topic + " to user " + channelName + ".");
                else
                    System.out.println("Broker " + broker.getHash() + ": No such topic: " + topic + ".");
            } else {
                success = broker.removeTopicFromUser(channelName, topic);
                if (success)
                    System.out.println("Broker " + broker.getHash() + ": Removed topic " + topic + " from user " + channelName + ".");
                else
                    System.out.println("Broker " + broker.getHash() + ": No such topic: " + topic + ".");
            }

            notifyBrokersOnChanges();

            // Send true
            output.writeBoolean(true);
            output.flush();

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void acceptNotifyChanges() {
        try {
            // Receive the registeredUsers hashmap
            Object response = input.readObject();
            ConcurrentMap<String, List<String>> changes = (ConcurrentHashMap<String, List<String>>) response;

            // Check for new users and topics
            for (String user : changes.keySet()) {
                broker.registerUser(user);
                for (String topic : changes.get(user))
                    broker.addTopicToUser(user, topic);
            }
            System.out.println("Broker " + broker.getHash() + ": Got notified about changes.");

        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
    }

    // Sends the video chunks for a requested topic
    private void sendChunks() {
        try {
            // Wait for the channel's name and the topic
            String channelName = input.readUTF();
            String topic = input.readUTF();

            // Find the number of videos that will be sent
            int videosFound = 0;
            List<VideoInfo> videos = new ArrayList<>();
            for (VideoInfo video : broker.getSavedVideos().keySet()) {
                if (video.getAssociatedHashtags().contains(topic) &&
                    !channelName.equals(video.getChannelName())) {
                        videos.add(video);
                        videosFound++;
                }
            }

            // Tell the consumer whether the requested topic has any videos
            output.writeBoolean(videosFound > 0);
            output.flush();

            // Send the number of videos to be expected
            output.writeInt(videosFound);
            output.flush();

            // Send the videos
            List<Chunk> chunks;
            boolean success;
            for (VideoInfo video : videos) {
                // Send the video's class instance
                output.writeObject(video);
                output.flush();

                // Send the chunks
                chunks = broker.getSavedVideos().get(video);
                for (Chunk chunk : chunks) {
                    output.writeObject(chunk);
                    output.flush();

                    // Wait for true
                    success = input.readBoolean();
                    if (!success) break;
                }

                System.out.println("Broker " + broker.getHash() + ": Sent video " + video.getFilename() + ".");
            }
        
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void acceptConnectionFromPublisher() {
        try{
            // Send true
            output.writeBoolean(true);
            output.flush();

            // Wait for the channel's name
            Object response = input.readObject();
            broker.addRegisteredPublisher((ChannelName) response);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException ce) {
            ce.printStackTrace();
        }
    }

    private void acceptUpload() {
        try {
            // Wait for the channel name and the video's class instance
            String channelName = input.readUTF();
            Object response = input.readObject();
            VideoInfo video = (VideoInfo) response;

            // Extract topics from video
            for (String topic : video.getAssociatedHashtags())
                broker.addHashtagToPublisher(channelName, topic);

            // Find whether the video has already been pulled
            boolean exists = false;
            for (VideoInfo v : broker.getSavedVideos().keySet()) {
                if (video.getFilename().equals(v.getFilename())) {
                    exists = true;
                    break;
                }
            }

            // Pull the video
            if (!exists) {
                for (ChannelName channel : broker.getRegisteredPublishers()) {
                    if (channel.getChannelName().equals(channelName)) {
                        pull(video.getFilename(), channel.getIpAddress(), channel.getPort());
                        break;
                    }
                }
            }

            // Send true
            output.writeBoolean(true);
            output.flush();

        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
    }

    private void sendVideoList() {
        try {
            // Wait for the topic and the channel's name
            String topic = input.readUTF();
            String channelName = input.readUTF();

            // Send all videos with the same topic
            List<VideoInfo> videos = new ArrayList<>();
            for (VideoInfo v : broker.getSavedVideos().keySet()) {
                if (v.getAssociatedHashtags().contains(topic) && !v.getChannelName().equals(channelName))
                    videos.add(v);
            }
            output.writeObject(videos);
            output.flush();

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void pull(String filename, String ipAddress, int port) {
        try {
            // Connect to the publisher's request handler
            Socket videoSocket = new Socket(ipAddress, port);
            ObjectOutputStream videoOutput = new ObjectOutputStream(videoSocket.getOutputStream());
            ObjectInputStream videoInput = new ObjectInputStream(videoSocket.getInputStream());

            // Send the filename
            videoOutput.writeUTF(filename);
            videoOutput.flush();

            // Wait for confirmation of the video's existence
            boolean exists = videoInput.readBoolean();
            if (!exists) {
                System.out.println("Broker " + broker.getHash() + ": Video " + filename + " doesn't exist.");
                videoOutput.close();
                videoInput.close();
                videoSocket.close();
                return;
            }

            // Wait for the video class instance and save the video
            Object response = videoInput.readObject();
            VideoInfo requestedVideo = (VideoInfo) response;
            broker.saveVideo(requestedVideo);

            // Receive the chunks
            Chunk chunk;
            while (true) {
                response = videoInput.readObject();
                chunk = (Chunk) response;

                // Save the chunk and send true
                broker.addChunkToVideo(requestedVideo, chunk);
                videoOutput.writeBoolean(true);
                videoOutput.flush();

                // Break if this is the last chunk
                if (chunk.getLastChunk()) break;
            }

            System.out.println("Broker " + broker.getHash() + ": Received video " + requestedVideo.getFilename() + ".");

            // Close the connection
            videoOutput.close();
            videoInput.close();
            videoSocket.close();

        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
    }
}