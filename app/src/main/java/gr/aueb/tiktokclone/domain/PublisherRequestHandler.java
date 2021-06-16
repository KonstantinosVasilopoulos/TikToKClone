package gr.aueb.tiktokclone.domain;

import android.os.AsyncTask;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.lang.Thread;

public class PublisherRequestHandler extends AsyncTask<Void, Void, Void> {
    private final Publisher publisher;
    private ServerSocket server;

    private final int PORT;

    public PublisherRequestHandler(int port, Publisher publisher) {
        super();
        PORT = port;
        this.publisher = publisher;
    }

    @Override
    protected Void doInBackground(Void... voids) {
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
        return null;
    }

    @Override
    protected void onPostExecute(Void unused) {
        try {
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getPort() {
        return PORT;
    }
}
