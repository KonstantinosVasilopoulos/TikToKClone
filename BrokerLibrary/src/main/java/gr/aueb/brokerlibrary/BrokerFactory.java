package gr.aueb.brokerlibrary;

public class BrokerFactory {

    private final static String IP_ADDRESS = "192.168.1.4";

    public void execute(int params) {
        for (int i = 0; i < params; i++) {
            Thread thread = new Thread(new Broker(IP_ADDRESS, 55217 + i));
            thread.start();
        }
    }

    public static String getIP_ADDRESS() {
        return IP_ADDRESS;
    }

    public static void main(String[] args) {
        new BrokerFactory().execute(3);
    }
}