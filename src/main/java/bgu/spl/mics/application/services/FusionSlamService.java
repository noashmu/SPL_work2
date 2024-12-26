package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.MicroService;


import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.FusionSlam;
/**
 * FusionSlamService integrates data from multiple sensors to build and update
 * the robot's global map.
 * 
 * This service receives TrackedObjectsEvents from LiDAR workers and PoseEvents from the PoseService,
 * transforming and updating the map with new landmarks.
 */
public class FusionSlamService extends MicroService {
    /**
     * Constructor for FusionSlamService.
     *
     * @param fusionSlam The FusionSLAM object responsible for managing the global map.
     */
    private FusionSlam fusionSlam;

    public FusionSlamService(FusionSlam fusionSlam) {
        super("FusionSlamService");
        this.fusionSlam = fusionSlam;
    }

    /**
     * Initializes the FusionSlamService.
     * Registers the service to handle TrackedObjectsEvents, PoseEvents, and TickBroadcasts,
     * and sets up callbacks for updating the global map.
     */
    @Override
    protected void initialize() {
        //בדיקה אם האובייקטים כבר קיימים?
        this.subscribeEvent(TrackedObjectsEvent.class, (TrackedObjectsEvent event) -> {
            //   fusionSLAM.updateMap(event.getCloudPoints(), event.getObjectId());
            synchronized (fusionSlam) {
                fusionSlam.addLandMark(event.getTrackedObjects());
            }
            //   this.complete(event,);
        });

        this.subscribeEvent(PoseEvent.class, (PoseEvent event) -> {
            synchronized (fusionSlam) {
                fusionSlam.addPose(event.getPose());
            }
            //     this.complete(event, true);

        });

        this.subscribeBroadcast(TickBroadcast.class, (TickBroadcast tick) -> {
        });
        this.subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast term) -> {
            terminate();
        });
        this.subscribeBroadcast(CrashedBroadcast.class, (CrashedBroadcast crash) -> {
            terminate();
        });
    }
}

