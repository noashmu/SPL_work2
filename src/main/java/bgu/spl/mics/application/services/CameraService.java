package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.MicroService;


import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.*;

import java.util.ArrayList;
import java.util.List;


/**
 * CameraService is responsible for processing data from the camera and
 * sending DetectObjectsEvents to LiDAR workers.
 * 
 * This service interacts with the Camera object to detect objects and updates
 * the system's StatisticalFolder upon sending its observations.
 */
public class CameraService extends MicroService {
    private Camera camera;

    /**
     * Constructor for CameraService.
     *
     * @param camera The Camera object that this service will use to detect objects.
     */
    public CameraService(Camera camera) {
        super("CameraService");
        this.camera = camera;
    }

    /**
     * Initializes the CameraService.
     * Registers the service to handle TickBroadcasts and sets up callbacks for sending
     * DetectObjectsEvents.
     */
    @Override
    protected void initialize() {

        this.subscribeBroadcast(TickBroadcast.class, (TickBroadcast tick) -> {
            int currentTick = tick.getTick();

            if (camera.isActive()) {
                if (camera.shouldSendEvent(currentTick)) {
                    DetectObjectsEvent detectObjectsEvent = camera.createDetectObjectsEvent(currentTick);
                    camera.setCountDetected(camera.getCountDetected()-detectObjectsEvent.getDetectedObjects().size());

                    if (detectObjectsEvent != null) {
                        StatisticalFolder.getInstance().addDetectedObjects(detectObjectsEvent.getDetectedObjects().size());
                        sendEvent(detectObjectsEvent);
                        StatisticalFolder.getInstance().addTrackedObjects(detectObjectsEvent.getDetectedObjects().size());
                    }
                    if (camera.getCountDetected()<=0)
                    {
                        camera.TurnOffCamera();
                        FusionSlam.getInstance().decreseSensorCount();
                    }
                }
            }

                if (camera.detectError(currentTick)) {
                    sendBroadcast(new CrashedBroadcast(camera.errorDescription(currentTick),"Camera"
                        ,camera.getDetectedObject(currentTick),
                        LiDarDataBase.getInstance().getCloudPoints(camera.getLastDetectedObject(currentTick)),
                        FusionSlam.getInstance().getPoses()));
                terminate();
            }
        });

        this.subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast term) -> {
            sendBroadcast(new TerminatedBroadcast());
           //  terminate();
        });

        this.subscribeBroadcast(CrashedBroadcast.class, (CrashedBroadcast crash) -> {
            terminate();
        });
   }
}

