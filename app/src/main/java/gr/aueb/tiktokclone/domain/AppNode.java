package gr.aueb.tiktokclone.domain;

import android.content.Context;

import java.io.File;
import java.util.Scanner;

import gr.aueb.brokerlibrary.VideoInfo;

public class AppNode {
    private Publisher publisher;
    private Consumer consumer;
    private Scanner in;

    public AppNode(String channelName, String ip, int port, Context context) {
        publisher = new Publisher(channelName, ip, port, context);
        consumer = new Consumer(channelName, context);
    }

    // The CLIModule allows the user to operate an AppNode's 
    // functions using the terminal
    public void startCLIModule() {
        boolean operational = true;
        String command;
        String[] commands = {
            "upload",
            "download",
            "subscribe",
            "unsubscribe",
            "list",
            "commands",
            "exit",
        };

        in = new Scanner(System.in);

        while (operational) {
            // Receive a command from the user
            System.out.println("\nAppNode: Command: ");
            command = in.nextLine();

            // Categorize the command and performe the requested action
            switch (command) {
                case "upload":
                    upload();
                    break;

                case "download":
                    download();
                    break;

                case "subscribe":
                    subscribe();
                    break;

                case "unsubscribe":
                    unsubscribe();
                    break;

                case "list":
                    list();
                    break;

                case "commands":
                    for (int i = 0; i < commands.length; i++)
                        System.out.println((i + 1) + ". " + commands[i]);
                    break;

                case "exit":
                    operational = false;
                    consumer.close();
                    System.exit(0);

                default:
                    System.out.println("AppNode: No such command " + command + ".");
            }
        }

        in.close();
    }

    // The consumer requests to download a video
    public void download() {
        // Get a topic
        System.out.println("AppNode: Give a topic: ");
        String topic = in.nextLine();
        consumer.query(topic);
    }

    public void upload() {
        // Get the filename
        String filename;
        do {
            System.out.println("AppNode: Give the filename of the video: ");
            filename = in.nextLine();
        } while (!(new File(publisher.getVideosDir(), filename).exists()));

        // Get the name of the video
        System.out.println("AppNode: Give the video's name: ");
        String videoName = in.nextLine();

        // Get the hashtags for the video
        VideoInfo video = new VideoInfo(videoName, publisher.getVideosDir(), filename, publisher.getChannelName().getChannelName());
        String hashtag;
        do {
            System.out.println("AppNode: Give one hashtag or \"done\" to upload: ");
            hashtag = in.nextLine();
            video.addAssociatedHashtag(hashtag);
        } while (!hashtag.equals("done"));

        // Upload the video
        publisher.upload(video);
    }

    public void subscribe() {
        // Get a topic
        System.out.println("AppNode: Give a topic: ");
        String topic = in.nextLine();
        consumer.subscribe(topic);
    }

    public void unsubscribe() {
        // Get a topic
        System.out.println("AppNode: Give a topic: ");
        String topic = in.nextLine();
        consumer.unsubscribe(topic);
    }

    // Show a list of videos with a specific topic
    public void list() {
        // Get a topic
        System.out.println("AppNode: Give a topic: ");
        String topic = in.nextLine();

        for (VideoInfo video : publisher.requestVideoList(topic))
            System.out.println("AppNode: " + video.getName() + " - " + video.getFilename());
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public Consumer getConsumer() {
        return consumer;
    }
}