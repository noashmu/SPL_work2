package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;

import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.*;

import java.io.File;
import java.util.List;

/**
 * FusionSlamService integrates data from multiple sensors to build and update
 * the robot's global map.
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
    private String configPath;

    public FusionSlamService(FusionSlam fusionSlam,String configPath) {
        super("FusionSlamService");
        this.fusionSlam = fusionSlam;
        File baseFile = new File(configPath).getParentFile();
        this.configPath=baseFile.getAbsolutePath();
    }

    /**
     * Initializes the FusionSlamService.
     * Registers the service to handle TrackedObjectsEvents, PoseEvents, and TickBroadcasts,
     * and sets up callbacks for updating the global map.
     */
    @Override
    protected void initialize() {
        this.subscribeEvent(TrackedObjectsEvent.class, (TrackedObjectsEvent event) -> {
            synchronized (fusionSlam) {
                try {
                    List<TrackedObject> trackedObjects = event.getTrackedObjects();

                    for (TrackedObject obj : trackedObjects) {
                        Pose currentPose = fusionSlam.getCurrentPose(obj.getTime());
                        obj.transformToGlobalCoordinates(currentPose);
                    }

                    for (TrackedObject obj : trackedObjects) {
                        if (fusionSlam.isNewLandmark(obj)) {
                            LandMark newLandMark = new LandMark(obj.getId(),obj.getDescription());
                            fusionSlam.addLandMark(newLandMark);
                            fusionSlam.updateLandMark(obj);
                            StatisticalFolder.getInstance().addLandmark();

                        }
                        else {
                            fusionSlam.updateLandMark(obj);
                        }

                    }
                    this.complete(event, true);
                } catch (Exception e) {
                    this.complete(event, false);
                }
            }
        });

        this.subscribeEvent(PoseEvent.class, (PoseEvent event) -> {
            synchronized (fusionSlam) {
                try {
                    Pose newPose = event.getPose();
                    fusionSlam.addPose(newPose);
                    this.complete(event, true);
                } catch (Exception e) {
                    this.complete(event, false);
                }
            }

        });

        this.subscribeBroadcast(TickBroadcast.class, (TickBroadcast tick) -> {
            if (fusionSlam.getSensorCount()<=0) {
                StatisticalFolder.getInstance().createOutputFile(configPath, null, null, null);
                this.sendBroadcast(new TerminatedBroadcast());
                terminate();

            }
        });
        this.subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast term) -> {
            StatisticalFolder.getInstance().createOutputFile(configPath, null, null, null);
            terminate();
        });
        this.subscribeBroadcast(CrashedBroadcast.class, (CrashedBroadcast crash) -> {
            StatisticalFolder.getInstance().createOutputFileError(configPath
                    , crash.getErrorDescription(), crash.getSensorCausingError(),
                    crash.getCloudPoints(), crash.getPoseList());
            terminate();
        });
    }
}

