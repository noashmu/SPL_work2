package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.StatisticalFolder;

/**
 * TimeService acts as the global timer for the system, broadcasting TickBroadcast messages
 * at regular intervals and controlling the simulation's duration.
 */
public class TimeService extends MicroService {
    private int TickTime;
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
        this.TickTime = TickTime;
        this.duration = Duration;
        this.currentTick = 0;
    }

    /**
     * Initializes the TimeService.
     * Starts broadcasting TickBroadcast messages and terminates after the specified duration.
     */
    @Override
    protected void initialize() {
        Thread timeThread = new Thread(() -> {
            StatisticalFolder.getInstance().setTickTime(TickTime);
            while (currentTick < duration) {

                StatisticalFolder.getInstance().incrementRuntime();
                try {
                    Thread.sleep(TickTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                currentTick=currentTick+TickTime;
                this.sendBroadcast(new TickBroadcast(currentTick));

                if (currentTick >= duration) {
                    this.sendBroadcast(new TerminatedBroadcast());
                    terminate();
                }
            }
        });
        timeThread.start();

        this.subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast term) -> {
            sendBroadcast(new TerminatedBroadcast());
            terminate();
        });
    }


}
