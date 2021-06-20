package gr.aueb.brokerlibrary;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public interface Node {

    public void init();

    public void connect(String address, int port);

    public void disconnect();

    public Map<String, List<String>> getBrokers();

    public void setBrokers(Map<String, List<String>> brokers);

    public String getSHA1Hash(String text);

    // Returns the broker to whom the hash belongs to
    // The broker is a list containing the broker's IP
    // address and port
    public List<String> getBrokerForHash(String hash);
}