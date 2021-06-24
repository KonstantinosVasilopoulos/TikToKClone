package gr.aueb.tiktokclone.domain;

import java.util.concurrent.ExecutionException;

public class ConsumerSubscriptionsTimer implements Runnable {
    private final Consumer consumer;
    private long startTime;
    private long endTime;
    private final long PERIOD;
    private volatile boolean ticking;

    public ConsumerSubscriptionsTimer(Consumer consumer) {
        super();
        this.consumer = consumer;
        PERIOD = 12000;
        ticking = true;
    }

    @Override
    public void run() {
        while (ticking) {
            endTime = System.currentTimeMillis() - startTime;
            if (endTime >= PERIOD) {
                for (String topic : consumer.getSubscribedTopics()) {
                    consumer.query(topic);

                    // Sleep for 1 second
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                // Reset values for next clock tick
                startTime = System.currentTimeMillis();

            } else {
                // Sleep for 2 minutes
                try {
                    Thread.sleep(PERIOD);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setTicking(boolean ticking) {
        this.ticking = ticking;
    }
}
