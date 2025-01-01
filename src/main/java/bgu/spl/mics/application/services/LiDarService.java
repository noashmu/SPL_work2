package bgu.spl.mics.application.services;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.*;

import bgu.spl.mics.MicroService;

import java.util.ArrayList;
import java.util.List;

/**
 * LiDarService is responsible for processing data from the LiDAR sensor and
 * sending TrackedObjectsEvents to the FusionSLAM service.
 * 
 * This service interacts with the LiDarWorkerTracker object to retrieve and process
 * cloud point data and updates the system's StatisticalFolder upon sending its
 * observations.
 */
public class LiDarService extends MicroService {
    private LiDarWorkerTracker LiDarWorkerTracker;

    /**
     * Constructor for LiDarService.
     *
     * @param LiDarWorkerTracker A LiDAR Tracker worker object that this service will use to process data.
     */
    public LiDarService(LiDarWorkerTracker LiDarWorkerTracker) {
        super("LidarWorkerTrackerService");
        this.LiDarWorkerTracker = LiDarWorkerTracker;
    }

    /**
     * Initializes the LiDarService.
     * Registers the service to handle DetectObjectsEvents and TickBroadcasts,
     * and sets up the necessary callbacks for processing data.
     */
    @Override
    protected void initialize() {
        // לבדוק אם צריך לשלוח את האירוע בbroadcast או בevent
        this.subscribeEvent(DetectObjectsEvent.class, (DetectObjectsEvent event) -> {
            try {
                LiDarWorkerTracker.setLastTrackedObjects(event.getDetectedObjects(), event.getTime());
                if (LiDarWorkerTracker.detectError()) {
                    List<List<CloudPoint>> points = new ArrayList<>();
                    for (TrackedObject tracked: LiDarWorkerTracker.getLastTrackedObjects()){
                        points.add(tracked.getCoordinates());
                    }

                    sendBroadcast(new CrashedBroadcast("Sensor Lidar disconnected",
                            "Lidar", null, points,null));
                    terminate();
                }
                this.complete(event,true);

            }
            catch (Exception e)
            {
                this.complete(event,false);
            }
        });

        this.subscribeBroadcast(TickBroadcast.class, (TickBroadcast event) -> {
            if(LiDarWorkerTracker.shouldSendEvent(event.getTick())){
                TrackedObjectsEvent trackedObjectsEvent = new TrackedObjectsEvent(LiDarWorkerTracker.getLastTrackedObjects(),event.getTick());
                this.sendEvent(trackedObjectsEvent);
            }
        });

        this.subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast term) -> {
            sendBroadcast(new TerminatedBroadcast());
            terminate();
        });

        this.subscribeBroadcast(CrashedBroadcast.class, (CrashedBroadcast crash) -> {
            terminate();
        });
    }
}
