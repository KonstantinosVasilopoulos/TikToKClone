package gr.aueb.tiktokclone.domain;

import android.os.AsyncTask;

public class ConsumerSubscriptionsTimer extends AsyncTask<Void, Void, Void> {
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
    protected Void doInBackground(Void... voids) {
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

        return null;
    }

    public void setTicking(boolean ticking) {
        this.ticking = ticking;
    }
}
