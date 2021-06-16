package gr.aueb.brokerlibrary;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public abstract class Node {
    // Keys are hashes and values are lists with IP address and ports
    protected Map<String, List<String>> brokers;

    public Node() {
        brokers = new HashMap<>();
    }

    public abstract void init();

    public abstract void connect(String address, int port);

    public abstract void disconnect();

    public Map<String, List<String>> getBrokers() {
        return brokers;
    }

    public void setBrokers(Map<String, List<String>> brokers) {
        this.brokers = brokers;
    }

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
}