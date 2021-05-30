package gr.aueb.tiktokclone.domain;

import java.net.Socket;
import java.util.List;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

import gr.aueb.tiktokclone.domain.Chunk;
import gr.aueb.tiktokclone.domain.Publisher;
import gr.aueb.tiktokclone.domain.Video;

public class VideoUploader implements Runnable {
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private Publisher publisher;

    public VideoUploader(Socket socket, Publisher publisher) {
        this.socket = socket;
        this.publisher = publisher;

        try {
            // Open IO streams
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    
    // This method is basically the push function
    public void run() {
        try {
            // Wait for the filename
            String filename = input.readUTF();

            // Find out whether the video exists
            boolean exists = false;
            Video video = null;
            for (Video v : publisher.getSavedVideos()) {
                if (filename.equals(v.getFilename())) {
                    exists = true;
                    video = v;
                    break;
                }
            }

            // Tell the broker that the video exists
            output.writeBoolean(exists);;
            output.flush();

            // Send the video instance
            output.writeObject(video);
            output.flush();

            // Send the chunks
            List<Chunk> chunks = publisher.generateChunks(video.getFilename());
            boolean success;
            for (Chunk chunk : chunks) {
                // Send chunk
                output.writeObject(chunk);
                output.flush();

                // Wait for true
                success = input.readBoolean();
                if (!success) return;
            }
            
            System.out.println("Publisher: Sent video " + video.getFilename() + ".");

            // Close the connection
            output.close();
            input.close();
            socket.close();

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}