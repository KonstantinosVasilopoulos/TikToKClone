package gr.aueb.brokerlibrary;

public class BrokerFactory {

    public void execute(int params) {
        for (int i = 0; i < params; i++) {
            Thread thread = new Thread(new Broker("127.0.0.1", 55217 + i));
            thread.start();
        }
    }

    public static void main(String[] args) {
        new BrokerFactory().execute(3);
    }
}