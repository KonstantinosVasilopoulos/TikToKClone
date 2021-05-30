package gr.aueb.tiktokclone.domain;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.lang.Thread;

public class PublisherRequestHandler implements Runnable {
    private Publisher publisher;
    private ServerSocket server;

    private final int PORT;

    public PublisherRequestHandler(int port, Publisher publisher) {
        PORT = port;
        this.publisher = publisher;
    }

    public void run() {
        try {
            // Open a server socket
            server = new ServerSocket(PORT);
            System.out.println("Publisher: Listening at port " + PORT + ".");

            while (true) {
                // Wait for a new request
                Socket socket = server.accept();

                // Create a new thread to handle the request
                Thread uploader = new Thread(new VideoUploader(socket, publisher));
                uploader.start();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void close() {
        try {
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }

    public int getPort() {
        return PORT;
    }
}
