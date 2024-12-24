package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TickBroadcast;

/**
 * TimeService acts as the global timer for the system, broadcasting TickBroadcast messages
 * at regular intervals and controlling the simulation's duration.
 */
public class TimeService extends MicroService {
    private int tickTime;
    private int duration;
    private int currentTick;
    /**
     * Constructor for TimeService.
     *
     * @param TickTime  The duration of each tick in milliseconds.
     * @param Duration  The total number of ticks before the service terminates.
     */
    public TimeService(int TickTime, int Duration) {
        super("TimeService");
        this.tickTime = TickTime;
        this.duration = Duration;
        this.currentTick = 0;
    }

    /**
     * Initializes the TimeService.
     * Starts broadcasting TickBroadcast messages and terminates after the specified duration.
     */
    @Override
    protected void initialize() {
        // Start broadcasting TickBroadcast messages at regular intervals
        Thread timeThread = new Thread(() -> {
            while (currentTick < duration) {
                // Wait for the duration of one tick
                try {
                    Thread.sleep(tickTime);
                    currentTick++;
                    this.sendBroadcast(new TickBroadcast(currentTick));
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                }

                if (currentTick >= duration) {
                    terminate(); //לבדוק אם צריך
                }
            }
        });
        timeThread.start();
    }

}
