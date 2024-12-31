package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.StatisticalFolder;

/**
 * PoseService is responsible for maintaining the robot's current pose (position and orientation)
 * and broadcasting PoseEvents at every tick.
 */
public class PoseService extends MicroService {
    private GPSIMU gpsimu;

    /**
     * Constructor for PoseService.
     *
     * @param gpsimu The GPSIMU object that provides the robot's pose data.
     */
    public PoseService(GPSIMU gpsimu) {
        super("PoseService");
        this.gpsimu = gpsimu;
    }

    /**
     * Initializes the PoseService.
     * Subscribes to TickBroadcast and sends PoseEvents at every tick based on the current pose.
     */
    @Override
    protected void initialize() {
        this.subscribeBroadcast(TickBroadcast.class, (TickBroadcast tick) -> {
            if (gpsimu.getStatus().equals(STATUS.ERROR)) {
                sendBroadcast(new CrashedBroadcast("Sensor GPSIMU disconnected","GPSIMU"
                        ,null, null, gpsimu.getPoseList()));
                terminate();
            }
            Pose currentPose = gpsimu.getPose(tick.getTick());
            sendEvent(new PoseEvent(currentPose));
       });

        this.subscribeBroadcast(CrashedBroadcast.class, (CrashedBroadcast crash) -> {
            terminate();
        });

        this.subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast terminated) -> {
            sendBroadcast(new TerminatedBroadcast());
            terminate();
        });
    }
}
