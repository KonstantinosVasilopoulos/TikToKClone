package gr.aueb.tiktokclone.domain;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import gr.aueb.brokerlibrary.Broker;
import gr.aueb.brokerlibrary.ChannelName;

class ConsumerTest {
    private List<Consumer> consumers;

    @BeforeEach
    void setUp() {
        consumers = new ArrayList<>();

        // Start 3 brokers
//        for (int i = 0; i < 3; i++) {
//            Thread broker = new Thread(new Broker("127.0.0.1", 55217 + i));
//            broker.start();
//        }

        // Create 5 publishers and 5 consumers
        Context context = InstrumentationRegistry.getInstrumentation().getContext();
        for (int i = 0; i < 5; i++) {
            new Publisher("name" + i, "127.0.0.1", 55220 + i);
            consumers.add(new Consumer("name" + i));
        }
    }

    @Test
    void query() {
    }

    @Test
    void requestRecommendedChannels() {
        List<ChannelName> channels = consumers.get(0).requestRecommendedChannels();
        Assertions.assertEquals(4, channels.size());
    }
}